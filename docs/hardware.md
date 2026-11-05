# Hardware und fehlende Angaben

Stand 01.11.2026. SPI-Verdrahtung geplant; keine konkrete CC1101-Verdrahtung elektrisch bestätigt.

| Teil | Bekannt | Noch erforderlich |
| --- | --- | --- |
| Radio | EKIY KK3, Android 10 laut Benutzerangabe | API-Version am Gerät bestätigen, USB-Host-Unterstützung, Anschluss, Strombudget, Standby-Verhalten |
| Controller | Fotos: S2 Mini V1.0.0, USB-C; Verkäufer: ESP32-S2FN4R2 | Hersteller, passender Schaltplan, USB-Beschaltung und Chip-/Flashbestätigung am Gerät |
| Funkmodul | Auf Produktbild E07-M1101D V2.0 / 433M | Originaldatenblatt, bestätigte acht Pins, Versorgung und Antenne |
| Handsender | Kopiersender hat am eigenen Tor funktioniert | Modell, Frequenz, Modulation, wiederholte Aufnahmen und praktischer Festcodenachweis |

CC1101 nur mit 3,3 V und passenden Signalpegeln versorgen. Kein direkter Anschluss
an 12 V. SPI-GPIOs sind unten als Planung dokumentiert; GDO bleibt offen. USB D−/D+ des ESP32-S2 sind GPIO19/20;
diese bleiben reserviert. Das ersetzt keine Prüfung des konkreten Boards.
USB-Stromversorgung, Antennenposition und Temperatur müssen am Einbauort geprüft werden.

## Verdrahtungsplanung vom 01.11.2026

Die CC1101-Pinnummern stammen aus der vom Benutzer bereitgestellten Tabelle
und dem Platinenfoto: Bauteilseite, Anschluss oben, Antenne unten; oberste
Reihe von links 7/5/3/1, unterste Reihe 8/6/4/2. Keine elektrische Prüfung
oder Bestätigung durch ein Originaldatenblatt des konkreten Moduls.

| CC1101 | S2 Mini | Grundlage |
| --- | --- | --- |
| 1 GND | GND | Versorgung |
| 2 VCC | 3V3 | Kein Anschluss an VBUS |
| 3 GDO0 | Offen | Noch keine GPIO-Zuweisung |
| 4 CSN | GPIO12 | LOLIN-S2-Mini-SPI-Standard |
| 5 SCK | GPIO7 | LOLIN-S2-Mini-SPI-Standard |
| 6 MOSI | GPIO11 | LOLIN-S2-Mini-SPI-Standard |
| 7 MISO/GDO1 | GPIO9 | LOLIN-S2-Mini-SPI-Standard |
| 8 GDO2 | Offen | Noch keine GPIO-Zuweisung |

