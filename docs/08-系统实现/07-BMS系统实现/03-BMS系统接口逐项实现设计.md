# 03-BMS系统接口逐项实现设计

> 本文承接 [02-BMS系统接口事件实现逻辑](02-BMS系统接口事件实现逻辑.md)，继续把 `docs/06-子系统接口设计/07-BMS系统接口设计.md` 中的接口拆成可编码实现小节。每个接口按“接口层 -> 应用层 -> 领域层 -> 基础设施层 -> 事件/返回 -> 异常”说明。

## 1. 统一代码分层

### 1.1 包结构

```text
bms-service
  ├── interfaces
  │   ├── web
  │   ├── openapi
  │   ├── internal
  │   ├── mq
  │   └── job
  ├── application
  │   ├── command
  │   ├── query
  │   ├── event
  │   └── dto
  ├── domain
  │   ├── billingobject
  │   ├── billingrule
  │   ├── sourceevent
  │   ├── billingitem
  │   ├── adjustment
  │   ├── reconciliation
  │   ├── bill
  │   ├── invoice
  │   ├── finance
  │   └── shared
  └── infrastructure
      ├── persistence
      ├── rpc
      ├── mq
      ├── idempotency
      ├── audit
      └── file
```

### 1.2 命名约定

| 类型 | 示例 | 说明 |
| --- | --- | --- |
| Controller | `BillingRuleController` | 前端 HTTP 接口 |
| OpenApiController | `BillingSourceOpenApiController` | 来源事实和外部门户入口 |
| InternalController | `FinanceCallbackController` | 财务/发票内部回调入口 |
| QueryService | `BillingItemQueryService` | 查询读模型 |
| ApplicationService | `ReconciliationApplicationService` | 编排权限、幂等、金额校验、事务、事件 |
| Aggregate | `ReconciliationAggregate` | 保护对账状态机 |
| DomainEvent | `BillingItemGeneratedEvent` | 写入 `bms_domain_event` |

## 2. 工作台接口

### 2.1 查询工作台统计

`GET /api/bms/v1/workbench/summary`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BmsWorkbenchController.summary(WorkbenchSummaryQueryRequest)` 解析账期、结算方向、组织。 |
| 应用层 | `BmsWorkbenchQueryService.querySummary(query)` 校验 `bms:workbench:read` 和财务数据范围。 |
| 领域层 | 无聚合行为；这是读模型查询。 |
| 基础设施层 | 聚合查询计费失败、待对账、待开票、待交财务、回调失败读模型。 |
| 返回 | 待计费、计费失败、待对账、待确认账单、待开票、待入账数量。 |
| 异常 | 无权限返回 `403`；数据范围为空返回空统计。 |

### 2.2 查询待办列表

`GET /api/bms/v1/workbench/todos`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BmsWorkbenchController.todos(TodoPageQueryRequest)` 接收 `todoType/pageNo/pageSize`。 |
| 应用层 | `BmsTodoQueryService.pageTodos(query)` 校验工作台读权限和财务数据范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 从费用来源、费用明细、调整、对账、账单、发票、财务交接读模型或统一待办表查询。 |
| 返回 | `todoId`、`businessType`、`businessNo`、`billingObjectName`、`statusName`、`targetRoute`。 |
| 异常 | `pageSize` 超限制返回 `400`；数据范围为空返回空分页。 |

## 3. 计费对象和计费规则接口

### 3.1 查询计费对象列表

`GET /api/bms/v1/billing-objects`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BillingObjectController.page(BillingObjectPageQuery)` 接收对象类型、结算方向、状态和分页。 |
| 应用层 | `BillingObjectQueryService.page(query)` 校验计费对象读权限和组织范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询计费对象读模型，补充客户/供应商/物流商快照。 |
| 返回 | 对象编码、对象名称、对象类型、结算方向、税务信息状态、启停状态。 |
| 异常 | 无权限返回空分页。 |

### 3.2 创建计费对象

`POST /api/bms/v1/billing-objects`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BillingObjectController.create(CreateBillingObjectRequest)` 校验请求体和幂等键。 |
| 应用层 | `BillingObjectApplicationService.create(command)` 校验对象类型、结算方向、税务资料和编码唯一。 |
| 领域层 | `BillingObjectAggregate.create(...)` 创建计费对象，状态启用或停用。 |
| 基础设施层 | 保存计费对象，写事件和审计。 |
| 事件 | `BillingObjectCreated`。 |
| 异常 | 对象编码重复返回 `409`；税务资料不完整返回 `422`。 |

### 3.3 发布计费规则

