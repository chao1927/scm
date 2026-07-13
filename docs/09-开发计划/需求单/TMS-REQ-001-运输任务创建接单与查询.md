# TMS-REQ-001 运输任务创建、接单与查询

## 1. 目标

为 OMS、采购、WMS、退供和调拨等来源系统提供统一运输任务入口。TMS 成为运输任务状态、承运接单和后续运单/轨迹的事实源。

## 2. 范围

| 项 | 内容 |
| --- | --- |
| 对应计划 | `TMS-API-001 创建、接单与查询运输任务` |
| 写接口 | `POST /openapi/tms/v1/transport-tasks`；`POST /api/tms/v1/transport-tasks/{taskNo}/accept` |
| 读接口 | `GET /api/tms/v1/transport-tasks`；`GET /api/tms/v1/transport-tasks/{taskNo}` |
| 聚合 | `TransportTaskAggregate` |
| 事件 | `TransportTaskCreated`、`TransportTaskAccepted` |
| 持久化 | `tms_transport_task`、`tms_domain_event`、`tms_operation_log` |

## 3. 验收标准

1. 同一 `sourceSystem + sourceOrderNo + scenario` 的有效运输任务重复创建时返回既有任务，不重复写业务单。
2. 运输任务必须包含来源、场景、货主、仓库、起止地址、包裹、物流产品和费用责任。
3. 接单只能在待接单状态执行，并校验期望版本；成功后状态变为已接单。
4. 创建和接单都写 Outbox 事件和操作日志。
5. 查询支持来源系统、场景、状态、仓库、承运商和分页。
6. 领域、应用服务和接口层测试通过。

## 4. 风险与后续

首切片不直接调用真实承运商、主数据、权限和消息队列；这些通过后续 `TMS-API-002` 至 `TMS-API-005` 补齐。
