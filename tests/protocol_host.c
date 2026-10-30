#include "protocol.h"
#include <stdio.h>
static void emit(const char *s, size_t n, void *ctx) { (void)ctx; fwrite(s,1,n,stdout); }
int main(void) {
    protocol_t p; protocol_init(&p,"simulation-device","simulation-session");
    int c;
    while ((c=getchar()) != EOF) { unsigned char b=(unsigned char)c; protocol_feed(&p,&b,1,emit,NULL); }
    return 0;
}
