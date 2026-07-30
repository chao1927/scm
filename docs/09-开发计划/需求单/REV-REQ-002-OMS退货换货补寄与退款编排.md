# REV-REQ-002 OMS 退货、换货、补寄与退款编排

## 1. 目标

在保留原仅退款接口兼容性的前提下，新增完整逆向售后聚合，覆盖 `REFUND_ONLY`、`RETURN_REFUND`、`EXCHANGE`、`RESHIP` 四种类型。

## 2. 交付

| 类别 | 内容 |
| --- | --- |
| 聚合 | `ReverseAfterSaleAggregate`：申请、审核、收货、质检、退款、补寄和完成状态机 |
| 持久化 | `oms_reverse_after_sale`，RMA 唯一，订单+SKU、状态索引，乐观锁 |
| 接口 | `/api/oms/v1/reverse-after-sales` 创建、审核、退款、补寄、查询 |
| 内部事件 | `/internal/oms/v1/reverse-after-sale-events` 消费 WMS/BMS/OMS 事实，Inbox 幂等 |
| 命令 | 审核退货生成 TMS 退货运输和 WMS 退货入库命令；质检后生成 BMS 退款或 OMS 补寄命令 |

## 3. 验收规则

1. 申请数量不超过原订单行数量，退款金额不超过 `申请数量 × 原订单行单价`。
2. 退货退款/换货必须生成 RMA，未收到 WMS 质检事实不得退款或补寄。
3. 同一订单+SKU 仅允许一个未终结售后；重复外部事件不重复推进。
4. 仅退款审核后直接进入待退款；补寄审核后直接进入待补寄。
5. 聚合、命令、Outbox、Inbox 和状态更新在同一事务中完成。

## 4. 兼容与后续

原 `/api/oms/v1/after-sales` 保留，避免破坏存量仅退款调用；新业务使用 `/reverse-after-sales`。WMS 退货收货质检和中央库存五类处置由 `REV-REQ-003` 接续。
