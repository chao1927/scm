#!/usr/bin/env bash
set -Eeuo pipefail

DEFAULT_JAVA_17_HOME="/Applications/IntelliJ IDEA.app/Contents/jbr/Contents/Home"
JAVA_HOME="${QA_JAVA_HOME:-${DEFAULT_JAVA_17_HOME}}"
if [[ ! -x "${JAVA_HOME}/bin/java" ]]; then
  echo "未找到 JDK 17，请通过 QA_JAVA_HOME 指定 JDK 17 安装目录" >&2
  exit 1
fi
JAVA_MAJOR_VERSION="$("${JAVA_HOME}/bin/java" -version 2>&1 | awk -F'[.\"]' '/version/ {print $2; exit}')"
if [[ "${JAVA_MAJOR_VERSION}" != "17" ]]; then
  echo "九服务联调必须使用 JDK 17，当前 ${JAVA_HOME} 为 Java ${JAVA_MAJOR_VERSION}" >&2
  exit 1
fi
export JAVA_HOME
export PATH="${JAVA_HOME}/bin:/usr/local/bin:/opt/homebrew/bin:${PATH}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
ROOT_DIR="$(cd "${PROJECT_DIR}/.." && pwd)"
BACKEND_DIR="${PROJECT_DIR}/backend"
STACK_DIR="${ROOT_DIR}/middleware-stack"
ENV_FILE="${STACK_DIR}/.env"
LOG_DIR="${SCRIPT_DIR}/.nine-service-smoke"
JWT_SECRET="${QA_IAM_JWT_SECRET:-scm-nine-service-qa-jwt-secret-32-bytes-minimum}"
MFA_MASTER_KEY="${QA_IAM_MFA_MASTER_KEY:-0123456789abcdef0123456789abcdef}"
QA_USERNAME="${QA_IAM_USERNAME:-qa_admin}"
QA_PASSWORD="${QA_IAM_PASSWORD:-qa-local-password}"
PIDS=()

SERVICES='supplier-service|8101|scm_supplier_qa
iam-service|8097|scm_iam_qa
mdm-service|8098|scm_mdm_qa
oms-service|8099|scm_oms_qa
tms-service|8100|scm_tms_qa
purchase-service|8102|scm_purchase_qa
wms-service|8103|scm_wms_qa
inventory-service|8104|scm_inventory_qa
bms-service|8110|scm_bms_qa'

[[ -f "${ENV_FILE}" ]] || {
  echo "缺少 ${ENV_FILE}，请先执行 middleware-stack/bin/dev.sh up" >&2
  exit 1
}

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a
mkdir -p "${LOG_DIR}"

cleanup() {
  local pid
  for pid in "${PIDS[@]:-}"; do
    if kill -0 "${pid}" 2>/dev/null; then
      kill "${pid}" 2>/dev/null || true
    fi
  done
  for pid in "${PIDS[@]:-}"; do
    wait "${pid}" 2>/dev/null || true
  done
}
trap cleanup EXIT

build_artifacts() {
  (cd "${BACKEND_DIR}" && mvn -o -DskipTests package)
}

initialize_rocketmq_contracts() {
  local topic consumer_group
  echo "校验并初始化 RocketMQ Topic/消费组..."
  for topic in \
    supplier-domain-event purchase-domain-event wms-domain-event \
    inventory-domain-event iam-domain-event mdm-domain-event \
    oms-domain-event tms-domain-event bms-domain-event \
    master-data-domain-event iam-approval-domain-event \
    mdm-publication-receipt supplier-operations-event; do
    docker compose --env-file "${ENV_FILE}" -f "${STACK_DIR}/docker-compose.yml" \
      exec -T rocketmq-broker sh mqadmin updateTopic \
      -n rocketmq-namesrv:9876 -c DefaultCluster -t "${topic}" >/dev/null
  done
  for consumer_group in \
    supplier-master-data-snapshot supplier-contract-approval \
    supplier-business-event-v1 purchase-business-event-consumer \
    wms-business-event-consumer inventory-domain-event-consumer \
    iam-business-event-consumer mdm-business-event-consumer \
    oms-business-event-consumer tms-business-event-consumer \
    bms-business-event-consumer; do
    docker compose --env-file "${ENV_FILE}" -f "${STACK_DIR}/docker-compose.yml" \
      exec -T rocketmq-broker sh mqadmin updateSubGroup \
      -n rocketmq-namesrv:9876 -c DefaultCluster -g "${consumer_group}" >/dev/null
  done
  # Proxy 会缓存 Topic 不存在的路由结果；契约补建后重启以清除负缓存。
  docker compose --env-file "${ENV_FILE}" -f "${STACK_DIR}/docker-compose.yml" \
    restart rocketmq-broker >/dev/null
  local attempt health
  for attempt in {1..90}; do
    health="$(docker inspect --format '{{.State.Health.Status}}' \
      scm-rocketmq-broker 2>/dev/null || true)"
    if [[ "${health}" == "healthy" ]]; then
      echo "  ✓ RocketMQ 业务契约已就绪，Proxy 路由缓存已刷新"
      return 0
    fi
    sleep 1
  done
  echo "RocketMQ Broker/Proxy 重启后健康检查超时" >&2
  return 1
}

