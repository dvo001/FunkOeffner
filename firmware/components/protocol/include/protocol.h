#pragma once
#include <stddef.h>
#include <stdbool.h>
#define PROTOCOL_MAX_LINE 1024
/* Single owner only. Emit must consume the complete response synchronously. */
typedef void (*protocol_emit)(const char *, size_t, void *);
typedef struct {
    char line[PROTOCOL_MAX_LINE + 1];
    size_t used;
    bool dropping;
    const char *device_id;
    const char *session;
} protocol_t;
void protocol_init(protocol_t *, const char *, const char *);
void protocol_feed(protocol_t *, const unsigned char *, size_t, protocol_emit, void *);
