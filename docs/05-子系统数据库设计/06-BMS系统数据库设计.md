# BMS系统数据库设计

> 本文根据 `docs/03-核心业务模型` 的聚合、不变量、命令事件，以及 `docs/04-子系统功能设计` 的页面、状态、枚举和操作场景整理。本文是数据库表字段设计入口；领域模型和功能设计文档不再保留字段迁移链接。

## 1. 设计口径

| 项目 | 口径 |
| --- | --- |
| 所属系统 | BMS系统 |
| 表名前缀 | `bms_` |
| 主键策略 | 业务表使用 bigint 主键，建议雪花 ID；业务单号另设唯一索引 |
| 时间类型 | 业务时间使用 `datetime`，日期使用 `date`，金额使用 `decimal(18,2)` 或更高精度 |
| 数量类型 | 库存、采购、履约数量统一使用 `decimal(18,4)` |
| 状态字段 | 状态机字段只能由聚合命令推进，页面只配置展示名、排序和颜色 |
| 枚举字段 | 业务可配置枚举进入枚举配置；核心状态枚举可以配置标签但不能随意增删 |

## 2. 通用字段

| 字段 | 类型 | 是否必填 | 约束 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | bigint | 是 | 主键，自增或雪花ID | 技术主键 |
| `tenant_id` | bigint | 否 | 多租户时启用 | 租户ID |
| `org_id` | bigint | 否 | 按组织隔离时启用 | 组织ID |
| `created_by` | bigint | 是 | 写入时填充 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 逻辑删除标记 |

## 3. 表清单

| 表名 | 业务对象 | 来源聚合 | 说明 |
| --- | --- | --- | --- |
| `bms_billing` | 计费对象 | `02-计费对象聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `bms_billing_rule` | 计费规则 | `03-计费规则聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `bms_fee` | 费用来源事件 | `04-费用来源事件聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `bms_fee_line` | 费用明细 | `05-费用明细聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `bms_adjustment` | 调整单 | `06-费用调整单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `bms_reconcile` | 对账单 | `07-对账单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `bms_bill` | 账单 | `08-账单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `bms_invoice` | 发票交接 | `09-发票交接聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `bms_finance` | 财务交接 | `10-财务交接聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |

## 4. 表字段设计

### 4.1 `bms_billing`

业务对象：计费对象

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `billing_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 计费对象主键 |
| `object_code` | varchar(64) | 是 | 唯一 | 标识/引用 | 对象编码 |
| `object_name` | varchar(256) | 是 |   | 业务属性 | 对象名称 |
| `object_type` | varchar(32) | 是 | `BILLING_OBJECT_TYPE` | 状态/分类 | 客户、货主、供应商、物流商 |
| `settlement_direction` | varchar(32) | 是 | `SETTLEMENT_DIRECTION` | 业务属性 | 应收、应付 |
| `settlement_cycle` | varchar(32) | 是 | `SETTLEMENT_CYCLE` | 业务属性 | 日结、周结、月结、单结 |
| `tax_rate` | decimal(8,4) | 否 | >= 0 | 数量/金额 | 默认税率 |
| `currency` | varchar(16) | 是 | `CURRENCY` | 业务属性 | 币种 |
| `status` | varchar(32) | 是 | `COMMON_STATUS` | 状态/分类 | 启用、停用 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_bms_billing_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_bms_billing_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_bms_billing_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.2 `bms_billing_rule`

