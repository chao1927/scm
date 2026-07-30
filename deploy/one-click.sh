#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
DEPLOY_DIR="$ROOT_DIR/deploy"
ACTION=${1:-all}

usage() {
  cat <<'EOF'
用法: sudo ./deploy/one-click.sh <命令>

命令:
  all       安装 Docker、启动 Linux 中间件并安装单节点 K3s（默认）
  infra     安装 Docker并启动 Jenkins/MySQL/Redis/RocketMQ/Nacos/MinIO/Nginx
  k3s      安装单节点 K3s
  status    显示基础设施和 K3s 状态
  backup    备份 MySQL、Jenkins、Nacos、MinIO 和配置
  down      停止基础设施容器，保留数据
  destroy   删除基础设施容器；数据仍保留，需 ALLOW_DESTROY=true
EOF
}

require_root() {
  if [[ ${EUID} -ne 0 ]]; then
    echo "请使用 sudo 运行此命令。" >&2
    exit 1
  fi
}

case "$ACTION" in
  all)
    require_root
    "$DEPLOY_DIR/linux/install-infra.sh"
    "$DEPLOY_DIR/k3s/install-k3s.sh"
    "$DEPLOY_DIR/linux/status.sh"
    ;;
  infra)
    require_root
    "$DEPLOY_DIR/linux/install-infra.sh"
    "$DEPLOY_DIR/linux/status.sh"
    ;;
  k3s)
    require_root
    "$DEPLOY_DIR/k3s/install-k3s.sh"
    ;;
  status)
    "$DEPLOY_DIR/linux/status.sh"
    ;;
  backup)
    require_root
    "$DEPLOY_DIR/linux/backup.sh"
    ;;
  down)
    require_root
    docker compose --env-file "$DEPLOY_DIR/linux/.env" -f "$DEPLOY_DIR/linux/docker-compose.yml" down
    ;;
  destroy)
    require_root
    [[ ${ALLOW_DESTROY:-false} == true ]] || { echo "请显式设置 ALLOW_DESTROY=true。" >&2; exit 1; }
    docker compose --env-file "$DEPLOY_DIR/linux/.env" -f "$DEPLOY_DIR/linux/docker-compose.yml" down --remove-orphans
    ;;
  -h|--help|help) usage ;;
  *) usage; exit 2 ;;
esac
