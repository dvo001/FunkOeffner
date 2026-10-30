# USB-Protokoll v1 – Diagnoseumfang 0.1.0

CDC-ACM, eine JSON-Nachricht pro LF-Zeile; CRLF wird toleriert. 115200/8N1 als
CDC-Leitungseinstellung, ohne physische UART-Baudratenbindung. Maximal 1024 Bytes
vor LF. Antwort ebenfalls höchstens 1024 Bytes. Fragmentierte Lesevorgänge und
mehrere Zeilen pro Empfang sind zulässig. Unvollständige Zeilen bewirken nichts.
Überlange Zeilen werden bis LF verworfen; danach ist der Parser wieder bereit.

Die erste Stufe akzeptiert ausschliesslich ASCII (gültige UTF-8-Teilmenge), keine
NUL-Bytes. Namen und Profile sind noch nicht implementiert. Unicode-Erweiterung
benötigt explizite UTF-8- und Escapevalidierung vor Einführung der Profilbefehle.

```json
{"v":1,"id":"request-1","cmd":"HELLO"}
{"v":1,"id":"request-1","session":"boot-random","result":{"device_id":"s2-...","firmware":"0.1.0","protocol":1,"capabilities":["HELLO","STATUS"]}}
```

STATUS liefert `state=IDLE`, `radio=NOT_CONFIGURED`, `transmit_enabled=false`,
`profile_revision=0`. Das ist keine Torpositionsmeldung.
Request-ID ist eine nicht leere Zeichenfolge mit maximal 64 Bytes. Schlüssel sind
case-sensitive; doppelte Schlüssel werden abgelehnt. Fehler tragen `error.code`
anstelle von `result`. Codes: BAD_JSON, BAD_REQUEST, VERSION_MISMATCH,
FRAME_INVALID, UNSUPPORTED_COMMAND. Nicht zuordenbare Framefehler haben `id=null`.
Jede Antwort enthält die aktuelle Sitzung. App verwirft Verbindungen bei falscher
Version, Request-ID oder wechselnder Sitzung. Antworttimeout: 3 Sekunden.

Nur HELLO/STATUS sind verfügbar und nebenwirkungsfrei wiederholbar. LIST, GET,
LEARN_START, LEARN_CANCEL, SAVE, DELETE und SEND liefern UNSUPPORTED_COMMAND.
Keine asynchronen Vorgänge/Ereignisse und keine Profilübertragung in dieser Stufe.
Maximale Profilgrösse derzeit **0**; vor Erweiterung sind Impuls-, Zeit-, Chunk-
und Speichergrenzen sowie Revisionsprüfung festzulegen und zu testen.

Vor SEND-Einführung müssen Duplikatsperre und Sitzungsbindung implementiert sein.
Eine erfolgreiche Aussendung darf ausschliesslich als Funkübertragung bezeichnet
werden; sie bestätigt keine Toröffnung.
