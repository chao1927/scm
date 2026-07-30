# PUR-REQ-010 采购持久化幂等

## 业务目标

将采购写命令的幂等事实从单 JVM 内存迁移到 MySQL，防止重启、多实例和并发请求导致重复单据、重复事件或重复外部命令。

## 不变量

| 场景 | 规则 |
| --- | --- |
| 首次请求 | `businessType + idempotencyKey` 原子占用 |
| 重复完成 | 返回原结果并标记 `duplicated=true`，不再执行业务动作 |
| 内容冲突 | 同一幂等键的 SHA-256 请求摘要不一致时拒绝 |
| 并发占用 | 已在处理的请求拒绝第二个执行者 |
| 失败恢复 | 业务失败与幂等事实同事务回滚；非事务失败记录可用同摘要抢占重试 |
| 原子性 | 业务聚合、Outbox、审计和幂等结果同事务提交 |

## 实现

- `V11__purchase_persistent_idempotency.sql` 新增幂等表、唯一键和状态索引。
- `PurchaseIdempotencyMapper` 实现占用、查询、完成、失败和重试 CAS。
- `MyBatisIdempotencyAdapter` 替代 Spring 中的内存适配器。
- `PurchaseIdempotencyKeyFilter` 缓存原始请求体，`CommandContextFactory` 计算方法+路径+请求体 SHA-256 摘要。
- 内部事件使用事件请求号/幂等键生成稳定摘要。

## 验收

- `MyBatisIdempotencyAdapterTest` 覆盖重复完成、摘要冲突、处理中冲突和失败重试。
- `mvn -q -pl purchase-service -am test` 通过。
- 后端全量 `mvn -q test` 通过。
