# Projektauftrag: USB-Garagentoröffner für ein Android-Autoradio

Stand: 30.10.2026. Dieses Dokument ist der Arbeitsauftrag für Codex. Es beschreibt ein geplantes Projekt; Hardwarekompatibilität und praktische Funktion sind noch nicht nachgewiesen.

## Ziel

Entwickle eine native Android-App und eine ESP32-S2-Firmware für einen im Auto eingebauten, lernfähigen 433-MHz-Öffner. Die App läuft auf einem Autoradio mit eigenem Android und steuert die Hardware über USB. Betrieb ohne Internet, Cloud, WLAN oder Bluetooth.

Verbindung:

```text
Android-Autoradio mit App
    → USB (Android als Host, ESP als CDC-ACM-Gerät)
    → ESP32-S2 Mini
    → SPI und GDO-Signale
    → CC1101-Funkmodul mit 433-MHz-Antenne
    → kompatibler Torempfänger
```

Vorhandene Handsender dienen als Quelle beim Anlernen. Der Benutzer konnte die betreffenden Tore bereits mit einem handelsüblichen Kopiersender bedienen. Das beweist keine Kompatibilität mit dem eigenen CC1101-Aufbau und identifiziert das Funkprotokoll nicht.

## Hardware und offene Voraussetzungen

- Geplant: ESP32-S2 Mini; genaue Boardvariante und Schaltplan noch bestätigen.
- Geplant: CC1101-Modul, auf dem Produktbild als E07-M1101D V2.0 und 433M beschriftet, mit SMA-Anschluss und acht Anschlusspins.
- Die Pinfunktionen sind noch nicht bestätigt. Keine Belegung allein aus einem ähnlichen Produkt übernehmen.
- CC1101 mit 3,3 V betreiben; keine 5-V-Signale an seine Anschlüsse.
- USB-Datenverbindung und Stromversorgung über den Radioanschluss vorsehen. Strombudget und Stabilität praktisch prüfen.
- Keine direkte Verbindung zur 12-V-Bordspannung. Eine allfällige spätere separate Versorgung benötigt eine eigene geeignete Auslegung.
- Hersteller, Modell, tatsächliche Android-Version und USB-Host-Zugriff des Radios sind unbekannt.
- Android Auto ist nicht die Zielplattform.
- Frequenz, Modulation und Protokoll der Originalsender müssen noch bestimmt werden.
- Antennenposition und Temperaturbedingungen am Einbauort müssen praktisch bewertet werden; Entwicklungsboards nicht als für den Fahrzeugeinsatz qualifiziert darstellen.

Der ESP32-S2 unterstützt natives USB, hat aber kein Bluetooth. Seine USB-Datenpins dürfen bei der GPIO-Planung nicht anderweitig belegt werden. Boardunterlagen und aktuelle Herstellerdokumentation prüfen.

## Umfang der ersten Version

Unterstütze zunächst ein konkret geprüftes 433-MHz-Festcode-System. Ausgangspunkt ist ASK/OOK, sofern die Untersuchung des vorhandenen Senders dies bestätigt. Weitere Modulationen und Protokolle erst nach nachgewiesenem Bedarf ergänzen.

Lernen bedeutet: demodulierte Impulsfolgen mit passenden Funkparametern erfassen, prüfen, speichern und reproduzieren. Es handelt sich nicht um eine vollständige Spektrums- oder IQ-Aufnahme.

Nicht Bestandteil der ersten Version:

- Umgehung von Rolling-Code- oder Verschlüsselungsschutz.
- Garantie einer universellen Senderkompatibilität.
- Automatische Frequenzsuche über beliebige Bänder.
- Automatisches Öffnen durch Standort, Fahrzeugstart oder Wiederverbindung.
- Cloud, Benutzerkonten und Fernzugriff.
- Bestätigung der tatsächlichen Torposition ohne zusätzliche Rückmeldung.

## Anforderungen an die Android-App

- Native App in Kotlin; UI-Technik und unterstützte Android-Versionen nach Prüfung des Radios festlegen und dokumentieren.
- Hauptansicht mit grossen, benannten Tasten, Favoriten und frei wählbarer Reihenfolge.
- Klarer Status: getrennt, verbunden, Hardwarefehler, lernt, sendet oder Ergebnis unklar.
- Verwaltungsbereich für Anlernen, Benennen, Testen, Löschen sowie Sicherung und Wiederherstellung.
- Lernassistent mit explizitem Start, Zeitlimit, Abbruch und Prüfung mehrerer Aufnahmen.
- Profile erst nach Bestätigung dauerhaft speichern; Testsendung nur nach bewusster Bedienung.
- USB-Geräte gezielt erkennen, Zugriffsfreigabe anfordern und Verbindungswechsel sauber behandeln.
- Wiederverbindung nach Neustart, Abziehen und Radio-Standby; keine Sendebefehle dabei nachholen.
- Hardwarezugriff und Datenverarbeitung dürfen die Oberfläche nicht blockieren.
- Keine Torfunktion auslösen, solange Geräteidentität, Protokollversion oder Profilzustand unklar sind.
- Verwaltung für Bedienung im Stand gestalten. Keine Fahrzeugstillstandserkennung behaupten, solange keine entsprechende Datenquelle integriert ist.
- Sicherungen enthalten Zugangsdaten in Form von Funkprofilen: nur expliziter Export, keine Rohsignale in gewöhnlichen Logs, keine unbeabsichtigte Cloud-Sicherung.

