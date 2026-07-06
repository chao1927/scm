# 09-数据质量问题聚合CQRS设计

> 所属上下文：主数据领域。本文按 DDD + CQRS 深入到聚合属性、命令处理、应用服务编排、领域服务规则、事件产生和事件消费逻辑。关键时序图使用 Mermaid 最小兼容语法，便于 VSCode Markdown 预览稳定渲染。

## 1. 业务目标分析

发现并治理主数据重复、缺失、过期、引用断裂、字段不规范和疑似合并问题，支撑冻结、修复、合并和关闭；TMS 反馈的承运商接口失败、物流产品不可用、地址区域无法匹配、禁运规则冲突和面单模板错误也应进入主数据质量治理。

| 设计项 | 结论 |
| --- | --- |
| 限界上下文 | 主数据上下文 |
| 子域类型 | 支撑域，数据质量治理 |
| 聚合根 | 数据质量问题 |
| 数据主权 | 主数据上下文拥有类型定义、字段模板、编码、主数据记录、版本、发布订阅、导入结果和数据质量治理事实；采购、OMS、WMS、库存、TMS、BMS 只消费主数据事件、保存必要快照和本地引用，不拥有主数据权威口径。 |
| 主要使用角色 | 数据治理人员、主数据专员、审批人、业务负责人 |
| 核心不变量 | 质量问题必须绑定问题类型、影响对象和处理结论；合并或冻结必须保留追溯；外部只能通过聚合根修改内部实体；命令和消费事件必须幂等 |

## 2. 角色、场景与流程分析

| 场景 | 发起角色 | 应用服务处理逻辑 | 领域服务 | 结果事件 |
| --- | --- | --- | --- | --- |
| 发现质量问题 | 数据治理任务 | 围绕数据质量问题执行发现质量问题，校验类型、字段、编码、引用、状态、幂等键、审批权限和发布范围 | 数据质量检测服务 | 数据质量问题已发现 |
| 发现物流主数据质量问题 | TMS/数据治理任务 | 根据 TMS 联调、承运商接口、线路匹配、禁运校验、面单打印或轨迹订阅失败发现质量问题 | 物流主数据质量检测服务 | 数据质量问题已发现 |
| 分派质量问题 | 数据治理任务 | 围绕数据质量问题执行分派质量问题，校验类型、字段、编码、引用、状态、幂等键、审批权限和发布范围 | 数据质量检测服务 | 数据质量问题已分派 |
| 修复质量问题 | 数据治理任务 | 围绕数据质量问题执行修复质量问题，校验类型、字段、编码、引用、状态、幂等键、审批权限和发布范围 | 数据质量检测服务 | 数据质量问题已修复 |
| 确认治理结果 | 数据治理任务 | 围绕数据质量问题执行确认治理结果，校验类型、字段、编码、引用、状态、幂等键、审批权限和发布范围 | 数据质量检测服务 | 数据质量问题已关闭 |
| 驳回治理结果 | 数据治理任务 | 围绕数据质量问题执行驳回治理结果，校验类型、字段、编码、引用、状态、幂等键、审批权限和发布范围 | 数据质量检测服务 | 数据质量问题已重新打开 |

## 3. 领域边界与分层架构

主数据事件的位置要明确区分三层含义：领域层产生权威主数据事实，应用层保存聚合与事件发布表，基础设施层投递消息并消费审批、联调、数据质量和下游回执等外部事实。