业务对象：计费规则

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `billing_rule_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 计费规则主键 |
| `rule_code` | varchar(64) | 是 | 唯一 | 标识/引用 | 规则编码 |
| `rule_name` | varchar(128) | 是 |   | 业务属性 | 规则名称 |
| `fee_type` | varchar(32) | 是 | `FEE_TYPE` | 状态/分类 | 入库费、出库费、仓储费、物流费、耗材费、增值服务 |
| `pricing_method` | varchar(32) | 是 | `PRICING_METHOD` | 业务属性 | 按件、按重量、按体积、按天、阶梯、固定价 |
| `price_config` | text | 是 | JSON | 数量/金额 | 价格配置 |
| `tax_rate` | decimal(8,4) | 否 | >= 0 | 数量/金额 | 税率 |
| `effective_from` | date | 是 |   | 业务属性 | 生效日期 |
| `effective_to` | date | 否 |   | 业务属性 | 失效日期 |
| `rule_version` | int | 是 | >= 1 | 业务属性 | 规则版本 |
| `status` | varchar(32) | 是 | `RULE_STATUS` | 状态/分类 | 草稿、已发布、已停用 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_bms_billing_rule_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_bms_billing_rule_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_bms_billing_rule_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.3 `bms_fee`

业务对象：费用来源事件

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `fee_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 费用来源事件主键 |
| `event_name` | varchar(128) | 是 |   | 业务属性 | 事件名称 |
| `source_system` | varchar(32) | 是 | `SOURCE_SYSTEM` | 业务属性 | WMS、OMS、库存、TMS 等 |
| `source_order_no` | varchar(64) | 否 |   | 标识/引用 | 来源单号 |
| `biz_type` | varchar(32) | 是 | `BILLING_BIZ_TYPE` | 状态/分类 | 入库、出库、存储、运输、退货、售后 |
| `process_status` | varchar(32) | 是 | `EVENT_PROCESS_STATUS` | 状态/分类 | 待处理、成功、失败、已忽略 |
| `fail_reason` | varchar(1024) | 否 |   | 业务属性 | 失败原因 |
| `received_at` | datetime | 是 |   | 业务属性 | 接收时间 |
| `processed_at` | datetime | 否 |   | 业务属性 | 处理时间 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_bms_fee_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_bms_fee_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_bms_fee_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.4 `bms_fee_line`

业务对象：费用明细

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `fee_line_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 费用明细主键 |
| `billing_item_no` | varchar(64) | 是 | 唯一 | 标识/引用 | 费用明细号 |
| `settlement_direction` | varchar(32) | 是 | `SETTLEMENT_DIRECTION` | 业务属性 | 应收、应付 |
| `fee_type` | varchar(32) | 是 | `FEE_TYPE` | 状态/分类 | 费用类型 |
| `source_order_no` | varchar(64) | 否 |   | 标识/引用 | 来源单号 |
| `rule_version` | int | 否 |   | 业务属性 | 规则版本 |
| `quantity` | decimal(18,4) | 是 | >= 0 | 业务属性 | 计费数量 |
| `unit_price` | decimal(18,6) | 是 | >= 0 | 数量/金额 | 单价 |
| `amount` | decimal(18,2) | 是 | 可正可负 | 数量/金额 | 未税金额 |
| `tax_rate` | decimal(8,4) | 否 | >= 0 | 数量/金额 | 税率 |
| `tax_amount` | decimal(18,2) | 否 |   | 数量/金额 | 税额 |
| `tax_included_amount` | decimal(18,2) | 是 |   | 数量/金额 | 含税金额 |
| `billing_period` | varchar(32) | 是 |   | 业务属性 | 账期，如 `2026-06` |
| `billing_status` | varchar(32) | 是 | `BILLING_ITEM_STATUS` | 状态/分类 | 待计算、计算异常、待对账、对账差异、已确认、已入账、已作废 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_bms_fee_line_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_bms_fee_line_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_bms_fee_line_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.5 `bms_adjustment`

业务对象：调整单

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `adjustment_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 调整单主键 |
| `adjustment_no` | varchar(64) | 是 | 唯一 | 标识/引用 | 调整单号 |
| `adjustment_type` | varchar(32) | 是 | `BILLING_ADJUSTMENT_TYPE` | 状态/分类 | 减免、补收、冲减、修正 |
| `adjustment_amount` | decimal(18,2) | 是 | 可正可负 | 数量/金额 | 调整金额 |
| `adjustment_reason` | varchar(512) | 是 |   | 业务属性 | 调整原因 |
| `approval_status` | varchar(32) | 是 | `APPROVAL_STATUS` | 状态/分类 | 草稿、待审批、已批准、已驳回 |
| `adjustment_status` | varchar(32) | 是 | `ADJUSTMENT_STATUS` | 状态/分类 | 草稿、待审批、已执行、已驳回、已取消 |
| `executed_at` | datetime | 否 |   | 业务属性 | 执行时间 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_bms_adjustment_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_bms_adjustment_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_bms_adjustment_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.6 `bms_reconcile`

