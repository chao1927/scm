# 05-OMS系统数据库设计

> 本文根据 `docs/03-核心业务模型` 的聚合、不变量、命令事件，以及 `docs/04-子系统功能设计` 的页面、状态、枚举和操作场景整理。本文是数据库表字段设计入口；领域模型和功能设计文档不再保留字段迁移链接。

## 1. 设计口径

| 项目 | 口径 |
| --- | --- |
| 所属系统 | OMS系统 |
| 表名前缀 | `oms_` |
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
| `oms_sales_order` | 销售订单 | `02-销售订单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `oms_sales_order_line` | 销售订单行 | `02-销售订单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `oms_order_result` | 订单审单结果 | `02-销售订单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `oms_fulfillment` | 履约单 | `03-履约单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `oms_fulfillment_line` | 履约单行 | `03-履约单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `oms_stock_reservation` | 库存预占引用 | `03-履约单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `oms_outbound` | 出库单 | `04-出库单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `oms_outbound_line` | 出库单行 | `04-出库单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `oms_cancel` | 取消申请 | `05-取消申请聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `oms_after_sale` | 售后单 | `06-售后单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `oms_after_sale_line` | 售后单行 | `06-售后单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `oms_oms_rule` | OMS 规则配置 | `07-OMS规则配置聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |

## 4. 表字段设计

### 4.1 `oms_sales_order`

业务对象：销售订单

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `sales_order_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 销售订单主键 |
| `sales_order_no` | varchar(64) | 是 | 唯一 | 标识/引用 | 内部销售订单号 |
| `external_order_no` | varchar(128) | 是 | 幂等组合 | 标识/引用 | 外部订单号 |
| `customer_name` | varchar(256) | 否 | 快照 | 业务属性 | 客户名称 |
| `order_type` | varchar(32) | 是 | `SALES_ORDER_TYPE` | 状态/分类 | 普通、预售、换货补发、手工 |
| `pay_status` | varchar(32) | 是 | `PAY_STATUS` | 状态/分类 | 未支付、已支付、部分退款、已退款 |
| `audit_status` | varchar(32) | 是 | `ORDER_AUDIT_STATUS` | 状态/分类 | 待审核、通过、异常、驳回 |
| `order_status` | varchar(32) | 是 | `SALES_ORDER_STATUS` | 状态/分类 | 已创建、待审核、异常待处理、待预占、缺货待处理、已预占、已下发仓库、出库中、已发货、已签收、已完成、已取消 |
| `fulfillment_status` | varchar(32) | 是 | `FULFILLMENT_STATUS` | 状态/分类 | 未履约、部分履约、履约中、已履约、履约失败 |
| `total_amount` | decimal(18,2) | 是 | >= 0 | 数量/金额 | 订单总额 |
| `discount_amount` | decimal(18,2) | 是 | >= 0 | 数量/金额 | 优惠金额 |
| `pay_amount` | decimal(18,2) | 是 | >= 0 | 数量/金额 | 实付金额 |
| `receiver_name` | varchar(128) | 是 |   | 业务属性 | 收货人 |
| `receiver_mobile` | varchar(32) | 是 |   | 业务属性 | 收货电话 |
| `receiver_address` | varchar(512) | 是 |   | 业务属性 | 收货地址 |
| `paid_at` | datetime | 否 |   | 业务属性 | 支付时间 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_oms_sales_order_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_oms_sales_order_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_oms_sales_order_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.2 `oms_sales_order_line`

业务对象：销售订单行

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `sales_order_line_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 销售订单行主键 |
| `sku_code` | varchar(64) | 是 | 快照 | 标识/引用 | SKU 编码 |
| `sku_name` | varchar(256) | 是 | 快照 | 业务属性 | SKU 名称 |
| `order_qty` | decimal(18,4) | 是 | > 0 | 数量/金额 | 下单数量 |
| `reserved_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 已预占数量 |
| `outbound_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 已下发出库数量 |
| `shipped_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 已发货数量 |
| `returned_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 已退货数量 |
| `unit_price` | decimal(18,6) | 是 | >= 0 | 数量/金额 | 商品单价 |
| `line_amount` | decimal(18,2) | 是 | >= 0 | 数量/金额 | 行金额 |
| `line_status` | varchar(32) | 是 | `SALES_ORDER_LINE_STATUS` | 状态/分类 | 待审核、待预占、已预占、出库中、已发货、已完成、已取消 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_oms_sales_order_line_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_oms_sales_order_line_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_oms_sales_order_line_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.3 `oms_order_result`

