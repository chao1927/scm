# 03-WMS系统接口逐项实现设计

> 本文承接 [02-WMS系统接口事件实现逻辑](02-WMS系统接口事件实现逻辑.md)，继续把 `docs/06-子系统接口设计/03-WMS系统接口设计.md` 中的接口拆成可编码实现小节。每个接口按“接口层 -> 应用层 -> 领域层 -> 基础设施层 -> 事件/返回 -> 异常”说明。

## 1. 统一代码分层

### 1.1 包结构

```text
wms-service
  ├── interfaces
  │   ├── web
  │   ├── pda
  │   ├── openapi
  │   ├── mq
  │   └── job
  ├── application
  │   ├── command
  │   ├── query
  │   ├── event
  │   └── dto
  ├── domain
  │   ├── inbound
  │   ├── receiving
  │   ├── inspection
  │   ├── putaway
  │   ├── stock
  │   ├── outbound
  │   ├── wave
  │   ├── picking
  │   ├── container
  │   ├── packing
  │   ├── handover
  │   ├── stocktake
  │   ├── exception
  │   └── shared
  └── infrastructure
      ├── persistence
      ├── rpc
      ├── mq
      ├── idempotency
      ├── audit
      └── device
```

### 1.2 命名约定

| 类型 | 示例 | 说明 |
| --- | --- | --- |
| Controller | `WmsInboundOrderController` | 后台 HTTP 协议转换 |
| PdaController | `WmsPdaReceivingController` | PDA 扫码命令入口 |
| OpenApiController | `WmsInboundOpenApiController` | 来源系统创建作业入口 |
| QueryService | `InboundOrderQueryService` | 查询读模型 |
| ApplicationService | `ReceivingApplicationService` | 编排权限、设备、幂等、事务、聚合、事件 |
| Aggregate | `ReceivingAggregate` | 保护作业状态和数量不变量 |
| DomainEvent | `GoodsReceivedEvent` | 写入 `wms_domain_event` |

## 2. 工作台接口

### 2.1 查询工作台统计

`GET /api/wms/v1/workbench/summary`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `WmsWorkbenchController.summary(WorkbenchSummaryQueryRequest)` 解析仓库、货主、作业类型和当前用户。 |
| 应用层 | `WmsWorkbenchQueryService.querySummary(query)` 校验 `wms:workbench:read`，追加仓库、货主、库区权限。 |
| 领域层 | 无聚合行为；这是读模型查询。 |
| 基础设施层 | 聚合查询入库、收货、质检、上架、出库、拣货、包装、异常读模型。 |
| 返回 | 待收货、待质检、待上架、待拣货、待复核、待交接、异常数量。 |
| 异常 | 无权限返回 `403`；数据范围为空返回空统计。 |

### 2.2 查询待办列表

`GET /api/wms/v1/workbench/todos`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `WmsWorkbenchController.todos(TodoPageQueryRequest)` 接收 `todoType/pageNo/pageSize`。 |
| 应用层 | `WmsTodoQueryService.pageTodos(query)` 校验工作台读权限和仓库/货主范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 从收货、质检、上架、拣货、复核、交接、盘点、异常读模型或统一待办表查询。 |
| 返回 | `todoId`、`businessType`、`businessNo`、`warehouseName`、`statusName`、`targetRoute`。 |
| 异常 | `pageSize` 超限制返回 `400`；数据范围为空返回空分页。 |

## 3. 入库和收货接口

### 3.1 查询入库单列表

`GET /api/wms/v1/inbound-orders`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `InboundOrderController.page(InboundOrderPageQuery)` 接收来源系统、来源单号、仓库、状态。 |
| 应用层 | `InboundOrderQueryService.page(query)` 校验入库读权限、仓库和货主范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询 `wms_inbound_order` 和收货/质检/上架摘要读模型。 |
| 返回 | 入库单号、来源单、仓库、货主、状态、通知数量、已收/已上架数量。 |
| 异常 | 无权限返回空分页；来源系统非法返回 `400`。 |

### 3.2 查询入库单详情