业务对象：对账单

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `reconcile_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 对账单主键 |
| `reconciliation_no` | varchar(64) | 是 | 唯一 | 标识/引用 | 对账单号 |
| `settlement_direction` | varchar(32) | 是 | `SETTLEMENT_DIRECTION` | 业务属性 | 应收、应付 |
| `billing_period` | varchar(32) | 是 | 账期 | 业务属性 | 账期 |
| `total_amount` | decimal(18,2) | 是 |   | 数量/金额 | 未税金额 |
| `tax_amount` | decimal(18,2) | 否 |   | 数量/金额 | 税额 |
| `tax_included_amount` | decimal(18,2) | 是 |   | 数量/金额 | 含税金额 |
| `diff_amount` | decimal(18,2) | 是 | 默认 0 | 数量/金额 | 差异金额 |
| `recon_status` | varchar(32) | 是 | `RECON_STATUS` | 状态/分类 | 草稿、待确认、差异处理中、已确认、已关闭 |
| `confirmed_at` | datetime | 否 |   | 业务属性 | 确认时间 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_bms_reconcile_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_bms_reconcile_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_bms_reconcile_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.7 `bms_bill`

业务对象：账单

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `bill_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 账单主键 |
| `bill_no` | varchar(64) | 是 | 唯一 | 标识/引用 | 账单号 |
| `settlement_direction` | varchar(32) | 是 | `SETTLEMENT_DIRECTION` | 业务属性 | 应收、应付 |
| `billing_period` | varchar(32) | 是 |   | 业务属性 | 账期 |
| `total_amount` | decimal(18,2) | 是 |   | 数量/金额 | 未税金额 |
| `tax_amount` | decimal(18,2) | 否 |   | 数量/金额 | 税额 |
| `tax_included_amount` | decimal(18,2) | 是 |   | 数量/金额 | 含税金额 |
| `invoice_required` | boolean | 是 | true/false | 业务属性 | 是否需要开票 |
| `bill_status` | varchar(32) | 是 | `BILL_STATUS` | 状态/分类 | 草稿、待确认、差异处理中、已确认、待开票、已开票、待入账、已入账、已关闭 |
| `confirmed_at` | datetime | 否 |   | 业务属性 | 确认时间 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_bms_bill_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_bms_bill_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_bms_bill_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.8 `bms_invoice`

业务对象：发票交接

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `invoice_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 发票交接主键 |
| `invoice_type` | varchar(32) | 是 | `INVOICE_TYPE` | 状态/分类 | 普票、专票、电子票 |
| `invoice_no` | varchar(128) | 否 |   | 标识/引用 | 发票号 |
| `invoice_amount` | decimal(18,2) | 是 |   | 数量/金额 | 开票金额 |
| `invoice_status` | varchar(32) | 是 | `INVOICE_STATUS` | 状态/分类 | 待申请、已申请、已开票、已作废 |
| `invoice_file_url` | varchar(512) | 否 |   | 业务属性 | 发票附件 |
| `requested_at` | datetime | 否 |   | 业务属性 | 申请时间 |
| `issued_at` | datetime | 否 |   | 业务属性 | 开票时间 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_bms_invoice_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_bms_invoice_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_bms_invoice_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.9 `bms_finance`

业务对象：财务交接

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `finance_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 财务交接主键 |
| `handover_no` | varchar(64) | 是 | 唯一 | 标识/引用 | 交接单号 |
| `handover_status` | varchar(32) | 是 | `FINANCE_HANDOVER_STATUS` | 状态/分类 | 待交接、已交接、已入账、失败 |
| `voucher_no` | varchar(128) | 否 |   | 标识/引用 | 财务凭证号 |
| `handover_at` | datetime | 否 |   | 业务属性 | 交接时间 |
| `posted_at` | datetime | 否 |   | 业务属性 | 入账时间 |
| `fail_reason` | varchar(512) | 否 |   | 业务属性 | 失败原因 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_bms_finance_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_bms_finance_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_bms_finance_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

## 5. 枚举值

枚举字段在 DDL 中统一使用 `smallint` 存储，枚举项从 `1` 开始递增；页面展示名、排序和启停状态通过枚举配置表维护。

