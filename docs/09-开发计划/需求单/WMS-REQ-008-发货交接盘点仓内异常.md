# WMS-REQ-008 发货交接、盘点、仓内异常

## 1. 背景与目标

WMS 出库执行完成后需要形成发货交接事实；库存治理需要盘点差异确认；仓内作业需要异常创建与关闭。本需求单完成 `WMS-API-006` 的最小闭环。

## 2. 范围

| 类型 | 内容 |
| --- | --- |
| 包含 | 发货交接创建/确认、盘点差异创建/确认、仓内异常创建/关闭、事件发布 |
| 不包含 | TMS 运单真实调用、中央库存调整真实调用、异常责任/赔付流程 |

## 3. 接口

| 方法 | 路径 |
| --- | --- |
| `POST` | `/api/wms/v1/handovers` |
| `POST` | `/api/wms/v1/handovers/confirm` |
| `POST` | `/api/wms/v1/stocktakes` |
| `POST` | `/api/wms/v1/stocktakes/confirm-difference` |
| `POST` | `/api/wms/v1/warehouse-exceptions` |
| `POST` | `/api/wms/v1/warehouse-exceptions/close` |
