# 03-库存预占聚合CQRS设计

> 所属上下文：中央库存领域。本文按 DDD + CQRS 深入到聚合属性、命令处理、应用服务编排、领域服务规则、事件产生和事件消费逻辑。关键时序图使用 Mermaid 最小兼容语法，便于 VSCode Markdown 预览稳定渲染。

## 1. 业务目标分析

管理销售、调拨、退供等业务对库存的承诺锁定、释放、扣减、过期和关闭，防止超卖与重复扣减；对已生成运单的履约单据，记录 TMS 运输异常对预占履约风险的影响。

| 设计项 | 结论 |
| --- | --- |
| 限界上下文 | 中央库存上下文 |
| 子域类型 | 核心域，库存承诺与释放 |
| 聚合根 | 库存预占 |
| 数据主权 | 中央库存拥有 `库存预占` 的数量口径、库存账本状态、库存流水、预占冻结调整结果和领域事件；TMS 拥有运输轨迹事实，中央库存只保存预占相关运单引用和异常快照 |
| 主要使用角色 | OMS、调拨系统、采购退供、库存运营、WMS、TMS |
| 核心不变量 | 库存数量必须有来源；余额、预占、冻结、释放、扣减、调整和流水必须同事务或可补偿；命令和事件必须幂等 |

## 2. 角色、场景与流程分析

| 场景 | 发起角色 | 应用服务处理逻辑 | 领域服务 | 结果事件 |
| --- | --- | --- | --- | --- |
| 预占库存 | OMS | 围绕库存预占执行预占库存，校验库存维度、数量口径、来源单据、幂等键和权限 | 库存预占分配服务 | 库存已预占 |
| 释放预占 | OMS | 围绕库存预占执行释放预占，校验库存维度、数量口径、来源单据、幂等键和权限 | 库存预占分配服务 | 预占已释放 |
| 扣减预占 | OMS | 围绕库存预占执行扣减预占，校验库存维度、数量口径、来源单据、幂等键和权限 | 库存预占分配服务 | 预占已扣减 |
| 记录运输异常 | TMS | 消费运单拒收、丢失、破损或延误事件，更新预占履约风险和单据轨迹，不改变预占数量 | 预占履约风险归因服务 | 预占运输异常已记录 |
| 关闭预占 | OMS | 围绕库存预占执行关闭预占，校验库存维度、数量口径、来源单据、幂等键和权限 | 库存预占分配服务 | 预占已关闭 |
| 标记预占过期 | OMS | 围绕库存预占执行标记预占过期，校验库存维度、数量口径、来源单据、幂等键和权限 | 库存预占分配服务 | 预占已过期 |

## 3. 领域边界与分层架构

中央库存领域事件的位置要明确区分三层含义：领域层产生库存账本事实，应用层保存聚合与事件发布表，基础设施层投递消息并消费外部库存事实。

