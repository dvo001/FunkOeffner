# FunkOffner 0.1.0

Erste Projektstufe gemäss CODEX-PROJEKTAUFTRAG.md: native Kotlin-App und
ESP32-S2-USB-Firmware mit HELLO/STATUS. Keine Funkfunktion implementiert.
Kein WLAN, Bluetooth, Internetzugriff oder Cloud-Dienst in der App.

## Softwaretests ohne Hardware

Voraussetzungen: Python 3, C11-Compiler und Bash.

```sh
./tools/check.sh
printf '{"v":1,"id":"demo","cmd":"HELLO"}\n' | ./build/protocol-host
```

Der Host-Prozess verwendet denselben Protokollkern wie die Firmware, aber eine
explizite Simulationsidentität. Er emuliert keinen USB-Controller und kein Radio.

## Android bauen

Voraussetzungen: JDK 17, Android SDK Platform 35, akzeptierte SDK-Lizenzen.
SDK-Pfad über ANDROID_HOME oder android/local.properties (`sdk.dir=...`) setzen.
Android 8/API 26 ist eine vorläufige Entwicklungsuntergrenze; das Radio ist unbekannt.
Android Views vermeiden zusätzliche UI-Abhängigkeiten.

```sh
cd android
./gradlew --no-daemon assembleDebug
```

APK: `android/app/build/outputs/apk/debug/app-debug.apk`.
Auf dem Radio manuell installieren oder nach ausdrücklich freigegebener
Geräteinstallation `adb install -r <APK>` verwenden. App öffnen, genau ein
passendes Entwicklungsgerät anschliessen und USB-Freigabe erteilen.
Es gibt noch keine Favoriten, Lernverwaltung oder Profilverwaltung.

## Firmware bauen

ESP-IDF **v5.4.2**, Ziel **esp32s2**, esp_tinyusb **1.7.6**.
ESP-IDF gemäss Herstelleranleitung separat installieren und export.sh laden.

```sh
cd firmware
idf.py set-target esp32s2
idf.py build
```

Direkte Versionen sind fixiert; `firmware/dependencies.lock` hält auch die
aufgelösten transitiven Abhängigkeiten fest (TinyUSB 0.21.0~2).
Für Windows/VS Code siehe [Entwicklungsumgebung](docs/development-windows.md).
Der Arbeitsbereich `FunkOffner.code-workspace` enthält eine Build-Aufgabe für
`Strg+Shift+B`.
Nach Bestätigung von Board, USB-Anschluss und ausdrücklichem Flashauftrag:
`idf.py -p <PORT> flash`. Am 31.10.2026 wurde die Diagnosefirmware nach Auftrag erstmals auf dem
bestätigten S2 Mini geflasht und HELLO/STATUS am Windows-PC geprüft.
Logs bleiben auf UART; USB CDC ist ausschliesslich für JSON vorgesehen.

## Stand und nächste Abnahme

Siehe [Hardware](docs/hardware.md), [Architektur](docs/architecture.md),
[USB-Protokoll](docs/usb-protocol.md), [Prüfplan](docs/test-plan.md) und
[Senderstatus](docs/supported-remotes.md).

Am 31.10.2026: acht Softwaretests bestanden. Der ESP32-S2-Firmware-Build
mit ESP-IDF 5.4.2 unter Windows ist erfolgreich. Auch die Android-Debug-APK
wurde unter Windows erfolgreich gebaut (API 35, Build-Tools 34.0.0).
HELLO/STATUS über echtes USB am Windows-PC sind geprüft.
USB am Radio, Versorgung, Funk und Torreaktion sind ungeprüft. Das ist ein Entwicklungsgerüst,
keine bereits abgenommene Garagentorsteuerung.

USB-Diagnose am PC (Python mit `pyserial`, tatsächlichen COM-Port einsetzen):

```sh
python tools/usb-smoke.py --port COM4
```

Der Test verwendet ausschliesslich HELLO/STATUS, öffnet den Port zweimal und
prüft fragmentierte Anfragen. Er flasht nicht und setzt das Board nicht zurück.
