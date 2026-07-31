# IAM-REQ-005 MFA 挑战、恢复与审计基线

> 状态：待开发。实施入口为 `IAM-NEXT-001C`。

## 1. 业务目标

为登录、敏感操作和账号恢复建立可追踪、不可重放的二次认证闭环，而不是只在登录请求中增加一个 `mfaCode` 字段。

## 2. 领域模型

| 项目 | 口径 |
| --- | --- |
| 聚合 | MFA 配置、MFA Challenge、恢复码集合 |
| 状态 | `CREATED -> VERIFIED/EXPIRED/LOCKED/CANCELLED` |
| 命令 | 注册 TOTP、验证注册、创建挑战、验证挑战、使用恢复码、重置 MFA |
| 事件 | `MfaEnrolled/ChallengeCreated/Verified/Failed/Locked/Reset` |
| 数据主权 | IAM 拥有 MFA 密钥密文、挑战、失败计数、恢复码摘要和审计事实 |

## 3. 核心不变量

1. Challenge 必须绑定用户、会话、用途、设备摘要和有效期；验证成功后不可再次使用。
2. TOTP 种子必须加密保存，不进入日志、Token、页面响应或普通审计明细。
3. 恢复码只保存摘要，每个恢复码只能使用一次。
4. 失败次数达到阈值后锁定 Challenge；高风险失败可联动用户或会话风控。
5. 重置 MFA 必须经过当前 MFA、恢复流程或审批，并撤销现有会话。

## 4. 接口范围

- `POST /mfa/totp/enroll|confirm|disable`
- `POST /mfa/challenges`
- `POST /mfa/challenges/{challengeId}/verify`
- `POST /mfa/recovery-codes/regenerate`
- `POST /admin/users/{userId}/mfa/reset`

登录接口在账号密码通过后返回 `MFA_REQUIRED + challengeId`，不得直接接受没有挑战上下文的裸验证码。

## 5. 验收

- 过期、重复、跨会话、跨用途和错误 Challenge 均被拒绝。
- TOTP 时间窗口、失败锁定、恢复码单次消费和并发验证有自动化测试。
- MFA 注册、验证、失败、锁定、重置均写安全审计，但不记录秘密材料。
- 管理员重置 MFA 需要高风险权限、原因和审批/二次验证。

## 继续上下文

当前结论：MFA Challenge 是安全聚合，不是登录 DTO 的附属字段。

关键假设：第一阶段使用 TOTP + 恢复码。

待决问题：短信/邮件 OTP 不进入第一阶段。

下一步：与 OAuth 登录事务、会话签发和安全策略联调。
