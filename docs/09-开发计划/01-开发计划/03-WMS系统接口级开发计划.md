# WMS 系统接口级开发计划

> 当前状态与剩余验收见[供应链系统开发总纲](../00-供应链系统开发总纲.md#5-九子系统当前状态)。

实现资料：`docs/08-系统实现/03-WMS系统实现/03-WMS系统接口逐项实现设计.md`。

## WMS-API-001 外部创建/取消入库单
`POST /openapi/wms/v1/inbound-orders`、`POST /inbound-orders/{id}/cancel`

- 接口层：`InboundOrderOpenApiController` 校验来源系统、事件编码、幂等键和 `inboundType=PURCHASE/TRANSFER/SALES_RETURN`；`InboundOrderController.cancel` 校验仓库范围。
- 应用层：`InboundOrderApplicationService` 按业务类型加载采购订单/调拨单/售后单快照，处理创建、预约和取消补偿。
- 领域层：`InboundOrderAggregate` 保证“来源上下文 + 来源单号 + 来源行 + 业务类型”唯一、未收货才可取消；不同业务类型不得共用含义冲突的状态和数量字段。
- 基础设施层：入库单资源库、Inbox、采购/供应商 ACL、Outbox。
- 事件：消费采购订单/ASN、调拨入库、OMS 售后退货命令；生产 `WmsInboundOrderCreated/Cancelled`，载荷必须携带业务类型和来源引用。
- 设计事实源：[入库单聚合多类型规则](../../03-核心业务模型/03-WMS领域模型/02-入库单聚合CQRS设计.md#13-多类型入库规则doc-next-001)。`ReturnOperation/TransferOperation` 只作为执行聚合并关联统一入库单；外部类型经 ACL 归一为 `PURCHASE/TRANSFER/SALES_RETURN`。

```mermaid
flowchart LR
  P[采购或供应商] --> O[InboundOrderOpenApiController]
  O --> I[Inbox]
  I --> A[InboundOrderApplicationService]
  A --> G[InboundOrderAggregate]
  G --> E[WmsInboundOrderCreated]
```

## WMS-API-002 PDA 扫码收货与提交收货
`POST /receipts/{id}/scan`、`POST /receipts/{id}/complete`

- 接口层：`ReceiptPdaController` 校验 PDA 用户、仓库、条码、批次、效期和幂等键。
- 应用层：`ReceivingApplicationService` 累积行级实收/拒收，提交时校验收货汇总与入库单数量。
- 领域层：`ReceiptAggregate` 保护收货行不超通知，拒收必须给原因；完成后不能再扫码。
- 基础设施层：收货头行、扫码流水、条码解析器、库存记账 Outbox。
- 事件：生产 `WmsArrivalRegistered/WmsReceiptCompleted`；供应商、采购、中央库存消费。

```mermaid
sequenceDiagram
  participant P as PDA收货员
  participant C as ReceiptPdaController
  participant A as ReceivingApplicationService
  participant G as ReceiptAggregate
  participant O as Outbox
  P->>C: 扫码收货
  C->>A: ScanReceiptCommand
  A->>G: receiveLine
  P->>C: 提交完成
  A->>G: complete
  A->>O: WmsReceiptCompleted
```

## WMS-API-003 质检与上架
`POST /quality-inspections/{id}/result`、`POST /putaway-tasks/{id}/scan`

- 接口层：`QualityInspectionController`、`PutawayTaskController` 接收质检结果/目标库位和操作版本。
- 应用层：质检服务按采购、调拨、售后退货生成合格/不合格/隔离处置；上架服务调用库位推荐并提交库内记账。
- 领域层：`QualityInspectionAggregate` 保护抽检/全检状态；`PutawayTaskAggregate` 只允许可上架数量进入允许库位。售后退货必须记录原订单/售后行、实收、错退、少件、良品和不良品数量。
- 基础设施层：质检/上架资源库、库位容量查询、库存 ACL。
- 事件：生产 `QualityInspectionCompleted/PutawayCompleted`；采购不合格通知供应商，调拨差异通知中央库存，售后退货发布 `ReturnInspected` 供 OMS/BMS/库存消费。
- 测试补充：三类来源复合唯一键、到达/收货不提前增加可用、调拨最终实收与差异守恒、售后五类处置守恒、重复/乱序事实无副作用。

```mermaid
flowchart LR
  Q[质检结果] --> I[QualityInspectionAggregate]
  I --> OK[合格库存]
  I --> NG[不合格区库存]
  OK --> P[PutawayTaskAggregate]
  P --> S[仓内库存]
```

## WMS-API-004 外部创建出库单与库存分配
`POST /openapi/wms/v1/outbound-orders`、`POST /outbound-orders/{id}/allocate|cancel`

- 接口层：`OutboundOrderOpenApiController`、`OutboundOrderController` 校验 OMS/退供/调拨来源、仓库和业务键。
- 应用层：`OutboundOrderApplicationService` 编排库存分配、取消释放和缺货异常。
- 领域层：`OutboundOrderAggregate` 保证可分配数量不超过库内可用；取消前检查拣货/交接状态。
- 基础设施层：出库资源库、仓内库存资源库、中央库存 ACL、异常表。
- 事件：消费 OMS/供应商/调拨命令；生产 `WmsOutboundAllocated/Cancelled`。

```mermaid
flowchart LR
  OMS[OMS或退供命令] --> O[OutboundOrderAggregate]
  O --> WS[仓内库存]
  WS --> A[分配结果]
  A --> W[波次和拣货]
```

## WMS-API-005 波次、拣货、容器与复核包装
`POST /waves`、`POST /waves/{id}/release`、`POST /pick-tasks/{id}/scan`、`POST /containers/bind`、`POST /packing/{id}/verify`

- 接口层：波次、拣货、容器、复核 Controller 按 PDA/PC 权限区分。
- 应用层：波次服务按规则分组；拣货服务校验库位/容器/数量；复核服务校验拣货与订单行一致。
- 领域层：`WaveAggregate`、`PickTaskAggregate`、`ContainerAggregate`、`PackingAggregate` 防止重复领取、重复拣取和错货发运。
- 基础设施层：波次/任务/容器/包装资源库、条码服务、面单/TMS ACL。
- 事件：生产 `PickCompleted/PackingVerified`；异常写仓内异常聚合。

```mermaid
flowchart LR
  W[波次] --> P[拣货任务]
  P --> C[周转容器]
  C --> V[复核包装]
  V --> H[发货交接]
```

## WMS-API-006 发货交接、盘点和仓内异常
`POST /handovers`、`POST /stocktakes`、`POST /stocktakes/{id}/confirm-difference`、`POST /warehouse-exceptions/{id}/handle`

- 接口层：`ShipmentHandoverController`、`StocktakeController`、`WarehouseExceptionController`。
- 应用层：交接服务调用 TMS；盘点服务生成差异确认命令；异常服务建立责任与补偿任务。
- 领域层：交接聚合确保已复核才能交接；盘点聚合限制差异确认审批；异常聚合限制关闭条件。
- 基础设施层：交接/盘点/异常资源库、TMS/中央库存 ACL、审计与事件表。
- 事件：生产 `WmsShipmentHandedOver/StocktakeDifferenceConfirmed/WarehouseExceptionClosed`。

```mermaid
flowchart LR
  V[复核完成] --> H[发货交接]
  H --> T[TMS建运单]
  P[盘点差异] --> I[中央库存调整]
  E[仓内异常] --> A[责任与补偿]
```

## WMS-NEXT-002 交付证据（2026-08-06）

- `V12__wms_multitype_inbound.sql` 将历史来源类型归一为 `PURCHASE/TRANSFER/SALES_RETURN`，唯一键收敛为 `sourceSystem + sourceOrderNo + sourceLineNo + inboundType`。
- `InboundType` ACL 防止 OMS 伪造采购入库、调拨来源越界；重复命令仅在来源快照完全一致时幂等命中。
- `TransferOperationApplicationService` 创建统一调拨入库单，最终回执携带调出、累计实收和差异数量；`ReturnOperationApplicationService` 关联统一售后入库单并保护五类处置数量守恒。
- 生产事件由 WMS Outbox 投递到真实 RocketMQ，入站命令经 Inbox 幂等消费；不使用 HTTP/日志模拟消费。
- JDK 17 下 `scm-common 9/9 + WMS 50/50` 通过。真实 MySQL/Flyway/RocketMQ 链路归入 `QA-NEXT-001`，WMS 存量 Ali Check 63 项纳入代码注释治理债务。
