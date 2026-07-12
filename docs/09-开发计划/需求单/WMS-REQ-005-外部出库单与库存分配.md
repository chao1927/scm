# WMS-REQ-005 外部出库单与库存分配

## 1. 背景与目标

OMS、退供、调拨等系统需要向 WMS 下发出库指令。WMS 需要按来源系统 + 来源单号 + 仓库幂等创建出库单，支持分配和取消，并通过 Outbox 发布出库事实，为后续波次、拣货、复核、发货交接提供入口。

## 2. 范围

| 类型 | 内容 |
| --- | --- |
| 包含 | 外部创建出库单、分配出库单、取消出库单、状态机、Outbox 事件、应用服务测试 |
| 不包含 | 明细行、真实库位分配算法、中央库存预占校验、释放库存、波次拣货 |

## 3. 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/openapi/wms/v1/outbound-orders` | 外部幂等创建出库单 |
| `POST` | `/api/wms/v1/outbound-orders/allocate` | 分配出库单 |
| `POST` | `/openapi/wms/v1/outbound-orders/cancel` | 外部取消出库单 |

## 4. 事件

| 事件 | 触发 |
| --- | --- |
| `WmsOutboundOrderCreated` | 创建出库单 |
| `WmsOutboundAllocated` | 分配出库单 |
| `WmsOutboundCancelled` | 取消出库单 |

## 5. 后续

下一切片进入 `WMS-API-005` 波次、拣货、容器与复核包装，并补出库明细与真实库存分配。