`GET /api/wms/v1/inbound-orders/{inboundOrderNo}`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `InboundOrderController.detail(inboundOrderNo)`。 |
| 应用层 | `InboundOrderQueryService.getDetail(no)` 校验单据所属仓库/货主权限。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询入库头行、收货、质检、上架任务、异常和操作日志。 |
| 返回 | 入库详情、明细、状态时间线、上下游引用。 |
| 异常 | 单据不存在或无权限返回 `404`。 |

### 3.3 外部创建入库单

`POST /openapi/wms/v1/inbound-orders`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `InboundOpenApiController.create(CreateInboundOrderRequest)` 校验来源系统、签名和幂等键。 |
| 应用层 | `InboundOrderApplicationService.createFromSource(command)` 校验仓库、货主、SKU、批次策略和来源单版本。 |
| 领域层 | `InboundOrderAggregate.create(...)` 创建待收货入库单，锁定来源单快照和可收数量。 |
| 基础设施层 | 保存入库头行，写事件和审计。 |
| 事件 | `InboundOrderCreated`。 |
| 异常 | 来源单重复返回历史结果；主数据失效返回 `422`。 |

### 3.4 取消入库单

`POST /api/wms/v1/inbound-orders/{inboundOrderNo}/cancel`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `InboundOrderController.cancel(inboundOrderNo, CancelRequest)` 接收取消原因和版本。 |
| 应用层 | `InboundOrderApplicationService.cancel(command)` 校验取消权限、幂等和作业进度。 |
| 领域层 | `InboundOrderAggregate.cancel(...)` 只允许未收货或可冲销状态取消。 |
| 基础设施层 | 更新入库状态，写事件和审计，通知来源系统。 |
| 事件 | `InboundOrderCanceled`。 |
| 异常 | 已收货不可直接取消，返回 `409 STATE_CONFLICT`。 |

### 3.5 查询收货单列表

`GET /api/wms/v1/receipts`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `ReceivingController.page(ReceiptPageQuery)` 接收入库单、仓库、收货状态和分页。 |
| 应用层 | `ReceivingQueryService.page(query)` 校验收货读权限和仓库/货主范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询收货单、容器、批次、差异读模型。 |
| 返回 | 收货单号、入库单号、收货数量、差异数量、收货状态。 |
| 异常 | 外部 ASN 不可用时不影响本地展示。 |

### 3.6 PDA 扫码收货

`POST /api/wms/v1/pda/receipts/scan`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `PdaReceivingController.scan(PdaReceiveScanRequest)` 校验设备、条码、作业人和幂等键。 |
| 应用层 | `ReceivingApplicationService.receiveByPda(command)` 校验作业人仓库/库区权限、入库单状态、SKU/批次/容器。 |
| 领域层 | `ReceivingAggregate.receiveLine(...)` 累计收货数量，按超收策略判断禁止、转异常或待审批。 |
| 基础设施层 | 保存收货明细、暂存库存和库内库存流水，写事件和审计。 |
| 事件 | `GoodsReceived`。 |
| 异常 | 重复扫码幂等返回上次结果；超收禁止返回 `422 OVER_RECEIVE_NOT_ALLOWED`。 |

### 3.7 提交收货完成

`POST /api/wms/v1/receipts/{receiptNo}/submit`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `ReceivingController.submit(receiptNo, SubmitReceiptRequest)` 接收版本和差异说明。 |
| 应用层 | `ReceivingApplicationService.submit(command)` 校验提交权限、幂等和收货状态。 |
| 领域层 | `ReceivingAggregate.submit(...)` 状态收货中 -> 已收货；根据策略触发质检或上架。 |
| 基础设施层 | 更新收货单、入库单进度，写事件和审计，创建质检单/上架任务。 |
| 事件 | `ReceivingCompleted`。 |
| 异常 | 未处理差异不可提交；数量不平返回 `422`。 |

## 4. 质检、上架和库内库存接口

### 4.1 查询质检单列表