| 枚举类型 | 数值 | 枚举项 | 说明 |
| --- | --- | --- | --- |
| `ADJUSTMENT_STATUS` | `1` | 草稿 | ADJUSTMENT_STATUS 的第 1 个枚举项 |
| `ADJUSTMENT_STATUS` | `2` | 待审批 | ADJUSTMENT_STATUS 的第 2 个枚举项 |
| `ADJUSTMENT_STATUS` | `3` | 已执行 | ADJUSTMENT_STATUS 的第 3 个枚举项 |
| `ADJUSTMENT_STATUS` | `4` | 已驳回 | ADJUSTMENT_STATUS 的第 4 个枚举项 |
| `ADJUSTMENT_STATUS` | `5` | 已取消 | ADJUSTMENT_STATUS 的第 5 个枚举项 |
| `APPROVAL_STATUS` | `1` | 草稿 | APPROVAL_STATUS 的第 1 个枚举项 |
| `APPROVAL_STATUS` | `2` | 待审批 | APPROVAL_STATUS 的第 2 个枚举项 |
| `APPROVAL_STATUS` | `3` | 已批准 | APPROVAL_STATUS 的第 3 个枚举项 |
| `APPROVAL_STATUS` | `4` | 已驳回 | APPROVAL_STATUS 的第 4 个枚举项 |
| `BILLING_ADJUSTMENT_TYPE` | `1` | 减免 | BILLING_ADJUSTMENT_TYPE 的第 1 个枚举项 |
| `BILLING_ADJUSTMENT_TYPE` | `2` | 补收 | BILLING_ADJUSTMENT_TYPE 的第 2 个枚举项 |
| `BILLING_ADJUSTMENT_TYPE` | `3` | 冲减 | BILLING_ADJUSTMENT_TYPE 的第 3 个枚举项 |
| `BILLING_ADJUSTMENT_TYPE` | `4` | 修正 | BILLING_ADJUSTMENT_TYPE 的第 4 个枚举项 |
| `BILLING_BIZ_TYPE` | `1` | 入库 | BILLING_BIZ_TYPE 的第 1 个枚举项 |
| `BILLING_BIZ_TYPE` | `2` | 出库 | BILLING_BIZ_TYPE 的第 2 个枚举项 |
| `BILLING_BIZ_TYPE` | `3` | 存储 | BILLING_BIZ_TYPE 的第 3 个枚举项 |
| `BILLING_BIZ_TYPE` | `4` | 运输 | BILLING_BIZ_TYPE 的第 4 个枚举项 |
| `BILLING_BIZ_TYPE` | `5` | 退货 | BILLING_BIZ_TYPE 的第 5 个枚举项 |
| `BILLING_BIZ_TYPE` | `6` | 售后 | BILLING_BIZ_TYPE 的第 6 个枚举项 |
| `BILLING_ITEM_STATUS` | `1` | 待计算 | BILLING_ITEM_STATUS 的第 1 个枚举项 |
| `BILLING_ITEM_STATUS` | `2` | 计算异常 | BILLING_ITEM_STATUS 的第 2 个枚举项 |
| `BILLING_ITEM_STATUS` | `3` | 待对账 | BILLING_ITEM_STATUS 的第 3 个枚举项 |
| `BILLING_ITEM_STATUS` | `4` | 对账差异 | BILLING_ITEM_STATUS 的第 4 个枚举项 |
| `BILLING_ITEM_STATUS` | `5` | 已确认 | BILLING_ITEM_STATUS 的第 5 个枚举项 |
| `BILLING_ITEM_STATUS` | `6` | 已入账 | BILLING_ITEM_STATUS 的第 6 个枚举项 |
| `BILLING_ITEM_STATUS` | `7` | 已作废 | BILLING_ITEM_STATUS 的第 7 个枚举项 |
| `BILLING_OBJECT_TYPE` | `1` | 客户 | BILLING_OBJECT_TYPE 的第 1 个枚举项 |
| `BILLING_OBJECT_TYPE` | `2` | 货主 | BILLING_OBJECT_TYPE 的第 2 个枚举项 |
| `BILLING_OBJECT_TYPE` | `3` | 供应商 | BILLING_OBJECT_TYPE 的第 3 个枚举项 |
| `BILLING_OBJECT_TYPE` | `4` | 物流商 | BILLING_OBJECT_TYPE 的第 4 个枚举项 |
| `BILL_STATUS` | `1` | 草稿 | BILL_STATUS 的第 1 个枚举项 |
| `BILL_STATUS` | `2` | 待确认 | BILL_STATUS 的第 2 个枚举项 |
| `BILL_STATUS` | `3` | 差异处理中 | BILL_STATUS 的第 3 个枚举项 |
| `BILL_STATUS` | `4` | 已确认 | BILL_STATUS 的第 4 个枚举项 |
| `BILL_STATUS` | `5` | 待开票 | BILL_STATUS 的第 5 个枚举项 |
| `BILL_STATUS` | `6` | 已开票 | BILL_STATUS 的第 6 个枚举项 |
| `BILL_STATUS` | `7` | 待入账 | BILL_STATUS 的第 7 个枚举项 |
| `BILL_STATUS` | `8` | 已入账 | BILL_STATUS 的第 8 个枚举项 |
| `BILL_STATUS` | `9` | 已关闭 | BILL_STATUS 的第 9 个枚举项 |
| `COMMON_STATUS` | `1` | 启用 | COMMON_STATUS 的第 1 个枚举项 |
| `COMMON_STATUS` | `2` | 停用 | COMMON_STATUS 的第 2 个枚举项 |
| `CONSUME_STATUS` | `1` | 待消费 | CONSUME_STATUS 的第 1 个枚举项 |
| `CONSUME_STATUS` | `2` | 处理中 | CONSUME_STATUS 的第 2 个枚举项 |
| `CONSUME_STATUS` | `3` | 消费成功 | CONSUME_STATUS 的第 3 个枚举项 |
| `CONSUME_STATUS` | `4` | 消费失败 | CONSUME_STATUS 的第 4 个枚举项 |
| `CONSUME_STATUS` | `5` | 已忽略 | CONSUME_STATUS 的第 5 个枚举项 |
| `CURRENCY` | `1` | 人民币 | CURRENCY 的第 1 个枚举项 |
| `CURRENCY` | `2` | 美元 | CURRENCY 的第 2 个枚举项 |
| `CURRENCY` | `3` | 欧元 | CURRENCY 的第 3 个枚举项 |
| `CURRENCY` | `4` | 港币 | CURRENCY 的第 4 个枚举项 |
| `CURRENCY` | `5` | 日元 | CURRENCY 的第 5 个枚举项 |
| `EVENT_PROCESS_STATUS` | `1` | 待处理 | EVENT_PROCESS_STATUS 的第 1 个枚举项 |
| `EVENT_PROCESS_STATUS` | `2` | 成功 | EVENT_PROCESS_STATUS 的第 2 个枚举项 |
| `EVENT_PROCESS_STATUS` | `3` | 失败 | EVENT_PROCESS_STATUS 的第 3 个枚举项 |
| `EVENT_PROCESS_STATUS` | `4` | 已忽略 | EVENT_PROCESS_STATUS 的第 4 个枚举项 |
| `EVENT_STATUS` | `1` | 待发布 | EVENT_STATUS 的第 1 个枚举项 |
| `EVENT_STATUS` | `2` | 发布中 | EVENT_STATUS 的第 2 个枚举项 |
| `EVENT_STATUS` | `3` | 已发布 | EVENT_STATUS 的第 3 个枚举项 |
| `EVENT_STATUS` | `4` | 发布失败 | EVENT_STATUS 的第 4 个枚举项 |
| `EVENT_STATUS` | `5` | 已取消 | EVENT_STATUS 的第 5 个枚举项 |
| `FEE_TYPE` | `1` | 入库费 | FEE_TYPE 的第 1 个枚举项 |
| `FEE_TYPE` | `2` | 出库费 | FEE_TYPE 的第 2 个枚举项 |
| `FEE_TYPE` | `3` | 仓储费 | FEE_TYPE 的第 3 个枚举项 |
| `FEE_TYPE` | `4` | 物流费 | FEE_TYPE 的第 4 个枚举项 |
| `FEE_TYPE` | `5` | 耗材费 | FEE_TYPE 的第 5 个枚举项 |
| `FEE_TYPE` | `6` | 增值服务 | FEE_TYPE 的第 6 个枚举项 |
| `FEE_TYPE` | `7` | 费用类型 | FEE_TYPE 的第 7 个枚举项 |
| `FINANCE_HANDOVER_STATUS` | `1` | 待交接 | FINANCE_HANDOVER_STATUS 的第 1 个枚举项 |
| `FINANCE_HANDOVER_STATUS` | `2` | 已交接 | FINANCE_HANDOVER_STATUS 的第 2 个枚举项 |
| `FINANCE_HANDOVER_STATUS` | `3` | 已入账 | FINANCE_HANDOVER_STATUS 的第 3 个枚举项 |
| `FINANCE_HANDOVER_STATUS` | `4` | 失败 | FINANCE_HANDOVER_STATUS 的第 4 个枚举项 |
| `INVOICE_STATUS` | `1` | 待申请 | INVOICE_STATUS 的第 1 个枚举项 |
| `INVOICE_STATUS` | `2` | 已申请 | INVOICE_STATUS 的第 2 个枚举项 |
| `INVOICE_STATUS` | `3` | 已开票 | INVOICE_STATUS 的第 3 个枚举项 |
| `INVOICE_STATUS` | `4` | 已作废 | INVOICE_STATUS 的第 4 个枚举项 |
| `INVOICE_TYPE` | `1` | 普票 | INVOICE_TYPE 的第 1 个枚举项 |
| `INVOICE_TYPE` | `2` | 专票 | INVOICE_TYPE 的第 2 个枚举项 |
| `INVOICE_TYPE` | `3` | 电子票 | INVOICE_TYPE 的第 3 个枚举项 |
| `OPERATION_RESULT` | `1` | 成功 | OPERATION_RESULT 的第 1 个枚举项 |
| `OPERATION_RESULT` | `2` | 失败 | OPERATION_RESULT 的第 2 个枚举项 |
| `PRICING_METHOD` | `1` | 按件 | PRICING_METHOD 的第 1 个枚举项 |
| `PRICING_METHOD` | `2` | 按重量 | PRICING_METHOD 的第 2 个枚举项 |
| `PRICING_METHOD` | `3` | 按体积 | PRICING_METHOD 的第 3 个枚举项 |
| `PRICING_METHOD` | `4` | 按天 | PRICING_METHOD 的第 4 个枚举项 |
| `PRICING_METHOD` | `5` | 阶梯 | PRICING_METHOD 的第 5 个枚举项 |
| `PRICING_METHOD` | `6` | 固定价 | PRICING_METHOD 的第 6 个枚举项 |
| `RECON_STATUS` | `1` | 草稿 | RECON_STATUS 的第 1 个枚举项 |
| `RECON_STATUS` | `2` | 待确认 | RECON_STATUS 的第 2 个枚举项 |
| `RECON_STATUS` | `3` | 差异处理中 | RECON_STATUS 的第 3 个枚举项 |
| `RECON_STATUS` | `4` | 已确认 | RECON_STATUS 的第 4 个枚举项 |
| `RECON_STATUS` | `5` | 已关闭 | RECON_STATUS 的第 5 个枚举项 |
| `RULE_STATUS` | `1` | 草稿 | RULE_STATUS 的第 1 个枚举项 |
| `RULE_STATUS` | `2` | 已发布 | RULE_STATUS 的第 2 个枚举项 |
| `RULE_STATUS` | `3` | 已停用 | RULE_STATUS 的第 3 个枚举项 |
| `SETTLEMENT_CYCLE` | `1` | 日结 | SETTLEMENT_CYCLE 的第 1 个枚举项 |
| `SETTLEMENT_CYCLE` | `2` | 周结 | SETTLEMENT_CYCLE 的第 2 个枚举项 |
| `SETTLEMENT_CYCLE` | `3` | 月结 | SETTLEMENT_CYCLE 的第 3 个枚举项 |
| `SETTLEMENT_CYCLE` | `4` | 单结 | SETTLEMENT_CYCLE 的第 4 个枚举项 |
| `SETTLEMENT_DIRECTION` | `1` | 应收 | SETTLEMENT_DIRECTION 的第 1 个枚举项 |
| `SETTLEMENT_DIRECTION` | `2` | 应付 | SETTLEMENT_DIRECTION 的第 2 个枚举项 |
| `SOURCE_SYSTEM` | `1` | 库存 | SOURCE_SYSTEM 的第 1 个枚举项 |

