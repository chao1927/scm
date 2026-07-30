# TRF-REQ-003 WMS/TMS 调拨执行与差异闭环

## 1. 目标

在中央库存调拨单的数据主权下，完成 WMS 调出/调入执行、TMS 运输启运/送达、中央库存调入记账与短收差异确认闭环。

## 2. 领域边界

| 上下文 | 数据主权 | 关键行为 | 生产事实 |
| --- | --- | --- | --- |
| 中央库存 | 调拨单、数量守恒、库存余额与台账 | 出库扣减、在途、调入记账、差异确认 | `TransferOutboundCompleted`、`TransferInTransit`、`TransferCompleted`、`TransferDifferenceRaised` |
| WMS | 调拨仓内执行任务 | 创建调出单、实际出库、创建调入单、最终收货、取消补偿 | `TransferOutboundCompleted`、`TransferReceived`、`TransferCancellationCompensated` |
| TMS | 运输任务 | 创建 `TRANSFER` 任务、接单、启运、送达 | `TransferInTransit`、`TransferDelivered` |

跨上下文事件版本只用于审计；消费者使用 Inbox 幂等键去重，并读取本地聚合版本执行乐观锁，禁止把生产方版本当成本地版本。

## 3. 接口与验收

| 接口/入口 | 权限/身份 | 验收 |
| --- | --- | --- |
| `POST /internal/wms/v1/events` | 库存应用身份、`wms:event:manage` | 预占、在途、取消事件幂等消费并可失败重放 |
| `POST /api/wms/v1/transfer-operations/{no}/outbound` | `wms:transfer:write` + 调出仓范围 | 实际出库量必须等于计划量并发布 Outbox |
| `POST /api/wms/v1/transfer-operations/{no}/receive` | `wms:transfer:write` + 调入仓范围 | 累计收货不超出库量，支持最终短收 |
| `POST /api/tms/v1/transport-tasks/{no}/start` | `tms:task:manage` | `TRANSFER` 场景发布 `TransferInTransit` |
| `POST /api/tms/v1/transport-tasks/{no}/deliver` | `tms:task:manage` | 仅在途任务可送达 |
| `POST /internal/inventory/v1/transfer-events` | WMS/TMS 应用身份 | 出库、在途、收货事件 Inbox 幂等消费 |
| `POST /api/inventory/v1/transfers/{no}/difference/confirm` | `inventory:transfer:manage` + 双仓范围 | 仅差异状态可确认，数量守恒不变 |

## 4. 不变量与补偿

1. 调拨出库量必须等于预占量；出库时关闭预占并记录 `TRANSFER_OUTBOUND` 台账。
2. 调入累计量不得超过调出量；每次收货在目标仓记入库存余额和入库台账。
3. 最终收货时 `收货量 + 差异量 = 出库量`；差异确认只确认损益事实，不伪造实收数量。
4. 未出库调拨可取消，库存释放预占，WMS 取消调出任务；已出库后禁止直接取消。
5. TMS 运输地址由集成编排根据仓库主数据快照补齐后调用现有 `TRANSFER` 运输任务 OpenAPI。

## 5. 测试

- 中央库存：调拨聚合、应用服务、Inbox 幂等、短收差异确认。
- WMS：调拨执行聚合、通用 Inbox 回归。
- TMS：任务接单、启运、送达及 `TRANSFER` 事件。
- 后端 Maven 全量回归通过后方可标记完成。
