# WMS-REQ-004 Inbox 入站命令与失败重放

## 1. 背景与目标

WMS 需要接收采购、OMS、供应商系统发来的入库/出库命令。入站命令不能直接执行后丢失上下文，必须先进入 Inbox，按来源系统 + 事件编码幂等处理，失败后支持人工查询和重放。

## 2. 范围

| 类型 | 内容 |
| --- | --- |
| 包含 | 标准事件信封、Inbox 幂等落库、入库/出库创建命令分发、失败记录、失败查询、人工重放 |
| 不包含 | RocketMQ Listener、复杂事件路由、死信队列、入站签名验签 |

## 3. 支持事件

| 事件类型 | 动作 | 必填 payload |
| --- | --- | --- |
| `CreateInboundOrderRequested` | 创建 WMS 入库单 | `sourceType`、`sourceNo`、`warehouseId`、`expectedArrivalAt` |
| `CreateOutboundOrderRequested` | 创建 WMS 出库单 | `sourceType`、`sourceNo`、`warehouseId` |

## 4. 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/internal/wms/v1/events` | 接收标准入站事件 |
| `GET` | `/api/wms/v1/operations/inbox/failed-events` | 查询失败入站事件 |
| `POST` | `/api/wms/v1/operations/inbox/failed-events/{inboxId}/replay` | 人工重放失败事件 |

## 5. 后续

下一切片进入 `WMS-API-004` 出库单与库存分配，并补真实 MQ Listener 适配。
