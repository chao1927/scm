# 九子系统多 Agent 开发任务状态

状态：`[ ] 待领取`、`[-] 进行中`、`[x] 已完成`、`[!] 阻塞`。

任务验收和文件范围见[供应链系统开发总纲](../docs/09-开发计划/00-供应链系统开发总纲.md#73-任务规格)。

## P0 并行基础

| 状态 | 编号 | Agent | 依赖 | 任务 |
| --- | --- | --- | --- | --- |
| [x] | PAR-NEXT-001 | frontend-foundation | 无 | 前端九子系统配置拆分 |
| [x] | PAR-NEXT-002 | plan-governance | 无 | 计划与契约一致性校验 |

## 批次 1：供给、仓储与库存

| 状态 | 编号 | Agent | 依赖 | 任务 |
| --- | --- | --- | --- | --- |
| [x] | SUP-NEXT-001A | frontend-foundation | PAR-NEXT-001 | 供应商导出文件与对象存储端口 |
| [x] | SUP-NEXT-001B | frontend-foundation | SUP-NEXT-001A | 供应商 Web/MySQL 契约测试 |
| [x] | SUP-MQ-001 | frontend-foundation | SUP-NEXT-001B | 供应商真实 RocketMQ 统一业务事件消费与 HTTP 主链路下线 |
| [x] | PUR-NEXT-001A | plan-governance | PAR-NEXT-001 | 采购工作台与经营读模型 |
| [x] | PUR-NEXT-001B | plan-governance | PUR-NEXT-001A | 采购目标上下文路由契约测试 |
| [x] | PUR-MQ-001 | plan-governance | PUR-NEXT-001B | 采购真实 RocketMQ 事件消费链路 |
| [x] | WMS-NEXT-001A | root-coordinator | PAR-NEXT-001 | WMS 入库作业读模型 |
| [x] | INV-NEXT-001A | inventory-domain | 无 | 库存冻结与调整独立聚合 |
| [x] | INV-NEXT-001B | inventory-domain | INV-NEXT-001A | 库存事件载荷版本与失败治理 |
| [x] | INV-NEXT-001C | inventory-operations | PAR-NEXT-001、INV-NEXT-001B | 库存运营读模型与导出 |

## 批次 2：履约、运输、结算与主数据

| 状态 | 编号 | Agent | 依赖 | 任务 |
| --- | --- | --- | --- | --- |
| [x] | OMS-NEXT-001A | oms-read-models | PAR-NEXT-001 | OMS 履约、售后和异常读模型 |
| [x] | OMS-NEXT-001B | oms-read-models | OMS-NEXT-001A | OMS 履约指标与异步导出 |
| [x] | TMS-NEXT-001A | root-coordinator | 无 | 承运商回调验签与运单状态推进 |
| [x] | TMS-NEXT-001B | frontend-foundation | PAR-NEXT-001、TMS-NEXT-001A | 面单附件与 TMS 标准页面 |
| [x] | BMS-NEXT-001A | root-coordinator | 无 | 财税支付防腐层 |
| [x] | BMS-NEXT-001B | bms-finance-reports | PAR-NEXT-001、BMS-NEXT-001A | 财务页面与异步报表 |
| [x] | MDM-NEXT-001A | root-coordinator | 无 | 主数据真实文件导入导出 |
| [x] | MDM-NEXT-001B | root-coordinator | PAR-NEXT-001、MDM-NEXT-001A | OpenAPI 数据边界与页面 |

## P0：身份安全收口

| 状态 | 编号 | Agent | 依赖 | 任务 |
| --- | --- | --- | --- | --- |
| [-] | IAM-NEXT-001A | iam_oauth_completion | 无 | Redis TokenCache 与密钥轮换 |
| [x] | IAM-NEXT-001B | iam_oauth_completion | IAM-NEXT-001A | OAuth/OIDC 授权服务 |
| [ ] | IAM-NEXT-001C | 未领取 | PAR-NEXT-001、IAM-NEXT-001A | MFA 挑战与治理页面 |

## P1：需求基线与 DDD 设计收口

| 状态 | 编号 | Agent | 依赖 | 任务 |
| --- | --- | --- | --- | --- |
| [x] | DOC-NEXT-001 | ddd_design_closure | 无 | 调拨、退款与多类型入库 DDD 设计收口 |
| [ ] | SUP-NEXT-002 | 未领取 | SUP-NEXT-001B、DOC-NEXT-001 | 供应商核心需求追踪与异常回归 |
| [x] | PUR-NEXT-002 | purchase_core_regression | PUR-NEXT-001B | 采购核心需求追踪与异常回归 |
| [-] | WMS-NEXT-002 | wms_multitype_closure | WMS-NEXT-001C、DOC-NEXT-001 | 多类型入库收口 |
| [x] | INV-NEXT-002 | inventory_transfer_hardening | INV-NEXT-001C、DOC-NEXT-001 | 调拨数量守恒回归 |
| [-] | BMS-NEXT-002 | supplier_core_regression | BMS-NEXT-001A、DOC-NEXT-001 | 退款聚合与回执回归 |

## P2：仓库生产准备与路线图

| 状态 | 编号 | Agent | 依赖 | 任务 |
| --- | --- | --- | --- | --- |
| [ ] | INT-NEXT-002 | 未领取 | P0/P1 共享契约冻结 | 九服务 Dubbo Provider 仓库闭环 |
| [ ] | OPS-NEXT-001A | 未领取 | P0/P1 核心链路冻结 | 生产运维仓库资产 |

`PLAN-ROADMAP-001` 为延后路线图，不可领取，不计入当前代码任务。

## P3：剩余前端与最终 QA

| 状态 | 编号 | Agent | 依赖 | 任务 |
| --- | --- | --- | --- | --- |
| [x] | WMS-NEXT-001B | root-coordinator | WMS-NEXT-001A | WMS 出库作业读模型 |
| [x] | WMS-NEXT-001C | root-coordinator | WMS-NEXT-001B | WMS 退货、盘点与异常读模型 |
| [ ] | FE-NEXT-001 | 未领取 | IAM-NEXT-001C、九子系统页面任务 | 统一交互与可访问性回归 |
| [ ] | QA-NEXT-001 | 未领取 | P0/P1/P2 可领取任务、FE-NEXT-001 | 九服务真实 API 与数据库回归 |

## 外部阻塞

| 状态 | 编号 | Agent | 阻塞原因 | 任务 |
| --- | --- | --- | --- | --- |
| [!] | INT-REQ-005B | 不可领取 | 缺 RocketMQ/Dubbo/ERP/税务/支付/承运商真实资料 | 真实中间件与外部系统联调 |
| [!] | OPS-REQ-001 | 不可领取 | 缺预演环境、监控告警、容量目标和演练窗口 | 生产监控、备份恢复与上线演练 |

## 历史任务归档

- 72 张正式需求状态见[供应链系统开发总纲](../docs/09-开发计划/00-供应链系统开发总纲.md#123-72-张需求精确覆盖索引)。
- `INT-REQ-001~005A`、`FE-REQ-002`、`RPT-REQ-001` 已迁移或撤销，不得重新领取。
- 2026-07-13、2026-07-16 任务表仅作历史记录。

下一项：按 P0→P2 执行。先完成 `IAM-NEXT-001A/B/C`，再推进剩余 P1 与 P2 任务。
