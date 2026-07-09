# 03-TMS系统接口逐项实现设计

> 本文承接 [02-TMS系统接口事件实现逻辑](02-TMS系统接口事件实现逻辑.md)，继续把 `docs/06-子系统接口设计/06-TMS系统接口设计.md` 中的接口拆成可编码实现小节。每个接口按“接口层 -> 应用层 -> 领域层 -> 基础设施层 -> 事件/返回 -> 异常”说明。

## 1. 统一代码分层

### 1.1 包结构

```text
tms-service
  ├── interfaces
  │   ├── web
  │   ├── openapi
  │   ├── carrier
  │   ├── mq
  │   └── job
  ├── application
  │   ├── command
  │   ├── query
  │   ├── event
  │   └── dto
  ├── domain
  │   ├── task
  │   ├── waybill
  │   ├── label
  │   ├── tracking
  │   ├── receipt
  │   ├── exception
  │   ├── fee
  │   ├── carrier
  │   └── shared
  └── infrastructure
      ├── persistence
      ├── carrier
      ├── rpc
      ├── mq
      ├── idempotency
      ├── audit
      └── file
```

### 1.2 命名约定

| 类型 | 示例 | 说明 |
| --- | --- | --- |
| Controller | `TransportTaskController` | 前端 HTTP 接口 |
| OpenApiController | `TransportTaskOpenApiController` | OMS/WMS/采购等来源系统入口 |
| CarrierCallbackController | `CarrierCallbackController` | 承运商回调入口 |
| QueryService | `WaybillQueryService` | 查询读模型 |
| ApplicationService | `WaybillApplicationService` | 编排权限、幂等、承运商调用、事务、事件 |
| Aggregate | `WaybillAggregate` | 保护运单状态机 |
| DomainEvent | `WaybillCreatedEvent` | 写入 `tms_domain_event` |

## 2. 工作台接口

### 2.1 查询工作台统计

`GET /api/tms/v1/workbench/summary`

| 层     | 实现逻辑                                                                               |
| ----- | ---------------------------------------------------------------------------------- |
| 接口层   | `TmsWorkbenchController.summary(WorkbenchSummaryQueryRequest)` 解析组织、仓库、承运商、时间范围。   |
| 应用层   | `TmsWorkbenchQueryService.querySummary(query)` 校验 `tms:workbench:read` 和承运/仓库数据范围。 |
| 领域层   | 无聚合行为；这是读模型查询。                                                                     |
| 基础设施层 | 聚合查询待接单、待建单、轨迹异常、签收异常、费用推送失败读模型。                                                   |
| 返回    | 待创建运单、待同步轨迹、待处理异常、待推送 BMS 费用来源数量。                                                  |
| 异常    | 无权限返回 `403`；数据范围为空返回空统计。                                                           |

### 2.2 查询待办列表

`GET /api/tms/v1/workbench/todos`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `TmsWorkbenchController.todos(TodoPageQueryRequest)` 接收 `todoType/pageNo/pageSize`。 |
| 应用层 | `TmsTodoQueryService.pageTodos(query)` 校验工作台读权限和数据范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 从运输任务、运单、轨迹、签收、异常、费用来源读模型或统一待办表查询。 |
| 返回 | `todoId`、`businessType`、`businessNo`、`title`、`statusName`、`targetRoute`。 |
| 异常 | `pageSize` 超限制返回 `400`；数据范围为空返回空分页。 |

## 3. 运输任务和运单接口

### 3.1 查询运输任务列表

`GET /api/tms/v1/transport-tasks`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `TransportTaskController.page(TransportTaskPageQuery)` 接收来源系统、仓库、承运商、状态和分页。 |
| 应用层 | `TransportTaskQueryService.page(query)` 校验运输任务读权限、来源系统和承运商范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询任务、运单、轨迹摘要读模型。 |
| 返回 | 任务号、来源单、承运商、物流产品、任务状态、运单状态。 |
| 异常 | 来源单无权限返回空分页。 |

### 3.2 外部创建运输任务

