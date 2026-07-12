# OMS-REQ-002 分仓预占与 WMS 出库下发

## 1. 需求背景

OMS 已完成渠道订单接入和销售订单审核，但审核通过后尚未形成履约执行单元。该需求补齐“审核通过 -> 分仓 -> 库存预占 -> WMS 出库”的首个销售履约闭环，并保留跨系统失败后的重试与释放补偿入口。

## 2. 业务目标

- 为审核通过的销售订单生成履约单，明确履约仓、物流产品和履约数量。
- 通过中央库存命令请求/释放库存预占，OMS 只保存预占引用，不直接修改库存余额。
- 预占成功后创建并下发 OMS 出库单到 WMS；WMS 失败时支持重推，取消时支持先取消 WMS 再释放库存。
- 所有写命令具备幂等键、版本校验、操作审计和 Outbox/Inbox 可靠性记录。

## 3. 范围与边界

### 本期范围

- `OMS-API-002`：分仓、换仓、按 SKU 拆分履约。
- `OMS-API-003`：库存预占、释放、预占结果消费、创建/下发/取消/重推 WMS 出库。
- 履约单、预占引用、出库单、跨系统命令、事件 Inbox 和 OMS Outbox 持久化。

### 本期不包含

- 真实中央库存/WMS Dubbo 或 HTTP 客户端；本期以集成命令 Outbox 表达跨上下文请求。
- WMS 仓内拣货、复核、包装、发货业务；这些事实由 WMS 后续事件切片回传。
- 取消申请、售后、退款、TMS 运输任务和复杂自动分仓规则。

## 4. 领域模型与状态

| 聚合 | 核心状态 | 关键不变量 |
| --- | --- | --- |
| `FulfillmentAggregate` | `PENDING_RESERVATION`、`RESERVED`、`PENDING_OUTBOUND`、`OUTBOUND_ISSUED`、`SHIPPED`、`CANCELLED`、`FAILED` | 履约数量不超过订单数量；预占成功后才能出库；已出库/已发货不可换仓或拆分 |
| `ReservationReference` | `PENDING`、`RESERVED`、`FAILED`、`RELEASE_REQUESTED`、`RELEASED` | 同一履约单只有一个有效预占引用；释放必须引用原预占幂等键 |
| `OutboundAggregate` | `DRAFT`、`ISSUED`、`WMS_ACCEPTED`、`PICKING`、`SHIPPED`、`CANCEL_REQUESTED`、`CANCELLED`、`EXCEPTION` | 同一履约单不重复创建有效出库单；已发货不可取消；重推不能重复创建 WMS 单 |

## 5. 接口与事件

| 类型 | 契约 |
| --- | --- |
| 分仓 | `POST /api/oms/v1/sales-orders/{salesOrderNo}/fulfillments` |
| 换仓 | `POST /api/oms/v1/fulfillments/{fulfillmentNo}/change-warehouse` |
| 拆分 | `POST /api/oms/v1/fulfillments/{fulfillmentNo}/split` |
| 预占/释放 | `POST /api/oms/v1/fulfillments/{fulfillmentNo}/reserve`、`POST /api/oms/v1/reservations/{reservationRefNo}/release` |
| 出库 | `POST /api/oms/v1/fulfillments/{fulfillmentNo}/outbound`、`POST /api/oms/v1/outbounds/{outboundNo}/dispatch|cancel|retry` |
| 外部事件 | `POST /internal/oms/v1/events`，消费 `StockReserved`、`StockReservationFailed`、`StockReleased`、`WmsOutboundAccepted`、`WmsOutboundShipped`、`WmsOutboundCancelled` |
| 生产事件 | `FulfillmentOrderCreated`、`FulfillmentWarehouseChanged`、`FulfillmentSplit`、`StockReservationRequested`、`StockReservationReleaseRequested`、`OutboundOrderCreated`、`OutboundInstructionIssued`、`OutboundCancelRequested` |

## 6. 验收标准

- 审核通过订单可以创建履约单；拦截订单、重复创建和数量不合法请求被拒绝。
- 履约单在未预占/未出库前可以换仓或拆分，拆分前后数量守恒；已预占或已出库后禁止换仓/拆分。
- 预占请求写入预占引用和库存集成命令；重复幂等键不重复写命令。
- `StockReserved` 推进履约到待出库；失败事件推进履约到失败并保留原因；释放请求写补偿命令。
- 预占成功后才能创建、下发 WMS 出库；WMS 失败支持重推，已发货出库不能取消。
- 领域单测、应用服务测试覆盖状态机、数量守恒、幂等、版本冲突和事件 Inbox；Maven 全量测试通过。

## 7. 设计假设与后续风险

- 首期按一个销售订单生成一个初始履约单，拆分后允许生成子履约单；自动分仓规则暂以调用方提供目标仓为准。
- 跨系统调用采用本地集成命令 Outbox，真实 ACL 客户端和消息投递器在集成中心切片补齐。
- 订单行当前沿用 OMS 首切片的文本快照格式，后续需要切换为正式行表读写并补 MySQL/Flyway 集成测试。
