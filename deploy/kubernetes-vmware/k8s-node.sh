#!/usr/bin/env bash
set -Eeuo pipefail

# 在 Ubuntu 24.04 节点上安装/初始化 Kubernetes。
# 本文件由 Windows 端 install-k8s.ps1 调用，不建议单独执行。

readonly CONTAINERD_SOCKET="unix:///run/containerd/containerd.sock"
readonly CALICO_VERSION="v3.32.1"

log() {
  printf '\n[%s] %s\n' "$(date '+%F %T')" "$*"
}

die() {
  printf '\n错误：%s\n' "$*" >&2
  exit 1
}

require_root() {
  [[ "${EUID}" -eq 0 ]] || die "请通过 sudo 执行此脚本"
}

regular_user() {
  if [[ -n "${SUDO_USER:-}" && "${SUDO_USER}" != "root" ]]; then
    printf '%s' "${SUDO_USER}"
  else
    id -un
  fi
}

regular_home() {
  getent passwd "$(regular_user)" | cut -d: -f6
}

check_ubuntu() {
  [[ -r /etc/os-release ]] || die "无法识别操作系统"
  # shellcheck disable=SC1091
  source /etc/os-release
  [[ "${ID:-}" == "ubuntu" ]] || die "本脚本只支持 Ubuntu，当前为 ${ID:-unknown}"
  [[ "${VERSION_ID:-}" == "24.04" ]] || die "本脚本按 Ubuntu 24.04 LTS 验证，当前为 ${VERSION_ID:-unknown}"
  [[ "$(uname -m)" == "x86_64" ]] || die "本脚本只支持 amd64/x86_64"
}

check_ip_on_host() {
  local expected_ip="$1"
  ip -4 -o addr show scope global | awk '{print $4}' | cut -d/ -f1 | grep -Fxq "${expected_ip}" \
    || die "本机网卡没有 IP ${expected_ip}；请先修正 VMware/Netplan 固定 IP"
}

prepare_node() {
  local node_name="$1"
  local kubernetes_minor="$2"

  require_root
  check_ubuntu
  [[ "${node_name}" =~ ^[a-z0-9]([-a-z0-9]*[a-z0-9])?$ ]] \
    || die "节点名 ${node_name} 不符合 Kubernetes 命名规则"
  [[ "${kubernetes_minor}" =~ ^v[0-9]+\.[0-9]+$ ]] \
    || die "Kubernetes 次版本格式应类似 v1.36"

  log "设置节点名与时间同步：${node_name}"
  hostnamectl set-hostname "${node_name}"
  timedatectl set-ntp true

  log "安装基础软件和 containerd"
  export DEBIAN_FRONTEND=noninteractive
  apt-get update
  apt-get install -y ca-certificates curl gpg containerd conntrack socat
  if command -v ufw >/dev/null 2>&1 && ufw status | grep -q '^Status: active'; then
    die "检测到 UFW 已启用。此实验集群请先执行 sudo ufw disable，或按 Kubernetes/Calico 官方端口清单精确放行后再运行"
  fi

  log "关闭 swap（kubelet 默认检测到 swap 时拒绝启动）"
  swapoff -a
  if [[ ! -f /etc/fstab.before-k8s ]]; then
    cp /etc/fstab /etc/fstab.before-k8s
  fi
  sed -ri '/^[[:space:]]*#/!{/^[^[:space:]]+[[:space:]]+[^[:space:]]+[[:space:]]+swap[[:space:]]/s/^/# k8s-disabled: /}' /etc/fstab

  log "加载容器网络所需内核模块和 sysctl"
  cat >/etc/modules-load.d/k8s.conf <<'EOF'
overlay
br_netfilter
EOF
  modprobe overlay
  modprobe br_netfilter
  cat >/etc/sysctl.d/99-kubernetes-cri.conf <<'EOF'
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF
  sysctl --system >/dev/null

  log "配置 containerd 使用 systemd cgroup"
  mkdir -p /etc/containerd
  containerd config default >/etc/containerd/config.toml
  sed -ri 's/(SystemdCgroup = )false/\1true/' /etc/containerd/config.toml
  grep -q 'SystemdCgroup = true' /etc/containerd/config.toml \
    || die "未能将 containerd 配置为 systemd cgroup"
  systemctl enable --now containerd
  systemctl restart containerd

  log "从 Kubernetes 官方软件仓库安装 kubelet、kubeadm、kubectl ${kubernetes_minor}.x"
  install -m 0755 -d /etc/apt/keyrings
  rm -f /etc/apt/keyrings/kubernetes-apt-keyring.gpg
  curl -fsSL "https://pkgs.k8s.io/core:/stable:/${kubernetes_minor}/deb/Release.key" \
    | gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
  cat >/etc/apt/sources.list.d/kubernetes.list <<EOF
deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/${kubernetes_minor}/deb/ /
EOF
  apt-get update
  apt-get install -y kubelet kubeadm kubectl
  apt-mark hold kubelet kubeadm kubectl
  systemctl enable --now kubelet

  cat >/etc/crictl.yaml <<EOF
runtime-endpoint: ${CONTAINERD_SOCKET}
image-endpoint: ${CONTAINERD_SOCKET}
timeout: 10
debug: false
EOF

  [[ "$(swapon --show --noheadings | wc -l)" -eq 0 ]] || die "swap 仍未完全关闭"
  sysctl net.ipv4.ip_forward | grep -q '= 1' || die "IPv4 转发未开启"
  systemctl is-active --quiet containerd || die "containerd 未运行"
  log "节点基础环境准备完成：$(kubeadm version -o short) / $(containerd --version)"
}

