#!/usr/bin/env bash
set -Eeuo pipefail

STACK_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${STACK_DIR}/.env"
COMPOSE_FILE="${STACK_DIR}/docker-compose.yml"

cd "${STACK_DIR}"

random_value() {
  openssl rand -hex 18
}

create_env_if_missing() {
  if [[ -f "${ENV_FILE}" ]]; then
    return
  fi

  umask 077
  local mysql_root_password mysql_app_password mysql_nacos_password
  local redis_password nacos_password identity_key identity_value auth_token nginx_password
  local iam_jwt_secret iam_mfa_master_key bms_external_shared_secret
  mysql_root_password="R$(random_value)"
  mysql_app_password="A$(random_value)"
  mysql_nacos_password="N$(random_value)"
  redis_password="R$(random_value)"
  nacos_password="N$(random_value)"
  identity_key="K$(random_value)"
  identity_value="V$(random_value)"
  auth_token="$(openssl rand -base64 48 | tr -d '\r\n')"
  nginx_password="W$(random_value)"
  iam_jwt_secret="$(openssl rand -hex 32)"
  iam_mfa_master_key="$(openssl rand -hex 16)"
  bms_external_shared_secret="$(openssl rand -hex 32)"

  {
    echo "TZ=Asia/Shanghai"
    echo
    echo "MYSQL_PORT=3306"
    echo "MYSQL_ROOT_PASSWORD=${mysql_root_password}"
    echo "MYSQL_APP_USER=scm_app"
    echo "MYSQL_APP_PASSWORD=${mysql_app_password}"
    echo "MYSQL_NACOS_USER=nacos"
    echo "MYSQL_NACOS_PASSWORD=${mysql_nacos_password}"
    echo
    echo "REDIS_PORT=6379"
    echo "REDIS_USERNAME=scm_app"
    echo "REDIS_PASSWORD=${redis_password}"
    echo
    echo "ROCKETMQ_NAMESRV_PORT=9876"
    echo "ROCKETMQ_BROKER_PORT=10911"
    echo "ROCKETMQ_BROKER_FAST_PORT=10909"
    echo "ROCKETMQ_HA_PORT=10912"
    echo "ROCKETMQ_PROXY_GRPC_PORT=8081"
    echo "ROCKETMQ_PROXY_REMOTING_PORT=8082"
    echo
    echo "NACOS_API_PORT=8848"
    echo "NACOS_CONSOLE_PORT=8080"
    echo "NACOS_GRPC_PORT=9848"
    echo "NACOS_USERNAME=nacos"
    echo "NACOS_PASSWORD=${nacos_password}"
    echo "NACOS_AUTH_IDENTITY_KEY=${identity_key}"
    echo "NACOS_AUTH_IDENTITY_VALUE=${identity_value}"
    echo "NACOS_AUTH_TOKEN=${auth_token}"
    echo
    echo "IAM_JWT_SECRET=${iam_jwt_secret}"
    echo "IAM_MFA_MASTER_KEY=${iam_mfa_master_key}"
    echo "BMS_EXTERNAL_SHARED_SECRET=${bms_external_shared_secret}"
    echo
    echo "NGINX_HTTP_PORT=8088"
    echo "NGINX_USERNAME=admin"
    echo "NGINX_PASSWORD=${nginx_password}"
  } > "${ENV_FILE}"
  chmod 600 "${ENV_FILE}"
  echo "已生成本机随机账号密码：${ENV_FILE}"
}

ensure_application_secrets() {
  local changed="false"
  umask 077
  if ! grep -q '^IAM_JWT_SECRET=' "${ENV_FILE}"; then
    printf '\nIAM_JWT_SECRET=%s\n' "$(openssl rand -hex 32)" >>"${ENV_FILE}"
    changed="true"
  fi
  if ! grep -q '^IAM_MFA_MASTER_KEY=' "${ENV_FILE}"; then
    printf 'IAM_MFA_MASTER_KEY=%s\n' "$(openssl rand -hex 16)" >>"${ENV_FILE}"
    changed="true"
  fi
  local mfa_master_key
  mfa_master_key="$(sed -n 's/^IAM_MFA_MASTER_KEY=//p' "${ENV_FILE}" | tail -n 1)"
  if [[ "${#mfa_master_key}" -ne 32 ]]; then
    local replacement_file="${ENV_FILE}.replacement.$$"
    awk -v replacement="IAM_MFA_MASTER_KEY=$(openssl rand -hex 16)" \
      '/^IAM_MFA_MASTER_KEY=/{print replacement; next} {print}' \
      "${ENV_FILE}" >"${replacement_file}"
    mv "${replacement_file}" "${ENV_FILE}"
    changed="true"
  fi
  if ! grep -q '^BMS_EXTERNAL_SHARED_SECRET=' "${ENV_FILE}"; then
    printf 'BMS_EXTERNAL_SHARED_SECRET=%s\n' "$(openssl rand -hex 32)" >>"${ENV_FILE}"
    changed="true"
  fi
  chmod 600 "${ENV_FILE}"
  if [[ "${changed}" == "true" ]]; then
        echo "已为现有 .env 补齐独立的应用本地密钥。"
  fi
}

