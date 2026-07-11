# 供应链系统工程

本目录是 `docs/` 设计文档的代码实现。当前阶段先建立可持续扩展的工程基线，并完成供应商系统 ASN 聚合的首个纵向切片。

## 工程结构

```text
project/
  backend/
    scm-common/          通用响应、分页和异常协议
    supplier-service/    供应商限界上下文
  frontend/              React 管理端应用壳
```

后端按接口层、应用层、领域层、基础设施层组织。写请求使用聚合保护业务规则，业务表、事件发布表和审计数据在同一本地事务中提交；查询接口直接读取查询模型。

## 技术基线

- JDK 21 LTS
- Spring Boot 4.1
- MyBatis、MySQL 8、Flyway
- Spring Security OAuth2 Resource Server、Redis
- JavaScript、React 19、Vite

后端统一使用 JDK 21。MySQL、Redis 和消息队列暂不由本项目启动，但代码和配置按它们已经存在编写。

## 已实现切片

供应商 ASN 已实现：

- 创建 ASN 草稿
- 提交 ASN
- 取消 ASN
- 确认发货
- ASN 详情查询
- 乐观锁、数据权限、幂等、领域事件发布表
- ASN 状态机领域测试

供应商档案已实现：

- 主数据供应商档案本地快照查询
- 资料变更申请、分页查询和撤回
- 禁止通过资料变更修改供应商 ID、编码和生命周期状态

供应商商品已实现：

- 建立供货关系、修改 MOQ/MPQ/交期/采购单位/有效期
- 暂停和恢复供货
- 列表、详情、供应商数据范围和乐观锁
- 消费主数据 SKU 与供应商状态事件，维护本地可用快照；新增或恢复供货关系时校验快照状态

供应商报价已实现：

- 报价草稿、提交、采购确认、采纳和拒绝状态机
- 报价行价格、税率、数量、有效期和 SKU 唯一性校验
- 采纳后发布 `SupplierQuoteAdopted`，为采购价格协议和比价消费提供事实

供应商合同已实现：

- 合同草稿、提交审批、生效、续签与终止状态机
- 绑定供应商、报价及价格协议引用，生效后发布合同约束事实给采购和结算系统

采购订单确认已实现：

- 接收采购系统发布的采购订单
- 整单确认、拒绝、反馈差异、修改承诺交期
- 确认数量与订单行完整性校验

可靠事件投递已实现：

- 业务数据和领域事件同事务写入 Outbox
- 定时批量抢占、`SKIP LOCKED` 并发隔离
- RocketMQ 5 发布、失败重试、最大重试次数和卡死任务恢复
- 设置 `ROCKETMQ_ENABLED=true` 与 `ROCKETMQ_ENDPOINTS` 后启用投递任务
- 设置 `ROCKETMQ_MASTER_DATA_CONSUMER_ENABLED=true` 后，订阅主数据主题并以收件箱保证幂等消费

Flyway 迁移位于 `supplier-service/src/main/resources/db/migration`。它补齐了原始设计 DDL 中 ASN 聚合关系所需的外键字段。

## 验证命令

```bash
cd backend
mvn -s maven-settings.xml test

cd ../frontend
npm install
npm run build
```

如果 macOS 的 Maven 使用 ASCII 默认字符集且项目路径包含中文，可临时使用 `-DforkCount=0` 运行测试。
