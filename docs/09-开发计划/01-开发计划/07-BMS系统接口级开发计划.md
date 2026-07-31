# BMS 系统接口级开发计划

> 当前状态与剩余验收见[供应链系统开发总纲](../00-供应链系统开发总纲.md#5-九子系统当前状态)。

实现资料：`docs/08-系统实现/07-BMS系统实现/03-BMS系统接口逐项实现设计.md`。

## BMS-API-001 计费对象与计费规则
`GET/POST /billing-subjects`、`POST /billing-rules/{id}/publish`

- 接口层：`BillingSubjectController`、`BillingRuleController` 校验组织、币种、计费项、版本和发布权限。
- 应用层：计费对象/规则服务校验适用范围和规则版本，发布后创建只读规则快照。
- 领域层：`BillingSubjectAggregate`、`BillingRuleAggregate` 保证规则生效期不重叠、阶梯和公式合法、已发布版本不可覆盖。
- 基础设施层：对象/规则资源库、规则版本表、审批 ACL、Outbox。
- 事件：`BillingRulePublished`；费用计算服务消费快照。

```mermaid
flowchart LR
  U[计费管理员] --> C[BillingRuleController]
  C --> A[BillingRuleApplicationService]
  A --> G[BillingRuleAggregate]
  G --> V[规则版本快照]
  V --> E[BillingRulePublished]
```

## BMS-API-002 采集/查询/重放费用来源
`POST /openapi/charge-sources`、`GET /charge-sources`、`POST /charge-sources/{id}/replay`

- 接口层：`ChargeSourceOpenApiController` 校验来源系统、业务单号、费用维度和事件编码；查询 Controller 提供分页/范围。
- 应用层：`ChargeSourceApplicationService` 先 Inbox 幂等再标准化来源；重放服务读取失败载荷。
- 领域层：`ChargeSourceAggregate` 保证同来源事件只形成一次费用事实，状态包含待计算/已计算/失败。
- 基础设施层：来源资源库、Inbox、失败表、Outbox。
- 事件：消费 TMS/WMS/采购/OMS 费用事实；生产 `ChargeSourceAccepted/Failed`。

```mermaid
sequenceDiagram
  participant S as 来源系统
  participant C as ChargeSourceOpenApiController
  participant I as Inbox
  participant G as ChargeSourceAggregate
  participant O as Outbox
  S->>C: 费用来源事件
  C->>I: 幂等声明
  I->>G: 创建费用来源
  G->>O: ChargeSourceAccepted
```

## BMS-API-003 费用明细重算/作废与调整
`GET /charges`、`POST /charges/{id}/recalculate|void`、`POST /charge-adjustments/{id}/execute`

- 接口层：`ChargeDetailController`、`ChargeAdjustmentController` 接收版本、调整原因、附件和审批结论。
- 应用层：计算服务加载发布规则和来源快照；调整服务校验权限、审批和原费用状态。
- 领域层：`ChargeDetailAggregate` 固化计算依据；`ChargeAdjustmentAggregate` 不允许直接改原金额，必须产生正/负调整明细。
- 基础设施层：费用明细/调整资源库、规则快照、审计、Outbox。
- 事件：`ChargeCalculated/Recalculated/Voided/Adjusted`；对账单投影消费。

```mermaid
flowchart LR
  S[费用来源] --> C[计费规则快照]
  C --> D[ChargeDetailAggregate]
  D --> A[调整单聚合]
  A --> E[费用变更事件]
```

## BMS-API-004 对账、账单与发票财务
`POST /reconciliation-statements`、`POST /{id}/confirm|difference`、`POST /bills`、`POST /invoices`、`POST /financial-handovers`、财务回调。

- 接口层：对账、账单、发票、财务交接 Controller；财务回调 OpenAPI 校验来源和事件编码。
- 应用层：对账服务比较费用明细与对方确认；账单服务汇总已确认费用；发票服务校验税额；交接服务调用 ERP ACL。
- 领域层：对账单、账单、发票交接、财务交接聚合保证金额守恒、差异未处理不可生成账单、未交财务不可关闭账单。
- 基础设施层：各资源库、税务/ERP ACL、Inbox/Outbox、审计和附件存储。
- 事件：`ReconciliationIssued/Confirmed/DifferenceRaised`、`BillGenerated`、`InvoiceValidated`、`FinancialPosted`；供应商/采购/OMS 消费。

```mermaid
flowchart LR
  D[费用明细] --> R[对账单]
  R --> X{无差异?}
  X -- 是 --> B[账单]
  X -- 否 --> H[差异处理]
  B --> I[发票]
  I --> F[财务交接]
```

## BMS-API-005 退款结算、报表与通用事件
`POST /refund-settlements`、`GET /refund-settlements/{refundNo}`、`POST /refund-settlements/{refundNo}/retry|close`、`GET /reports/settlement-summary`、`POST /events`

- 接口层：`RefundSettlementController` 校验原支付/账单、售后单、退款金额、币种、幂等键和版本；重试/人工关闭需要高风险权限和原因。`BmsReportController` 和事件入口只操作读模型或 Inbox。
- 应用层：退款结算服务加载 OMS 售后快照和原账单/支付事实，执行创建、提交支付、消费回执、失败重试和人工补偿；报表查询服务使用读模型。
- 领域层：`RefundSettlementAggregate` 保护累计退款不超过可退金额、币种一致、终态不可重提、支付回执只应用一次。状态为 `REQUESTED/CONFIRMATION_PENDING/FINISHED/FAILED/CLOSED`。
- 基础设施层：退款资源库、退款累计投影、OMS/支付/ERP ACL、回执 Inbox、Outbox、失败记录和审计。
- 事件：消费 `RefundRequested` 和支付回执；生产 `RefundSettlementRequested/RefundConfirmationPending/RefundCompleted/RefundFailed/RefundRetryRequested/RefundManuallyClosed`。
- 补偿：支付超时进入待确认并主动查询；失败可按同一退款业务键重试但不得重复增加累计退款；人工关闭必须记录未退款金额和责任原因。
- 测试：并发退款、重复请求、重复/乱序回执、超额退款、币种不一致、超时查询和人工关闭。
- 设计事实源：[退款结算聚合 CQRS 设计](../../03-核心业务模型/07-BMS领域模型/11-退款结算聚合CQRS设计.md)。规范状态为 `REQUESTED/CONFIRMATION_PENDING/FINISHED/FAILED/CLOSED`；超时待确认继续占额，人工处置需凭证与双人复核。

```mermaid
flowchart LR
  OMS[OMS退款请求] --> R[退款结算聚合]
  R --> ERP[ERP或支付]
  ERP --> I[财务回执Inbox]
  I --> R
```