`GET /api/wms/v1/inspections`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `InspectionController.page(InspectionPageQuery)` 接收质检状态、仓库、SKU、时间。 |
| 应用层 | `InspectionQueryService.page(query)` 校验质量角色和仓库/货主权限。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询质检头行、抽检明细和附件摘要。 |
| 返回 | 质检单号、来源收货单、待检数量、合格/不合格数量、状态。 |
| 异常 | 附件加载失败不影响主体。 |

### 4.2 提交质检结果

`POST /api/wms/v1/inspections/{inspectionNo}/result`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `InspectionController.submitResult(inspectionNo, InspectionResultRequest)` 校验质检结果和附件。 |
| 应用层 | `InspectionApplicationService.submitResult(command)` 校验质检权限、幂等和版本。 |
| 领域层 | `InspectionAggregate.submitResult(...)` 校验合格、不合格、让步数量合计等于质检数量。 |
| 基础设施层 | 保存质检结果，生成上架任务或不合格处理，写事件和审计。 |
| 事件 | `InspectionCompleted`。 |
| 异常 | 数量不平返回 `422`；已上架后不可修改结果。 |

### 4.3 查询上架任务列表

`GET /api/wms/v1/putaway-tasks`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `PutawayTaskController.page(PutawayTaskPageQuery)` 接收仓库、库区、状态、作业人。 |
| 应用层 | `PutawayTaskQueryService.page(query)` 校验库区和上架权限。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询上架任务、推荐库位、容器和执行进度。 |
| 返回 | 上架任务号、来源收货/质检单、推荐库位、待上架数量、状态。 |
| 异常 | 推荐库位失效时返回 `locationFresh=false`。 |

### 4.4 PDA 扫码上架

`POST /api/wms/v1/pda/putaway/scan`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `PdaPutawayController.scan(PdaPutawayScanRequest)` 校验设备、来源容器、目标库位和幂等键。 |
| 应用层 | `PutawayApplicationService.putawayByPda(command)` 校验作业人、库位、SKU、批次、容量和混放规则。 |
| 领域层 | `PutawayTaskAggregate.putawayLine(...)` 推进任务数量；`WarehouseStockAggregate.increase(...)` 增加库内库存。 |
| 基础设施层 | 写上架明细、库内库存、库存流水和事件；可调用中央库存入库确认。 |
| 事件 | `GoodsPutawayCompleted`。 |
| 异常 | 库位容量不足返回 `422`；重复扫码幂等返回历史结果。 |

### 4.5 查询库内库存

`GET /api/wms/v1/stocks`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `WarehouseStockController.page(WarehouseStockPageQuery)` 接收仓库、库区、库位、SKU、批次、库存状态。 |
| 应用层 | `WarehouseStockQueryService.page(query)` 校验仓库、货主、库区权限。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询库位/批次/容器维度库存和库内流水摘要。 |
| 返回 | 库内库存数量、库位、批次、容器、冻结/锁定状态，并标记 `warehouseStockOnly=true`。 |
| 异常 | 不作为可售承诺；跨货主查询返回 `403`。 |

### 4.6 库内移库

`POST /api/wms/v1/stocks/move`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `WarehouseStockController.move(StockMoveRequest)` 校验来源/目标库位、SKU、数量和幂等键。 |
| 应用层 | `WarehouseStockApplicationService.move(command)` 校验移库权限、库位规则和可移动数量。 |
| 领域层 | `WarehouseStockAggregate.move(...)` 扣减来源库位并增加目标库位，保证数量守恒。 |
| 基础设施层 | 更新库内库存，写库内流水、事件和审计。 |
| 事件 | `WarehouseStockMoved`。 |
| 异常 | 来源库存不足返回 `422`；目标库位混放限制返回 `422`。 |

## 5. 出库、波次、拣货和容器接口

### 5.1 外部创建出库单

