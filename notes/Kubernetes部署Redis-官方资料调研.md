# Kubernetes 部署 Redis：官方资料调研

> 调研日期：2026-07-20  
> 资料范围：仅使用 Redis、Redis 官方源码仓库、Docker Official Image 与 Kubernetes 官方文档。  
> 目标拓扑：裸机/VMware `kubeadm` 三节点集群，`scm-infra` 命名空间，单实例 Redis，数据固定在 `k8s-worker1`；应用用户 `scm_app` 只能访问 `scm:*` 键与频道。

## 1. 可落地结论

| 设计项 | 推荐值 | 官方依据与说明 |
| --- | --- | --- |
| Redis 镜像 | `redis:8.8.0-trixie` | Redis 8.8.0 是截至调研日最新 GA；Docker Official Image 已发布 `8.8.0`、`8.8.0-trixie` 等固定标签。部署必须锁定完整版本，不能使用会漂移的 `latest`；更严格环境应再锁定目标架构 digest。[Redis 8.8.0 Release](https://github.com/redis/redis/releases/tag/8.8.0) [Docker Official Image 标签](https://hub.docker.com/_/redis) |
| 工作负载 | `StatefulSet`，`replicas: 1` | StatefulSet 为 Pod 保留稳定身份并适合持久化工作负载；但单副本本身不提供 Redis 高可用。[StatefulSet](https://kubernetes.io/docs/concepts/workloads/controllers/statefulset/) |
| 存储 | 静态 `local` PV + PVC，`ReadWriteOnce`，`Retain`，固定 `k8s-worker1` | `local` PV 显式携带 `nodeAffinity`，调度器因此把 Pod 放到正确节点；比直接使用 Pod `hostPath` 更能表达存储与节点约束。`Retain` 表示 PVC 删除后需人工回收，避免自动清除数据。[Local volume](https://kubernetes.io/docs/concepts/storage/volumes/#local) [PV 回收策略](https://kubernetes.io/docs/concepts/storage/persistent-volumes/#reclaim-policy) |
| Redis 配置 | `redis.conf` 放 ConfigMap，按目录只读挂载 | Redis 官方建议正式运行使用配置文件；官方容器支持把自定义配置挂载到 `/usr/local/etc/redis`，以 `redis-server /usr/local/etc/redis/redis.conf` 启动。[Redis 配置](https://redis.io/docs/latest/operate/oss_and_stack/management/config/) [官方容器用法](https://hub.docker.com/_/redis) |
| 密码 | Kubernetes Secret 同时保存客户端密码与哈希化 `users.acl` | 密码不能放 ConfigMap。Kubernetes Secret 可作为文件挂载，但默认在 etcd 中并非自动加密，仍需启用静态加密和最小 RBAC。[Secret](https://kubernetes.io/docs/concepts/configuration/secret/) |
| 网络 | 仅 `ClusterIP:6379`，不建 NodePort/LoadBalancer | ClusterIP 仅从集群内部可达；但默认 Kubernetes Pod 网络通常允许 Pod 间互通，因此生产还应加 NetworkPolicy，只允许明确应用命名空间/Pod。[Service](https://kubernetes.io/docs/concepts/services-networking/service/) [Kubernetes 网络模型](https://kubernetes.io/docs/concepts/services-networking/) |

## 2. 推荐拓扑与边界

```text
应用 Pod ── redis.scm-infra.svc:6379 ── ClusterIP ── redis-0
                                                        │
                                                PVC redis-data
                                                        │
                         local PV /var/lib/redis-scm (k8s-worker1)
```

该方案适合开发、集成测试和可接受短时中断的小型环境。它不是高可用 Redis：`k8s-worker1` 宕机、VM 丢失或本地磁盘损坏时，Pod 无法自动带着数据迁到另一节点。Kubernetes 官方明确指出，底层节点不可用时 `local` 卷不可访问，使用它的 Pod 无法运行，并可能产生数据丢失。[官方限制](https://kubernetes.io/docs/concepts/storage/volumes/#local)

## 3. ConfigMap 中的 `redis.conf`

推荐基础配置：

```conf
bind 0.0.0.0
port 6379
protected-mode yes
daemonize no
supervised no
dir /data

aclfile /etc/redis-auth/users.acl

appendonly yes
appendfsync everysec
aof-use-rdb-preamble yes

save 900 1
save 300 10
save 60 10000

maxmemory 1536mb
maxmemory-policy noeviction
loglevel notice
```

设计说明：

- AOF 会记录每次写操作并可在启动时重放；`appendfsync everysec` 是常见的耐久性/性能折中，但极端故障仍可能损失约 1 秒写入。RDB 与 AOF 可以组合使用。[Redis 持久化](https://redis.io/docs/latest/operate/oss_and_stack/management/persistence/)
- `maxmemory` 必须低于容器 memory limit，给 Redis 进程、fork/Copy-on-Write、AOF rewrite 和系统开销留出空间；否则可能被 kubelet/OOM killer 杀死。具体值应以业务数据量和压测为准。[Redis 管理说明](https://redis.io/docs/latest/operate/oss_and_stack/management/admin/)
- `noeviction` 适合不允许静默丢键的业务数据：达到上限后写命令失败，由应用告警和降级。若 Redis 只作为可丢缓存，可改成与业务一致的淘汰策略；这属于业务决策，不应由脚本擅自决定。[Redis 配置说明](https://redis.io/docs/latest/operate/oss_and_stack/management/config/#configuring-redis-as-a-cache)
- 不应把密码写入 `requirepass`，因为本方案使用 Redis 6+ ACL 多用户认证。`requirepass` 属于兼容旧客户端的传统认证方式。[Redis 安全](https://redis.io/docs/latest/operate/oss_and_stack/management/security/)

## 4. Secret 与 Redis ACL

### 4.1 推荐 ACL 文件

Redis 的 ACL 配置行格式为 `user <username> ...`；`~pattern` 限制键，`&pattern` 限制 Pub/Sub 频道，`>password` 增加明文输入的密码（Redis 内部保存为 SHA-256）。Redis 7.0 起新用户默认 `resetchannels`。[Redis ACL](https://redis.io/docs/latest/operate/oss_and_stack/management/security/acl/)

```text
user default off
user scm_app reset on #${REDIS_PASSWORD_SHA256} ~scm:* &scm:* +@read +@write +@connection +@transaction +@pubsub +@scripting -@admin -@dangerous +info
```

ACL 文件中每个用户必须占完整一行，不能为了 YAML/文档排版插入反斜杠换行。实施时也不能期待 ConfigMap 自动替换变量。推荐一键脚本在控制端计算随机密码的 SHA-256，把明文 `password` 与只含 `#<64位SHA-256>` 的完整 `users.acl` 都写入 `Secret/redis-auth`；Pod 将 `users.acl` 只读挂载到 `/etc/redis-auth/users.acl`，主容器通过 `aclfile` 读取。这样无须 initContainer，且明文不会进入 ConfigMap、StatefulSet YAML、ACL 文件或容器启动参数。Redis 官方说明 `#<hash>` 可直接加入 SHA-256 密码摘要；明文密码仍只供客户端探针和应用认证使用。[ACL 密码规则](https://redis.io/docs/latest/operate/oss_and_stack/management/security/acl/#acl-rules)

权限取舍：

- `default off` 阻止匿名/仅密码式默认用户访问；客户端必须使用 Redis URI `redis://scm_app:<password>@redis.scm-infra.svc.cluster.local:6379/0` 或等价的用户名密码参数。
- `reset` 先清理同名用户的既有规则，避免 ACL 规则是增量累加这一特性留下旧权限。[ACL SETUSER](https://redis.io/docs/latest/commands/acl-setuser/)
- `~scm:*` 与 `&scm:*` 分别限制键和频道；从 Redis 6.2 起 ACL 支持频道访问控制。[Redis ACL](https://redis.io/docs/latest/operate/oss_and_stack/management/security/acl/)
- 最后的 `+info` 只回加运维验收所需的 `INFO` 命令；规则从左到右应用，因此必须放在移除管理/危险类别之后。若应用不应查看实例指标，可创建单独运维用户并从 `scm_app` 移除它。
- 上述命令类别是通用业务默认值，不保证适配所有客户端。上线前要用实际客户端执行 `ACL DRYRUN` 或集成测试核对；若不使用 Lua，应移除 `+@scripting`。不能简单使用 `+@all`，因为它会把模块命令也全部放开，Redis 官方特别提示此风险。[Redis ACL 命令类别](https://redis.io/docs/latest/operate/oss_and_stack/management/security/acl/#command-categories)

### 4.2 Secret 的安全边界

- 一键脚本应以隐藏输入读取密码，或用密码学安全随机源生成至少 24 字符密码；禁止把默认密码提交到仓库。
- `kubectl create secret generic ... --from-literal` 会把密码出现在本机进程参数/历史中，不适合作为默认实现；更稳妥的是脚本把内容通过标准输入交给 `kubectl apply -f -`，并关闭命令回显。
- Secret volume 应设置严格 `defaultMode`，Pod 使用专属 ServiceAccount 且 `automountServiceAccountToken: false`。
- Kubernetes 官方明确警告 Secret 默认以未加密形式存于 etcd；应启用 [静态数据加密](https://kubernetes.io/docs/tasks/administer-cluster/encrypt-data/) 并用 RBAC 限制 `get/list/watch secrets`。

## 5. 静态 local PV、PVC 与节点准备

在 `k8s-worker1` 预先创建 `/var/lib/redis-scm`，由容器 Redis 用户可写。Docker Official Image 默认会切换到 `redis` 用户运行，并说明默认场景会修正数据/配置权限；显式设置 Kubernetes `runAsUser` 时镜像会跳过入口脚本的降权逻辑，因此需要由部署脚本先核验镜像 UID/GID 并正确设置宿主机目录权限。[Docker Official Image：Process User and Privileges](https://hub.docker.com/_/redis)

推荐资源关系：

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: redis-scm-pv
spec:
  capacity:
    storage: 20Gi
  volumeMode: Filesystem
  accessModes: [ReadWriteOnce]
  persistentVolumeReclaimPolicy: Retain
  storageClassName: redis-local
  local:
    path: /var/lib/redis-scm
  nodeAffinity:
    required:
      nodeSelectorTerms:
        - matchExpressions:
            - key: kubernetes.io/hostname
              operator: In
              values: [k8s-worker1]
```

关键点：

- `local` 卷只支持静态创建，必须设置 PV `nodeAffinity`；官方建议对应 StorageClass 使用 `volumeBindingMode: WaitForFirstConsumer`，让 PVC 绑定与 Pod 的其他调度条件一起计算。[Local volume](https://kubernetes.io/docs/concepts/storage/volumes/#local)
- `Retain` 只保留存储资产，不等于备份。PVC 删除后 PV 会进入 `Released`，重新绑定前要由管理员确认旧数据、处理 `claimRef`，不能让脚本自动清空目录。[PV 生命周期与 Retain](https://kubernetes.io/docs/concepts/storage/persistent-volumes/)
- 本地卷没有 Kubernetes 自动扩容能力，也不会把数据复制到其他节点；容量增长和迁移必须有独立运维步骤。
- 不推荐直接在 StatefulSet 中声明 `hostPath`：它把节点路径耦合藏在 Pod 模板里，且不提供 PV/PVC 生命周期、容量声明和显式回收策略。若只是单机临时试验可以使用，但正式脚本应使用静态 `local` PV。

## 6. StatefulSet 与安全上下文

推荐单副本 StatefulSet 的关键安全项：

```yaml
spec:
  serviceName: redis-headless
  replicas: 1
  template:
    spec:
      serviceAccountName: redis
      automountServiceAccountToken: false
      securityContext:
        runAsNonRoot: true
        runAsUser: 999
        runAsGroup: 999
        fsGroup: 999
        fsGroupChangePolicy: OnRootMismatch
        seccompProfile:
          type: RuntimeDefault
      containers:
        - name: redis
          image: redis:8.8.0-trixie
          securityContext:
            allowPrivilegeEscalation: false
            capabilities:
              drop: [ALL]
          resources:
            requests:
              cpu: 100m
              memory: 512Mi
            limits:
              cpu: "1"
              memory: 2Gi
```

- `runAsNonRoot`、禁用提权、删除 capabilities、`RuntimeDefault` seccomp 是标准的 Pod/容器最小权限措施。[Kubernetes securityContext](https://kubernetes.io/docs/tasks/configure-pod-container/security-context/)
- `fsGroupChangePolicy: OnRootMismatch` 可避免每次启动都递归调整大数据卷权限；该字段只对支持 `fsGroup` 的卷类型生效。[官方说明](https://kubernetes.io/docs/tasks/configure-pod-container/security-context/#configure-volume-permission-and-ownership-change-policy-for-pods)
- UID/GID `999` 是当前固定版本官方 Redis Debian 镜像的 `redis` 身份；显式设置它可让 `runAsNonRoot` 通过，并意味着入口脚本不再先以 root 修正权限。因此部署脚本必须提前把宿主机数据目录设为 `999:999`，且每次升级镜像都重新核验 UID/GID，不能对无版本镜像永久假定。
- `readOnlyRootFilesystem: true` 只有在确认 Redis 及入口脚本的全部写路径已单独挂载后再启用；否则可能破坏官方入口脚本的权限修复或临时文件写入。不要为了表面“加固”直接启用而不做启动测试。

## 7. 健康探针

探针必须完成带 ACL 的 `PING`，不能只做 TCP 检测；TCP 连接成功不能证明 Redis 已完成数据加载并可认证服务。推荐 `exec`：

```yaml
startupProbe:
  exec:
    command: ["/bin/sh", "-ec", "REDISCLI_AUTH=$(cat /run/redis-secret/password) redis-cli --user scm_app ping | grep -qx PONG"]
  periodSeconds: 5
  failureThreshold: 60
readinessProbe:
  exec:
    command: ["/bin/sh", "-ec", "REDISCLI_AUTH=$(cat /run/redis-secret/password) redis-cli --user scm_app ping | grep -qx PONG"]
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 3
livenessProbe:
  exec:
    command: ["/bin/sh", "-ec", "REDISCLI_AUTH=$(cat /run/redis-secret/password) redis-cli --user scm_app ping | grep -qx PONG"]
  periodSeconds: 10
  timeoutSeconds: 3
  failureThreshold: 6
```

Kubernetes 的 startup probe 成功前不会执行 liveness/readiness，适合 AOF 恢复时间不确定的 Redis；readiness 失败会把 Pod 从 Service 后端摘除，liveness 连续失败则重启容器。阈值必须按最大 AOF 恢复时间与磁盘性能调整，配置过紧会形成重启循环。[探针官方文档](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)

## 8. Service 与网络安全

业务 Service 使用 `type: ClusterIP`，集群内地址为：

```text
redis.scm-infra.svc.cluster.local:6379
```

StatefulSet 的 `serviceName` 还应对应一个 headless Service（`clusterIP: None`）以提供稳定网络身份；StatefulSet 官方文档说明该 Service 需由部署者创建。[StatefulSet 限制](https://kubernetes.io/docs/concepts/workloads/controllers/statefulset/#limitations)

ClusterIP 只解决“不向集群外直接暴露”，不等于租户隔离。默认 Kubernetes 网络模型允许 Pod 互通；若当前 Calico 已启用 NetworkPolicy，应在 `scm-infra` 对 Redis 设置默认拒绝入站，只允许带指定标签的 SCM 应用 Pod 访问 TCP 6379，并按 DNS/运维需要配置出站规则。[Kubernetes NetworkPolicy](https://kubernetes.io/docs/concepts/services-networking/network-policies/)

## 9. ConfigMap/Secret 更新和重启边界

| 变化 | Kubernetes 投影行为 | Redis 是否自动采用 | 推荐操作 |
| --- | --- | --- | --- |
| ConfigMap 目录挂载更新 | 最终一致地更新；可能有 kubelet 同步与缓存延迟 | 否。Redis 不会自动重读整个 `redis.conf` | `kubectl rollout restart statefulset/redis -n scm-infra`，等待 Ready 后验收 |
| ConfigMap 使用 `subPath` | **不会**收到 ConfigMap 更新 | 否 | 不使用 `subPath`，按目录挂载；仍需滚动重启 |
| Secret volume 更新 | 最终一致地更新 `password` 与 `users.acl` 文件 | Redis 不会自动执行 `ACL LOAD` | 更新 Secret 后滚动重启，确认新密码成功、旧密码失败 |
| `CONFIG SET` | 直接改运行时内存配置 | 仅支持部分参数，重启后丢失 | 紧急变更后同步修改 ConfigMap；不要依赖 `CONFIG REWRITE` 写只读 ConfigMap |
| 直接修改外部 ACL 文件 | 文件内容改变 | 需显式执行 `ACL LOAD` | 本简化方案统一用 Secret 更新 + 重启；若要无重启轮换，需专门设计双密码过渡和受控 `ACL LOAD` |

官方依据：ConfigMap 以 volume 挂载时会最终更新，但环境变量不会自动更新，`subPath` 挂载也不会更新。[ConfigMap 更新](https://kubernetes.io/docs/concepts/configuration/configmap/#mounted-configmaps-are-updated-automatically) Redis 支持部分配置用 `CONFIG SET` 在线改变，但不会同步修改配置文件；外部 ACL 文件可用 `ACL LOAD` 加载，失败时保留旧配置。[Redis 配置](https://redis.io/docs/latest/operate/oss_and_stack/management/config/#changing-redis-configuration-while-the-server-is-running) [Redis ACL 文件](https://redis.io/docs/latest/operate/oss_and_stack/management/security/acl/#use-an-external-acl-file)

一键脚本应在 `kubectl apply` 后执行受控滚动重启并验收，避免误以为 ConfigMap 投影更新等于 Redis 配置已生效。配置校验至少包括：Pod Ready、PV/PVC Bound、调度节点为 `k8s-worker1`、认证 PING、`scm:*` 可读写、非 `scm:*` 返回 NOPERM、旧密码失效、AOF 已开启且数据目录确有文件。

## 10. 关键风险清单

1. **单点和本地盘风险（最高）**：单实例 + local PV 无自动故障转移；worker1 故障即不可用，Retain 也不能防磁盘/VM 丢失。必须另做定期备份与恢复演练。
2. **密码不等于加密**：Redis ACL 解决认证授权，不提供链路加密；跨不可信网络要增加 TLS 方案。ClusterIP 不能替代 NetworkPolicy。
3. **Secret 仍可能泄露**：默认 etcd 不加密，具有命名空间建 Pod 或读取 Secret 权限的主体可能取得密码；需静态加密、最小 RBAC 与审计。
4. **配置不会自动生效**：ConfigMap 文件更新不代表 Redis 已重载；`subPath` 甚至不接收更新。必须滚动重启并验收。
5. **AOF 不是备份**：误删除、`FLUSH*` 或逻辑错误会被忠实持久化；Retain/AOF 都不能替代离线备份。
6. **资源与 fork 峰值**：AOF rewrite/RDB fork 可能显著增加内存压力；2 GiB limit/1.5 GiB maxmemory 只是 8 GiB 共机实验默认值，生产要压测调优。
7. **版本升级风险**：应锁完整镜像版本，升级时重新核验 UID/GID、ACL 命令类别、AOF兼容性，并先备份和做恢复测试。

## 11. 推荐脚本验收标准

```bash
kubectl -n scm-infra get statefulset,pod,svc,pvc
kubectl get pv redis-scm-pv
kubectl -n scm-infra get pod redis-0 -o wide
kubectl -n scm-infra exec redis-0 -- redis-cli --user scm_app --askpass PING
kubectl -n scm-infra exec redis-0 -- redis-cli --user scm_app --askpass SET scm:install:test ok
kubectl -n scm-infra exec redis-0 -- redis-cli --user scm_app --askpass GET scm:install:test
kubectl -n scm-infra exec redis-0 -- redis-cli --user scm_app --askpass SET forbidden:test denied
kubectl -n scm-infra exec redis-0 -- redis-cli --user scm_app --askpass INFO persistence
```

最后一条越权写入必须返回 `NOPERM` 才算通过。交付脚本不应打印 Secret 明文；连接密码应通过用户隐藏输入或受限临时文件提供。
