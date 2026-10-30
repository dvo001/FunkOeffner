import json
import subprocess
import unittest
from pathlib import Path
EXE = str(Path(__file__).resolve().parent.parent / 'build/protocol-host')
def exchange(raw):
    return [json.loads(line) for line in subprocess.check_output([EXE], input=raw).splitlines()]
def command(cmd, **kw):
    return (json.dumps(dict(v=1, id='test-1', cmd=cmd, **kw))+'\n').encode()
class ProtocolTests(unittest.TestCase):
    def test_fragmented_and_coalesced(self):
        # Host feeds each byte separately; both requests share the same stream.
        a,b=exchange(command('HELLO')+command('STATUS'))
        self.assertEqual(a['id'],'test-1')
        self.assertEqual(a['result']['protocol'],1)
        self.assertEqual(a['session'],b['session'])
        self.assertFalse(b['result']['transmit_enabled'])
    def test_all_mutations_disabled_and_repeated(self):
        for cmd in ['SEND','SAVE','DELETE','LEARN_START','LEARN_CANCEL','LIST','GET']:
            responses=exchange(command(cmd)*2)
            self.assertEqual(len(responses),2)
            self.assertTrue(all(r['error']['code']=='UNSUPPORTED_COMMAND' for r in responses))
    def test_oversize_recovers(self):
        r=exchange(b'x'*1025+b'\n'+command('HELLO'))
        self.assertEqual(r[0]['error']['code'],'FRAME_INVALID')
        self.assertIn('result',r[1])
    def test_bad_input_recovers(self):
        for raw in [b'{}',b'[]',b'{',b'{} garbage',b'{"v":1,"id":"x","cmd":"HELLO","cmd":"SEND"}', b'\x00',b'\xff']:
            r=exchange(raw+b'\n'+command('STATUS'))
            self.assertIn('error',r[0]); self.assertIn('result',r[1])
    def test_version(self):
        r=exchange(b'{"v":2,"id":"x","cmd":"HELLO"}\n')[0]
        self.assertEqual(r['error']['code'],'VERSION_MISMATCH')
    def test_incomplete_no_reply(self):
        self.assertEqual(exchange(b'{"v":1'),[])
if __name__=='__main__': unittest.main()
