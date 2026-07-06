# 05-权限点聚合CQRS设计

> 所属上下文：权限系统领域。本文按 DDD + CQRS 深入到聚合属性、命令处理、应用服务编排、领域服务规则、事件产生和事件消费逻辑。关键时序图使用 Mermaid 最小兼容语法，便于 VSCode Markdown 预览稳定渲染。

## 1. 业务目标分析

注册菜单、页面、按钮、API、字段等可授权资源，形成角色授权和接口鉴权的基础资源目录。TMS 的运输任务、运单、面单、轨迹、签收、物流异常、物流费用来源等页面、按钮、接口和字段也必须建模为权限点，否则物流敏感动作会绕过统一授权。

| 设计项 | 结论 |
| --- | --- |
| 限界上下文 | 权限上下文 |
| 子域类型 | 核心支撑域，权限资源目录 |
| 聚合根 | 权限点 |
| 数据主权 | 权限上下文拥有应用接入、身份、角色、权限点、数据范围、会话、审批结果、审计日志和安全策略事实；业务系统只消费权限结果、提交审批请求、写入审计事件，不直接修改权限权威口径。 |
| 主要使用角色 | 子系统管理员、权限管理员、开发负责人 |
| 核心不变量 | 权限编码在应用内唯一；API 权限必须绑定方法和路径；停用权限点不能再授权；外部只能通过聚合根修改内部实体；命令和消费事件必须幂等 |

## 2. 角色、场景与流程分析

| 场景 | 发起角色 | 应用服务处理逻辑 | 领域服务 | 结果事件 |
| --- | --- | --- | --- | --- |
| 注册权限点 | 子系统管理员 | 围绕权限点执行注册权限点，校验身份、应用、授权范围、状态、权限版本、幂等键和审计要求 | 权限编码校验服务 | 权限点已注册 |
| 变更权限点 | 子系统管理员 | 围绕权限点执行变更权限点，校验身份、应用、授权范围、状态、权限版本、幂等键和审计要求 | 权限编码校验服务 | 权限点已变更 |
| 绑定API资源 | 子系统管理员 | 围绕权限点执行绑定API资源，校验身份、应用、授权范围、状态、权限版本、幂等键和审计要求 | 权限编码校验服务 | 权限点API已绑定 |
| 注册TMS权限点 | TMS产品 / 权限管理员 | 围绕运输任务、运单、面单、轨迹、签收、异常和费用来源注册页面、按钮、API和敏感字段权限 | 权限编码校验服务 | 权限点已注册 |
| 停用权限点 | 子系统管理员 | 围绕权限点执行停用权限点，校验身份、应用、授权范围、状态、权限版本、幂等键和审计要求 | 权限编码校验服务 | 权限点已停用 |

## 3. 领域边界与分层架构

权限事件的位置要明确区分三层含义：领域层产生身份、授权、审批和审计事实，应用层保存聚合与事件发布表，基础设施层投递消息并消费 HR、主数据、业务系统和安全风控的外部事实。

