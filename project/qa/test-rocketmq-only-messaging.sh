#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BACKEND_DIR="${ROOT_DIR}/project/backend"
DOCS_DIR="${ROOT_DIR}/docs"
SERVICES=(supplier purchase wms inventory iam mdm oms tms bms)

fail() {
  echo "[FAIL] $*" >&2
  exit 1
}

event_routes="$(
  rg -n --glob '*.java' \
    '@(RequestMapping|PostMapping|PutMapping|PatchMapping).*(events|/openapi/bms/v1/charge-sources)"' \
    "${BACKEND_DIR}" 2>/dev/null \
    | rg -v 'failed-events' || true
)"
if [[ -n "${event_routes}" ]]; then
  echo "${event_routes}" >&2
  fail "检测到 HTTP 事件消费入口；跨上下文业务事件只能由 RocketMQ Consumer 接收"
fi

if rg -n 'legacy-http-event-ingress' "${BACKEND_DIR}" >/dev/null 2>&1; then
  fail "检测到旧 HTTP 事件入口开关；该兼容能力必须彻底删除"
fi

http_event_contracts="$(
  rg -n '/(internal|openapi)/[^`[:space:]]*(events|source-events|charge-sources)|(POST|PUT|PATCH) /[^`[:space:]]*events([`[:space:]|]|$)' \
    "${DOCS_DIR}" 2>/dev/null \
    | rg -v 'failed-events' || true
)"
if [[ -n "${http_event_contracts}" ]]; then
  echo "${http_event_contracts}" >&2
  fail "文档仍声明 HTTP 事件消费契约；跨上下文事件必须改为 RocketMQ Topic/Consumer"
fi

for service in "${SERVICES[@]}"; do
  mq_dir="${BACKEND_DIR}/${service}-service/src/main/java"
  if ! find "${mq_dir}" -path '*/infrastructure/mq/*RocketMq*.java' -type f -print -quit \
      | grep -q .; then
    fail "${service}-service 缺少真实 RocketMQ Producer/Consumer 实现"
  fi
done

echo "[PASS] 九服务仅通过 RocketMQ 生产和消费跨上下文业务事件"
