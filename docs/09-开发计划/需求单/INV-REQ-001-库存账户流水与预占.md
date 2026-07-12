# INV-REQ-001 库存账户流水与预占

## 1. 背景与目标

WMS 已能产生上架、出库、盘点等仓内事实，中央库存需要承接企业级库存账户、可用量、预占量、冻结量和不可变流水。本需求单覆盖中央库存首个可运行闭环。

## 2. 范围

| 类型 | 内容 |
| --- | --- |
| 包含 | 库存账户、流水查询、WMS入库记账、WMS出库扣减、库存预占、释放、冻结、解冻、调整 |
| 不包含 | 快照重建、对账单、真实 MQ 消费、权限系统联调 |

## 3. 接口

| 方法 | 路径 |
| --- | --- |
| `GET` | `/api/inventory/v1/stocks` |
| `GET` | `/api/inventory/v1/stock-ledgers` |
| `POST` | `/openapi/inventory/v1/wms/inbound` |
| `POST` | `/openapi/inventory/v1/wms/outbound` |
| `POST` | `/openapi/inventory/v1/reservations` |
| `POST` | `/openapi/inventory/v1/reservations/{reservationNo}/release` |
| `POST` | `/api/inventory/v1/freezes` |
| `POST` | `/api/inventory/v1/freezes/unfreeze` |
| `POST` | `/api/inventory/v1/adjustments` |
