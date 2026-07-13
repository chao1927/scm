# TMS-REQ-004 物流异常与费用来源

## 1. 目标

在运输任务、运单、轨迹和签收事实之后，补齐物流异常处理和 BMS 费用来源生成能力，使 TMS 能向结算系统提供可追溯的费用事实、索赔依据和异常责任结论。

## 2. 范围

| 项 | 内容 |
| --- | --- |
| 对应计划 | `TMS-API-004 物流异常与费用来源` |
| 写接口 | `POST /api/tms/v1/transport-exceptions`；`POST /api/tms/v1/transport-exceptions/{exceptionNo}/close`；`POST /api/tms/v1/waybills/{waybillNo}/fee-sources`；`POST /api/tms/v1/fee-sources/{feeSourceNo}/push-bms` |
| 读接口 | `GET /api/tms/v1/transport-exceptions`；`GET /api/tms/v1/fee-sources` |
| 聚合 | `LogisticsExceptionAggregate`、`LogisticsFeeSourceAggregate` |
| 事件 | `LogisticsExceptionRegistered`、`LogisticsExceptionClosed`、`LogisticsFeeSourceGenerated`、`LogisticsFeeSourcePushed`、`LogisticsFeeSourcePushFailed` |
| 持久化 | `tms_logistics_exception`、`tms_logistics_fee_source`，复用 `tms_domain_event`、`tms_operation_log` |

## 3. 验收标准

1. 异常登记必须绑定运单、异常类型、级别和描述。
2. 异常关闭必须填写处理结果和责任方，并校验期望版本。
3. 同一运单同一费用项重复生成费用来源时返回既有来源。
4. 费用来源必须包含承运商、物流产品、费用项、金额、币种、账期和责任方。
5. 推送 BMS 以 Outbox 事件表达，不直接修改 BMS 数据。
6. 领域、应用服务和接口层测试通过。

## 4. 风险与后续

首轮不实现真实 BMS RPC、索赔附件、异常自动派单、费用规则计算和 BMS 采集回执；这些进入 BMS 和生产化联调阶段继续增强。