reset_qa_databases() {
  local sql=""
  local service port database
  while IFS='|' read -r service port database; do
    sql="${sql}DROP DATABASE IF EXISTS ${database}; CREATE DATABASE ${database} CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci; GRANT ALL PRIVILEGES ON ${database}.* TO '${MYSQL_APP_USER}'@'%'; "
  done <<<"${SERVICES}"
  docker compose --env-file "${ENV_FILE}" -f "${STACK_DIR}/docker-compose.yml" \
    exec -T mysql mysql -uroot "-p${MYSQL_ROOT_PASSWORD}" -e "${sql} FLUSH PRIVILEGES;"
}

wait_health() {
  local service="$1"
  local port="$2"
  local pid="$3"
  local log_file="$4"
  local attempt
  for attempt in {1..90}; do
    if ! kill -0 "${pid}" 2>/dev/null; then
      echo "${service} 进程提前退出" >&2
      tail -n 120 "${log_file}" >&2
      return 1
    fi
    if curl -fsS "http://127.0.0.1:${port}/actuator/health" 2>/dev/null | grep -q '"status":"UP"'; then
      echo "  ✓ ${service} :${port}"
      return 0
    fi
    sleep 1
  done
  echo "${service} 健康检查超时" >&2
  tail -n 120 "${log_file}" >&2
  return 1
}

start_service() {
  local service="$1"
  local port="$2"
  local database="$3"
  local log_file="${LOG_DIR}/${service}.log"
  local jar="${BACKEND_DIR}/${service}/target/${service}-0.1.0-SNAPSHOT.jar"

  env \
    JAVA_TOOL_OPTIONS="-Xms64m -Xmx256m -XX:MaxDirectMemorySize=256m -XX:ActiveProcessorCount=2" \
    SPRING_PROFILES_ACTIVE="prod" \
    SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:${MYSQL_PORT}/${database}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai" \
    SPRING_DATASOURCE_USERNAME="${MYSQL_APP_USER}" \
    SPRING_DATASOURCE_PASSWORD="${MYSQL_APP_PASSWORD}" \
    SPRING_DATA_REDIS_HOST="127.0.0.1" \
    SPRING_DATA_REDIS_PORT="${REDIS_PORT}" \
    SPRING_DATA_REDIS_USERNAME="${REDIS_USERNAME}" \
    SPRING_DATA_REDIS_PASSWORD="${REDIS_PASSWORD}" \
    NACOS_SERVER_ADDR="127.0.0.1:${NACOS_API_PORT}" \
    NACOS_NAMESPACE="scm-prod" \
    NACOS_GROUP="SCM_GROUP" \
    NACOS_USERNAME="${NACOS_USERNAME}" \
    NACOS_PASSWORD="${NACOS_PASSWORD}" \
    IAM_JWT_SECRET="${JWT_SECRET}" \
    IAM_MFA_MASTER_KEY="${MFA_MASTER_KEY}" \
    BMS_EXTERNAL_SHARED_SECRET="scm-bms-qa-hmac-secret" \
    SCM_BMS_EXTERNAL_SHARED_SECRET="scm-bms-qa-hmac-secret" \
    SCM_BMS_EXTERNAL_ERP_POST_URL="http://127.0.0.1:19001/api/finance/post" \
    SCM_BMS_EXTERNAL_TAX_ISSUE_URL="http://127.0.0.1:19002/api/invoices/issue" \
    SCM_BMS_EXTERNAL_PAYMENT_REFUND_URL="http://127.0.0.1:19003/api/refunds" \
    ROCKETMQ_ENDPOINTS="127.0.0.1:${ROCKETMQ_PROXY_GRPC_PORT}" \
    SCM_ROCKETMQ_ENDPOINTS="127.0.0.1:${ROCKETMQ_PROXY_GRPC_PORT}" \
    ROCKETMQ_SSL_ENABLED="false" \
    ROCKETMQ_ENABLED="true" \
    SCM_ROCKETMQ_ENABLED="true" \
    ROCKETMQ_BUSINESS_CONSUMER_ENABLED="true" \
    SCM_DUBBO_REGISTRY_ADDRESS="nacos://127.0.0.1:${NACOS_API_PORT}?username=${NACOS_USERNAME}&password=${NACOS_PASSWORD}" \
    DUBBO_QOS_ENABLE="false" \
    SERVER_PORT="${port}" \
    java -jar "${jar}" >"${log_file}" 2>&1 &
  local pid=$!
  PIDS+=("${pid}")
  wait_health "${service}" "${port}" "${pid}" "${log_file}"
}