init_control_plane() {
  local control_plane_ip="$1"
  local pod_cidr="$2"
  local user user_home kubeconfig

  require_root
  check_ip_on_host "${control_plane_ip}"
  user="$(regular_user)"
  user_home="$(regular_home)"
  kubeconfig="${user_home}/.kube/config"

  if [[ ! -f /etc/kubernetes/admin.conf ]]; then
    log "预拉取 Kubernetes 控制平面镜像"
    kubeadm config images pull --cri-socket "${CONTAINERD_SOCKET}"

    log "初始化单控制平面集群：${control_plane_ip}:6443"
    kubeadm init \
      --apiserver-advertise-address "${control_plane_ip}" \
      --control-plane-endpoint "${control_plane_ip}:6443" \
      --node-name "$(hostname -s)" \
      --pod-network-cidr "${pod_cidr}" \
      --cri-socket "${CONTAINERD_SOCKET}"
  else
    log "检测到现有 /etc/kubernetes/admin.conf，跳过 kubeadm init"
  fi

  install -d -m 0700 -o "${user}" -g "${user}" "${user_home}/.kube"
  install -m 0600 -o "${user}" -g "${user}" /etc/kubernetes/admin.conf "${kubeconfig}"
  export KUBECONFIG=/etc/kubernetes/admin.conf

  if ! kubectl get installation.operator.tigera.io default >/dev/null 2>&1; then
    log "安装 Calico ${CALICO_VERSION} 网络插件"
    kubectl create -f "https://raw.githubusercontent.com/projectcalico/calico/${CALICO_VERSION}/manifests/v1_crd_projectcalico_org.yaml"
    kubectl create -f "https://raw.githubusercontent.com/projectcalico/calico/${CALICO_VERSION}/manifests/tigera-operator.yaml"
    kubectl wait --for=condition=Established crd/installations.operator.tigera.io --timeout=180s
    kubectl wait -n tigera-operator --for=condition=Available deployment/tigera-operator --timeout=300s

    cat >/tmp/calico-custom-resources.yaml <<EOF
apiVersion: operator.tigera.io/v1
kind: Installation
metadata:
  name: default
spec:
  calicoNetwork:
    ipPools:
      - blockSize: 26
        cidr: ${pod_cidr}
        encapsulation: VXLANCrossSubnet
        natOutgoing: Enabled
        nodeSelector: all()
EOF
    kubectl create -f /tmp/calico-custom-resources.yaml
  else
    log "Calico Installation 已存在，跳过重复安装"
  fi

  log "生成工作节点加入命令"
  kubeadm token create --ttl 2h --print-join-command \
    | sed "s|$| --cri-socket ${CONTAINERD_SOCKET}|" >"${user_home}/k8s-join.sh"
  chown "${user}:${user}" "${user_home}/k8s-join.sh"
  chmod 0600 "${user_home}/k8s-join.sh"
  log "控制平面初始化完成"
}

join_worker() {
  local join_file="$1"
  require_root
  [[ -s "${join_file}" ]] || die "加入命令文件不存在或为空：${join_file}"
  if [[ -f /etc/kubernetes/kubelet.conf ]]; then
    log "本节点已经加入集群，跳过 kubeadm join"
    return
  fi
  log "将 $(hostname -s) 加入集群"
  bash "${join_file}"
  log "工作节点加入完成"
}

verify_cluster() {
  local user_home
  user_home="$(regular_home)"
  export KUBECONFIG="${user_home}/.kube/config"
  [[ -r "${KUBECONFIG}" ]] || die "找不到 kubeconfig：${KUBECONFIG}"

  log "等待 3 个节点全部 Ready（最长 10 分钟）"
  local deadline=$((SECONDS + 600))
  while (( SECONDS < deadline )); do
    if [[ "$(kubectl get nodes --no-headers 2>/dev/null | wc -l)" -eq 3 ]] \
      && [[ "$(kubectl get nodes --no-headers 2>/dev/null | awk '$2 != "Ready" {count++} END {print count+0}')" -eq 0 ]]; then
      break
    fi
    sleep 10
  done
  [[ "$(kubectl get nodes --no-headers | wc -l)" -eq 3 ]] || die "集群节点数不是 3"
  kubectl wait node --all --for=condition=Ready --timeout=60s

  log "创建 nginx 冒烟测试并等待调度成功"
  kubectl create namespace k8s-smoke-test --dry-run=client -o yaml | kubectl apply -f -
  kubectl -n k8s-smoke-test create deployment nginx \
    --image=nginx:1.27-alpine --replicas=2 --dry-run=client -o yaml | kubectl apply -f -
  kubectl -n k8s-smoke-test rollout status deployment/nginx --timeout=300s

  printf '\n========== Kubernetes 安装成功 ==========\n'
  kubectl get nodes -o wide
  printf '\n'
  kubectl get pods -A
  printf '\n冒烟测试保留在 namespace/k8s-smoke-test，可执行 kubectl delete ns k8s-smoke-test 删除。\n'
}

usage() {
  cat <<'EOF'
用法：
  k8s-node.sh prepare <节点名> <Kubernetes 次版本，例如 v1.36>
  k8s-node.sh init <控制平面 IP> <Pod CIDR>
  k8s-node.sh join <加入命令文件>
  k8s-node.sh verify
EOF
}

mode="${1:-}"
case "${mode}" in
  prepare)
    [[ $# -eq 3 ]] || { usage; exit 2; }
    prepare_node "$2" "$3"
    ;;
  init)
    [[ $# -eq 3 ]] || { usage; exit 2; }
    init_control_plane "$2" "$3"
    ;;
  join)
    [[ $# -eq 2 ]] || { usage; exit 2; }
    join_worker "$2"
    ;;
  verify)
    [[ $# -eq 1 ]] || { usage; exit 2; }
    verify_cluster
    ;;
  *)
    usage
    exit 2
    ;;
esac
