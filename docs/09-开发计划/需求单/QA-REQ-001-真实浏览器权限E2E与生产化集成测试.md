# QA-REQ-001 真实浏览器权限 E2E 与生产化集成测试

> 状态：已完成（仓库与本地真实环境阶段）。外部 ERP、税务、支付、承运商沙箱和生产环境演练不属于本需求，分别由 `INT-REQ-005B`、`OPS-REQ-001` 跟踪。

## 1. 背景

前端业务页面、基础菜单权限规则和路由拆包已完成。需要补真实浏览器 E2E 权限验证，并明确生产化集成测试所需外部条件。

## 2. 已完成范围

| 范围 | 结果 |
| --- | --- |
| Playwright 安装 | 已安装 `@playwright/test` 和 Chromium 运行时 |
| E2E 配置 | `playwright.config.js` 使用独立端口 `5174`，兼容无 GPU Chromium |
| 菜单权限 E2E | 已覆盖权限快照、未授权菜单、交互与响应式场景 |
| 前端验证 | Vitest `43/43`、Vite `4953` 模块构建、Chromium E2E `11/11` 通过 |
| 后端验证 | JDK 17 下后端 11 模块全量测试通过；Supplier 真实 MySQL `3/3`、Inventory 真实 MySQL `1/1` |
| 九服务真实联调 | `project/qa/nine-service-local-smoke.sh` 显式导入九库完整 schema 后一键启动九服务，连接真实 Nacos、MySQL、Redis、RocketMQ；九服务均限制为 256 MiB JVM |
| API 与安全基线 | 九服务端口、匿名 `401`、越权 `403`、授权 `200/JSON` 查询全部通过 |

## 3. 外部验收边界

| 测试项 | 所需条件 | 当前状态 |
| --- | --- | --- |
| 前后端真实 API 与权限快照 | 本地九服务、数据库、测试账号、Token 与权限点 | 已完成 |
| RocketMQ/Dubbo 本地联调 | 本地 nameserver、topic、group、Nacos/Dubbo 注册中心 | 已完成 |
| ERP/税务/支付/承运商沙箱 | 沙箱地址、凭证、验签资料、样例载荷、错误码 | `INT-REQ-005B` 阻塞 |
| 生产监控、容量和演练 | 预演环境、告警路由、RTO/RPO、故障注入窗口 | `OPS-REQ-001` 阻塞 |

## 4. 结论

`QA-REQ-001` 的仓库内和本地真实环境验收已经闭环。外部沙箱与生产演练继续由各自需求单管理，不再阻塞 CP-3D。
