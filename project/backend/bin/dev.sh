#!/usr/bin/env bash
set -Eeuo pipefail

BACKEND_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT_DIR="$(cd "${BACKEND_DIR}/.." && pwd)"
ROOT_DIR="$(cd "${PROJECT_DIR}/.." && pwd)"
ENV_FILE="${ROOT_DIR}/middleware-stack/.env"
RUNTIME_DIR="${BACKEND_DIR}/.runtime"
PID_DIR="${RUNTIME_DIR}/pids"
LOG_DIR="${RUNTIME_DIR}/logs"
SCM_ENV="${SCM_ENV:-test}"

SERVICES="
iam-service:8097
mdm-service:8098
oms-service:8099
tms-service:8100
supplier-service:8101
purchase-service:8102
wms-service:8103
inventory-service:8104
bms-service:8110
"

load_env() {
  if [[ "${SCM_ENV}" != "test" && "${SCM_ENV}" != "prod" ]]; then
    echo "SCM_ENV 只允许 test 或 prod，当前值：${SCM_ENV}" >&2
    exit 2
  fi
  if [[ ! -f "${ENV_FILE}" ]]; then
    echo "缺少 ${ENV_FILE}，请先执行 middleware-stack/bin/dev.sh up" >&2
    exit 1
  fi
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  local required_name
  for required_name in MYSQL_PORT MYSQL_APP_USER MYSQL_APP_PASSWORD \
      REDIS_PORT REDIS_USERNAME REDIS_PASSWORD ROCKETMQ_PROXY_GRPC_PORT \
      NACOS_API_PORT NACOS_USERNAME NACOS_PASSWORD IAM_JWT_SECRET IAM_MFA_MASTER_KEY \
      BMS_EXTERNAL_SHARED_SECRET; do
    if [[ -z "${!required_name:-}" ]]; then
      echo "${ENV_FILE} 缺少 ${required_name}，请先执行 middleware-stack/bin/dev.sh up" >&2
      exit 1
    fi
  done
}

use_java_17() {
  local candidate
  for candidate in \
    "${JDK17_HOME:-}" \
    "$(/usr/libexec/java_home -v 17 2>/dev/null || true)" \
    "/Applications/IntelliJ IDEA.app/Contents/jbr/Contents/Home" \
    "/Applications/GoLand.app/Contents/jbr/Contents/Home"; do
    if [[ -n "${candidate}" && -x "${candidate}/bin/java" ]] \
        && "${candidate}/bin/java" -version 2>&1 | head -n 1 | grep -q '"17\.'; then
      export JAVA_HOME="${candidate}"
      export PATH="${JAVA_HOME}/bin:${PATH}"
      export LANG="en_US.UTF-8"
      export LC_ALL="en_US.UTF-8"
      return
    fi
  done
  echo "未找到 JDK 17；可通过 JDK17_HOME 指定安装目录" >&2
  exit 1
}

is_running() {
  local service="$1"
  local pid_file="${PID_DIR}/${service}.pid"
  [[ -f "${pid_file}" ]] && kill -0 "$(cat "${pid_file}")" 2>/dev/null
}

build_services() {
  local source_repository settings_file
  use_java_17
  source_repository="$(cd "${BACKEND_DIR}" && mvn -o -X validate 2>&1 \
    | sed -n 's/.*Using local repository at //p' | head -n 1)"
  if [[ -z "${source_repository}" || ! -d "${source_repository}" ]]; then
    echo "无法定位 Maven 本地仓库" >&2
    exit 1
  fi

  settings_file="${RUNTIME_DIR}/maven-local-settings.xml"
  printf '%s\n' \
    '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">' \
    '  <mirrors>' \
    '    <mirror>' \
    '      <id>scm-local-source</id>' \
    '      <mirrorOf>*</mirrorOf>' \
    "      <url>file://${source_repository}</url>" \
    '    </mirror>' \
    '  </mirrors>' \
    '</settings>' >"${settings_file}"

  echo "使用 JDK 17 和本地 Maven 仓库 file://${source_repository} 构建 ${SCM_ENV} 环境 JAR..."
  (cd "${BACKEND_DIR}" && mvn -s "${settings_file}" \
    -Dmaven.repo.local="${RUNTIME_DIR}/m2" \
    -P"${SCM_ENV}" -DskipTests package)
}

