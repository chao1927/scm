# 03-OMS系统接口逐项实现设计

> 本文承接 [02-OMS系统接口事件实现逻辑](02-OMS系统接口事件实现逻辑.md)，继续把 `docs/06-子系统接口设计/05-OMS系统接口设计.md` 中的接口拆成可编码实现小节。每个接口按“接口层 -> 应用层 -> 领域层 -> 基础设施层 -> 事件/返回 -> 异常”说明。

## 1. 统一代码分层

### 1.1 包结构

```text
oms-service
  ├── interfaces
  │   ├── web
  │   ├── openapi
  │   ├── mq
  │   └── job
  ├── application
  │   ├── command
  │   ├── query
  │   ├── event
  │   └── dto
  ├── domain
  │   ├── channel
  │   ├── salesorder
  │   ├── audit
  │   ├── fulfillment
  │   ├── outbound
  │   ├── cancel
  │   ├── aftersale
  │   ├── exception
  │   ├── rule
  │   └── shared
  └── infrastructure
      ├── persistence
      ├── rpc
      ├── mq
      ├── idempotency
      ├── audit
      └── channel
```

### 1.2 命名约定

| 类型 | 示例 | 说明 |
| --- | --- | --- |
| Controller | `SalesOrderController` | 前端 HTTP 接口 |
| OpenApiController | `OmsChannelOrderOpenApiController` | 渠道/客服接入入口 |
| QueryService | `SalesOrderQueryService` | 查询读模型 |
| ApplicationService | `SalesOrderApplicationService` | 编排权限、幂等、事务、外部命令、事件 |
| Aggregate | `SalesOrderAggregate` | 保护订单状态、数量、金额不变量 |
| DomainEvent | `SalesOrderApprovedEvent` | 写入 `oms_domain_event` |

## 2. 工作台接口

### 2.1 查询工作台统计

`GET /api/oms/v1/workbench/summary`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `OmsWorkbenchController.summary(WorkbenchSummaryQueryRequest)` 解析组织、渠道、店铺、时间范围。 |
| 应用层 | `OmsWorkbenchQueryService.querySummary(query)` 校验 `oms:workbench:read`，追加组织、渠道、店铺数据范围。 |
| 领域层 | 无聚合行为；这是读模型查询。 |
| 基础设施层 | 聚合查询待审单、待预占、待下发、取消、售后、异常读模型。 |
| 返回 | 待审单数、预占失败数、WMS 下发失败数、取消待处理数、售后待处理数、异常数。 |
| 异常 | 无权限返回 `403`；数据范围为空返回空统计。 |

### 2.2 查询待办列表

`GET /api/oms/v1/workbench/todos`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `OmsWorkbenchController.todos(TodoPageQueryRequest)` 接收 `todoType/pageNo/pageSize`。 |
| 应用层 | `OmsTodoQueryService.pageTodos(query)` 校验工作台读权限和订单数据范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 从审单、预占、出库、取消、售后、异常读模型或统一待办表查询。 |
| 返回 | `todoId`、`businessType`、`businessNo`、`title`、`statusName`、`targetRoute`。 |
| 异常 | `pageSize` 超限制返回 `400`；数据范围为空返回空分页。 |

## 3. 渠道订单和销售订单接口

### 3.1 接入渠道订单

`POST /openapi/oms/v1/channel-orders/import`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `OmsChannelOrderOpenApiController.importOrder(ChannelOrderImportRequest)` 校验渠道签名、店铺和幂等键。 |
| 应用层 | `ChannelOrderApplicationService.importOrder(command)` 保存原始报文，解析渠道订单并校验渠道配置。 |
| 领域层 | `ChannelOrderAggregate.accept(...)` 记录接入结果；`SalesOrderAggregate.createFromChannel(...)` 创建销售订单。 |
| 基础设施层 | 保存接入记录、销售订单头行、事件和审计。 |
| 事件 | `ChannelOrderImported`、`SalesOrderCreated`。 |
| 异常 | 解析失败写失败记录，不丢原文；重复订单返回历史结果。 |

### 3.2 查询渠道订单列表

