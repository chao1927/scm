# 10-退供申请聚合CQRS设计

> 所属上下文：采购领域。本文按 DDD + CQRS 深入到聚合属性、命令处理、应用服务编排、领域服务规则、事件产生和事件消费逻辑。关键时序图使用 Mermaid 最小兼容语法，便于 VSCode Markdown 预览稳定渲染。

## 1. 业务目标分析

采购侧发起退供应商意图，说明退供原因、来源、数量、责任和审批结果，并把批准事实传递给供应商、WMS 和 TMS 执行，随后消费退供运单、签收/拒收和运输异常事实完成采购侧闭环。

| 设计项 | 结论 |
| --- | --- |
| 限界上下文 | 02-采购系统上下文 |
| 子域类型 | 核心域，连接采购质量异常与退供执行 |
| 聚合根 | 退供申请 |
| 数据主权 | 采购上下文拥有 `退供申请` 的生命周期、状态、业务规则和领域事件；TMS拥有退供运输任务、运单、签收/拒收、轨迹和物流异常；外部系统只能通过命令或事件协作 |
| 主要使用角色 | 采购员、质控人员、采购经理、WMS、TMS、供应商系统 |
| 核心不变量 | 外部只能通过聚合根修改内部实体；状态流转必须合法；写命令和消费事件必须幂等 |

## 2. 角色、场景与流程分析

| 场景 | 发起角色 | 应用服务处理逻辑 | 领域服务 | 结果事件 |
| --- | --- | --- | --- | --- |
| 创建退供申请 | 采购员 | 围绕退供申请执行创建退供申请，校验状态、来源和业务规则 | 退供来源校验服务 | 退供申请已创建 |
| 提交退供申请 | 采购员 | 围绕退供申请执行提交退供申请，校验状态、来源和业务规则 | 退供来源校验服务 | 退供申请已提交 |
| 审批退供申请 | 采购员 | 围绕退供申请执行审批退供申请，校验状态、来源和业务规则 | 退供来源校验服务 | 退供申请已批准 |
| 通知退供执行 | 采购员 | 通知供应商确认、WMS退供出库和TMS创建退供运输任务 | 退供来源校验服务 | 退供申请已通知执行 |
| 记录退供运单 | TMS事件消费者 | 消费 TMS 运单创建事实，写入承运商和运输状态 | 退供运输事实校验服务 | 退供运输信息已记录 |
| 记录供应商签收/拒收 | TMS事件消费者 | 消费供应商签收或拒收事实，更新退供执行结果 | 退供运输事实校验服务 | 退供供应商已签收 / 退供供应商已拒收 |
| 记录退供运输异常 | TMS事件消费者 | 消费破损、丢失、延误等异常，生成退供异常待办 | 退供运输事实校验服务 | 退供运输异常已登记 |
| 关闭退供申请 | 采购员 | 围绕退供申请执行关闭退供申请，校验状态、来源和业务规则 | 退供来源校验服务 | 退供申请已关闭 |

## 3. 领域边界与分层架构

领域事件的位置要明确区分三层含义：领域层产生事件，应用层保存聚合与事件发布表，基础设施层投递消息并消费外部事件。

