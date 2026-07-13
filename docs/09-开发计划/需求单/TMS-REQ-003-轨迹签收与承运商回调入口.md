# TMS-REQ-003 轨迹、签收与承运商回调入口

## 1. 目标

补齐 TMS 运输事实回写能力：承运商和人工操作可以追加轨迹、记录签收/拒收，并通过 Inbox 幂等入口沉淀为标准领域事件，供 OMS、采购、WMS、中央库存和 BMS 消费。

## 2. 范围

| 项 | 内容 |
| --- | --- |
| 对应计划 | `TMS-API-003 同步/补录轨迹与签收证明`、`TMS-API-005 承运商回调与通用事件入口` 的首轮 |
| 写接口 | `POST /api/tms/v1/waybills/{waybillNo}/tracks`；`POST /api/tms/v1/delivery-receipts`；`POST /openapi/tms/v1/carrier-callbacks/{carrierCode}` |
| 读接口 | `GET /api/tms/v1/waybills/{waybillNo}/tracks`；`GET /api/tms/v1/delivery-receipts/{receiptNo}` |
| 聚合 | `TrackingAggregate`、`DeliveryReceiptAggregate` |
| 事件 | `TrackingAppended`、`TrackingSupplemented`、`TransportArrived`、`TransportSigned`、`TransportRejected`、`PartialSigned` |
| 持久化 | `tms_tracking_node`、`tms_delivery_receipt`、`tms_event_inbox`，复用 `tms_domain_event`、`tms_operation_log` |

## 3. 验收标准

1. 轨迹只能追加，重复 `waybillNo + nodeCode + trackAt` 返回既有轨迹。
2. 到达节点额外发布 `TransportArrived`。
3. 同一运单只能生成一个有效签收/拒收/部分签收终态记录。
4. 拒收必须填写拒收原因，签收必须填写签收人。
5. 承运商回调先声明 Inbox，重复事件直接忽略，失败事件记录错误。
6. 领域、应用服务和接口层测试通过。

## 4. 风险与后续

首轮不做承运商签名验签、轨迹节点映射配置、附件对象存储、签收冲正和运单终态推进；这些在后续生产化联调中增强。
