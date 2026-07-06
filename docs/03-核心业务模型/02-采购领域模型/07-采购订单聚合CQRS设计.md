# 07-采购订单聚合CQRS设计

> 所属上下文：采购领域。本文按 DDD + CQRS 深入到聚合属性、命令处理、应用服务编排、领域服务规则、事件产生和事件消费逻辑。关键时序图使用 Mermaid 最小兼容语法，便于 VSCode Markdown 预览稳定渲染。

## 1. 业务目标分析

形成对供应商的正式采购承诺，并跟踪审批、发布、供应商确认、TMS 运输到仓、WMS 收货、质检、上架、完成、取消和关闭。

| 设计项 | 结论 |
| --- | --- |
| 限界上下文 | 02-采购系统上下文 |
| 子域类型 | 核心域，承载采购义务 |
| 聚合根 | 采购订单 |
| 数据主权 | 采购上下文拥有 `采购订单` 的生命周期、状态、业务规则和领域事件；TMS拥有运输任务、运单、轨迹、到仓、异常和物流费用来源；外部系统只能通过命令或事件协作 |
| 主要使用角色 | 采购员、采购经理、供应商系统、TMS、WMS、库存系统 |
| 核心不变量 | 外部只能通过聚合根修改内部实体；状态流转必须合法；写命令和消费事件必须幂等 |

## 2. 角色、场景与流程分析

| 场景 | 发起角色 | 应用服务处理逻辑 | 领域服务 | 结果事件 |
| --- | --- | --- | --- | --- |
| 创建采购订单 | 采购员 | 围绕采购订单执行创建采购订单，校验状态、来源和业务规则 | 采购订单生成服务 | 采购订单已创建 |
| 提交采购订单 | 采购员 | 围绕采购订单执行提交采购订单，校验状态、来源和业务规则 | 采购订单生成服务 | 采购订单已提交 |
| 审批采购订单 | 采购员 | 围绕采购订单执行审批采购订单，校验状态、来源和业务规则 | 采购订单生成服务 | 采购订单已批准 |
| 发布采购订单 | 采购员 | 围绕采购订单执行发布采购订单，校验状态、来源和业务规则 | 采购订单生成服务 | 采购订单已发布 |
| 记录运输事实 | TMS事件消费者 | 消费运单创建、运输到达和物流异常，刷新采购运输快照和异常待办 | 采购执行进度判定服务 | 采购订单运输信息已记录 |
| 记录入库事实 | 采购员 | 围绕采购订单执行记录入库事实，校验状态、来源和业务规则 | 采购订单生成服务 | 采购订单已部分入库 |
| 关闭采购订单 | 采购员 | 围绕采购订单执行关闭采购订单，校验状态、来源和业务规则 | 采购订单生成服务 | 采购订单已关闭 |

## 3. 领域边界与分层架构

领域事件的位置要明确区分三层含义：领域层产生事件，应用层保存聚合与事件发布表，基础设施层投递消息并消费外部事件。

