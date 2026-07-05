# 询价单聚合 CQRS 深度设计

> 所属上下文：采购领域。本文按 DDD + CQRS 深入到聚合属性、命令处理、应用服务编排、领域服务规则、事件产生和事件消费逻辑。关键时序图使用 Mermaid 最小兼容语法，便于 VSCode Markdown 预览稳定渲染。

## 1. 业务目标分析

组织采购寻源范围，向可供货供应商发出询价邀请，控制报价截止、截标、取消和关闭。

| 设计项 | 结论 |
| --- | --- |
| 限界上下文 | 采购系统上下文 |
| 子域类型 | 核心域，决定采购向哪些供应商询价 |
| 聚合根 | 询价单 |
| 数据主权 | 采购上下文拥有 `询价单` 的生命周期、状态、业务规则和领域事件；外部系统只能通过命令或事件协作 |
| 主要使用角色 | 采购员、采购经理、供应商系统、系统截标任务 |
| 核心不变量 | 外部只能通过聚合根修改内部实体；状态流转必须合法；写命令和消费事件必须幂等 |

## 2. 角色、场景与流程分析

| 场景 | 发起角色 | 应用服务处理逻辑 | 领域服务 | 结果事件 |
| --- | --- | --- | --- | --- |
| 创建询价单 | 采购员 | 从请购或手工需求创建草稿，确定询价行和候选供应商 | 询价范围校验服务 | 询价单已创建 |
| 发布询价单 | 采购员 | 校验邀请供应商可用、询价行完整、截止时间合法 | 供应商邀请校验服务 | 询价单已发布 |
| 修改询价单 | 采购员 | 未发布或允许变更窗口内修改非核心字段 | 询价范围校验服务 | 询价单已修改 |
| 截标询价单 | 系统任务/采购员 | 到达截止时间后停止接收报价 | 询价截标判定服务 | 询价已截标 |
| 取消询价单 | 采购员/采购经理 | 未定标前取消并通知供应商 | 询价取消判定服务 | 询价单已取消 |
| 关闭询价单 | 采购员 | 已定标或无需采购时关闭 | 询价关闭判定服务 | 询价单已关闭 |

## 3. 领域边界与分层架构

领域事件的位置要明确区分三层含义：领域层产生事件，应用层保存聚合与事件发布表，基础设施层投递消息并消费外部事件。

```mermaid
flowchart TB
    CommandAPI["CommandAPI 命令接口"]
    QueryAPI["QueryAPI 查询接口"]
    EventConsumer["EventConsumer 事件消费入口"]
    AppService["AppService 询价应用服务"]
    CommandHandler["CommandHandler 命令处理器"]
    EventHandler["EventHandler 事件处理器"]
    Aggregate["Aggregate 询价单"]
    DomainService["DomainService 领域服务"]
    DomainEvent["DomainEvent 领域事件"]
    Repository["Repository 资源库"]
    Outbox["Outbox 事件发布表"]
    Inbox["Inbox 事件收件箱"]
    ReadModel["ReadModel 读模型"]
    Broker["MessageBroker 消息中间件"]

    CommandAPI --> AppService
    QueryAPI --> ReadModel
    EventConsumer --> EventHandler
    AppService --> CommandHandler
    CommandHandler --> Repository
    Repository --> Aggregate
    CommandHandler --> DomainService
    Aggregate --> DomainEvent
    AppService --> Outbox
    EventHandler --> Inbox
    Outbox --> Broker
    Broker --> EventConsumer
    Outbox --> ReadModel
```

## 4. 聚合属性设计

| 属性 | 业务含义 | 模型归属 | 是否可变 | 主要修改命令 | 变化规则 |
| --- | --- | --- | --- | --- | --- |
| rfqId | 询价单ID | 聚合根 | 否 | 创建询价单 | 全局唯一 |
| rfqNo | 询价单号 | 值对象 | 否 | 创建询价单 | 按规则生成 |
| status | 询价状态 | 值对象 | 是 | 发布/截标/取消/关闭 | 草稿、已发布、报价中、已截标、已定标、已取消、已关闭 |
| rfqLineList | 询价行 | 内部实体集合 | 是 | 创建/修改 | SKU、数量、质量要求、交付要求 |
| invitedSupplierList | 邀请供应商 | 内部实体集合 | 是 | 创建/发布 | 供应商、邀请状态、报价状态 |
| quoteDeadline | 报价截止时间 | 值对象 | 是 | 创建/修改/发布 | 截止后不能接收普通报价 |
| sourceRequisitionRef | 请购来源 | 值对象 | 否 | 创建询价单 | 记录来源请购或采购计划 |

## 5. 命令与应用服务逻辑

应用服务负责编排用例：校验权限、检查幂等、加载聚合、调用领域服务、执行聚合行为、保存聚合、写发布表、写审计日志。

