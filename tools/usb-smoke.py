"""Bounded HELLO/STATUS check on real USB CDC; never flashes or sends RF commands."""
import argparse
import json
import time
import uuid

import serial


def request(port, command, session=None, fragmented=False):
    request_id = str(uuid.uuid4())
    frame = (json.dumps(dict(v=1, id=request_id, cmd=command)) + '\n').encode('ascii')
    if fragmented:
        for byte in frame:
            port.write(bytes([byte]))
    else:
        port.write(frame)
    port.flush()
    deadline = time.monotonic() + 3
    line = bytearray()
    while time.monotonic() < deadline:
        byte = port.read(1)
        if not byte:
            continue
        if byte == b'\n':
            if not line:
                continue
            response = json.loads(line.decode('ascii'))
            assert response['v'] == 1 and response['id'] == request_id, response
            assert 'error' not in response and isinstance(response['result'], dict), response
            assert isinstance(response['session'], str) and response['session'], response
            assert session is None or response['session'] == session, response
            return response
        line.extend(byte)
        assert len(line) <= 1024, 'Antwort zu lang'
    raise TimeoutError('Keine vollstaendige USB-Antwort innerhalb von 3 Sekunden')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--port', required=True)
    args = parser.parse_args()
    session = None
    device = None
    for attempt in range(2):
        with serial.Serial(args.port, 115200, timeout=0.1, write_timeout=1) as port:
            port.dtr = True
            port.rts = True
            hello = request(port, 'HELLO', session, fragmented=True)
            result = hello['result']
            assert result['protocol'] == 1 and result['device_id'].startswith('s2-'), hello
            assert set(result['capabilities']) == {'HELLO', 'STATUS'}, hello
            assert device is None or result['device_id'] == device, hello
            session, device = hello['session'], result['device_id']
            status = request(port, 'STATUS', session)
            assert status['result'] == dict(state='IDLE', radio='NOT_CONFIGURED',
                                            transmit_enabled=False, profile_revision=0), status
            print(json.dumps(dict(connection=attempt + 1, hello=hello, status=status)))
        time.sleep(0.25)
    print('OK: HELLO/STATUS, fragmentierte Anfrage und erneutes Oeffnen des COM-Ports')


if __name__ == '__main__':
    main()
