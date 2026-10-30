#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build
cc -std=c11 -Wall -Wextra -Werror -Ifirmware/components/protocol/include -Ifirmware/components/cjson/include firmware/components/protocol/protocol.c firmware/components/cjson/cJSON.c tests/protocol_host.c -lm -o build/protocol-host
python3 -m unittest discover -s tests -v
