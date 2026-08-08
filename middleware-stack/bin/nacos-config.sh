#!/usr/bin/env bash
set -Eeuo pipefail

STACK_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${STACK_DIR}/.env"
NACOS_GROUP="SCM_GROUP"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "缺少 ${ENV_FILE}，请先执行 ./bin/dev.sh up" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

login() {
  local response
  response="$(curl -fsS -X POST "http://127.0.0.1:${NACOS_API_PORT}/nacos/v3/auth/user/login" \
    --data-urlencode "username=${NACOS_USERNAME}" \
    --data-urlencode "password=${NACOS_PASSWORD}")"
  ACCESS_TOKEN="$(printf '%s' "${response}" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')"
  if [[ -z "${ACCESS_TOKEN}" ]]; then
    echo "Nacos 登录失败" >&2
    exit 1
  fi
}

namespace_list() {
  curl -fsS "http://127.0.0.1:${NACOS_CONSOLE_PORT}/v3/console/core/namespace/list?accessToken=${ACCESS_TOKEN}"
}

ensure_namespace() {
  local environment="$1"
  local namespace_id="scm-${environment}"
  if namespace_list | grep -q "\"namespace\":\"${namespace_id}\""; then
    echo "  ✓ namespace ${namespace_id}"
    return
  fi

  curl -fsS -X POST \
    "http://127.0.0.1:${NACOS_CONSOLE_PORT}/v3/console/core/namespace?accessToken=${ACCESS_TOKEN}" \
    --data-urlencode "customNamespaceId=${namespace_id}" \
    --data-urlencode "namespaceName=SCM-${environment}" \
    --data-urlencode "namespaceDesc=SCM ${environment} environment" >/dev/null
  echo "  ✓ 创建 namespace ${namespace_id}"
}

publish_config() {
  local namespace_id="$1"
  local data_id="$2"
  local content="$3"
  local response
  response="$(curl -fsS -X POST \
    "http://127.0.0.1:${NACOS_CONSOLE_PORT}/v3/console/cs/config?accessToken=${ACCESS_TOKEN}" \
    --data-urlencode "namespaceId=${namespace_id}" \
    --data-urlencode "groupName=${NACOS_GROUP}" \
    --data-urlencode "dataId=${data_id}" \
    --data-urlencode "type=yaml" \
    --data-urlencode "content=${content}")"
  if [[ "${response}" != *'"data":true'* ]]; then
    echo "发布 ${namespace_id}/${data_id} 失败" >&2
    exit 1
  fi
  echo "    ✓ ${data_id}"
}

delete_config_if_present() {
  local namespace_id="$1"
  local data_id="$2"
  local list_response
  local delete_response
  list_response="$(curl -fsS \
    "http://127.0.0.1:${NACOS_CONSOLE_PORT}/v3/console/cs/config/list?namespaceId=${namespace_id}&groupName=${NACOS_GROUP}&pageNo=1&pageSize=100&accessToken=${ACCESS_TOKEN}")"
  if [[ "${list_response}" != *"\"dataId\":\"${data_id}\""* ]]; then
    return
  fi
  delete_response="$(curl -fsS -X DELETE \
    "http://127.0.0.1:${NACOS_CONSOLE_PORT}/v3/console/cs/config?accessToken=${ACCESS_TOKEN}" \
    --data-urlencode "namespaceId=${namespace_id}" \
    --data-urlencode "groupName=${NACOS_GROUP}" \
    --data-urlencode "dataId=${data_id}")"
  if [[ "${delete_response}" != *'"data":true'* ]]; then
    echo "删除遗留配置 ${namespace_id}/${data_id} 失败" >&2
    exit 1
  fi
  echo "    ✓ 删除遗留配置 ${data_id}"
}

common_config() {
  local max_pool="$1"
  local min_idle="$2"
  printf 'spring:\n  datasource:\n    username: %s\n    password: %s\n    hikari:\n      maximum-pool-size: %s\n      minimum-idle: %s\n      connection-timeout: 3000\nscm:\n  logging:\n    path: ./logs\n    max-file-size: 100MB\n    max-history: 7\n    info-total-size-cap: 2GB\n    error-total-size-cap: 1GB\n' \
    "${MYSQL_APP_USER}" "${MYSQL_APP_PASSWORD}" "${max_pool}" "${min_idle}"
}

service_config() {
  local environment="$1"
  local service_name="$2"
  local database_name="$3"
  local server_port="$4"
  local mysql_host="127.0.0.1"
  if [[ "${environment}" == "prod" ]]; then
    mysql_host="mysql"
  fi
  printf 'spring:\n  datasource:\n    url: jdbc:mysql://%s:3306/%s?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai\nserver:\n  port: %s\n' \
    "${mysql_host}" "${database_name}" "${server_port}"
}

