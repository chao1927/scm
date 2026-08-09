#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
BACKEND_DIR="${PROJECT_DIR}/backend"
REGISTRY_ADDRESS="${DUBBO_REGISTRY_ADDRESS:-nacos://127.0.0.1:8848}"
PROVIDER_PORT="${DUBBO_WMS_PORT:-20881}"
WEB_PORT="${WMS_WEB_PORT:-18083}"
MYSQL_URL_VALUE="${MYSQL_URL:-jdbc:mysql://127.0.0.1:3306/scm_wms?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai}"
MYSQL_USERNAME_VALUE="${MYSQL_USERNAME:-root}"
MYSQL_PASSWORD_VALUE="${MYSQL_PASSWORD:-root}"
ROCKETMQ_ENDPOINTS_VALUE="${SCM_ROCKETMQ_ENDPOINTS:-127.0.0.1:8081}"
IAM_JWT_SECRET_VALUE="${IAM_JWT_SECRET:-scm-dubbo-smoke-jwt-secret-at-least-32-bytes}"
SMOKE_ID="${DUBBO_SMOKE_ID:-$(date +%s)}"
SMOKE_KEY="DUBBO-SMOKE-${SMOKE_ID}"
LOG_DIR="${SCRIPT_DIR}/.dubbo-smoke"
PROVIDER_PID=""

mkdir -p "${LOG_DIR}"

cleanup() {
  if [[ -n "${PROVIDER_PID}" ]] && kill -0 "${PROVIDER_PID}" 2>/dev/null; then
    kill "${PROVIDER_PID}" 2>/dev/null || true
    wait "${PROVIDER_PID}" 2>/dev/null || true
  fi
}
trap cleanup EXIT

build_artifacts() {
  cd "${BACKEND_DIR}"
  mvn -o -pl wms-service,supplier-service -am -DskipTests package
}

start_provider() {
  local log_file="$1"
  SCM_DUBBO_REGISTRY_ADDRESS="${REGISTRY_ADDRESS}" \
  SCM_DUBBO_PROTOCOL_PORT="${PROVIDER_PORT}" \
  SERVER_PORT="${WEB_PORT}" \
  SPRING_PROFILES_ACTIVE="test" \
  MYSQL_URL="${MYSQL_URL_VALUE}" \
  MYSQL_USERNAME="${MYSQL_USERNAME_VALUE}" \
  MYSQL_PASSWORD="${MYSQL_PASSWORD_VALUE}" \
  IAM_JWT_SECRET="${IAM_JWT_SECRET_VALUE}" \
  SCM_ROCKETMQ_ENABLED="true" \
  SCM_ROCKETMQ_ENDPOINTS="${ROCKETMQ_ENDPOINTS_VALUE}" \
  java -jar "${BACKEND_DIR}/wms-service/target/wms-service-0.1.0-SNAPSHOT.jar" \
    >"${log_file}" 2>&1 &
  PROVIDER_PID=$!
  for _ in {1..40}; do
    if ! kill -0 "${PROVIDER_PID}" 2>/dev/null; then
      tail -n 80 "${log_file}"
      return 1
    fi
    if nc -z 127.0.0.1 "${PROVIDER_PORT}" 2>/dev/null; then
      return 0
    fi
    sleep 1
  done
  tail -n 80 "${log_file}"
  return 1
}

stop_provider() {
  kill "${PROVIDER_PID}"
  wait "${PROVIDER_PID}" 2>/dev/null || true
  PROVIDER_PID=""
}

run_consumer() {
  java -Dloader.main=com.chaobo.scm.supplier.infrastructure.integration.DubboWmsSmokeClient \
    -cp "${BACKEND_DIR}/supplier-service/target/supplier-service-0.1.0-SNAPSHOT.jar" \
    org.springframework.boot.loader.launch.PropertiesLauncher \
    "${REGISTRY_ADDRESS}" "${SMOKE_ID}" "${SMOKE_KEY}"
}

build_artifacts
start_provider "${LOG_DIR}/provider-first.log"
run_consumer
stop_provider

if run_consumer >"${LOG_DIR}/consumer-without-provider.log" 2>&1; then
  echo "未注册 Provider 时 Consumer 未失败关闭" >&2
  exit 1
fi

start_provider "${LOG_DIR}/provider-restarted.log"
run_consumer

echo "Dubbo 跨 JVM 注册发现、未注册失败关闭、Provider 重启恢复和重复命令幂等验证通过。"
