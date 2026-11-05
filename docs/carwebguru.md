# CarWebGuru-Taste für FunkOffner

Stand 01.11.2026, App 0.1.3. Integration vorbereitet, Senden nicht implementiert.

## Aktuell verfügbar

Die APK bietet zwei Android-Launcher-Einstiege: «FunkOffner» für Verwaltung
und USB-Diagnose sowie «Tor öffnen» als Vorbereitung der späteren Tastenaktion.
Zusätzlich ist bei FunkOffner ein statischer Android-App-Shortcut «Tor öffnen»
hinterlegt. Beide Wege öffnen `ch.funkoffner.OpenGateActivity`.

Dieser Einstieg öffnet die normale Hauptseite mit «Tor öffnen» und einem
antippbaren Verbindungshinweis zu den Einstellungen. Die Tortaste bleibt
deaktiviert, solange Funk und Profile nicht implementiert sind. Der Einstieg
wertet keine Intent-Extras als Befehle aus und speichert keine Sendewünsche.
Auch wiederholter Start oder Wiederherstellung lösen keine Übertragung aus.

## Zuordnung am Radio prüfen

1. APK 0.1.3 installieren.
2. In der Tastenbelegung der installierten CarWebGuru-Version prüfen, ob
   «Tor öffnen» als eigener App-Einstieg oder als FunkOffner-App-Shortcut
   auswählbar ist. Die Menübezeichnungen sind versions-/themeabhängig.
3. Falls verfügbar, der gewünschten Taste zuweisen und antippen. Erwartet:
   normale Hauptseite. Der Verbindungshinweis führt zu den Einstellungen;
   Zurück führt zur Hauptseite.
4. Falls CarWebGuru nur Pakete statt einzelner Launcher-Activities anzeigt
   und keine App-Shortcuts anbietet, ist dieser Weg dort noch nicht bestätigt.
   Auswahlmenü dokumentieren, bevor eine andere Integration umgesetzt wird.

Die offizielle CarWebGuru-Hilfe dokumentiert keine verbindliche Schnittstelle
für externe Torbefehle oder die Auswahl einzelner Launcher-Activities.
Die tatsächliche Tastenbelegung muss mit der installierten Version geprüft werden.
Eine Emulatorprüfung der Android-Einstiege ersetzt diese Abnahme nicht.

## Späteres Zielverhalten

Nach Implementierung und Abnahme der Funk-/Profilstufe soll ein bewusster
Tastendruck genau einen begrenzten Vorgang für ein fest zugeordnetes, geprüftes
Profil auslösen. Kein frei übergebenes Funkprofil aus externen Intent-Extras.
Vor dem Senden sind Geräteidentität, Firmwarefähigkeiten, Sitzung und
Profilrevision zu prüfen. Bei fehlendem USB, nötiger Freigabe, Wiederherstellung
oder Timeout wird kein Sendewunsch für später gespeichert. Nach Wiederverbindung
ist ein neuer Tastendruck erforderlich. Doppelte Requests müssen auch in der
Firmware abgefangen werden. Eine Übertragung bestätigt keine Toröffnung.

Die normale Verwaltungs-Activity darf bei ihrem Start niemals senden.
Eine automatische Auslösung allein im `onCreate`/`onResume` der jetzigen
Vorbereitungs-Activity ist keine geeignete spätere Implementierung: Android
kann Activities ohne neuen Tastendruck wiederherstellen.

## Quellen

- [CarWebGuru](https://carwebguru.com/)
- [Offizielle Hilfe](https://carwebguru.com/help/)
- [Android: App-Shortcuts erstellen](https://developer.android.com/develop/ui/compose/system/shortcuts/creating-shortcuts)