`POST /openapi/tms/v1/transport-tasks`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `TransportTaskOpenApiController.create(CreateTransportTaskRequest)` 校验来源系统、签名和幂等键。 |
| 应用层 | `TransportTaskApplicationService.createFromSource(command)` 校验地址、包裹、物流产品和承运能力。 |
| 领域层 | `TransportTaskAggregate.create(...)` 创建待接单/待建单任务；承运能力服务判断是否可承运。 |
| 基础设施层 | 保存运输任务，写事件和审计。 |
| 事件 | `TransportTaskCreated`；承运失败写 `CarrierValidationFailed`。 |
| 异常 | 禁运、超范围、地址异常返回 `422`；重复来源单幂等返回历史结果。 |

### 3.3 接单运输任务

`POST /api/tms/v1/transport-tasks/{taskNo}/accept`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `TransportTaskController.accept(taskNo, AcceptTaskRequest)` 接收承运方和版本。 |
| 应用层 | `TransportTaskApplicationService.accept(command)` 校验接单权限、承运商范围和幂等。 |
| 领域层 | `TransportTaskAggregate.accept(...)` 状态待接单 -> 已接单。 |
| 基础设施层 | 更新任务状态，写审计。 |
| 事件 | `TransportTaskAccepted`。 |
| 异常 | 已取消或已建单返回 `409 STATE_CONFLICT`。 |

### 3.4 创建运单

`POST /api/tms/v1/transport-tasks/{taskNo}/create-waybill`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `TransportTaskController.createWaybill(taskNo, CreateWaybillRequest)` 接收承运商产品和幂等键。 |
| 应用层 | `WaybillApplicationService.createFromTask(command)` 校验任务状态，调用承运商网关建单。 |
| 领域层 | `WaybillAggregate.create(...)` 保存承运商单号、包裹、面单状态。 |
| 基础设施层 | `CarrierGateway` 调用承运商 API，保存运单，写事件和审计。 |
| 事件 | `WaybillCreated`；失败写 `WaybillCreateFailed`。 |
| 异常 | 承运商超时进入待确认，Job 查询结果补偿，避免重复建单。 |

### 3.5 作废运单

`POST /api/tms/v1/waybills/{waybillNo}/cancel`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `WaybillController.cancel(waybillNo, CancelWaybillRequest)` 接收作废原因和审批引用。 |
| 应用层 | `WaybillApplicationService.cancel(command)` 校验作废权限、运单状态和承运商规则。 |
| 领域层 | `WaybillAggregate.voidWaybill(...)` 只允许未揽收/未签收运单作废。 |
| 基础设施层 | 调用承运商作废接口，更新运单状态，写事件和审计。 |
| 事件 | `WaybillVoided`。 |
| 异常 | 承运商拒绝作废时保持有效并记录失败原因。 |

### 3.6 同步运单轨迹

`POST /api/tms/v1/waybills/{waybillNo}/sync-tracks`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `WaybillController.syncTracks(waybillNo, SyncTracksRequest)` 接收同步原因。 |
| 应用层 | `TrackingApplicationService.syncFromCarrier(command)` 调用承运商轨迹查询，按节点幂等追加。 |
| 领域层 | `TrackingAggregate.append(...)` 追加新轨迹，不覆盖历史轨迹。 |
| 基础设施层 | 保存轨迹节点，写事件和审计。 |
| 事件 | `TrackingAppended`。 |
| 异常 | 承运商失败生成同步失败记录，可重试。 |

## 4. 面单、轨迹、签收接口

### 4.1 生成面单

`POST /api/tms/v1/waybills/{waybillNo}/labels`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `ShippingLabelController.generate(waybillNo, GenerateLabelRequest)` 接收模板、包裹和幂等键。 |
| 应用层 | `ShippingLabelApplicationService.generate(command)` 校验运单有效、包裹完整和模板配置。 |
| 领域层 | `ShippingLabelAggregate.generate(...)` 创建面单记录。 |
| 基础设施层 | 调用承运商/模板服务生成文件，保存对象存储地址，写事件和审计。 |
| 事件 | `ShippingLabelGenerated`。 |
| 异常 | 面单生成失败保留承运商错误码。 |

### 4.2 打印面单

