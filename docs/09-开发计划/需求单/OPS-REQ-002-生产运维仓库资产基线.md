# OPS-REQ-002 生产运维仓库资产基线

> 状态：已完成（仓库阶段）。真实监控平台、告警通道和生产演练环境验收仍归 `OPS-REQ-001`。

## 1. 目标

先完成不依赖生产环境的运维资产，使监控、备份、恢复和上线演练具备可执行输入，而不是等环境齐备后再从零编写。

## 2. 仓库交付范围

- 九服务健康检查、readiness/liveness、依赖降级和启动检查清单。
- MySQL、Redis、RocketMQ、Nacos/Dubbo 的指标目录、告警规则模板和仪表盘定义。
- MySQL 备份、恢复校验、Outbox/Inbox 对账恢复脚本或操作清单。
- 配置变更、灰度、回滚、数据库迁移前滚/补偿和上线检查表。
- 故障场景脚本：消息积压、外部超时、Redis 不可用、Provider 下线、数据库只读/连接耗尽。

## 3. 验收

- 所有脚本默认只读或显式要求环境参数；破坏性步骤有人工确认和回退说明。
- 每个告警说明指标、阈值占位、持续时间、影响、处置步骤和负责人角色。
- 备份产物有校验和、保留策略占位和恢复验证步骤。
- 资产可在本地/测试环境做静态或最小冒烟；不得把模板存在视为生产演练完成。

## 4. 交付证据（2026-08-06）

- 九服务统一引入本地已有的 Spring Boot 4 Actuator，并开放 liveness/readiness；端口均支持环境变量覆盖。
- `project/deploy/alerts/scm-alert-rules.yml` 提供服务不可用、5xx、p99、Outbox、RocketMQ、数据库池和 Dubbo Provider 七类症状告警，全部使用 `page/ticket` 两级并链接处置手册。
- `project/deploy/bin/backup-mysql.sh`、`verify-backup.sh`、`restore-mysql.sh` 完成九库备份、SHA-256/大小校验和双重确认恢复；默认只预览，密码只从环境变量读取。
- `event-ledger-audit.sh` 提供九库 Outbox 只读盘点；发布回滚、备份恢复和六类故障演练手册已补齐。
- `project/deploy/tests/test-deploy-assets.sh` 验证 Shell 语法、健康检查成功/失败、告警等级与 runbook 完整性、备份默认无副作用，测试通过。
- JDK 17、Maven 离线模式完成九服务全量 `package`。本地只有旧版 Prometheus Registry，不与 Boot 4 强行混用；Registry 由部署平台按兼容版本注入。

## 继续上下文

当前结论：OPS 仓库资产已完成，生产实跑继续保持外部阻塞。

关键假设：具体容量阈值、RTO/RPO 和告警路由由部署环境补充。

待决问题：监控平台最终选型尚未固定。

下一步：在 `QA-NEXT-001` 执行测试环境最小运行验证；生产监控接入和演练归 `OPS-REQ-001`。