Referenzen: [Espressif-Boarddefinition](https://github.com/espressif/arduino-esp32/blob/master/variants/lolin_s2_mini/pins_arduino.h),
[WEMOS-Referenzschaltplan V1.0.0](https://www.wemos.cc/en/latest/_static/files/sch_s2_mini_v1.0.0.pdf).
Die Übereinstimmung des gelieferten Boards mit dem Referenzschaltplan ist
unbestätigt. Die Firmware hält diese SPI-Planung in `main/board_config.h`
bereit, verwendet sie aber noch nicht für Hardwarezugriffe. GDO bleibt
unzugewiesen; HELLO/STATUS meldet weiterhin NOT_CONFIGURED.

Herstellerquellen:
- [ESP32-S2 USB, ESP-IDF 5.4.2](https://docs.espressif.com/projects/esp-idf/en/v5.4.2/esp32s2/api-reference/peripherals/usb_device.html)
- [ESP32-S2 RMT](https://docs.espressif.com/projects/esp-idf/en/v5.4.2/esp32s2/api-reference/peripherals/rmt.html)
- [TI CC1101 Datenblatt](https://www.ti.com/lit/ds/symlink/cc1101.pdf)
- [Android USB Host API](https://developer.android.com/develop/connectivity/usb/host)

RMT-Puffer, asynchroner Funkmodus, konkrete Frequenz und am Einsatzort geltende
Funkbedingungen sind vor der Funkimplementierung anhand offizieller Quellen zu prüfen.

## Controller: Fotos und Verkäuferangaben vom 31.10.2026

Auf den bereitgestellten Fotos beider Seiten sind der Aufdruck **S2 Mini V1.0.0**,
ein USB-C-Anschluss und die Tastenbeschriftungen RST und 0 erkennbar. Die
Chipbeschriftung ist nicht ausreichend lesbar. Ein bestimmter Boardhersteller
oder die Übereinstimmung mit dessen Schaltplan ist damit nicht bestätigt.

Laut übermittelter Artikelbeschreibung: ESP32-S2FN4R2, 27 digitale I/O-Pins,
ADC, DAC, I2C, SPI, UART und USB OTG; Abmessungen 35 × 25 × 4 mm, Gewicht 5 g.
Das sind Verkäuferangaben, keine Mess- oder Funktionsnachweise.

Espressif spezifiziert den **ESP32-S2FN4R2 mit 4 MB Flash und 2 MB PSRAM**:
[ESP32-S2-Datenblatt](https://www.espressif.com/sites/default/files/documentation/esp32-s2_datasheet_en.pdf).
Diese Speicherbestückung ist für das gelieferte Board zu erwarten, sofern die
Chipangabe stimmt. Bestätigung durch Geräteabfrage steht aus. Die bisherigen
Firmware-Builds verwenden weiterhin den konservativen 2-MB-Flashdefault; PSRAM
wird für HELLO/STATUS nicht benötigt und nicht aktiviert.

Nächster Hardware-Schritt: Board ohne CC1101 mit einem USB-Datenkabel am PC
anschliessen und USB-Erkennung sowie Chip/Flash lesend prüfen. Dies ist noch
kein Flashauftrag. USB-Funktion am Radio und Versorgung bleiben separat offen.

## Lesende Geräteprüfung am 31.10.2026

Nach manuellem Eintritt in den ROM-Downloadmodus (0 halten, RST kurz drücken,
0 loslassen) erkennt Windows den Controller als COM3, VID 303A / PID 0002.
Der CDC-Port meldet Status OK. Ein weiteres Interface MI_02 meldet unter Windows
Status Error; dessen Ursache wurde nicht untersucht und es war für diese
serielle Abfrage nicht erforderlich.

`esptool` 4.12.0 wurde mit `--chip esp32s2 --port COM3 --before no_reset
--after no_reset --no-stub flash_id` ausgeführt (Exitcode 0). Geräteantwort:

- Chip: ESP32-S2FNR2, Revision v1.0 (Bezeichnung der Werkzeugausgabe).
- Embedded Flash: 4 MB; zusätzlich per Flash-ID als 4 MB erkannt.
- Embedded PSRAM: 2 MB laut Chipmerkmalen, kein separater RAM-Funktionstest.
- Quarz: 40 MHz; USB-Modus: USB-OTG.
- Flash-Hersteller-ID 20, Geräte-ID 4016, Quad-SPI.

Kein Flash geschrieben oder gelöscht, kein Stub in RAM geladen, kein Funkbefehl.
Das Board blieb im ROM-Bootloader. Damit sind PC-USB-Datenverbindung und
Chip-/Flashabfrage nachgewiesen. Die eigene Firmware, HELLO/STATUS über USB,
das Radio und die CC1101-Verdrahtung sind damit noch nicht abgenommen.

## Eigene Firmware am PC, 31.10.2026

Nach freigegebenem Flashen von Firmware 0.1.0 wurde der native USB-C-Datenpfad
mit HELLO/STATUS erfolgreich geprüft: VID 303A / PID 4001, CDC COM4.
Geräte-ID `s2-cc8da28c7fa6`. Beide Diagnosebefehle und erneutes Öffnen des Ports
funktionieren. Der ROM-Port COM3 und der Firmware-Port COM4 sind unterschiedliche
USB-Identitäten desselben Boards. Details im Prüfplan; Radioabnahme bleibt offen.

## Radiomodell, Benutzerangabe vom 31.10.2026

Das vorgesehene Autoradio ist ein **EKIY KK3**. Die Modellangabe allein bestätigt
weder die tatsächliche Android/API-Version noch USB-Host-Zugriff, Strombudget
oder Standby-Verhalten. Diese Eigenschaften sind am konkreten Gerät zu prüfen.
Nächste Angabe: Android-Version aus Einstellungen → Über das Gerät bzw.
Systeminformationen; die App benötigt derzeit mindestens Android 8 / API 26.

Benutzerangabe ergänzt: Headunit mit **Android 10** (entspricht bei regulärer
Android-Version API 29). Damit ist die konfigurierte Mindestversion Android 8 /
API 26 erfüllt. Noch keine unabhängige API-Abfrage, APK-Installation oder
USB-Abnahme am Radio durchgeführt.

## Radio-USB-Test am 01.11.2026

Das Benutzerfoto der Diagnose-App zeigt Android 10 / API 29,
USB-Host-Unterstützung und den ESP als 303A:4001 mit erteilter Freigabe.
HELLO/STATUS funktionieren: Geräte-ID `s2-cc8da28c7fa6`, Zustand IDLE,
Funk nicht eingerichtet. Der Benutzer bestätigt Wiederverbindung nach
Abziehen/Wiederanstecken, Radio-Neustart und Standby/Aufwachen.
Damit sind die oben historisch offenen USB-Grundtests am Radio erfolgt;
Strombudget, Langzeitstabilität und elektrische Eigenschaften bleiben ungeprüft.
Die Funkhardware ist laut Benutzer bestellt und noch nicht eingetroffen.
CC1101-Verdrahtung sowie Funk- und Torabnahme bleiben offen.
