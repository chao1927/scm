# SEC-REQ-001 统一后端认证授权公共契约

## 业务目标

为供应链各限界上下文提供统一的 JWT 认证事实、功能权限和数据范围判定，防止各服务自行信任请求头、权限语义不一致或默认放行。

## 信任边界与威胁

| 边界 | 主要风险 | 控制 |
| --- | --- | --- |
| HTTP 到业务服务 | 伪造用户、权限和数据范围 | JWT 签名校验后才建立 `ScmAccessContext` |
| JWT Claims 到授权判定 | 通配权限扩大、非法用户标识 | 仅支持精确权限、全局 `*` 和命名空间 `:*`；用户标识必须为正数 |
| 数据范围到业务对象 | 跨组织、仓库、货主越权 | `requireScope` 仅接受令牌声明范围，不直接信任客户端范围头 |
| 密钥配置 | 默认弱密钥或空密钥上线 | 启用配置时 HMAC 密钥至少 32 字节，否则启动失败 |

## 实现与验收

- `ScmAccessContext`：统一操作人、应用、权限码和数据范围，并提供拒绝优先的授权方法。
- `ScmJwtAuthenticationConverter`：把已经验签的 JWT 转换为 Spring Security 权限和访问上下文。
- `ScmSecurityProperties`：安全默认启用，拒绝空密钥和短密钥。
- `ScmSecurityConfiguration`：除健康检查外默认要求认证，统一 OAuth2 Resource Server 转换器。
- 测试覆盖精确权限、命名空间权限、越权数据范围、JWT Claims 转换和弱密钥拒绝。

## 完成证据

- `mvn -q -pl scm-common test` 通过。
- 后端全量 `mvn -q test` 通过，共 228 项测试、0 失败。
- 后续服务接入由 `SEC-REQ-002`、`SEC-REQ-003` 完成。
