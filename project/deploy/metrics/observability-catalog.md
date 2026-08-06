# SCM 可观测性指标目录

## 值班问题与信号

| 值班问题 | 主信号 | 辅助信号 | 处理入口 |
| --- | --- | --- | --- |
| 九个服务是否仍能对外提供能力？ | `up`、readiness、HTTP 5xx 比例 | JVM/进程重启次数 | `ScmServiceUnavailable` |
| 用户请求是否变慢或失败？ | HTTP 请求速率、错误率、p95/p99 延迟 | JDBC 连接池饱和度 | `ScmHttpErrorRateHigh`、`ScmHttpP99LatencyHigh` |
| 业务事实是否及时送达？ | Outbox 最老待投递时长、RocketMQ 消费延迟 | 重试失败数量、DLQ 数量 | `ScmOutboxOldestPending`、`ScmRocketMqConsumerLagHigh` |
| 依赖故障影响到哪些服务？ | MySQL/Redis/Nacos/Dubbo 依赖调用错误率和延迟 | 连接池、客户端重连次数 | `ScmDatabasePoolSaturated`、`ScmDubboNoProvider` |

## 指标规范

- HTTP 使用 Micrometer 自动提供的 RED 指标；路由标签必须是模板路径，禁止使用原始 URL、用户 ID、订单号和异常文本。仓库离线环境只强制 Actuator 健康探针，Prometheus/OTel Registry 由部署平台按 Spring Boot 4 兼容版本注入，禁止混用本地旧版 Registry。
- 服务和依赖标签只允许九个固定服务名及 `mysql/redis/rocketmq/nacos/dubbo` 固定集合。
- 延迟必须使用直方图计算 p95/p99，不使用平均值作为告警依据。
- Outbox/Inbox 定时盘点结果应转成 `scm_event_pending_total`、`scm_event_failed_total`、`scm_event_oldest_seconds`；在接入指标采集前可先使用 `event-ledger-audit.sh` 只读输出。
- 日志必须携带 `traceId`/`spanId`，不得输出密码、Token、MFA 密钥、完整请求体或未脱敏个人信息。

## SLO 占位

仓库模板使用保守的本地/测试阈值。生产启用前必须结合容量压测和业务 RTO/RPO 审批调整 `alerts/scm-alert-rules.yml`，不得把占位阈值直接视为生产承诺。
