# Prüfplan und Ergebnisse

## Hauptseite und Einstellungen, App 0.1.3, 01.11.2026

- Windows `assembleDebug lintDebug`: erfolgreich, 0 Fehler, 25 Hinweise.
- APK im Android-10-Tablet-Emulator installiert und gestartet.
- UI-Automator-Prüfung: Hauptseite zeigt «Tor öffnen», keine Diagnosebereiche;
  Antippen von «Nicht verbunden · Einstellungen» öffnet die Einstellungen
  mit USB-Verbindung. Android-Zurück führt wieder zur Hauptseite.
- Separater Launcher-Einstieg `OpenGateActivity` zeigt ebenfalls die normale
  Hauptseite. Die Tortaste ist weiterhin deaktiviert, kein Sendepfad vorhanden.
- USB-Verhalten mit Hardware und Darstellung am EKIY KK3 für diese Version
  noch nicht geprüft. Der Seitenwechsel selbst bleibt in derselben Activity.

## CarWebGuru-Vorbereitung, App 0.1.2, 01.11.2026

- Windows `assembleDebug lintDebug` erfolgreich: 0 Fehler, 22 Lint-Hinweise.
- Fertige APK mit `aapt dump xmltree` geprüft: separater Launcher-Einstieg
  `OpenGateActivity` und statischer Shortcut mit passendem Ziel vorhanden.
- Kein Emulator/Android-Gerät beim Prüfen über ADB verbunden; keine
  Laufzeitprüfung von 0.1.2 und keine CarWebGuru-Abnahme durchgeführt.
- Testfolge für die Tastenbelegung steht in `docs/carwebguru.md`.
- Firmware und USB-Protokoll unverändert; kein Sendepfad hinzugefügt.

## Android-App 0.1.1, 01.11.2026

Die Diagnoseansicht wurde mit grossen Bedienelementen, getrennten Bereichen
für USB/Funk/Diagnose und Zeitstempel der letzten Statusantwort überarbeitet.
Verspätete Anzeigeänderungen alter Verbindungsgenerationen werden verworfen;
Verbindungsaufbau erfolgt nur bei fortgesetzter Activity. USB-Protokoll und
Firmwareumfang bleiben HELLO/STATUS.

Softwareprüfung: Windows `gradlew.bat --no-daemon assembleDebug lintDebug`
erfolgreich. Lint: 0 Fehler, 23 Hinweise (Textressourcen, fehlendes App-Symbol,
Backup-Konfiguration). Keine neue Geräte- oder Radioabnahme dieser App-Version.

Am EKIY KK3 noch prüfen:

1. APK 0.1.1 installieren und Lesbarkeit im Hoch-/Querformat prüfen; Inhalte
   müssen auch auf kleiner Höhe durch Scrollen erreichbar sein.
2. Mit vorhandener Firmware: Gerät, Firmwareversion und aktuelle Uhrzeit der
   Statusantwort erscheinen; Funk bleibt «Nicht eingerichtet · IDLE».
3. USB abziehen: Anzeige «Getrennt», alte Geräteantwort gelöscht. Wieder
   anschliessen und Freigabe prüfen; Statusantworten müssen neu eintreffen.
4. App verlassen und fortsetzen sowie Standby/Aufwachen prüfen; keine alte
   Verbunden-Anzeige nach einer Trennung. Bei Timeout Fehleranzeige und
   erneute Prüfung über «USB erneut prüfen» testen.

Keine Lern-/Sendebefehle, kein Flashen und keine Funkübertragung.

## USB-Diagnose am 01.11.2026

- PC: COM4 mit VID 303A / PID 4001 erreichbar. `tools/usb-smoke.py`
  bestand HELLO/STATUS, fragmentierte Anfrage und zweimaliges Öffnen.
- Benutzer: App am Radio installiert; beim Antippen der USB-Schaltfläche
  keine sichtbare Änderung. Laut Benutzer dasselbe Kabel wie beim PC-Test.
- App ergänzt: sichtbarer Prüfzähler, Android/API-Version, gemeldete
  USB-Host-Unterstützung, USB-Geräteliste mit VID/PID und Freigabestatus,
  sowie Meldungen für Freigabe, Verbindungsaufbau und Verbindungsfehler.
- Windows `assembleDebug`: bestanden. Keine Firmwareänderung, kein Flashen, kein Funk.
- Radio, Foto des Benutzers: Diagnose-APK installiert, Android 10 / API 29,
  USB-Host gemeldet, 303A:4001 mit Freigabe erkannt; Geräte-ID
  `s2-cc8da28c7fa6`, HELLO/STATUS erfolgreich, Zustand IDLE.
