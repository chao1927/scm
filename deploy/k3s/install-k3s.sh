#!/usr/bin/env bash
set -Eeuo pipefail

K3S_CHANNEL=${K3S_CHANNEL:-stable}
K3S_CONFIG_DIR=/etc/rancher/k3s
KUBECONFIG_TARGET=${KUBECONFIG_TARGET:-/root/.kube/config}

log() { printf '\n[%s] %s\n' "$(date '+%F %T')" "$*"; }

if command -v k3s >/dev/null 2>&1 && systemctl is-active --quiet k3s; then
  log "K3s 已运行，跳过安装"
else
  log "安装 K3s 单节点服务器（禁用内置 Traefik，入口统一由 Linux Nginx 管理）"
  install -d -m 0755 "$K3S_CONFIG_DIR"
  cat > "$K3S_CONFIG_DIR/config.yaml" <<'EOF'
write-kubeconfig-mode: "600"
disable:
  - traefik
secrets-encryption: true
protect-kernel-defaults: false
EOF
  installer=$(mktemp)
  curl --proto '=https' --tlsv1.2 -fsSL https://get.k3s.io -o "$installer"
  INSTALL_K3S_CHANNEL="$K3S_CHANNEL" sh "$installer"
  rm -f "$installer"
fi

log "等待 Kubernetes 节点 Ready"
k3s kubectl wait --for=condition=Ready node --all --timeout=180s
install -d -m 0700 "$(dirname "$KUBECONFIG_TARGET")"
install -m 0600 /etc/rancher/k3s/k3s.yaml "$KUBECONFIG_TARGET"
log "K3s 部署完成；kubeconfig: $KUBECONFIG_TARGET"
k3s kubectl get nodes -o wide