## 6. 事件、事件表与审计表字段

事件采用本地消息表模式：应用服务在同一事务内保存业务表和领域事件表，异步发布后更新事件状态；消费外部事件时先写消费日志，使用幂等键避免重复处理。

### 6.1 本系统领域事件

| 事件 | 来源聚合 | 触发动作 | 主要载荷 | 订阅/用途 |
| --- | --- | --- | --- | --- |
| 计费对象已创建 | 02-计费对象聚合CQRS设计 | 创建计费对象 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`草稿`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 计费对象已启用 | 02-计费对象聚合CQRS设计 | 启用计费对象 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已启用`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 计费关系已变更 | 02-计费对象聚合CQRS设计 | 变更计费关系 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已启用`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 计费对象已停用 | 02-计费对象聚合CQRS设计 | 停用计费对象 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已停用`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 客户已启用 | 02-计费对象聚合CQRS设计 | 计费对象应用服务 | 创建或更新客户计费对象快照 | 来源上下文+事件编号+业务主键 |
| 供应商已启用 | 02-计费对象聚合CQRS设计 | 计费对象应用服务 | 创建或更新供应商计费对象快照 | 来源上下文+事件编号+业务主键 |
| 计费规则已创建 | 03-计费规则聚合CQRS设计 | 创建计费规则 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`草稿`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 计费规则已提交审批 | 03-计费规则聚合CQRS设计 | 提交规则审批 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`审批中`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 计费规则已发布 | 03-计费规则聚合CQRS设计 | 发布计费规则 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已发布`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 计费规则已停用 | 03-计费规则聚合CQRS设计 | 停用计费规则 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已停用`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 计费规则版本已复制 | 03-计费规则聚合CQRS设计 | 复制规则版本 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`草稿`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 税率已变更 | 03-计费规则聚合CQRS设计 | 计费规则应用服务 | 标记受影响规则需要复核 | 来源上下文+事件编号+业务主键 |
| 费用来源事件已采集 | 04-费用来源事件聚合CQRS设计 | 采集来源事件 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已采集`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 可计费事实已识别 | 04-费用来源事件聚合CQRS设计 | 识别可计费事实 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待计算`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 费用来源异常已标记 | 04-费用来源事件聚合CQRS设计 | 标记来源异常 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`异常`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 费用来源事件已忽略 | 04-费用来源事件聚合CQRS设计 | 忽略来源事件 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已忽略`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| WMS已发货 | 04-费用来源事件聚合CQRS设计 | 费用采集应用服务 | 生成出库操作费或包装费来源事实 | 来源上下文+事件编号+业务主键 |
| WMS上架已完成 | 04-费用来源事件聚合CQRS设计 | 费用采集应用服务 | 生成入库操作费或上架费来源事实 | 来源上下文+事件编号+业务主键 |
| 费用明细已生成 | 05-费用明细聚合CQRS设计 | 计算费用 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待对账`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 费用已确认 | 05-费用明细聚合CQRS设计 | 确认费用 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已确认`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 费用已作废 | 05-费用明细聚合CQRS设计 | 作废费用 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已作废`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 费用已重算 | 05-费用明细聚合CQRS设计 | 重算费用 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待对账`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 费用调整单已创建 | 06-费用调整单聚合CQRS设计 | 创建调整单 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`草稿`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 费用调整已提交审批 | 06-费用调整单聚合CQRS设计 | 提交调整审批 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`审批中`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 费用调整已审批 | 06-费用调整单聚合CQRS设计 | 审批调整单 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待执行`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 费用已调整 | 06-费用调整单聚合CQRS设计 | 执行调整 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已执行`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 费用调整已驳回 | 06-费用调整单聚合CQRS设计 | 驳回调整单 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已驳回`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 对账差异已创建 | 06-费用调整单聚合CQRS设计 | 费用调整应用服务 | 生成待处理调整建议 | 来源上下文+事件编号+业务主键 |
| 账单已入账 | 06-费用调整单聚合CQRS设计 | 费用调整应用服务 | 限制原费用直接调整并要求冲减补差 | 来源上下文+事件编号+业务主键 |
| 对账单已生成 | 07-对账单聚合CQRS设计 | 生成对账单 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待提交`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 对账单已提交 | 07-对账单聚合CQRS设计 | 提交对账 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待确认`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 对账单已确认 | 07-对账单聚合CQRS设计 | 确认对账 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已确认`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 对账单已关闭 | 07-对账单聚合CQRS设计 | 关闭对账 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已关闭`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 账单已生成 | 08-账单聚合CQRS设计 | 生成账单 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待确认`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 账单已确认 | 08-账单聚合CQRS设计 | 确认账单 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已确认`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 开票已请求 | 08-账单聚合CQRS设计 | 请求开票 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待开票`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 财务交接已请求 | 08-账单聚合CQRS设计 | 请求财务交接 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待入账`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 账单已关闭 | 08-账单聚合CQRS设计 | 关闭账单 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已关闭`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 发票已开具 | 08-账单聚合CQRS设计 | 账单应用服务 | 更新账单开票状态 | 来源上下文+事件编号+业务主键 |
| 发票交接已创建 | 09-发票交接聚合CQRS设计 | 创建开票交接 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待开票`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 发票已请求 | 09-发票交接聚合CQRS设计 | 提交开票请求 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`开票中`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 发票已作废 | 09-发票交接聚合CQRS设计 | 作废发票 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已作废`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 发票交接已关闭 | 09-发票交接聚合CQRS设计 | 关闭发票交接 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已关闭`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 开票系统已回调 | 09-发票交接聚合CQRS设计 | 发票交接应用服务 | 更新发票号、金额和附件 | 来源上下文+事件编号+业务主键 |
| 财务交接已创建 | 10-财务交接聚合CQRS设计 | 创建财务交接 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待交接`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 财务交接已提交 | 10-财务交接聚合CQRS设计 | 提交财务交接 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`交接中`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 财务交接已完成 | 10-财务交接聚合CQRS设计 | 回填入账结果 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已入账`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 财务交接已失败 | 10-财务交接聚合CQRS设计 | 标记交接失败 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`失败`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |
| 财务交接已关闭 | 10-财务交接聚合CQRS设计 | 关闭财务交接 | 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已关闭`、规则版本、来源事件、幂等键 | BMS读模型、对账、账单、发票交接、财务交接、审计日志、报表 |

