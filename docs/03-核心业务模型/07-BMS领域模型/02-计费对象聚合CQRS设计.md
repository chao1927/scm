# 02-计费对象聚合CQRS设计

> 所属上下文：BMS 领域。本文按 DDD + CQRS 深入到聚合属性、命令处理、应用服务编排、领域服务规则、事件产生和事件消费逻辑。关键时序图使用 Mermaid 最小兼容语法，便于 VSCode Markdown 预览稳定渲染。

## 1. 业务目标分析

维护客户、货主、供应商、物流商等可计费主体，以及结算方向、账期、税率、币种和结算资料，为后续规则匹配、物流费用计算和费用生成提供合法主体。

| 设计项 | 结论 |
| --- | --- |
| 限界上下文 | BMS 上下文 |
| 子域类型 | 核心域，结算主体建模 |
| 聚合根 | 计费对象 |
| 数据主权 | BMS 拥有计费对象、计费规则、费用来源、费用明细、调整、对账、账单、发票交接和财务交接事实；TMS 拥有物流商能力、运单、轨迹和物流费用来源事实。 |
| 主要使用角色 | 结算运营、财务、主数据系统、客户、供应商、物流商 |
| 核心不变量 | 停用或资料不完整的计费对象不能生成新费用；外部只能通过聚合根修改内部实体；命令和消费事件必须幂等 |

## 2. 角色、场景与流程分析

| 场景 | 发起角色 | 应用服务处理逻辑 | 领域服务 | 结果事件 |
| --- | --- | --- | --- | --- |
| 创建计费对象 | 结算运营 | 围绕计费对象执行创建计费对象，校验计费对象、账期、费用类型、金额、税率、状态、幂等键和权限 | 计费主体校验服务 | 计费对象已创建 |
| 启用计费对象 | 结算运营 | 围绕计费对象执行启用计费对象，校验计费对象、账期、费用类型、金额、税率、状态、幂等键和权限 | 计费主体校验服务 | 计费对象已启用 |
| 变更计费关系 | 结算运营 | 围绕计费对象执行变更计费关系，校验计费对象、账期、费用类型、金额、税率、状态、幂等键和权限 | 计费主体校验服务 | 计费关系已变更 |
| 同步物流商结算资料 | 主数据/TMS | 同步物流商主体、结算账户、服务产品和账期，形成物流商应付计费对象 | 物流商结算资料校验服务 | 物流商计费对象已同步 |
| 停用计费对象 | 结算运营 | 围绕计费对象执行停用计费对象，校验计费对象、账期、费用类型、金额、税率、状态、幂等键和权限 | 计费主体校验服务 | 计费对象已停用 |

## 3. 领域边界与分层架构

BMS 事件的位置要明确区分三层含义：领域层产生结算事实，应用层保存聚合与事件发布表，基础设施层投递消息并消费 WMS、OMS、库存、主数据和财务系统的外部事实。

