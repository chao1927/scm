#!/usr/bin/env bash
set -Eeuo pipefail

FRONTEND_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUNTIME_DIR="${FRONTEND_DIR}/.runtime"
PID_FILE="${RUNTIME_DIR}/frontend.pid"
LOG_FILE="${RUNTIME_DIR}/frontend.log"

is_running() {
  [[ -f "${PID_FILE}" ]] && kill -0 "$(cat "${PID_FILE}")" 2>/dev/null
}

up() {
  mkdir -p "${RUNTIME_DIR}"
  if is_running; then
    echo "前端已运行：http://127.0.0.1:5173/"
    return
  fi
  (
    cd "${FRONTEND_DIR}"
    nohup npm run dev -- --host 127.0.0.1 >"${LOG_FILE}" 2>&1 &
    echo "$!" >"${PID_FILE}"
  )
  for _ in $(seq 1 60); do
    if ! is_running; then
      tail -n 80 "${LOG_FILE}" >&2
      exit 1
    fi
    if curl -fsS http://127.0.0.1:5173/ >/dev/null 2>&1; then
      echo "前端已启动：http://127.0.0.1:5173/"
      return
    fi
    sleep 1
  done
  echo "前端启动超时" >&2
  tail -n 80 "${LOG_FILE}" >&2
  exit 1
}

down() {
  if is_running; then
    kill "$(cat "${PID_FILE}")"
    echo "前端已停止"
  fi
  rm -f "${PID_FILE}"
}

case "${1:-up}" in
  up) up ;;
  down) down ;;
  restart) down; up ;;
  status)
    if is_running; then echo "UP http://127.0.0.1:5173/"; else echo "DOWN"; fi
    ;;
  logs) tail -f "${LOG_FILE}" ;;
  *) echo "用法：$0 {up|down|restart|status|logs}" >&2; exit 2 ;;
esac
