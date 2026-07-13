# MDM-REQ-003 发布订阅、回执与重试

## 1. 目标

补齐主数据发布订阅链路，使已生成版本能够按目标系统生成发布日志，下游系统可幂等回传成功或失败回执，失败发布可人工重试。

## 2. 范围

| 项 | 内容 |
| --- | --- |
| 对应计划 | `MDM-API-003 发布订阅、回执与重试` |
| 写接口 | `POST /api/mdm/v1/publication-subscriptions`；`POST /api/mdm/v1/publications`；`POST /api/mdm/v1/publications/{publicationNo}/retry`；`POST /openapi/mdm/v1/publication-receipts` |
| 读接口 | `GET /api/mdm/v1/publication-subscriptions`；`GET /api/mdm/v1/publications` |
| 聚合 | `PublicationSubscriptionAggregate`、`PublicationAggregate` |
| 事件 | `PublicationSubscriptionCreated`、`MasterDataPublished`、`MasterDataPublishConfirmed`、`MasterDataRepublished` |
| 持久化 | `mdm_publication_subscription`、`mdm_publication_log`、`mdm_event_inbox`，复用 `mdm_outbox_event`、`mdm_operation_log` |

## 3. 验收标准

1. 同一 `typeCode + targetSystem + eventTopic` 不能重复启用订阅。
2. 发布必须绑定已存在版本，且按目标系统生成发布日志。
3. 回执入口必须先写 Inbox，重复事件不能重复推进发布状态。
4. 失败发布可重试，成功发布不可重试。
5. 订阅、发布、回执、重试和 Outbox/Inbox 有测试覆盖。

## 4. 风险与后续

首轮不接真实 MQ、订阅过滤表达式执行和下游补偿拉取；后续与集成中心、TMS/WMS/OMS 等系统联调时增强事件投递和死信重放。