### 6.2 `bms_domain_event`

| 字段 | 类型 | 是否必填 | 说明 |
| --- | --- | --- | --- |
| `event_id` | bigint | 是 | 事件主键 |
| `event_code` | varchar(128) | 是 | 全局唯一事件编码 |
| `event_name` | varchar(128) | 是 | 中文事件名 |
| `event_type` | varchar(128) | 是 | 稳定事件类型编码 |
| `aggregate_type` | varchar(128) | 是 | 聚合类型 |
| `aggregate_id` | bigint | 是 | 聚合 ID |
| `aggregate_no` | varchar(128) | 否 | 业务单号或编码 |
| `source_system` | varchar(64) | 是 | 来源系统 |
| `payload_json` | json | 是 | 事件载荷 |
| `event_status` | smallint | 是 | `EVENT_STATUS`：1 待发布，2 发布中，3 已发布，4 发布失败，5 已取消 |
| `retry_count` | int | 是 | 重试次数 |
| `fail_reason` | varchar(1024) | 否 | 失败原因 |
| `occurred_at` | datetime | 是 | 业务发生时间 |
| `published_at` | datetime | 否 | 发布时间 |
| `created_at` | datetime | 是 | 创建时间 |
| `updated_at` | datetime | 是 | 更新时间 |