```mermaid
flowchart TB
    CommandAPI["CommandAPI 命令接口"]
    QueryAPI["QueryAPI 查询接口"]
    EventConsumer["EventConsumer 事件消费入口"]
    AppService["AppService 采购订单应用服务"]
    CommandHandler["CommandHandler 命令处理器"]
    EventHandler["EventHandler 事件处理器"]
    Aggregate["Aggregate 采购订单"]
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
| 采购订单Id | 采购订单ID | 聚合根 | 否 | 创建采购订单 | 全局唯一 |
| 采购订单No | 采购订单单号 | 值对象 | 否 | 创建采购订单 | 按编码规则生成 |
| status | 业务状态 | 值对象 | 是 | 状态推进命令 | 必须按状态机流转 |
| lineList | 明细行 | 内部实体集合 | 是 | 创建或变更命令 | 记录SKU、数量、价格、交期等明细 |
| sourceRef | 来源引用 | 值对象 | 否 | 创建采购订单 | 记录请购、询价、报价、PO或外部事件来源 |
| snapshot | 业务快照 | 值对象 | 是 | 创建或事件消费 | 保存供应商、SKU、仓库、价格等外部事实快照 |
| transportSnapshot | 运输快照 | 值对象 | 是 | 记录运输事实 | TMS运输任务号、运单号、承运商、运输状态、预计到仓、到仓时间、异常类型、最新轨迹摘要 |
| freightCostRef | 物流费用来源引用 | 值对象 | 是 | 物流费用来源事件消费 | TMS费用来源单号、预估费用、实际费用、币种、计费状态 |
| operationLog | 操作记录 | 内部实体集合 | 是 | 所有写命令 | 记录操作者、动作、原因、前后状态 |

## 5. 命令与应用服务逻辑

应用服务负责编排用例：校验权限、检查幂等、加载聚合、调用领域服务、执行聚合行为、保存聚合、写发布表、写审计日志。

| 命令 | 发起者 | 应用服务处理逻辑 | 参与领域服务 | 成功后领域事件 |
| --- | --- | --- | --- | --- |
| 创建采购订单 | 采购员 | 围绕采购订单执行创建采购订单，校验状态、来源和业务规则 | 采购订单生成服务 | 采购订单已创建 |
| 提交采购订单 | 采购员 | 围绕采购订单执行提交采购订单，校验状态、来源和业务规则 | 采购订单生成服务 | 采购订单已提交 |
| 审批采购订单 | 采购员 | 围绕采购订单执行审批采购订单，校验状态、来源和业务规则 | 采购订单生成服务 | 采购订单已批准 |
| 发布采购订单 | 采购员 | 围绕采购订单执行发布采购订单，校验状态、来源和业务规则 | 采购订单生成服务 | 采购订单已发布 |
| 记录运输事实 | TMS事件消费者 | 记录运单、预计到仓、到仓、延误、破损、丢失等事实，只刷新运输快照和异常待办 | 采购执行进度判定服务 | 采购订单运输信息已记录 |
| 记录入库事实 | 采购员 | 围绕采购订单执行记录入库事实，校验状态、来源和业务规则 | 采购订单生成服务 | 采购订单已部分入库 |
| 关闭采购订单 | 采购员 | 围绕采购订单执行关闭采购订单，校验状态、来源和业务规则 | 采购订单生成服务 | 采购订单已关闭 |

### 5.1 应用服务通用处理模板

1. 接口层接收请求并转换为命令对象。
2. 应用层校验用户、角色、组织、采购范围和数据权限。
3. 使用 `来源系统 + 业务单号 + 命令类型 + 幂等键` 做幂等检查。
4. 通过资源库加载 `采购订单` 聚合根，新建场景先校验业务唯一性。
5. 调用领域服务完成跨实体、跨策略或外部事实快照的规则判断。
6. 聚合根执行行为，修改属性、内部实体和值对象，并产生领域事件。
7. 同一事务保存聚合、事件发布表和操作审计。
8. 事件发布器异步投递事件，读模型投影器更新查询模型。

### 5.2 关键命令处理细节

| 关键命令 | 前置校验 | 聚合行为 | 异常或补偿处理 |
| --- | --- | --- | --- |
| 创建采购订单 | 采购订单状态允许执行，来源数据和权限有效 | 修改采购订单状态或明细并产生事件 采购订单已创建 | 状态不匹配则拒绝；外部协作失败进入待办或补偿流程 |
| 提交采购订单 | 采购订单状态允许执行，来源数据和权限有效 | 修改采购订单状态或明细并产生事件 采购订单已提交 | 状态不匹配则拒绝；外部协作失败进入待办或补偿流程 |
| 审批采购订单 | 采购订单状态允许执行，来源数据和权限有效 | 修改采购订单状态或明细并产生事件 采购订单已批准 | 状态不匹配则拒绝；外部协作失败进入待办或补偿流程 |
| 发布采购订单 | PO已批准；供应商、目的仓、收货地址和运输要求完整 | 状态进入待确认；发布给01-供应商系统，必要时给 TMS 形成采购运输预期 | TMS 创建运输任务失败不回滚 PO 发布，只生成运输异常待办 |
| 记录运输事实 | 来源为 TMS；事件未消费；PO、ASN、运单与供应商/仓库匹配 | 刷新运输快照、预计到仓、到仓时间或异常待办；不累计收货/入库数量 | 乱序事件按发生时间和事件版本保护；到仓后长时间未收货生成采购/WMS待办 |
| 记录入库事实 | 来源为 WMS/库存；事件未消费；数量不超过容差 | 累计收货、合格、上架和入库数量，按行判断部分入库或完成 | TMS 到仓不能替代 WMS 收货；数量超容差进入异常待办 |

## 6. 领域服务逻辑

| 领域服务 | 核心逻辑 |
| --- | --- |
| 采购订单生成服务 | 围绕采购订单的状态、不变量、外部事实快照和策略配置进行业务判定。 |
| 采购订单发布校验服务 | 围绕采购订单的状态、不变量、外部事实快照和策略配置进行业务判定。 |
| 采购执行进度判定服务 | 汇总供应商确认、TMS运输、WMS收货质检上架、库存入库事实，判断 PO 行和单头进度；TMS 到仓只表示货物到达园区/仓库，不等于已收货。 |

## 7. 事件产生逻辑

| 领域事件 | 触发命令 | 关键载荷 | 主要消费者 |
| --- | --- | --- | --- |
| 采购订单已创建 | 创建采购订单 | 采购订单ID、业务状态、关键明细摘要 | 本领域读模型、上下游协作系统、审计日志 |
| 采购订单已提交 | 提交采购订单 | 采购订单ID、业务状态、关键明细摘要 | 本领域读模型、上下游协作系统、审计日志 |
| 采购订单已批准 | 审批采购订单 | 采购订单ID、业务状态、关键明细摘要 | 本领域读模型、上下游协作系统、审计日志 |
| 采购订单已发布 | 发布采购订单 | 采购订单ID、供应商、订单行、目的仓、收货地址、运输要求 | 本领域读模型、供应商系统、TMS、审计日志 |
| 采购订单运输信息已记录 | 记录运输事实 | 采购订单ID、ASN、TMS运输任务号、运单号、承运商、运输状态、预计到仓、到仓时间、异常类型 | 本领域读模型、采购异常看板、供应商履约分析 |
| 采购订单已部分入库 | 记录入库事实 | 采购订单ID、业务状态、关键明细摘要 | 本领域读模型、上下游协作系统、审计日志 |
| 采购订单已关闭 | 关闭采购订单 | 采购订单ID、业务状态、关键明细摘要 | 本领域读模型、上下游协作系统、审计日志 |

### 7.1 事件生成规则

- 领域事件使用过去式命名，只表达已经发生的业务事实。
- 聚合根在业务行为成功后产生领域事件；应用服务负责收集、持久化和发布。
- 事件载荷必须包含事件编号、事件版本、发生时间、来源上下文、聚合ID、聚合版本、操作者和关键业务字段。
- 命令幂等命中时，返回原处理结果，不能重复产生事件。
- 外部事件消费必须先进入事件收件箱，再由应用服务加载聚合并执行本地业务行为。

## 8. 事件订阅与消费逻辑

| 订阅事件 | 处理应用服务 | 消费后数据变化 | 幂等键 |
| --- | --- | --- | --- |
| 供应商订单已确认 | 外部事件消费服务 | 记录供应商确认结果并推进待到货 | 来源上下文+事件编号+业务主键 |
| 运单已创建 | TMS事件消费服务 | 记录 TMS 运输任务、运单、承运商和预计到仓，刷新运输快照 | TMS上下文+事件编号+waybillNo |
| 运输已到达 | TMS事件消费服务 | 记录到仓时间，标记到仓待收货，不改变收货和入库数量 | TMS上下文+事件编号+waybillNo |
| 物流异常已登记 | TMS事件消费服务 | 记录延误、破损、丢失等运输异常并生成采购异常待办 | TMS上下文+事件编号+exceptionNo |
| 物流费用来源已生成 | TMS事件消费服务 | 记录物流费用来源引用，用于采购成本分析和BMS对账辅助 | TMS上下文+事件编号+freightCostSourceNo |
| 供应商已冻结 | 供应商事件消费服务 | 标记供应商不可用并生成业务异常待办 | 供应商上下文+事件编号+supplierId |
| SKU已停用 | 主数据事件消费服务 | 标记相关明细不可继续执行 | 主数据上下文+事件编号+skuId |

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

    User->>CommandAPI: 采购员提交命令 创建采购订单
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载采购订单聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 采购订单已创建
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 采购订单已创建
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

    User->>CommandAPI: 采购员提交命令 提交采购订单
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载采购订单聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 采购订单已提交
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 采购订单已提交
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

    User->>CommandAPI: 采购员提交命令 审批采购订单
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载采购订单聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 采购订单已批准
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 采购订单已批准
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

    MessageBroker->>EventConsumer: 投递外部事件 运输已到达
    EventConsumer->>Inbox: 检查事件是否已消费
    Inbox->>EventConsumer: 返回未消费
    EventConsumer->>AppService: 转换为事件消费命令
    AppService->>Repository: 加载相关聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>Aggregate: 应用TMS运输事实
    Aggregate->>AppService: 记录到仓时间并标记待收货
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

    AppService->>Aggregate: 执行命令 创建采购订单
    Aggregate->>EventLog: 产生事件 采购订单已创建
    EventLog->>ReadModel: 投影状态 草稿
    AppService->>Aggregate: 执行命令 提交采购订单
    Aggregate->>EventLog: 产生事件 采购订单已提交
    EventLog->>ReadModel: 投影状态 待审批
    AppService->>Aggregate: 执行命令 审批采购订单
    Aggregate->>EventLog: 产生事件 采购订单已批准
    EventLog->>ReadModel: 投影状态 已批准
    AppService->>Aggregate: 执行命令 发布采购订单
    Aggregate->>EventLog: 产生事件 采购订单已发布
    EventLog->>ReadModel: 投影状态 待确认
    AppService->>Aggregate: 消费事件 运单已创建
    Aggregate->>EventLog: 产生事件 采购订单运输信息已记录
    EventLog->>ReadModel: 投影状态 待到仓
    AppService->>Aggregate: 消费事件 运输已到达
    Aggregate->>EventLog: 产生事件 采购订单运输信息已记录
    EventLog->>ReadModel: 投影状态 到仓待收货
    AppService->>Aggregate: 执行命令 记录入库事实
    Aggregate->>EventLog: 产生事件 采购订单已部分入库
    EventLog->>ReadModel: 投影状态 部分入库
    AppService->>Aggregate: 执行命令 关闭采购订单
    Aggregate->>EventLog: 产生事件 采购订单已关闭
    EventLog->>ReadModel: 投影状态 已关闭
```