`GET /api/oms/v1/channel-orders`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `ChannelOrderController.page(ChannelOrderPageQuery)` 接收渠道、店铺、状态、时间和分页。 |
| 应用层 | `ChannelOrderQueryService.page(query)` 校验渠道/店铺/组织范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询接入记录、解析状态和关联销售订单。 |
| 返回 | 接入单号、外部订单号、解析状态、关联销售订单、失败原因。 |
| 异常 | 原始报文字段按权限脱敏。 |

### 3.3 重试渠道订单解析

`POST /api/oms/v1/channel-orders/{importNo}/retry`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `ChannelOrderController.retry(importNo, RetryImportRequest)` 接收重试原因和幂等键。 |
| 应用层 | `ChannelOrderApplicationService.retry(command)` 校验状态为解析失败或可重试。 |
| 领域层 | `ChannelOrderAggregate.retry(...)` 记录重试次数和配置版本。 |
| 基础设施层 | 重新解析原始报文，成功时创建/更新销售订单，写审计。 |
| 事件 | 成功时写 `SalesOrderCreated` 或 `ChannelOrderRetried`。 |
| 异常 | 原始报文缺失返回 `422`；已生成订单不可重复重试。 |

### 3.4 查询销售订单列表

`GET /api/oms/v1/sales-orders`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `SalesOrderController.page(SalesOrderPageQuery)` 接收渠道、客户、状态、时间和分页。 |
| 应用层 | `SalesOrderQueryService.page(query)` 校验 `oms:sales_order:read` 和组织/渠道/店铺范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询销售订单读模型，聚合审单、履约、售后摘要。 |
| 返回 | 销售订单号、渠道订单号、客户、金额、订单状态、履约状态、售后状态。 |
| 异常 | 金额字段无权限时脱敏。 |

### 3.5 查询销售订单详情

`GET /api/oms/v1/sales-orders/{salesOrderNo}`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `SalesOrderController.detail(salesOrderNo)`。 |
| 应用层 | `SalesOrderQueryService.getDetail(no)` 校验订单归属组织/渠道/店铺范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询订单头行、审单、履约、预占、出库、物流、售后、操作日志。 |
| 返回 | 订单详情、明细、状态时间线、履约轨迹。 |
| 异常 | 不存在或无权限返回 `404`。 |

### 3.6 审核销售订单

`POST /api/oms/v1/sales-orders/{salesOrderNo}/approve`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `SalesOrderController.approve(salesOrderNo, ApproveSalesOrderRequest)` 接收版本和幂等键。 |
| 应用层 | `SalesOrderApplicationService.approve(command)` 执行地址、库存、风控、金额、黑名单等审单规则。 |
| 领域层 | `SalesOrderAggregate.approve(...)` 状态待审核 -> 已审核/待预占，记录审单结果。 |
| 基础设施层 | 保存订单状态、审单结果、事件和审计。 |
| 事件 | `SalesOrderApproved`；阻断时写 `SalesOrderAuditBlocked`。 |
| 异常 | 审单失败进入异常或驳回，不直接下发履约。 |

### 3.7 取消销售订单

`POST /api/oms/v1/sales-orders/{salesOrderNo}/cancel`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `SalesOrderController.cancel(salesOrderNo, CancelSalesOrderRequest)` 接收取消原因和幂等键。 |
| 应用层 | `SalesOrderApplicationService.cancel(command)` 校验取消权限、订单状态和下游履约阶段。 |
| 领域层 | `SalesOrderAggregate.cancel(...)` 未发货可取消；已发货需转售后。 |
| 基础设施层 | 更新订单/履约状态，调用库存释放或 WMS 取消，写事件和审计。 |
| 事件 | `SalesOrderCanceled` 或 `CancelRequestCreated`。 |
| 异常 | 已发货普通取消返回 `409`。 |

## 4. 审单与履约接口

### 4.1 查询审单结果

`GET /api/oms/v1/audit-results`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `OrderAuditController.page(AuditResultPageQuery)` 接收订单号、规则类型、处理状态。 |
| 应用层 | `OrderAuditQueryService.page(query)` 校验审单读权限和订单数据范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询审单结果、规则命中和人工处理记录。 |
| 返回 | 审单结果、阻断规则、处理建议、处理状态。 |
| 异常 | 高风险规则详情按权限脱敏。 |

### 4.2 人工通过审单

