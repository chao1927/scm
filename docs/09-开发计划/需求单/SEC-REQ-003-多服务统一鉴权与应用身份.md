# SEC-REQ-003 OMS、TMS、BMS、MDM、集成中心统一鉴权

> 历史说明：`ARCH-REQ-001` 已删除独立集成中心；OMS、TMS、BMS、MDM 的鉴权结论继续有效，原集成中心身份校验已随集成能力迁回各业务子系统。

## 目标

将销售履约、运输、计费、主数据和集成中心接入统一 JWT 信任边界，并防止 OpenAPI/内部事件调用方冒充其他来源系统。

## 验收规则

| 项目 | 规则 |
| --- | --- |
| 认证 | 除健康检查外默认需要已验签 JWT |
| 命名空间 | OMS/TMS/BMS/MDM/集成中心分别只接受本系统权限命名空间 |
| 功能权限 | 控制器按订单、履约、售后、运单、轨迹、计费、主数据记录、发布和集成消息等职责映射 |
| 应用身份 | 集成事件/命令、TMS 建单/承运商回调、BMS 费用/事件、MDM 内部事件、OMS 外部事件均校验 JWT `app` 与来源一致 |
| 密钥 | 所有资源服务使用 `IAM_JWT_SECRET`，缺失或少于 32 字节时拒绝启动 |

## 完成证据

- `ScmAccessContext.requireApplication` 覆盖应用冒充拒绝。
- 五服务安全配置、权限标注和应用身份校验均已编译运行。
- `mvn -q -pl oms-service,tms-service,bms-service,mdm-service,integration-service -am test` 通过。

## 范围说明

本需求完成资源服务鉴权和应用身份验证，不声称已实现完整 OAuth/OIDC 客户端凭证流；该能力与密钥轮换归入后续 IAM 生产化。
