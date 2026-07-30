# Java 多环境与 Nacos 配置中心使用手册

## 1. 目标

后端九个业务服务统一支持 `test`、`prod` 两套环境，并遵循以下配置优先级：

1. Java 启动参数、系统环境变量（Spring Boot 标准最高优先级）。
2. Nacos 的服务级配置。
3. Nacos 的环境公共配置。
4. 项目内 `application-test.yml` 或 `application-prod.yml` 默认值。
5. 项目内 `application.yml` 公共默认值。

Nacos 配置使用 `optional:nacos:` 导入。Nacos 中没有对应 DataId，或开发时 Nacos 暂不可用，应用会继续使用项目内默认值；Nacos 有配置时，导入配置会覆盖本地默认值。

## 2. 版本与兼容性

| 组件 | 版本 |
| --- | --- |
| Java | 17（编译、测试、打包和运行统一基线） |
| Spring Boot | 4.0.0 |
| Spring Cloud Alibaba | 2025.1.0.0 |
| Nacos Config 接入 | `spring-cloud-starter-alibaba-nacos-config` |

原父 POM 的 Spring Boot `4.1.0` 不在 Spring Cloud Alibaba `2025.1.0.0` 的官方兼容范围内，因此调整为 `4.0.0`。

父 POM 使用 `java.version=17` 和 `maven.compiler.release=17`。一键启动脚本会校验实际 JVM 主版本，未找到 JDK 17 时直接终止，不会静默使用 JDK 21。

官方依据：