```mermaid
flowchart TB
    CommandAPI["CommandAPI 命令接口"]
    QueryAPI["QueryAPI 查询接口"]
    EventConsumer["EventConsumer 事件消费入口"]
    AppService["AppService 退供申请应用服务"]
    CommandHandler["CommandHandler 命令处理器"]
    EventHandler["EventHandler 事件处理器"]
    Aggregate["Aggregate 退供申请"]
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
| 退供申请Id | 退供申请ID | 聚合根 | 否 | 创建退供申请 | 全局唯一 |
| 退供申请No | 退供申请单号 | 值对象 | 否 | 创建退供申请 | 按编码规则生成 |
| status | 业务状态 | 值对象 | 是 | 状态推进命令 | 必须按状态机流转 |
| lineList | 明细行 | 内部实体集合 | 是 | 创建或变更命令 | 记录SKU、数量、价格、交期等明细 |
| sourceRef | 来源引用 | 值对象 | 否 | 创建退供申请 | 记录请购、询价、报价、PO或外部事件来源 |
| snapshot | 业务快照 | 值对象 | 是 | 创建或事件消费 | 保存供应商、SKU、仓库、价格等外部事实快照 |
| returnTransportSnapshot | 退供运输快照 | 内部实体 | 是 | 记录退供运单/签收/异常 | TMS运输任务号、运单号、承运商、运输状态、签收/拒收结果、异常类型、责任方 |
| operationLog | 操作记录 | 内部实体集合 | 是 | 所有写命令 | 记录操作者、动作、原因、前后状态 |

## 5. 命令与应用服务逻辑

应用服务负责编排用例：校验权限、检查幂等、加载聚合、调用领域服务、执行聚合行为、保存聚合、写发布表、写审计日志。

| 命令 | 发起者 | 应用服务处理逻辑 | 参与领域服务 | 成功后领域事件 |
| --- | --- | --- | --- | --- |
| 创建退供申请 | 采购员 | 围绕退供申请执行创建退供申请，校验状态、来源和业务规则 | 退供来源校验服务 | 退供申请已创建 |
| 提交退供申请 | 采购员 | 围绕退供申请执行提交退供申请，校验状态、来源和业务规则 | 退供来源校验服务 | 退供申请已提交 |
| 审批退供申请 | 采购员 | 围绕退供申请执行审批退供申请，校验状态、来源和业务规则 | 退供来源校验服务 | 退供申请已批准 |
| 通知退供执行 | 采购员 | 围绕退供申请执行通知退供执行，校验状态、来源和业务规则 | 退供来源校验服务 | 退供申请已通知执行 |
| 记录退供运单 | TMS事件消费者 | 写入 TMS 运单、承运商和在途状态 | 退供运输事实校验服务 | 退供运输信息已记录 |
| 记录供应商签收/拒收 | TMS事件消费者 | 写入签收/拒收数量、时间、证明和差异 | 退供运输事实校验服务 | 退供供应商已签收 / 退供供应商已拒收 |
| 记录退供运输异常 | TMS事件消费者 | 写入运输异常、影响数量和责任方，阻塞正常关闭 | 退供运输事实校验服务 | 退供运输异常已登记 |
| 关闭退供申请 | 采购员 | 围绕退供申请执行关闭退供申请，校验状态、来源和业务规则 | 退供来源校验服务 | 退供申请已关闭 |

### 5.1 应用服务通用处理模板

1. 接口层接收请求并转换为命令对象。
2. 应用层校验用户、角色、组织、采购范围和数据权限。
3. 使用 `来源系统 + 业务单号 + 命令类型 + 幂等键` 做幂等检查。
4. 通过资源库加载 `退供申请` 聚合根，新建场景先校验业务唯一性。
5. 调用领域服务完成跨实体、跨策略或外部事实快照的规则判断。
6. 聚合根执行行为，修改属性、内部实体和值对象，并产生领域事件。
7. 同一事务保存聚合、事件发布表和操作审计。
8. 事件发布器异步投递事件，读模型投影器更新查询模型。

### 5.2 关键命令处理细节

| 关键命令 | 前置校验 | 聚合行为 | 异常或补偿处理 |
| --- | --- | --- | --- |
| 创建退供申请 | 退供申请状态允许执行，来源数据和权限有效 | 修改退供申请状态或明细并产生事件 退供申请已创建 | 状态不匹配则拒绝；外部协作失败进入待办或补偿流程 |
| 提交退供申请 | 退供申请状态允许执行，来源数据和权限有效 | 修改退供申请状态或明细并产生事件 退供申请已提交 | 状态不匹配则拒绝；外部协作失败进入待办或补偿流程 |
| 审批退供申请 | 退供申请状态允许执行，来源数据和权限有效 | 修改退供申请状态或明细并产生事件 退供申请已批准 | 状态不匹配则拒绝；外部协作失败进入待办或补偿流程 |
| 通知退供执行 | 退供申请已批准；退供仓库、供应商退货地址、运输要求完整 | 状态进入已通知执行；发布给供应商、WMS、TMS | TMS创建运单失败不回滚通知事实，进入退供运输异常待办 |
| 记录退供运单 | 来源为 TMS；退供申请已通知执行；运单与退供申请匹配 | 写入运单、承运商、预计到达，状态进入退货中/在途 | 重复运单幂等；不匹配进入异常事实池 |
| 记录供应商签收/拒收 | 来源为 TMS；退供运单已记录；签收/拒收对象为目标供应商 | 写入签收或拒收结果；签收无差异可完成，有差异进入异常待处理 | 拒收不能直接关闭，需采购、供应商、BMS 处理责任和索赔 |
| 关闭退供申请 | TMS签收或争议结论完成；WMS出库和BMS影响已确认 | 状态改已关闭，发布退供申请已关闭 | 运输异常或拒收未处理时不能正常关闭 |

## 6. 领域服务逻辑

| 领域服务 | 核心逻辑 |
| --- | --- |
| 退供来源校验服务 | 围绕退供申请的状态、不变量、外部事实快照和策略配置进行业务判定。 |
| 退供数量校验服务 | 围绕退供申请的状态、不变量、外部事实快照和策略配置进行业务判定。 |
| 退供审批判定服务 | 围绕退供申请的状态、不变量、外部事实快照和策略配置进行业务判定。 |
| 退供运输事实校验服务 | 校验 TMS 运单、签收、拒收和异常是否匹配退供申请、供应商、仓库和数量，防止外部事件错绑。 |

## 7. 事件产生逻辑

| 领域事件 | 触发命令 | 关键载荷 | 主要消费者 |
| --- | --- | --- | --- |
| 退供申请已创建 | 创建退供申请 | 退供申请ID、业务状态、关键明细摘要 | 本领域读模型、上下游协作系统、审计日志 |
| 退供申请已提交 | 提交退供申请 | 退供申请ID、业务状态、关键明细摘要 | 本领域读模型、上下游协作系统、审计日志 |
| 退供申请已批准 | 审批退供申请 | 退供申请ID、业务状态、关键明细摘要 | 本领域读模型、上下游协作系统、审计日志 |
| 退供申请已通知执行 | 通知退供执行 | 退供申请ID、供应商、退货仓、退供行、退货地址、运输要求 | 本领域读模型、供应商系统、WMS、TMS、审计日志 |
| 退供运输信息已记录 | 记录退供运单 | 退供申请ID、TMS运输任务号、运单号、承运商、预计到达 | 本领域读模型、退供跟踪 |
| 退供供应商已签收 | 记录供应商签收/拒收 | 退供申请ID、运单号、签收数量、签收时间、签收证明 | 本领域读模型、BMS、供应商履约分析 |
| 退供供应商已拒收 | 记录供应商签收/拒收 | 退供申请ID、运单号、拒收数量、拒收原因、责任方 | 本领域读模型、采购异常看板、BMS |
| 退供运输异常已登记 | 记录退供运输异常 | 退供申请ID、运单号、异常类型、影响数量、责任方 | 本领域读模型、采购异常看板、BMS |
| 退供申请已关闭 | 关闭退供申请 | 退供申请ID、业务状态、关键明细摘要 | 本领域读模型、上下游协作系统、审计日志 |

### 7.1 事件生成规则

- 领域事件使用过去式命名，只表达已经发生的业务事实。
- 聚合根在业务行为成功后产生领域事件；应用服务负责收集、持久化和发布。
- 事件载荷必须包含事件编号、事件版本、发生时间、来源上下文、聚合ID、聚合版本、操作者和关键业务字段。
- 命令幂等命中时，返回原处理结果，不能重复产生事件。
- 外部事件消费必须先进入事件收件箱，再由应用服务加载聚合并执行本地业务行为。

## 8. 事件订阅与消费逻辑

| 订阅事件 | 处理应用服务 | 消费后数据变化 | 幂等键 |
| --- | --- | --- | --- |
| 质检已完成 | 外部事件消费服务 | 不合格时创建退供候选申请 | 来源上下文+事件编号+业务主键 |
| 退供出库已完成 | WMS事件消费服务 | 记录退供已出库，等待 TMS 运单和供应商签收 | WMS上下文+事件编号+outboundId |
| 运单已创建 | TMS事件消费服务 | 写入退供运单、承运商、预计到达，状态进入退货中/在途 | TMS上下文+事件编号+waybillNo |
| 运输已签收 | TMS事件消费服务 | 写入供应商签收数量、签收时间和证明，满足关闭条件之一 | TMS上下文+事件编号+waybillNo |
| 运输已拒收 | TMS事件消费服务 | 写入拒收原因和责任方，进入异常待处理 | TMS上下文+事件编号+waybillNo |
| 物流异常已登记 | TMS事件消费服务 | 写入运输异常，阻塞正常关闭，必要时形成索赔依据 | TMS上下文+事件编号+exceptionNo |
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

    User->>CommandAPI: 采购员提交命令 创建退供申请
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载退供申请聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 退供申请已创建
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 退供申请已创建
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

    User->>CommandAPI: 采购员提交命令 提交退供申请
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载退供申请聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 退供申请已提交
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 退供申请已提交
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

    User->>CommandAPI: 采购员提交命令 审批退供申请
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载退供申请聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 退供申请已批准
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 退供申请已批准
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

    MessageBroker->>EventConsumer: 投递外部事件 运输已签收
    EventConsumer->>Inbox: 检查事件是否已消费
    Inbox->>EventConsumer: 返回未消费
    EventConsumer->>AppService: 转换为事件消费命令
    AppService->>Repository: 加载相关聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>Aggregate: 应用TMS签收事实
    Aggregate->>AppService: 记录供应商签收结果
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

    AppService->>Aggregate: 执行命令 创建退供申请
    Aggregate->>EventLog: 产生事件 退供申请已创建
    EventLog->>ReadModel: 投影状态 草稿
    AppService->>Aggregate: 执行命令 提交退供申请
    Aggregate->>EventLog: 产生事件 退供申请已提交
    EventLog->>ReadModel: 投影状态 待审批
    AppService->>Aggregate: 执行命令 审批退供申请
    Aggregate->>EventLog: 产生事件 退供申请已批准
    EventLog->>ReadModel: 投影状态 已批准
    AppService->>Aggregate: 执行命令 通知退供执行
    Aggregate->>EventLog: 产生事件 退供申请已通知执行
    EventLog->>ReadModel: 投影状态 已通知
    AppService->>Aggregate: 消费事件 运单已创建
    Aggregate->>EventLog: 产生事件 退供运输信息已记录
    EventLog->>ReadModel: 投影状态 在途
    AppService->>Aggregate: 消费事件 运输已签收
    Aggregate->>EventLog: 产生事件 退供供应商已签收
    EventLog->>ReadModel: 投影状态 已签收
    AppService->>Aggregate: 执行命令 关闭退供申请
    Aggregate->>EventLog: 产生事件 退供申请已关闭
    EventLog->>ReadModel: 投影状态 已关闭
```