```mermaid
flowchart TB
    CommandAPI["CommandAPI 命令接口"]
    QueryAPI["QueryAPI 查询接口"]
    EventConsumer["EventConsumer 事件消费入口"]
    AppService["AppService 数据质量应用服务"]
    CommandHandler["CommandHandler 命令处理器"]
    EventHandler["EventHandler 事件处理器"]
    Aggregate["Aggregate 数据质量问题"]
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
| qualityIssueId | 数据质量问题ID | 聚合根 | 否 | 发现质量问题 | 全局唯一 |
| qualityIssueNo | 数据质量问题编码或单号 | 值对象 | 否 | 发现质量问题 | 按主数据编码规则生成或录入后校验唯一 |
| typeRef | 主数据类型引用 | 值对象 | 是 | 创建或配置命令 | 关联主数据类型和字段模板版本 |
| businessPayload | 业务载荷 | 值对象 | 是 | 创建、变更、导入命令 | 按字段模板校验必填、唯一、枚举、引用和关键字段 |
| tmsQualityEvidence | TMS质量证据 | 值对象 | 是 | 发现质量问题、修复质量问题 | 保存 TMS 反馈系统、物流商/产品/区域编码、失败类型、错误消息、影响业务和建议处理 |
| status | 生命周期状态 | 值对象 | 是 | 状态推进命令 | 必须按状态机流转 |
| versionRef | 当前版本引用 | 值对象 | 是 | 启用、变更、发布命令 | 有效变更必须生成版本，版本快照不可覆盖 |
| publishScope | 发布范围 | 值对象 | 是 | 发布或订阅命令 | 定义目标系统、主题、过滤条件和补偿拉取策略 |
| operationLog | 操作记录 | 内部实体集合 | 是 | 所有写命令 | 记录操作者、原因、前后状态和事件编号 |

## 5. 命令与应用服务逻辑

应用服务负责编排主数据用例：校验权限、检查幂等、加载聚合、调用领域服务、执行聚合行为、保存聚合、写发布表、写审计日志。

| 命令 | 发起者 | 应用服务处理逻辑 | 参与领域服务 | 成功后领域事件 |
| --- | --- | --- | --- | --- |
| 发现质量问题 | 数据治理任务 | 加载数据质量问题聚合，校验字段模板、编码唯一、引用关系、生命周期状态、审批权限和幂等键，执行聚合行为并写入事件发布表 | 数据质量检测服务 | 数据质量问题已发现 |
| 分派质量问题 | 数据治理任务 | 加载数据质量问题聚合，校验字段模板、编码唯一、引用关系、生命周期状态、审批权限和幂等键，执行聚合行为并写入事件发布表 | 数据质量检测服务 | 数据质量问题已分派 |
| 修复质量问题 | 数据治理任务 | 加载数据质量问题聚合，校验字段模板、编码唯一、引用关系、生命周期状态、审批权限和幂等键，执行聚合行为并写入事件发布表 | 数据质量检测服务 | 数据质量问题已修复 |
| 确认治理结果 | 数据治理任务 | 加载数据质量问题聚合，校验字段模板、编码唯一、引用关系、生命周期状态、审批权限和幂等键，执行聚合行为并写入事件发布表 | 数据质量检测服务 | 数据质量问题已关闭 |
| 驳回治理结果 | 数据治理任务 | 加载数据质量问题聚合，校验字段模板、编码唯一、引用关系、生命周期状态、审批权限和幂等键，执行聚合行为并写入事件发布表 | 数据质量检测服务 | 数据质量问题已重新打开 |

### 5.1 应用服务通用处理模板

1. 接口层接收页面请求、导入任务、审批回调或下游回执，并转换为命令对象。
2. 应用层校验用户、角色、组织、主数据类型、字段权限、数据范围和审批权限。
3. 使用 `来源系统 + 来源单号 + 命令类型 + 幂等键` 做幂等检查。
4. 通过资源库加载 `数据质量问题` 聚合根，新建场景先校验业务唯一性。
5. 调用领域服务完成字段模板、编码规则、引用关系、关键字段、版本和发布范围的判断。
6. 聚合根执行行为，修改属性、内部实体和值对象，并产生领域事件。
7. 同一事务保存聚合、事件发布表和操作审计。
8. 事件发布器异步投递事件，读模型投影器更新主数据查询模型。

### 5.2 关键命令处理细节

| 关键命令 | 前置校验 | 聚合行为 | 异常或补偿处理 |
| --- | --- | --- | --- |
| 发现质量问题 | 数据质量问题处于允许状态，编码唯一，字段模板有效，引用关系存在，命令未重复 | 修改数据质量问题状态为`待处理`，记录快照或明细，产生`数据质量问题已发现` | 状态不匹配则拒绝；校验失败返回错误清单；发布失败进入重试或补偿拉取 |
| 分派质量问题 | 数据质量问题处于允许状态，编码唯一，字段模板有效，引用关系存在，命令未重复 | 修改数据质量问题状态为`处理中`，记录快照或明细，产生`数据质量问题已分派` | 状态不匹配则拒绝；校验失败返回错误清单；发布失败进入重试或补偿拉取 |
| 修复质量问题 | 数据质量问题处于允许状态，编码唯一，字段模板有效，引用关系存在，命令未重复 | 修改数据质量问题状态为`待验证`，记录快照或明细，产生`数据质量问题已修复` | 状态不匹配则拒绝；校验失败返回错误清单；发布失败进入重试或补偿拉取 |
| 确认治理结果 | 数据质量问题处于允许状态，编码唯一，字段模板有效，引用关系存在，命令未重复 | 修改数据质量问题状态为`已关闭`，记录快照或明细，产生`数据质量问题已关闭` | 状态不匹配则拒绝；校验失败返回错误清单；发布失败进入重试或补偿拉取 |

## 6. 领域服务逻辑

| 领域服务 | 核心逻辑 |
| --- | --- |
| 数据质量检测服务 | 处理数据质量问题中跨类型、跨字段模板、跨编码规则、跨引用对象或跨目标系统的业务判断，保证主数据口径、版本和发布契约一致。 |
| 重复主数据识别服务 | 处理数据质量问题中跨类型、跨字段模板、跨编码规则、跨引用对象或跨目标系统的业务判断，保证主数据口径、版本和发布契约一致。 |
| 治理影响评估服务 | 处理数据质量问题中跨类型、跨字段模板、跨编码规则、跨引用对象或跨目标系统的业务判断，保证主数据口径、版本和发布契约一致。 |
| 物流主数据质量检测服务 | 识别 TMS 反馈的承运商接口、物流产品、地址区域、禁运规则、面单模板和轨迹接口配置问题，并评估是否需要冻结新运输引用。 |

## 7. 事件产生逻辑

| 领域事件 | 触发命令 | 关键载荷 | 主要消费者 |
| --- | --- | --- | --- |
| 数据质量问题已发现 | 发现质量问题、发现物流主数据质量问题 | 聚合ID、类型编码、主数据编码、版本号、状态`待处理`、变更摘要、目标系统、TMS质量证据、操作者、幂等键 | 采购、OMS、WMS、中央库存、TMS、BMS、供应商系统、读模型、审计日志 |
| 数据质量问题已分派 | 分派质量问题 | 聚合ID、类型编码、主数据编码、版本号、状态`处理中`、变更摘要、目标系统、操作者、幂等键 | 采购、OMS、WMS、中央库存、BMS、供应商系统、读模型、审计日志 |
| 数据质量问题已修复 | 修复质量问题 | 聚合ID、类型编码、主数据编码、版本号、状态`待验证`、变更摘要、目标系统、操作者、幂等键 | 采购、OMS、WMS、中央库存、BMS、供应商系统、读模型、审计日志 |
| 数据质量问题已关闭 | 确认治理结果 | 聚合ID、类型编码、主数据编码、版本号、状态`已关闭`、变更摘要、目标系统、操作者、幂等键 | 采购、OMS、WMS、中央库存、BMS、供应商系统、读模型、审计日志 |
| 数据质量问题已重新打开 | 驳回治理结果 | 聚合ID、类型编码、主数据编码、版本号、状态`处理中`、变更摘要、目标系统、操作者、幂等键 | 采购、OMS、WMS、中央库存、BMS、供应商系统、读模型、审计日志 |

### 7.1 事件生成规则

- 领域事件使用过去式命名，只表达已经发生的主数据事实。
- 聚合根在业务行为成功后产生领域事件；应用服务负责收集、持久化和发布。
- 事件载荷必须包含事件编号、事件版本、发生时间、来源上下文、聚合ID、聚合版本、主数据类型、主数据编码、变更摘要、操作者和幂等键。
- 命令幂等命中时，返回原处理结果，不能重复启用、发布、导入、生成版本或创建质量问题。
- 外部事件消费必须先进入事件收件箱，再由应用服务加载聚合并执行本地主数据行为。

## 8. 事件订阅与消费逻辑

| 订阅事件 | 处理应用服务 | 消费后数据变化 | 幂等键 |
| --- | --- | --- | --- |
| 主数据已变更 | 数据质量应用服务 | 重新检测该记录是否仍存在质量风险 | 来源上下文+事件编号+业务主键 |
| 导入任务已完成 | 数据质量应用服务 | 对导入结果生成质量扫描任务 | 来源上下文+事件编号+业务主键 |
| 承运商接口校验失败 | 数据质量应用服务 | 生成物流商或轨迹接口配置质量问题，必要时建议冻结新运输引用 | TMS+物流商编码+接口配置版本 |
| 物流产品不可用反馈 | 数据质量应用服务 | 生成物流产品、服务区域或禁运规则质量问题 | TMS+物流产品编码+反馈事件号 |
| 审批已完成 | 审批结果消费服务 | 根据审批结果推进启用、驳回或变更生效 | 审批上下文+事件编号+审批单号 |
| 下游回执已返回 | 发布回执消费服务 | 更新发布日志、订阅状态和补偿任务 | 目标系统+事件编号+发布任务号 |

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

    User->>CommandAPI: 数据治理任务提交命令 发现质量问题
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载数据质量问题聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 数据质量问题已发现
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 数据质量问题已发现
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

    User->>CommandAPI: 数据治理任务提交命令 分派质量问题
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载数据质量问题聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 数据质量问题已分派
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 数据质量问题已分派
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

    User->>CommandAPI: 数据治理任务提交命令 修复质量问题
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载数据质量问题聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 数据质量问题已修复
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 数据质量问题已修复
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

    MessageBroker->>EventConsumer: 投递外部事件 主数据已变更
    EventConsumer->>Inbox: 检查事件是否已消费
    Inbox->>EventConsumer: 返回未消费
    EventConsumer->>AppService: 转换为事件消费命令
    AppService->>Repository: 加载相关聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>Aggregate: 应用外部主数据事实
    Aggregate->>AppService: 重新检测该记录是否仍存在质量风险
    AppService->>Repository: 保存聚合变化
    AppService->>ReadModel: 更新主数据查询投影
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

    AppService->>Repository: 加载数据质量问题
    Repository->>AppService: 返回状态 初始或上一业务状态
    AppService->>DomainService: 校验不变量 问题对象和处理结论可追溯
    DomainService->>AppService: 返回允许
    AppService->>Aggregate: 执行命令 发现质量问题
    Aggregate->>AppService: 状态变为 待处理
    AppService->>Repository: 保存新状态
    AppService->>Outbox: 保存状态变化事件
```

