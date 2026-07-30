# Kubernetes 部署 Nginx：官方资料调研

> 调研日期：2026-07-20  
> 资料范围：仅使用 NGINX、NGINX 官方容器仓库、Docker Official Image 与 Kubernetes 官方文档。  
> 适用场景：VMware 上的三节点 `kubeadm` 集群，通过 Deployment 运行无状态 Nginx，使用 HTTP Basic Auth 控制访问。

## 1. 可落地结论

| 设计项 | 推荐值 | 官方依据与说明 |
| --- | --- | --- |
| 镜像 | `nginx:1.30.4-alpine` | NGINX 1.30.4 是截至调研日的最新 stable 安全更新；Docker Official Image 已将 `1.30.4` 列为 stable，并提供 `1.30.4-alpine`。部署固定完整版本，不用会漂移的 `latest`/`stable`；生产环境还可固定镜像 digest。[NGINX 1.30 变更记录](https://nginx.org/en/CHANGES-1.30) [Docker Official Image 标签事实源](https://github.com/docker-library/official-images/blob/master/library/nginx) |
| 工作负载 | `Deployment`，默认 2 副本 | Nginx 静态站点/反向代理是无状态工作负载，Deployment 支持副本管理和滚动更新。`RollingUpdate` 是默认策略，可用 `maxUnavailable`/`maxSurge` 约束更新期的可用性。[Deployment](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/#strategy) |
| 普通配置 | `nginx.conf` 放 ConfigMap | ConfigMap 用于非机密配置，可作为文件挂载到 Pod，与镜像解耦。[ConfigMap](https://kubernetes.io/docs/concepts/configuration/configmap/) |
| 账号密码 | `.htpasswd` 放 Secret | ConfigMap 不提供保密或加密，机密数据应使用 Secret。Secret 中的 base64 只是编码，不是加密；集群仍应启用 etcd 静态加密并收紧 RBAC。[ConfigMap 机密边界](https://kubernetes.io/docs/concepts/configuration/configmap/#configmaps-and-pods) [Secret](https://kubernetes.io/docs/concepts/configuration/secret/) |
| 对外服务 | 集群内优先 `ClusterIP:80 -> Pod:8080` | Service 为可变 Pod 提供稳定访问入口。VMware 实验环境如需从 Windows 访问，可另开 NodePort；生产应使用 Ingress/Gateway/LoadBalancer 并配置 TLS，不要把 NodePort 当作完整的入口安全方案。[Service](https://kubernetes.io/docs/concepts/services-networking/service/) |

## 2. HTTP Basic Auth 与 Secret

NGINX 的 `ngx_http_auth_basic_module` 使用 `auth_basic` 开启 Basic Auth，使用 `auth_basic_user_file` 读取“用户名:密码哈希”文件。官方支持 `crypt()`、Apache `apr1` 以及部分 RFC 2307 形式，并明确说明明文 `PLAIN` 和无盐 SHA-1 不应用于新密码。[NGINX Basic Auth 模块](https://nginx.org/en/docs/http/ngx_http_auth_basic_module.html)

推荐 Secret 仅保存完整 `.htpasswd` 文件：

```text
scm_admin:$6$<salt>$<sha512-crypt-hash>
```

实施要点：

- 一键脚本用隐藏输入读取用户名和密码，密码二次确认，不设置仓库内默认密码。
- 用固定目标镜像可验证的 `crypt()` 格式生成哈希；NGINX 官方文档指出可由 `htpasswd` 或 `openssl passwd` 生成。应让脚本在 Linux 控制节点通过标准输入交给 `openssl passwd -6 -stdin`，避免明文出现在命令行参数。
- 若目标镜像/平台的 `crypt()` 不支持 SHA-512 crypt，可回退为 NGINX 明确内置支持的 `apr1`，但必须增强密码强度并始终使用 TLS。脚本应在上线前同时执行 `nginx -t` 和真实 `curl -u` 验证，不只检查 Secret 已创建。
- Secret 挂载目录例如 `/etc/nginx/auth/.htpasswd`，配置中使用 `auth_basic_user_file /etc/nginx/auth/.htpasswd;`，Secret volume 设为只读和严格文件权限。
- 密码哈希仍属认证凭据，不应放到 ConfigMap、Deployment YAML、脚本日志或 Git 仓库。

## 3. ConfigMap 中的 Nginx 配置

为支持非 root、只读根文件系统和无认证健康检查，推荐 ConfigMap 中的核心配置为：

```nginx
pid /tmp/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    client_body_temp_path /tmp/client_temp;
    proxy_temp_path       /tmp/proxy_temp;
    fastcgi_temp_path     /tmp/fastcgi_temp;
    uwsgi_temp_path       /tmp/uwsgi_temp;
    scgi_temp_path        /tmp/scgi_temp;

    access_log /dev/stdout;
    error_log /dev/stderr notice;

    server {
        listen 8080;
        server_name _;

        auth_basic "SCM Nginx";
        auth_basic_user_file /etc/nginx/auth/.htpasswd;

        location = /healthz {
            auth_basic off;
            access_log off;
            default_type text/plain;
            return 200 "ok\n";
        }

        location / {
            root /usr/share/nginx/html;
            index index.html;
        }
    }
}
```

为什么这样配置：

- 官方 Docker NGINX 文档说明，以任意非 root UID/GID 运行时，需把 `pid` 和各类 `*_temp_path` 改到该用户可写的位置。只读模式下，默认的 `/var/cache/nginx` 和 `/var/run` 也需改为可写卷。[Docker Official NGINX 镜像说明](https://hub.docker.com/_/nginx)
- `listen 8080` 避免非 root 进程绑定特权端口 80，Service 仍可向用户提供 80 端口。NGINX 官方维护的 unprivileged 镜像也采用 8080、`/tmp/nginx.pid` 和 `/tmp/*_temp` 路径，可作为这一安全布局的一手佐证。[NGINX Unprivileged 官方仓库](https://github.com/nginx/docker-nginx-unprivileged)
- Basic Auth 在 `server` 层开启；`/healthz` 显式用 `auth_basic off` 取消继承，使 kubelet 无需持有业务密码就能做探针。NGINX 官方文档明确说明 `off` 会取消上层继承的认证。[Basic Auth 指令](https://nginx.org/en/docs/http/ngx_http_auth_basic_module.html)
- ConfigMap 应整个挂载为目录，不使用 `subPath`，否则 Pod 不会收到 ConfigMap 后续更新。[ConfigMap `subPath` 限制](https://kubernetes.io/docs/concepts/configuration/configmap/#mounted-configmaps-are-updated-automatically)

## 4. 非 root 与只读根文件系统

Docker Official NGINX Alpine 镜像中 `nginx` 用户/组 UID/GID 均为 101；官方文档也说明 Alpine 与 Debian 变体统一使用 101。因此固定 `nginx:1.30.4-alpine` 时可配置：[Docker Official NGINX：User and group id](https://hub.docker.com/_/nginx)

```yaml
spec:
  automountServiceAccountToken: false
  securityContext:
    runAsNonRoot: true
    runAsUser: 101
    runAsGroup: 101
    seccompProfile:
      type: RuntimeDefault
  containers:
    - name: nginx
      image: nginx:1.30.4-alpine
      command: ["nginx"]
      args: ["-g", "daemon off;"]
      securityContext:
        allowPrivilegeEscalation: false
        readOnlyRootFilesystem: true
        capabilities:
          drop: ["ALL"]
      volumeMounts:
        - name: runtime
          mountPath: /tmp
  volumes:
    - name: runtime
      emptyDir:
        sizeLimit: 64Mi
```

关键边界：

- Kubernetes 官方定义 `readOnlyRootFilesystem` 为把容器根文件系统挂成只读，`allowPrivilegeEscalation: false` 阻止进程获得比父进程更多的权限。[Kubernetes securityContext](https://kubernetes.io/docs/tasks/configure-pod-container/security-context/)
- `/tmp` 必须单独挂载可写 `emptyDir`，同时设置 `sizeLimit`；否则 Nginx 无法创建 PID 和请求/代理临时文件。若后续启用缓存、上传或大响应缓冲，需按压测结果调整容量。
- `command: ["nginx"]` 绕过官方镜像的 root-oriented 入口初始化脚本，直接以 UID 101 启动已由 ConfigMap 完整定义的配置。因此 ConfigMap 必须是完整可启动配置，并在部署前执行 `nginx -t`。
- 每次升级镜像都应重新核对 UID/GID 和可写路径，不对无版本标签永久假定。

## 5. 探针与资源限制

三种探针都访问不需认证的 `/healthz`：

```yaml
startupProbe:
  httpGet:
    path: /healthz
    port: http
  periodSeconds: 2
  failureThreshold: 30
readinessProbe:
  httpGet:
    path: /healthz
    port: http
  periodSeconds: 5
  timeoutSeconds: 2
  failureThreshold: 3
livenessProbe:
  httpGet:
    path: /healthz
    port: http
  periodSeconds: 10
  timeoutSeconds: 2
  failureThreshold: 3
```

- Startup probe 成功前，Kubernetes 不会执行 liveness/readiness；readiness 失败的 Pod 不会接收 Service 流量，liveness 持续失败才导致重启。[Kubernetes 探针](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)
- `/healthz` 只验证 Nginx worker 能处理 HTTP，不验证 Basic Auth 凭据是否正确。一键部署验收必须另外做两次真实访问：无凭据返回 401，正确账号密码返回 200。

三台 8 GiB 虚拟机的实验默认值：

```yaml
resources:
  requests:
    cpu: 50m
    memory: 64Mi
    ephemeral-storage: 32Mi
  limits:
    cpu: 500m
    memory: 256Mi
    ephemeral-storage: 256Mi
```

Requests 用于调度，limits 由 kubelet/容器运行时通过 cgroup 执行；CPU 超限通常被节流，内存超限可能触发 OOM kill。上述只是小型静态页默认值，代理、大文件、缓存、TLS 或高并发场景要根据指标与压测调整。[Kubernetes 资源管理](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/)

## 6. ConfigMap/Secret 更新和热更新边界

| 变化 | Kubernetes 行为 | Nginx 是否自动采用 | 推荐操作 |
| --- | --- | --- | --- |
| ConfigMap 目录挂载更新 | kubelet 最终一致地投影新文件，存在同步周期和缓存传播延迟 | 否，Nginx 不会因文件替换自动 reload | 先在临时 Pod 对新 ConfigMap 执行 `nginx -t`，再 `kubectl rollout restart deployment/nginx -n scm-infra` |
| ConfigMap `subPath` 挂载 | 不接收后续 ConfigMap 更新 | 否 | 不使用 `subPath`，按目录挂载 |
| Secret volume 中 `.htpasswd` 更新 | 最终一致地投影新文件 | Nginx 后续请求通常会重新打开认证文件，但不应依赖未受控的投影时延 | 统一执行滚动重启，验证新密码成功、旧密码失效 |
| Pod 环境变量引用 ConfigMap/Secret | 不自动更新 | 否 | 必须重启 Pod |

Kubernetes 官方说明，ConfigMap volume 的投影会最终更新，但环境变量不会自动更新，`subPath` 也不会收到更新。“投影文件已变”不等于“Nginx 已重载配置”。对这类小型三节点集群，一键脚本统一滚动重启更简单可验证。[ConfigMap 更新行为](https://kubernetes.io/docs/concepts/configuration/configmap/#mounted-configmaps-are-updated-automatically) [kubectl rollout restart](https://kubernetes.io/docs/reference/kubectl/generated/kubectl_rollout/kubectl_rollout_restart/)

`nginx -s reload` 可以在单 Pod 内平滑重载，但对多副本 Deployment 仍需解决每个 Pod 的执行、失败回退、新旧 Secret 轮换与审计。本方案推荐 Deployment 滚动替换，而不在业务容器内嵌入常驻 reload sidecar。

## 7. Deployment 和 Service 默认建议

```yaml
spec:
  replicas: 2
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 0
      maxSurge: 1
  minReadySeconds: 5
  progressDeadlineSeconds: 600
```

- 2 副本 + `maxUnavailable: 0` 适合可同时容纳 3 个 Nginx Pod 的资源宽松环境；如集群资源紧张，可改为 `maxUnavailable: 1`，但更新时可能只剩一副本。
- 建议加 `topologySpreadConstraints` 或 Pod anti-affinity，尽量将两副本分布到两个 worker；小集群用 `ScheduleAnyway` 避免单节点不可用时完全无法调度。
- Service 在集群内使用 `port: 80` 和 `targetPort: 8080`。如实验环境另建 NodePort，需明确记录端口、防火墙范围和下线方式。
- 当 Pod readiness 失败时，Kubernetes 不会通过 Service 向其发送流量，因此滚动更新验收必须等待 Deployment Available，不能只看 `kubectl apply` 成功。[Kubernetes readiness](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/#define-readiness-probes)

## 8. 一键脚本应执行的验收

```bash
kubectl -n scm-infra get deployment,pod,service -l app.kubernetes.io/name=nginx -o wide
kubectl -n scm-infra rollout status deployment/nginx --timeout=5m
kubectl -n scm-infra exec deploy/nginx -- nginx -t
kubectl -n scm-infra exec deploy/nginx -- id
```

还必须从实际 Service/NodePort 入口做 HTTP 验收：

1. `GET /healthz` 无凭据返回 200。
2. `GET /` 无凭据返回 401，且带 `WWW-Authenticate` 响应头。
3. `GET /` 使用正确账号密码返回 200。
4. 密码轮换后，新密码返回 200，旧密码返回 401。
5. Pod 的 UID/GID 为 101，根文件系统不可写，仅 `/tmp` 临时卷可写。
6. 两个副本均 Ready，并尽量位于不同 worker 节点。

## 9. 主要风险

1. **Basic Auth 不等于 TLS**：账号密码认证只限制访问，不加密 HTTP 传输。只要越过不可信网络边界，就应在 Ingress/Gateway/Nginx 入口启用 HTTPS。
2. **Secret 不是默认加密保险箱**：base64 不提供保密，必须配合 etcd 静态加密、最小 RBAC 和审计。
3. **ConfigMap 更新不等于 Nginx 已生效**：先 `nginx -t`，再滚动重启，最后从实际入口做认证验收。
4. **探针不验证密码**：`/healthz` 特意关闭认证，避免向 kubelet 散发凭据；业务路径 401/200 必须由部署脚本另行验收。
5. **静态文件非持久卷**：官方镜像内容适合示例页。若用户要持续维护站点内容，应构建自有镜像或引入明确的存储/发布流程，不在运行中 Pod 内手改文件。

## 10. 最终实施建议

本次一键方案建议采用：

- `Namespace/scm-infra`
- `ConfigMap/nginx-config`：完整 `nginx.conf`
- `Secret/nginx-basic-auth`：仅 `.htpasswd`
- `Deployment/nginx`：2 副本，`nginx:1.30.4-alpine`，UID/GID 101，只读根文件系统
- `Service/nginx`：ClusterIP 80 -> 8080；VMware 实验访问可选独立 NodePort
- Windows PowerShell 一键脚本：隐藏输入凭据、生成 `crypt()` 哈希、创建/更新 Secret、应用 ConfigMap/Deployment/Service、滚动重启、验证 401/200 和旧密码失效

这个结构同时满足“配置用 ConfigMap”和“账号密码不进 ConfigMap”两个边界，也避免在 Nginx 这类无状态服务上引入不必要的 PV/StatefulSet。