`POST /api/oms/v1/audit-results/{resultId}/approve`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `OrderAuditController.approve(resultId, AuditApproveRequest)` 接收处理意见。 |
| 应用层 | `OrderAuditApplicationService.approve(command)` 校验人工审核权限和幂等。 |
| 领域层 | 审单结果聚合标记已人工通过；销售订单聚合推进到待预占。 |
| 基础设施层 | 更新审单结果和订单状态，写事件和审计。 |
| 事件 | `OrderAuditApproved`。 |
| 异常 | 非阻断项不可人工通过，返回 `422`。 |

### 4.3 分仓履约

`POST /api/oms/v1/sales-orders/{salesOrderNo}/allocate`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `FulfillmentController.allocate(salesOrderNo, AllocateRequest)` 接收分仓策略和幂等键。 |
| 应用层 | `FulfillmentApplicationService.allocateWarehouse(command)` 校验订单已审核、仓库策略和库存查询结果。 |
| 领域层 | `FulfillmentAggregate.create(...)` 生成履约单，记录履约仓和物流产品。 |
| 基础设施层 | 保存履约单，写事件和审计。 |
| 事件 | `FulfillmentOrderCreated`、`FulfillmentWarehouseAllocated`。 |
| 异常 | 无可用仓生成履约异常，不直接失败订单。 |

### 4.4 换仓

`POST /api/oms/v1/fulfillments/{fulfillmentOrderNo}/change-warehouse`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `FulfillmentController.changeWarehouse(fulfillmentOrderNo, ChangeWarehouseRequest)` 接收目标仓和原因。 |
| 应用层 | `FulfillmentApplicationService.changeWarehouse(command)` 校验履约未发货、原预占可释放和目标仓可履约。 |
| 领域层 | `FulfillmentAggregate.changeWarehouse(...)` 更新履约仓，记录换仓原因。 |
| 基础设施层 | 调用库存释放/重新预占，保存履约状态，写事件和审计。 |
| 事件 | `FulfillmentWarehouseChanged`。 |
| 异常 | WMS 已出库不可换仓，返回 `409`。 |

### 4.5 拆分履约单

`POST /api/oms/v1/fulfillments/{fulfillmentOrderNo}/split`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `FulfillmentController.split(fulfillmentOrderNo, SplitFulfillmentRequest)` 接收拆分明细。 |
| 应用层 | `FulfillmentApplicationService.split(command)` 校验拆分权限、数量守恒和下游状态。 |
| 领域层 | `FulfillmentAggregate.split(...)` 生成子履约单，保证总数量不变。 |
| 基础设施层 | 保存子履约单、来源关系、事件和审计。 |
| 事件 | `FulfillmentSplit`。 |
| 异常 | 已发货不可拆分；拆分数量不平返回 `422`。 |

## 5. 库存预占和出库接口

### 5.1 请求库存预占

`POST /api/oms/v1/fulfillments/{fulfillmentOrderNo}/reserve`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `StockReservationController.reserve(fulfillmentOrderNo, ReserveRequest)` 接收幂等键。 |
| 应用层 | `OmsStockReservationApplicationService.reserve(command)` 校验履约待预占，调用中央库存预占命令。 |
| 领域层 | 履约聚合记录预占请求状态；预占事实由中央库存事件回传。 |
| 基础设施层 | 保存预占引用，调用 `InventoryReservationRpcClient`，写事件和审计。 |
| 事件 | `StockReservationRequested`。 |
| 异常 | 库存预占失败进入缺货异常或待重试。 |

### 5.2 释放库存预占

`POST /api/oms/v1/reservations/{reservationRefNo}/release`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `StockReservationController.release(reservationRefNo, ReleaseReservationRequest)` 接收释放原因。 |
| 应用层 | `OmsStockReservationApplicationService.release(command)` 校验取消/换仓/售后场景和幂等。 |
| 领域层 | 履约聚合记录释放请求，不直接改库存。 |
| 基础设施层 | 调用中央库存释放命令，写释放请求事件和审计。 |
| 事件 | `StockReservationReleaseRequested`。 |
| 异常 | 中央库存释放失败进入补偿任务。 |

### 5.3 下发 WMS 出库

`POST /api/oms/v1/outbounds/{outboundOrderNo}/release`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `OmsOutboundController.release(outboundOrderNo, ReleaseOutboundRequest)` 接收版本和幂等键。 |
| 应用层 | `OmsOutboundApplicationService.release(command)` 校验已预占、履约仓、出库状态和 WMS 配置。 |
| 领域层 | OMS 出库引用聚合状态 -> 已下发/下发中。 |
| 基础设施层 | 调用 WMS 创建出库单，保存 WMS 引用，写事件和审计。 |
| 事件 | `OutboundOrderReleased`。 |
| 异常 | WMS 失败生成重推待办，不回滚履约。 |

