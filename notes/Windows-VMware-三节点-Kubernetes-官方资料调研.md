# Windows + VMware 三节点 Kubernetes 官方资料调研

> 调研时间：2026-07-20  
> 目标：为“Windows 宿主机上用 VMware Workstation 运行 3 台 Ubuntu Server（每台 8 GiB），使用 kubeadm + containerd + Calico 搭建 1 控制平面 + 2 工作节点集群”提供可追溯依据。  
> 资料边界：只引用 VMware/Broadcom、Canonical Ubuntu、Kubernetes、containerd 和 Calico/Tigera 官方文档或官方仓库。

## 1. 结论摘要

1. 本方案可行。3 台虚拟机每台 8 GiB 显著高于 kubeadm 的每节点 2 GiB 下限，也高于 Ubuntu Server 24.04 LTS 的建议最低内存。控制平面至少需 2 CPU，节点间必须全互通。[创建 kubeadm 集群](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/)、[Ubuntu Server 系统要求](https://ubuntu.com/server/docs/reference/installation/system-requirements/)
2. 截至调研日，Kubernetes 官方当前文档为 v1.36；Calico 3.32 官方测试 Kubernetes 1.34–1.36。因此可复现的默认组合建议为 **Kubernetes v1.36 小版本仓库 + Calico v3.32.1**。[Calico Kubernetes 系统要求](https://docs.tigera.io/calico/latest/getting-started/kubernetes/requirements)
3. 容器运行时采用 containerd；所有节点都要安装 CRI 运行时。kubelet 与 containerd 必须使用一致的 cgroup 驱动，systemd 系统上建议两者都用 `systemd`。[Kubernetes 容器运行时](https://kubernetes.io/docs/setup/production-environment/container-runtimes/)、[containerd CRI 配置](https://github.com/containerd/containerd/blob/main/docs/cri/config.md)
4. 默认 kubelet 检测到 swap 会拒绝启动。实验集群中最简单、可预期的做法是在 3 台 Ubuntu 上禁用 swap 并从持久配置中移除/注释 swap。[安装 kubeadm：Swap configuration](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/#swap-configuration)
5. Kubernetes 不会自动提供 Pod 网络，必须只安装一个 CNI。本方案选 Calico，并将官方清单默认的 `192.168.0.0/16` 改成 `10.244.0.0/16`，以减少与常见 VMware NAT、家庭网络的冲突；仍必须确认它不与 Windows 物理网段、VMware VMnet 网段、VPN 网段及 Service CIDR 重叠。CNI 安装前 CoreDNS 不会正常运行。[创建 kubeadm 集群：安装 Pod 网络](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/#pod-network)、[Calico 本地部署安装](https://docs.tigera.io/calico/latest/getting-started/kubernetes/self-managed-onprem/onpremises)

## 2. Windows 宿主机与 VMware 前置条件

| 检查项 | 官方事实 | 本方案的可执行建议 |
| --- | --- | --- |
| CPU 与虚拟化 | VMware Workstation 要求兼容的 64 位 x86/AMD64 CPU；Intel 需 VT-x，AMD 需 AMD-V。[官方系统要求](https://knowledge.broadcom.com/external/article?legacyId=90112) | 在 BIOS/UEFI 中启用 Intel VT-x/VT-d 或 AMD-V/SVM。Kubernetes 本身不需要“嵌套虚拟化”，不必在 Linux VM 中再暴露 VT-x/AMD-V。 |
| Windows 版本 | Workstation Pro 17.x/25H2/26H1 官方列表要求 64 位宿主 OS，并列出支持的 Windows 10/11 版本。[官方宿主 OS 矩阵](https://knowledge.broadcom.com/external/article?legacyId=80807) | 使用当前受支持的 64 位 Windows 10/11 和当前 Workstation Pro，不用已结束支持的旧版本。 |
| VMware 获取 | Workstation Pro 17.5.2 及以上可供个人、教育和商业用户免费使用，需通过 Broadcom Support Portal 下载。[官方下载与授权说明](https://knowledge.broadcom.com/external/article/368667/download-and-license-vmware-desktop-hype.html) | 下载当前稳定版，用管理员身份安装。[官方安装步骤](https://knowledge.broadcom.com/external/article/387947/installing-vmware-workstation-pro.html) |
| 宿主内存 | 3 台 VM 各 8 GiB，仅 VM 分配就是 24 GiB。 | **工程推断**：宿主机 32 GiB 是勉强可用下限，48/64 GiB 更稳妥；必须给 Windows 和 VMware 自身留内存，不能把全部物理内存分给 VM。 |
| CPU 与磁盘 | kubeadm 要求控制平面至少 2 CPU；Ubuntu Server 24.04 建议至少 25 GB 存储。[创建 kubeadm 集群](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/)、[Ubuntu 系统要求](https://ubuntu.com/server/docs/reference/installation/system-requirements/) | **工程建议**：每台 4 vCPU、8 GiB RAM、80 GiB 动态磁盘；至少保证 control-plane 2 vCPU。三台同时高负载时不应严重超分物理 CPU。 |

### VMware 网络建议（工程选型）

- 实验环境优先用同一个 VMware NAT/VMnet 网段：3 台 VM 可互通、可上网拉取包与镜像，又不直接暴露到办公/家庭局域网。
- 固定 3 个节点 IP，例如 `192.168.56.10/11/12`；实际地址必须以 VMware Virtual Network Editor 中的真实 VMnet 网段为准，不可盲抄示例。
- 固定 IP 是必要的运维约束：Kubernetes 控制平面 IP 会写入证书 SAN，事后改 IP 需重签证书和重启组件。[创建 kubeadm 集群：Network setup](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/#network-setup)
- 每个节点必须有唯一 hostname、MAC 和 `product_uuid`，否则安装可能失败。[安装 kubeadm：Before you begin](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/#before-you-begin)

## 3. Ubuntu 选型与节点基线

### 3.1 建议版本

- 使用 **Ubuntu Server 24.04 LTS amd64**，不安装桌面。Ubuntu 官方文档当前以最新 LTS 为目标；Server ISO 可从 Ubuntu 官方 release 站下载。[Ubuntu Server 基础安装](https://ubuntu.com/server/docs/tutorial/basic-installation/)
- Ubuntu 24.04 的默认内核高于 Calico 3.32 要求的 Linux 5.10；Calico 明确列出 Ubuntu 20.04+ 为已知良好支持发行版。[Calico 系统要求](https://docs.tigera.io/calico/latest/getting-started/kubernetes/requirements)

### 3.2 每台节点必须做的基线配置

| 项目 | 结论与依据 |
| --- | --- |
| 节点命名 | 建议 `k8s-cp1`、`k8s-worker1`、`k8s-worker2`，三者 hostname 唯一。Kubernetes 官方要求 hostname/MAC/product_uuid 唯一。[安装 kubeadm](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/) |
| 名称解析 | 在 3 台 VM 的 `/etc/hosts` 中配置三个固定 IP 与 hostname，或使用可靠 DNS。节点间要有完整网络连通。[创建 kubeadm 集群](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/) |
| Swap | 执行 `swapoff -a` 只是临时禁用；还需从 `/etc/fstab`、`systemd.swap` 等实际配置源持久禁用。[官方 Swap 配置](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/#swap-configuration) |
| IPv4 转发 | 持久设置 `net.ipv4.ip_forward = 1` 并执行 `sysctl --system`；Kubernetes 官方容器运行时文档明确要求检查该值为 1。[官方网络前置](https://kubernetes.io/docs/setup/production-environment/container-runtimes/#network-configuration) |
| 内核模块 | 建议持久加载 `overlay` 和 `br_netfilter`，并设置 `net.bridge.bridge-nf-call-iptables=1`、`net.bridge.bridge-nf-call-ip6tables=1`。`overlay` 对应 containerd 的默认 `overlayfs` snapshotter；`br_netfilter` 使 bridge 流量可进入 netfilter/iptables。当前 Kubernetes 英文页将网络细节交给 CNI，而 containerd 官方 CRI 文档明确默认 snapshotter 为 `overlayfs`。[containerd CRI 配置](https://github.com/containerd/containerd/blob/main/docs/cri/config.md#snapshotter)、[Kubernetes 官方网络说明](https://kubernetes.io/docs/setup/production-environment/container-runtimes/#network-configuration) |
| 防火墙/iptables 管理器 | Calico 要求不要让 Firewalld 或其他 iptables 管理器干扰它生成的规则。实验环境最简单是禁用 UFW；若不禁用，必须明确开放 Kubernetes 与 Calico 端口/协议并验证规则不被改写。[Calico 节点要求](https://docs.tigera.io/calico/latest/getting-started/kubernetes/requirements#node-requirements) |
| 时间同步 | **工程建议**：启用 Ubuntu 默认时间同步并确认 3 台 VM 时间一致，避免证书有效期判断与日志排障受影响。 |

> 关于内核模块的边界：Calico 并不声称只需 `overlay`/`br_netfilter`。它还要求 iptables/ipset/conntrack，以及所选封装模式的 IPIP、VXLAN 或 WireGuard 内核支持；官方说明 Ubuntu 20.04+ 默认具备所需内核依赖。[Calico Kernel dependencies](https://docs.tigera.io/calico/latest/getting-started/bare-metal/requirements#kernel-dependencies)

## 4. containerd 与 cgroup 核对

1. Kubernetes 每个节点必须有 CRI 兼容容器运行时；containerd 的已知 socket 为 `unix:///var/run/containerd/containerd.sock`。Docker Engine 本身不实现 CRI，所以此方案不需要 Docker。[安装 kubeadm：Installing a container runtime](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/#installing-runtime)
2. kubelet 和容器运行时必须使用相同 cgroup 驱动；对 systemd 发行版，Kubernetes 建议用 `systemd`。kubeadm 从 v1.22 起在未显式配置时将 kubelet 默认为 `systemd`。[Kubernetes cgroup 驱动配置](https://kubernetes.io/docs/tasks/administer-cluster/kubeadm/configure-cgroup-driver/)
3. containerd 配置文件默认在 `/etc/containerd/config.toml`，可用 `containerd config default` 生成。[containerd Getting Started](https://github.com/containerd/containerd/blob/main/docs/getting-started.md#customizing-containerd)
4. `SystemdCgroup = true` 的表路径依 containerd 主版本而不同，一键脚本必须分支处理，不能用一个盲目 `sed` 同时假设 1.x 和 2.x：
   - containerd 2.x：`[plugins.'io.containerd.cri.v1.runtime'.containerd.runtimes.runc.options]`
   - containerd 1.x：`[plugins."io.containerd.grpc.v1.cri".containerd.runtimes.runc.options]`
   - 两者都设 `SystemdCgroup = true`。[containerd 官方 CRI Cgroup Driver](https://github.com/containerd/containerd/blob/main/docs/cri/config.md#cgroup-driver)
5. 脚本应确认 CRI 插件没有出现在 `disabled_plugins` 中，然后重启并启用 containerd；否则 kubeadm 无法通过 CRI 使用它。containerd 官方配置手册定义了 `disabled_plugins`。[containerd config.toml 手册](https://github.com/containerd/containerd/blob/main/docs/man/containerd-config.toml.5.md)

## 5. Kubernetes 软件源、版本与 kubeadm

### 5.1 软件源

- 使用 `pkgs.k8s.io`；2023-09-13 之后发布的 Kubernetes 版本必须使用新包仓库。[安装 kubeadm](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/#installing-kubeadm-kubelet-and-kubectl)
- `pkgs.k8s.io` 的仓库 URL 按 Kubernetes **minor version** 区分。v1.36 使用 `https://pkgs.k8s.io/core:/stable:/v1.36/deb/`；切换其他小版本必须同步改 URL。[官方 Debian 包安装步骤](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/#installing-kubeadm-kubelet-and-kubectl)
- 在所有节点安装 `kubelet kubeadm kubectl`，并用 `apt-mark hold` 锁定；官方警告这些组件的版本不匹配可导致不可预期行为。[安装 kubeadm](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/#installing-kubeadm-kubelet-and-kubectl)

### 5.2 集群初始化的关键约束

- 本地学习集群为单控制平面，不是高可用集群；`k8s-cp1` 故障时控制面不可用。kubeadm 的 HA 官方方案需要共享控制平面 endpoint/负载均衡器和多控制平面节点，超出本三节点学习方案边界。[官方 kubeadm HA 指南](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/high-availability/)
- `kubeadm init` 要显式使用固定的 API Server advertise address，并使用与 Calico 自定义资源一致的 `--pod-network-cidr=10.244.0.0/16`。[创建 kubeadm 集群](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/)
- `kubeadm init` 输出的 `kubeadm join ... --token ... --discovery-token-ca-cert-hash ...` 是敏感凭据，一键编排应只通过受信 SSH 传给工作节点，不应写入版本库或长期日志。Kubernetes 官方明确指出，持有该 token 者可将已认证节点加入集群。[创建 kubeadm 集群：Initializing control plane](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/#initializing-your-control-plane-node)
- `admin.conf` 拥有集群管理员权限，不可公开或共享；一键脚本应将它复制到控制平面普通用户的 `~/.kube/config` 并保持最小可读权限。[官方 kubeconfig 安全警告](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/#initializing-your-control-plane-node)

## 6. 端口、协议与网络开放

### 6.1 Kubernetes 默认端口

| 节点 | 方向 | 协议/端口 | 用途 |
| --- | --- | --- | --- |
| control-plane | 入站 | TCP 6443 | Kubernetes API Server |
| control-plane | 入站 | TCP 2379-2380 | etcd client/peer API |
| control-plane | 入站 | TCP 10250 | kubelet API |
| control-plane | 入站 | TCP 10257 | kube-controller-manager |
| control-plane | 入站 | TCP 10259 | kube-scheduler |
| worker | 入站 | TCP 10250 | kubelet API |
| worker | 入站 | TCP 10256 | kube-proxy |
| worker | 入站 | TCP/UDP 30000-32767 | NodePort Service 默认范围；不使用 NodePort 时无需向不受信网络全开 |

上表来自 [Kubernetes 官方 Ports and Protocols](https://kubernetes.io/docs/reference/networking/ports-and-protocols/)。端口是可配置的，如果改了默认值，防火墙必须开实际端口。

### 6.2 Calico 端口/协议

Calico 的开放项取决于封装和控制模式，不应无差别全开：

| 模式 | 节点间需求 |
| --- | --- |
| BGP | 双向 TCP 179 |
| IP-in-IP（若配置） | 双向 IP 协议号 4，它不是 TCP/UDP 端口 4 |
| VXLAN | 双向 UDP 4789 |
| Typha（若开启） | agent 访问 Typha TCP 5473 |
| Calico 访问 Kubernetes API | 通常 TCP 6443（取决于 API Server `--secure-port`） |

来源：[Calico Kubernetes 网络要求](https://docs.tigera.io/calico/latest/getting-started/kubernetes/requirements#network-requirements)。

**本地 VMware 方案推荐**：3 台 VM 都在同一受信 VMnet 时，节点间可先完全放行，仅对外限制访问。若必须保留 UFW，按最终 Installation 的实际模式放行：锁定的 v3.32.1 官方 custom resource 使用 `VXLANCrossSubnet`，应允许 UDP 4789；如 BGP 未禁用，同时允许 TCP 179。为了减少学习环境的变量，也可在 Installation 中显式改为 `encapsulation: VXLAN` 和 `bgp: Disabled`，让所有跨节点 Pod 流量走 VXLAN；这是工程选型，不是官方 manifest 默认值。[Calico Overlay networking](https://docs.tigera.io/calico/latest/networking/configuring/vxlan-ipip)、[Calico Operator Installation API](https://docs.tigera.io/calico/latest/reference/installation/api)

## 7. Calico 安装核对

1. Calico 3.32 测试 Kubernetes 1.34、1.35、1.36，与本方案 v1.36 匹配。[Calico 版本支持](https://docs.tigera.io/calico/latest/getting-started/kubernetes/requirements#kubernetes-requirements)
2. Calico 必须作为 CNI 安装，使用默认目录 `/etc/cni/net.d` 和 `/opt/cni/bin`，且通常不能和另一个网络提供者同时作为集群 CNI。[Calico CNI 要求](https://docs.tigera.io/calico/latest/getting-started/kubernetes/requirements#cni-plug-in-enabled)
3. Calico 官方对新集群推荐 Tigera Operator；v3.32.1 官方步骤是创建 CRD 与 operator，下载 `custom-resources.yaml` 后再创建。[官方 on-premises 安装](https://docs.tigera.io/calico/latest/getting-started/kubernetes/self-managed-onprem/onpremises)
4. v3.32.1 官方 `custom-resources.yaml` 的 `Installation/default` 实际字段是：`spec.calicoNetwork.ipPools[0].name=default-ipv4-ippool`、`blockSize=26`、`cidr=192.168.0.0/16`、`encapsulation=VXLANCrossSubnet`、`natOutgoing=Enabled`、`nodeSelector=all()`。这是对当前锁定 manifest 的直接核对，不应把旧版 manifest 的 IPIP 默认口径套用过来。[官方 v3.32.1 custom-resources.yaml](https://raw.githubusercontent.com/projectcalico/calico/v3.32.1/manifests/custom-resources.yaml)
5. Operator Installation API 允许的 `encapsulation` 枚举是 `IPIP | VXLAN | IPIPCrossSubnet | VXLANCrossSubnet | None`，`bgp` 是 `Enabled | Disabled`，`natOutgoing` 是 `Enabled | Disabled`；这些值区分大小写，脚本不得写成 raw IPPool API 的 `Always/CrossSubnet/true`。[Calico Operator Installation API](https://docs.tigera.io/calico/latest/reference/installation/api)
6. `192.168.0.0/16` 是 v3.32.1 官方 custom resource 的默认 Pod CIDR；本方案为降低与 VMware/家庭网段冲突，将其改为 `10.244.0.0/16`，并保证 `kubeadm init` 与 Calico IP pool 两处一致。[官方 Calico 安装说明](https://docs.tigera.io/calico/latest/getting-started/kubernetes/self-managed-onprem/onpremises)、[Kubernetes Pod 网络警告](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/#pod-network)
7. 只有 Calico 就绪后节点才会 `Ready`，验收不能在 `kubectl apply/create` 命令返回 0 时立即结束；应等待 `tigerastatus` 的组件 `AVAILABLE=True`、CoreDNS Running、三节点 Ready。[官方 Calico 部署监控](https://docs.tigera.io/calico/latest/getting-started/kubernetes/self-managed-onprem/onpremises)、[Kubernetes CNI 验证](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/#installing-a-pod-network-add-on)

## 8. “一键安装”的可靠实现边界

官方文档支持用 kubeadm 作为自动化的构建块，但它不替代 VM/OS 创建、SSH 信任、IP 分配和 CNI 选型。[创建 kubeadm 集群：kubeadm 用途](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/)

因此，最稳妥的“一键”交付应分两层：

1. **一次性人工前置**：安装 VMware，创建 3 台 Ubuntu VM，设定唯一 hostname 和固定 IP，确保三者互通/可上网，建立一个可 SSH 登录且可 `sudo` 的管理用户。
2. **Windows 一键编排**：PowerShell 入口通过 OpenSSH 向 3 节点分发/执行 Linux 脚本：
   1. 并行执行通用预检与安装（swap、sysctl、containerd、Kubernetes 包）；
   2. 仅在 `k8s-cp1` 执行 `kubeadm init`；
   3. 生成短期 join 命令并立即在两个 worker 执行；
   4. 仅在 control-plane 安装锁定版本的 Calico；
   5. 循环等待并校验节点、CoreDNS、Calico，再运行 Pod 跨节点/DNS/Service 冒烟测试。

一键脚本必须具备以下属性：

- **参数化**：节点 IP、SSH 用户、Kubernetes minor、Calico 版本、Pod CIDR 不得散落硬编码。
- **快速失败**：任一节点 SSH/网络/DNS/软件源检查失败就终止，不继续做半套集群。
- **幂等前置**：已安装的包、已存在的 sysctl/模块配置可重复执行；但 `kubeadm init/join` 不应无条件重复。
- **不泄露凭据**：join token 和 `admin.conf` 不写入 Git，日志中打码。
- **完整验收**：不以“命令执行成功”代替“集群可用”。

## 9. 建议的最终验收标准

| 检查 | 通过条件 |
| --- | --- |
| containerd | 3 节点 `systemctl is-active containerd` 为 active，CRI 可响应，cgroup driver 为 systemd |
| kubelet | 3 节点 kubelet 服务正常，无 swap/cgroup/CRI 致命错误 |
| 节点 | `kubectl get nodes -o wide` 显示 3 节点全部 `Ready`，版本一致 |
| 系统 Pod | CoreDNS、kube-proxy、Calico 组件全部 Running/Ready |
| Calico | `kubectl get tigerastatus` 的关键组件 `AVAILABLE=True`，无 Degraded |
| 跨节点网络 | 两个不同 worker 上的 Pod 可通讯 |
| DNS | Pod 内可解析 `kubernetes.default.svc.cluster.local` |
| Service | ClusterIP 可访问；如开启 NodePort，仅在预期端口范围可访问 |
| 重启 | 三台 VM 重启后 swap 仍禁用，sysctl/模块仍生效，集群恢复 Ready |

## 10. 对实施文档的直接建议

- 默认参数：Ubuntu Server 24.04 LTS amd64，1 control-plane + 2 worker，每节点 4 vCPU/8 GiB/60～80 GiB，Kubernetes v1.36，containerd，Calico v3.32.1，Pod CIDR `10.244.0.0/16`。
- 在文档开头增加“网段冲突检查”；若 Windows LAN/VPN/VMnet 使用 `10.244.0.0/16`，必须把 kubeadm 与 Calico 的 Pod CIDR 同步改为其他不重叠网段。
- 将脚本拆成 Linux 公共准备脚本、control-plane 初始化脚本、Windows PowerShell 编排入口；对用户仍保留一个入口命令。
- 下载 Calico 配置后再安装，锁定 `v3.32.1` tag，不使用 `master`/`latest` 浮动 URL。
- 安装文档必须声明：这是学习/开发用单控制平面集群，不满足生产高可用。

## 11. 官方资料索引

- VMware/Broadcom
  - [Workstation 系统要求](https://knowledge.broadcom.com/external/article?legacyId=90112)
  - [Workstation Pro 官方下载和授权](https://knowledge.broadcom.com/external/article/368667/download-and-license-vmware-desktop-hype.html)
  - [Workstation Pro 安装](https://knowledge.broadcom.com/external/article/387947/installing-vmware-workstation-pro.html)
  - [Workstation 宿主 OS 支持矩阵](https://knowledge.broadcom.com/external/article?legacyId=80807)
- Ubuntu
  - [Ubuntu Server 24.04 LTS 系统要求](https://ubuntu.com/server/docs/reference/installation/system-requirements/)
  - [Ubuntu Server 基础安装](https://ubuntu.com/server/docs/tutorial/basic-installation/)
- Kubernetes
  - [Installing kubeadm](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/)
  - [Creating a cluster with kubeadm](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/)
  - [Container Runtimes](https://kubernetes.io/docs/setup/production-environment/container-runtimes/)
  - [Configuring a cgroup driver](https://kubernetes.io/docs/tasks/administer-cluster/kubeadm/configure-cgroup-driver/)
  - [Ports and Protocols](https://kubernetes.io/docs/reference/networking/ports-and-protocols/)
  - [Creating Highly Available Clusters with kubeadm](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/high-availability/)
- containerd
  - [Getting started](https://github.com/containerd/containerd/blob/main/docs/getting-started.md)
  - [CRI Plugin Config Guide](https://github.com/containerd/containerd/blob/main/docs/cri/config.md)
  - [containerd-config.toml manual](https://github.com/containerd/containerd/blob/main/docs/man/containerd-config.toml.5.md)
- Calico/Tigera
  - [Kubernetes system requirements](https://docs.tigera.io/calico/latest/getting-started/kubernetes/requirements)
  - [On-premises installation](https://docs.tigera.io/calico/latest/getting-started/kubernetes/self-managed-onprem/onpremises)
  - [Bare-metal/kernel requirements](https://docs.tigera.io/calico/latest/getting-started/bare-metal/requirements)
  - [Overlay networking](https://docs.tigera.io/calico/latest/networking/configuring/vxlan-ipip)

## 继续上下文

当前结论：采用 Ubuntu 24.04 + Kubernetes v1.36 + containerd(systemd cgroup) + Calico v3.32.1，架构为 1 控制平面 + 2 worker。  
关键假设：Windows 宿主机至少 32 GiB（建议 48/64 GiB），3 台 VM 在同一 VMnet 且使用固定 IP，`10.244.0.0/16` 不与现有网络重叠。  
待决问题：实际 Windows/VMware 版本、物理内存/CPU、VMnet 网段、三个节点 IP 和 SSH 用户。  
下一步：基于本调研编写可照做的完整安装文档与 PowerShell/Linux 一键脚本。