### 6.3 `bms_event_consume_log`

| 字段 | 类型 | 是否必填 | 说明 |
| --- | --- | --- | --- |
| `consume_log_id` | bigint | 是 | 消费日志主键 |
| `event_code` | varchar(128) | 是 | 来源事件编码 |
| `source_system` | varchar(64) | 是 | 来源系统 |
| `consumer_name` | varchar(128) | 是 | 消费者名称 |
| `idempotent_key` | varchar(256) | 是 | 幂等键 |
| `consume_status` | smallint | 是 | `CONSUME_STATUS`：1 待消费，2 处理中，3 消费成功，4 消费失败，5 已忽略 |
| `retry_count` | int | 是 | 重试次数 |
| `fail_reason` | varchar(1024) | 否 | 失败原因 |
| `consumed_at` | datetime | 否 | 消费完成时间 |
| `created_at` | datetime | 是 | 创建时间 |
| `updated_at` | datetime | 是 | 更新时间 |

### 6.4 `bms_operation_audit_log`

| 字段 | 类型 | 是否必填 | 说明 |
| --- | --- | --- | --- |
| `operation_log_id` | bigint | 是 | 操作日志主键 |
| `operator_id` | bigint | 是 | 操作人 ID |
| `operator_name` | varchar(128) | 否 | 操作人名称 |
| `operation_type` | varchar(64) | 是 | 操作类型 |
| `target_type` | varchar(128) | 是 | 操作对象类型 |
| `target_id` | bigint | 否 | 操作对象 ID |
| `target_no` | varchar(128) | 否 | 操作对象单号或编码 |
| `before_snapshot` | json | 否 | 操作前快照 |
| `after_snapshot` | json | 否 | 操作后快照 |
| `result` | smallint | 是 | `OPERATION_RESULT`：1 成功，2 失败 |
| `fail_reason` | varchar(1024) | 否 | 失败原因 |
| `request_id` | varchar(128) | 否 | 请求 ID |
| `operation_at` | datetime | 是 | 操作时间 |
| `created_at` | datetime | 是 | 创建时间 |

## 7. DDL 文件

完整 MySQL 8.0 DDL 已生成到：

- [`ddl/06-BMS系统.sql`](ddl/06-BMS系统.sql)

DDL 包含业务表、枚举类型表、枚举项初始化数据、领域事件发布表、事件消费日志表和操作日志表。

当前结论：数据库表围绕聚合当前状态、内部实体明细、事件日志和读模型支撑设计；枚举字段使用数值编码落库，通过枚举配置表维护显示名和启停状态。