`POST /api/tms/v1/shipping-labels/{labelNo}/print`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `ShippingLabelController.print(labelNo, PrintLabelRequest)` 接收打印机和打印场景。 |
| 应用层 | `ShippingLabelApplicationService.print(command)` 校验打印权限和面单状态。 |
| 领域层 | `ShippingLabelAggregate.print(...)` 增加打印次数，记录设备。 |
| 基础设施层 | 调用打印服务或返回面单文件，写事件和审计。 |
| 事件 | `ShippingLabelPrinted`。 |
| 异常 | 面单已作废不可打印。 |

### 4.3 查询运单轨迹

`GET /api/tms/v1/waybills/{waybillNo}/tracks`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `TrackingController.list(waybillNo)`。 |
| 应用层 | `TrackingQueryService.listByWaybill(waybillNo)` 校验运单读权限和来源范围。 |
| 领域层 | 无聚合行为。 |
| 基础设施层 | 查询轨迹读模型，按发生时间和接收时间排序。 |
| 返回 | 轨迹节点、地点、发生时间、来源、原始节点编码。 |
| 异常 | 乱序轨迹保留原始顺序字段，不回退终态。 |

### 4.4 人工补录轨迹

`POST /api/tms/v1/waybills/{waybillNo}/tracks`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `TrackingController.supplement(waybillNo, SupplementTrackRequest)` 接收节点、地点、时间、原因。 |
| 应用层 | `TrackingApplicationService.supplement(command)` 校验补录权限、审批引用和幂等。 |
| 领域层 | `TrackingAggregate.supplement(...)` 追加人工轨迹节点。 |
| 基础设施层 | 保存轨迹、附件、事件和审计。 |
| 事件 | `TrackingSupplemented`。 |
| 异常 | 高风险节点无审批不可补录。 |

### 4.5 签收冲正

`POST /api/tms/v1/delivery-receipts/{receiptNo}/correct`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `DeliveryReceiptController.correct(receiptNo, CorrectReceiptRequest)` 接收新签收结果、原因和审批引用。 |
| 应用层 | `DeliveryReceiptApplicationService.correct(command)` 校验冲正权限、审批、费用/对账影响。 |
| 领域层 | `DeliveryReceiptAggregate.correct(...)` 生成冲正事实，不覆盖原签收事实。 |
| 基础设施层 | 保存冲正记录、事件和审计，通知 OMS/采购/BMS。 |
| 事件 | `DeliveryReceiptCorrected`。 |
| 异常 | 已财务结算的冲正需额外审批。 |

### 4.6 上传签收证明

`POST /api/tms/v1/delivery-receipts/{receiptNo}/proof`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `DeliveryReceiptController.uploadProof(receiptNo, UploadProofRequest)` 接收附件。 |
| 应用层 | `DeliveryReceiptApplicationService.uploadProof(command)` 校验文件类型、大小和签收记录权限。 |
| 领域层 | 签收记录聚合关联证明附件，不改变签收事实。 |
| 基础设施层 | 对象存储保存文件，写附件记录和审计。 |
| 返回 | 附件 ID、文件地址、上传状态。 |
| 异常 | 文件上传失败不改签收状态。 |

## 5. 异常、费用来源和配置接口

### 5.1 登记物流异常

`POST /api/tms/v1/exceptions`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `LogisticsExceptionController.create(CreateExceptionRequest)` 接收异常类型、责任方、影响单据。 |
| 应用层 | `LogisticsExceptionApplicationService.create(command)` 校验异常登记权限和关联运单。 |
| 领域层 | `LogisticsExceptionAggregate.register(...)` 创建异常，记录责任方和影响范围。 |
| 基础设施层 | 保存异常、附件、事件和审计。 |
| 事件 | `LogisticsExceptionRegistered`。 |
| 异常 | 重复异常按来源单+类型合并或关联。 |

### 5.2 关闭物流异常

`POST /api/tms/v1/exceptions/{exceptionNo}/close`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `LogisticsExceptionController.close(exceptionNo, CloseExceptionRequest)` 接收处理结果。 |
| 应用层 | `LogisticsExceptionApplicationService.close(command)` 校验关闭权限、责任处理和费用影响。 |
| 领域层 | `LogisticsExceptionAggregate.close(...)` 状态 -> 已关闭。 |
| 基础设施层 | 保存处理结果，写事件和审计。 |
| 事件 | `LogisticsExceptionClosed`。 |
| 异常 | 未处理责任或费用影响不可关闭。 |

