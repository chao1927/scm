# 配置、灰度与回滚手册

## 上线前

- 固定应用镜像/JAR 校验和、Nacos namespace/DataId 版本和 Flyway 当前版本。
- 运行全量测试、健康检查、API 基线、备份校验和 Outbox/Inbox 盘点。
- 数据库迁移优先采用兼容的 expand/contract；DDL 执行后只能前滚或补偿，禁止盲目降版本。

## 灰度

先发布单实例，观察 5xx、p99、数据库池、Outbox 最老时长和 RocketMQ 积压至少一个业务观察窗，再逐批扩展。任何 page 级告警立即停止扩批。

## 回滚

回滚到已记录校验和的上一应用版本和上一份 Nacos 配置；保持数据库在向前兼容版本。回滚后重新执行 readiness、API 只读/幂等基线和事件盘点。