业务对象：订单审单结果

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `order_result_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 订单审单结果主键 |
| `audit_type` | varchar(32) | 是 | `AUDIT_TYPE` | 状态/分类 | 商品、客户、地址、价格、风控、信用 |
| `audit_result` | varchar(32) | 是 | `AUDIT_RESULT` | 业务属性 | 通过、拦截、警告 |
| `exception_reason` | varchar(512) | 否 |   | 业务属性 | 异常原因 |
| `processed_status` | varchar(32) | 是 | `AUDIT_PROCESS_STATUS` | 状态/分类 | 待处理、已放行、已驳回、已修正 |
| `processed_by` | bigint | 否 |   | 业务属性 | 处理人 |
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
| `uk_oms_order_result_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_oms_order_result_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_oms_order_result_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.4 `oms_fulfillment`

业务对象：履约单

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `fulfillment_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 履约单主键 |
| `fulfillment_order_no` | varchar(64) | 是 | 唯一 | 标识/引用 | 履约单号 |
| `logistics_product_code` | varchar(64) | 否 |   | 标识/引用 | 物流产品 |
| `promise_ship_at` | datetime | 否 |   | 业务属性 | 承诺发货时间 |
| `promise_arrive_at` | datetime | 否 |   | 业务属性 | 承诺送达时间 |
| `fulfillment_status` | varchar(32) | 是 | `FULFILLMENT_ORDER_STATUS` | 状态/分类 | 待预占、已预占、待出库、已下发、出库中、已发货、已取消、失败 |
| `split_reason` | varchar(512) | 否 |   | 业务属性 | 拆单原因 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_oms_fulfillment_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_oms_fulfillment_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_oms_fulfillment_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.5 `oms_fulfillment_line`

业务对象：履约单行

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `fulfillment_line_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 履约单行主键 |
| `fulfillment_qty` | decimal(18,4) | 是 | > 0 | 数量/金额 | 履约数量 |
| `reserved_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 预占数量 |
| `shipped_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 发货数量 |
| `line_status` | varchar(32) | 是 | `FULFILLMENT_LINE_STATUS` | 状态/分类 | 待预占、已预占、待出库、出库中、已发货、已取消 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_oms_fulfillment_line_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_oms_fulfillment_line_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_oms_fulfillment_line_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.6 `oms_stock_reservation`

业务对象：库存预占引用

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `stock_reservation_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 库存预占引用主键 |
| `reservation_no` | varchar(64) | 否 | 唯一可空 | 标识/引用 | 中央库存预占号 |
| `reserve_qty` | decimal(18,4) | 是 | > 0 | 数量/金额 | 请求预占数量 |
| `reserved_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 实际预占数量 |
| `reservation_status` | varchar(32) | 是 | `RESERVATION_STATUS` | 状态/分类 | 待预占、预占成功、预占失败、已释放、已扣减 |
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
| `uk_oms_stock_reservation_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_oms_stock_reservation_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_oms_stock_reservation_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.7 `oms_outbound`

业务对象：出库单

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `outbound_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 出库单主键 |
| `outbound_order_no` | varchar(64) | 是 | 唯一 | 标识/引用 | OMS 出库单号 |
| `outbound_type` | varchar(32) | 是 | `OUTBOUND_TYPE` | 状态/分类 | 销售出库、换货补发、手工补发 |
| `wms_order_no` | varchar(64) | 否 |   | 标识/引用 | WMS 出库单号 |
| `outbound_status` | varchar(32) | 是 | `OUTBOUND_STATUS` | 状态/分类 | 草稿、已下发、WMS已接单、拣货中、已发货、已取消、异常 |
| `released_at` | datetime | 否 |   | 业务属性 | 下发时间 |
| `shipped_at` | datetime | 否 |   | 业务属性 | 发货时间 |
| `cancel_reason` | varchar(512) | 否 |   | 业务属性 | 取消原因 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_oms_outbound_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_oms_outbound_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_oms_outbound_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.8 `oms_outbound_line`

业务对象：出库单行

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `outbound_line_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 出库单行主键 |
| `planned_qty` | decimal(18,4) | 是 | > 0 | 数量/金额 | 计划出库数量 |
| `shipped_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 实际发货数量 |
| `line_status` | varchar(32) | 是 | `OUTBOUND_LINE_STATUS` | 状态/分类 | 待下发、已下发、拣货中、已发货、已取消 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_oms_outbound_line_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_oms_outbound_line_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_oms_outbound_line_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.9 `oms_cancel`

业务对象：取消申请

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `cancel_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 取消申请主键 |
| `cancel_request_no` | varchar(64) | 是 | 唯一 | 标识/引用 | 取消申请号 |
| `cancel_source` | varchar(32) | 是 | `CANCEL_SOURCE` | 状态/分类 | 客户、客服、渠道、系统 |
| `cancel_reason` | varchar(64) | 是 | `CANCEL_REASON` | 业务属性 | 不想要、地址错误、缺货、风控、其他 |
| `cancel_status` | varchar(32) | 是 | `CANCEL_STATUS` | 状态/分类 | 待审核、已同意、已拒绝、取消中、已完成、转售后 |
| `wms_cancel_status` | varchar(32) | 否 | `WMS_CANCEL_STATUS` | 状态/分类 | 未请求、请求中、成功、失败 |
| `stock_release_status` | varchar(32) | 否 | `STOCK_RELEASE_STATUS` | 状态/分类 | 未释放、释放中、已释放、释放失败 |
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
| `uk_oms_cancel_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_oms_cancel_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_oms_cancel_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.10 `oms_after_sale`