start_service() {
  local service="$1"
  local port="$2"
  local jar_file="${BACKEND_DIR}/${service}/target/${service}-0.1.0-SNAPSHOT.jar"
  local log_file="${LOG_DIR}/${service}.log"
  local pid_file="${PID_DIR}/${service}.pid"
  local database="scm_${service%-service}"
  local nacos_registry="nacos://127.0.0.1:${NACOS_API_PORT}?username=${NACOS_USERNAME}&password=${NACOS_PASSWORD}"

  if is_running "${service}"; then
    echo "  ✓ ${service} 已运行"
    return
  fi
  if [[ ! -f "${jar_file}" ]]; then
    echo "缺少 ${jar_file}" >&2
    exit 1
  fi

  nohup env -i \
    HOME="${HOME}" \
    TMPDIR="${TMPDIR:-/tmp}" \
    LANG="en_US.UTF-8" \
    LC_ALL="en_US.UTF-8" \
    TZ="${TZ:-Asia/Shanghai}" \
    JAVA_HOME="${JAVA_HOME}" \
    PATH="${JAVA_HOME}/bin:/usr/local/bin:/opt/homebrew/bin:/usr/bin:/bin" \
    SPRING_PROFILES_ACTIVE="${SCM_ENV}" \
    SPRING_CLOUD_NACOS_CONFIG_ENABLED="true" \
    MYSQL_URL="jdbc:mysql://127.0.0.1:${MYSQL_PORT}/${database}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai" \
    MYSQL_USERNAME="${MYSQL_APP_USER}" \
    MYSQL_PASSWORD="${MYSQL_APP_PASSWORD}" \
    SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:${MYSQL_PORT}/${database}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai" \
    SPRING_DATASOURCE_USERNAME="${MYSQL_APP_USER}" \
    SPRING_DATASOURCE_PASSWORD="${MYSQL_APP_PASSWORD}" \
    REDIS_HOST="127.0.0.1" \
    REDIS_PORT="${REDIS_PORT}" \
    REDIS_USERNAME="${REDIS_USERNAME}" \
    REDIS_PASSWORD="${REDIS_PASSWORD}" \
    SPRING_DATA_REDIS_HOST="127.0.0.1" \
    SPRING_DATA_REDIS_PORT="${REDIS_PORT}" \
    SPRING_DATA_REDIS_USERNAME="${REDIS_USERNAME}" \
    SPRING_DATA_REDIS_PASSWORD="${REDIS_PASSWORD}" \
    NACOS_SERVER_ADDR="127.0.0.1:${NACOS_API_PORT}" \
    NACOS_NAMESPACE="scm-${SCM_ENV}" \
    NACOS_GROUP="SCM_GROUP" \
    NACOS_USERNAME="${NACOS_USERNAME}" \
    NACOS_PASSWORD="${NACOS_PASSWORD}" \
    IAM_JWT_SECRET="${IAM_JWT_SECRET}" \
    IAM_MFA_MASTER_KEY="${IAM_MFA_MASTER_KEY}" \
    BMS_EXTERNAL_SHARED_SECRET="${BMS_EXTERNAL_SHARED_SECRET}" \
    SCM_ROCKETMQ_ENABLED="true" \
    SCM_ROCKETMQ_ENDPOINTS="127.0.0.1:${ROCKETMQ_PROXY_GRPC_PORT}" \
    ROCKETMQ_SSL_ENABLED="false" \
    ROCKETMQ_BUSINESS_CONSUMER_ENABLED="true" \
    ROCKETMQ_MASTER_DATA_CONSUMER_ENABLED="true" \
    ROCKETMQ_CONTRACT_APPROVAL_CONSUMER_ENABLED="true" \
    SCM_DUBBO_REGISTRY_ADDRESS="${nacos_registry}" \
    DUBBO_REGISTRY_ADDRESS="${nacos_registry}" \
    DUBBO_QOS_ENABLE="false" \
    SCM_LOGGING_PATH="${LOG_DIR}" \
    SERVER_PORT="${port}" \
    "${JAVA_HOME}/bin/java" \
      -Xms128m -Xmx512m -Xss256k \
      -XX:ActiveProcessorCount=2 \
      -jar "${jar_file}" \
    >"${log_file}" 2>&1 &
  echo "$!" >"${pid_file}"
  echo "  → ${service} :${port}"
}

wait_for_service() {
  local service="$1"
  local port="$2"
  local waited=0
  while (( waited < 180 )); do
    if ! is_running "${service}"; then
      echo "${service} 启动失败：" >&2
      tail -n 80 "${LOG_DIR}/${service}.log" >&2
      return 1
    fi
    if curl -s -o /dev/null "http://127.0.0.1:${port}/"; then
      echo "  ✓ ${service} :${port}"
      return
    fi
    sleep 2
    waited=$((waited + 2))
  done
  echo "${service} 等待端口 ${port} 超时" >&2
  tail -n 80 "${LOG_DIR}/${service}.log" >&2
  return 1
}

up() {
  load_env
  mkdir -p "${PID_DIR}" "${LOG_DIR}"
  build_services
  echo "依次启动 9 个 Java 服务..."
  local failed=0
  while IFS=: read -r service port; do
    [[ -n "${service}" ]] || continue
    start_service "${service}" "${port}"
    if ! wait_for_service "${service}" "${port}"; then
      failed=1
      break
    fi
  done <<<"${SERVICES}"
  if [[ "${failed}" != "0" ]]; then
    echo "部分后端服务启动失败，请查看 ${LOG_DIR}" >&2
    return 1
  fi
  echo "后端 9 个服务已以 ${SCM_ENV} 环境启动。"
}

down() {
  mkdir -p "${PID_DIR}"
  while IFS=: read -r service port; do
    [[ -n "${service}" ]] || continue
    pid_file="${PID_DIR}/${service}.pid"
    if [[ -f "${pid_file}" ]]; then
      pid="$(cat "${pid_file}")"
      if kill -0 "${pid}" 2>/dev/null; then
        kill "${pid}"
        echo "  ✓ 已停止 ${service}"
      fi
      rm -f "${pid_file}"
    fi
  done <<<"${SERVICES}"
}

status_services() {
  while IFS=: read -r service port; do
    [[ -n "${service}" ]] || continue
    if is_running "${service}" && curl -s -o /dev/null "http://127.0.0.1:${port}/"; then
      echo "UP    ${service} :${port}"
    else
      echo "DOWN  ${service} :${port}"
    fi
  done <<<"${SERVICES}"
}

command="${1:-up}"
case "${command}" in
  up) up ;;
  foreground)
    up
    echo "后端服务以前台守护模式运行，按 Ctrl+C 结束。"
    wait
    ;;
  down) down ;;
  restart) down; up ;;
  status) status_services ;;
  logs)
    service="${2:-}"
    if [[ -z "${service}" ]]; then
      echo "用法：$0 logs <service-name>" >&2
      exit 2
    fi
    tail -f "${LOG_DIR}/${service}.log"
    ;;
  *)
    echo "用法：SCM_ENV=test|prod $0 {up|foreground|down|restart|status|logs <service-name>}" >&2
    exit 2
    ;;
esac
