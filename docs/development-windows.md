# ESP-IDF unter Windows und VS Code

Firmware: ESP-IDF v5.4.2, Ziel ESP32-S2. Die Boardvariante und deren
Verdrahtung bleiben offen. Einrichtung und Build benötigen kein angeschlossenes Board.

## Arbeitsbereich

`FunkOffner.code-workspace` in VS Code öffnen. Der Ordner **Firmware** ist das
ESP-IDF-Projekt; **Projekt** enthält auch Android, Tests und Dokumentation.
In der Espressif-Erweiterung muss **Firmware** als Projekt ausgewählt sein.
Die Erweiterung `espressif.esp-idf-extension` ist empfohlen.

`Strg+Shift+B` startet die Aufgabe **Firmware bauen (ESP-IDF 5.4.2)**.
Alternativ aus dem Repository in Windows PowerShell:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tools\build-firmware.ps1
```

Das Skript liest die abgeschlossene Installation aus
`C:\Espressif\tools\eim_idf.json`, aktiviert genau v5.4.2 und baut ausschliesslich
für `esp32s2`. Bei einem anderen Installationsverzeichnis kann der Pfad mit
`-InstallationFile <Datei>` angegeben werden. Es enthält keinen Flashbefehl.

## Lokale Installation

Die Espressif-Erweiterung war bei der Einrichtung bereits installiert (2.3.0).
Der offizielle ESP-IDF Installation Manager (EIM CLI 0.20.1) installiert v5.4.2.
ESP-IDF liegt lokal unter `C:\Users\danie\Espressif\v5.4.2\esp-idf`,
die Werkzeuge unter `C:\Espressif\tools`. Diese maschinenspezifischen Pfade
gehören nicht zur gemeinsamen Firmwarekonfiguration.

`firmware/.vscode/settings.json` wird lokal mit der ausgewählten Installation
konfiguriert und über `.git/info/exclude` von Commits ausgenommen.
Bestehende Einstellungen im Repository-Hauptordner bleiben erhalten.

Ein von WSL gestarteter Windows-Prozess hatte bei der Einrichtung nur `.CPL`
in `PATHEXT`. Das Buildskript setzt die üblichen ausfuehrbaren Dateiendungen
für seinen eigenen Prozess; es ändert keine globale Windows-Umgebungsvariable.

## Nachweis

Am 30.10.2026 erfolgreich mit `tools/build-firmware.ps1` gebaut (Exitcode 0).
Der Firmware-Build ersetzt keine USB-Abnahme am Radio und keinen Hardwaretest.
Buildartefakte befinden sich unter `firmware/build/` und werden nicht versioniert.
Abhängigkeiten stehen in `firmware/dependencies.lock` nach erfolgreicher Auflösung.

Herstelleranleitung:
[ESP-IDF Installation Manager CLI](https://docs.espressif.com/projects/idf-im-ui/en/latest/cli_installation.html).
