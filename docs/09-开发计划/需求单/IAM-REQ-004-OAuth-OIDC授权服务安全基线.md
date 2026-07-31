# IAM-REQ-004 OAuth/OIDC 授权服务安全基线

> 状态：部分完成（OAuth 第一阶段已于 2026-07-30 完成）。授权码 + PKCE、Client Credentials、刷新轮换/复用撤销、主动撤销、UserInfo、审计和 Outbox 已交付；OIDC Discovery、JWKS、ID Token 与 introspect 尚未开放，且不得伪装可用。

## 1. 业务目标

为内部管理端、供应商门户和受信任服务提供标准授权能力，避免把普通登录接口、SSO 配置和 OAuth 授权服务混为一体。

## 2. 领域边界

| 项目 | 口径 |
| --- | --- |
| 限界上下文 | 身份与访问 |
| 核心聚合 | OAuth 客户端、授权事务、授权码、Token 授权记录 |
| 数据主权 | IAM 拥有客户端、授权码、scope、audience、Token 撤销和授权审计事实 |
| 不负责范围 | 业务系统自己的业务权限、订单或库存状态 |

## 3. 必须支持的授权方式

- 授权码 + PKCE：面向浏览器和公开客户端。
- Client Credentials：仅面向明确登记的机器身份，不产生用户身份。
- Refresh Token：必须轮换，发现复用时撤销所属授权链。
- 第一阶段不支持密码模式和隐式模式。

## 4. 核心不变量

1. `redirect_uri` 必须与客户端登记值精确匹配，不允许前缀、通配或二次跳转。
2. 授权码短时有效、只能消费一次，并绑定 client、redirect URI、PKCE challenge、用户和授权事务。
3. scope 只能取客户端允许范围、用户授权范围和请求范围的交集。
4. access token 必须固化 issuer、subject、audience、scope、client、权限版本和密钥 `kid`。
5. Client Credentials 不得继承用户角色或用户数据范围。
6. 客户端密钥只保存不可逆摘要；重置后旧密钥立即失效并留下审计。

## 5. 接口与事件

| API/事件 | 说明 | 幂等/安全要求 |
| --- | --- | --- |
| `GET/POST /oauth2/authorize` | 创建和确认授权事务 | state、nonce、PKCE、登录会话绑定 |
| `POST /oauth2/token` | 换取或刷新 Token | 授权码一次性消费；刷新令牌轮换 |
| `POST /oauth2/revoke` | 撤销 Token 或授权链 | 重复撤销无副作用 |
| `POST /oauth2/introspect` | 受信任资源服务查询 Token | 强客户端认证和最小返回字段 |
| `GET /.well-known/openid-configuration` | OIDC 发现文档 | 仅在本需求范围实现 OIDC 时开放 |
| `GET /.well-known/jwks.json` | 公钥集合 | 只暴露可验证密钥，不暴露私钥材料 |

## 6. 验收

- redirect URI 绕过、授权码重放、PKCE 缺失/不匹配、scope 越权全部失败关闭。
- 授权码、刷新令牌复用和撤销均有并发测试与审计证据。
- Client Credentials Token 不包含用户角色和用户数据范围。
- 明确记录 OIDC Discovery、JWKS、ID Token、nonce 和 UserInfo 的实现范围；未实现的端点不得伪装可用。

## 继续上下文

当前结论：OAuth/OIDC 必须作为独立授权模型实施，不能附着在普通登录接口上。

关键假设：第一阶段只支持授权码 + PKCE、Client Credentials 和 Refresh Token。

待决问题：UserInfo 是否在第一阶段开放。

下一步：先冻结接口、聚合和威胁测试，再编码。