| 命令 | 发起者 | 应用服务处理逻辑 | 参与领域服务 | 成功后领域事件 |
| --- | --- | --- | --- | --- |
| 创建询价单 | 采购员 | 从请购或手工需求创建草稿，确定询价行和候选供应商 | 询价范围校验服务 | 询价单已创建 |
| 发布询价单 | 采购员 | 校验邀请供应商可用、询价行完整、截止时间合法 | 供应商邀请校验服务 | 询价单已发布 |
| 修改询价单 | 采购员 | 未发布或允许变更窗口内修改非核心字段 | 询价范围校验服务 | 询价单已修改 |
| 截标询价单 | 系统任务/采购员 | 到达截止时间后停止接收报价 | 询价截标判定服务 | 询价已截标 |
| 取消询价单 | 采购员/采购经理 | 未定标前取消并通知供应商 | 询价取消判定服务 | 询价单已取消 |
| 关闭询价单 | 采购员 | 已定标或无需采购时关闭 | 询价关闭判定服务 | 询价单已关闭 |

### 5.1 应用服务通用处理模板

1. 接口层接收请求并转换为命令对象。
2. 应用层校验用户、角色、组织、采购范围和数据权限。
3. 使用 `来源系统 + 业务单号 + 命令类型 + 幂等键` 做幂等检查。
4. 通过资源库加载 `询价单` 聚合根，新建场景先校验业务唯一性。
5. 调用领域服务完成跨实体、跨策略或外部事实快照的规则判断。
6. 聚合根执行行为，修改属性、内部实体和值对象，并产生领域事件。
7. 同一事务保存聚合、事件发布表和操作审计。
8. 事件发布器异步投递事件，读模型投影器更新查询模型。

### 5.2 关键命令处理细节

| 关键命令 | 前置校验 | 聚合行为 | 异常或补偿处理 |
| --- | --- | --- | --- |
| 创建询价单 | 询价单状态允许执行，来源数据和权限有效 | 修改询价单状态或明细并产生事件 询价单已创建 | 状态不匹配则拒绝；外部协作失败进入待办或补偿流程 |
| 发布询价单 | 询价单状态允许执行，来源数据和权限有效 | 修改询价单状态或明细并产生事件 询价单已发布 | 状态不匹配则拒绝；外部协作失败进入待办或补偿流程 |
| 修改询价单 | 询价单状态允许执行，来源数据和权限有效 | 修改询价单状态或明细并产生事件 询价单已修改 | 状态不匹配则拒绝；外部协作失败进入待办或补偿流程 |

## 6. 领域服务逻辑

| 领域服务 | 核心逻辑 |
| --- | --- |
| 询价范围校验服务 | 校验询价行SKU、数量、质量要求、交付要求是否明确，来源请购是否可用。 |
| 供应商邀请校验服务 | 判断供应商是否启用、是否具备供货关系、是否被冻结、是否满足品类资质。 |
| 询价截标判定服务 | 按截止时间、报价状态、人工延期规则判断是否截标。 |
| 询价取消判定服务 | 判断是否已有有效报价、是否已进入比价定标，决定取消影响范围。 |

## 7. 事件产生逻辑

| 领域事件 | 触发命令 | 关键载荷 | 主要消费者 |
| --- | --- | --- | --- |
| 询价单已创建 | 创建询价单 | rfqId、询价行、来源 | 询价列表 |
| 询价单已发布 | 发布询价单 | rfqId、邀请供应商、截止时间 | 供应商系统、报价待办 |
| 询价单已修改 | 修改询价单 | rfqId、变更字段 | 供应商系统、读模型 |
| 询价已截标 | 截标询价单 | rfqId、截标时间 | 比价结果、报价系统 |
| 询价单已取消 | 取消询价单 | rfqId、取消原因 | 供应商系统、读模型 |
| 询价单已关闭 | 关闭询价单 | rfqId、关闭原因 | 读模型、审计 |

### 7.1 事件生成规则

- 领域事件使用过去式命名，只表达已经发生的业务事实。
- 聚合根在业务行为成功后产生领域事件；应用服务负责收集、持久化和发布。
- 事件载荷必须包含事件编号、事件版本、发生时间、来源上下文、聚合ID、聚合版本、操作者和关键业务字段。
- 命令幂等命中时，返回原处理结果，不能重复产生事件。
- 外部事件消费必须先进入事件收件箱，再由应用服务加载聚合并执行本地业务行为。

## 8. 事件订阅与消费逻辑

| 订阅事件 | 处理应用服务 | 消费后数据变化 | 幂等键 |
| --- | --- | --- | --- |
| 采购申请已批准 | 请购事件消费服务 | 生成询价候选需求或草稿 | 采购上下文+事件编号+requisitionId |
| 供应商商品已启用 | 供应商事件消费服务 | 更新可邀请供应商快照 | 供应商上下文+事件编号+supplierSkuId |
| 供应商已冻结 | 供应商事件消费服务 | 未发布询价移除或标记供应商不可邀请 | 供应商上下文+事件编号+supplierId |
| 供应商报价已提交 | 报价事件消费服务 | 更新邀请供应商报价状态 | 供应商上下文+事件编号+quoteId |

## 9. 关键时序图

### 9.1 命令处理、聚合变更与事件发布

