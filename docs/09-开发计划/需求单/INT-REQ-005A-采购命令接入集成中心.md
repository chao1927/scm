# INT-REQ-005A 采购命令接入统一集成中心

> 历史需求：`ARCH-REQ-001` 已将本方案改为采购系统直接按 `scm.integration.routes.*.url` 路由到目标上下文，并保留采购侧重试、熔断、最终失败和人工重放；不再调用独立集成中心。

## 目标

将采购 Outbox 产生的跨系统命令投递到统一集成中心，替换固定失败占位实现，并建立可重试、可熔断、可查询、可人工重放的运行闭环。

## 契约

| 项目 | 规则 |
| --- | --- |
| 投递 | HTTP `POST /openapi/integration/v1/commands`，携带 PURCHASE 应用 JWT 和命令 ID 幂等键 |
| 超时 | 连接 1s、读取 3s，可配置 |
| 重试 | 指数退避，最大 300s，默认最多 8 次 |
| 熔断 | 连续 5 次失败后打开 30s，可配置；成功后复位 |
| 成功 | 回写集成中心 `messageNo` 和完成时间 |
| 最终失败 | 进入状态 5，保留错误、重试次数和下次时间 |
| 人工补偿 | 需 `purchase:integration-command:replay` 权限和幂等键，仅最终失败状态可重放 |

## 接口

- `GET /api/purchase/v1/operations/failed-commands`
- `POST /api/purchase/v1/operations/failed-commands/{id}/replay`

## 验收

- `PurchaseIntegrationCommandDispatcherTest` 覆盖成功回写、退避和最终失败。
- `HttpIntegrationCommandGatewayTest` 覆盖缺少应用令牌和熔断。
- `PurchaseOperationsApplicationServiceTest` 覆盖重放权限。
- `mvn -q -pl purchase-service -am test` 通过。
