# 项目级记忆

更新时间：2026-07-11

## 1. 项目模块划分

| 路径 | 职责 | 当前状态 |
| --- | --- | --- |
| `docs/` | 供应链业务、DDD、功能、数据库、接口、事件、实现和开发计划的权威设计资料 | 九个子系统设计已齐备 |
| `project/backend/scm-common` | 后端共享契约与公共对象；跨系统 Dubbo 协作接口位于 `com.chaobo.scm.common.integration` | 已存在 |
| `project/backend/supplier-service` | 当前唯一已落地的后端业务服务；供应商协同及部分采购履约、ASN、质量、退供、财务、评分、运营能力 | 已实现部分功能 |
| `project/frontend` | React/Vite 前端；当前以 ASN 页面和 API 客户端为主 | 已存在基础实现 |
| `prototype/` | 供应商、采购、WMS、中央库存、OMS、主数据、权限系统的静态原型 | 用于页面/字段参考，不是生产前端 |
| `skills/` | 项目专用技能，包括供应链设计、DDD 重构与开发执行 Skill | 开发前应读取相关 Skill |
| `docs/09-开发计划/` | 代码实施顺序、接口/类实施包、开发日志 | 后续开发的执行基线 |

后端根模块 `project/backend` 目前只有两个 Maven 子模块：`scm-common`、`supplier-service`。采购、WMS、中央库存、OMS、TMS、BMS、主数据、权限尚未建立独立后端 Maven 服务，先以 `docs/09-开发计划/01-开发计划.md` 的实施包创建。

## 2. 运行时与构建版本

| 项目 | 权威版本/命令 |
| --- | --- |
| Java/JDK | JDK 21 LTS；`pom.xml` 使用 `java.version=21`、`maven.compiler.release=21` |
| Maven | Maven 多模块工程；编译插件 `maven-compiler-plugin 3.14.1` |
| Spring Boot | `4.1.0` |
| MyBatis Spring Boot | `4.0.0` |
| Dubbo | `3.3.6` |
| RocketMQ Java Client | `5.0.8` |
| 后端测试 | 在 `project/backend` 运行：`JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home PATH=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home/bin:$PATH mvn -o -s maven-settings.xml -Dmaven.repo.local=.m2 -DforkCount=0 test -q` |

注意：`docs/08-系统实现/01-技术选型与系统架构总览.md` 中有 JDK 25 的设计阶段备选方案；实际工程和 `project/README.md` 明确使用 JDK 21。编码和验证以 Maven 根 `pom.xml` 为准。

## 3. 主要业务模块

供应链目标系统包含九个限界上下文：供应商、采购、WMS、中央库存、OMS、TMS、BMS、主数据、权限。完整功能、表、接口、事件和实现设计分别位于 `docs/03` 至 `docs/08`。

当前 `supplier-service` 的包结构遵循 DDD 四层：

- `interfaces.web`：REST/OpenAPI 入口。
- `application`：`account`、`asn`、`contract`、`finance`、`integration`、`item`、`masterdata`、`operations`、`order`、`outbox`、`profile`、`qualification`、`quality`、`quote`、`returning`、`rfq`、`score`、`shared`。
- `domain`：`asn`、`contract`、`item`、`order`、`profile`、`qualification`、`quality`、`quote`、`returning`、`shared`。
- `infrastructure`：`integration`、`mq`、`persistence`、`security`。

供应商服务关键技术事实：MyBatis + Flyway + MySQL、Redis、RocketMQ、Dubbo；写操作使用 `TransactionalCommandExecutor`、`CommandContext`、审计、Outbox；入站事件使用 Inbox、原始载荷保存和重放服务。

## 4. 流程审批相关代码位置

| 审批/审核流程 | 聚合/状态机 | 应用服务 | 接口或消息入口 |
| --- | --- | --- | --- |
| 供应商准入 | `domain/profile/SupplierAdmissionAggregate`，状态 `POTENTIAL -> PENDING -> APPROVED/REJECTED/WITHDRAWN` | `application/profile/SupplierAdmissionApplicationService` | `interfaces/web/SupplierAdmissionController` |
| 供应商资质审核 | `domain/qualification/SupplierQualificationAggregate` | `application/qualification/SupplierQualificationApplicationService` | `interfaces/web/SupplierQualificationController` |
| 合同审批 | `domain/contract/SupplierContractAggregate`，状态 `DRAFT -> APPROVING -> ACTIVE`，可驳回/终止/到期 | `application/contract/SupplierContractApplicationService`、`ContractApprovalEventConsumerApplicationService` | `ContractApprovalOpenApiController`、`ContractApprovalEventOpenApiController`、`infrastructure/mq/RocketMqContractApprovalConsumer` |
| 资料变更审批 | `domain/profile/ProfileChangeAggregate`，状态 `PENDING/APPROVED/REJECTED/WITHDRAWN` | `application/profile/ProfileApplicationService` | 对应档案变更 REST 接口 |
| 退供应商审核 | `domain/returning/SupplierReturnAggregate`，`PENDING_REVIEW -> APPROVED` 或退回草稿 | `application/returning/SupplierReturnApplicationService` | `interfaces/web/SupplierReturnController` |
| 质量整改验证 | `domain/quality/SupplierQualityIssueAggregate`，整改方案提交后进入 `PENDING_VERIFICATION` | `application/quality/SupplierQualityIssueApplicationService` | `interfaces/web/SupplierQualityIssueController` |
| 未来统一审批平台 | 设计在 `docs/03-核心业务模型/09-权限系统领域模型/08-审批实例聚合CQRS设计.md`；当前未独立实现权限系统服务 | 后续由 IAM 服务实现审批实例、任务和回调 |

审批回调原则：来源系统校验、事件编码幂等、聚合版本校验、Inbox 失败落库、人工重放、审计日志；不允许回调直接更新数据库绕过聚合。

## 5. 用户编码偏好与强制约定

- 默认中文沟通与文档；重要结论优先写 Markdown，不只在对话中说明。
- 严格 DDD：接口层仅协议转换；应用层处理事务、权限、幂等、审计和编排；领域层放状态机/不变量；基础设施层实现 Mapper、MQ、RPC、缓存。
- 新功能必须先读取 `docs/09-开发计划/01-开发计划.md` 的任务编号和接口/类实施包；完成后写 `docs/09-开发计划/02-开发日志.md`。
- Java 必须多行、清晰格式化；禁止新增压缩成一行的类或方法。注释只解释业务原因、边界、并发、补偿或外部兼容性。
- 写接口必须具备权限、数据范围、`X-Idempotency-Key`、乐观锁/版本、审计；查询接口必须具备分页总数、排序白名单和数据范围。
- 跨系统同步命令使用 Dubbo/HTTP ACL；已发生事实使用领域事件；出站 Outbox、入站 Inbox，支持失败重试、死信和人工重放。
- 不修改用户已有改动；使用 `apply_patch` 修改文件；避免破坏性 Git 操作。
- 设计问题先按“业务目标、角色流程、领域边界、聚合不变量、命令事件读模型、异常补偿权限审计”分析；信息不足时采用明确假设继续，不反复追问。