## Anforderungen an die Firmware

- ESP-IDF als vorgesehene Basis; vor Implementierung eine kompatible stabile Version festlegen und Abhängigkeiten fixieren.
- Natives USB CDC-ACM; CC1101 direkt über SPI konfigurieren.
- Für Rohimpulse einen passenden asynchronen CC1101-Modus und GDO-Anschlüsse verwenden; FIFO-Paketbetrieb nicht mit beliebiger Signalaufzeichnung gleichsetzen.
- Zeitkritische Erfassung und Ausgabe mit geeigneten Hardwarefunktionen, beispielsweise RMT. Machbarkeit und Puffergrenzen auf dem S2 prüfen.
- Zustandsmaschine mindestens mit IDLE, LEARNING, TRANSMITTING und ERROR.
- Nur einen Funkvorgang gleichzeitig zulassen.
- Dauer, Anzahl Impulse, Wiederholungen, Nachrichtengrösse und Speicherverbrauch begrenzen.
- Lernaufnahme im RAM halten, bis der Benutzer sie speichert.
- Profile mit Schema-Version und Integritätsprüfung dauerhaft speichern. Unterbrochene Schreibvorgänge dürfen vorhandene gültige Profile nicht zerstören.
- Start, Reset, USB-Verbindung und Fehlerbehandlung dürfen keine unbeabsichtigte Übertragung auslösen.
- Sendebetrieb immer zeitlich begrenzen; kein endloses Senden bei Softwarefehlern oder Verbindungsabbruch.
- Frequenz und Sendeparameter auf den geprüften vorgesehenen Betrieb begrenzen. Vor realem Senden die am Einsatzort geltenden Funkbedingungen anhand offizieller Quellen prüfen und dokumentieren.

## Profile und Synchronisation

Der ESP hält die aktiven Funkprofile. Die App verwaltet Darstellung und eine lokale synchronisierte Kopie. Nach Verbindungsaufbau Geräte-ID und Profilrevisionen vergleichen. Unterschiede anzeigen oder nach einer dokumentierten Regel auflösen; niemals unbemerkt Profile überschreiben oder beim Senden ein anderes Profil verwenden.

Profilfelder mindestens:

- Stabile Profil-ID, Revision und Schema-Version.
- Name und unterstützter Protokoll-/Signaltyp.
- Frequenz und erforderliche Modulations-/Empfangsparameter.
- Impulsauflösung, Startpegel, Impulsdauern, Rahmenpausen und Wiederholungen.
- Prüfstatus: aufgenommen oder am Zielsystem praktisch bestätigt.

Gleiche Aufnahmen beweisen keinen Festcode; unterschiedliche Aufnahmen beweisen keinen Rolling Code. Rauschen, Timingabweichungen und wechselnde Nutzdaten berücksichtigen. Unklare Signale als ungeklärt kennzeichnen.

## USB-Protokoll Version 1

Versioniertes UTF-8-JSON mit einer Nachricht pro Zeile ist der Ausgangspunkt. Maximale Zeilenlänge und Profilgrösse vor Implementierung festlegen. Grössere Profile gegebenenfalls begrenzt und prüfbar in Teilen übertragen. Fragmentierte USB-Lesevorgänge, mehrere Nachrichten pro Lesevorgang und fehlerhafte Eingaben berücksichtigen.

Jeder Auftrag erhält eine Request-ID; Antworten enthalten dieselbe ID und entweder ein Ergebnis oder einen strukturierten Fehlercode. Asynchrone Ereignisse besitzen eine eindeutige Zuordnung zum Vorgang. Gerätestarts anhand einer Sitzungs-ID erkennbar machen.

| Befehl | Aufgabe |
| --- | --- |
| HELLO | Geräte-ID, Sitzung, Protokollversion, Firmware und Fähigkeiten |
| STATUS | Hardware- und Betriebsstatus |
| LIST / GET | Profile auflisten oder abrufen |
| LEARN_START / LEARN_CANCEL | Begrenzten Lernvorgang starten oder abbrechen |
| SAVE / DELETE | Profil validiert speichern oder löschen |
| SEND | Ein bestimmtes Profil mit bestimmter Revision auslösen |

SEND darf bei Timeout oder Wiederverbindung nicht automatisch wiederholt werden. Doppelte Request-IDs innerhalb einer Sitzung dürfen keine zweite Übertragung auslösen. Nach Reset keine ausstehenden Sendewünsche wiederherstellen. Bei unklarer Antwort muss die App das Ergebnis als unklar anzeigen und einen neuen bewussten Tastendruck verlangen.

Eine erfolgreiche Funkübertragung ist keine Bestätigung einer Toröffnung. Dies sprachlich in UI, Protokoll und Dokumentation unterscheiden.

