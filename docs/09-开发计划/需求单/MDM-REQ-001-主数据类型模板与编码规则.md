# MDM-REQ-001 主数据类型、字段模板与编码规则

## 目标

完成主数据系统第一条可验收闭环：主数据管理员可以维护主数据类型、字段模板和编码规则，并由系统按已启用编码规则生成唯一主数据编码。

## 范围

- 主数据类型：创建、启用、停用、列表查询。
- 字段模板：创建、发布、停用、列表查询。
- 编码规则：创建、启用、停用、生成编码、列表查询。
- 可靠事件：写入 MDM Outbox，供后续统一投递。
- 审计日志：记录关键命令、操作者、业务编号和幂等键。

## 业务规则

| 规则 | 说明 |
| --- | --- |
| 类型编码唯一 | 同一主数据上下文内 `typeCode` 不可重复 |
| 字段编码唯一 | 同一模板内 `fieldCode` 不可重复 |
| 字段模板发布前必须有字段 | 防止下游拿到不可建档的空模板 |
| 编码规则必须先启用再生成编码 | 防止草稿规则被业务引用 |
| 编码序号只能递增 | 号段不能回退或重复使用 |
| 已创建对象不物理删除 | 通过停用保留历史与引用追溯 |

## 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/mdm/v1/master-data-types` | 创建类型 |
| `POST` | `/api/mdm/v1/master-data-types/{typeCode}/enable` | 启用类型 |
| `POST` | `/api/mdm/v1/master-data-types/{typeCode}/disable` | 停用类型 |
| `GET` | `/api/mdm/v1/master-data-types` | 查询类型 |
| `POST` | `/api/mdm/v1/field-templates` | 创建字段模板 |
| `POST` | `/api/mdm/v1/field-templates/{templateCode}/publish` | 发布字段模板 |
| `POST` | `/api/mdm/v1/field-templates/{templateCode}/disable` | 停用字段模板 |
| `GET` | `/api/mdm/v1/field-templates` | 查询字段模板 |
| `POST` | `/api/mdm/v1/code-rules` | 创建编码规则 |
| `POST` | `/api/mdm/v1/code-rules/{ruleCode}/enable` | 启用编码规则 |
| `POST` | `/api/mdm/v1/code-rules/{ruleCode}/disable` | 停用编码规则 |
| `POST` | `/api/mdm/v1/code-rules/{ruleCode}/generate` | 生成主数据编码 |
| `GET` | `/api/mdm/v1/code-rules` | 查询编码规则 |

## 验收

- 聚合测试覆盖类型、字段模板、编码规则状态机和不变量。
- 应用服务测试覆盖类型启停、模板发布、编码生成、Outbox 和审计日志。
- 全量 `mvn test -q` 通过。