seed_iam_user() {
  local sql
  sql="INSERT INTO iam_user(user_id,username,password_hash,user_status,failed_attempts,version,created_at,updated_at) VALUES(9001,'${QA_USERNAME}','HASH:${QA_PASSWORD}',1,0,0,NOW(3),NOW(3)); INSERT INTO iam_role(role_id,role_code,role_name,role_status,version,created_at,updated_at) VALUES(9001,'QA_ADMIN','QA 全权验收',1,0,NOW(3),NOW(3)); INSERT INTO iam_user_role(user_id,role_id,created_at) VALUES(9001,9001,NOW(3)); INSERT INTO iam_permission(permission_id,app_code,permission_code,permission_name,created_at) VALUES(9001,'SCM','*','QA 全权',NOW(3)); INSERT INTO iam_role_permission(role_id,permission_code,created_at) VALUES(9001,'*',NOW(3)); INSERT INTO iam_data_scope(scope_id,role_id,scope_type,scope_value,created_at) VALUES(9001,9001,'OWNER','*',NOW(3)),(9002,9001,'WAREHOUSE','*',NOW(3)),(9003,9001,'SUPPLIER','*',NOW(3));"
  docker compose --env-file "${ENV_FILE}" -f "${STACK_DIR}/docker-compose.yml" \
    exec -T mysql mysql -uroot "-p${MYSQL_ROOT_PASSWORD}" scm_iam_qa -e "${sql}" </dev/null
}

login_access_token() {
  local response token
  response="$(curl -fsS -X POST "http://127.0.0.1:8097/api/iam/v1/auth/login" \
    -H 'Content-Type: application/json' \
    --data "{\"username\":\"${QA_USERNAME}\",\"password\":\"${QA_PASSWORD}\",\"appCode\":\"SCM_WEB\",\"deviceDigest\":\"qa-local-smoke\"}")"
  token="$(printf '%s' "${response}" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')"
  [[ -n "${token}" ]] || {
    echo "IAM 登录未返回 accessToken" >&2
    return 1
  }
  printf '%s' "${token}"
}

base64_url() {
  openssl base64 -A | tr '+/' '-_' | tr -d '='
}

forbidden_token() {
  local now expires header payload unsigned signature
  now="$(date +%s)"
  expires="$((now + 3600))"
  header="$(printf '%s' '{"alg":"HS256","kid":"active","typ":"JWT"}' | base64_url)"
  payload="$(printf '{"sub":"9002","username":"qa_forbidden","app":"SCM_WEB","iat":%s,"exp":%s,"permissions":["forbidden:read"]}' "${now}" "${expires}" | base64_url)"
  unsigned="${header}.${payload}"
  signature="$(printf '%s' "${unsigned}" | openssl dgst -sha256 -mac HMAC -macopt "key:${JWT_SECRET}" -binary | base64_url)"
  printf '%s.%s' "${unsigned}" "${signature}"
}

build_artifacts
initialize_rocketmq_contracts
reset_qa_databases
echo "启动九个真实服务..."
while IFS='|' read -r service port database; do
  start_service "${service}" "${port}" "${database}"
  if [[ "${service}" == "iam-service" ]]; then
    seed_iam_user
  fi
done <<<"${SERVICES}"

ACCESS_TOKEN="$(login_access_token)"
FORBIDDEN_TOKEN="$(forbidden_token)"
"${SCRIPT_DIR}/api-baseline.sh" \
  --token "${ACCESS_TOKEN}" \
  --forbidden-token "${FORBIDDEN_TOKEN}" \
  --mysql-address "127.0.0.1:${MYSQL_PORT}"

echo "九服务真实 Flyway/MySQL/Nacos/Redis/RocketMQ 启动及 API 安全基线验收通过。"