load_env() {
  create_env_if_missing
  ensure_application_secrets
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
}

generate_runtime_files() {
  mkdir -p "${STACK_DIR}/runtime/redis" "${STACK_DIR}/runtime/nginx"

  local redis_password_sha
  redis_password_sha="$(printf '%s' "${REDIS_PASSWORD}" | shasum -a 256 | awk '{print $1}')"
  {
    echo "user default off"
    echo "user ${REDIS_USERNAME} reset on #${redis_password_sha} ~scm:* &scm:* +@read +@write +@connection +@transaction +@pubsub +@scripting -@admin -@dangerous +info"
  } > "${STACK_DIR}/runtime/redis/users.acl"
  chmod 600 "${STACK_DIR}/runtime/redis/users.acl"

  printf '%s:%s\n' "${NGINX_USERNAME}" "$(printf '%s' "${NGINX_PASSWORD}" | openssl passwd -6 -stdin)" \
    > "${STACK_DIR}/runtime/nginx/htpasswd"
  chmod 600 "${STACK_DIR}/runtime/nginx/htpasswd"
}

compose() {
  docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" "$@"
}

wait_for_health() {
  local container="$1"
  local timeout_seconds="${2:-300}"
  local elapsed=0
  local state

  while (( elapsed < timeout_seconds )); do
    state="$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "${container}" 2>/dev/null || true)"
    if [[ "${state}" == "healthy" ]]; then
      echo "  ✓ ${container}"
      return 0
    fi
    if [[ "${state}" == "unhealthy" || "${state}" == "exited" || "${state}" == "dead" ]]; then
      echo "${container} 状态异常：${state}" >&2
      compose logs --tail=100 "${container#scm-}" 2>/dev/null || true
      return 1
    fi
    sleep 3
    elapsed=$((elapsed + 3))
  done

  echo "等待 ${container} 健康检查超时" >&2
  return 1
}

initialize_nacos_admin() {
  local login_url="http://127.0.0.1:${NACOS_API_PORT}/nacos/v3/auth/user/login"
  local admin_url="http://127.0.0.1:${NACOS_API_PORT}/nacos/v3/auth/user/admin"

  if curl -fsS -X POST "${login_url}" \
      --data-urlencode "username=${NACOS_USERNAME}" \
      --data-urlencode "password=${NACOS_PASSWORD}" 2>/dev/null | grep -q '"accessToken"'; then
    echo "  ✓ Nacos 管理员鉴权"
    return
  fi

  # 初始化接口只能成功一次；已有管理员时不会覆盖密码。
  curl -fsS -X POST "${admin_url}" --data-urlencode "password=${NACOS_PASSWORD}" >/dev/null 2>&1 || true

  if ! curl -fsS -X POST "${login_url}" \
      --data-urlencode "username=${NACOS_USERNAME}" \
      --data-urlencode "password=${NACOS_PASSWORD}" | grep -q '"accessToken"'; then
    echo "Nacos 管理员登录失败。若复用了旧数据卷，请恢复旧密码或执行 ./bin/dev.sh reset。" >&2
    return 1
  fi
  echo "  ✓ Nacos 管理员初始化与鉴权"
}

initialize_rocketmq_topics() {
  local topic consumer_group
  for topic in \
    supplier-domain-event \
    purchase-domain-event \
    wms-domain-event \
    inventory-domain-event \
    iam-domain-event \
    mdm-domain-event \
    oms-domain-event \
    tms-domain-event \
    bms-domain-event \
    master-data-domain-event \
    iam-approval-domain-event \
    mdm-publication-receipt \
    supplier-operations-event; do
    compose exec -T rocketmq-broker sh mqadmin updateTopic \
      -n rocketmq-namesrv:9876 \
      -c DefaultCluster \
      -t "${topic}" >/dev/null
    echo "  ✓ RocketMQ Topic ${topic}"
  done
  for consumer_group in \
    supplier-master-data-snapshot \
    supplier-contract-approval \
    supplier-business-event-v1 \
    purchase-business-event-consumer \
    wms-business-event-consumer \
    inventory-domain-event-consumer \
    iam-business-event-consumer \
    mdm-business-event-consumer \
    oms-business-event-consumer \
    tms-business-event-consumer \
    bms-business-event-consumer; do
    compose exec -T rocketmq-broker sh mqadmin updateSubGroup \
      -n rocketmq-namesrv:9876 \
      -c DefaultCluster \
      -g "${consumer_group}" >/dev/null
    echo "  ✓ RocketMQ Consumer Group ${consumer_group}"
  done
}

