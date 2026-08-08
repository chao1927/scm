# MySQL 备份与恢复手册

1. 用 `backup-mysql.sh --output-dir <新目录> --execute` 生成九库一致性逻辑备份及 SHA-256 清单。
2. 将产物复制到不可变存储；保留周期和 RPO/RTO 由生产审批填写，仓库不提供虚假固定值。
3. 每次备份必须执行 `verify-backup.sh`，并定期在隔离实例做恢复演练。
4. 恢复默认只预览。实际执行必须设置 `SCM_RESTORE_CONFIRM=<目标主机>`；生产还需 `SCM_ALLOW_PRODUCTION_RESTORE=YES`。
5. 恢复后核对完整 schema 校验和、九库关键表行数、Outbox/Inbox、只读查询，再逐个放开写流量。

失败回退：停止目标实例写入，保留失败恢复现场；重新创建隔离目标实例并从已校验备份恢复，不在半恢复数据库上继续写入。
