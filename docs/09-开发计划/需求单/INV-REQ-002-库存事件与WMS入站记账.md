# INV-REQ-002 库存事件与 WMS 入站记账

## 1. 背景与目标

中央库存需要可靠消费 WMS 上架、发货交接、盘点差异事实，并通过 Outbox 发布库存变化事件。

## 2. 范围

| 类型 | 内容 |
| --- | --- |
| 包含 | Inbox 幂等、WMS 入库/出库/盘点差异记账、Outbox 事件、手动投递 |
| 不包含 | 真实 RocketMQ、死信队列、复杂 JSON 载荷版本 |

## 3. 接口

| 方法 | 路径 |
| --- | --- |
| `POST` | `/internal/inventory/v1/events` |
| `POST` | `/api/inventory/v1/operations/outbox/dispatch` |