supplier_config() {
  local environment="$1"
  local mysql_host="127.0.0.1"
  local redis_host="127.0.0.1"
  local rocketmq_host="127.0.0.1"
  local nacos_host="127.0.0.1"
  if [[ "${environment}" == "prod" ]]; then
    mysql_host="mysql"
    redis_host="redis"
    rocketmq_host="rocketmq"
    nacos_host="nacos"
  fi
  printf 'spring:\n  datasource:\n    url: jdbc:mysql://%s:3306/scm_supplier?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai\n  data:\n    redis:\n      host: %s\n      port: 6379\n      username: %s\n      password: %s\n      database: 1\n      timeout: 2s\nserver:\n  port: 8101\nscm:\n  dubbo:\n    registry-address: "nacos://%s:8848?username=%s&password=%s"\n    timeout-ms: 2000\n  rocketmq:\n    enabled: true\n    endpoints: %s:8081\n    topic: supplier-domain-event\n    master-data-consumer:\n      enabled: true\n      topic: master-data-domain-event\n      group: supplier-master-data-snapshot\n    contract-approval-consumer:\n      enabled: true\n      topic: iam-approval-domain-event\n      group: supplier-contract-approval\n' \
    "${mysql_host}" "${redis_host}" "${REDIS_USERNAME}" "${REDIS_PASSWORD}" \
    "${nacos_host}" "${NACOS_USERNAME}" "${NACOS_PASSWORD}" "${rocketmq_host}"
}

purchase_config() {
  local environment="$1"
  local mysql_host="127.0.0.1"
  local rocketmq_host="127.0.0.1"
  if [[ "${environment}" == "prod" ]]; then
    mysql_host="mysql"
    rocketmq_host="rocketmq"
  fi
  printf 'spring:\n  datasource:\n    url: jdbc:mysql://%s:3306/scm_purchase?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai\nserver:\n  port: 8102\nscm:\n  rocketmq:\n    enabled: true\n    endpoints: %s:8081\n    purchase-topic: purchase-domain-event\n' \
    "${mysql_host}" "${rocketmq_host}"
}

publish_environment() {
  local environment="$1"
  local namespace_id="scm-${environment}"
  local max_pool="10"
  local min_idle="2"
  if [[ "${environment}" == "prod" ]]; then
    max_pool="20"
    min_idle="4"
  fi

  echo "发布 ${namespace_id}/${NACOS_GROUP}："
  publish_config "${namespace_id}" "scm-common-${environment}.yaml" "$(common_config "${max_pool}" "${min_idle}")"
  publish_config "${namespace_id}" "iam-service-${environment}.yaml" "$(service_config "${environment}" iam-service scm_iam 8097)"
  publish_config "${namespace_id}" "mdm-service-${environment}.yaml" "$(service_config "${environment}" mdm-service scm_mdm 8098)"
  publish_config "${namespace_id}" "supplier-service-${environment}.yaml" "$(supplier_config "${environment}")"
  publish_config "${namespace_id}" "purchase-service-${environment}.yaml" "$(purchase_config "${environment}")"
  publish_config "${namespace_id}" "wms-service-${environment}.yaml" "$(service_config "${environment}" wms-service scm_wms 8103)"
  publish_config "${namespace_id}" "inventory-service-${environment}.yaml" "$(service_config "${environment}" inventory-service scm_inventory 8104)"
  publish_config "${namespace_id}" "oms-service-${environment}.yaml" "$(service_config "${environment}" oms-service scm_oms 8099)"
  publish_config "${namespace_id}" "tms-service-${environment}.yaml" "$(service_config "${environment}" tms-service scm_tms 8100)"
  publish_config "${namespace_id}" "bms-service-${environment}.yaml" "$(service_config "${environment}" bms-service scm_bms 8110)"
}

config_count() {
  local environment="$1"
  local response
  response="$(curl -fsS \
    "http://127.0.0.1:${NACOS_CONSOLE_PORT}/v3/console/cs/config/list?namespaceId=scm-${environment}&groupName=${NACOS_GROUP}&pageNo=1&pageSize=100&accessToken=${ACCESS_TOKEN}")"
  printf '%s' "${response}" | grep -o '"dataId":"' | wc -l | tr -d ' '
}

login
ensure_namespace test
ensure_namespace prod
for environment in test prod; do
  delete_config_if_present "scm-${environment}" "integration-service-${environment}.yaml"
  delete_config_if_present "scm-${environment}" "report-service-${environment}.yaml"
done
publish_environment test
publish_environment prod

for environment in test prod; do
  count="$(config_count "${environment}")"
  if [[ "${count}" != "10" ]]; then
    echo "scm-${environment} 预期 10 个 DataId，实际 ${count}" >&2
    exit 1
  fi
  echo "  ✓ scm-${environment} 共 ${count} 个 DataId"
done

echo "Nacos namespace 与 DataId 发布完成。"
