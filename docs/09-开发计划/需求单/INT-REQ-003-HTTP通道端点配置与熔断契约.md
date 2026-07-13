# INT-REQ-003 HTTP 通道端点配置与熔断契约

## 目标

在集成中心投递运行面的基础上，补齐可配置的 HTTP/OpenAPI 真实传输通道，使跨系统事件和命令能按目标系统端点投递，并具备超时、连续失败熔断、契约请求头和端点运维能力。

## 范围

| 类型 | 内容 |
| --- | --- |
| 接口 | `POST /api/integration/v1/endpoints`、`GET /api/integration/v1/endpoints`、`POST /api/integration/v1/endpoints/{endpointNo}/disable` |
| 传输 | `ConfiguredIntegrationTransportAdapter` 支持 `HTTP`/`OPENAPI` 通道，使用 Java `HttpClient` 真实 POST 目标端点 |
| 配置 | `int_endpoint` 保存目标系统、通道类型、URL、超时时间、失败阈值、连续失败次数、状态和版本 |
| 契约 | 传输请求必须携带 `X-Integration-Message-No`、`X-Source-System`、`X-Target-System`、`X-Business-No`、`X-Idempotency-Key` |
| 熔断 | HTTP 失败累计达到端点阈值后停用端点；后续投递返回端点未配置/不可用失败，继续进入消息重试/死信链路 |

## 验收

- 能创建、查询、停用 HTTP/OpenAPI 端点。
- HTTP 请求构造包含统一契约请求头、目标 URL 和超时。
- HTTP 投递失败会累计端点连续失败次数，达到阈值后熔断端点。
- 投递失败仍沿用集成消息 `FAILED -> DEAD_LETTER -> REPLAYED` 补偿链路。
- `integration-service` 测试和全量 Maven 回归通过。

## 不在本单范围

- RocketMQ Producer 和 Dubbo 泛化调用需要真实中间件、注册中心和依赖矩阵，作为 `INT-REQ-004` 接入运行环境后实施。