```mermaid
flowchart TB
    CommandAPI["CommandAPI 命令接口"]
    QueryAPI["QueryAPI 查询接口"]
    EventConsumer["EventConsumer 事件消费入口"]
    AppService["AppService 权限点应用服务"]
    CommandHandler["CommandHandler 命令处理器"]
    EventHandler["EventHandler 事件处理器"]
    Aggregate["Aggregate 权限点"]
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
| permissionId | 权限点ID | 聚合根 | 否 | 注册权限点 | 全局唯一 |
| permissionCode | 权限点编码或单号 | 值对象 | 否 | 注册权限点 | 全局唯一或在应用内唯一 |
| subjectRef | 身份主体引用 | 值对象 | 是 | 登录或授权命令 | 关联用户、外部主体、组织或客户端 |
| appRef | 应用引用 | 值对象 | 是 | 接入或授权命令 | 关联子系统、客户端、菜单、API 和回调地址 |
| logisticsResourceType | 物流资源类型 | 值对象 | 是 | 注册权限点 / 变更权限点 | TMS权限点记录运输任务、运单、面单、轨迹、签收、物流异常或物流费用来源，非TMS权限为空 |
| permissionSnapshot | 权限快照 | 值对象 | 是 | 授权、登录、校验命令 | 保存角色、权限点、数据范围和权限版本 |
| status | 生命周期状态 | 值对象 | 是 | 状态推进命令 | 必须按状态机流转 |
| securitySnapshot | 安全快照 | 值对象 | 是 | 登录、风控、策略命令 | 保存 MFA、IP、设备、失败次数、风险等级和有效期 |
| operationLog | 操作记录 | 内部实体集合 | 是 | 所有写命令 | 记录操作者、原因、前后状态、权限版本和事件编号 |

## 5. 命令与应用服务逻辑

应用服务负责编排权限用例：校验权限、检查幂等、加载聚合、调用领域服务、执行聚合行为、保存聚合、写发布表、写审计日志。

| 命令 | 发起者 | 应用服务处理逻辑 | 参与领域服务 | 成功后领域事件 |
| --- | --- | --- | --- | --- |
| 注册权限点 | 子系统管理员 | 加载权限点聚合，校验账号、应用、角色、权限点、数据范围、审批状态、风险策略和幂等键，执行聚合行为并写入事件发布表 | 权限编码校验服务 | 权限点已注册 |
| 变更权限点 | 子系统管理员 | 加载权限点聚合，校验账号、应用、角色、权限点、数据范围、审批状态、风险策略和幂等键，执行聚合行为并写入事件发布表 | 权限编码校验服务 | 权限点已变更 |
| 绑定API资源 | 子系统管理员 | 加载权限点聚合，校验账号、应用、角色、权限点、数据范围、审批状态、风险策略和幂等键，执行聚合行为并写入事件发布表 | 权限编码校验服务 | 权限点API已绑定 |
| 注册TMS权限点 | TMS产品 / 权限管理员 | 加载权限点聚合，校验TMS资源类型合法、接口路径不冲突、高危动作已标记敏感级别，执行聚合行为并写入事件发布表 | 权限编码校验服务 | 权限点已注册 |
| 停用权限点 | 子系统管理员 | 加载权限点聚合，校验账号、应用、角色、权限点、数据范围、审批状态、风险策略和幂等键，执行聚合行为并写入事件发布表 | 权限编码校验服务 | 权限点已停用 |

### 5.1 应用服务通用处理模板

1. 接口层接收页面请求、SSO 请求、Token 校验请求、审批请求、审计写入或外部事件，并转换为命令对象。
2. 应用层校验用户、角色、应用、组织、数据范围、管理权限和敏感操作权限。
3. 使用 `来源系统 + 来源单号 + 命令类型 + 幂等键` 做幂等检查。
4. 通过资源库加载 `权限点` 聚合根，新建场景先校验业务唯一性。
5. 调用领域服务完成账号状态、角色授权、权限点、数据范围、安全策略、审批状态和 Token 有效性的判断。
6. 聚合根执行行为，修改属性、内部实体和值对象，并产生领域事件。
7. 同一事务保存聚合、事件发布表和操作审计。
8. 事件发布器异步投递事件，读模型投影器更新权限查询模型和缓存版本。

### 5.2 关键命令处理细节

| 关键命令 | 前置校验 | 聚合行为 | 异常或补偿处理 |
| --- | --- | --- | --- |
| 注册权限点 | 权限点处于允许状态，授权人有管理权限，权限范围合法，命令未重复 | 修改权限点状态为`已启用`，刷新权限版本或安全快照，产生`权限点已注册` | 状态不匹配则拒绝；越权授权拦截；缓存失效后要求重新拉取权限 |
| 变更权限点 | 权限点处于允许状态，授权人有管理权限，权限范围合法，命令未重复 | 修改权限点状态为`已启用`，刷新权限版本或安全快照，产生`权限点已变更` | 状态不匹配则拒绝；越权授权拦截；缓存失效后要求重新拉取权限 |
| 绑定API资源 | 权限点处于允许状态，授权人有管理权限，权限范围合法，命令未重复 | 修改权限点状态为`已启用`，刷新权限版本或安全快照，产生`权限点API已绑定` | 状态不匹配则拒绝；越权授权拦截；缓存失效后要求重新拉取权限 |
| 停用权限点 | 权限点处于允许状态，授权人有管理权限，权限范围合法，命令未重复 | 修改权限点状态为`已停用`，刷新权限版本或安全快照，产生`权限点已停用` | 状态不匹配则拒绝；越权授权拦截；缓存失效后要求重新拉取权限 |

## 6. 领域服务逻辑

| 领域服务 | 核心逻辑 |
| --- | --- |
| 权限编码校验服务 | 处理权限点中跨用户、角色、应用、权限点、数据范围、安全策略或审批实例的判断，保证最终权限、安全状态和审计链路一致。 |
| API资源绑定校验服务 | 处理权限点中跨用户、角色、应用、权限点、数据范围、安全策略或审批实例的判断，保证最终权限、安全状态和审计链路一致。 |
| 权限树兼容服务 | 处理权限点中跨用户、角色、应用、权限点、数据范围、安全策略或审批实例的判断，保证最终权限、安全状态和审计链路一致。 |

## 7. 事件产生逻辑

| 领域事件 | 触发命令 | 关键载荷 | 主要消费者 |
| --- | --- | --- | --- |
| 权限点已注册 | 注册权限点 | 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已启用`、操作者、原因、幂等键 | 业务子系统、权限缓存、审计日志、安全风控、待办中心、读模型 |
| 权限点已变更 | 变更权限点 | 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已启用`、操作者、原因、幂等键 | 业务子系统、权限缓存、审计日志、安全风控、待办中心、读模型 |
| 权限点API已绑定 | 绑定API资源 | 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已启用`、操作者、原因、幂等键 | 业务子系统、权限缓存、审计日志、安全风控、待办中心、读模型 |
| 权限点已停用 | 停用权限点 | 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已停用`、操作者、原因、幂等键 | 业务子系统、权限缓存、审计日志、安全风控、待办中心、读模型 |

### 7.1 事件生成规则

- 领域事件使用过去式命名，只表达已经发生的身份、授权、审批、安全或审计事实。
- 聚合根在业务行为成功后产生领域事件；应用服务负责收集、持久化和发布。
- 事件载荷必须包含事件编号、事件版本、发生时间、来源上下文、聚合ID、聚合版本、应用、用户、权限版本、操作者、原因和幂等键。
- 命令幂等命中时，返回原处理结果，不能重复授权、重复审批、重复签发会话或重复写入审计事实。
- 外部事件消费必须先进入事件收件箱，再由应用服务加载聚合并执行本地权限行为。

## 8. 事件订阅与消费逻辑

| 订阅事件 | 处理应用服务 | 消费后数据变化 | 幂等键 |
| --- | --- | --- | --- |
| 应用已启用 | 权限点应用服务 | 允许在该应用下注册权限点 | 来源上下文+事件编号+业务主键 |
| API资源已扫描 | 权限点应用服务 | 生成或更新权限点建议；TMS接口要识别运输任务、运单、面单、轨迹、签收、异常和费用来源资源类型 | 来源上下文+事件编号+业务主键 |
| 主数据已变更 | 权限对象同步服务 | 更新组织、仓库、货主、供应商、客户、物流商等数据范围对象快照 | 主数据上下文+事件编号+对象编码 |
| 安全风险已识别 | 安全风控消费服务 | 锁定账号、失效会话或要求重新认证 | 安全上下文+事件编号+风险编号 |

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

    User->>CommandAPI: 子系统管理员提交命令 注册权限点
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载权限点聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 权限点已注册
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 权限点已注册
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

    User->>CommandAPI: 子系统管理员提交命令 变更权限点
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载权限点聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 权限点已变更
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 权限点已变更
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

    User->>CommandAPI: 子系统管理员提交命令 绑定API资源
    CommandAPI->>AppService: 转换命令对象
    AppService->>Repository: 加载权限点聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>DomainService: 执行业务规则校验
    DomainService->>AppService: 返回规则校验结果
    AppService->>Aggregate: 调用聚合行为
    Aggregate->>AppService: 返回领域事件 权限点API已绑定
    AppService->>Repository: 保存聚合状态
    AppService->>Outbox: 写入事件发布表
    AppService->>AuditLog: 写入操作审计
    Outbox->>MessageBroker: 发布领域事件 权限点API已绑定
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

    MessageBroker->>EventConsumer: 投递外部事件 应用已启用
    EventConsumer->>Inbox: 检查事件是否已消费
    Inbox->>EventConsumer: 返回未消费
    EventConsumer->>AppService: 转换为事件消费命令
    AppService->>Repository: 加载相关聚合
    Repository->>AppService: 返回聚合当前状态
    AppService->>Aggregate: 应用外部权限事实
    Aggregate->>AppService: 允许在该应用下注册权限点
    AppService->>Repository: 保存聚合变化
    AppService->>ReadModel: 更新权限查询投影
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

    AppService->>Repository: 加载权限点
    Repository->>AppService: 返回状态 初始或上一业务状态
    AppService->>DomainService: 校验不变量 权限编码唯一且资源绑定有效
    DomainService->>AppService: 返回允许
    AppService->>Aggregate: 执行命令 注册权限点
    Aggregate->>AppService: 状态变为 已启用
    AppService->>Repository: 保存新状态
    AppService->>Outbox: 保存状态变化事件
```