- Benutzer bestätigt: Nach Abziehen und Wiederanstecken wechselt die Anzeige
  von Getrennt zurück auf Verbunden. Wiederverbindung nach Radio-Neustart
  sowie Standby/Aufwachen funktioniert ebenfalls. Anzahl Zyklen und Dauer
  wurden nicht erfasst; keine Langzeit- oder elektrische Abnahme.
- Funkhardware laut Benutzer bestellt, noch nicht eingetroffen. CC1101-,
  Funk- und Tortests bleiben offen. Verweigerte Freigabe, mehrere passende
  Geräte sowie Fehler- und Timeoutfälle am Radio bleiben ebenfalls offen.

Stand 01.11.2026.

| Prüfung | Ergebnis |
| --- | --- |
| C-Protokollkern mit GCC, Warnungen als Fehler | Bestanden |
| 8 Python-Unittests gegen denselben C-Kern | Bestanden am 31.10.2026 |
| Android APK-Build | Bestanden am 31.10.2026: Windows, JDK 21.0.7, Gradle 8.9, API 35, Build-Tools 34.0.0 |
| ESP-IDF Firmware-Build | Bestanden: Windows, ESP-IDF 5.4.2, GCC 14.2.0, Ziel esp32s2 |
| Radio-USB, erteilte Freigabe, HELLO/STATUS | Bestanden laut Foto am 01.11.2026 |
| Abziehen/Wiederanstecken, Radio-Neustart, Standby/Aufwachen | Bestanden laut Benutzer am 01.11.2026 |
| Funk, Strombudget, Torreaktion, Fahrzeugzyklen | Nicht durchgeführt |

`./tools/check.sh` prüft HELLO/STATUS, byteweise Fragmentierung, mehrere Nachrichten,
Duplikate gesperrter Befehle, falsche Version, ungültiges JSON, doppelte Schlüssel,
Überlängen und Wiederherstellung der Parserbereitschaft. Es prüft weder TinyUSB
noch Android noch elektrische Eigenschaften.

## USB-Abnahme nach bestätigter Hardware

1. Beide Zielbuilds erstellen und Versionen/Lockdatei dokumentieren.
2. Ohne angeschlossenes CC1101 Firmware nach Flashauftrag installieren.
3. App am Radio starten, USB verbinden, Freigabe erlauben/ablehnen testen.
4. HELLO und STATUS müssen stimmen; keine freien Logs im JSON-Kanal.
5. Kabel wiederholt abziehen, Radio neu starten und Standby/Aufwachen prüfen.
6. Mehrere CDC-Geräte, verweigerte Freigabe, Protokollfehler und Antwortverlust testen.
7. Es darf keine Funkübertragung geben. Versorgung und Verbindungsstabilität messen.

## Spätere Funk- und Profilabnahme

Erst nach Pinbestätigung: CC1101-SPI nachweisen, bekannte Signale mehrfach erfassen,
Rauschen/Trunkierung erkennen, bewusst angeforderte Wiedergabe testen. Aufnahme
beweist keinen Festcode. Profile nach Neustart prüfen; Schreibunterbrechung,
Integritätsfehler, Revisionskonflikte und volle Speicher testen. Doppelte SEND-IDs,
Reset, Timeout und Wiederverbindung dürfen keine zweite Aussendung verursachen.
Ergebnisse der Torreaktion und der Fahrzeugzyklen separat protokollieren.

## Windows-Build am 30.10.2026

`tools/build-firmware.ps1` lief vollständig mit Exitcode 0 durch. Erzeugt wurden
`firmware/build/funkoffner.bin`, `funkoffner.elf`, Bootloader und Partitionstabelle.
App-Image: 0x3dc60 Bytes; Standard-App-Partition: 0x100000 Bytes, 76 % frei.
ESP-IDF: v5.4.2, Commit f5c3654a1c2d2a01f7f67def7a0dc48e691f63c0.
USB-Abhängigkeiten: esp_tinyusb 1.7.6, TinyUSB 0.21.0~2; Integrität und Versionen
stehen in `firmware/dependencies.lock`. Keine Firmware-Quellcodeänderung nötig.

Der EIM-Installer hatte im Xtensa- und RISC-V-ZIP Verzeichnisse als leere Dateien
entpackt. Die vollständige Struktur wurde aus den vom Installer geprüften Archiven
wiederhergestellt. Der anschliessende vollständige ESP32-S2-Build bestätigt den
benötigten Compiler. Der mitinstallierte RISC-V-Compiler ist kein Projektziel.

Die generierte Flashkonfiguration verwendet Entwicklungsdefaults, darunter 2 MB
Flash. Diese sind keine Bestätigung des konkreten Mini-Boards. Vor einem späteren
Flashauftrag sind dessen Flashgrösse und USB-Anschluss zu bestätigen.
Hardware-, USB- und Funkabnahmen bleiben offen.