```mermaid
flowchart TB
    CommandAPI["CommandAPI 命令接口"]
    QueryAPI["QueryAPI 查询接口"]
    EventConsumer["EventConsumer 事件消费入口"]
    AppService["AppService 库存预占应用服务"]
    CommandHandler["CommandHandler 命令处理器"]
    EventHandler["EventHandler 事件处理器"]
    Aggregate["Aggregate 库存预占"]
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
| 库存预占Id | 库存预占ID | 聚合根 | 否 | 预占库存 | 全局唯一 |
| 库存预占No | 库存预占单号 | 值对象 | 否 | 预占库存 | 按中央库存编码规则生成 |
| inventoryDimension | 库存维度 | 值对象 | 否 | 预占库存 | 货主、仓库、SKU、批次、库存状态维度唯一 |
| status | 业务状态 | 值对象 | 是 | 状态推进命令 | 必须按状态机流转 |
| quantity | 数量对象 | 值对象 | 是 | 记账或调整命令 | 实物、可用、预占、冻结、调整数量不能无来源变化 |
| sourceRef | 来源引用 | 值对象 | 否 | 预占库存 | 来源系统、来源单、来源行、幂等键 |
| transportRef | 运输引用快照 | 值对象 | 是 | 记录运输异常 | 运单号、运输状态、签收/拒收时间、异常类型、异常原因；只用于预占履约风险和轨迹 |
| ledgerLineList | 库存流水 | 内部实体集合 | 是 | 所有记账命令 | 只追加，不物理修改历史流水 |
| operationLog | 操作记录 | 内部实体集合 | 是 | 所有写命令 | 记录操作者、原因、前后数量和事件编号 |

## 5. 命令与应用服务逻辑

应用服务负责编排用例：校验权限、检查幂等、加载聚合、调用领域服务、执行聚合行为、保存聚合和流水、写发布表、写审计日志。

| 命令 | 发起者 | 应用服务处理逻辑 | 参与领域服务 | 成功后领域事件 |
| --- | --- | --- | --- | --- |
| 预占库存 | OMS | 围绕库存预占执行预占库存，校验库存维度、数量口径、来源单据、幂等键和权限 | 库存预占分配服务 | 库存已预占 |
| 释放预占 | OMS | 围绕库存预占执行释放预占，校验库存维度、数量口径、来源单据、幂等键和权限 | 库存预占分配服务 | 预占已释放 |
| 扣减预占 | OMS | 围绕库存预占执行扣减预占，校验库存维度、数量口径、来源单据、幂等键和权限 | 库存预占分配服务 | 预占已扣减 |
| 记录运输异常 | TMS事件消费者 | 按运单号和来源单幂等记录拒收、丢失、破损或延误，生成异常待办；是否释放、补发或调整由 OMS/调拨/退供发起后续命令 | 预占履约风险归因服务 | 预占运输异常已记录 |
| 关闭预占 | OMS | 围绕库存预占执行关闭预占，校验库存维度、数量口径、来源单据、幂等键和权限 | 库存预占分配服务 | 预占已关闭 |
| 标记预占过期 | OMS | 围绕库存预占执行标记预占过期，校验库存维度、数量口径、来源单据、幂等键和权限 | 库存预占分配服务 | 预占已过期 |

### 5.1 应用服务通用处理模板

1. 接口层接收请求并转换为命令对象。
2. 应用层校验用户、角色、组织、货主、仓库、操作类型和数据权限。
3. 使用 `来源系统 + 来源单号 + 来源行号 + 命令类型 + 幂等键` 做幂等检查。
4. 通过资源库加载 `库存预占` 聚合根，新建场景先校验库存维度和业务唯一性。
5. 调用领域服务完成数量、可用、冻结、预占、审批、来源事实的规则判断。
6. 聚合根执行行为，修改数量对象、内部实体和值对象，并产生领域事件。
7. 同一事务保存聚合、库存流水、事件发布表和操作审计。
8. 事件发布器异步投递事件，读模型投影器更新库存查询模型。

### 5.2 关键命令处理细节

| 关键命令 | 前置校验 | 聚合行为 | 异常或补偿处理 |
| --- | --- | --- | --- |
| 预占库存 | 库存预占状态允许执行，库存维度、数量、来源单据、幂等键和权限有效 | 修改库存预占数量或状态并追加流水，产生事件 库存已预占 | 状态不匹配则拒绝；数量不足则失败；重复事件按幂等返回原结果 |
| 释放预占 | 库存预占状态允许执行，库存维度、数量、来源单据、幂等键和权限有效 | 修改库存预占数量或状态并追加流水，产生事件 预占已释放 | 状态不匹配则拒绝；数量不足则失败；重复事件按幂等返回原结果 |
| 扣减预占 | 库存预占状态允许执行，WMS 已发货或中央库存确认扣减事实有效，库存维度、数量、来源单据、幂等键和权限有效 | 修改库存预占数量或状态并追加流水，产生事件 预占已扣减 | TMS 签收不能替代 WMS 发货扣减；重复事件按幂等返回原结果 |
| 记录运输异常 | TMS 事件可匹配来源单、预占单或运单号 | 更新运输引用快照和异常待办，产生事件 预占运输异常已记录 | 无法匹配则进入待人工关联；不能直接释放或扣减预占 |

## 6. 领域服务逻辑

| 领域服务 | 核心逻辑 |
| --- | --- |
| 库存预占分配服务 | 围绕库存预占的库存维度、数量不变量、可用口径、来源幂等和审批权限进行业务判定。 |
| 可用库存计算服务 | 围绕库存预占的库存维度、数量不变量、可用口径、来源幂等和审批权限进行业务判定。 |
| 预占关闭判定服务 | 围绕库存预占的库存维度、数量不变量、可用口径、来源幂等和审批权限进行业务判定。 |
| 预占履约风险归因服务 | 判断 TMS 拒收、丢失、破损或延误是否影响销售、调拨、退供预占的履约风险，只生成异常待办和读模型提示。 |

## 7. 事件产生逻辑

| 领域事件 | 触发命令 | 关键载荷 | 主要消费者 |
| --- | --- | --- | --- |
| 库存已预占 | 预占库存 | 库存预占ID、库存维度、来源单、变化数量、变化前后数量、流水批次 | OMS、WMS、采购、调拨、BMS、读模型、审计日志 |
| 预占已释放 | 释放预占 | 库存预占ID、库存维度、来源单、变化数量、变化前后数量、流水批次 | OMS、WMS、采购、调拨、BMS、读模型、审计日志 |
| 预占已扣减 | 扣减预占 | 库存预占ID、库存维度、来源单、变化数量、变化前后数量、流水批次 | OMS、WMS、采购、调拨、BMS、读模型、审计日志 |
| 预占运输异常已记录 | 记录运输异常 | 预占单ID、来源单、运单号、运输状态、异常类型、异常原因、影响数量 | OMS、调拨、采购退供、读模型、审计日志 |
| 预占已关闭 | 关闭预占 | 库存预占ID、库存维度、来源单、变化数量、变化前后数量、流水批次 | OMS、WMS、采购、调拨、BMS、读模型、审计日志 |
| 预占已过期 | 标记预占过期 | 库存预占ID、库存维度、来源单、变化数量、变化前后数量、流水批次 | OMS、WMS、采购、调拨、BMS、读模型、审计日志 |

### 7.1 事件生成规则

- 领域事件使用过去式命名，只表达已经发生的库存账本事实。
- 聚合根在业务行为成功后产生领域事件；应用服务负责收集、持久化和发布。
- 事件载荷必须包含事件编号、事件版本、发生时间、来源上下文、货主、仓库、SKU、批次、库存状态、聚合ID、聚合版本、数量变化和幂等键。
- 命令幂等命中时，返回原处理结果，不能重复改变余额、预占、冻结或流水。
- 外部事件消费必须先进入事件收件箱，再由应用服务加载聚合并执行本地记账行为。

## 8. 事件订阅与消费逻辑

| 订阅事件 | 处理应用服务 | 消费后数据变化 | 幂等键 |
| --- | --- | --- | --- |
| 销售订单已审核 | 外部事件消费服务 | 创建库存预占并锁定可用数量 | 来源上下文+事件编号+业务主键 |
| TMS运输已拒收 | 预占履约风险事件处理器 | 更新预占履约风险、单据轨迹和异常待办，不释放预占 | TMS上下文+事件编号+waybillNo |
| TMS物流异常已登记 | 预占履约风险事件处理器 | 记录丢失、破损或延误异常，等待 OMS/调拨/退供发起补发、释放、调整或索赔 | TMS上下文+事件编号+exceptionNo |
| SKU已启用 | 主数据事件消费服务 | 初始化或刷新SKU库存维度 | 主数据上下文+事件编号+skuId |
| 仓库已启用 | 主数据事件消费服务 | 初始化或刷新仓库库存维度 | 主数据上下文+事件编号+warehouseId |
| 审批已通过 | 审批事件消费服务 | 推进冻结、调整或对账处理 | 审批上下文+事件编号+approvalId |

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

    User->>CommandAPI: OMS提交命令 预占库存
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载库存预占聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行数量和幂等校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 库存已预占
    AppService->>Repository: 保存聚合和流水
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 库存已预占
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

    User->>CommandAPI: OMS提交命令 释放预占
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载库存预占聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行数量和幂等校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 预占已释放
    AppService->>Repository: 保存聚合和流水
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 预占已释放
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

    User->>CommandAPI: OMS提交命令 扣减预占
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载库存预占聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行数量和幂等校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 预占已扣减
    AppService->>Repository: 保存聚合和流水
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 预占已扣减
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

    MessageBroker->>EventConsumer: 投递外部事件 销售订单已审核
    EventConsumer->>Inbox: 检查事件是否已消费
    Inbox->>EventConsumer: 返回未消费
    EventConsumer->>AppService: 转换为事件消费命令
    AppService->>Repository: 加载相关聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>Aggregate: 应用外部库存事实
    Aggregate->>AppService: 创建库存预占并锁定可用数量
    AppService->>Repository: 保存聚合和流水
    AppService->>ReadModel: 更新库存查询投影
    AppService->>Inbox: 标记事件消费成功
```

