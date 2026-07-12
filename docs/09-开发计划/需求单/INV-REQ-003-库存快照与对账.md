# INV-REQ-003 库存快照与对账

## 1. 背景与目标

中央库存需要定期保留账户快照，并能和 WMS 账实数量形成对账差异单，支撑人工确认和后续补偿。

## 2. 范围

| 类型 | 内容 |
| --- | --- |
| 包含 | 快照生成/查询、对账单创建/查询/确认 |
| 不包含 | 自动定时快照、差异自动调整、对账报表导出 |

## 3. 接口

| 方法 | 路径 |
| --- | --- |
| `POST` | `/api/inventory/v1/snapshots/generate` |
| `GET` | `/api/inventory/v1/snapshots` |
| `POST` | `/api/inventory/v1/inventory-reconciliations` |
| `POST` | `/api/inventory/v1/inventory-reconciliations/{reconcileNo}/confirm` |
| `GET` | `/api/inventory/v1/inventory-reconciliations` |
