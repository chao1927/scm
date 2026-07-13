# INT-REQ-002 集成中心投递运行面与尝试日志

## 目标

在 `INT-REQ-001` 的路由、接入、失败、死信和人工重放基础上，补齐集成中心运行面：批量扫描待投递/失败消息，按路由通道调用传输适配器，记录每次投递尝试，并提供运维汇总查询。

## 范围

| 类型 | 内容 |
| --- | --- |
| 接口 | `POST /api/integration/v1/dispatch-runs`、`GET /api/integration/v1/delivery-attempts`、`GET /api/integration/v1/operations/summary` |
| 应用服务 | `IntegrationDispatchRuntimeApplicationService` 扫描待投递/失败消息、调用传输端口、复用既有派发状态机、写入投递尝试日志 |
| 基础设施 | `IntegrationTransportPort`、`ConfiguredIntegrationTransportAdapter`、`int_delivery_attempt` |
| 不变量 | 只扫描待投递/失败消息；每次投递必须写尝试日志；无启用路由视为失败并进入重试/死信链路；派发结果必须复用 `IntegrationMessageAggregate` 状态机 |
| 事件/补偿 | 失败继续沿用 `FAILED -> DEAD_LETTER -> REPLAYED`；死信仍通过既有人工重放接口补偿 |

## 验收

- 批量投递成功时消息进入已派发，并写成功尝试日志。
- 连续投递失败达到阈值时消息进入死信，并写失败尝试日志。
- 缺少启用路由时记录失败尝试，不丢业务载荷。
- 运维汇总能返回待投递、已派发、失败、死信和已重放数量。
- `integration-service` 模块测试和全量 Maven 回归通过。