### 9.5 聚合状态推进时序

```mermaid
sequenceDiagram
    participant AppService
    participant Aggregate
    participant EventLog
    participant ReadModel

    AppService->>Aggregate: 执行命令 预占库存
    Aggregate->>EventLog: 产生事件 库存已预占
    EventLog->>ReadModel: 投影状态 已预占
    AppService->>Aggregate: 执行命令 释放预占
    Aggregate->>EventLog: 产生事件 预占已释放
    EventLog->>ReadModel: 投影状态 已释放
    AppService->>Aggregate: 执行命令 扣减预占
    Aggregate->>EventLog: 产生事件 预占已扣减
    EventLog->>ReadModel: 投影状态 已扣减
    AppService->>Aggregate: 执行命令 关闭预占
    Aggregate->>EventLog: 产生事件 预占已关闭
    EventLog->>ReadModel: 投影状态 已关闭
    AppService->>Aggregate: 执行命令 标记预占过期
    Aggregate->>EventLog: 产生事件 预占已过期
    EventLog->>ReadModel: 投影状态 已过期
```

## 10. 不变量、异常补偿、权限与审计

| 类型 | 规则 |
| --- | --- |
| 聚合不变量 | `库存预占` 的数量、状态和内部实体只能通过聚合根行为推进 |
| 数量不变量 | 实物、可用、预占、冻结、释放、扣减、调整不能出现无来源变化；可用不能被预占到负数 |
| 流水不变量 | 库存流水只追加，错误通过红冲、反向流水或调整单处理，不能物理修改历史 |
| TMS边界 | TMS 签收、拒收、丢失、破损和延误不能直接扣减、释放或关闭预占；必须由 OMS、调拨或退供业务命令触发后续处理 |
| 幂等 | 命令和事件消费都必须有幂等键，重复请求不能重复改变库存数量 |
| 并发 | 库存账户和预占扣减必须使用版本号或行级锁，防止并发超卖、重复扣减、重复入库 |
| 补偿 | 发布失败走事件发布表重试，消费失败走收件箱重试，业务差异进入对账或调整流程 |
| 权限 | 按角色、组织、货主、仓库、操作类型、金额或数量阈值控制命令可执行性 |
| 审计 | 所有写命令记录操作者、来源、幂等键、前后数量、流水批次、事件编号和失败原因 |