业务对象：售后单

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `after_sale_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 售后单主键 |
| `after_sale_no` | varchar(64) | 是 | 唯一 | 标识/引用 | 售后单号 |
| `after_sale_type` | varchar(32) | 是 | `AFTER_SALE_TYPE` | 状态/分类 | 仅退款、退货退款、换货补发 |
| `after_sale_reason` | varchar(64) | 是 | `AFTER_SALE_REASON` | 业务属性 | 质量问题、错发、少发、不想要、其他 |
| `refund_amount` | decimal(18,2) | 否 | >= 0 | 数量/金额 | 退款金额 |
| `after_sale_status` | varchar(32) | 是 | `AFTER_SALE_STATUS` | 状态/分类 | 已创建、待审核、审核驳回、待退货、待验收、待退款、待补发、异常待处理、已完成、已关闭 |
| `approved_at` | datetime | 否 |   | 业务属性 | 审核时间 |
| `completed_at` | datetime | 否 |   | 业务属性 | 完成时间 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_oms_after_sale_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_oms_after_sale_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_oms_after_sale_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.11 `oms_after_sale_line`

业务对象：售后单行

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `after_sale_line_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 售后单行主键 |
| `apply_qty` | decimal(18,4) | 是 | > 0 | 数量/金额 | 申请数量 |
| `received_qty` | decimal(18,4) | 否 | >= 0 | 数量/金额 | 退货入库数量 |
| `accepted_qty` | decimal(18,4) | 否 | >= 0 | 数量/金额 | 验收通过数量 |
| `reship_qty` | decimal(18,4) | 否 | >= 0 | 数量/金额 | 补发数量 |
| `line_status` | varchar(32) | 是 | `AFTER_SALE_LINE_STATUS` | 状态/分类 | 待审核、待退货、待验收、待退款、待补发、已完成 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_oms_after_sale_line_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_oms_after_sale_line_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_oms_after_sale_line_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.12 `oms_oms_rule`

