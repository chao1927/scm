#!/usr/bin/env bash
set -Eeuo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${PROJECT_DIR}/.." && pwd)"

up() {
  "${ROOT_DIR}/middleware-stack/bin/dev.sh" up
  "${ROOT_DIR}/middleware-stack/bin/nacos-config.sh"
  "${PROJECT_DIR}/backend/bin/dev.sh" up
  "${PROJECT_DIR}/frontend/bin/dev.sh" up
  echo
  echo "SCM 本地开发环境已启动：http://127.0.0.1:5173/"
}

down() {
  "${PROJECT_DIR}/frontend/bin/dev.sh" down
  "${PROJECT_DIR}/backend/bin/dev.sh" down
  if [[ "${2:-}" == "--with-middleware" ]]; then
    "${ROOT_DIR}/middleware-stack/bin/dev.sh" down
  fi
}

status_all() {
  "${ROOT_DIR}/middleware-stack/bin/dev.sh" status
  "${PROJECT_DIR}/backend/bin/dev.sh" status
  "${PROJECT_DIR}/frontend/bin/dev.sh" status
}

foreground() {
  "${ROOT_DIR}/middleware-stack/bin/dev.sh" up
  "${ROOT_DIR}/middleware-stack/bin/nacos-config.sh"
  "${PROJECT_DIR}/frontend/bin/dev.sh" up
  "${PROJECT_DIR}/backend/bin/dev.sh" foreground
}

case "${1:-up}" in
  up) up ;;
  foreground) foreground ;;
  down) down "$@" ;;
  restart) down; up ;;
  status) status_all ;;
  *) echo "用法：$0 {up|foreground|down [--with-middleware]|restart|status}" >&2; exit 2 ;;
esac
