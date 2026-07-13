# INT-REQ-004 MQ 与 Dubbo 通道契约预检

## 目标

在 HTTP/OpenAPI 通道已经可配置和可投递的基础上，为 RocketMQ 与 Dubbo 通道补齐统一端点契约和预检能力。该需求单解决“配置是否可被集成中心识别、路由是否可审计、后续接真实 SDK 是否有稳定接缝”的问题。

## 范围

| 类型 | 内容 |
| --- | --- |
| 接口 | `POST /api/integration/v1/endpoints/{endpointNo}/verify` |
| 契约 | RocketMQ 端点使用 `rocketmq://<nameserver>/<topic>?tag=<tag>`；Dubbo 端点使用 `dubbo://<serviceInterface>/<method>?version=<version>&group=<group>` |
| 应用服务 | `IntegrationEndpointContractApplicationService` 对 `HTTP/OPENAPI/ROCKETMQ/DUBBO/LOCAL_ACK/LOCAL_FAIL` 执行端点契约预检 |
| 验证内容 | scheme、nameserver、topic、tag、serviceInterface、method、version、group、timeoutMillis |
| 交付结果 | 端点预检返回是否有效、失败原因和解析后的元数据，供联调和发布前检查使用 |

## 验收

- RocketMQ 端点能解析 nameserver、topic、tag 和消息键口径。
- Dubbo 端点能解析接口、方法、版本、分组和超时。
- HTTP/OpenAPI 端点继续校验 URL scheme、host 和 path。
- 非法端点返回明确失败原因，不进入伪成功。
- `integration-service` 模块测试和全量 Maven 回归通过。

## 不在本单范围

- 真实 RocketMQ Producer、Dubbo 泛化调用需要中间件地址、注册中心、服务接口包或泛化调用依赖，列入 `INT-REQ-005`。
