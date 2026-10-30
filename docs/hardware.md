# Hardware und fehlende Angaben

Stand 30.10.2026. Keine konkrete Verdrahtung bestätigt.

| Teil | Bekannt | Noch erforderlich |
| --- | --- | --- |
| Radio | Eigenes Android vorgesehen | Hersteller, Modell, Android/API, USB-Host-Unterstützung, Anschluss, Strombudget, Standby-Verhalten |
| Controller | ESP32-S2 Mini vorgesehen | Hersteller, genaue Revision, Fotos beider Seiten, Schaltplan, USB-Beschaltung, Flashgrösse |
| Funkmodul | Auf Produktbild E07-M1101D V2.0 / 433M | Originaldatenblatt, bestätigte acht Pins, Versorgung und Antenne |
| Handsender | Kopiersender hat am eigenen Tor funktioniert | Modell, Frequenz, Modulation, wiederholte Aufnahmen und praktischer Festcodenachweis |

CC1101 nur mit 3,3 V und passenden Signalpegeln versorgen. Kein direkter Anschluss
an 12 V. Noch keine SPI/GDO-GPIOs vergeben. USB D−/D+ des ESP32-S2 sind GPIO19/20;
diese bleiben reserviert. Das ersetzt keine Prüfung des konkreten Boards.
USB-Stromversorgung, Antennenposition und Temperatur müssen am Einbauort geprüft werden.

Herstellerquellen:
- [ESP32-S2 USB, ESP-IDF 5.4.2](https://docs.espressif.com/projects/esp-idf/en/v5.4.2/esp32s2/api-reference/peripherals/usb_device.html)
- [ESP32-S2 RMT](https://docs.espressif.com/projects/esp-idf/en/v5.4.2/esp32s2/api-reference/peripherals/rmt.html)
- [TI CC1101 Datenblatt](https://www.ti.com/lit/ds/symlink/cc1101.pdf)
- [Android USB Host API](https://developer.android.com/develop/connectivity/usb/host)

RMT-Puffer, asynchroner Funkmodus, konkrete Frequenz und am Einsatzort geltende
Funkbedingungen sind vor der Funkimplementierung anhand offizieller Quellen zu prüfen.