## Fortsetzung am 31.10.2026

`tools/check.sh` erneut erfolgreich: C11-Build mit Warnungen als Fehler und acht
Python-Tests. Neue Regressionstests prüfen NUL- und Nicht-ASCII-JSON-Escapes in
Befehlen, IDs, zusätzlichen Werten und Schlüsseln, gültige ASCII-Escapes,
wörtliche Backslashes sowie die Parserbereitschaft nach einem Fehler.
Damit werden insbesondere durch eingebettete NUL-Zeichen verkürzte Befehle und
Request-IDs verhindert. Der Funktionsumfang bleibt auf HELLO/STATUS begrenzt.

In WSL ist kein Java-Befehl verfügbar. Unter Windows wurde ein Microsoft JDK 21
gefunden, jedoch kein Android SDK am üblichen Benutzerpfad und kein gesetzter
SDK-Pfad. Ein Android-Build und die USB-Abnahme am Radio bleiben offen.

Der aktualisierte ESP32-S2-Firmware-Build mit ESP-IDF 5.4.2 unter Windows
bestand ebenfalls (Exitcode 0). App-Image: 0x3dd30 Bytes, 76 % der
Standard-App-Partition frei. Es wurde ausschliesslich gebaut, nicht geflasht.

## Android-Build am 31.10.2026

Nach Einrichtung von Android Studio wurde der lokale SDK-Pfad in
`android/local.properties` mit Schrägstrichen korrigiert. Gradle hat mit den
bereits akzeptierten SDK-Lizenzen Platform 35 (Revision 2) und Build-Tools 34.0.0
nachinstalliert. `gradlew.bat --no-daemon assembleDebug` bestand unter Windows
mit Microsoft JDK 21.0.7, Gradle 8.9, Android-Gradle-Plugin 8.7.3 und Kotlin
2.0.21 (Exitcode 0, 33 Aufgaben ausgeführt).

Artefakt: `android/app/build/outputs/apk/debug/app-debug.apk`.
Es gab Warnungen zum SDK-XML-Format und zur veralteten Android-Methode
`getParcelableExtra`; der Build war erfolgreich. Die APK wurde nicht auf einem
Android-Gerät installiert oder gestartet. USB-Freigabe und Kommunikation am
Radio bleiben ungeprüft. Die obige fehlende SDK-Einrichtung ist damit erledigt.

## PC-USB-/Chipprüfung am 31.10.2026

S2 Mini V1.0.0 im manuellen ROM-Downloadmodus an Windows: CDC COM3 erkannt,
`esptool flash_id` ohne Stub erfolgreich. ESP32-S2 Revision v1.0, 4 MB Flash
und 2 MB eingebettetes PSRAM laut Chipmerkmalen bestätigt; kein RAM-Test.
Details und das separate fehlerhafte USB-Interface stehen in `docs/hardware.md`.
Kein Flashvorgang. Firmware-HELLO/STATUS und Radio-USB bleiben offen.

## Erster Flash und Firmware-USB-Test am PC, 31.10.2026

Nach ausdrücklichem Weiterauftrag zum angekündigten Flashen wurde COM3 erneut
lesend geprüft: identischer Controller, ESP32-S2 Revision v1.0, 4 MB Flash.
Danach wurden Bootloader, Partitionstabelle und Firmware 0.1.0 aus dem geprüften
ESP-IDF-Build mittels `write_flash @flash_args` geschrieben (Exitcode 0).
Alle drei Hashprüfungen bestanden. Keine vollständige Flashlöschung; weiterhin
2-MB-Entwicklungskonfiguration auf dem bestätigten 4-MB-Chip.

Nach automatischem Reset: VID 303A / PID 4001, Windows CDC **COM4**.
`tools/usb-smoke.py --port COM4` bestand mit pyserial aus der IDF-Umgebung:

- HELLO: Gerät `s2-cc8da28c7fa6`, Firmware 0.1.0, Protokoll 1,
  ausschliesslich Fähigkeiten HELLO und STATUS.
- STATUS: IDLE, NOT_CONFIGURED, transmit_enabled=false, profile_revision=0.
- Byteweise geschriebene HELLO-Anfrage korrekt beantwortet.
- Port zweimal geöffnet/geschlossen; Geräte-ID und Sitzung blieben gleich,
  beide HELLO/STATUS-Paare korrekt zugeordnet.

Dies ist ein echter Firmware-USB-Test am Windows-PC. Er ersetzt weder physisches
Abziehen/Wiederanstecken noch Reset-/Stromzyklustests oder Radio-/Android-Abnahme.
Es wurden ausschliesslich HELLO und STATUS gesendet, keine Funkbefehle.
Eine physische Messung auf Funkabstrahlung wurde nicht durchgeführt.
