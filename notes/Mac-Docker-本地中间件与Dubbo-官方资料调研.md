# Mac Docker 本地中间件与 Dubbo：官方资料调研

> 调研日期：2026-07-27  
> 资料范围：仅使用 Docker、MySQL、Redis、Apache RocketMQ、Nacos、NGINX、Apache Dubbo 官方文档、官方发布页和官方镜像事实源。  
> 目标：在 Intel Mac 或 Apple Silicon Mac 的 Docker Desktop 上运行本地开发中间件，并确保 Dubbo 3 通过 Nacos 完成真实的服务注册、发现和 RPC 调用。

## 1. 推荐版本矩阵

| 组件 | 固定版本 | Apple Silicon | 推荐结论 |
| --- | --- | --- | --- |
| MySQL | `mysql:8.4.10` | 官方 `arm64v8` | 8.4 LTS 分支，适合本地业务库与 Nacos 外置数据库共用一个实例、分库分账号。Docker Official Images 当前事实源同时列出 amd64 和 arm64v8。[官方镜像事实源](https://github.com/docker-library/official-images/blob/master/library/mysql) |
| Redis | `redis:8.8.1-alpine` | 官方 `arm64v8` | 当前 Redis 8.8 GA 补丁版，固定完整标签，避免 `latest` 漂移。Docker Official Images 明确列出 arm64v8。[官方镜像事实源](https://github.com/docker-library/official-images/blob/master/library/redis) |
| RocketMQ | `apache/rocketmq:5.5.0` | 官方 `linux/arm64` | 当前 RocketMQ GA；官方 Docker Hub 标签同时提供 amd64 与 arm64。[RocketMQ 5.5.0 发布页](https://github.com/apache/rocketmq/releases/tag/rocketmq-all-5.5.0) [官方镜像标签](https://hub.docker.com/r/apache/rocketmq/tags) |
| Nacos | `nacos/nacos-server:v3.2.3-slim` | 官方 `linux/arm64` | Nacos 3.2.3 是截至调研日最新 GA；官方 Docker 项目明确建议 Apple Silicon 使用 `-slim`，Docker Hub 提供 arm64 manifest。[发布历史](https://nacos.io/en/download/release-history/) [官方镜像标签](https://hub.docker.com/r/nacos/nacos-server/tags) [Nacos Docker](https://github.com/nacos-group/nacos-docker) |
| Nginx | `nginx:1.30.4-alpine` | 官方 `arm64v8` | 1.30.4 是当前 stable 安全补丁版；不要改用 mainline 或漂移标签。[NGINX 1.30 变更](https://nginx.org/en/CHANGES-1.30) [官方镜像事实源](https://github.com/docker-library/official-images/blob/master/library/nginx) |
| Dubbo Java | `3.3.6` | JVM 层与 CPU 架构无关 | 当前 Dubbo 3 最新功能版本，支持 JDK 8–21；推荐 Spring Boot 3 项目使用同版本官方 starter/BOM。[Dubbo 下载页](https://dubbo.apache.org/en/download/) [Dubbo 3.3.6 发布页](https://github.com/apache/dubbo/releases/tag/dubbo-3.3.6) |

### 1.1 为什么不使用 `latest`

本地开发环境也需要可复现。`latest`、`stable`、`8.4` 等别名会随上游更新而移动，同一份 Compose 文件在不同日期可能拉到不同二进制。建议：

- Compose 固定完整版本，如 `mysql:8.4.10`。
- 团队需要完全一致时，再固定多架构 image index digest。
- 升级采用显式改版本、备份、重新拉取、健康检查和集成测试，不让 Docker 自动升级业务依赖。

## 2. Apple Silicon 与 Docker Desktop 网络

上述五个服务均已有原生 arm64 镜像，M1/M2/M3/M4 Mac 不需要设置 `platform: linux/amd64`，也不需要 Rosetta/QEMU 模拟。强制 amd64 会增加内存与 CPU 开销，并掩盖真正的多架构兼容问题。

Compose 默认创建项目级 bridge 网络。服务会注册到内部 DNS，容器之间应使用服务名连接，例如：

```text
mysql:3306
redis:6379
nacos:8848
rmqnamesrv:9876
rmqbroker:10911
nginx:8080
```

不要在容器内用 `localhost` 访问另一个容器；`localhost` 只指当前容器。Compose 官方说明同一默认网络中的服务可直接按 service name 发现，容器重建后 IP 会变化但名称保持不变。[Compose 网络](https://docs.docker.com/compose/how-tos/networking/)

Mac 宿主机上的 IDE/Java 进程访问容器时使用发布到宿主机的端口，例如 `127.0.0.1:8848`。反过来，容器访问 Mac 宿主机进程时使用 `host.docker.internal`；Docker Desktop 官方明确说明该名称解析到宿主机内部地址。[Docker Desktop 网络](https://docs.docker.com/desktop/features/networking/networking-how-tos/)

## 3. 推荐 Compose 拓扑

```text
Mac/IDE 中的 Dubbo provider、consumer
       │ Nacos 注册发现：localhost:8848
       │ 真实 RPC：provider 发布的 tri://可达主机:50051
       ▼
Docker Desktop
├── mysql        MySQL 8.4.10
├── redis        Redis 8.8.1
├── nacos        Nacos 3.2.3 standalone + MySQL
├── rmqnamesrv   RocketMQ NameServer
├── rmqbroker    RocketMQ Broker + Proxy（本地模式）
└── nginx        Nginx 反向代理/静态入口
```

本地开发推荐 RocketMQ 的逻辑三组件结构，但物理上使用两个容器：

- `rmqnamesrv`：NameServer。
- `rmqbroker`：执行 `mqbroker --enable-proxy`，同一进程内运行 Broker 与 Proxy。

RocketMQ 官方把这种方式称为 Local mode，并明确说在没有特殊要求、本地测试或从旧版本平滑升级时可使用；独立 Proxy 是 Cluster mode，适合 Proxy 与 Broker 分别伸缩的集群，不值得在单机开发环境增加第三个 JVM。[RocketMQ 部署模式](https://rocketmq.apache.org/docs/deploymentOperations/01deploy/)

## 4. MySQL 与 Nacos 数据库

### 4.1 MySQL 初始化边界

Docker Official MySQL image 支持通过以下环境变量首次初始化：

```text
MYSQL_ROOT_PASSWORD
MYSQL_DATABASE
MYSQL_USER
MYSQL_PASSWORD
```

挂载到 `/docker-entrypoint-initdb.d/` 的 `.sql`/脚本只在数据目录首次初始化时执行；已有 named volume 时，修改环境变量或初始化 SQL 不会重建用户、密码和表。需要显式迁移或删除本地卷后重建，后者会清除数据。[MySQL Docker Official Image](https://hub.docker.com/_/mysql)

建议同一 MySQL 实例分离账号和数据库：

| 用途 | 数据库 | 账号 |
| --- | --- | --- |
| 供应链业务 | `scm` | `scm_app` |
| Nacos | `nacos_config` | `nacos` |

应用不能使用 MySQL root。密码放本地 `.env`，提交 `.env.example` 而不提交真实 `.env`。

### 4.2 Nacos 外置 MySQL

Nacos 官方 Docker 示例使用以下参数连接 MySQL：

```dotenv
MODE=standalone
SPRING_DATASOURCE_PLATFORM=mysql
MYSQL_SERVICE_HOST=mysql
MYSQL_SERVICE_PORT=3306
MYSQL_SERVICE_DB_NAME=nacos_config
MYSQL_SERVICE_USER=nacos
MYSQL_SERVICE_PASSWORD=...
MYSQL_SERVICE_DB_PARAM=characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
```

官方项目说明：使用自定义数据库时，第一次必须自行执行目标版本的初始化 schema；Nacos 3.2 版本升级也可能带来数据库结构变化，必须使用与固定镜像版本一致的 schema，不能从 `develop` 分支随手下载。[Nacos Docker MySQL 示例](https://github.com/nacos-group/nacos-docker/blob/master/example/standalone-mysql.yaml) [Nacos 升级手册](https://nacos.io/en/docs/latest/manual/admin/upgrading/)

推荐把 `3.2.3` 对应的 MySQL schema 固定在项目初始化目录，并通过一次性 `mysql-init` 服务或 `/docker-entrypoint-initdb.d/` 执行。Nacos 必须：

```yaml
depends_on:
  mysql:
    condition: service_healthy
```

Compose 默认只等待依赖容器“已启动”，不会等待服务“已就绪”；只有 `condition: service_healthy` 才会等待依赖 healthcheck 成功。[Compose 启动顺序](https://docs.docker.com/compose/how-tos/startup-order/)

## 5. Nacos 单机鉴权

Nacos 3.2 默认鉴权参数包括：

```dotenv
NACOS_AUTH_ENABLE=true
NACOS_AUTH_ADMIN_ENABLE=true
NACOS_AUTH_CONSOLE_ENABLE=true
NACOS_AUTH_SYSTEM_TYPE=nacos
NACOS_AUTH_TOKEN=<至少32个原始字符再Base64的签名密钥>
NACOS_AUTH_IDENTITY_KEY=<自定义服务端身份key>
NACOS_AUTH_IDENTITY_VALUE=<自定义服务端身份value>
```

官方约束：

- Nacos 3 的 OpenAPI/SDK、Admin API、Console API 鉴权默认开启。
- token signing key 必须是至少 32 个原始字符的 Base64 字符串。
- `identity.key/value` 和 token key 没有安全默认值，必须自行配置。
- 从 Nacos 2.4.0 起不再附带管理员默认密码。首次启动后通过控制台或 `POST /nacos/v3/auth/user/admin` 初始化 `nacos` 管理员密码。
- Nacos 自带鉴权是防误用的内部系统，不应把 Nacos 暴露到公网。[Nacos 鉴权手册](https://nacos.io/en/docs/latest/manual/admin/auth/)

一键脚本不能假设 `nacos/nacos`。正确流程是：

1. 生成 token key、identity key/value。
2. 启动 Nacos 并等待 readiness。
3. 如果管理员尚未初始化，调用一次初始化 API设置强密码。
4. 如果返回“管理员已存在”，不得自动覆盖现有密码。
5. 用管理员登录接口获取 token并验证一次需要鉴权的 API。

## 6. Dubbo 3 + Nacos：真实 RPC 配置

### 6.1 依赖与兼容性

建议统一使用 Dubbo BOM `3.3.6`，并引入官方 Nacos starter：

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.apache.dubbo</groupId>
      <artifactId>dubbo-bom</artifactId>
      <version>3.3.6</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo-spring-boot-starter3</artifactId>
  </dependency>
  <dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo-nacos-spring-boot-starter</artifactId>
  </dependency>
</dependencies>
```

Dubbo 3.3.6 官方发布记录包含 Nacos Client `2.5.1`；Nacos 3.2.x 官方兼容矩阵明确说明服务端兼容 Nacos Client 2.x 和 3.x，因此该组合有官方兼容依据。[Dubbo 3.3.6 发布说明](https://github.com/apache/dubbo/releases/tag/dubbo-3.3.6) [Nacos 3.2 客户端兼容矩阵](https://nacos.io/en/docs/latest/manual/admin/upgrading/#11-client-compatibility)

### 6.2 Provider 配置

Provider 运行在 Mac IDE、Nacos 在 Docker 时：

```yaml
dubbo:
  application:
    name: scm-inventory-provider
    metadata-type: remote
  protocol:
    name: tri
    port: 50051
    host: 127.0.0.1
  registry:
    address: nacos://127.0.0.1:8848
    username: nacos
    password: ${NACOS_PASSWORD}
    parameters:
      namespace: ${NACOS_NAMESPACE:public}
      group: ${NACOS_GROUP:DUBBO_GROUP}
    register-mode: instance
  metadata-report:
    address: nacos://127.0.0.1:8848
    username: nacos
    password: ${NACOS_PASSWORD}
```

Provider 服务实现必须使用真正的 Dubbo 导出注解：

```java
@DubboService
public class InventoryQueryServiceImpl implements InventoryQueryService {
    // ...
}
```

### 6.3 Consumer 配置

```yaml
dubbo:
  application:
    name: scm-order-consumer
  registry:
    address: nacos://127.0.0.1:8848
    username: nacos
    password: ${NACOS_PASSWORD}
    parameters:
      namespace: ${NACOS_NAMESPACE:public}
      group: ${NACOS_GROUP:DUBBO_GROUP}
    register-mode: instance
```

消费者必须使用 Dubbo 引用而不是本地 Spring Bean：

```java
@DubboReference(check = true, timeout = 3000)
private InventoryQueryService inventoryQueryService;
```

Dubbo 官方推荐新项目明确使用 Triple 协议；`tri` 默认端口 50051，支持 Java interface 和 Protobuf 两种服务定义。[Dubbo 协议文档](https://dubbo.apache.org/en/overview/mannual/java-sdk/tasks/protocols/protocol/)

### 6.4 什么才算“真实 RPC”

仅在 Nacos 控制台看到服务名，不等于 RPC 可用。完整路径是：

```text
Provider --注册应用/实例和元数据--> Nacos
Consumer --订阅地址列表----------> Nacos
Consumer --tri://host:50051------> Provider
```

必须同时满足：

1. Provider 和 Consumer 使用完全相同的 namespace ID 与 group。
2. Provider 注册到 Nacos 的协议 host/port 能被 Consumer 直接访问。
3. Provider 实际监听 `tri` 端口，接口由 `@DubboService` 导出。
4. Consumer 通过 `@DubboReference` 获得远程代理。
5. 验收调用返回的数据来自 Provider 进程，并可在 Provider 日志中看到请求。

Nacos 只负责注册发现和元数据，不转发 Dubbo 业务流量。如果 Consumer 在 Docker 容器、Provider 在 Mac 宿主机，Provider 不能向 Nacos 注册 `127.0.0.1`，因为容器里的 `127.0.0.1` 是 Consumer 自己。此时应注册容器可达的宿主机名称/地址，并让 Consumer 通过 `host.docker.internal:50051` 访问；或者把 Provider 与 Consumer 都放进 Compose 网络，按服务名互通。[Docker Desktop 宿主机访问](https://docs.docker.com/desktop/features/networking/networking-how-tos/)

Dubbo 官方说明 Nacos registry 地址格式支持用户名、密码、namespace 和 group；Dubbo 3 默认有应用级与接口级注册迁移机制，新项目可显式设置 `register-mode: instance`。[Dubbo Nacos registry](https://dubbo.apache.org/en/docs3-v2/java-sdk/reference-manual/registry/nacos/) [应用级服务发现](https://dubbo.apache.org/en/overview/mannual/java-sdk/reference-manual/upgrades-and-compatibility/migration-service-discovery/)

## 7. RocketMQ NameServer + Broker + Proxy

### 7.1 本地推荐启动方式

```text
rmqnamesrv:
  apache/rocketmq:5.5.0 sh mqnamesrv

rmqbroker:
  NAMESRV_ADDR=rmqnamesrv:9876
  apache/rocketmq:5.5.0 sh mqbroker \
    -n rmqnamesrv:9876 \
    --enable-proxy \
    -c /home/rocketmq/rocketmq-5.5.0/conf/broker.conf
```

关键端口：

| 组件 | 容器端口 | 用途 |
| --- | ---: | --- |
| NameServer | 9876 | 路由发现、传统 Remoting 客户端 |
| Broker | 10911 | Broker 主监听端口 |
| Broker | 10909/10912 | VIP/HA 等相关端口 |
| Proxy | 8080 | Remoting proxy |
| Proxy | 8081 | gRPC proxy，RocketMQ 5.x gRPC SDK 入口 |

官方 Docker 快速开始也是先启动 NameServer，再以 `--enable-proxy` 启动 Broker + Proxy，并发布上述端口。[RocketMQ Docker 快速开始](https://rocketmq.apache.org/docs/quickStart/02quickstartWithDocker/)

### 7.2 Apple Silicon

`apache/rocketmq:5.5.0` Docker Hub manifest 同时包含 `linux/amd64` 和 `linux/arm64`，无需第三方 ARM 镜像，也不要保留旧教程中的 `platform: linux/amd64`。[官方 Docker Hub 标签](https://hub.docker.com/r/apache/rocketmq/tags)

### 7.3 认证边界

RocketMQ 5.3.3 起不再支持 ACL 1.0，应使用 ACL 2.0。官方建议密码至少 16 字符并控制 super user 范围；账号密码不能提交到仓库。[RocketMQ ACL 2.0](https://rocketmq.apache.org/docs/bestPractice/06access/)

本地开发若暂不启用 ACL，必须只绑定 `127.0.0.1`，不能把 9876/10911/8080/8081 暴露到局域网或公网。若启用 ACL 2.0，Broker 和 Proxy 都要使用一致的认证/授权配置，并用真实 Producer/Consumer 做验收。

## 8. Redis 与 Nginx

### 8.1 Redis

推荐使用 ACL 用户而不是只设置默认用户密码：

```conf
user default off
user scm_app on >强密码 ~scm:* &scm:* +@read +@write +@connection +@transaction +@pubsub
appendonly yes
appendfsync everysec
```

配置文件只读挂载，数据使用 named volume。客户端连接：

```text
redis://scm_app:<password>@127.0.0.1:6379/0
```

Redis 官方 ACL 支持用户、键模式、频道模式和命令类别；默认用户关闭后，客户端必须同时提供用户名和密码。[Redis ACL](https://redis.io/docs/latest/operate/oss_and_stack/management/security/acl/)

### 8.2 Nginx

本地开发推荐 Nginx 监听容器 8080，宿主机发布 `127.0.0.1:8088:8080`。普通 `nginx.conf` 放只读 bind mount；Basic Auth 的 `.htpasswd` 单独作为本地 secret 文件并加入 `.gitignore`。

```nginx
location = /healthz {
    auth_basic off;
    return 200 "ok\n";
}

location / {
    auth_basic "SCM Local";
    auth_basic_user_file /etc/nginx/auth/.htpasswd;
    proxy_pass http://host.docker.internal:8080;
}
```

NGINX 官方模块支持 `crypt()` 和 Apache `apr1` 格式，明确不建议新密码使用明文或无盐 SHA-1。[NGINX Basic Auth](https://nginx.org/en/docs/http/ngx_http_auth_basic_module.html)

## 9. 健康检查矩阵

| 服务 | Compose healthcheck 推荐 | 验收含义 |
| --- | --- | --- |
| MySQL | `mysqladmin ping -h 127.0.0.1 -uroot -p... --silent` | 服务端已接受连接；随后还要用 `nacos`/`scm_app` 登录并执行 `SELECT 1` 验证账号权限 |
| Redis | `redis-cli --user scm_app --pass ... ping` | ACL 用户认证成功并返回 `PONG` |
| Nacos | `curl -fsS http://127.0.0.1:8080/v3/console/health/readiness` | Nacos Console/数据读取状态 ready；该端点从 3.0 起公开、无需认证。[Nacos Console API](https://nacos.io/docs/latest/manual/admin/console-api/) |
| NameServer | 检查日志出现 `The Name Server boot success`，再执行 `mqadmin clusterList -n rmqnamesrv:9876` | NameServer 已启动且能返回 Broker 路由 |
| Broker + Proxy | `mqadmin clusterList -n rmqnamesrv:9876` + TCP 检查 8081 | Broker 已向 NameServer 注册，gRPC Proxy 端口已监听 |
| Nginx | `wget -qO- http://127.0.0.1:8080/healthz` | Nginx worker 能处理 HTTP；另需验证受保护路径无凭据 401、正确凭据 200 |
| Dubbo Provider | Spring Boot readiness + Triple 端口 50051 +真实 RPC 冒烟调用 | 仅 Actuator UP 或 Nacos 已注册都不足以证明 RPC 成功 |

RocketMQ 官方启动文档以日志中的 `The Name Server boot success` 和 `The broker[...] boot success` 作为启动成功信号；真正的 Compose 健康检查还应使用 `mqadmin` 验证路由，避免“进程存在但未注册”。[RocketMQ Docker 快速开始](https://rocketmq.apache.org/docs/quickStart/02quickstartWithDocker/)

Compose 配置建议统一使用：

```yaml
healthcheck:
  interval: 10s
  timeout: 5s
  retries: 12
  start_period: 30s
```

Java 服务首次启动可能更慢，应为 Nacos/RocketMQ 增大 `start_period`。下游服务使用：

```yaml
depends_on:
  nacos:
    condition: service_healthy
```

但 `depends_on` 只解决容器启动顺序，不取代应用运行期的连接重试、超时、熔断和重连。[Compose 启动顺序](https://docs.docker.com/compose/how-tos/startup-order/)

## 10. 一键脚本验收标准

一键启动不能在 `docker compose up -d` 返回后立即宣告成功，应按以下顺序：

1. 检查 Docker Desktop 正常、Compose 可用、目标端口未占用。
2. 检查当前架构并用 `docker buildx imagetools inspect` 确认固定镜像有 `linux/arm64` 或 `linux/amd64`。
3. 生成本地 `.env` 强密码和 Nacos token/identity；不打印明文。
4. 启动 MySQL，等待 healthy，确认 Nacos schema 和两个业务账号。
5. 启动 Redis，执行带 ACL 的 `PING` 和 `SET/GET scm:install:probe`。
6. 启动 Nacos，检查 readiness，首次初始化管理员，再用登录 API验证鉴权。
7. 启动 RocketMQ NameServer、Broker + Proxy，检查 Broker 已注册且 8081 可达。
8. 启动 Nginx，检查 `/healthz=200`、受保护路径无凭据 401、正确凭据 200。
9. 启动 Dubbo provider/consumer，确认 Nacos 中实例健康并执行一次真实 RPC；停止 Provider 后，Consumer 调用应失败或走预期降级，不能偷偷调用同进程本地实现。
10. 输出固定版本、访问地址、账号名称和凭据文件位置，不在终端回显密码。

## 11. 结论

推荐本地基线为：

```text
MySQL       8.4.10
Redis       8.8.1-alpine
RocketMQ    5.5.0（NameServer + Broker --enable-proxy）
Nacos       3.2.3-slim（standalone + MySQL + 鉴权）
Nginx       1.30.4-alpine
Dubbo       3.3.6 + Triple + Nacos registry
```

这套版本全部有原生 Apple Silicon 支持。最容易被忽略的两个边界是：

- RocketMQ 本地环境应运行 NameServer、Broker、Proxy 三个逻辑组件，但 Broker + Proxy 用官方 Local mode 同进程即可。
- Dubbo 在 Nacos 中“注册成功”不等于 RPC 成功；必须确保 Provider 公布的 Triple 地址能被 Consumer 访问，并以真实远程调用作为最终验收。