## 10. 不变量、异常补偿、权限与审计

| 类型 | 规则 |
| --- | --- |
| 聚合不变量 | `采购订单` 的状态只能通过聚合根行为推进，内部实体不能被外部直接修改 |
| 数量和金额不变量 | 数量、金额、税率、币种、交期、有效期必须通过值对象校验 |
| 幂等 | 命令和事件消费都必须有幂等键，重复请求不能重复产生业务事实 |
| 并发 | 聚合保存使用版本号乐观锁，冲突时重新加载聚合并返回可重试错误 |
| 补偿 | 发布失败走事件发布表重试，消费失败走收件箱重试；TMS运单创建失败、运输延误、到仓未收货、物流费用来源延迟等进入人工待办或补偿流程 |
| 权限 | 按角色、组织、采购范围、供应商范围、金额阈值控制命令可执行性 |
| 审计 | 所有写命令记录操作者、来源、请求摘要、前后状态、事件编号和失败原因 |

## 11. 读模型设计

读模型服务于查询和页面展示，不参与聚合不变量保护。写入决策必须回到应用服务、聚合根和领域服务。

| 读模型 | 使用场景 | 主要字段 |
| --- | --- | --- |
| 采购订单列表读模型 | 查询、分页、筛选 | 单号、状态、供应商、金额、数量、运输状态、预计到仓、更新时间 |
| 采购订单详情读模型 | 详情页展示 | 单头、明细、运输快照、物流费用来源引用、状态历史、事件历史、操作日志 |
| 采购订单异常读模型 | 异常处理看板 | 异常类型、运单号、责任人、处理状态、阻塞原因 |

## 12. 设计结论与待确认问题

### 12.1 设计结论

- `采购订单` 是采购领域内独立保护业务规则和状态流转的聚合根。
- 命令处理属于应用层编排，核心规则属于聚合根和领域服务。
- 采购上下文不直接修改供应商、TMS、WMS、中央库存、BMS 的主权数据，只消费事实并保存采购侧快照。TMS 到仓只更新运输进度，不能替代 WMS 收货或中央库存入库。

### 12.2 待确认问题

| 问题 | 默认建议 |
| --- | --- |
| 是否多组织、多采购组织、多仓库 | 默认保留组织、采购组织、仓库、供应商数据范围 |
| 是否允许终态单据强制修改 | 默认不允许，需通过变更、关闭、作废或补偿单处理 |
| 是否需要事件溯源 | 当前阶段建议当前状态表 + 历史表 + 事件日志，不做全量事件溯源 |
