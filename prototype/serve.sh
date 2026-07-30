#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PORT="${1:-4173}"

cd "$SCRIPT_DIR"
echo "供应链原型已启动：http://127.0.0.1:${PORT}"
echo "按 Ctrl+C 停止服务"
python3 -m http.server "$PORT" --bind 127.0.0.1