`POST /api/bms/v1/billing-rules/{ruleCode}/publish`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BillingRuleController.publish(ruleCode, PublishBillingRuleRequest)` 接收版本、生效期和幂等键。 |
| 应用层 | `BillingRuleApplicationService.publish(command)` 校验配置权限、规则语法、税率和区间冲突。 |
| 领域层 | `BillingRuleAggregate.publish(...)` 生成不可变规则版本。 |
| 基础设施层 | 保存规则版本，刷新计费缓存，写事件和审计。 |
| 事件 | `BillingRulePublished`。 |
| 异常 | 生效区间冲突返回 `409`；规则语法错误返回 `422`。 |

## 4. 费用来源和费用明细接口

### 4.1 采集费用来源事件

`POST /openapi/bms/v1/source-events/collect`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BillingSourceOpenApiController.collect(SourceEventCollectRequest)` 校验来源系统、签名和幂等键。 |
| 应用层 | `BillingSourceEventApplicationService.collect(command)` 校验计费对象、账期、指标和来源唯一性。 |
| 领域层 | `BillingSourceEventAggregate.collect(...)` 记录来源事实；计费识别服务匹配可计费事实。 |
| 基础设施层 | 保存来源事件、处理日志、费用明细或失败原因，写事件和审计。 |
| 事件 | `BillingSourceEventCollected`、`BillableFactIdentified`、`BillingItemGenerated`。 |
| 异常 | 规则缺失进入计费失败；重复来源幂等返回历史结果。 |

### 4.2 查询费用来源列表

`GET /api/bms/v1/source-events`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BillingSourceEventController.page(SourceEventPageQuery)` 接收来源系统、账期、处理状态。 |
| 应用层 | `BillingSourceEventQueryService.page(query)` 校验费用来源读权限和对象范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询来源事件、处理状态、失败原因和关联费用明细。 |
| 返回 | 来源事件号、来源系统、来源单、处理状态、计费对象、账期。 |
| 异常 | 原始 payload 按权限脱敏。 |

### 4.3 重放费用来源

`POST /api/bms/v1/source-events/{sourceEventNo}/replay`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BillingSourceEventController.replay(sourceEventNo, ReplaySourceEventRequest)` 接收重放原因。 |
| 应用层 | `BillingSourceEventApplicationService.replay(command)` 校验事件状态、规则版本和权限。 |
| 领域层 | 来源事件聚合重新进入待处理；计费识别服务重新匹配规则。 |
| 基础设施层 | 更新处理状态，重新生成或修正费用明细，写事件和审计。 |
| 事件 | `BillingSourceEventReplayed`、必要时 `BillingItemGenerated`。 |
| 异常 | 已对账/已账单费用不可覆盖，必须走调整单。 |

### 4.4 查询费用明细

`GET /api/bms/v1/billing-items`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BillingItemController.page(BillingItemPageQuery)` 接收对象、账期、费用类型、状态。 |
| 应用层 | `BillingItemQueryService.page(query)` 校验费用明细读权限和金额字段权限。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询费用明细、规则快照、来源事件。 |
| 返回 | 费用明细号、对象、费用类型、数量、单价、金额、税额、状态。 |
| 异常 | 金额字段无权限时脱敏。 |

### 4.5 重算费用明细

`POST /api/bms/v1/billing-items/{billingItemNo}/recalculate`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BillingItemController.recalculate(billingItemNo, RecalculateBillingItemRequest)` 接收重算原因。 |
| 应用层 | `BillingItemApplicationService.recalculate(command)` 校验费用未锁定或审批通过，重新匹配规则。 |
| 领域层 | `BillingItemAggregate.recalculate(...)` 更新数量、单价、金额、税额和规则版本。 |
| 基础设施层 | 保存重算结果，写事件和审计。 |
| 事件 | `BillingItemRecalculated`。 |
| 异常 | 已对账确认不可直接重算，返回 `409`。 |

### 4.6 作废费用明细

`POST /api/bms/v1/billing-items/{billingItemNo}/void`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BillingItemController.voidItem(billingItemNo, VoidBillingItemRequest)` 接收作废原因。 |
| 应用层 | `BillingItemApplicationService.voidItem(command)` 校验状态、权限和幂等。 |
| 领域层 | `BillingItemAggregate.voidItem(...)` 状态 -> 已作废，记录原因。 |
| 基础设施层 | 保存作废状态，写事件和审计。 |
| 事件 | `BillingItemVoided`。 |
| 异常 | 已交财务不可作废，只能负向调整。 |

## 5. 调整、对账和账单接口

### 5.1 执行费用调整单

`POST /api/bms/v1/adjustments/{adjustmentNo}/execute`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BillingAdjustmentController.execute(adjustmentNo, ExecuteAdjustmentRequest)` 接收版本和幂等键。 |
| 应用层 | `BillingAdjustmentApplicationService.execute(command)` 校验已审批、金额方向和关联费用状态。 |
| 领域层 | `BillingAdjustmentAggregate.execute(...)` 生成正负向费用明细或修正明细状态。 |
| 基础设施层 | 保存调整和费用明细，写事件和审计。 |
| 事件 | `BillingAdjustmentExecuted`、`BillingItemGenerated`。 |
| 异常 | 已执行不可重复；调整金额为 0 返回 `422`。 |