`POST /openapi/wms/v1/outbound-orders`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `OutboundOpenApiController.create(CreateOutboundOrderRequest)` 校验来源系统、签名和幂等键。 |
| 应用层 | `OutboundOrderApplicationService.createFromSource(command)` 校验仓库、货主、SKU、批次策略和来源单状态。 |
| 领域层 | `OutboundOrderAggregate.create(...)` 创建待分配出库单，锁定来源单快照。 |
| 基础设施层 | 保存出库头行，写事件和审计。 |
| 事件 | `OutboundOrderCreated`。 |
| 异常 | 重复来源单幂等返回历史结果；来源取消中拒绝创建。 |

### 5.2 取消出库单

`POST /openapi/wms/v1/outbound-orders/{sourceOrderNo}/cancel`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `OutboundOpenApiController.cancel(sourceOrderNo, CancelOutboundRequest)` 校验来源系统和幂等键。 |
| 应用层 | `OutboundOrderApplicationService.cancelBySource(command)` 加载出库单，校验状态。 |
| 领域层 | `OutboundOrderAggregate.cancel(...)` 按状态判断可取消、可拦截或不可取消。 |
| 基础设施层 | 更新出库状态，释放库内锁定，写事件和审计。 |
| 事件 | `OutboundOrderCanceled` 或 `OutboundCancelRejected`。 |
| 异常 | 已交接/已发货不可取消，返回拦截失败原因。 |

### 5.3 分配出库库存

`POST /api/wms/v1/outbound-orders/{outboundOrderNo}/allocate`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `OutboundOrderController.allocate(outboundOrderNo, AllocateRequest)` 接收分配策略和版本。 |
| 应用层 | `OutboundOrderApplicationService.allocate(command)` 校验分配权限、仓库策略和幂等。 |
| 领域层 | 分配领域服务按批次、效期、库位策略选择库存；出库聚合锁定分配结果。 |
| 基础设施层 | 写分配明细、库内锁定流水、事件和审计。 |
| 事件 | `OutboundAllocated`。 |
| 异常 | 库存不足生成缺货异常，不直接扣中央库存。 |

### 5.4 创建波次

`POST /api/wms/v1/waves`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `WaveController.create(CreateWaveRequest)` 接收出库单范围和波次策略。 |
| 应用层 | `WaveApplicationService.create(command)` 校验波次权限、仓库作业策略和幂等。 |
| 领域层 | `WaveAggregate.create(...)` 校验出库单可波次且未重复加入。 |
| 基础设施层 | 保存波次和波次明细，写事件和审计。 |
| 事件 | `WaveCreated`。 |
| 异常 | 出库单已波次或状态不符返回 `409`。 |

### 5.5 释放波次

`POST /api/wms/v1/waves/{waveNo}/release`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `WaveController.release(waveNo, ReleaseWaveRequest)` 接收版本和幂等键。 |
| 应用层 | `WaveApplicationService.release(command)` 校验释放权限和拣货策略。 |
| 领域层 | `WaveAggregate.release(...)` 状态草稿 -> 已释放，生成拣货任务。 |
| 基础设施层 | 写波次状态、拣货单、事件和审计。 |
| 事件 | `WaveReleased`。 |
| 异常 | 未分配库存不可释放。 |

### 5.6 PDA 扫码拣货

`POST /api/wms/v1/pda/pickings/scan`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `PdaPickingController.scan(PdaPickingScanRequest)` 校验设备、库位、容器、SKU 条码和幂等键。 |
| 应用层 | `PickingApplicationService.pickByPda(command)` 校验作业人、任务状态、库位、SKU、批次和应拣数量。 |
| 领域层 | `PickingAggregate.pickLine(...)` 累计拣货数量；短拣触发异常或补拣策略。 |
| 基础设施层 | 写拣货明细、容器状态、库内库存锁定变化、事件和审计。 |
| 事件 | `PickingCompleted` 或 `PickingShortageReported`。 |
| 异常 | 短拣生成仓内异常；重复扫码幂等返回历史结果。 |

### 5.7 绑定周转容器