```mermaid
flowchart TB
    CommandAPI["CommandAPI 命令接口"]
    QueryAPI["QueryAPI 查询接口"]
    EventConsumer["EventConsumer 事件消费入口"]
    AppService["AppService 计费对象应用服务"]
    CommandHandler["CommandHandler 命令处理器"]
    EventHandler["EventHandler 事件处理器"]
    Aggregate["Aggregate 计费对象"]
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
| billingObjectId | 计费对象ID | 聚合根 | 否 | 创建计费对象 | 全局唯一 |
| billingObjectCode | 计费对象单号或编码 | 值对象 | 否 | 创建计费对象 | 按BMS编码规则生成 |
| billingObjectRef | 计费对象引用 | 值对象 | 是 | 创建或匹配命令 | 关联客户、货主、供应商或物流商结算主体 |
| carrierSettlementRef | 物流商结算引用 | 值对象 | 是 | 同步物流商结算资料/变更计费关系 | 物流商编码、服务产品、结算账户、账期、币种、税率、对账方式 |
| period | 账期 | 值对象 | 是 | 创建、汇总、确认命令 | 账期关闭后不能直接改写 |
| amountSnapshot | 金额快照 | 值对象 | 是 | 计算、调整、确认命令 | 保存未税金额、税额、含税金额、币种和汇率 |
| sourceSnapshot | 来源事实快照 | 值对象 | 是 | 采集或生成命令 | 保存来源上下文、来源单号、业务发生时间和幂等键 |
| status | 业务状态 | 值对象 | 是 | 状态推进命令 | 必须按状态机流转 |
| lineList | 明细行 | 内部实体集合 | 是 | 生成、调整、确认命令 | 记录费用类型、数量、单价、税率、金额和差异 |
| operationLog | 操作记录 | 内部实体集合 | 是 | 所有写命令 | 记录操作者、原因、前后状态和事件编号 |

## 5. 命令与应用服务逻辑

应用服务负责编排结算用例：校验权限、检查幂等、加载聚合、调用领域服务、执行聚合行为、保存聚合、写发布表、写审计日志。

| 命令 | 发起者 | 应用服务处理逻辑 | 参与领域服务 | 成功后领域事件 |
| --- | --- | --- | --- | --- |
| 创建计费对象 | 结算运营 | 加载计费对象聚合，校验状态、金额口径、来源事实、规则版本、审批权限和幂等键，执行聚合行为并写入事件发布表 | 计费主体校验服务 | 计费对象已创建 |
| 启用计费对象 | 结算运营 | 加载计费对象聚合，校验状态、金额口径、来源事实、规则版本、审批权限和幂等键，执行聚合行为并写入事件发布表 | 计费主体校验服务 | 计费对象已启用 |
| 变更计费关系 | 结算运营 | 加载计费对象聚合，校验状态、金额口径、来源事实、规则版本、审批权限和幂等键，执行聚合行为并写入事件发布表 | 计费主体校验服务 | 计费关系已变更 |
| 同步物流商结算资料 | 主数据/TMS事件消费者 | 校验物流商启用、服务产品有效、结算资料完整，创建或更新物流商应付计费对象 | 物流商结算资料校验服务 | 物流商计费对象已同步 |
| 停用计费对象 | 结算运营 | 加载计费对象聚合，校验状态、金额口径、来源事实、规则版本、审批权限和幂等键，执行聚合行为并写入事件发布表 | 计费主体校验服务 | 计费对象已停用 |

### 5.1 应用服务通用处理模板

1. 接口层接收请求、回调或事件，并转换为命令对象。
2. 应用层校验用户、角色、组织、计费对象、账期、金额权限和数据权限。
3. 使用 `来源上下文 + 来源单号 + 命令类型 + 幂等键` 做幂等检查。
4. 通过资源库加载 `计费对象` 聚合根，新建场景先校验业务唯一性。
5. 调用领域服务完成计费对象、规则版本、金额、税额、账期和外部事实的判断。
6. 聚合根执行行为，修改属性、内部实体和值对象，并产生领域事件。
7. 同一事务保存聚合、事件发布表和操作审计。
8. 事件发布器异步投递事件，读模型投影器更新结算查询模型。

### 5.2 关键命令处理细节

| 关键命令 | 前置校验 | 聚合行为 | 异常或补偿处理 |
| --- | --- | --- | --- |
| 创建计费对象 | 计费对象处于允许状态，计费对象有效，金额口径一致，账期未关闭，命令未重复 | 修改计费对象状态为`草稿`，记录计算或确认快照，产生`计费对象已创建` | 状态不匹配则拒绝；金额不平进入差异处理；外部交接失败进入补偿待办 |
| 启用计费对象 | 计费对象处于允许状态，计费对象有效，金额口径一致，账期未关闭，命令未重复 | 修改计费对象状态为`已启用`，记录计算或确认快照，产生`计费对象已启用` | 状态不匹配则拒绝；金额不平进入差异处理；外部交接失败进入补偿待办 |
| 变更计费关系 | 计费对象处于允许状态，计费对象有效，金额口径一致，账期未关闭，命令未重复 | 修改计费对象状态为`已启用`，记录计算或确认快照，产生`计费关系已变更` | 状态不匹配则拒绝；金额不平进入差异处理；外部交接失败进入补偿待办 |
| 同步物流商结算资料 | 物流商已启用，结算账户、税率、账期和服务产品有效 | 创建或更新物流商计费对象快照，产生`物流商计费对象已同步` | 资料缺失则进入待补资料；不能为缺失结算资料的物流商生成新应付运费 |
| 停用计费对象 | 计费对象处于允许状态，计费对象有效，金额口径一致，账期未关闭，命令未重复 | 修改计费对象状态为`已停用`，记录计算或确认快照，产生`计费对象已停用` | 状态不匹配则拒绝；金额不平进入差异处理；外部交接失败进入补偿待办 |

## 6. 领域服务逻辑

| 领域服务 | 核心逻辑 |
| --- | --- |
| 计费主体校验服务 | 处理计费对象中跨对象、跨规则、跨账期或跨来源事实的结算判断，保证金额、税额、状态和版本口径一致。 |
| 结算资料完整性服务 | 处理计费对象中跨对象、跨规则、跨账期或跨来源事实的结算判断，保证金额、税额、状态和版本口径一致。 |
| 结算关系冲突检查服务 | 处理计费对象中跨对象、跨规则、跨账期或跨来源事实的结算判断，保证金额、税额、状态和版本口径一致。 |
| 物流商结算资料校验服务 | 校验物流商、物流产品、结算方向、币种、税率、账期和对账方式，避免 TMS 费用来源无法匹配应付主体。 |

## 7. 事件产生逻辑

| 领域事件 | 触发命令 | 关键载荷 | 主要消费者 |
| --- | --- | --- | --- |
| 计费对象已创建 | 创建计费对象 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`草稿`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 计费对象已启用 | 启用计费对象 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已启用`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 计费关系已变更 | 变更计费关系 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已启用`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 物流商计费对象已同步 | 同步物流商结算资料 | 聚合ID、物流商、服务产品、结算方向、账期、币种、税率、状态 | BMS读模型、计费规则、费用计算、对账、审计日志 |
| 计费对象已停用 | 停用计费对象 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已停用`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |

### 7.1 事件生成规则

- 领域事件使用过去式命名，只表达已经发生的结算事实。
- 聚合根在业务行为成功后产生领域事件；应用服务负责收集、持久化和发布。
- 事件载荷必须包含事件编号、事件版本、发生时间、来源上下文、聚合ID、聚合版本、计费对象、账期、金额口径、操作者和幂等键。
- 命令幂等命中时，返回原处理结果，不能重复生成费用、调整、对账、账单、发票或财务交接事实。
- 外部事件消费必须先进入事件收件箱，再由应用服务加载聚合并执行本地结算行为。

## 8. 事件订阅与消费逻辑

| 订阅事件 | 处理应用服务 | 消费后数据变化 | 幂等键 |
| --- | --- | --- | --- |
| 客户已启用 | 计费对象应用服务 | 创建或更新客户计费对象快照 | 来源上下文+事件编号+业务主键 |
| 供应商已启用 | 计费对象应用服务 | 创建或更新供应商计费对象快照 | 来源上下文+事件编号+业务主键 |
| 物流商已启用 | 计费对象应用服务 | 创建或更新物流商计费对象快照和结算资料 | 主数据上下文+事件编号+carrierId |
| TMS服务产品已变更 | 计费对象应用服务 | 刷新物流商服务产品和结算关系候选项 | TMS上下文+事件编号+serviceProductId |
| 计费对象已启用 | 计费对象同步服务 | 更新计费对象快照或创建结算待办 | 主数据上下文+事件编号+对象编码 |
| 财务交接失败 | 财务交接补偿服务 | 标记交接异常并生成补偿任务 | 财务上下文+事件编号+交接单号 |

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

    User->>CommandAPI: 结算运营提交命令 创建计费对象
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载计费对象聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 计费对象已创建
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 计费对象已创建
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

    User->>CommandAPI: 结算运营提交命令 启用计费对象
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载计费对象聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 计费对象已启用
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 计费对象已启用
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

    User->>CommandAPI: 结算运营提交命令 变更计费关系
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载计费对象聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 计费关系已变更
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 计费关系已变更
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

    MessageBroker->>EventConsumer: 投递外部事件 客户已启用
    EventConsumer->>Inbox: 检查事件是否已消费
    Inbox->>EventConsumer: 返回未消费
    EventConsumer->>AppService: 转换为事件消费命令
    AppService->>Repository: 加载相关聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>Aggregate: 应用外部结算事实
    Aggregate->>AppService: 创建或更新客户计费对象快照
    AppService->>Repository: 保存聚合变化
    AppService->>ReadModel: 更新结算查询投影
    AppService->>Inbox: 标记事件消费成功