check_services() {
  echo "执行连通性与鉴权检查..."
  compose exec -T mysql mysqladmin ping -h 127.0.0.1 -uroot "-p${MYSQL_ROOT_PASSWORD}" --silent
  echo "  ✓ MySQL"
  compose exec -T redis redis-cli --user "${REDIS_USERNAME}" --pass "${REDIS_PASSWORD}" --no-auth-warning ping | grep -q PONG
  echo "  ✓ Redis ACL"
  # Broker 对 Mac 广播 127.0.0.1，因此从 Broker 自身执行管理检查，能同时访问 NameServer 与广播的 Broker 地址。
  compose exec -T rocketmq-broker sh mqadmin clusterList -n rocketmq-namesrv:9876 >/dev/null
  echo "  ✓ RocketMQ NameServer/Broker"
  initialize_nacos_admin
  curl -fsS -u "${NGINX_USERNAME}:${NGINX_PASSWORD}" "http://127.0.0.1:${NGINX_HTTP_PORT}/" >/dev/null
  echo "  ✓ Nginx Basic Auth 与 Nacos 控制台代理"

  local container memory_limit
  for container in scm-mysql scm-redis scm-rocketmq-namesrv scm-rocketmq-broker scm-nacos scm-nginx; do
    memory_limit="$(docker inspect --format '{{.HostConfig.Memory}}' "${container}")"
    if [[ "${memory_limit}" != "1073741824" ]]; then
      echo "${container} 内存限制不是 1GiB：${memory_limit}" >&2
      return 1
    fi
  done
  echo "  ✓ 每个运行组件容器内存上限为 1GiB"
}

show_endpoints() {
  echo
  echo "本机连接地址："
  echo "  MySQL       127.0.0.1:${MYSQL_PORT}  用户 ${MYSQL_APP_USER}"
  echo "  Redis       127.0.0.1:${REDIS_PORT}  用户 ${REDIS_USERNAME}"
  echo "  RocketMQ    gRPC 127.0.0.1:${ROCKETMQ_PROXY_GRPC_PORT}；NameServer 127.0.0.1:${ROCKETMQ_NAMESRV_PORT}"
  echo "  Nacos API   127.0.0.1:${NACOS_API_PORT}  用户 ${NACOS_USERNAME}"
  echo "  Nacos UI    http://127.0.0.1:${NACOS_CONSOLE_PORT}/"
  echo "  Nginx UI    http://127.0.0.1:${NGINX_HTTP_PORT}/  用户 ${NGINX_USERNAME}"
  echo "  密码文件    ${ENV_FILE}"
}

up() {
  load_env
  generate_runtime_files
  compose up -d
  echo "等待组件就绪..."
  wait_for_health scm-mysql
  wait_for_health scm-redis
  wait_for_health scm-rocketmq-namesrv
  wait_for_health scm-rocketmq-broker
  wait_for_health scm-nacos
  wait_for_health scm-nginx
  initialize_rocketmq_topics
  check_services
  show_endpoints
}

reset_stack() {
  load_env
  if [[ "${2:-}" != "--yes" ]]; then
    read -r -p "这会删除本地中间件全部 named volumes，输入 RESET 继续：" answer
    [[ "${answer}" == "RESET" ]] || {
      echo "已取消"
      return
    }
  fi
  compose down -v --remove-orphans
  echo "本地中间件容器、网络和数据卷已删除；.env 账号密码仍保留。"
}

command="${1:-up}"
case "${command}" in
  up)
    up
    ;;
  down)
    load_env
    compose down
    ;;
  restart)
    load_env
    generate_runtime_files
    compose restart
    ;;
  status)
    load_env
    compose ps
    ;;
  logs)
    load_env
    compose logs -f --tail=200 "${2:-}"
    ;;
  check)
    load_env
    check_services
    show_endpoints
    ;;
  config)
    load_env
    generate_runtime_files
    compose config
    ;;
  reset)
    reset_stack "$@"
    ;;
  *)
    echo "用法：$0 {up|down|restart|status|logs [service]|check|config|reset [--yes]}" >&2
    exit 2
    ;;
esac
