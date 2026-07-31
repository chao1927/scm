# 部署资产导航

完整部署说明统一见 [`docs/10-供应链系统完整部署手册.md`](../docs/10-供应链系统完整部署手册.md)。

## 当前入口

| 目录/脚本 | 状态 | 用途 |
| --- | --- | --- |
| `kubernetes-vmware/` | 实验可用 | Windows + VMware 三节点 kubeadm 集群 |
| `middleware-native/` | 实验可用 | Ubuntu 原生 MySQL 与 RocketMQ |
| `redis-k8s/` | 实验可用 | Kubernetes 单节点 Redis |
| `nginx-k8s/` | 实验可用 | Kubernetes Nginx 基础入口 |
| `k3s/` | 实验适配 | Linux 单节点 K3s |
| `one-click.sh`、`linux/` | 暂不可用 | 依赖缺失文件，禁止作为当前部署入口 |

本地开发中间件不在本目录，使用：

```bash
cd middleware-stack
./bin/dev.sh up
./bin/nacos-config.sh
```

九服务本地启动使用：

```bash
cd project/backend
./bin/dev.sh up
```
