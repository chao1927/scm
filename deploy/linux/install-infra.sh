#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
ENV_FILE="$SCRIPT_DIR/.env"
ENV_EXAMPLE="$SCRIPT_DIR/.env.example"

log() { printf '\n[%s] %s\n' "$(date '+%F %T')" "$*"; }
fail() { echo "错误: $*" >&2; exit 1; }
command_exists() { command -v "$1" >/dev/null 2>&1; }

install_docker() {
  if command_exists docker && docker compose version >/dev/null 2>&1; then
    log "Docker 与 Compose 已安装"
    return
  fi
  command_exists curl || fail "缺少 curl，请先安装 curl"
  log "通过 Docker 官方安装脚本安装 Docker Engine"
  local installer
  installer=$(mktemp)
  curl --proto '=https' --tlsv1.2 -fsSL https://get.docker.com -o "$installer"
  sh "$installer"
  rm -f "$installer"
  systemctl enable --now docker
}

random_hex() { openssl rand -hex "$1"; }
random_b64() { openssl rand -base64 "$1" | tr -d '\n'; }

write_env() {
  if [[ -f "$ENV_FILE" ]]; then
    chmod 600 "$ENV_FILE"
    log "保留现有 .env，不覆盖密码"
    return
  fi
  command_exists openssl || fail "缺少 openssl，无法安全生成初始密码"
  cp "$ENV_EXAMPLE" "$ENV_FILE"
  sed -i \
    -e "s|__MYSQL_ROOT_PASSWORD__|$(random_hex 24)|" \
    -e "s|__MYSQL_APP_PASSWORD__|$(random_hex 24)|" \
    -e "s|__REDIS_PASSWORD__|$(random_hex 24)|" \
    -e "s|__NACOS_IDENTITY_KEY__|$(random_hex 16)|" \
    -e "s|__NACOS_IDENTITY_VALUE__|$(random_hex 24)|" \
    -e "s|__NACOS_AUTH_TOKEN__|$(random_b64 48)|" \
    -e "s|__MINIO_ROOT_USER__|scmadmin|" \
    -e "s|__MINIO_ROOT_PASSWORD__|$(random_hex 24)|" \
    "$ENV_FILE"
  chmod 600 "$ENV_FILE"
  log "已生成 $ENV_FILE（权限 600）"
}

prepare_directories() {
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  [[ ${SCM_DATA_ROOT:-} == /* ]] || fail "SCM_DATA_ROOT 必须是绝对路径"
  install -d -m 0750 \
    "$SCM_DATA_ROOT/mysql" "$SCM_DATA_ROOT/redis" \
    "$SCM_DATA_ROOT/rocketmq/namesrv/logs" "$SCM_DATA_ROOT/rocketmq/broker/logs" \
    "$SCM_DATA_ROOT/rocketmq/broker/store" "$SCM_DATA_ROOT/nacos" \
    "$SCM_DATA_ROOT/minio" "$SCM_DATA_ROOT/jenkins" "$SCM_DATA_ROOT/nginx/logs"
  chown -R 1000:1000 "$SCM_DATA_ROOT/rocketmq" "$SCM_DATA_ROOT/jenkins"
}

wait_healthy() {
  local deadline=$((SECONDS + ${DEPLOY_TIMEOUT_SECONDS:-600}))
  while (( SECONDS < deadline )); do
    local unhealthy
    unhealthy=$(docker compose --env-file "$ENV_FILE" -f "$SCRIPT_DIR/docker-compose.yml" ps --format json 2>/dev/null \
      | sed -n 's/.*"Health":"\([^"]*\)".*/\1/p' \
      | grep -Ev '^(healthy|)$' || true)
    local running
    running=$(docker compose --env-file "$ENV_FILE" -f "$SCRIPT_DIR/docker-compose.yml" ps --status running -q | wc -l | tr -d ' ')
    if [[ -z "$unhealthy" && "$running" -ge 9 ]]; then
      return
    fi
    sleep 5
  done
  docker compose --env-file "$ENV_FILE" -f "$SCRIPT_DIR/docker-compose.yml" ps
  fail "基础设施未在超时时间内全部就绪"
}

install_docker
write_env
prepare_directories
log "校验 Compose 配置"
docker compose --env-file "$ENV_FILE" -f "$SCRIPT_DIR/docker-compose.yml" config --quiet
log "拉取镜像并启动基础设施"
docker compose --env-file "$ENV_FILE" -f "$SCRIPT_DIR/docker-compose.yml" pull
docker compose --env-file "$ENV_FILE" -f "$SCRIPT_DIR/docker-compose.yml" up -d --build --remove-orphans
wait_healthy
log "Linux 基础设施部署完成"
