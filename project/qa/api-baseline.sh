#!/usr/bin/env bash
set -Eeuo pipefail
export LC_ALL=C

SERVICES='iam-service|8097|/api/iam/v1/me
mdm-service|8098|/api/mdm/v1/master-data-types
oms-service|8099|/api/oms/v1/sales-orders?pageNo=1&pageSize=1
tms-service|8100|/api/tms/v1/transport-tasks?pageNo=1&pageSize=1
supplier-service|8101|/api/supplier/v1/contracts?pageNo=1&pageSize=1
purchase-service|8102|/api/purchase/v1/requisitions?pageNo=1&pageSize=1
wms-service|8103|/api/wms/v1/inbound-orders?pageNo=1&pageSize=1
inventory-service|8104|/api/inventory/v1/stocks?pageNo=1&pageSize=1
bms-service|8110|/api/bms/v1/billing-subjects?pageNo=1&pageSize=1'

ACCESS_TOKEN="${SCM_ACCESS_TOKEN:-}"
FORBIDDEN_TOKEN="${SCM_FORBIDDEN_TOKEN:-}"
MYSQL_ADDRESS="${SCM_MYSQL_ADDRESS:-}"
HTTP_TIMEOUT="${SCM_QA_HTTP_TIMEOUT:-8}"
CURL_BIN="${SCM_CURL_BIN:-curl}"
TCP_CHECK_BIN="${SCM_TCP_CHECK_BIN:-}"
BASE_OVERRIDES=""
DRY_RUN=0

usage() {
  cat <<'EOF'
用法：api-baseline.sh [选项]

  --token <IAM access token>       具备九服务查询权限的 IAM access token
  --forbidden-token <token>        签名有效但不具备目标命名空间权限的 token
  --mysql-address <host:port>      MySQL TCP 地址，例如 127.0.0.1:3306
  --base <service=url>             覆盖单个服务基址，可重复传入
  --dry-run                        只输出端口和 API 验收矩阵，不访问网络
  --help                           显示帮助

也可使用 SCM_ACCESS_TOKEN、SCM_FORBIDDEN_TOKEN、SCM_MYSQL_ADDRESS，
以及 SCM_<SERVICE>_BASE_URL（例如 SCM_IAM_SERVICE_BASE_URL）。
脚本不签发 token，不嵌入账号、密码或密钥。
EOF
}

while [[ "$#" -gt 0 ]]; do
  case "$1" in
    --token) ACCESS_TOKEN="${2:-}"; shift 2 ;;
    --forbidden-token) FORBIDDEN_TOKEN="${2:-}"; shift 2 ;;
    --mysql-address) MYSQL_ADDRESS="${2:-}"; shift 2 ;;
    --base)
      [[ "${2:-}" == *=* ]] || { echo "--base 必须为 service=url" >&2; exit 2; }
      BASE_OVERRIDES="${BASE_OVERRIDES}${BASE_OVERRIDES:+$'\n'}${2%%=*}|${2#*=}"
      shift 2
      ;;
    --dry-run) DRY_RUN=1; shift ;;
    --help|-h) usage; exit 0 ;;
    *) echo "未知参数：$1" >&2; usage >&2; exit 2 ;;
  esac
done

service_exists() {
  local expected="$1"
  while IFS='|' read -r service _; do
    [[ "${service}" == "${expected}" ]] && return 0
  done <<<"${SERVICES}"
  return 1
}

while IFS='|' read -r override_service _; do
  [[ -z "${override_service}" ]] && continue
  service_exists "${override_service}" || {
    echo "未知服务基址覆盖：${override_service}" >&2
    exit 2
  }
done <<<"${BASE_OVERRIDES}"

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

service_base() {
  local service="$1"
  local port="$2"
  local override=""
  while IFS='|' read -r candidate value; do
    [[ "${candidate}" == "${service}" ]] && override="${value}"
  done <<<"${BASE_OVERRIDES}"
  if [[ -z "${override}" ]]; then
    override="$(environment_base "${service}")"
  fi
  printf '%s' "${override:-http://127.0.0.1:${port}}"
}

