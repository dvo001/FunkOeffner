# Architektur

## Implementierte erste Stufe

Android Activity mit grossen Diagnoseelementen, USB-Zugriffsfreigabe und einem
seriellen Hintergrund-Executor. Lebenszykluswechsel schliessen die Verbindung.
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
