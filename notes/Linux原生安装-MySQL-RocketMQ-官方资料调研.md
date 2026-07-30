# Linux 原生安装 MySQL 与 RocketMQ：官方资料调研

> 调研日期：2026-07-20  
> 资料范围：仅使用 Ubuntu/Canonical、Oracle MySQL、Apache RocketMQ 及 Apache 项目源码等官方一手资料。  
> 目标拓扑：MySQL 安装在 `k8s-worker1`；RocketMQ NameServer + Broker 安装在 `k8s-worker2`；两者都以 Linux 原生 systemd 服务运行，不使用 Docker/Pod。

## 1. 可落地结论

| 组件 | 选型 | 结论 |
| --- | --- | --- |
| MySQL | Ubuntu 24.04 LTS 官方 APT 包 | 执行 `apt install mysql-server`；截至调研日期，`noble-updates` 包页显示 `8.0.46-0ubuntu0.24.04.3`。APT 安装时应以当时仓库的安全更新版本为准，安装后记录 `mysql --version`。[安装指南](https://documentation.ubuntu.com/server/how-to/databases/install-mysql/) [Ubuntu 包页](https://packages.ubuntu.com/noble-updates/database/mysql-server) |
| MySQL 认证 | 保留 `root@localhost` 的 `auth_socket`，另建密码管理员和应用账号 | Ubuntu 默认允许通过 `sudo mysql -u root` 本机管理，不需要 MySQL root 密码。这避免开放远程 root，也不会把 root 密码塞入自动化命令行。[官方说明](https://documentation.ubuntu.com/server/how-to/databases/install-mysql/#user-setup) |
| MySQL 密码插件 | `caching_sha2_password` | Ubuntu 指南明确给出该插件的创建账号方法；MySQL 8.0 也将它作为默认并推荐使用，`mysql_native_password` 已属过时方案。[官方认证文档](https://dev.mysql.com/doc/refman/8.0/en/caching-sha2-pluggable-authentication.html) |
| RocketMQ | Apache RocketMQ 5.5.0 官方二进制包 | 官方下载页显示 5.5.0 发布于 2026-04-10，为调研日期的最新稳定发行版；安装脚本应锁定版本，不使用漂移的 `latest`。[下载页](https://rocketmq.apache.org/download/) [5.5.0 Release Notes](https://rocketmq.apache.org/release-notes/2026/04/10/5.5.0/) |
| RocketMQ Java | Ubuntu `openjdk-17-jdk-headless` | RocketMQ 官方要求 64 位 JDK 8+；5.5.0 启动脚本区分 Java 9 以上的 JVM 参数，且 5.5.0 修复了 Linux 自定义 `JAVA_HOME`支持。因此 Ubuntu 24.04 使用 JDK 17 是符合官方兼容范围的保守选择。[快速入门](https://rocketmq.apache.org/docs/quickStart/01quickstart/) [5.5.0 启动脚本](https://github.com/apache/rocketmq/blob/rocketmq-all-5.5.0/distribution/bin/runbroker.sh) |
| RocketMQ ACL | ACL 2.0 | ACL 2.0 适用于 5.3.0+；5.3.3 起 ACL 1.0 已移除。5.5.0 必须配 ACL 2.0，不能继续使用 `plain_acl.yml`。[ACL 2.0 官方文档](https://rocketmq.apache.org/docs/bestPractice/06access/) |

## 2. MySQL 安装与账号的官方依据

### 2.1 安装、服务和监听

- Ubuntu 官方安装命令为 `sudo apt install mysql-server`，包安装完成后服务应自动启动，可用 `systemctl status mysql`/`service mysql status` 检查，用 `journalctl -u mysql` 排障。[来源](https://documentation.ubuntu.com/server/how-to/databases/install-mysql/)
- Ubuntu 默认示例只在 `127.0.0.1:3306` 监听。如需远程连接，修改 `/etc/mysql/mysql.conf.d/mysqld.cnf` 中 `bind-address` 为 **MySQL 虚拟机的固定内网 IP**，再执行 `systemctl restart mysql.service`。不建议默认配成 `0.0.0.0`，因为定向绑定可减少暴露面。[来源](https://documentation.ubuntu.com/server/how-to/databases/install-mysql/#configure-mysql)
- 用 `ss -lntp` 检查开放端口是 Ubuntu 安全文档给出的官方方法。[来源](https://documentation.ubuntu.com/security/security-features/network/open-ports/)

### 2.2 root 账号的推荐做法

**推荐默认：不把 `root@localhost` 从 `auth_socket` 改成密码认证。**

理由：Ubuntu 的默认模型是本机特权用户通过 `sudo mysql -u root` 进入，数据库 root 不能从远程登录，也无需在安装脚本、shell 历史或进程参数中保留 root 密码。[来源](https://documentation.ubuntu.com/server/how-to/databases/install-mysql/#user-setup)

如业务要求必须存在“密码管理员”，应另建立 `dbadmin@localhost`，而不是开放远程 root。若确实要转换 root，MySQL 官方支持：

```sql
ALTER USER 'root'@'localhost'
  IDENTIFIED WITH caching_sha2_password BY '<ROOT_STRONG_PASSWORD>';
```

这一操作会改变 Ubuntu 默认的 `sudo mysql` 运维方式，并且一键脚本后续不能再依赖 socket 无密码登录。MySQL 官方语法见 [ALTER USER](https://dev.mysql.com/doc/refman/8.0/en/assigning-passwords.html)。

### 2.3 应用库和远程 app 账号

建议脚本创建一个业务库（例如 `supply_chain`）和一个最小权限账号，账号 Host 应限制到实际应用节点 IP：

```sql
CREATE DATABASE IF NOT EXISTS `supply_chain`
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE USER 'scm_app'@'<APP_NODE_IP>'
  IDENTIFIED WITH caching_sha2_password BY '<APP_STRONG_PASSWORD>'
  FAILED_LOGIN_ATTEMPTS 5 PASSWORD_LOCK_TIME 1;

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX,
      REFERENCES, EXECUTE
ON `supply_chain`.* TO 'scm_app'@'<APP_NODE_IP>';
```

实施要点：

- MySQL 账号由 `user@host` 共同确定。不应为了方便直接创建 `'scm_app'@'%'`；MySQL 官方已将 Host 中 `%`/`_` 的通配行为标记为弃用。可使用精确 IP，或 IPv4 网络/掩码表达式。[账号名官方文档](https://dev.mysql.com/doc/refman/8.4/en/account-names.html)
- `CREATE USER` 时账号初始无权限，必须显式 `GRANT`；可设置失败登录次数和临时锁定时间。[来源](https://dev.mysql.com/doc/refman/8.4/en/create-user.html)
- 上述权限满足常规应用 schema migration，但不包含全局管理权限。生产环境更建议拆成运行账号（DML）与迁移账号（DDL）。
- 若客户端跨越不可信网络，应给账号加 `REQUIRE SSL`，客户端使用 `--ssl-mode=REQUIRED` 或更严格的 `VERIFY_CA/VERIFY_IDENTITY`。[加密连接官方文档](https://dev.mysql.com/doc/refman/8.0/en/using-encrypted-connections.html)

### 2.4 防火墙

只允许应用节点或明确的内网网段访问 3306，例如：

```bash
sudo ufw allow proto tcp from <APP_NODE_IP> to any port 3306
```

Ubuntu 官方 UFW 文档提供了该精确来源 IP/网段语法。如果现有 Kubernetes 安装已禁用 UFW，一键脚本不应自作主张重新启用 UFW，而应明确警告用户在 VMware/上游防火墙中执行同等限制。[来源](https://documentation.ubuntu.com/server/how-to/security/firewalls/)

### 2.5 密码自动化安全

- 不把 MySQL 密码放在 `mysql -pPASSWORD` 或 `mysqladmin ... password "..."` 的命令行参数中。MySQL 官方明确警告，命令行密码可能暂时或持续被 `ps` 等系统状态工具看到。[来源](https://dev.mysql.com/doc/refman/8.0/en/assigning-passwords.html)
- 一键脚本应交互式隐藏读取密码，临时配置/SQL 文件权限必须为 `0600`，使用后立即删除，并避免在日志中输出密码。
- 密码要经过 SQL 字符串安全转义；不允许将未校验的库名、用户名、Host 原样拼接到 SQL 标识符中。

## 3. RocketMQ 安装的官方依据

### 3.1 版本、下载和 SHA-512

应下载官方二进制发行包，同时下载官方 `.sha512`，在解压前校验：

```bash
ROCKETMQ_VERSION=5.5.0
BASE_URL="https://dist.apache.org/repos/dist/release/rocketmq/${ROCKETMQ_VERSION}"
FILE="rocketmq-all-${ROCKETMQ_VERSION}-bin-release.zip"

curl --fail --location --proto '=https' --tlsv1.2 \
  --output "$FILE" "$BASE_URL/$FILE"
curl --fail --location --proto '=https' --tlsv1.2 \
  --output "$FILE.sha512" "$BASE_URL/$FILE.sha512"
EXPECTED_SHA512="$(sed '1s/^[^:]*://' "$FILE.sha512" | tr -d '[:space:]' | tr '[:upper:]' '[:lower:]')"
ACTUAL_SHA512="$(sha512sum "$FILE" | awk '{print $1}')"
test "$ACTUAL_SHA512" = "$EXPECTED_SHA512"
```

- 5.5.0 Release Notes 直接列出 Binary、PGP 和 SHA512 链接。[来源](https://rocketmq.apache.org/release-notes/2026/04/10/5.5.0/)
- 调研日期直接读取官方 SHA-512 文件得到 `498F831C4B1BA95C23E34648FBEE5AEE94D77E0AABDD142D68BF90837D1925E5534CF01EDD852A56847F4DBA67AAE01DDDECA703C58845701DAF64BF006065F8`。Apache 此文件采用“文件名 + 冒号 + 分行空格哈希”的格式，并不是 GNU `sha512sum -c` 格式，所以脚本应像上例一样规范化后比较；仍须实时下载[Apache 官方 `.sha512` 文件](https://downloads.apache.org/rocketmq/5.5.0/rocketmq-all-5.5.0-bin-release.zip.sha512)，不能只信任脚本内常量。
- Apache RocketMQ 发版手册要求下载发行包、`.asc` 和 `.sha512` 并校验。[来源](https://rocketmq.apache.org/docs/contributionGuide/04release-manual/)
- 自动脚本的最低要求是 SHA-512 成功才解压；更高保证需再用 Apache 的 KEYS 校验 `.asc`。

### 3.2 安装目录与 systemd

Apache 二进制包自带 `bin/mqnamesrv`、`bin/mqbroker`、`bin/mqadmin` 等脚本，官方快速入门使用 `nohup` 启动，**并未提供 systemd unit**。因此下列是对官方前台命令的 Linux 原生托管封装，不是 Apache 预置服务：

- 创建专用无登录 shell 用户 `rocketmq`，不以 root 运行。
- 程序建议安装到 `/opt/rocketmq-5.5.0`，并用 `/opt/rocketmq` 软链接指向当前版本；持久化数据放 `/var/lib/rocketmq`，日志放 `/var/log/rocketmq`。
- `rocketmq-namesrv.service` 执行 `/opt/rocketmq/bin/mqnamesrv`。
- `rocketmq-broker.service` 应设置 `After=network-online.target rocketmq-namesrv.service` 与 `Requires=rocketmq-namesrv.service`，执行 `/opt/rocketmq/bin/mqbroker -n <ROCKETMQ_IP>:9876 -c /opt/rocketmq/conf/broker.conf`。
- `Restart=on-failure`，`LimitNOFILE=655350`；优先使用 systemd 的 `User=rocketmq` 和目录权限做隔离。RocketMQ 官方 JVM/OS 文档建议文件描述符上限 655350。[来源](https://rocketmq.apache.org/docs/bestPractice/07JVMOS/)
- 停止时 systemd 发送 `SIGTERM`；RocketMQ Broker 源码注册了 JVM shutdown hook 做优雅关停。[官方源码](https://github.com/apache/rocketmq/blob/rocketmq-all-5.5.0/broker/src/main/java/org/apache/rocketmq/broker/BrokerStartup.java)

### 3.3 8 GB 节点的 JVM 必要修改

RocketMQ 5.5.0 发行包默认参数不能直接用于本拓扑：

- `runserver.sh` 默认 `-Xms4g -Xmx4g`。[官方源码](https://github.com/apache/rocketmq/blob/rocketmq-all-5.5.0/distribution/bin/runserver.sh)
- `runbroker.sh` 默认 `-Xms8g -Xmx8g -XX:MaxDirectMemorySize=15g`。[官方源码](https://github.com/apache/rocketmq/blob/rocketmq-all-5.5.0/distribution/bin/runbroker.sh)

`k8s-worker2` 总内存只有 8 GB，还要运行 Ubuntu、containerd、kubelet 和可能的 Pod。如不覆盖，Broker 在 `AlwaysPreTouch` 下很可能启动失败或触发 OOM。建议试验环境显式配置：

```ini
# NameServer systemd Environment
JAVA_OPT_EXT=-Xms512m -Xmx512m

# Broker systemd Environment
JAVA_OPT_EXT=-Xms2g -Xmx2g -XX:MaxDirectMemorySize=1g
```

但需特别注意：5.5.0 原脚本把默认 JVM 参数放在 `JAVA_OPT_EXT` 之前，同名参数重复时是否以最后一个为准依赖 JVM 解析行为。一键脚本实施后必须通过以下命令核验最终参数，不能只检查 unit 文件：

```bash
jcmd "$(pgrep -f 'NamesrvStartup')" VM.flags
jcmd "$(pgrep -f 'BrokerStartup')" VM.flags
```

RocketMQ 官方的通用原则是 `Xms` 与 `Xmx` 相同，使用 G1，生产环境不建议盲目将 Broker 缩到 2G。这里 2G 是针对“8G 工作节点共机试验”的资源折中；生产应使用独立 Broker 主机并按压测调整。[官方 JVM/OS 文档](https://rocketmq.apache.org/docs/bestPractice/07JVMOS/)

### 3.4 NameServer/Broker 端口

| 端口 | 组件 | 用途与依据 | 防火墙建议 |
| --- | --- | --- | --- |
| `9876/tcp` | NameServer | 官方 5.5.0 README/快速入门明确 NameServer 监听 `0.0.0.0:9876`。[来源](https://github.com/apache/rocketmq/tree/rocketmq-all-5.5.0#run-rocketmq-locally) | 只向 Broker、可信应用节点与运维机开放 |
| `10911/tcp` | Broker Remoting | 5.5.0 `BrokerStartup` 显式将 Broker Netty 端口设为 10911。[源码](https://github.com/apache/rocketmq/blob/rocketmq-all-5.5.0/broker/src/main/java/org/apache/rocketmq/broker/BrokerStartup.java) | 只向可信应用节点与运维机开放 |
| `10909/tcp` | Broker Fast Remoting | `BrokerController` 以主监听端口减 2 创建 fast remoting server，因此默认为 10909。[源码](https://github.com/apache/rocketmq/blob/rocketmq-all-5.5.0/broker/src/main/java/org/apache/rocketmq/broker/BrokerController.java) | 与 10911 相同的可信来源 |
| `10912/tcp` | Broker HA | `BrokerStartup` 在 HA 端口未指定时设为主监听端口 + 1，默认 10912。[源码](https://github.com/apache/rocketmq/blob/rocketmq-all-5.5.0/broker/src/main/java/org/apache/rocketmq/broker/BrokerStartup.java) | 单 Master 无 Slave 时不对其他主机开放；集群时仅 Broker 间开放 |

官方安全基线指出：若未开启 ACL，任何可达 RocketMQ 端口的主体都可能发送、接收或执行管理操作；必须开启 ACL 或严格隔离到可信网络。[来源](https://rocketmq.apache.org/docs/security/01security/)

### 3.5 Broker 基础配置

试验环境的单 Master Broker 建议至少显式配置：

```properties
brokerClusterName=DefaultCluster
brokerName=broker-a
brokerId=0
brokerIP1=<ROCKETMQ_NODE_IP>
namesrvAddr=<ROCKETMQ_NODE_IP>:9876
listenPort=10911
storePathRootDir=/var/lib/rocketmq/store
storePathCommitLog=/var/lib/rocketmq/store/commitlog
autoCreateTopicEnable=false
autoCreateSubscriptionGroup=false
deleteWhen=04
fileReservedTime=72
brokerRole=ASYNC_MASTER
flushDiskType=ASYNC_FLUSH
```

- `brokerIP1` 必须是其他节点可达的 VMware 固定 IP，不能广播成 `127.0.0.1`。
- 禁用自动建 Topic/消费组是运维可控性选择，不是启动必需条件；后续应使用已认证的 `mqadmin` 显式创建。
- 单 Broker 是学习/开发拓扑，不具备高可用；其主机或数据盘故障会中断服务甚至丢失消息。

## 4. RocketMQ ACL 2.0 账号密码

### 4.1 Broker 必需配置字段

```properties
authenticationEnabled=true
authenticationMetadataProvider=org.apache.rocketmq.auth.authentication.provider.LocalAuthenticationMetadataProvider
authenticationStrategy=org.apache.rocketmq.auth.authentication.strategy.StatefulAuthenticationStrategy

authorizationEnabled=true
authorizationMetadataProvider=org.apache.rocketmq.auth.authorization.provider.LocalAuthorizationMetadataProvider
authorizationStrategy=org.apache.rocketmq.auth.authorization.strategy.StatefulAuthorizationStrategy

initAuthenticationUser={"username":"<RMQ_ADMIN_USER>","password":"<RMQ_ADMIN_PASSWORD>"}
innerClientAuthenticationCredentials={"accessKey":"<RMQ_ADMIN_USER>","secretKey":"<RMQ_ADMIN_PASSWORD>"}
```

官方字段语义：

- `authenticationEnabled`/`authorizationEnabled` 默认都为 `false`，必须显式开启。
- `authenticationMetadataProvider` 与 `authorizationMetadataProvider` 是必填提供者；单 Broker 本地元数据使用官方的 `Local...MetadataProvider`。
- 官方对生产推荐 Stateful 认证/授权策略。
- `initAuthenticationUser` 是首次启动自动创建的系统初始化用户。
- `innerClientAuthenticationCredentials` 用于组件内部认证；有组件间通信时凭据必须一致。

以上均来自 [ACL 2.0 配置参数](https://rocketmq.apache.org/docs/bestPractice/06access/#configuration)。官方安全提示要求生产密码至少 16 位、包含大小写字母/数字/特殊字符，不能使用文档示例密码或将凭据明文提交到代码仓库。[来源](https://rocketmq.apache.org/docs/bestPractice/06access/)

> 注意：RocketMQ 的 [参数约束](https://rocketmq.apache.org/docs/introduction/03limits/) 另建议 AK/SK/Token 只使用 `a-z A-Z 0-9`。官方 ACL 文档的“特殊字符”密码建议与此存在张力。为避免客户端兼容性问题，本自动化建议为 RocketMQ 生成至少 24 位的随机大小写字母+数字密码，用长度和随机性补偿字符集限制；这是对两份官方指南的实施性折中。

### 4.2 mqadmin 凭据和验证

ACL 开启后，`mqadmin` 要使用初始化超级用户，在 `/opt/rocketmq/conf/tools.yml` 配置：

```yaml
accessKey: <RMQ_ADMIN_USER>
secretKey: <RMQ_ADMIN_PASSWORD>
```

文件必须属于 `rocketmq:rocketmq` 且权限 `0600`。然后执行官方验证命令：

```bash
sudo -u rocketmq /opt/rocketmq/bin/mqadmin \
  listUser -n <ROCKETMQ_NODE_IP>:9876 -c DefaultCluster
```

官方文档明确指定 `tools.yml` 使用 `accessKey`/`secretKey`，并以 `listUser` 作为连通与认证验证。[来源](https://rocketmq.apache.org/docs/bestPractice/06access/#mqadmin-tool-configuration)

业务应用不应使用超级用户。应再创建 Normal 用户，按 Topic/Group 授予最小的 `Pub`/`Sub` 权限：

```bash
sudo -u rocketmq /opt/rocketmq/bin/mqadmin createUser \
  -n <ROCKETMQ_NODE_IP>:9876 -c DefaultCluster \
  -u <RMQ_APP_USER> -p <RMQ_APP_PASSWORD> -t Normal

sudo -u rocketmq /opt/rocketmq/bin/mqadmin createAcl \
  -n <ROCKETMQ_NODE_IP>:9876 -c DefaultCluster \
  -s User:<RMQ_APP_USER> -r Topic:<APP_TOPIC_PATTERN> \
  -a Pub,Sub -d Allow

sudo -u rocketmq /opt/rocketmq/bin/mqadmin getUser \
  -n <ROCKETMQ_NODE_IP>:9876 -c DefaultCluster -u <RMQ_APP_USER>
```

这些用户和 ACL 命令的语法来自 [ACL 2.0 官方场景示例](https://rocketmq.apache.org/docs/bestPractice/06access/#best-practices)。一键脚本不应把密码通过命令行参数长期暴露；但 `mqadmin createUser` 官方 CLI 语法本身要求 `-p`，因此应在受控安装时段执行，不打印 shell xtrace，不写入日志，执行后从 shell 变量中清除。

## 5. 一键安装脚本应有的验收项

### MySQL

```bash
systemctl is-enabled mysql
systemctl is-active mysql
mysql --version
sudo mysql -NBe "SELECT VERSION();"
sudo mysql -NBe "SELECT user,host,plugin FROM mysql.user;"
ss -lntp | grep ':3306'
mysqladmin --host=<MYSQL_NODE_IP> --user=<MYSQL_APP_USER> --password ping
```

最后一条会交互提示密码，不应使用 `--password=<value>`。还应从真正的应用节点执行远程连接，确认 Host 授权、路由与防火墙同时正确。

### RocketMQ

```bash
systemctl is-enabled rocketmq-namesrv rocketmq-broker
systemctl is-active rocketmq-namesrv rocketmq-broker
java -version
ss -lntp | grep -E ':(9876|10909|10911|10912)\b'
sudo -u rocketmq /opt/rocketmq/bin/mqadmin \
  clusterList -n <ROCKETMQ_NODE_IP>:9876
sudo -u rocketmq /opt/rocketmq/bin/mqadmin \
  listUser -n <ROCKETMQ_NODE_IP>:9876 -c DefaultCluster
jcmd "$(pgrep -f 'NamesrvStartup')" VM.flags
jcmd "$(pgrep -f 'BrokerStartup')" VM.flags
```

除进程和端口外，必须验证 `clusterList` 能看到 `broker-a`、`listUser` 能在 ACL 下成功，且 JVM 最终堆上限确实是为 8G 主机设定的值。

## 6. 关键风险

1. **内存过量分配**：RocketMQ 官方默认 Broker 堆就是 8G，而整个 `k8s-worker2` 只有 8G。不覆盖 JVM 参数必然高风险；即使改为 2G，也应给该 Kubernetes 节点打 taint/label 或限制 Pod，避免 Broker 与 Pod 争抢内存。
2. **单点不高可用**：MySQL 与 RocketMQ 都只有单实例；工作节点宕机、VMware 磁盘损坏或宿主机故障都会导致业务不可用。本拓扑适合开发/学习，不能作为生产高可用设计。
3. **凭据明文持久化**：RocketMQ ACL 初始化凭据存在 `broker.conf`/`tools.yml`中；必须权限 `0600`、不纳入 Git，并定期轮换。systemd unit 也不应直接内嵌密码。
4. **MySQL root 需求误解**：“配好账号密码”不等于必须将 Ubuntu 的 socket root 改成密码 root。推荐保留 socket root，给 app/运维另建最小权限的密码账号。
5. **网络暴露**：`bind-address` 与 RocketMQ `brokerIP1` 解决服务监听/广播，不等于防火墙。即使开了 ACL，3306/9876/10909/10911/10912 也应仅允许信任网段。
6. **RocketMQ ACL 版本差异**：5.5.0 必须用 ACL 2.0，网上大量 `aclEnable=true + plain_acl.yml` 的 ACL 1.0 教程不再适用。
7. **安装可重入性**：`initAuthenticationUser` 只用于初始用户自动创建。已有 RocketMQ 元数据或 MySQL 数据的节点重跑脚本时，不得销毁数据目录或无条件重置密码；脚本必须区分首次安装和已安装状态。

## 7. 实施给主文档/脚本的默认值

| 项目 | 建议默认 |
| --- | --- |
| MySQL 节点 | `k8s-worker1` / 由用户传入固定 IP |
| MySQL root | 保留 `auth_socket`，仅本地 `sudo mysql` |
| MySQL 库/账号 | `supply_chain` / `scm_app` / 隐藏交互输入密码 |
| MySQL Host | 由用户传入的应用节点 IP 或 IPv4 网络/掩码，不默认 `%` |
| RocketMQ 节点 | `k8s-worker2` / 由用户传入固定 IP |
| RocketMQ 版本 | `5.5.0`，官方 binary + SHA-512 强制校验 |
| RocketMQ 模式 | 单 NameServer + 单 ASYNC_MASTER Broker，不启 Proxy |
| RocketMQ JVM | NameServer 512M；Broker 2G + Direct Memory 1G，安装后用 `jcmd` 验证 |
| RocketMQ ACL | ACL 2.0；初始超级用户 + 独立 Normal app 用户；凭据隐藏输入 |
| systemd | 专用 `rocketmq` 用户，`Restart=on-failure`，Broker 依赖 NameServer |