url_host_port() {
  local url="$1"
  local fallback_port="$2"
  if [[ "${url}" =~ ^https?://([^/:]+)(:([0-9]+))?(/.*)?$ ]]; then
    URL_HOST="${BASH_REMATCH[1]}"
    URL_PORT="${BASH_REMATCH[3]:-${fallback_port}}"
    if [[ -z "${BASH_REMATCH[3]:-}" && "${url}" == https://* ]]; then
      URL_PORT=443
    fi
    return 0
  fi
  return 1
}

tcp_check() {
  local host="$1"
  local port="$2"
  if [[ -n "${TCP_CHECK_BIN}" ]]; then
    "${TCP_CHECK_BIN}" "${host}" "${port}" "${HTTP_TIMEOUT}"
  elif command -v nc >/dev/null 2>&1; then
    nc -z -w "${HTTP_TIMEOUT}" "${host}" "${port}" >/dev/null 2>&1
  else
    (exec 3<>"/dev/tcp/${host}/${port}") >/dev/null 2>&1
  fi
}

validate_json_body() {
  local body_file="$1"
  [[ -s "${body_file}" ]] || return 1
  if command -v jq >/dev/null 2>&1; then
    jq -e . "${body_file}" >/dev/null 2>&1
  elif command -v python3 >/dev/null 2>&1; then
    python3 -c 'import json,sys; json.load(open(sys.argv[1], encoding="utf-8"))' "${body_file}" >/dev/null 2>&1
  else
    grep -Eq '^[[:space:]]*[\[{]' "${body_file}"
  fi
}

http_status() {
  local url="$1"
  local token="$2"
  local body_file="$3"
  local header_file="$4"
  local args=(-sS --connect-timeout "${HTTP_TIMEOUT}" --max-time "${HTTP_TIMEOUT}" -o "${body_file}" -D "${header_file}" -w '%{http_code}')
  if [[ -n "${token}" ]]; then
    args+=(-H "Authorization: Bearer ${token}")
  fi
  "${CURL_BIN}" "${args[@]}" "${url}"
}

print_header() {
  printf '%-19s %-7s %-8s %-12s %-12s %-16s %s\n' "TARGET" "PORT" "TCP" "ANONYMOUS" "FORBIDDEN" "REAL_QUERY" "ENDPOINT"
  printf '%-19s %-7s %-8s %-12s %-12s %-16s %s\n' "-------------------" "-------" "--------" "------------" "------------" "----------------" "--------"
}

if [[ "${DRY_RUN}" == "0" && ( -z "${ACCESS_TOKEN}" || -z "${FORBIDDEN_TOKEN}" || -z "${MYSQL_ADDRESS}" ) ]]; then
  print_header
  printf '%-19s %-7s %-8s %-12s %-12s %-16s %s\n' "configuration" "-" "-" "-" "-" "FAIL" "需要 token、forbidden-token 和 mysql-address"
  exit 2
fi

RESULT_FILE="$(mktemp "${TMPDIR:-/tmp}/scm-api-baseline.XXXXXX")"
WORK_DIR="$(mktemp -d "${TMPDIR:-/tmp}/scm-api-baseline-http.XXXXXX")"
trap 'rm -f "${RESULT_FILE}"; rm -rf "${WORK_DIR}"' EXIT
FAILED=0

if [[ -n "${MYSQL_ADDRESS}" && "${MYSQL_ADDRESS}" == *:* ]]; then
  mysql_host="${MYSQL_ADDRESS%:*}"
  mysql_port="${MYSQL_ADDRESS##*:}"
  if [[ "${DRY_RUN}" == "1" ]]; then
    printf 'mysql|%s|DRY-RUN|-|-|-|tcp://%s\n' "${mysql_port}" "${MYSQL_ADDRESS}" >>"${RESULT_FILE}"
  elif tcp_check "${mysql_host}" "${mysql_port}"; then
    printf 'mysql|%s|PASS|-|-|-|tcp://%s\n' "${mysql_port}" "${MYSQL_ADDRESS}" >>"${RESULT_FILE}"
  else
    printf 'mysql|%s|FAIL|-|-|-|tcp://%s\n' "${mysql_port}" "${MYSQL_ADDRESS}" >>"${RESULT_FILE}"
    FAILED=1
  fi
else
  printf 'mysql|-|%s|-|-|-|%s\n' "$([[ "${DRY_RUN}" == "1" ]] && echo 'NOT-CONFIGURED' || echo 'FAIL')" "${MYSQL_ADDRESS:-missing}" >>"${RESULT_FILE}"
  [[ "${DRY_RUN}" == "1" ]] || FAILED=1
fi

index=0
while IFS='|' read -r service port query_path; do
  [[ -n "${service}" ]] || continue
  index=$((index + 1))
  base="$(service_base "${service}" "${port}")"
  endpoint="${base%/}${query_path}"
  if ! url_host_port "${base}" "${port}"; then
    printf '%s|%s|FAIL|-|-|-|%s\n' "${service}" "${port}" "${endpoint}" >>"${RESULT_FILE}"
    FAILED=1
    continue
  fi
  effective_port="${URL_PORT}"
  if [[ "${DRY_RUN}" == "1" ]]; then
    printf '%s|%s|DRY-RUN|DRY-RUN|DRY-RUN|DRY-RUN|%s\n' "${service}" "${effective_port}" "${endpoint}" >>"${RESULT_FILE}"
    continue
  fi

  port_result=PASS
  anonymous_result=FAIL
  forbidden_result=FAIL
  query_result=FAIL
  if ! tcp_check "${URL_HOST}" "${effective_port}"; then
    port_result=FAIL
    FAILED=1
  else
    body="${WORK_DIR}/${index}.body"
    headers="${WORK_DIR}/${index}.headers"
    status="$(http_status "${endpoint}" "" "${body}" "${headers}" || true)"
    [[ "${status}" == "401" ]] && anonymous_result='PASS(401)' || { anonymous_result="FAIL(${status:-curl})"; FAILED=1; }

    status="$(http_status "${endpoint}" "${FORBIDDEN_TOKEN}" "${body}" "${headers}" || true)"
    [[ "${status}" == "403" ]] && forbidden_result='PASS(403)' || { forbidden_result="FAIL(${status:-curl})"; FAILED=1; }

    status="$(http_status "${endpoint}" "${ACCESS_TOKEN}" "${body}" "${headers}" || true)"
    if [[ "${status}" == "200" ]] \
        && grep -qi '^Content-Type:[[:space:]]*application/json' "${headers}" \
        && validate_json_body "${body}"; then
      query_result='PASS(200/JSON)'
    else
      query_result="FAIL(${status:-curl})"
      FAILED=1
    fi
  fi
  printf '%s|%s|%s|%s|%s|%s|%s\n' "${service}" "${effective_port}" \
    "${port_result}" "${anonymous_result}" "${forbidden_result}" "${query_result}" "${endpoint}" >>"${RESULT_FILE}"
done <<<"${SERVICES}"

print_header
while IFS='|' read -r target port tcp anonymous forbidden query endpoint; do
  printf '%-19s %-7s %-8s %-12s %-12s %-16s %s\n' "${target}" "${port}" "${tcp}" "${anonymous}" "${forbidden}" "${query}" "${endpoint}"
done <"${RESULT_FILE}"

if [[ "${DRY_RUN}" == "1" ]]; then
  echo "DRY-RUN 仅校验配置与矩阵，未访问 MySQL 或 HTTP。"
  exit 0
fi
if [[ "${FAILED}" != "0" ]]; then
  echo "API 基线验收失败；上表中 FAIL 为真实端口、认证或查询契约失败。" >&2
  exit 1
fi
echo "API 基线验收通过：MySQL TCP 和九服务端口、401、403、真实 JSON 查询全部通过。"