## 10. 异常、补偿、幂等、权限、审计

| 类型 | 设计 |
| --- | --- |
| 异常 | 重复记录误判、引用影响范围过大、合并后历史追溯断裂、冻结影响未完业务、治理超时、TMS 反馈无法匹配主数据版本、物流类问题影响在途运单。 |
| 补偿 | 支持错误清单、重新提交、补生成版本、重试发布、下游补偿拉取、冻结新业务和人工治理 |
| 幂等 | 命令幂等键使用来源系统、来源单号、命令类型和请求流水；事件消费幂等使用事件编号和业务主键 |
| 权限 | 按角色、组织、主数据类型、字段、关键字段、审批节点和发布目标系统控制 |
| 审计 | 所有写命令记录请求摘要、操作者、原因、前后状态、字段差异、版本号、事件编号和下游回执编号 |

## 11. 读模型设计

| 读模型 | 用途 | 数据来源 | 刷新方式 |
| --- | --- | --- | --- |
| 数据质量问题列表读模型 | 列表查询、条件筛选、分页和导出 | 聚合事件投影 + 类型和字段模板快照 | 事件投影更新 |
| 数据质量问题详情读模型 | 查看字段、状态、版本、发布范围、错误明细和操作记录 | 聚合当前状态 + 操作日志 | 写命令后同步刷新 |
| 物流主数据质量读模型 | 按物流商、物流产品、地址区域、禁运规则、面单模板、轨迹接口配置和 TMS 反馈查看质量问题 | 数据质量事件 + TMS质量证据 | 事件投影更新 |
| 主数据工作台读模型 | 展示待审核、待发布、发布失败、质量问题和导入异常 | 多聚合事件汇总 | 异步投影 |
| 主数据变更追溯读模型 | 按类型、编码、版本、字段差异追溯历史 | 版本事件、发布日志、操作审计 | 事件增量 + 定时校准 |

## 12. 当前结论与待确认问题

当前结论：`数据质量问题` 是主数据权威口径治理中的关键聚合，写侧必须以聚合根保护编码、字段模板、版本、状态、发布和幂等不变量，读侧使用投影支撑列表、详情、工作台和追溯。

关键假设：主数据系统拥有基础资料权威口径；TMS 反馈的物流类质量问题会回流主数据治理，但 TMS 不直接修改主数据权威记录。

待确认问题：枚举、税率、币种、组织是否全部归主数据统一治理；如果部分归平台配置，需要在后续字段模型和上下文映射中拆开边界。