## 11. 读模型设计

读模型服务于查询、库存可用、单据轨迹、快照和对账，不参与聚合不变量保护。写入决策必须回到应用服务、聚合根和领域服务。

| 读模型 | 使用场景 | 主要字段 |
| --- | --- | --- |
| 库存预占列表读模型 | 查询、分页、筛选 | 单号、库存维度、状态、数量、来源单、更新时间 |
| 库存预占详情读模型 | 详情页展示 | 单头、明细、流水、事件历史、操作日志 |
| 库存余额读模型 | 库存余额和可用查询 | 货主、仓库、SKU、批次、实物、可用、预占、冻结 |
| 单据库存轨迹读模型 | 按来源单追溯库存变化 | 命令、事件、流水、预占、释放、扣减、调整、运单号、运输状态、异常原因 |

## 12. 设计结论与待确认问题

### 12.1 设计结论

- `库存预占` 是中央库存领域内独立保护库存数量规则和状态流转的聚合根。
- 中央库存拥有统一库存数量账本和流水；WMS 拥有仓内实物事实；TMS 拥有运输轨迹事实；OMS、采购、调拨拥有业务意图。
- 库存预占的扣减以 WMS 发货或中央库存确认的扣减事实为准，不能以 TMS 签收作为直接扣减依据。
- 命令处理属于应用层编排，核心数量规则属于聚合根和领域服务。

### 12.2 待确认问题

| 问题 | 默认建议 |
| --- | --- |
| 是否多货主、多仓、多批次、多库存状态 | 默认保留货主、仓库、SKU、批次、库存状态维度 |
| 是否允许负库存 | 默认不允许，特殊场景必须配置白名单并强审计 |
| 是否需要事件溯源 | 当前阶段建议当前状态表 + 库存流水表 + 事件日志，不做全量事件溯源 |