```mermaid
sequenceDiagram
    participant User
    participant CommandAPI
    participant AppService
    participant Repository
    participant DomainService
    participant Aggregate
    participant Outbox
    participant AuditLog
    participant MessageBroker

    User->>CommandAPI: 采购员提交命令 创建询价单
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载询价单聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 询价单已创建
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 询价单已创建
```

### 9.2 典型业务命令一

```mermaid
sequenceDiagram
    participant User
    participant CommandAPI
    participant AppService
    participant Repository
    participant DomainService
    participant Aggregate
    participant Outbox
    participant AuditLog
    participant MessageBroker

    User->>CommandAPI: 采购员提交命令 发布询价单
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载询价单聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 询价单已发布
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 询价单已发布
```

### 9.3 典型业务命令二

```mermaid
sequenceDiagram
    participant User
    participant CommandAPI
    participant AppService
    participant Repository
    participant DomainService
    participant Aggregate
    participant Outbox
    participant AuditLog
    participant MessageBroker

    User->>CommandAPI: 采购员提交命令 修改询价单
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载询价单聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 询价单已修改
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 询价单已修改
```

### 9.4 事件订阅、幂等消费与本地状态变化

```mermaid
sequenceDiagram
    participant MessageBroker
    participant EventConsumer
    participant Inbox
    participant AppService
    participant Repository
    participant Aggregate
    participant ReadModel

    MessageBroker->>EventConsumer: 投递外部事件 采购申请已批准
    EventConsumer->>Inbox: 检查事件是否已消费
    Inbox->>EventConsumer: 返回未消费
    EventConsumer->>AppService: 转换为事件消费命令
    AppService->>Repository: 加载相关聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>Aggregate: 应用外部事实
    Aggregate->>AppService: 生成询价候选需求或询价草稿
    AppService->>Repository: 保存聚合变化
    AppService->>ReadModel: 更新查询投影
    AppService->>Inbox: 标记事件消费成功
```

### 9.5 聚合状态推进时序

```mermaid
sequenceDiagram
    participant AppService
    participant Aggregate
    participant EventLog
    participant ReadModel

    AppService->>Aggregate: 执行命令 创建询价单
    Aggregate->>EventLog: 产生事件 询价单已创建
    EventLog->>ReadModel: 投影状态 草稿
    AppService->>Aggregate: 执行命令 发布询价单
    Aggregate->>EventLog: 产生事件 询价单已发布
    EventLog->>ReadModel: 投影状态 已发布
    AppService->>Aggregate: 执行命令 截标询价单
    Aggregate->>EventLog: 产生事件 询价已截标
    EventLog->>ReadModel: 投影状态 已截标
    AppService->>Aggregate: 执行命令 取消询价单
    Aggregate->>EventLog: 产生事件 询价单已取消
    EventLog->>ReadModel: 投影状态 已取消
    AppService->>Aggregate: 执行命令 关闭询价单
    Aggregate->>EventLog: 产生事件 询价单已关闭
    EventLog->>ReadModel: 投影状态 已关闭
```

## 10. 不变量、异常补偿、权限与审计

| 类型 | 规则 |
| --- | --- |
| 聚合不变量 | `询价单` 的状态只能通过聚合根行为推进，内部实体不能被外部直接修改 |
| 数量和金额不变量 | 数量、金额、税率、币种、交期、有效期必须通过值对象校验 |
| 幂等 | 命令和事件消费都必须有幂等键，重复请求不能重复产生业务事实 |
| 并发 | 聚合保存使用版本号乐观锁，冲突时重新加载聚合并返回可重试错误 |
| 补偿 | 发布失败走事件发布表重试，消费失败走收件箱重试，业务阻塞进入人工待办 |
| 权限 | 按角色、组织、采购范围、供应商范围、金额阈值控制命令可执行性 |
| 审计 | 所有写命令记录操作者、来源、请求摘要、前后状态、事件编号和失败原因 |

## 11. 读模型设计

读模型服务于查询和页面展示，不参与聚合不变量保护。写入决策必须回到应用服务、聚合根和领域服务。

| 读模型 | 使用场景 | 主要字段 |
| --- | --- | --- |
| 询价列表读模型 | 询价查询、分页、状态筛选 | 询价单号、状态、供应商数量、截止时间 |
| 供应商报价进度读模型 | 采购员查看报价进度 | 邀请供应商、是否已报价、报价时间 |
| 询价异常看板 | 处理供应商不足、过期未报价 | 异常类型、责任人、处理状态 |

## 12. 设计结论与待确认问题

### 12.1 设计结论

- `询价单` 是采购领域内独立保护业务规则和状态流转的聚合根。
- 命令处理属于应用层编排，核心规则属于聚合根和领域服务。
- 采购上下文不直接修改供应商、WMS、中央库存、BMS 的主权数据，只消费事实并保存采购侧快照。

### 12.2 待确认问题

| 问题 | 默认建议 |
| --- | --- |
| 是否多组织、多采购组织、多仓库 | 默认保留组织、采购组织、仓库、供应商数据范围 |
| 是否允许终态单据强制修改 | 默认不允许，需通过变更、关闭、作废或补偿单处理 |
| 是否需要事件溯源 | 当前阶段建议当前状态表 + 历史表 + 事件日志，不做全量事件溯源 |