### 5.4 取消 WMS 出库

`POST /api/oms/v1/outbounds/{outboundOrderNo}/cancel`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `OmsOutboundController.cancel(outboundOrderNo, CancelOutboundRequest)` 接收取消原因。 |
| 应用层 | `OmsOutboundApplicationService.cancel(command)` 校验出库阶段，调用 WMS 拦截。 |
| 领域层 | 出库引用聚合根据 WMS 结果推进已取消、取消中或取消失败。 |
| 基础设施层 | 调用 WMS 取消接口，写事件和审计。 |
| 事件 | `OutboundCancelRequested`、`OutboundCanceled`。 |
| 异常 | WMS 已发货则取消失败，转售后建议。 |

### 5.5 重推 WMS 出库

`POST /api/oms/v1/outbounds/{outboundOrderNo}/repush`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `OmsOutboundController.repush(outboundOrderNo, RepushRequest)` 接收重推原因。 |
| 应用层 | `OmsOutboundApplicationService.repush(command)` 校验同步失败或 WMS 未接收状态。 |
| 领域层 | 出库引用聚合增加重推次数，保持业务状态。 |
| 基础设施层 | 重新调用 WMS，按来源单幂等回填 WMS 单号。 |
| 事件 | `OutboundRepushed`。 |
| 异常 | WMS 已存在但本地缺引用时先查询回填，不重复创建。 |

## 6. 取消、售后、异常和规则接口

### 6.1 创建取消申请

`POST /api/oms/v1/cancel-requests`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `CancelRequestController.create(CreateCancelRequestRequest)` 校验订单、取消原因和幂等键。 |
| 应用层 | `CancelRequestApplicationService.create(command)` 校验订单状态、发起方和可拦截阶段。 |
| 领域层 | `CancelRequestAggregate.create(...)` 创建取消申请并记录影响履约。 |
| 基础设施层 | 保存取消申请，写事件和审计。 |
| 事件 | `CancelRequestCreated`。 |
| 异常 | 已发货订单不可普通取消，返回 `409`。 |

### 6.2 审批取消申请

`POST /api/oms/v1/cancel-requests/{cancelRequestNo}/approve`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `CancelRequestController.approve(cancelRequestNo, ApproveCancelRequest)` 接收审批结论。 |
| 应用层 | `CancelRequestApplicationService.approve(command)` 编排释放库存、取消 WMS、关闭履约。 |
| 领域层 | 取消申请聚合状态 -> 已批准/处理中；销售订单聚合推进取消。 |
| 基础设施层 | 调用库存/WMS，保存结果，写事件和审计。 |
| 事件 | `CancelRequestApproved`。 |
| 异常 | 下游部分失败时取消单保持处理中并进入补偿。 |

### 6.3 创建售后单

`POST /api/oms/v1/after-sales`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `AfterSaleController.create(CreateAfterSaleRequest)` 校验订单、售后类型、数量和幂等键。 |
| 应用层 | `AfterSaleApplicationService.create(command)` 校验可售后数量、签收状态和客户范围。 |
| 领域层 | `AfterSaleAggregate.create(...)` 创建售后单，锁定售后数量。 |
| 基础设施层 | 保存售后头行，写事件和审计。 |
| 事件 | `AfterSaleCreated`。 |
| 异常 | 超过可售后数量返回 `422`。 |

### 6.4 审核售后单

`POST /api/oms/v1/after-sales/{afterSaleNo}/approve`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `AfterSaleController.approve(afterSaleNo, ApproveAfterSaleRequest)` 接收审核结论。 |
| 应用层 | `AfterSaleApplicationService.approve(command)` 按售后类型编排退款、退货入库或补发。 |
| 领域层 | `AfterSaleAggregate.approve(...)` 状态 -> 已审核/处理中。 |
| 基础设施层 | 调用 WMS/BMS 或创建补发履约，写事件和审计。 |
| 事件 | `AfterSaleApproved`。 |
| 异常 | 退款或退货入库部分失败进入补偿。 |