`POST /api/wms/v1/containers/{containerNo}/bind`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `ContainerController.bind(containerNo, BindContainerRequest)` 接收作业单、绑定对象和幂等键。 |
| 应用层 | `ContainerApplicationService.bind(command)` 校验容器状态、仓库、货主和作业权限。 |
| 领域层 | `ContainerAggregate.bind(...)` 校验容器未占用或允许复用。 |
| 基础设施层 | 更新容器状态和装载明细，写事件和审计。 |
| 事件 | `ContainerBound`。 |
| 异常 | 容器已占用返回 `409`；跨仓绑定返回 `422`。 |

## 6. 复核包装、发货、盘点和异常接口

### 6.1 PDA 复核包装

`POST /api/wms/v1/pda/packing/scan`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `PdaPackingController.scan(PdaPackingScanRequest)` 校验包裹、SKU、重量体积、设备和幂等键。 |
| 应用层 | `PackingApplicationService.verifyAndPack(command)` 校验复核权限、拣货完成、称重和包材规则。 |
| 领域层 | `PackingAggregate.verifyAndPack(...)` 校验复核数量等于应发数量，生成包裹。 |
| 基础设施层 | 保存包裹、重量体积、包材，写事件和审计，可调用 TMS 生成面单。 |
| 事件 | `PackageCompleted`。 |
| 异常 | 复核差异生成仓内异常，禁止静默放行。 |

### 6.2 打印面单

`POST /api/wms/v1/packing-orders/{packingNo}/print-label`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `PackingController.printLabel(packingNo, PrintLabelRequest)` 接收打印机和包裹号。 |
| 应用层 | `PackingApplicationService.printLabel(command)` 校验打印权限和包裹状态。 |
| 领域层 | 复核包装聚合校验包裹已生成且面单可打印。 |
| 基础设施层 | 调用 TMS 面单接口或读取已生成面单，记录打印次数。 |
| 事件 | `ShippingLabelPrintedByWms` 本地审计事件。 |
| 异常 | TMS 失败可重试，包装状态不回退。 |

### 6.3 发货交接

`POST /api/wms/v1/handover-orders/{handoverNo}/ship`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `HandoverController.ship(handoverNo, ShipRequest)` 接收承运商、交接人、包裹清单和版本。 |
| 应用层 | `HandoverApplicationService.ship(command)` 校验交接权限、月台、承运商和幂等。 |
| 领域层 | `HandoverAggregate.ship(...)` 校验包裹已复核，状态 -> 已交接/已发货。 |
| 基础设施层 | 保存交接结果，调用中央库存扣减或写待记账任务，写事件和审计。 |
| 事件 | `OutboundOrderShipped`、`HandoverCompleted`。 |
| 异常 | 中央库存扣减失败进入补偿，发货状态标记待记账。 |

### 6.4 创建退货入库

`POST /api/wms/v1/return-inbound-orders`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `ReturnInboundController.create(CreateReturnInboundRequest)` 校验售后来源和幂等键。 |
| 应用层 | `ReturnInboundApplicationService.create(command)` 校验 OMS 售后单、仓库、货主、SKU。 |
| 领域层 | 入库单聚合按退货场景创建待收货入库单。 |
| 基础设施层 | 保存退货入库单，写事件和审计。 |
| 事件 | `ReturnInboundOrderCreated`。 |
| 异常 | 来源售后单重复幂等返回历史结果。 |

### 6.5 创建盘点计划

`POST /api/wms/v1/stocktakes`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `StocktakeController.create(CreateStocktakeRequest)` 接收盘点范围、冻结策略和幂等键。 |
| 应用层 | `StocktakeApplicationService.create(command)` 校验盘点权限、仓库/库区范围和策略。 |
| 领域层 | `StocktakePlanAggregate.create(...)` 校验盘点范围不与进行中计划冲突。 |
| 基础设施层 | 保存盘点计划和任务，必要时冻结库内库存，写事件和审计。 |
| 事件 | `StocktakePlanCreated`。 |
| 异常 | 范围过大进入异步生成任务；范围冲突返回 `409`。 |

### 6.6 确认盘点差异

