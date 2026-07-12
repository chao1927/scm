# OMS-REQ-003 取消申请与售后退款基础闭环

## 1. 需求背景

OMS 已完成销售履约和 WMS 出库下发，但客户取消、仅退款和下游拦截结果没有统一业务单据承接。该需求补齐发货前取消和基础售后退款路径，避免订单状态、库存预占、WMS 出库和退款请求互相脱节。

## 2. 本期范围

- 取消申请创建、审核、请求 WMS 拦截、WMS 取消结果消费、库存释放请求、取消完成。
- 售后单创建、审核、仅退款请求、退款结果消费、售后完成。
- 取消和售后单的 Outbox/Inbox、操作审计、BMS/WMS/库存集成命令。

## 3. 暂不包含

- 已签收后的 TMS 拦截、退货取件和退货运单。
- 退货入库、换货补发、复杂退款拆分和财务审批。
- 真实 WMS、中央库存、BMS、支付 ACL；本期以集成命令 Outbox 记录跨系统请求。

## 4. 领域模型与不变量

| 聚合 | 状态 | 关键不变量 |
| --- | --- | --- |
| `CancellationRequestAggregate` | `PENDING_REVIEW`、`APPROVED`、`PROCESSING`、`COMPLETED`、`REJECTED`、`AFTER_SALE` | 已发货订单不能走普通取消；WMS 已发货不能直接取消；取消完成必须等待拦截/释放结果或明确人工结论 |
| `AfterSaleAggregate` | `PENDING_REVIEW`、`APPROVED`、`REFUND_REQUESTED`、`REFUNDED`、`COMPLETED`、`REJECTED` | 售后数量不超过原订单/已发货数量；退款金额不超过实付金额；重复退款请求必须幂等 |

## 5. 接口与事件

| 类型 | 契约 |
| --- | --- |
| 取消申请 | `POST /api/oms/v1/cancel-requests`、`POST /api/oms/v1/cancel-requests/{no}/approve` |
| 取消处理 | `POST /api/oms/v1/cancel-requests/{no}/process`、`POST /api/oms/v1/cancel-requests/{no}/complete` |
| 售后申请 | `POST /api/oms/v1/after-sales`、`POST /api/oms/v1/after-sales/{no}/approve` |
| 退款处理 | `POST /api/oms/v1/after-sales/{no}/request-refund`、`POST /api/oms/v1/after-sales/{no}/complete` |
| 外部事件 | 复用 `POST /internal/oms/v1/events`，新增 `WmsOutboundCancelled`、`StockReleased`、`RefundCompleted` |
| 生产事件 | `CancelRequestCreated`、`CancelRequestApproved`、`WmsCancelRequested`、`StockReleaseRequested`、`SalesOrderCanceled`、`AfterSaleCreated`、`AfterSaleApproved`、`RefundRequested`、`AfterSaleCompleted` |

## 6. 验收标准

- 发货前销售订单可以创建取消申请并审核；已发货订单普通取消被拒绝。
- 审核通过后写 WMS 取消命令；WMS 取消成功后写库存释放命令，重复外部事件不重复推进状态。
- 仅退款售后单校验金额、订单归属和幂等键，审核后写 BMS 退款命令。
- `RefundCompleted` 只允许匹配当前售后单，退款金额不超申请金额，完成后产生售后完成事件。
- 领域/应用测试覆盖状态机、金额数量边界、Outbox、Inbox 幂等和失败状态。