## 10. 不变量、异常补偿、权限与审计

| 类型 | 规则 |
| --- | --- |
| 聚合不变量 | `退供申请` 的状态只能通过聚合根行为推进，内部实体不能被外部直接修改 |
| 数量和金额不变量 | 退供申请数量、出库数量、签收数量、拒收数量必须通过值对象校验；签收数量不能大于退供出库数量 |
| 幂等 | 命令和事件消费都必须有幂等键，重复请求不能重复产生业务事实 |
| 并发 | 聚合保存使用版本号乐观锁，冲突时重新加载聚合并返回可重试错误 |
| 补偿 | 发布失败走事件发布表重试，消费失败走收件箱重试；TMS运单创建失败、签收延迟、拒收、破损、丢失等业务阻塞进入人工待办 |
| 权限 | 按角色、组织、采购范围、供应商范围、金额阈值控制命令可执行性 |
| 审计 | 所有写命令记录操作者、来源、请求摘要、前后状态、事件编号和失败原因 |

## 11. 读模型设计

读模型服务于查询和页面展示，不参与聚合不变量保护。写入决策必须回到应用服务、聚合根和领域服务。

| 读模型 | 使用场景 | 主要字段 |
| --- | --- | --- |
| 退供申请列表读模型 | 查询、分页、筛选 | 单号、状态、供应商、数量、退供运单、运输状态、更新时间 |
| 退供申请详情读模型 | 详情页展示 | 单头、明细、退供运输快照、签收/拒收结果、状态历史、事件历史、操作日志 |
| 退供申请异常读模型 | 异常处理看板 | 异常类型、运单号、责任人、影响数量、处理状态、阻塞原因 |

## 12. 设计结论与待确认问题

### 12.1 设计结论

- `退供申请` 是采购领域内独立保护业务规则和状态流转的聚合根。
- 命令处理属于应用层编排，核心规则属于聚合根和领域服务。
- 采购上下文不直接修改供应商、TMS、WMS、中央库存、BMS 的主权数据，只消费事实并保存采购侧快照；退供签收、拒收和运输异常以 TMS 事实为准。

### 12.2 待确认问题

| 问题 | 默认建议 |
| --- | --- |
| 是否多组织、多采购组织、多仓库 | 默认保留组织、采购组织、仓库、供应商数据范围 |
| 是否允许终态单据强制修改 | 默认不允许，需通过变更、关闭、作废或补偿单处理 |
| 是否需要事件溯源 | 当前阶段建议当前状态表 + 历史表 + 事件日志，不做全量事件溯源 |
