# TMS-REQ-002 运单创建、作废、面单生成与打印

## 1. 目标

在运输任务已接单后，建立 TMS 运单与面单事实源，为 WMS 贴标发货、OMS 履约轨迹和后续费用结算提供承运商单号、面单文件和作废审计。

## 2. 范围

| 项 | 内容 |
| --- | --- |
| 对应计划 | `TMS-API-002 创建/作废运单与生成面单` |
| 写接口 | `POST /api/tms/v1/transport-tasks/{taskNo}/waybills`；`POST /api/tms/v1/waybills/{waybillNo}/void`；`POST /api/tms/v1/waybills/{waybillNo}/labels`；`POST /api/tms/v1/shipping-labels/{labelNo}/print` |
| 读接口 | `GET /api/tms/v1/waybills`；`GET /api/tms/v1/waybills/{waybillNo}`；`GET /api/tms/v1/waybills/{waybillNo}/labels` |
| 聚合 | `WaybillAggregate`、`ShippingLabelAggregate` |
| 事件 | `WaybillCreated`、`WaybillVoided`、`ShippingLabelGenerated`、`ShippingLabelPrinted` |
| 持久化 | `tms_waybill`、`tms_shipping_label`，复用 `tms_domain_event`、`tms_operation_log` |

## 3. 验收标准

1. 只有已接单运输任务可以创建有效运单。
2. 同一有效运输任务不能重复创建有效运单，重复创建返回既有运单。
3. 运单作废校验期望版本，只允许未进入后续运输状态的运单作废。
4. 面单必须绑定有效运单和包裹，重复生成返回既有有效面单。
5. 面单打印必须记录打印设备、打印次数和操作日志。
6. 领域、应用服务和接口层测试通过。

## 4. 风险与后续

首轮以承运商回执入参模拟真实承运商下单结果；真实承运商 ACL、对象存储、打印服务、作废回调和面单文件生成将在后续联调中增强。
