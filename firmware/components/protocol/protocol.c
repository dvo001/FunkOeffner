#include "protocol.h"
#include "cJSON.h"
#include <string.h>
#include <stdlib.h>
static void reply(protocol_t *p, const char *id, const char *cmd, const char *error,
                  protocol_emit emit, void *ctx) {
    cJSON *r = cJSON_CreateObject();
    if (!r) return;
    cJSON_AddNumberToObject(r, "v", 1);
    if (id) cJSON_AddStringToObject(r, "id", id); else cJSON_AddNullToObject(r, "id");
    cJSON_AddStringToObject(r, "session", p->session);
    if (error) {
        cJSON *e = cJSON_AddObjectToObject(r, "error");
        cJSON_AddStringToObject(e, "code", error);
    } else {
        cJSON *v = cJSON_AddObjectToObject(r, "result");
        if (!strcmp(cmd, "HELLO")) {
            cJSON_AddStringToObject(v, "device_id", p->device_id);
            cJSON_AddStringToObject(v, "firmware", "0.1.0");
            cJSON_AddNumberToObject(v, "protocol", 1);
            cJSON *a = cJSON_AddArrayToObject(v, "capabilities");
            cJSON_AddItemToArray(a, cJSON_CreateString("HELLO"));
            cJSON_AddItemToArray(a, cJSON_CreateString("STATUS"));
        } else {
            cJSON_AddStringToObject(v, "state", "IDLE");
            cJSON_AddStringToObject(v, "radio", "NOT_CONFIGURED");
            cJSON_AddBoolToObject(v, "transmit_enabled", 0);
            cJSON_AddNumberToObject(v, "profile_revision", 0);
        }
    }
    char *s = cJSON_PrintUnformatted(r);
    if (s) { emit(s, strlen(s), ctx); emit("\n", 1, ctx); free(s); }
    cJSON_Delete(r);
}
/* Validate escaped code points before cJSON can decode embedded NUL bytes.
 * Skip escaped backslashes so literal text such as "\\\\u0000" stays valid. */
static bool escaped_ascii_only(const char *line, size_t length) {
    for (size_t i = 0; i < length; i++) {
        if (line[i] != '\\' || i + 1 >= length) continue;
        i++;
        if (line[i] != 'u' || i + 4 >= length) continue;
        unsigned value = 0;
        bool valid = true;
        for (size_t j = 1; j <= 4; j++) {
            unsigned char c = (unsigned char)line[i + j];
            unsigned digit;
            if (c >= '0' && c <= '9') digit = c - '0';
            else if (c >= 'a' && c <= 'f') digit = c - 'a' + 10;
            else if (c >= 'A' && c <= 'F') digit = c - 'A' + 10;
            else { valid = false; break; }
            value = value * 16 + digit;
        }
        if (valid && (value == 0 || value > 127)) return false;
    }
    return true;
}
static void handle(protocol_t *p, protocol_emit emit, void *ctx) {
    if (!escaped_ascii_only(p->line, p->used)) {
        reply(p, NULL, NULL, "FRAME_INVALID", emit, ctx); return;
    }
    const char *end = NULL;
    cJSON *r = cJSON_ParseWithLengthOpts(p->line, p->used + 1, &end, 1);
    if (!r || !cJSON_IsObject(r)) {
        cJSON_Delete(r); reply(p, NULL, NULL, "BAD_JSON", emit, ctx); return;
    }
    cJSON *id = cJSON_GetObjectItemCaseSensitive(r, "id");
    cJSON *v = cJSON_GetObjectItemCaseSensitive(r, "v");
    cJSON *cmd = cJSON_GetObjectItemCaseSensitive(r, "cmd");
    const char *error = NULL;
    if (!cJSON_IsString(id) || !id->valuestring[0] || strlen(id->valuestring) > 64)
        error = "BAD_REQUEST";
    else if (!cJSON_IsNumber(v) || v->valuedouble != 1) error = "VERSION_MISMATCH";
    else if (!cJSON_IsString(cmd)) error = "BAD_REQUEST";
    else if (strcmp(cmd->valuestring, "HELLO") && strcmp(cmd->valuestring, "STATUS"))
        error = "UNSUPPORTED_COMMAND";
    /* Ambiguous duplicate keys are rejected, even when values agree. */
    for (cJSON *a = r->child; a; a = a->next)
        for (cJSON *b = a->next; b; b = b->next)
            if (!strcmp(a->string, b->string)) error = "BAD_REQUEST";
    reply(p, cJSON_IsString(id) ? id->valuestring : NULL,
          cJSON_IsString(cmd) ? cmd->valuestring : NULL, error, emit, ctx);
    cJSON_Delete(r);
}
void protocol_init(protocol_t *p, const char *device, const char *session) {
    memset(p, 0, sizeof(*p)); p->device_id = device; p->session = session;
}
void protocol_feed(protocol_t *p, const unsigned char *data, size_t n, protocol_emit emit, void *ctx) {
    for (size_t i = 0; i < n; i++) {
        unsigned char c = data[i];
        if (c == '\n') {
            if (p->dropping) reply(p, NULL, NULL, "FRAME_INVALID", emit, ctx);
            else if (p->used) { p->line[p->used] = 0; handle(p, emit, ctx); }
            p->used = 0; p->dropping = false;
        } else if (!p->dropping) {
            if (p->used == PROTOCOL_MAX_LINE || c == 0 || c >= 128) {
                p->used = 0; p->dropping = true;
            } else p->line[p->used++] = (char)c;
        }
    }
}
