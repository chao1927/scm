# IAM-REQ-001 权限系统核心闭环

## 1. 背景与目标

当前供应商、采购、WMS、库存服务多以请求头或本地默认权限模拟授权。IAM 需要提供登录会话、用户角色授权、权限点、数据范围、审批、审计和安全策略的统一事实源。

## 2. 范围

| 类型 | 内容 |
| --- | --- |
| 包含 | 登录/刷新/登出/me、用户、角色、角色授权、权限点、数据范围、审批、操作日志、安全策略 |
| 不包含 | JWT 真签名、Redis 缓存、SSO/MFA、真实风控、审批流引擎 |

## 3. 接口覆盖

| 计划 | 代表接口 |
| --- | --- |
| IAM-API-001 | `/auth/login`、`/auth/refresh`、`/auth/logout`、`/me` |
| IAM-API-002 | `/users`、`/roles`、`/roles/bind-user`、`/roles/grant-permission` |
| IAM-API-003 | `/permissions` |
| IAM-API-004 | `/data-scopes`、`/openapi/iam/v1/data-scope` |
| IAM-API-005 | `/approval-instances`、`/operation-logs`、`/security-policies` |
