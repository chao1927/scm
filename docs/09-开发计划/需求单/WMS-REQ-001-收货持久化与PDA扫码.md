# WMS-REQ-001 收货持久化与 PDA 扫码

## 1. 背景与目标

WMS 已完成入库单创建/取消以及收货、质检、上架的领域数量规则，但开发日志未登记收货持久化和 PDA 扫码接口的实际完成度。当前需求单用于把 `WMS-API-002` 固化为可验收切片：收货单可创建、PDA 可幂等扫码、提交收货后可发布收货完成事件。

## 2. 范围

| 类型 | 内容 |
| --- | --- |
| 包含 | 收货单创建、PDA 扫码、提交收货、扫码幂等、收货数量不变量、Outbox 事件、应用服务测试 |
| 不包含 | 质检结果持久化、上架任务持久化、库位容量校验、中央库存真实记账、RocketMQ 真实投递 |

## 3. 用户故事与验收

| 编号 | 用户故事 | 验收条件 |
| --- | --- | --- |
| WMS-REQ-001-US01 | 作为仓库收货员，我可以基于入库单打开收货单 | 重复打开同一收货单返回幂等结果；首次打开发布 `WmsArrivalRegistered` |
| WMS-REQ-001-US02 | 作为 PDA 收货员，我可以扫码登记实收/拒收数量 | 必须提供 `X-Idempotency-Key`；重复扫码不重复累加；拒收必须填写原因 |
| WMS-REQ-001-US03 | 作为收货主管，我可以在数量平账后提交收货 | 实收 + 拒收必须等于通知数量；提交后发布 `WmsReceiptCompleted` |

## 4. 领域规则

| 规则 | 说明 |
| --- | --- |
| 收货数量上限 | 累计实收 + 累计拒收不得超过通知数量 |
| 拒收原因 | 拒收数量大于 0 时必须填写原因 |
| 提交平账 | 提交时累计实收 + 累计拒收必须等于通知数量 |
| 幂等边界 | 同一收货单、同一 `X-Idempotency-Key` 的 PDA 扫码只能生效一次 |

## 5. 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/wms/v1/receipts` | 创建/打开收货单 |
| `POST` | `/api/wms/v1/pda/receipts/scan` | PDA 扫码收货，写接口必须带 `X-Idempotency-Key` |
| `POST` | `/api/wms/v1/receipts/{receiptNo}/submit` | 提交收货，按版本防并发 |

## 6. 实现清单

| 层次 | 类/文件 |
| --- | --- |
| 接口层 | `ReceivingController` |
| 应用层 | `ReceivingApplicationService` |
| 领域层 | `ReceiptAggregate`、`ReceiptStatus`、`ReceiptRepository` |
| 基础设施 | `MyBatisReceiptRepository`、`ReceiptMapper`、`ReceiptScanMapper`、`MyBatisWmsEventPublisher` |
| 数据迁移 | `V2__wms_operation_tables.sql`、`V4__wms_receipt_scan.sql` |
| 测试 | `ReceiptAggregateTest`、`ReceivingApplicationServiceTest` |

## 7. 剩余风险

| 风险 | 后续处理 |
| --- | --- |
| 中央库存记账仍是事件事实，未真实扣/增库存账户 | 在 `WMS-API-003` 上架完成后接入中央库存 OpenAPI/事件消费 |
| 收货行当前按单 SKU 建模，尚未覆盖多行 ASN | 下一切片将收货单行表与入库单行表对齐 |
| RocketMQ 真实投递未联调 | WMS Outbox 投递任务切片补齐 |