### 6.5 发起退款

`POST /api/oms/v1/after-sales/{afterSaleNo}/refund`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `AfterSaleController.refund(afterSaleNo, RefundRequest)` 接收退款金额和原因。 |
| 应用层 | `AfterSaleApplicationService.refund(command)` 校验售后状态、退款权限和金额。 |
| 领域层 | 售后聚合记录退款请求，不拥有财务入账事实。 |
| 基础设施层 | 调用 BMS 退款结算接口，保存退款引用，写事件和审计。 |
| 事件 | `RefundRequested`。 |
| 异常 | BMS 失败生成退款补偿任务。 |

### 6.6 处理订单异常

`POST /api/oms/v1/exceptions/{exceptionNo}/process`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `OrderExceptionController.process(exceptionNo, ProcessExceptionRequest)` 接收处理方案。 |
| 应用层 | `OrderExceptionApplicationService.process(command)` 校验责任人、处理权限和关联单据。 |
| 领域层 | `OrderExceptionAggregate.process(...)` 推进异常状态，记录处理结果。 |
| 基础设施层 | 保存异常处理记录、事件和审计。 |
| 事件 | `OrderExceptionProcessed`。 |
| 异常 | 未解决下游失败不可关闭。 |

### 6.7 发布 OMS 规则

`POST /api/oms/v1/rules/{ruleCode}/publish`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `OmsRuleController.publish(ruleCode, PublishRuleRequest)` 接收规则版本和发布说明。 |
| 应用层 | `OmsRuleApplicationService.publish(command)` 校验配置权限、规则语法和冲突范围。 |
| 领域层 | `OmsRuleAggregate.publish(...)` 生成不可变规则版本，只影响新审单/履约。 |
| 基础设施层 | 保存规则版本，刷新缓存，写事件和审计。 |
| 事件 | `OmsRulePublished`。 |
| 异常 | 规则语法错误返回 `422`；已发布版本不可原地修改。 |

## 7. 外部查询和事件入口

### 7.1 查询外部订单状态

`GET /openapi/oms/v1/sales-orders/{salesOrderNo}/status`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `OmsSalesOrderOpenApiController.status(salesOrderNo)` 校验渠道/客服系统签名和授权。 |
| 应用层 | `SalesOrderOpenQueryService.getStatus(query)` 校验订单归属渠道或客户范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询订单、履约、出库、售后读模型。 |
| 返回 | 订单状态、履约状态、出库状态、售后状态、更新时间。 |
| 异常 | 越权返回 `404`，避免泄露订单存在性。 |

### 7.2 查询履约轨迹

`GET /openapi/oms/v1/sales-orders/{salesOrderNo}/trace`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `OmsSalesOrderOpenApiController.trace(salesOrderNo)` 校验来源系统授权。 |
| 应用层 | `SalesOrderTraceQueryService.getTrace(query)` 组装订单、库存、WMS、TMS、售后事件链路。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询本地履约轨迹读模型，必要时补 TMS 轨迹快照。 |
| 返回 | 时间线节点、节点类型、节点状态、发生时间、来源系统。 |
| 异常 | TMS 超时返回本地最后轨迹并标记 `traceFresh=false`。 |

### 7.3 通用事件入口

`POST /internal/oms/v1/events`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `OmsEventController.receive(EventEnvelope)` 校验事件信封、来源系统和版本。 |
| 应用层 | `OmsEventDispatchService.dispatch(event)` 先写 `oms_event_consume_log`，再分发库存、WMS、TMS、BMS 事件处理器。 |
| 领域层 | 根据事件事实推进履约、出库、售后或结算引用状态。 |
| 基础设施层 | 保存 Inbox、业务状态和审计。 |
| 返回 | `eventId`、`consumeStatus`。 |
| 异常 | 重复事件返回历史结果；低版本状态回退事件忽略。 |

## 继续上下文

当前结论：OMS `03` 已按供应商接口逐项格式展开，覆盖工作台、渠道接入、订单、审单、履约、预占、出库、取消、售后、异常、规则、外部查询和事件入口。  
关键假设：OMS 只拥有订单和履约编排事实，不拥有库存扣减、仓内发货、运输签收和财务入账事实。  
待决问题：审单规则引擎位置、取消拦截失败默认策略。  
下一步：可继续补充每个接口的请求/响应 DTO 字段。
