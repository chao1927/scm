# 供应链系统仓库运维资产

本目录承载 `OPS-NEXT-001A` 可在仓库内验证的健康检查、告警、备份恢复、发布回滚和故障演练资产。真实监控平台、容量阈值、RTO/RPO 和生产演练仍属于外部阻塞的 `OPS-REQ-001`。

## 安全边界

- 检查脚本默认只读，不修改服务、数据库或消息状态。
- 备份、恢复和故障注入必须显式传入 `--execute`；恢复还必须确认目标环境。
- 密码、Token 和密钥只从环境变量或秘密文件读取，不接受命令行明文。
- 本目录不替代根目录 `deploy/` 的实验环境安装脚本。

## 资产索引

| 资产 | 用途 | 默认行为 |
| --- | --- | --- |
| `bin/health-check.sh` | 九服务存活/就绪检查 | 只读 |
| `bin/backup-mysql.sh` | 九业务库一致性逻辑备份 | 预览，需 `--execute` |
| `bin/restore-mysql.sh` | 恢复到明确指定的目标库实例 | 预览，需双重确认 |
| `bin/verify-backup.sh` | 校验备份清单和 SHA-256 | 只读 |
| `bin/event-ledger-audit.sh` | Outbox/Inbox 失败和积压盘点 | 只读 |
| `alerts/scm-alert-rules.yml` | Prometheus 告警规则模板 | 静态模板 |
| `runbooks/` | 告警处置、发布回滚、恢复步骤 | 操作说明 |
| `drills/` | 故障场景和退出条件 | 默认不执行注入 |
| `tests/test-deploy-assets.sh` | Shell 资产自测 | 使用假实现，无外部副作用 |

## 快速验证

```bash
project/deploy/tests/test-deploy-assets.sh
project/deploy/bin/health-check.sh --dry-run
project/deploy/bin/backup-mysql.sh --output-dir /tmp/scm-backup
```

完整部署与生产缺口见 [`docs/10-供应链系统完整部署手册.md`](../../docs/10-供应链系统完整部署手册.md)。
