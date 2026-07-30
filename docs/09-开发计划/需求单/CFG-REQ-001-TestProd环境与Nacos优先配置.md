# CFG-REQ-001 Test/Prod 环境与 Nacos 优先配置

## 1. 目标

为全部 Java 服务增加 `test`、`prod` 环境配置；只切换 Maven/Spring Profile 即可使用对应环境。所有业务配置允许由 Nacos 覆盖，本地文件保留完整默认值；远端配置缺失时服务仍能使用默认值启动。

## 2. 范围

- 九个可部署 Java 业务服务；`scm-common` 作为公共/base 模块随业务服务构建。
- MySQL、Redis、RocketMQ、Dubbo/Nacos 等中间件配置。
- 父 POM 的依赖版本、Maven Profile、资源过滤。
- Nacos namespace、group、DataId 及运维使用说明。

## 3. 配置契约

| 项目 | 约定 |
| --- | --- |
| 环境 | `test`、`prod` |
| 切换方式 | `mvn -Ptest ...`、`mvn -Pprod ...`；运行时可用 `SPRING_PROFILES_ACTIVE` 应急覆盖 |
| Nacos namespace | `scm-test`、`scm-prod` |
| Nacos group | `SCM_GROUP` |
| 公共 DataId | `scm-common-{env}.yaml` |
| 服务 DataId | `${spring.application.name}-{env}.yaml` |
| 缺失策略 | `optional:nacos:`，缺失时使用本地 Profile 默认值 |
| 覆盖顺序 | 服务级 Nacos > 公共 Nacos > Profile 默认值 > 公共默认值 |
| 固定连接凭证 | 父 POM Profile 中配置 Nacos 用户名、密码默认值 |

## 4. 验收标准

- [x] 九个服务均引入 Nacos Config starter。
- [x] 九个服务均有 `application-test.yml` 和 `application-prod.yml`。
- [x] test/prod 均包含 MySQL 完整默认值。
- [x] 使用 Redis、RocketMQ、Dubbo 的服务有对应环境默认值。
- [x] 远端公共与服务级 DataId 均使用 `optional:nacos:` 导入。
- [x] Maven Profile 能过滤活动环境、Nacos 地址、namespace、group 和固定凭证。
- [x] 后端全量 Maven 测试通过。
- [x] 形成配置中心使用与上线替换凭证说明。

## 5. 风险

- Spring Boot 4.1.0 与当前 Spring Cloud Alibaba 正式版本缺少官方兼容保证，本需求将 Spring Boot 对齐到官方支持的 4.0.0。
- `change-me`、`nacos/nacos` 仅为可见兜底值，不是可接受的生产密码。
- 命令行参数和系统环境变量按 Spring Boot 标准优先级高于 Nacos，作为部署与故障处理的应急覆盖通道。
