# TRF-REQ-002 中央库存调拨申请、审批、预占与在途

## 交付范围

- 调拨单创建幂等、提交、审批、预占、出库事实、在途事实和取消。
- 源仓预占复用中央库存账户、预占单和库存流水。
- 所有状态转换产生 Outbox 事实。
- WMS 出库、TMS 在途事实通过 Inbox 幂等消费。
- 管理 API 按货主、源仓和目标仓双向校验数据范围。

## 持久化

`V4__stock_transfer.sql` 新增 `inv_stock_transfer`，包含申请/预占/出库/入库/差异数量、状态、乐观锁版本和创建幂等键。

## 接口

- `POST/GET /api/inventory/v1/transfers`
- `GET /api/inventory/v1/transfers/{transferNo}`
- `POST /{transferNo}/submit|approve|reserve|outbound|in-transit|cancel`
- RocketMQ `inventory-domain-event-consumer` 消费调拨事件

## 事件

`TransferCreated`、`TransferSubmitted`、`TransferApproved`、`TransferStockReserved`、`TransferOutboundCompleted`、`TransferInTransit`、`TransferCancelled`。

## 验收

`StockTransferApplicationServiceTest`、`StockTransferEventApplicationServiceTest`、`StockTransferAggregateTest` 通过；`mvn -q -pl inventory-service -am test` 通过。