### 5.2 生成对账单

`POST /api/bms/v1/reconciliations/generate`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `ReconciliationController.generate(GenerateReconciliationRequest)` 接收对象、账期、方向和幂等键。 |
| 应用层 | `ReconciliationApplicationService.generate(command)` 查询待对账费用并校验并发生成。 |
| 领域层 | `ReconciliationAggregate.generate(...)` 锁定费用明细范围，汇总金额。 |
| 基础设施层 | 保存对账单、对账明细和费用锁定状态，写事件和审计。 |
| 事件 | `ReconciliationGenerated`。 |
| 异常 | 无可对账费用返回 `422`；同账期并发生成返回 `409`。 |

### 5.3 确认对账单

`POST /api/bms/v1/reconciliations/{reconciliationNo}/confirm`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `ReconciliationController.confirm(reconciliationNo, ConfirmReconciliationRequest)` 接收确认金额和意见。 |
| 应用层 | `ReconciliationApplicationService.confirm(command)` 校验确认权限、金额一致性和差异处理状态。 |
| 领域层 | `ReconciliationAggregate.confirm(...)` 状态 -> 已确认，费用明细进入已确认。 |
| 基础设施层 | 更新对账和费用明细状态，写事件和审计。 |
| 事件 | `ReconciliationConfirmed`。 |
| 异常 | 存在未处理差异返回 `409`。 |

### 5.4 反馈对账差异

`POST /api/bms/v1/reconciliations/{reconciliationNo}/differences`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `ReconciliationController.createDifference(reconciliationNo, ReconciliationDifferenceRequest)` 接收差异金额、原因和证据。 |
| 应用层 | `ReconciliationApplicationService.createDifference(command)` 校验差异权限和金额范围。 |
| 领域层 | `ReconciliationAggregate.createDifference(...)` 记录差异和责任方。 |
| 基础设施层 | 保存差异明细、附件、事件和审计。 |
| 事件 | `ReconciliationDifferenceCreated`。 |
| 异常 | 差异金额超过总额返回 `422`；证据缺失返回 `422`。 |

### 5.5 生成账单

`POST /api/bms/v1/bills/generate`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BillController.generate(GenerateBillRequest)` 接收对账单范围和幂等键。 |
| 应用层 | `BillApplicationService.generate(command)` 校验对账已确认、账单未重复生成。 |
| 领域层 | `BillAggregate.generate(...)` 基于确认对账生成账单，锁定账单金额。 |
| 基础设施层 | 保存账单和账单明细，写事件和审计。 |
| 事件 | `BillGenerated`。 |
| 异常 | 未确认对账不可生成账单。 |

### 5.6 确认账单

`POST /api/bms/v1/bills/{billNo}/confirm`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BillController.confirm(billNo, ConfirmBillRequest)` 接收确认意见。 |
| 应用层 | `BillApplicationService.confirm(command)` 校验账单确认权限、金额和幂等。 |
| 领域层 | `BillAggregate.confirm(...)` 状态 -> 已确认。 |
| 基础设施层 | 更新账单状态，写事件和审计。 |
| 事件 | `BillConfirmed`。 |
| 异常 | 已关闭或已交财务账单返回 `409`。 |

## 6. 发票、财务和外部回调接口

### 6.1 请求开票

`POST /api/bms/v1/bills/{billNo}/invoice-request`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `InvoiceController.requestInvoice(billNo, InvoiceRequest)` 接收开票资料。 |
| 应用层 | `InvoiceApplicationService.requestInvoice(command)` 校验账单已确认、开票资料完整和幂等。 |
| 领域层 | `InvoiceAggregate.request(...)` 创建发票交接，账单进入待开票。 |
| 基础设施层 | 保存发票交接，调用发票系统或生成待办，写事件和审计。 |
| 事件 | `InvoiceRequested`。 |
| 异常 | 重复请求幂等返回原发票交接号。 |

### 6.2 回填发票

`POST /internal/bms/v1/invoices/{invoiceNo}/issued`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `InvoiceInternalController.issued(invoiceNo, InvoiceIssuedCallback)` 校验内部系统签名和幂等键。 |
| 应用层 | `InvoiceApplicationService.issued(command)` 校验发票号码、金额、税额和账单匹配。 |
| 领域层 | `InvoiceAggregate.issued(...)` 状态 -> 已开票，记录发票号和附件。 |
| 基础设施层 | 保存发票结果，更新账单开票状态，写事件和审计。 |
| 事件 | `InvoiceIssued`。 |
| 异常 | 金额不匹配返回 `422`；重复回调幂等返回历史结果。 |