## 10. 异常、补偿、幂等、权限、审计

| 类型 | 设计 |
| --- | --- |
| 异常 | 权限编码重复、API 路径冲突、菜单树循环、停用权限点仍被角色使用、字段权限和页面字段不一致。 |
| 补偿 | 支持权限缓存刷新、强制下线、重新认证、审批撤回、授权回滚、日志重试和人工安全复核 |
| 幂等 | 命令幂等键使用来源系统、来源单号、命令类型和请求流水；事件消费幂等使用事件编号和业务主键 |
| 权限 | 按应用、角色、权限点、数据范围、字段、金额阈值、审批节点和安全等级控制 |
| 审计 | 所有登录、授权、审批、Token、数据权限、敏感操作和失败拒绝都记录请求摘要、操作者、对象、前后状态、IP、设备和事件编号 |

## 11. 读模型设计

| 读模型 | 用途 | 数据来源 | 刷新方式 |
| --- | --- | --- | --- |
| 权限点列表读模型 | 列表查询、条件筛选、分页和导出 | 聚合事件投影 + 用户应用快照 | 事件投影更新 |
| 权限点详情读模型 | 查看授权、状态、版本、安全快照、审批节点和操作记录 | 聚合当前状态 + 操作日志 | 写命令后同步刷新 |
| 用户权限视图 | 子系统渲染菜单、按钮、API 和字段权限 | 用户、角色、权限点、数据权限事件汇总 | 授权事件增量刷新 |
| 权限审计视图 | 查询登录、授权、审批、拒绝访问和敏感操作 | 操作日志、会话、安全策略事件 | 只追加投影 |

## 12. 当前结论与待确认问题

当前结论：`权限点` 是权限系统领域中的关键聚合，写侧必须以聚合根保护身份、授权、数据范围、审批、安全和审计不变量，读侧使用投影支撑权限计算、菜单渲染、数据过滤和审计追溯。

关键假设：权限系统拥有身份和授权权威口径；业务系统消费权限结果，但业务状态仍由各业务上下文自己拥有。

待确认问题：审批流是否长期留在权限系统内，还是后续拆成独立流程引擎；这会影响审批实例和业务系统之间的上下文映射。
