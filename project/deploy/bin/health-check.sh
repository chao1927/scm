#!/usr/bin/env bash
set -Eeuo pipefail
export LC_ALL=C

SERVICES='iam-service|8097
mdm-service|8098
oms-service|8099
tms-service|8100
supplier-service|8101
purchase-service|8102
wms-service|8103
inventory-service|8104
bms-service|8110'

CURL_BIN="${SCM_CURL_BIN:-curl}"
TIMEOUT="${SCM_HEALTH_TIMEOUT:-5}"
DRY_RUN=0
FAILED=0

usage() {
  cat <<'EOF'
用法：health-check.sh [--dry-run]

环境变量：
  SCM_<SERVICE>_BASE_URL   覆盖服务地址，例如 SCM_IAM_SERVICE_BASE_URL
  SCM_CURL_BIN             替换 curl，用于测试
  SCM_HEALTH_TIMEOUT       连接和总超时，默认 5 秒

脚本只调用 Actuator liveness/readiness，不修改任何状态。
EOF
}

while [[ "$#" -gt 0 ]]; do
  case "$1" in
    --dry-run) DRY_RUN=1; shift ;;
    --help|-h) usage; exit 0 ;;
    *) echo "未知参数：$1" >&2; usage >&2; exit 2 ;;
  esac
done

environment_base() {
  case "$1" in
    iam-service) printf '%s' "${SCM_IAM_SERVICE_BASE_URL:-}" ;;
    mdm-service) printf '%s' "${SCM_MDM_SERVICE_BASE_URL:-}" ;;
    oms-service) printf '%s' "${SCM_OMS_SERVICE_BASE_URL:-}" ;;
    tms-service) printf '%s' "${SCM_TMS_SERVICE_BASE_URL:-}" ;;
    supplier-service) printf '%s' "${SCM_SUPPLIER_SERVICE_BASE_URL:-}" ;;
    purchase-service) printf '%s' "${SCM_PURCHASE_SERVICE_BASE_URL:-}" ;;
    wms-service) printf '%s' "${SCM_WMS_SERVICE_BASE_URL:-}" ;;
    inventory-service) printf '%s' "${SCM_INVENTORY_SERVICE_BASE_URL:-}" ;;
    bms-service) printf '%s' "${SCM_BMS_SERVICE_BASE_URL:-}" ;;
  esac
}

check_endpoint() {
  local url="$1"
  local body_file="$2"
  local status
  status="$("${CURL_BIN}" -sS --connect-timeout "${TIMEOUT}" --max-time "${TIMEOUT}" \
    -o "${body_file}" -w '%{http_code}' "${url}" || true)"
  [[ "${status}" == "200" ]] && grep -Eq '"status"[[:space:]]*:[[:space:]]*"UP"' "${body_file}"
}

printf '%-20s %-10s %-10s %s\n' SERVICE LIVENESS READINESS BASE_URL
while IFS='|' read -r service port; do
  [[ -n "${service}" ]] || continue
  base="$(environment_base "${service}")"
  base="${base:-http://127.0.0.1:${port}}"
  if [[ "${DRY_RUN}" == "1" ]]; then
    printf '%-20s %-10s %-10s %s\n' "${service}" DRY-RUN DRY-RUN "${base}"
    continue
  fi
  live_file="$(mktemp "${TMPDIR:-/tmp}/scm-live.XXXXXX")"
  ready_file="$(mktemp "${TMPDIR:-/tmp}/scm-ready.XXXXXX")"
  live=FAIL
  ready=FAIL
  check_endpoint "${base%/}/actuator/health/liveness" "${live_file}" && live=PASS
  check_endpoint "${base%/}/actuator/health/readiness" "${ready_file}" && ready=PASS
  rm -f "${live_file}" "${ready_file}"
  [[ "${live}" == PASS && "${ready}" == PASS ]] || FAILED=1
  printf '%-20s %-10s %-10s %s\n' "${service}" "${live}" "${ready}" "${base}"
done <<<"${SERVICES}"

if [[ "${DRY_RUN}" == "1" ]]; then
  echo "DRY-RUN：未访问网络。"
  exit 0
fi
[[ "${FAILED}" == "0" ]] || { echo "健康检查失败。" >&2; exit 1; }
echo "九服务 liveness/readiness 全部通过。"