- [Spring Cloud Alibaba 版本说明](https://github.com/alibaba/spring-cloud-alibaba/releases)
- [Spring Cloud Alibaba 版本兼容关系](https://github.com/alibaba/spring-cloud-alibaba)
- [Nacos Config 快速开始](https://sca.aliyun.com/en/docs/2025.x/user-guide/nacos/quick-start/)
- [Nacos Config 高级用法](https://sca.aliyun.com/docs/2025.x/user-guide/nacos/advanced-guide/)
- [Spring Boot 外部化配置与 Config Data 导入优先级](https://docs.spring.io/spring-boot/reference/features/external-config.html)

## 3. 一键切换环境

### 3.1 本地 test 环境一键启动

在项目根目录执行：

```bash
cd project
./dev.sh foreground
```

脚本会依次完成：

1. 启动并检查 MySQL、Redis、RocketMQ、Nacos、Nginx。
2. 创建 RocketMQ Topic 和消费者组。
3. 创建 `scm-test`、`scm-prod` namespace，并发布两套共 20 个 DataId。
4. 仅使用本机 Maven 仓库，以 JDK 17 构建九个后端 JAR。
5. 启动九个后端服务和前端。
6. 由 Flyway 自动初始化各服务数据库。

前台运行时按 `Ctrl+C` 结束守护进程。也可以另开终端停止应用：

```bash
cd project
./dev.sh down
```

如需同时停止 Docker 中间件：

```bash
./dev.sh down --with-middleware
```

JDK 17 默认从 `JDK17_HOME`、macOS `java_home`、IntelliJ IDEA JBR、GoLand JBR 中依次查找；推荐显式设置：

```bash
export JDK17_HOME=/path/to/jdk-17
```

### 3.2 test/prod 构建切换

在 `project/backend` 目录执行：

```bash
# 测试环境；test 是默认 Maven Profile
mvn -Ptest clean package

# 生产环境
mvn -Pprod clean package
```

也可以在 IDE 或临时启动时覆盖活动环境：

```bash
SPRING_PROFILES_ACTIVE=test java -jar supplier-service/target/supplier-service-0.1.0-SNAPSHOT.jar
SPRING_PROFILES_ACTIVE=prod java -jar supplier-service/target/supplier-service-0.1.0-SNAPSHOT.jar
```

推荐构建环境和运行环境保持一致，即 test 包运行 test、prod 包运行 prod。因为 Maven Profile 同时提供对应环境的 Nacos 地址和 namespace 默认值。

## 4. Nacos 连接配置

Nacos 连接参数固定定义在父 POM：

| Maven Profile | 地址默认值 | namespace | group | 用户名/密码 |
| --- | --- | --- | --- | --- |
| `test` | `127.0.0.1:8848` | `scm-test` | `SCM_GROUP` | `nacos/nacos` |
| `prod` | `nacos:8848` | `scm-prod` | `SCM_GROUP` | `nacos/nacos` |

文件位置：`project/backend/pom.xml`。

如部署环境的地址或固定凭证不同，可修改 POM 中相应 Profile。保留了 `NACOS_SERVER_ADDR`、`NACOS_NAMESPACE`、`NACOS_GROUP`、`NACOS_USERNAME`、`NACOS_PASSWORD` 环境变量作为应急覆盖入口。

> 生产环境必须把示例的 `nacos/nacos`、`change-me` 改为真实凭证。业务中间件密码应放在 Nacos 或密钥管理系统，不应提交真实值到 Git。

## 5. Nacos namespace、group 与 DataId

先在 Nacos 创建两个 namespace：

- `scm-test`
- `scm-prod`

两个 namespace 均使用 group `SCM_GROUP`。每个服务会按顺序导入两个 DataId：

```text
scm-common-{env}.yaml
{spring.application.name}-{env}.yaml
```

服务级 DataId 后导入，因此相同配置项以服务级值为准。

| 服务 | test DataId | prod DataId |
| --- | --- | --- |
| 公共配置 | `scm-common-test.yaml` | `scm-common-prod.yaml` |
| IAM | `iam-service-test.yaml` | `iam-service-prod.yaml` |
| 主数据 | `mdm-service-test.yaml` | `mdm-service-prod.yaml` |
| 供应商 | `supplier-service-test.yaml` | `supplier-service-prod.yaml` |
| 采购 | `purchase-service-test.yaml` | `purchase-service-prod.yaml` |
| WMS | `wms-service-test.yaml` | `wms-service-prod.yaml` |
| 中央库存 | `inventory-service-test.yaml` | `inventory-service-prod.yaml` |
| OMS | `oms-service-test.yaml` | `oms-service-prod.yaml` |
| TMS | `tms-service-test.yaml` | `tms-service-prod.yaml` |
| BMS | `bms-service-test.yaml` | `bms-service-prod.yaml` |

Nacos 配置格式选择 `YAML`。

## 6. Nacos 配置模板

### 6.1 环境公共配置

`scm-common-test.yaml` 示例：

```yaml
spring:
  datasource:
    username: scm_app
    password: <test-mysql-password>
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 3000
```

`scm-common-prod.yaml` 示例：

```yaml
spring:
  datasource:
    username: scm_app
    password: <prod-mysql-password>
    hikari:
      maximum-pool-size: 20
      minimum-idle: 4
      connection-timeout: 3000
```

### 6.2 普通 MySQL 服务

以 `inventory-service-prod.yaml` 为例，其他服务只替换数据库名：

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/scm_inventory?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
server:
  port: 8104
```

数据库名对应关系：

| 服务 | 数据库 |
| --- | --- |
| IAM | `scm_iam` |
| 主数据 | `scm_mdm` |
| 供应商 | `scm_supplier` |
| 采购 | `scm_purchase` |
| WMS | `scm_wms` |
| 中央库存 | `scm_inventory` |
| OMS | `scm_oms` |
| TMS | `scm_tms` |
| BMS | `scm_bms` |

### 6.3 供应商服务：MySQL、Redis、RocketMQ、Dubbo

`supplier-service-prod.yaml` 示例：

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/scm_supplier?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
  data:
    redis:
      host: redis
      port: 6379
      username: scm_app
      password: <prod-redis-password>
      database: 1
      timeout: 2s

scm:
  dubbo:
    registry-address: nacos://nacos:8848?username=nacos&password=<prod-nacos-password>
    timeout-ms: 2000
  rocketmq:
    enabled: true
    endpoints: rocketmq:8081
    topic: supplier-domain-event
    master-data-consumer:
      enabled: true
      topic: master-data-domain-event
      group: supplier-master-data-snapshot
    contract-approval-consumer:
      enabled: true
      topic: iam-approval-domain-event
      group: supplier-contract-approval
```

### 6.4 采购服务：RocketMQ

`purchase-service-prod.yaml` 示例：

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/scm_purchase?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai

scm:
  rocketmq:
    enabled: true
    endpoints: rocketmq:8081
    purchase-topic: purchase-domain-event
```

## 7. 本地默认值

Nacos 未返回配置时：

| 环境 | MySQL | Redis | RocketMQ | Dubbo |
| --- | --- | --- | --- | --- |
| test | `127.0.0.1:3306`，`root/root` | `127.0.0.1:6379`，无密码 | `127.0.0.1:8081`，默认关闭 | `N/A` |
| prod | `mysql:3306`，`scm_app/change-me` | `redis:6379`，`scm_app/change-me` | `rocketmq:8081`，默认开启 | `nacos://nacos:8848` |

这些值是兜底和首次启动示例。生产环境应在 Nacos 发布真实地址与密码，或者用部署环境变量覆盖。

## 8. 配置生效验证

查看 Maven 过滤结果：

```bash
mvn -Ptest -pl supplier-service resources:resources
grep -n "active:\\|server-addr:\\|namespace:" supplier-service/target/classes/application.yml

mvn -Pprod -pl supplier-service resources:resources
grep -n "active:\\|server-addr:\\|namespace:" supplier-service/target/classes/application.yml
```

预期分别看到：

```text
active: test
server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
namespace: ${NACOS_NAMESPACE:scm-test}
```

```text
active: prod
server-addr: ${NACOS_SERVER_ADDR:nacos:8848}
namespace: ${NACOS_NAMESPACE:scm-prod}
```

启动日志中还应能看到对应 Nacos DataId 的加载记录。修改 Nacos 配置后，支持刷新的配置项会收到变更；连接池等不支持原地重建的 Bean 仍建议滚动重启服务。

## 继续上下文

当前结论：九个 Java 业务服务统一使用 JDK 17、test/prod 与 Nacos Config；本地环境支持一键启动。
关键假设：Nacos namespace 为 `scm-test`、`scm-prod`，group 为 `SCM_GROUP`。
待决问题：上线前替换全部示例密码和生产主机名。
下一步：本地执行 `project/dev.sh foreground`；部署环境按 `-Ptest`/`-Pprod` 构建。
