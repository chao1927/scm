# WMS-REQ-003 Outbox 投递与失败重试

## 1. 背景与目标

WMS 已能在入库、收货、质检、上架、出库动作中写出业务事件，但缺少 Outbox 投递、失败查询和人工重试能力。该能力是 WMS 与采购、供应商、中央库存、OMS/TMS 解耦协作的可靠性底座。

## 2. 范围

| 类型 | 内容 |
| --- | --- |
| 包含 | Outbox 待投递事件扫描、消息发布端口、成功/失败状态更新、失败事件查询、人工重试、应用服务测试 |
| 不包含 | 真实 RocketMQ Producer、入站 Inbox 消费处理、死信队列、定时任务调度 |

## 3. 验收

| 编号 | 验收项 |
| --- | --- |
| WMS-REQ-003-AC01 | `status=1/3` 的事件可批量投递，成功后标记 `status=2` |
| WMS-REQ-003-AC02 | 投递异常时标记 `status=3` 并增加重试次数 |
| WMS-REQ-003-AC03 | 失败事件可查询，可人工置回待投递 |

## 4. 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/wms/v1/operations/outbox/dispatch` | 手动触发一批 Outbox 投递 |
| `GET` | `/api/wms/v1/operations/outbox/failed-events` | 查询失败事件 |
| `POST` | `/api/wms/v1/operations/outbox/failed-events/{eventId}/retry` | 失败事件置回待投递 |

## 5. 实现清单

| 层次 | 类/文件 |
| --- | --- |
| 接口层 | `WmsOperationsController` |
| 应用层 | `WmsOutboxDispatchApplicationService`、`WmsMessageBrokerPort` |
| 基础设施 | `WmsEventMapper`、`LoggingWmsMessageBrokerAdapter` |
| 测试 | `WmsOutboxDispatchApplicationServiceTest` |

## 6. 后续

下一切片补入站 `wms_inbox_event` 消费和失败重放，随后推进 `WMS-API-004` 出库单与库存分配。
