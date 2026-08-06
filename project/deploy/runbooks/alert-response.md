# SCM 告警处置手册

所有处置先记录告警时间、环境、服务名和 traceId；禁止直接删除 Outbox/Inbox、跳过 Flyway 或在未知影响下重启全部服务。

## ScmServiceUnavailable

1. 检查 liveness/readiness 和最近一次配置、数据库迁移、密钥变更。
2. 仅重启单个异常实例；若新版本导致，按 `release-and-rollback.md` 回滚应用，不回滚已成功执行的数据库 DDL。
3. 无法在 10 分钟内恢复时由平台值班升级到对应子系统负责人。

## ScmHttpErrorRateHigh

按服务、路由模板和状态类定位错误，再用 traceId 查结构化日志。区分校验失败、数据库冲突和依赖超时；不得用放宽鉴权或关闭幂等来止血。

## ScmHttpP99LatencyHigh

检查慢路由、JDBC 等待、Dubbo/HTTP 依赖跨度和 RocketMQ 回压。先限制非核心导出/批任务，再按证据调整容量或回滚。

## ScmOutboxOldestPending

运行 `bin/event-ledger-audit.sh`，确认最早事件、重试次数和最后错误；检查 RocketMQ Proxy、Topic 与消费者。恢复依赖后让调度器重试，禁止手工改成“已发布”。

## ScmRocketMqConsumerLagHigh

确认积压 Topic、Consumer Group、最老消息时间和 DLQ。先恢复消费者或扩容；只有在确认幂等键和业务补偿后才能重放。

## ScmDatabasePoolSaturated

检查活动连接、等待线程、慢 SQL 和事务时间。停止非核心批任务；禁止直接扩大连接池导致 MySQL 过载。

## ScmDubboNoProvider

检查 Nacos 中 group=`scm-collaboration`、version=`1.0.0` 的 Provider、网络和健康状态。Consumer 必须保持失败关闭；恢复 Provider 后运行 `project/qa/dubbo-local-smoke.sh` 验证发现与幂等。