```

### 9.5 聚合状态推进时序

```mermaid
sequenceDiagram
    participant AppService
    participant Repository
    participant Aggregate
    participant DomainService
    participant Outbox

    AppService->>Repository: 加载计费对象
    Repository->>AppService: 返回状态 初始或上一业务状态
    AppService->>DomainService: 校验不变量 计费对象有效且结算资料完整
    DomainService->>AppService: 返回允许
    AppService->>Aggregate: 执行命令 创建计费对象
    Aggregate->>AppService: 状态变为 草稿
    AppService->>Repository: 保存新状态
    AppService->>Outbox: 保存状态变化事件
```

## 10. 异常、补偿、幂等、权限、审计

| 类型 | 设计 |
| --- | --- |
| 异常 | 结算资料缺失、税务资料无效、同一主体重复建档、结算关系冲突、停用对象仍被规则引用。 |
| TMS边界 | TMS 服务产品、物流商能力和运单事实不能直接创建费用；必须先匹配启用且资料完整的物流商计费对象。 |
| 补偿 | 允许在未确认或未入账前重算、作废、调整；已确认或已入账后必须通过调整单、冲减单或补差单处理 |
| 幂等 | 命令幂等键使用来源上下文、来源单号、命令类型和请求流水；事件消费幂等使用事件编号和业务主键 |
| 权限 | 按角色、组织、计费对象、账期、金额阈值、审批节点和数据范围控制 |
| 审计 | 所有写命令记录请求摘要、操作者、原因、前后状态、金额变化、事件编号和外部回调编号 |

## 11. 读模型设计

| 读模型 | 用途 | 数据来源 | 刷新方式 |
| --- | --- | --- | --- |
| 计费对象列表读模型 | 列表查询、条件筛选、分页和导出 | 聚合事件投影 + 主数据快照 | 事件投影更新 |
| 计费对象详情读模型 | 查看明细行、金额、税额、来源、状态、物流商结算资料和操作记录 | 聚合当前状态 + 操作日志 | 写命令后同步刷新 |
| BMS工作台读模型 | 展示待计算、待对账、待开票、待交财务和异常数量 | 多聚合事件汇总 | 异步投影 |
| 结算报表读模型 | 收入、成本、毛利、差异、账龄和开票进度分析 | 费用、对账、账单、财务交接事件 | 定时汇总 + 事件增量 |

## 12. 当前结论与待确认问题

当前结论：`计费对象` 是 BMS 结算生命周期中的关键聚合，写侧必须以聚合根保护金额、账期、规则版本、状态和幂等不变量，读侧使用投影支撑列表、详情、工作台和报表。

关键假设：BMS 消费业务事实并生成费用和账单，TMS 拥有物流商服务产品和运单事实，财务系统拥有最终凭证和资金入账事实。

待确认问题：是否需要支持多币种汇率重估、跨月补差、红冲发票、部分开票、供应商应付和客户应收在同一账期内抵扣。这些会影响字段模型和状态机细化。
