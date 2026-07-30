# SEC-REQ-002 采购、WMS、中央库存接入统一鉴权

## 业务目标

将采购、仓内作业和中央库存的写操作收敛到同一信任边界，防止伪造操作人、跨采购组织/仓库/货主操作、横向调用其他作业权限和重复写请求。

## 范围与不变量

| 范围 | 不变量 |
| --- | --- |
| 身份 | 操作人只能来自已验签 JWT，不信任 `X-Operator-Id` |
| 功能权限 | 请求必须具备所属系统命名空间和具体作业权限 |
| 数据权限 | 采购组织、仓库、货主均必须落在 JWT `data_scopes` 内 |
| 幂等 | 受保护的 POST/PUT/PATCH/DELETE 请求必须提供 `X-Idempotency-Key`，长度不超过 128 |
| 密钥 | IAM 和资源服务使用同一 `IAM_JWT_SECRET`，至少 32 字节 |

## 实现切片

1. 采购 `CommandContextFactory` 只从 `ScmAccessContext` 建立命令上下文，范围头仅作为被校验的选择值。
2. WMS 和库存写接口按作业域映射权限，并在进入应用服务前校验仓库/货主范围。
3. IAM 登录和刷新签发的访问令牌携带 `permissions` 与 `data_scopes`；刷新令牌不携带业务授权。
4. 三个服务的 Web 入口统一拒绝缺失或过长幂等键。

## 验收结果

- 匿名请求返回 401，错误权限命名空间返回 403。
- 伪造操作人头无效，越权采购组织、仓库或货主被拒绝。
- 写请求缺少幂等键返回 400。
- `mvn -q -pl iam-service,purchase-service,wms-service,inventory-service -am test` 通过。
