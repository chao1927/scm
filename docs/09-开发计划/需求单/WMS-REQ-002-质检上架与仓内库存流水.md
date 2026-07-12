# WMS-REQ-002 质检上架与仓内库存流水

## 1. 背景与目标

收货完成后，WMS 必须区分“已收货”“质检完成”“已上架入库”三个事实。中央库存只能消费上架完成后的可用库存事实，不能把收货数量直接当成可售库存。本需求单对应 `WMS-API-003`，目标是让质检结果、上架任务、仓内库存流水和完成事件形成可交接闭环。

## 2. 范围

| 类型 | 内容 |
| --- | --- |
| 包含 | 质检单创建、提交质检结果、上架任务创建、PDA 上架扫描、仓内库存流水、质检/上架完成事件、应用服务测试 |
| 不包含 | 库位容量推荐、冻结/不合格区库存账户、中央库存真实账户更新、PDA 设备鉴权 |

## 3. 用户故事与验收

| 编号 | 用户故事 | 验收条件 |
| --- | --- | --- |
| WMS-REQ-002-US01 | 作为质检员，我可以登记合格和不合格数量 | 合格 + 不合格必须等于质检数量；完成后发布 `WmsQualityInspectionCompleted` |
| WMS-REQ-002-US02 | 作为上架员，我可以分批扫描上架 | 上架数量累计不得超过合格数量；每次上架写 `wms_stock_ledger` |
| WMS-REQ-002-US03 | 作为库存系统，我可以订阅上架完成事实 | 任务全部上架后发布 `WmsPutawayCompleted` |

## 4. 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/wms/v1/inspections` | 创建质检单 |
| `POST` | `/api/wms/v1/inspections/{no}/result` | 提交质检结果，按版本防并发 |
| `POST` | `/api/wms/v1/putaway-tasks` | 创建上架任务 |
| `POST` | `/api/wms/v1/pda/putaway/scan` | PDA 上架扫描，写仓内库存流水 |

## 5. 实现清单

| 层次 | 类/文件 |
| --- | --- |
| 接口层 | `InspectionController`、`PutawayController` |
| 应用层 | `InspectionApplicationService`、`PutawayApplicationService` |
| 领域层 | `InspectionAggregate`、`PutawayTaskAggregate` |
| 基础设施 | `InspectionMapper`、`PutawayMapper`、`StockLedgerMapper`、`WmsEventMapper` |
| 数据迁移 | `V2__wms_operation_tables.sql` |
| 测试 | `InspectionAggregateTest`、`PutawayTaskAggregateTest`、`InspectionApplicationServiceTest`、`PutawayApplicationServiceTest` |

## 6. 剩余风险

| 风险 | 后续处理 |
| --- | --- |
| 暂无库位容量、混放、温层和不合格区规则 | 在 WMS 库位/库存账户切片补齐 |
| 上架扫描暂未做独立幂等键 | 后续 PDA 统一扫码幂等表时补齐 |
| 中央库存只收到事件，未做真实账户同步 | `INV-01` 与 WMS Outbox 投递切片共同完成 |
