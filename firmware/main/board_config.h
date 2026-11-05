#ifndef FUNKOFFNER_BOARD_CONFIG_H
#define FUNKOFFNER_BOARD_CONFIG_H

/* Geplante LOLIN-S2-Mini-SPI-Belegung; am gelieferten Board ungeprüft.
 * Nur Konfiguration: kein Treiber und keine GPIO-Initialisierung.
 * Quelle: Espressif arduino-esp32, variants/lolin_s2_mini/pins_arduino.h.
 */
enum {
    BOARD_CC1101_SCK_GPIO = 7,
    BOARD_CC1101_MISO_GPIO = 9,
    BOARD_CC1101_MOSI_GPIO = 11,
    BOARD_CC1101_CSN_GPIO = 12,
    BOARD_CC1101_GDO0_GPIO = -1,
    BOARD_CC1101_GDO2_GPIO = -1,
    BOARD_CC1101_ENABLED = 0
};

/* Erste Stufe bleibt ausschliesslich HELLO/STATUS. */
_Static_assert(BOARD_CC1101_ENABLED == 0,
               "CC1101 benoetigt eine gesonderte Implementierung und Abnahme");

#endif