### 6.3 交财务

`POST /api/bms/v1/bills/{billNo}/handover-finance`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `FinanceHandoverController.handover(billNo, HandoverFinanceRequest)` 接收交接说明。 |
| 应用层 | `FinanceHandoverApplicationService.handover(command)` 校验账单确认、发票要求和幂等。 |
| 领域层 | `FinanceHandoverAggregate.request(...)` 创建财务交接，账单进入待入账。 |
| 基础设施层 | 保存财务交接，调用财务系统，写事件和审计。 |
| 事件 | `FinanceHandoverRequested`。 |
| 异常 | 财务系统失败进入待重推，不关闭账单。 |

### 6.4 财务入账回调

`POST /internal/bms/v1/finance-handovers/{handoverNo}/posted`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `FinanceInternalController.posted(handoverNo, FinancePostedCallback)` 校验内部签名和幂等键。 |
| 应用层 | `FinanceHandoverApplicationService.posted(command)` 校验凭证号唯一、金额匹配和交接状态。 |
| 领域层 | `FinanceHandoverAggregate.completed(...)` 状态 -> 已入账，账单和费用明细进入已入账。 |
| 基础设施层 | 保存凭证、更新账单/费用状态，写事件和审计。 |
| 事件 | `FinanceHandoverCompleted`。 |
| 异常 | 凭证号重复返回 `409`；金额不匹配进入人工处理。 |

### 6.5 OMS 退款结算请求

`POST /openapi/bms/v1/refund-requests`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `RefundOpenApiController.receive(RefundRequest)` 校验 OMS 签名和幂等键。 |
| 应用层 | `RefundSettlementApplicationService.receive(command)` 校验售后单、退款金额、结算对象。 |
| 领域层 | 来源事件聚合记录退款事实，费用明细聚合生成负向费用或交财务记录。 |
| 基础设施层 | 保存退款来源、费用明细、事件和审计。 |
| 事件 | `RefundSettlementAccepted`。 |
| 异常 | 金额不匹配进入异常待办；重复退款幂等返回历史结果。 |

## 7. 报表、日志、枚举和事件入口

### 7.1 查询结算汇总报表

`GET /api/bms/v1/reports/settlement-summary`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BmsReportController.settlementSummary(SettlementSummaryQuery)` 接收账期、对象、方向。 |
| 应用层 | `BmsReportQueryService.querySettlementSummary(query)` 校验报表权限和对象范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询报表汇总表或 OLAP 读模型。 |
| 返回 | 应收/应付金额、已对账、已开票、已入账、差异金额。 |
| 异常 | 报表口径标记统计时间和版本。 |

### 7.2 查询操作日志

`GET /api/bms/v1/operation-logs`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BmsOperationLogController.page(OperationLogPageQuery)` 接收对象类型、对象单号、操作人和时间。 |
| 应用层 | `BmsOperationLogQueryService.page(query)` 校验审计权限和财务数据范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询 `bms_operation_audit_log` 或 ES 审计索引。 |
| 返回 | 操作时间、操作人、操作类型、前后值、traceId。 |
| 异常 | 查询跨度过大返回 `400`。 |

### 7.3 查询枚举项

`GET /api/bms/v1/enums`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BmsEnumController.list(EnumQuery)` 接收枚举类型。 |
| 应用层 | `BmsEnumQueryService.list(query)` 校验枚举读权限。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询枚举配置表和缓存。 |
| 返回 | 枚举编码、名称、排序、启停状态。 |
| 异常 | 枚举类型不存在返回空列表。 |

### 7.4 通用事件入口

`POST /internal/bms/v1/events`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `BmsEventController.receive(EventEnvelope)` 校验事件信封、来源系统和版本。 |
| 应用层 | `BmsEventDispatchService.dispatch(event)` 先写 `bms_event_consume_log`，再分发 WMS/OMS/TMS/库存/主数据事件处理器。 |
| 领域层 | 来源事件聚合采集费用事实，费用明细聚合生成费用。 |
| 基础设施层 | 保存 Inbox、来源事件、费用明细和审计。 |
| 返回 | `eventId`、`consumeStatus`。 |
| 异常 | 重复事件返回历史结果；规则缺失进入计费失败待办。 |

## 继续上下文

当前结论：BMS `03` 已按供应商接口逐项格式展开，覆盖工作台、计费对象、计费规则、费用来源、费用明细、调整、对账、账单、发票、财务、退款、报表、日志、枚举和事件入口。  
关键假设：BMS 只拥有结算处理结果，不拥有仓储、订单、运输和库存原始事实。  
待决问题：税率/汇率事实源、财务回调协议、外部门户签名方式。  
下一步：可继续补充每个接口的请求/响应 DTO 字段。