### 5.3 生成物流费用来源

`POST /api/tms/v1/waybills/{waybillNo}/generate-fee-source`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `LogisticsFeeSourceController.generate(waybillNo, GenerateFeeSourceRequest)` 接收账期和幂等键。 |
| 应用层 | `LogisticsFeeSourceApplicationService.generate(command)` 从运单、包裹、轨迹、签收提取计费指标。 |
| 领域层 | `LogisticsFeeSourceAggregate.generate(...)` 创建费用来源，保证同运单同费用类型不重复。 |
| 基础设施层 | 保存费用来源，写事件和审计。 |
| 事件 | `LogisticsFeeSourceGenerated`。 |
| 异常 | 缺少重量、体积、签收等必要计费指标返回 `422`。 |

### 5.4 推送 BMS 费用来源

`POST /api/tms/v1/fee-sources/{feeSourceNo}/push-bms`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `LogisticsFeeSourceController.pushBms(feeSourceNo, PushBmsRequest)` 接收推送原因。 |
| 应用层 | `LogisticsFeeSourceApplicationService.pushBms(command)` 校验费用来源状态和幂等。 |
| 领域层 | `LogisticsFeeSourceAggregate.markPushing(...)` 标记推送中。 |
| 基础设施层 | 调用 BMS 采集接口或发布事件，回写推送状态。 |
| 事件 | `LogisticsFeeSourcePushed` 或 `LogisticsFeeSourcePushFailed`。 |
| 异常 | BMS 失败进入重推任务；已采集后禁止重复推送。 |

### 5.5 测试承运商接口配置

`POST /api/tms/v1/carrier-integrations/{configId}/test`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `CarrierIntegrationController.test(configId, TestCarrierRequest)` 接收测试参数。 |
| 应用层 | `CarrierIntegrationApplicationService.test(command)` 校验配置权限，解密测试密钥。 |
| 领域层 | 配置规则服务校验承运商接口字段完整性。 |
| 基础设施层 | 调用承运商测试 API，记录测试结果和审计。 |
| 返回 | 测试是否成功、承运商响应码、耗时、失败原因。 |
| 异常 | 测试失败不可启用配置。 |

## 6. 承运商回调和事件入口

### 6.1 承运商回调

`POST /callback/tms/v1/carriers/{carrierCode}`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `CarrierCallbackController.receive(carrierCode, RawCallbackRequest)` 校验承运商签名和原始报文。 |
| 应用层 | `CarrierCallbackApplicationService.receive(command)` 先保存原文，按 `carrierCode` 选择适配器解析。 |
| 领域层 | 轨迹、签收、异常聚合根据解析结果追加事实。 |
| 基础设施层 | 写回调消息、轨迹/签收/异常、事件和审计。 |
| 事件 | `TrackingAppended`、`TransportSigned`、`TransportRejected`、`LogisticsExceptionRegistered`。 |
| 异常 | 解析失败保留原文并进入回调异常页；重复回调幂等返回成功。 |

### 6.2 通用事件入口

`POST /internal/tms/v1/events`

| 层 | 实现逻辑 |
| --- | --- |
| 接口层 | `TmsEventController.receive(EventEnvelope)` 校验事件信封、来源系统和事件版本。 |
| 应用层 | `TmsEventDispatchService.dispatch(event)` 先写 `tms_event_consume_log`，再分发 OMS/WMS/BMS/主数据事件处理器。 |
| 领域层 | 根据事件创建运输任务、补充包裹、回写 BMS 采集状态。 |
| 基础设施层 | 保存 Inbox、业务状态和审计。 |
| 返回 | `eventId`、`consumeStatus`。 |
| 异常 | 重复事件返回历史结果；缺前置运单进入待重试。 |

## 继续上下文

当前结论：TMS `03` 已按供应商接口逐项格式展开，覆盖工作台、运输任务、运单、面单、轨迹、签收、异常、费用来源、承运商配置、回调和事件入口。  
关键假设：承运商接口不稳定，需要原文落库、幂等、待确认和补偿。  
待决问题：承运商签名算法、面单文件有效期、签收冲正审批。  
下一步：可继续补充每个接口的请求/响应 DTO 字段。