`POST /api/wms/v1/stocktakes/{stocktakeNo}/confirm-difference`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `StocktakeController.confirmDifference(stocktakeNo, ConfirmDifferenceRequest)` 接收差异确认和审批引用。 |
| 应用层 | `StocktakeApplicationService.confirmDifference(command)` 校验盘点状态、差异阈值、审批权限。 |
| 领域层 | `StocktakePlanAggregate.confirmDifference(...)` 锁定最终差异。 |
| 基础设施层 | 保存差异结果，调用中央库存盘点差异确认，写事件和审计。 |
| 事件 | `StocktakeDifferenceConfirmed`。 |
| 异常 | 差异超阈值无审批返回 `403`；中央库存失败进入补偿。 |

### 6.7 处理仓内异常

`POST /api/wms/v1/exceptions/{exceptionNo}/process`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `WarehouseExceptionController.process(exceptionNo, ProcessExceptionRequest)` 接收处理方案和附件。 |
| 应用层 | `WarehouseExceptionApplicationService.process(command)` 校验处理人、责任方和影响单据。 |
| 领域层 | `WarehouseExceptionAggregate.process(...)` 推进异常状态，记录处理结果。 |
| 基础设施层 | 保存处理记录、附件、事件和审计。 |
| 事件 | `WarehouseExceptionProcessed`。 |
| 异常 | 未处理关联单据不可关闭；处理方案非法返回 `422`。 |

## 7. 操作日志和枚举接口

### 7.1 查询操作日志

`GET /api/wms/v1/operation-logs`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `WmsOperationLogController.page(OperationLogPageQuery)` 接收对象类型、对象单号、操作人和时间。 |
| 应用层 | `WmsOperationLogQueryService.page(query)` 校验审计权限和仓库/货主范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询 `wms_operation_audit_log` 或 ES 审计索引。 |
| 返回 | 操作时间、操作人、设备、操作类型、前后状态、traceId。 |
| 异常 | 查询跨度过大返回 `400`。 |

### 7.2 查询枚举项

`GET /api/wms/v1/enums`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `WmsEnumController.list(EnumQuery)` 接收枚举类型。 |
| 应用层 | `WmsEnumQueryService.list(query)` 校验枚举读权限。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询枚举配置表和缓存。 |
| 返回 | 枚举编码、名称、排序、启停状态。 |
| 异常 | 枚举类型不存在返回空列表。 |

## 8. 事件消费接口与监听器

### 8.1 通用事件入口

`POST /internal/wms/v1/events`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `WmsEventController.receive(EventEnvelope)` 校验事件信封、来源系统和事件版本。 |
| 应用层 | `WmsEventDispatchService.dispatch(event)` 先写 `wms_event_consume_log`，再按事件类型分发。 |
| 领域层 | 根据事件类型创建/取消入库出库，或回写库存/TMS 状态。 |
| 基础设施层 | 写 Inbox、业务表和审计。 |
| 返回 | `eventId`、`consumeStatus`。 |
| 异常 | 重复事件返回历史结果；缺前置单据进入待重试。 |

### 8.2 RocketMQ 监听器

`WmsRocketMqEventListener`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | Listener 接收采购、供应商、OMS、主数据、中央库存、TMS 事件。 |
| 应用层 | 调用同一套 `WmsEventDispatchService`，保证 MQ 和 HTTP 事件入口逻辑一致。 |
| 领域层 | 聚合按事件事实推进状态，不允许事件处理器直接改表。 |
| 基础设施层 | Inbox 幂等、失败重试、死信告警。 |
| 返回 | MQ ack 或重试。 |
| 异常 | 系统异常重试；业务异常写失败原因并进入人工处理。 |

## 继续上下文

当前结论：WMS `03` 已按供应商接口逐项格式展开，覆盖工作台、入库、收货、质检、上架、库存、出库、波次、拣货、容器、包装、发货、退货、盘点、异常、日志、枚举和事件入口。  
关键假设：WMS 拥有仓内作业事实，中央库存拥有全局账户事实。  
待决问题：PDA 离线策略、超收/短拣策略、盘点差异审批阈值。  
下一步：可继续补充每个接口的请求/响应 DTO 字段。