业务对象：OMS 规则配置

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `oms_rule_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | OMS 规则配置主键 |
| `rule_code` | varchar(64) | 是 | 唯一 | 标识/引用 | 规则编码 |
| `rule_name` | varchar(128) | 是 |   | 业务属性 | 规则名称 |
| `rule_type` | varchar(32) | 是 | `OMS_RULE_TYPE` | 状态/分类 | 审单、分仓、取消、售后、承运商 |
| `priority` | int | 是 | >= 0 | 业务属性 | 优先级 |
| `condition_config` | text | 是 | JSON | 业务属性 | 条件配置 |
| `action_config` | text | 是 | JSON | 业务属性 | 动作配置 |
| `status` | varchar(32) | 是 | `COMMON_STATUS` | 状态/分类 | 启用、停用 |
| `effective_from` | datetime | 否 |   | 业务属性 | 生效时间 |
| `effective_to` | datetime | 否 |   | 业务属性 | 失效时间 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_oms_oms_rule_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_oms_oms_rule_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_oms_oms_rule_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

## 5. 枚举值

枚举字段在 DDL 中统一使用 `smallint` 存储，枚举项从 `1` 开始递增；页面展示名、排序和启停状态通过枚举配置表维护。

| 枚举类型 | 数值 | 枚举项 | 说明 |
| --- | --- | --- | --- |
| `AFTER_SALE_LINE_STATUS` | `1` | 待审核 | AFTER_SALE_LINE_STATUS 的第 1 个枚举项 |
| `AFTER_SALE_LINE_STATUS` | `2` | 待退货 | AFTER_SALE_LINE_STATUS 的第 2 个枚举项 |
| `AFTER_SALE_LINE_STATUS` | `3` | 待验收 | AFTER_SALE_LINE_STATUS 的第 3 个枚举项 |
| `AFTER_SALE_LINE_STATUS` | `4` | 待退款 | AFTER_SALE_LINE_STATUS 的第 4 个枚举项 |
| `AFTER_SALE_LINE_STATUS` | `5` | 待补发 | AFTER_SALE_LINE_STATUS 的第 5 个枚举项 |
| `AFTER_SALE_LINE_STATUS` | `6` | 已完成 | AFTER_SALE_LINE_STATUS 的第 6 个枚举项 |
| `AFTER_SALE_REASON` | `1` | 质量问题 | AFTER_SALE_REASON 的第 1 个枚举项 |
| `AFTER_SALE_REASON` | `2` | 错发 | AFTER_SALE_REASON 的第 2 个枚举项 |
| `AFTER_SALE_REASON` | `3` | 少发 | AFTER_SALE_REASON 的第 3 个枚举项 |
| `AFTER_SALE_REASON` | `4` | 不想要 | AFTER_SALE_REASON 的第 4 个枚举项 |
| `AFTER_SALE_REASON` | `5` | 其他 | AFTER_SALE_REASON 的第 5 个枚举项 |
| `AFTER_SALE_STATUS` | `1` | 已创建 | AFTER_SALE_STATUS 的第 1 个枚举项 |
| `AFTER_SALE_STATUS` | `2` | 待审核 | AFTER_SALE_STATUS 的第 2 个枚举项 |
| `AFTER_SALE_STATUS` | `3` | 审核驳回 | AFTER_SALE_STATUS 的第 3 个枚举项 |
| `AFTER_SALE_STATUS` | `4` | 待退货 | AFTER_SALE_STATUS 的第 4 个枚举项 |
| `AFTER_SALE_STATUS` | `5` | 待验收 | AFTER_SALE_STATUS 的第 5 个枚举项 |
| `AFTER_SALE_STATUS` | `6` | 待退款 | AFTER_SALE_STATUS 的第 6 个枚举项 |
| `AFTER_SALE_STATUS` | `7` | 待补发 | AFTER_SALE_STATUS 的第 7 个枚举项 |
| `AFTER_SALE_STATUS` | `8` | 异常待处理 | AFTER_SALE_STATUS 的第 8 个枚举项 |
| `AFTER_SALE_STATUS` | `9` | 已完成 | AFTER_SALE_STATUS 的第 9 个枚举项 |
| `AFTER_SALE_STATUS` | `10` | 已关闭 | AFTER_SALE_STATUS 的第 10 个枚举项 |
| `AFTER_SALE_TYPE` | `1` | 仅退款 | AFTER_SALE_TYPE 的第 1 个枚举项 |
| `AFTER_SALE_TYPE` | `2` | 退货退款 | AFTER_SALE_TYPE 的第 2 个枚举项 |
| `AFTER_SALE_TYPE` | `3` | 换货补发 | AFTER_SALE_TYPE 的第 3 个枚举项 |
| `APPROVAL_STATUS` | `1` | 草稿 | APPROVAL_STATUS 的第 1 个枚举项 |
| `APPROVAL_STATUS` | `2` | 待审批 | APPROVAL_STATUS 的第 2 个枚举项 |
| `APPROVAL_STATUS` | `3` | 已批准 | APPROVAL_STATUS 的第 3 个枚举项 |
| `APPROVAL_STATUS` | `4` | 已驳回 | APPROVAL_STATUS 的第 4 个枚举项 |
| `AUDIT_PROCESS_STATUS` | `1` | 待处理 | AUDIT_PROCESS_STATUS 的第 1 个枚举项 |
| `AUDIT_PROCESS_STATUS` | `2` | 已放行 | AUDIT_PROCESS_STATUS 的第 2 个枚举项 |
| `AUDIT_PROCESS_STATUS` | `3` | 已驳回 | AUDIT_PROCESS_STATUS 的第 3 个枚举项 |
| `AUDIT_PROCESS_STATUS` | `4` | 已修正 | AUDIT_PROCESS_STATUS 的第 4 个枚举项 |
| `AUDIT_RESULT` | `1` | 通过 | AUDIT_RESULT 的第 1 个枚举项 |
| `AUDIT_RESULT` | `2` | 拦截 | AUDIT_RESULT 的第 2 个枚举项 |
| `AUDIT_RESULT` | `3` | 警告 | AUDIT_RESULT 的第 3 个枚举项 |
| `AUDIT_TYPE` | `1` | 商品 | AUDIT_TYPE 的第 1 个枚举项 |
| `AUDIT_TYPE` | `2` | 客户 | AUDIT_TYPE 的第 2 个枚举项 |
| `AUDIT_TYPE` | `3` | 地址 | AUDIT_TYPE 的第 3 个枚举项 |
| `AUDIT_TYPE` | `4` | 价格 | AUDIT_TYPE 的第 4 个枚举项 |
| `AUDIT_TYPE` | `5` | 风控 | AUDIT_TYPE 的第 5 个枚举项 |
| `AUDIT_TYPE` | `6` | 信用 | AUDIT_TYPE 的第 6 个枚举项 |
| `CANCEL_REASON` | `1` | 不想要 | CANCEL_REASON 的第 1 个枚举项 |
| `CANCEL_REASON` | `2` | 地址错误 | CANCEL_REASON 的第 2 个枚举项 |
| `CANCEL_REASON` | `3` | 缺货 | CANCEL_REASON 的第 3 个枚举项 |
| `CANCEL_REASON` | `4` | 风控 | CANCEL_REASON 的第 4 个枚举项 |
| `CANCEL_REASON` | `5` | 其他 | CANCEL_REASON 的第 5 个枚举项 |
| `CANCEL_SOURCE` | `1` | 客户 | CANCEL_SOURCE 的第 1 个枚举项 |
| `CANCEL_SOURCE` | `2` | 客服 | CANCEL_SOURCE 的第 2 个枚举项 |
| `CANCEL_SOURCE` | `3` | 渠道 | CANCEL_SOURCE 的第 3 个枚举项 |
| `CANCEL_SOURCE` | `4` | 系统 | CANCEL_SOURCE 的第 4 个枚举项 |
| `CANCEL_STATUS` | `1` | 待审核 | CANCEL_STATUS 的第 1 个枚举项 |
| `CANCEL_STATUS` | `2` | 已同意 | CANCEL_STATUS 的第 2 个枚举项 |
| `CANCEL_STATUS` | `3` | 已拒绝 | CANCEL_STATUS 的第 3 个枚举项 |
| `CANCEL_STATUS` | `4` | 取消中 | CANCEL_STATUS 的第 4 个枚举项 |
| `CANCEL_STATUS` | `5` | 已完成 | CANCEL_STATUS 的第 5 个枚举项 |
| `CANCEL_STATUS` | `6` | 转售后 | CANCEL_STATUS 的第 6 个枚举项 |
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
| `EVENT_STATUS` | `1` | 待发布 | EVENT_STATUS 的第 1 个枚举项 |
| `EVENT_STATUS` | `2` | 发布中 | EVENT_STATUS 的第 2 个枚举项 |
| `EVENT_STATUS` | `3` | 已发布 | EVENT_STATUS 的第 3 个枚举项 |
| `EVENT_STATUS` | `4` | 发布失败 | EVENT_STATUS 的第 4 个枚举项 |
| `EVENT_STATUS` | `5` | 已取消 | EVENT_STATUS 的第 5 个枚举项 |
| `FULFILLMENT_LINE_STATUS` | `1` | 待预占 | FULFILLMENT_LINE_STATUS 的第 1 个枚举项 |
| `FULFILLMENT_LINE_STATUS` | `2` | 已预占 | FULFILLMENT_LINE_STATUS 的第 2 个枚举项 |
| `FULFILLMENT_LINE_STATUS` | `3` | 待出库 | FULFILLMENT_LINE_STATUS 的第 3 个枚举项 |
| `FULFILLMENT_LINE_STATUS` | `4` | 出库中 | FULFILLMENT_LINE_STATUS 的第 4 个枚举项 |
| `FULFILLMENT_LINE_STATUS` | `5` | 已发货 | FULFILLMENT_LINE_STATUS 的第 5 个枚举项 |
| `FULFILLMENT_LINE_STATUS` | `6` | 已取消 | FULFILLMENT_LINE_STATUS 的第 6 个枚举项 |
| `FULFILLMENT_ORDER_STATUS` | `1` | 待预占 | FULFILLMENT_ORDER_STATUS 的第 1 个枚举项 |
| `FULFILLMENT_ORDER_STATUS` | `2` | 已预占 | FULFILLMENT_ORDER_STATUS 的第 2 个枚举项 |
| `FULFILLMENT_ORDER_STATUS` | `3` | 待出库 | FULFILLMENT_ORDER_STATUS 的第 3 个枚举项 |
| `FULFILLMENT_ORDER_STATUS` | `4` | 已下发 | FULFILLMENT_ORDER_STATUS 的第 4 个枚举项 |
| `FULFILLMENT_ORDER_STATUS` | `5` | 出库中 | FULFILLMENT_ORDER_STATUS 的第 5 个枚举项 |
| `FULFILLMENT_ORDER_STATUS` | `6` | 已发货 | FULFILLMENT_ORDER_STATUS 的第 6 个枚举项 |
| `FULFILLMENT_ORDER_STATUS` | `7` | 已取消 | FULFILLMENT_ORDER_STATUS 的第 7 个枚举项 |
| `FULFILLMENT_ORDER_STATUS` | `8` | 失败 | FULFILLMENT_ORDER_STATUS 的第 8 个枚举项 |
| `FULFILLMENT_STATUS` | `1` | 未履约 | FULFILLMENT_STATUS 的第 1 个枚举项 |
| `FULFILLMENT_STATUS` | `2` | 部分履约 | FULFILLMENT_STATUS 的第 2 个枚举项 |
| `FULFILLMENT_STATUS` | `3` | 履约中 | FULFILLMENT_STATUS 的第 3 个枚举项 |
| `FULFILLMENT_STATUS` | `4` | 已履约 | FULFILLMENT_STATUS 的第 4 个枚举项 |
| `FULFILLMENT_STATUS` | `5` | 履约失败 | FULFILLMENT_STATUS 的第 5 个枚举项 |
| `OMS_RULE_TYPE` | `1` | 审单 | OMS_RULE_TYPE 的第 1 个枚举项 |
| `OMS_RULE_TYPE` | `2` | 分仓 | OMS_RULE_TYPE 的第 2 个枚举项 |
| `OMS_RULE_TYPE` | `3` | 取消 | OMS_RULE_TYPE 的第 3 个枚举项 |
| `OMS_RULE_TYPE` | `4` | 售后 | OMS_RULE_TYPE 的第 4 个枚举项 |
| `OMS_RULE_TYPE` | `5` | 承运商 | OMS_RULE_TYPE 的第 5 个枚举项 |
| `OPERATION_RESULT` | `1` | 成功 | OPERATION_RESULT 的第 1 个枚举项 |
| `OPERATION_RESULT` | `2` | 失败 | OPERATION_RESULT 的第 2 个枚举项 |
| `ORDER_AUDIT_STATUS` | `1` | 待审核 | ORDER_AUDIT_STATUS 的第 1 个枚举项 |
| `ORDER_AUDIT_STATUS` | `2` | 通过 | ORDER_AUDIT_STATUS 的第 2 个枚举项 |
| `ORDER_AUDIT_STATUS` | `3` | 异常 | ORDER_AUDIT_STATUS 的第 3 个枚举项 |
| `ORDER_AUDIT_STATUS` | `4` | 驳回 | ORDER_AUDIT_STATUS 的第 4 个枚举项 |
| `OUTBOUND_LINE_STATUS` | `1` | 待下发 | OUTBOUND_LINE_STATUS 的第 1 个枚举项 |
| `OUTBOUND_LINE_STATUS` | `2` | 已下发 | OUTBOUND_LINE_STATUS 的第 2 个枚举项 |
| `OUTBOUND_LINE_STATUS` | `3` | 拣货中 | OUTBOUND_LINE_STATUS 的第 3 个枚举项 |
| `OUTBOUND_LINE_STATUS` | `4` | 已发货 | OUTBOUND_LINE_STATUS 的第 4 个枚举项 |
| `OUTBOUND_LINE_STATUS` | `5` | 已取消 | OUTBOUND_LINE_STATUS 的第 5 个枚举项 |
| `OUTBOUND_STATUS` | `1` | 草稿 | OUTBOUND_STATUS 的第 1 个枚举项 |
| `OUTBOUND_STATUS` | `2` | 已下发 | OUTBOUND_STATUS 的第 2 个枚举项 |
| `OUTBOUND_STATUS` | `3` | WMS已接单 | OUTBOUND_STATUS 的第 3 个枚举项 |
| `OUTBOUND_STATUS` | `4` | 拣货中 | OUTBOUND_STATUS 的第 4 个枚举项 |
| `OUTBOUND_STATUS` | `5` | 已发货 | OUTBOUND_STATUS 的第 5 个枚举项 |
| `OUTBOUND_STATUS` | `6` | 已取消 | OUTBOUND_STATUS 的第 6 个枚举项 |
| `OUTBOUND_STATUS` | `7` | 异常 | OUTBOUND_STATUS 的第 7 个枚举项 |
| `OUTBOUND_TYPE` | `1` | 销售出库 | OUTBOUND_TYPE 的第 1 个枚举项 |
| `OUTBOUND_TYPE` | `2` | 换货补发 | OUTBOUND_TYPE 的第 2 个枚举项 |
| `OUTBOUND_TYPE` | `3` | 手工补发 | OUTBOUND_TYPE 的第 3 个枚举项 |
| `PAY_STATUS` | `1` | 未支付 | PAY_STATUS 的第 1 个枚举项 |
| `PAY_STATUS` | `2` | 已支付 | PAY_STATUS 的第 2 个枚举项 |
| `PAY_STATUS` | `3` | 部分退款 | PAY_STATUS 的第 3 个枚举项 |
| `PAY_STATUS` | `4` | 已退款 | PAY_STATUS 的第 4 个枚举项 |
| `RESERVATION_STATUS` | `1` | 待预占 | RESERVATION_STATUS 的第 1 个枚举项 |
| `RESERVATION_STATUS` | `2` | 预占成功 | RESERVATION_STATUS 的第 2 个枚举项 |
| `RESERVATION_STATUS` | `3` | 预占失败 | RESERVATION_STATUS 的第 3 个枚举项 |
| `RESERVATION_STATUS` | `4` | 已释放 | RESERVATION_STATUS 的第 4 个枚举项 |
| `RESERVATION_STATUS` | `5` | 已扣减 | RESERVATION_STATUS 的第 5 个枚举项 |
| `SALES_ORDER_LINE_STATUS` | `1` | 待审核 | SALES_ORDER_LINE_STATUS 的第 1 个枚举项 |
| `SALES_ORDER_LINE_STATUS` | `2` | 待预占 | SALES_ORDER_LINE_STATUS 的第 2 个枚举项 |
| `SALES_ORDER_LINE_STATUS` | `3` | 已预占 | SALES_ORDER_LINE_STATUS 的第 3 个枚举项 |
| `SALES_ORDER_LINE_STATUS` | `4` | 出库中 | SALES_ORDER_LINE_STATUS 的第 4 个枚举项 |
| `SALES_ORDER_LINE_STATUS` | `5` | 已发货 | SALES_ORDER_LINE_STATUS 的第 5 个枚举项 |
| `SALES_ORDER_LINE_STATUS` | `6` | 已完成 | SALES_ORDER_LINE_STATUS 的第 6 个枚举项 |
| `SALES_ORDER_LINE_STATUS` | `7` | 已取消 | SALES_ORDER_LINE_STATUS 的第 7 个枚举项 |
| `SALES_ORDER_STATUS` | `1` | 已创建 | SALES_ORDER_STATUS 的第 1 个枚举项 |
| `SALES_ORDER_STATUS` | `2` | 待审核 | SALES_ORDER_STATUS 的第 2 个枚举项 |
| `SALES_ORDER_STATUS` | `3` | 异常待处理 | SALES_ORDER_STATUS 的第 3 个枚举项 |
| `SALES_ORDER_STATUS` | `4` | 待预占 | SALES_ORDER_STATUS 的第 4 个枚举项 |
| `SALES_ORDER_STATUS` | `5` | 缺货待处理 | SALES_ORDER_STATUS 的第 5 个枚举项 |
| `SALES_ORDER_STATUS` | `6` | 已预占 | SALES_ORDER_STATUS 的第 6 个枚举项 |
| `SALES_ORDER_STATUS` | `7` | 已下发仓库 | SALES_ORDER_STATUS 的第 7 个枚举项 |
| `SALES_ORDER_STATUS` | `8` | 出库中 | SALES_ORDER_STATUS 的第 8 个枚举项 |
| `SALES_ORDER_STATUS` | `9` | 已发货 | SALES_ORDER_STATUS 的第 9 个枚举项 |
| `SALES_ORDER_STATUS` | `10` | 已签收 | SALES_ORDER_STATUS 的第 10 个枚举项 |
| `SALES_ORDER_STATUS` | `11` | 已完成 | SALES_ORDER_STATUS 的第 11 个枚举项 |
| `SALES_ORDER_STATUS` | `12` | 已取消 | SALES_ORDER_STATUS 的第 12 个枚举项 |
| `SALES_ORDER_TYPE` | `1` | 普通 | SALES_ORDER_TYPE 的第 1 个枚举项 |
| `SALES_ORDER_TYPE` | `2` | 预售 | SALES_ORDER_TYPE 的第 2 个枚举项 |
| `SALES_ORDER_TYPE` | `3` | 换货补发 | SALES_ORDER_TYPE 的第 3 个枚举项 |
| `SALES_ORDER_TYPE` | `4` | 手工 | SALES_ORDER_TYPE 的第 4 个枚举项 |
| `STOCK_RELEASE_STATUS` | `1` | 未释放 | STOCK_RELEASE_STATUS 的第 1 个枚举项 |
| `STOCK_RELEASE_STATUS` | `2` | 释放中 | STOCK_RELEASE_STATUS 的第 2 个枚举项 |
| `STOCK_RELEASE_STATUS` | `3` | 已释放 | STOCK_RELEASE_STATUS 的第 3 个枚举项 |
| `STOCK_RELEASE_STATUS` | `4` | 释放失败 | STOCK_RELEASE_STATUS 的第 4 个枚举项 |
| `WMS_CANCEL_STATUS` | `1` | 未请求 | WMS_CANCEL_STATUS 的第 1 个枚举项 |
| `WMS_CANCEL_STATUS` | `2` | 请求中 | WMS_CANCEL_STATUS 的第 2 个枚举项 |
| `WMS_CANCEL_STATUS` | `3` | 成功 | WMS_CANCEL_STATUS 的第 3 个枚举项 |
| `WMS_CANCEL_STATUS` | `4` | 失败 | WMS_CANCEL_STATUS 的第 4 个枚举项 |

## 6. 事件、事件表与审计表字段

事件采用本地消息表模式：应用服务在同一事务内保存业务表和领域事件表，异步发布后更新事件状态；消费外部事件时先写消费日志，使用幂等键避免重复处理。

### 6.1 本系统领域事件

| 事件 | 来源聚合 | 触发动作 | 主要载荷 | 订阅/用途 |
| --- | --- | --- | --- | --- |
| 销售订单已创建 | 02-销售订单聚合CQRS设计 | 接收订单 | 销售订单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 订单已审核 | 02-销售订单聚合CQRS设计 | 审核订单 | 销售订单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 订单异常已标记 | 02-销售订单聚合CQRS设计 | 标记异常 | 销售订单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 订单已发货 | 02-销售订单聚合CQRS设计 | 确认发货 | 销售订单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 订单已签收 | 02-销售订单聚合CQRS设计 | 确认签收 | 销售订单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 订单已完成 | 02-销售订单聚合CQRS设计 | 完成订单 | 销售订单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 订单已取消 | 02-销售订单聚合CQRS设计 | 取消订单 | 销售订单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| WMS已发货 | 02-销售订单聚合CQRS设计 | 外部事件消费服务 | 记录发货事实并推进订单状态 | 来源上下文+事件编号+业务主键 |
| 库存已预占 | 02-销售订单聚合CQRS设计 | 库存事件消费服务 | 记录预占成功并推进履约 | 库存上下文+事件编号+reservationId |
| 退款已完成 | 02-销售订单聚合CQRS设计 | BMS事件消费服务 | 更新售后和订单退款状态 | BMS上下文+事件编号+refundId |
| 履约单已创建 | 03-履约单聚合CQRS设计 | 创建履约单 | 履约单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 库存预占已请求 | 03-履约单聚合CQRS设计 | 请求库存预占 | 履约单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 履约库存已预占 | 03-履约单聚合CQRS设计 | 记录预占成功 | 履约单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 履约出库已请求 | 03-履约单聚合CQRS设计 | 生成出库指令 | 履约单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 履约单已关闭 | 03-履约单聚合CQRS设计 | 关闭履约单 | 履约单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 出库单已创建 | 04-出库单聚合CQRS设计 | 创建出库单 | 出库单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 出库单已下发WMS | 04-出库单聚合CQRS设计 | 下发WMS | 出库单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| WMS已接单 | 04-出库单聚合CQRS设计 | 记录WMS接单 | 出库单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 出库单已取消 | 04-出库单聚合CQRS设计 | 取消出库单 | 出库单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 出库单已关闭 | 04-出库单聚合CQRS设计 | 关闭出库单 | 出库单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 取消申请已创建 | 05-取消申请聚合CQRS设计 | 创建取消申请 | 取消申请ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 取消申请已审核 | 05-取消申请聚合CQRS设计 | 审核取消申请 | 取消申请ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| WMS取消已请求 | 05-取消申请聚合CQRS设计 | 请求WMS取消 | 取消申请ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 取消库存释放已请求 | 05-取消申请聚合CQRS设计 | 释放库存预占 | 取消申请ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 取消申请已关闭 | 05-取消申请聚合CQRS设计 | 关闭取消申请 | 取消申请ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 取消请求已提交 | 05-取消申请聚合CQRS设计 | 外部事件消费服务 | 创建取消申请并判断可取消路径 | 来源上下文+事件编号+业务主键 |
| 售后单已创建 | 06-售后单聚合CQRS设计 | 创建售后 | 售后单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 售后已审核 | 06-售后单聚合CQRS设计 | 审核售后 | 售后单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 退货已验收 | 06-售后单聚合CQRS设计 | 确认退货验收 | 售后单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 退款已请求 | 06-售后单聚合CQRS设计 | 发起退款 | 售后单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 补发已请求 | 06-售后单聚合CQRS设计 | 创建补发 | 售后单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 售后已完成 | 06-售后单聚合CQRS设计 | 关闭售后 | 售后单ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| OMS规则已创建 | 07-OMS规则配置聚合CQRS设计 | 创建规则 | OMS规则配置ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| OMS规则已提交审批 | 07-OMS规则配置聚合CQRS设计 | 提交规则审批 | OMS规则配置ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| OMS规则已发布 | 07-OMS规则配置聚合CQRS设计 | 发布规则 | OMS规则配置ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| OMS规则已停用 | 07-OMS规则配置聚合CQRS设计 | 停用规则 | OMS规则配置ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| OMS规则已回滚 | 07-OMS规则配置聚合CQRS设计 | 回滚规则 | OMS规则配置ID、订单号、渠道、客户、SKU、数量、金额、状态 | 中央库存、WMS、BMS、读模型、审计日志、报表 |
| 仓库已启用 | 07-OMS规则配置聚合CQRS设计 | 外部事件消费服务 | 刷新规则可选仓库范围 | 来源上下文+事件编号+业务主键 |

### 6.2 `oms_domain_event`

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

### 6.3 `oms_event_consume_log`

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

### 6.4 `oms_operation_audit_log`

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

- [`ddl/05-OMS系统.sql`](ddl/05-OMS系统.sql)

DDL 包含业务表、枚举类型表、枚举项初始化数据、领域事件发布表、事件消费日志表和操作日志表。

当前结论：数据库表围绕聚合当前状态、内部实体明细、事件日志和读模型支撑设计；枚举字段使用数值编码落库，通过枚举配置表维护显示名和启停状态。
