# Prüfplan und Ergebnisse

Stand 30.10.2026.

| Prüfung | Ergebnis |
| --- | --- |
| C-Protokollkern mit GCC, Warnungen als Fehler | Bestanden |
| 6 Python-Unittests gegen denselben C-Kern | Bestanden |
| Android APK-Build | Offen: JDK und Android SDK fehlen |
| ESP-IDF Firmware-Build | Offen: ESP-IDF/Toolchain fehlen |
| Radio-USB, Freigabe, Abziehen und Wiederanstecken | Nicht durchgeführt |
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