## Umsetzung und Abnahmekriterien

### 1. Umgebung und USB-Nachweis

- Bestehende Dateien, Projektanweisungen und installierte Werkzeuge prüfen.
- Genaue Boardvarianten, Pinbelegung und Radioangaben dokumentieren.
- Minimale Firmware mit HELLO und STATUS erstellen.
- Minimale Android-Verbindung implementieren und am Radio prüfen.
- Abnahme: Gerät wird erkannt, Freigabe funktioniert, Nachrichten werden bidirektional übertragen. Abziehen und Wiederanstecken lösen keine Funkübertragung aus.

### 2. Funknachweis

- CC1101 initialisieren und Kommunikation sowie Konfiguration prüfen.
- Einen bekannten Sender mit festgelegten Parametern mehrfach erfassen.
- Impulsfolgen vergleichen, Störungen und abgeschnittene Aufnahmen erkennen.
- Wiedergabe nur nach explizitem Testauftrag auslösen.
- Abnahme: Der konkrete eigene Torempfänger reagiert wiederholt korrekt. Ohne Hardwaretest bleibt der Status unbestätigt.

### 3. Robuste Firmware

- Zustandsmaschine, begrenzte Vorgänge, Profile und dauerhafte Speicherung umsetzen.
- Doppelte Nachrichten, ungültige Parameter, Speichergrenzen und Abbruchfälle behandeln.
- Abnahme: Profile bleiben nach Neustart erhalten; fehlerhafte Eingaben erzeugen keine Übertragung und zerstören keine vorhandenen Profile.

### 4. Vollständige Android-App

- Hauptansicht, Verwaltung, Lernassistent, Synchronisation und Sicherungen umsetzen.
- Abnahme: Ein vollständiger Ablauf von Lernen über Speichern und Neustart bis zum bewussten Senden funktioniert; Verbindungsfehler werden nachvollziehbar angezeigt.

### 5. Prüfung im Auto

- USB-Versorgung und Verbindung bei Radio-Start, Standby und Aufwachen testen.
- Reichweite mit tatsächlicher Antennenposition prüfen.
- Wiederholte Betriebszyklen dokumentieren.
- Abnahme: Keine unbeabsichtigten Sendungen, keine stillen Profilverluste; tatsächliche Ergebnisse und Einschränkungen sind festgehalten.

## Repository und Dokumentation

```text
android/
firmware/
docs/
  hardware.md
  architecture.md
  usb-protocol.md
  supported-remotes.md
  test-plan.md
README.md
AGENTS.md
```

README mit Einrichtung, Build, Flashen, Installation und Inbetriebnahme. Hardwaredokumentation mit bestätigter Verdrahtung, Spannungen und Quellen. Unterstützte Sender nur mit nachvollziehbarem Prüfstatus aufführen. Versionsstände, offene Punkte und durchgeführte Tests aktuell halten.

## Arbeitsregeln für Codex

- Deutsch mit Schweizer Rechtschreibung verwenden.
- Bestehende Architektur und fremde Änderungen respektieren.
- Reversible Implementierungsdetails selbstständig entscheiden und wesentliche Annahmen dokumentieren.
- Keine zufälligen GPIO-Belegungen oder ungeprüften Bibliotheks-APIs als bestätigt ausgeben.
- Herstellerunterlagen für entscheidende Hardwaredetails verwenden. Relevante Grundlagen: ESP32-S2 USB Device Stack und RMT-Dokumentation, TI CC1101-Datenblatt sowie Android USB Host API.
- Bedeutungsvolle automatisierte Tests für Parser, Profilvalidierung, Synchronisation und Wiederholungsverhalten schreiben. Hardwareabhängige Funktionen mit aufgezeichneten Testdaten oder Testdoubles zusätzlich prüfen.
- Builds und Tests tatsächlich ausführen, soweit die Umgebung dies erlaubt. Fehlende Toolchains und nicht durchgeführte Prüfungen offen nennen.
- Softwaretests, USB-Tests am tatsächlichen Radio und physische Funk-/Tortests getrennt berichten.
- Fehlende Hardware nicht durch behauptete Testerfolge ersetzen. Unabhängige Softwarearbeiten können mit einem klar gekennzeichneten Simulationsmodus fortgesetzt werden.
- Keine Veröffentlichung, kein Flashen angeschlossener Hardware und keine realen Funkübertragungen ohne entsprechenden Auftrag ausführen.
- Abschliessend Ergebnis, Änderungen, Prüfungen und offene Punkte knapp zusammenfassen.

## Erster Auftrag beim Projektstart

Prüfe Repository und Entwicklungsumgebung. Erfasse die noch fehlenden Radio- und Boarddaten. Lege anschliessend das gemeinsame Projektgerüst, die dokumentierte USB-Protokollversion 1 und eine minimale HELLO/STATUS-Verbindung an. Implementiere zunächst keine ungeprüfte Tor-Sendefunktion. Wenn das Autoradio oder die Funkhardware nicht verfügbar ist, liefere baubare Komponenten und reproduzierbare Softwaretests mit klarer Trennung zur noch offenen Hardwareabnahme.
