# BMS-REQ-003 退款结算、报表与通用事件入口

## 目标

补齐 BMS 对 OMS 退款、结算报表和内部事件消费的首轮能力，形成计费结算系统可被其他系统订阅和回放的边界。

## 范围

| 类型 | 内容 |
| --- | --- |
| 接口 | `POST /api/bms/v1/refund-settlements`、`POST /refund-settlements/{refundNo}/finish|fail`、`GET /reports/settlement-summary`、`POST /internal/bms/v1/events` |
| 领域 | `RefundSettlementAggregate`、结算汇总读模型、BMS Inbox |
| 不变量 | 退款金额不得超过可退账单金额；退款结果只能从请求中进入成功或失败；内部事件以来源系统+事件 ID 幂等 |
| 事件 | `RefundSettlementRequested/Finished/Failed`、`BmsExternalEventConsumed` |
| 持久化 | `bms_refund_settlement`、`bms_event_consume_log`、`bms_domain_event` |

## 验收

- 可基于账单创建退款结算并接收成功/失败结果。
- 结算汇总报表能读取账单金额与数量。
- 内部事件入口具备 Inbox 幂等。
- BMS 模块级测试和全量回归通过。
