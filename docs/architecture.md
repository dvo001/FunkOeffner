# Architektur

## Implementierte erste Stufe

Android Activity mit grossen Diagnoseelementen, USB-Zugriffsfreigabe und einem
seriellen Hintergrund-Executor. Lebenszykluswechsel schliessen die Verbindung.
Die App 0.1.1 zeigt Verbindung, Funkstatus und USB-Diagnose in getrennten
Bereichen. Geräte-ID, Firmwareversion und Zeitpunkt der letzten Statusantwort
werden nur nach erfolgreicher HELLO/STATUS-Prüfung angezeigt. Beim Trennen
oder einem Fehler wird die aktuelle Statusbestätigung verworfen. Verspätete
UI-Antworten alter Verbindungen werden anhand der Verbindungsgeneration
ignoriert; im Hintergrund werden keine neuen Verbindungen geöffnet.
Beim Fortsetzen oder USB-Anschluss erfolgt eine neue HELLO-Prüfung. Abfragen
werden nicht dauerhaft gespeichert. Identitätsprüfung ist eine Plausibilitätsprüfung,
keine kryptografische Authentifizierung.

USB CDC-ACM verwendet die Android Host API direkt, ohne serielle Fremdbibliothek.
Die Auswahl ist auf Espressifs Entwicklungs-VID/PID 303a:4001 und eindeutige
CDC-Control-/Data-Interfaces begrenzt. Mehrere Kandidaten werden abgelehnt.
Diese VID/PID ist keine Produktionszuteilung und muss am realen Gerät geprüft werden.

Firmware: TinyUSB liefert Bytes an einen begrenzten C-Protokollparser. Geräte-ID
stammt von der werkseitigen MAC, Sitzung wird beim Start zufällig erzeugt.
Keine SPI-, GDO-, RMT- oder Sendefunktion ist vorhanden. Status bleibt IDLE,
Funkstatus NOT_CONFIGURED. cJSON 1.7.19 ist mit MIT-Lizenz mitgeliefert.

## Spätere Stufen

App 0.1.3 stellt mit `OpenGateActivity` einen separaten Launcher-Einstieg
«Tor öffnen» und einen statischen App-Shortcut bereit. Dieser Einstieg öffnet die normale Hauptseite mit einer vorerst deaktivierten
Tortaste. Der Verbindungshinweis führt auf die zweite Seite «Einstellungen»
mit USB-Diagnose und erneuter Verbindungsprüfung. Der Seitenwechsel bleibt
innerhalb derselben Activity und unterbricht die USB-Verbindung nicht.
Die Android-Zurücktaste führt von Einstellungen zur Hauptseite.
Der Launcher-Einstieg besitzt selbst weder USB-Zugriff noch einen Sendepfad. CarWebGuru-Zuordnung,
Abnahme und spätere Anforderungen stehen in `docs/carwebguru.md`.

`firmware/main/board_config.h` hält die geplanten SPI-GPIOs für das S2 Mini
fest. Diese Konfiguration initialisiert keine Anschlüsse. CC1101 bleibt
deaktiviert; GDO0/GDO2 sind mit -1 ausdrücklich nicht zugewiesen. Die
Konfiguration wird mit der Firmware kompiliert. Eine spätere Aktivierung
benötigt Treiber, geprüfte Verdrahtung und eine gesonderte Hardwareabnahme.

Softwareprüfung dieser Vorbereitung am 01.11.2026: `bash tools/check.sh`
erfolgreich (8 Tests); Firmware mit `tools/build-firmware.ps1`, ESP-IDF
5.4.2 / esp32s2 erfolgreich gebaut (`Project build complete.`). Kein Flashen
und keine elektrische SPI-, CC1101-, Funk- oder Torabnahme durchgeführt.

LEARNING, TRANSMITTING und ERROR mit Zeitlimits erst nach Hardwareklärung.
Aktive Profile liegen später auf dem ESP; App-Darstellung referenziert Geräte-ID,
Profil-ID und Revision. Bei Abweichungen bleibt Senden gesperrt, bis der Benutzer
die Unterschiede ausdrücklich aufgelöst hat. Kein automatisches Überschreiben.
Profile benötigen Schema, Integritätsprüfung und atomare Speicherung; derzeit
werden keine Profile angenommen oder gespeichert. Daher sind Profilvalidierungs-
und Synchronisationstests erst mit diesen Komponenten sinnvoll.

SEND muss eine konkrete Revision und Sitzung verlangen. Ein begrenzter
Request-ID-Speicher muss Wiederholungen verhindern und bei Erschöpfung ablehnen.
Timeout bedeutet Ergebnis unklar; niemals automatisch erneut senden.
