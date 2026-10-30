#include <stdio.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "tinyusb.h"
#include "tusb_cdc_acm.h"
#include "esp_mac.h"
#include "esp_random.h"
#include "protocol.h"
static protocol_t protocol;
static char device[32], session[33];
static void output(const char *s, size_t n, void *ctx) {
    (void)ctx;
    tinyusb_cdcacm_write_queue(TINYUSB_CDC_ACM_0, (const uint8_t *)s, n);
    tinyusb_cdcacm_write_flush(TINYUSB_CDC_ACM_0, 0);
}
static void received(int itf, cdcacm_event_t *event) {
    (void)event;
    uint8_t data[64]; size_t count = 0;
    do {
        if (tinyusb_cdcacm_read(itf, data, sizeof(data), &count) != ESP_OK) break;
        protocol_feed(&protocol, data, count, output, NULL);
    } while (count == sizeof(data));
}
void app_main(void) {
    uint8_t mac[6]; ESP_ERROR_CHECK(esp_efuse_mac_get_default(mac));
    snprintf(device, sizeof(device), "s2-%02x%02x%02x%02x%02x%02x", mac[0],mac[1],mac[2],mac[3],mac[4],mac[5]);
    uint32_t random[4]; esp_fill_random(random, sizeof(random));
    snprintf(session, sizeof(session), "%08lx%08lx%08lx%08lx", (unsigned long)random[0], (unsigned long)random[1], (unsigned long)random[2], (unsigned long)random[3]);
    protocol_init(&protocol, device, session);
    const tinyusb_config_t usb = {0};
    ESP_ERROR_CHECK(tinyusb_driver_install(&usb));
    const tinyusb_config_cdcacm_t cdc = {
        .usb_dev = TINYUSB_USBDEV_0, .cdc_port = TINYUSB_CDC_ACM_0,
        .rx_unread_buf_sz = 256, .callback_rx = received
    };
    ESP_ERROR_CHECK(tusb_cdc_acm_init(&cdc));
}
