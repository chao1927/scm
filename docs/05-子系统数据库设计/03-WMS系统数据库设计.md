# WMS系统数据库设计

> 本文根据 `docs/03-核心业务模型` 的聚合、不变量、命令事件，以及 `docs/04-子系统功能设计` 的页面、状态、枚举和操作场景整理。本文是数据库表字段设计入口；领域模型和功能设计文档不再保留字段迁移链接。

## 1. 设计口径

| 项目 | 口径 |
| --- | --- |
| 所属系统 | WMS系统 |
| 表名前缀 | `wms_` |
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
| `wms_inbound` | 入库单 | `02-入库单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `wms_receive` | 收货单 | `03-收货单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `wms_receive_line` | 收货行 | `03-收货单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `wms_qc` | 质检单 | `04-质检单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `wms_putaway` | 上架任务 | `05-上架任务聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `wms_stock` | 仓内库存 | `06-库内库存聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `wms_outbound` | 出库单 | `07-出库单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `wms_outbound_line` | 出库单行 | `07-出库单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `wms_wave` | 波次单 | `08-波次单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `wms_pick` | 拣货单 | `09-拣货单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `wms_pick_task` | 拣货任务 | `09-拣货单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `wms_container` | 周转容器 | `10-周转容器聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `wms_review_pack` | 复核包装单 | `11-复核包装单聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `wms_package` | 包裹 | `12-发货交接聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `wms_count` | 盘点计划 | `13-盘点计划聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |
| `wms_exception_record` | 异常记录 | `14-仓内异常聚合CQRS设计.md` | 承载聚合当前状态、业务快照和列表查询核心字段 |

## 4. 表字段设计

### 4.1 `wms_inbound`

业务对象：入库单

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `inbound_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 入库单主键 |
| `inbound_order_no` | varchar(64) | 是 | 唯一 | 标识/引用 | WMS 入库单号 |
| `source_order_no` | varchar(64) | 是 | 幂等组合 | 标识/引用 | 来源单号，如 ASN、调拨入库、售后退货 |
| `source_type` | varchar(32) | 是 | `INBOUND_SOURCE_TYPE` | 状态/分类 | 采购、调拨、销售退货、其他 |
| `inbound_status` | varchar(32) | 是 | `INBOUND_STATUS` | 状态/分类 | 待到货、到货中、收货中、待质检、待上架、部分上架、已上架、已关闭、已取消 |
| `expected_arrival_at` | datetime | 否 |   | 业务属性 | 预计到仓时间 |
| `arrived_at` | datetime | 否 |   | 业务属性 | 到货时间 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_wms_inbound_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_wms_inbound_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_wms_inbound_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.2 `wms_receive`

业务对象：收货单

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `receive_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 收货单主键 |
| `receipt_order_no` | varchar(64) | 是 | 唯一 | 标识/引用 | 收货单号 |
| `receipt_status` | varchar(32) | 是 | `RECEIPT_STATUS` | 状态/分类 | 待收货、收货中、部分收货、已收货、异常、已关闭 |
| `started_at` | datetime | 否 |   | 业务属性 | 开始收货时间 |
| `completed_at` | datetime | 否 |   | 业务属性 | 收货完成时间 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_wms_receive_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_wms_receive_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_wms_receive_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.3 `wms_receive_line`

业务对象：收货行

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `receive_line_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 收货行主键 |
| `sku_code` | varchar(64) | 是 | 快照 | 标识/引用 | SKU 编码 |
| `expected_qty` | decimal(18,4) | 是 | >= 0 | 数量/金额 | 应收数量 |
| `received_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 实收数量 |
| `short_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 短收数量 |
| `over_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 超收数量 |
| `batch_no` | varchar(128) | 否 |   | 标识/引用 | 批次号 |
| `production_date` | date | 否 |   | 时间 | 生产日期 |
| `expire_date` | date | 否 |   | 时间 | 效期 |
| `quality_status` | varchar(32) | 是 | `QUALITY_STATUS` | 状态/分类 | 待检、合格、不合格、免检、待处理 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_wms_receive_line_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_wms_receive_line_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_wms_receive_line_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.4 `wms_qc`

业务对象：质检单

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `qc_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 质检单主键 |
| `qc_order_no` | varchar(64) | 是 | 唯一 | 标识/引用 | 质检单号 |
| `sample_qty` | decimal(18,4) | 否 | >= 0 | 数量/金额 | 抽检数量 |
| `accepted_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 合格数量 |
| `rejected_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 不合格数量 |
| `qc_result` | varchar(32) | 是 | `QC_RESULT` | 业务属性 | 待检、合格、不合格、部分合格 |
| `reject_reason` | varchar(512) | 否 |   | 业务属性 | 不合格原因 |
| `completed_at` | datetime | 否 |   | 业务属性 | 质检完成时间 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_wms_qc_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_wms_qc_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_wms_qc_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.5 `wms_putaway`

业务对象：上架任务

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `putaway_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 上架任务主键 |
| `batch_no` | varchar(128) | 否 |   | 标识/引用 | 批次 |
| `quality_status` | varchar(32) | 是 | `QUALITY_STATUS` | 状态/分类 | 合格、不合格、待处理 |
| `required_qty` | decimal(18,4) | 是 | > 0 | 数量/金额 | 应上架数量 |
| `putaway_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 已上架数量 |
| `task_status` | varchar(32) | 是 | `PUTAWAY_TASK_STATUS` | 状态/分类 | 待上架、上架中、部分上架、已完成、异常 |
| `putaway_at` | datetime | 否 |   | 业务属性 | 上架完成时间 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_wms_putaway_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_wms_putaway_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_wms_putaway_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.6 `wms_stock`

业务对象：仓内库存

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `stock_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 仓内库存主键 |
| `batch_no` | varchar(128) | 否 |   | 标识/引用 | 批次 |
| `stock_status` | varchar(32) | 是 | `WMS_STOCK_STATUS` | 状态/分类 | 可拣、冻结、不合格、待退供、待报废 |
| `qty` | decimal(18,4) | 是 | >= 0 | 数量/金额 | 库位数量 |
| `locked_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 作业锁定数量 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_wms_stock_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_wms_stock_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_wms_stock_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.7 `wms_outbound`

业务对象：出库单

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `outbound_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 出库单主键 |
| `outbound_order_no` | varchar(64) | 是 | 唯一 | 标识/引用 | WMS 出库单号 |
| `source_order_no` | varchar(64) | 是 | 幂等组合 | 标识/引用 | 来源单号 |
| `source_type` | varchar(32) | 是 | `OUTBOUND_SOURCE_TYPE` | 状态/分类 | 销售、调拨、退供、其他 |
| `priority` | int | 是 | >= 0 | 业务属性 | 优先级 |
| `outbound_status` | varchar(32) | 是 | `WMS_OUTBOUND_STATUS` | 状态/分类 | 待接单、待分配、分配失败、待拣货、拣货中、待复核、复核异常、待发货、已发货、已关闭、已取消 |
| `shipped_at` | datetime | 否 |   | 业务属性 | 发货时间 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_wms_outbound_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_wms_outbound_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_wms_outbound_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.8 `wms_outbound_line`

业务对象：出库单行

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `outbound_line_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 出库单行主键 |
| `sku_code` | varchar(64) | 是 | 快照 | 标识/引用 | SKU 编码 |
| `planned_qty` | decimal(18,4) | 是 | > 0 | 数量/金额 | 计划出库数量 |
| `allocated_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 已分配数量 |
| `picked_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 已拣数量 |
| `shipped_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 已发货数量 |
| `line_status` | varchar(32) | 是 | `WMS_OUTBOUND_LINE_STATUS` | 状态/分类 | 待分配、已分配、拣货中、已拣货、已发货、已取消 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_wms_outbound_line_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_wms_outbound_line_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_wms_outbound_line_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.9 `wms_wave`

业务对象：波次单

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `wave_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 波次单主键 |
| `wave_no` | varchar(64) | 是 | 唯一 | 标识/引用 | 波次号 |
| `wave_type` | varchar(32) | 是 | `WAVE_TYPE` | 状态/分类 | 单单拣、批量拣、边拣边分 |
| `order_count` | int | 是 | >= 0 | 业务属性 | 出库单数 |
| `sku_count` | int | 是 | >= 0 | 业务属性 | SKU 数 |
| `wave_status` | varchar(32) | 是 | `WAVE_STATUS` | 状态/分类 | 草稿、已释放、拣货中、已完成、已取消 |
| `released_at` | datetime | 否 |   | 业务属性 | 释放时间 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_wms_wave_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_wms_wave_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_wms_wave_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.10 `wms_pick`

业务对象：拣货单

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `pick_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 拣货单主键 |
| `picking_order_no` | varchar(64) | 是 | 唯一 | 标识/引用 | 拣货单号 |
| `picking_mode` | varchar(32) | 是 | `PICKING_MODE` | 状态/分类 | 单单拣、批量拣、边拣边分 |
| `picking_status` | varchar(32) | 是 | `PICKING_STATUS` | 状态/分类 | 待分配、待拣货、拣货中、部分拣货、拣货异常、已拣货、已交接复核 |
| `started_at` | datetime | 否 |   | 业务属性 | 开始时间 |
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
| `uk_wms_pick_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_wms_pick_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_wms_pick_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.11 `wms_pick_task`

业务对象：拣货任务

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `pick_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 拣货任务主键 |
| `batch_no` | varchar(128) | 否 |   | 标识/引用 | 批次 |
| `required_qty` | decimal(18,4) | 是 | > 0 | 数量/金额 | 应拣数量 |
| `picked_qty` | decimal(18,4) | 是 | 默认 0 | 数量/金额 | 已拣数量 |
| `task_status` | varchar(32) | 是 | `PICKING_TASK_STATUS` | 状态/分类 | 待拣货、拣货中、已完成、异常 |
| `picked_at` | datetime | 否 |   | 业务属性 | 拣货完成时间 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_wms_pick_task_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_wms_pick_task_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_wms_pick_task_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.12 `wms_container`

业务对象：周转容器

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `container_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 周转容器主键 |
| `container_code` | varchar(64) | 是 | 唯一 | 标识/引用 | 容器编码 |
| `container_type` | varchar(32) | 是 | `CONTAINER_TYPE` | 状态/分类 | 周转箱、播种车、格口、托盘 |
| `bind_object_type` | varchar(32) | 否 | `CONTAINER_BIND_TYPE` | 状态/分类 | 拣货单、出库单、包裹 |
| `container_status` | varchar(32) | 是 | `CONTAINER_STATUS` | 状态/分类 | 空闲、已绑定、拣货中、待复核、已清空、停用 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_wms_container_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_wms_container_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_wms_container_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.13 `wms_review_pack`

业务对象：复核包装单

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `review_pack_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 复核包装单主键 |
| `pack_order_no` | varchar(64) | 是 | 唯一 | 标识/引用 | 包装单号 |
| `pack_status` | varchar(32) | 是 | `PACK_STATUS` | 状态/分类 | 待复核、复核中、复核异常、待打包、已打包、已关闭 |
| `weight` | decimal(18,4) | 否 | >= 0 | 业务属性 | 重量 |
| `volume` | decimal(18,4) | 否 | >= 0 | 业务属性 | 体积 |
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
| `uk_wms_review_pack_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_wms_review_pack_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_wms_review_pack_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.14 `wms_package`

业务对象：包裹

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `business_object_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 包裹主键 |
| `package_no` | varchar(64) | 是 | 唯一 | 标识/引用 | 包裹号 |
| `tracking_no` | varchar(128) | 否 |   | 标识/引用 | 运单号 |
| `weight` | decimal(18,4) | 否 | >= 0 | 业务属性 | 重量 |
| `package_status` | varchar(32) | 是 | `PACKAGE_STATUS` | 状态/分类 | 已打包、待交接、已交接、已取消 |
| `shipped_at` | datetime | 否 |   | 业务属性 | 发货时间 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_wms_package_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_wms_package_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_wms_package_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.15 `wms_count`

业务对象：盘点计划

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `count_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 盘点计划主键 |
| `count_plan_no` | varchar(64) | 是 | 唯一 | 标识/引用 | 盘点计划号 |
| `count_type` | varchar(32) | 是 | `COUNT_TYPE` | 状态/分类 | 全盘、动盘、循环盘、抽盘 |
| `scope_config` | text | 是 | JSON | 业务属性 | 盘点范围 |
| `plan_status` | varchar(32) | 是 | `COUNT_PLAN_STATUS` | 状态/分类 | 草稿、已下发、盘点中、差异处理中、已完成、已取消 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_wms_count_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_wms_count_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_wms_count_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

### 4.16 `wms_exception_record`

业务对象：异常记录

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `exception_record_id` | bigint | 是 | 主键，雪花ID或数据库自增 | 技术主键 | 异常记录主键 |
| `exception_no` | varchar(64) | 是 | 唯一 | 标识/引用 | 异常单号 |
| `source_type` | varchar(32) | 是 | `WMS_EXCEPTION_SOURCE` | 状态/分类 | 收货、质检、上架、分配、拣货、复核、发货、盘点 |
| `exception_type` | varchar(32) | 是 | `WMS_EXCEPTION_TYPE` | 状态/分类 | 短收、超收、错货、破损、库位不符、复核差异、盘点差异 |
| `responsible_party` | varchar(32) | 否 | `RESPONSIBLE_PARTY` | 业务属性 | 供应商、仓库、物流、客户、系统 |
| `exception_status` | varchar(32) | 是 | `EXCEPTION_STATUS` | 状态/分类 | 待处理、处理中、已关闭 |
| `description` | varchar(1024) | 否 |   | 业务属性 | 异常说明 |
| `closed_at` | datetime | 否 |   | 业务属性 | 关闭时间 |
| `created_by` | bigint | 是 | 写入时填充 | 审计 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 审计 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 审计 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 审计 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 并发控制 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 数据治理 | 逻辑删除标记 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_wms_exception_record_biz_no` | 业务单号或编码字段 | 如果该表存在编码/单号字段，必须唯一 |
| `idx_wms_exception_record_status_time` | 状态字段、`updated_at` | 支撑列表筛选和待办查询 |
| `idx_wms_exception_record_source` | 来源单号、外部单号或引用编码 | 支撑跨系统追踪 |

## 5. 枚举值

枚举字段在 DDL 中统一使用 `smallint` 存储，枚举项从 `1` 开始递增；页面展示名、排序和启停状态通过枚举配置表维护。

| 枚举类型 | 数值 | 枚举项 | 说明 |
| --- | --- | --- | --- |
| `APPROVAL_STATUS` | `1` | 草稿 | APPROVAL_STATUS 的第 1 个枚举项 |
| `APPROVAL_STATUS` | `2` | 待审批 | APPROVAL_STATUS 的第 2 个枚举项 |
| `APPROVAL_STATUS` | `3` | 已批准 | APPROVAL_STATUS 的第 3 个枚举项 |
| `APPROVAL_STATUS` | `4` | 已驳回 | APPROVAL_STATUS 的第 4 个枚举项 |
| `COMMON_STATUS` | `1` | 启用 | COMMON_STATUS 的第 1 个枚举项 |
| `COMMON_STATUS` | `2` | 停用 | COMMON_STATUS 的第 2 个枚举项 |
| `CONSUME_STATUS` | `1` | 待消费 | CONSUME_STATUS 的第 1 个枚举项 |
| `CONSUME_STATUS` | `2` | 处理中 | CONSUME_STATUS 的第 2 个枚举项 |
| `CONSUME_STATUS` | `3` | 消费成功 | CONSUME_STATUS 的第 3 个枚举项 |
| `CONSUME_STATUS` | `4` | 消费失败 | CONSUME_STATUS 的第 4 个枚举项 |
| `CONSUME_STATUS` | `5` | 已忽略 | CONSUME_STATUS 的第 5 个枚举项 |
| `CONTAINER_BIND_TYPE` | `1` | 拣货单 | CONTAINER_BIND_TYPE 的第 1 个枚举项 |
| `CONTAINER_BIND_TYPE` | `2` | 出库单 | CONTAINER_BIND_TYPE 的第 2 个枚举项 |
| `CONTAINER_BIND_TYPE` | `3` | 包裹 | CONTAINER_BIND_TYPE 的第 3 个枚举项 |
| `CONTAINER_STATUS` | `1` | 空闲 | CONTAINER_STATUS 的第 1 个枚举项 |
| `CONTAINER_STATUS` | `2` | 已绑定 | CONTAINER_STATUS 的第 2 个枚举项 |
| `CONTAINER_STATUS` | `3` | 拣货中 | CONTAINER_STATUS 的第 3 个枚举项 |
| `CONTAINER_STATUS` | `4` | 待复核 | CONTAINER_STATUS 的第 4 个枚举项 |
| `CONTAINER_STATUS` | `5` | 已清空 | CONTAINER_STATUS 的第 5 个枚举项 |
| `CONTAINER_STATUS` | `6` | 停用 | CONTAINER_STATUS 的第 6 个枚举项 |
| `CONTAINER_TYPE` | `1` | 周转箱 | CONTAINER_TYPE 的第 1 个枚举项 |
| `CONTAINER_TYPE` | `2` | 播种车 | CONTAINER_TYPE 的第 2 个枚举项 |
| `CONTAINER_TYPE` | `3` | 格口 | CONTAINER_TYPE 的第 3 个枚举项 |
| `CONTAINER_TYPE` | `4` | 托盘 | CONTAINER_TYPE 的第 4 个枚举项 |
| `COUNT_PLAN_STATUS` | `1` | 草稿 | COUNT_PLAN_STATUS 的第 1 个枚举项 |
| `COUNT_PLAN_STATUS` | `2` | 已下发 | COUNT_PLAN_STATUS 的第 2 个枚举项 |
| `COUNT_PLAN_STATUS` | `3` | 盘点中 | COUNT_PLAN_STATUS 的第 3 个枚举项 |
| `COUNT_PLAN_STATUS` | `4` | 差异处理中 | COUNT_PLAN_STATUS 的第 4 个枚举项 |
| `COUNT_PLAN_STATUS` | `5` | 已完成 | COUNT_PLAN_STATUS 的第 5 个枚举项 |
| `COUNT_PLAN_STATUS` | `6` | 已取消 | COUNT_PLAN_STATUS 的第 6 个枚举项 |
| `COUNT_TYPE` | `1` | 全盘 | COUNT_TYPE 的第 1 个枚举项 |
| `COUNT_TYPE` | `2` | 动盘 | COUNT_TYPE 的第 2 个枚举项 |
| `COUNT_TYPE` | `3` | 循环盘 | COUNT_TYPE 的第 3 个枚举项 |
| `COUNT_TYPE` | `4` | 抽盘 | COUNT_TYPE 的第 4 个枚举项 |
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
| `EXCEPTION_STATUS` | `1` | 待处理 | EXCEPTION_STATUS 的第 1 个枚举项 |
| `EXCEPTION_STATUS` | `2` | 处理中 | EXCEPTION_STATUS 的第 2 个枚举项 |
| `EXCEPTION_STATUS` | `3` | 已关闭 | EXCEPTION_STATUS 的第 3 个枚举项 |
| `INBOUND_SOURCE_TYPE` | `1` | 采购 | INBOUND_SOURCE_TYPE 的第 1 个枚举项 |
| `INBOUND_SOURCE_TYPE` | `2` | 调拨 | INBOUND_SOURCE_TYPE 的第 2 个枚举项 |
| `INBOUND_SOURCE_TYPE` | `3` | 销售退货 | INBOUND_SOURCE_TYPE 的第 3 个枚举项 |
| `INBOUND_SOURCE_TYPE` | `4` | 其他 | INBOUND_SOURCE_TYPE 的第 4 个枚举项 |
| `INBOUND_STATUS` | `1` | 待到货 | INBOUND_STATUS 的第 1 个枚举项 |
| `INBOUND_STATUS` | `2` | 到货中 | INBOUND_STATUS 的第 2 个枚举项 |
| `INBOUND_STATUS` | `3` | 收货中 | INBOUND_STATUS 的第 3 个枚举项 |
| `INBOUND_STATUS` | `4` | 待质检 | INBOUND_STATUS 的第 4 个枚举项 |
| `INBOUND_STATUS` | `5` | 待上架 | INBOUND_STATUS 的第 5 个枚举项 |
| `INBOUND_STATUS` | `6` | 部分上架 | INBOUND_STATUS 的第 6 个枚举项 |
| `INBOUND_STATUS` | `7` | 已上架 | INBOUND_STATUS 的第 7 个枚举项 |
| `INBOUND_STATUS` | `8` | 已关闭 | INBOUND_STATUS 的第 8 个枚举项 |
| `INBOUND_STATUS` | `9` | 已取消 | INBOUND_STATUS 的第 9 个枚举项 |
| `OPERATION_RESULT` | `1` | 成功 | OPERATION_RESULT 的第 1 个枚举项 |
| `OPERATION_RESULT` | `2` | 失败 | OPERATION_RESULT 的第 2 个枚举项 |
| `OUTBOUND_SOURCE_TYPE` | `1` | 销售 | OUTBOUND_SOURCE_TYPE 的第 1 个枚举项 |
| `OUTBOUND_SOURCE_TYPE` | `2` | 调拨 | OUTBOUND_SOURCE_TYPE 的第 2 个枚举项 |
| `OUTBOUND_SOURCE_TYPE` | `3` | 退供 | OUTBOUND_SOURCE_TYPE 的第 3 个枚举项 |
| `OUTBOUND_SOURCE_TYPE` | `4` | 其他 | OUTBOUND_SOURCE_TYPE 的第 4 个枚举项 |
| `PACKAGE_STATUS` | `1` | 已打包 | PACKAGE_STATUS 的第 1 个枚举项 |
| `PACKAGE_STATUS` | `2` | 待交接 | PACKAGE_STATUS 的第 2 个枚举项 |
| `PACKAGE_STATUS` | `3` | 已交接 | PACKAGE_STATUS 的第 3 个枚举项 |
| `PACKAGE_STATUS` | `4` | 已取消 | PACKAGE_STATUS 的第 4 个枚举项 |
| `PACK_STATUS` | `1` | 待复核 | PACK_STATUS 的第 1 个枚举项 |
| `PACK_STATUS` | `2` | 复核中 | PACK_STATUS 的第 2 个枚举项 |
| `PACK_STATUS` | `3` | 复核异常 | PACK_STATUS 的第 3 个枚举项 |
| `PACK_STATUS` | `4` | 待打包 | PACK_STATUS 的第 4 个枚举项 |
| `PACK_STATUS` | `5` | 已打包 | PACK_STATUS 的第 5 个枚举项 |
| `PACK_STATUS` | `6` | 已关闭 | PACK_STATUS 的第 6 个枚举项 |
| `PICKING_MODE` | `1` | 单单拣 | PICKING_MODE 的第 1 个枚举项 |
| `PICKING_MODE` | `2` | 批量拣 | PICKING_MODE 的第 2 个枚举项 |
| `PICKING_MODE` | `3` | 边拣边分 | PICKING_MODE 的第 3 个枚举项 |
| `PICKING_STATUS` | `1` | 待分配 | PICKING_STATUS 的第 1 个枚举项 |
| `PICKING_STATUS` | `2` | 待拣货 | PICKING_STATUS 的第 2 个枚举项 |
| `PICKING_STATUS` | `3` | 拣货中 | PICKING_STATUS 的第 3 个枚举项 |
| `PICKING_STATUS` | `4` | 部分拣货 | PICKING_STATUS 的第 4 个枚举项 |
| `PICKING_STATUS` | `5` | 拣货异常 | PICKING_STATUS 的第 5 个枚举项 |
| `PICKING_STATUS` | `6` | 已拣货 | PICKING_STATUS 的第 6 个枚举项 |
| `PICKING_STATUS` | `7` | 已交接复核 | PICKING_STATUS 的第 7 个枚举项 |
| `PICKING_TASK_STATUS` | `1` | 待拣货 | PICKING_TASK_STATUS 的第 1 个枚举项 |
| `PICKING_TASK_STATUS` | `2` | 拣货中 | PICKING_TASK_STATUS 的第 2 个枚举项 |
| `PICKING_TASK_STATUS` | `3` | 已完成 | PICKING_TASK_STATUS 的第 3 个枚举项 |
| `PICKING_TASK_STATUS` | `4` | 异常 | PICKING_TASK_STATUS 的第 4 个枚举项 |
| `PUTAWAY_TASK_STATUS` | `1` | 待上架 | PUTAWAY_TASK_STATUS 的第 1 个枚举项 |
| `PUTAWAY_TASK_STATUS` | `2` | 上架中 | PUTAWAY_TASK_STATUS 的第 2 个枚举项 |
| `PUTAWAY_TASK_STATUS` | `3` | 部分上架 | PUTAWAY_TASK_STATUS 的第 3 个枚举项 |
| `PUTAWAY_TASK_STATUS` | `4` | 已完成 | PUTAWAY_TASK_STATUS 的第 4 个枚举项 |
| `PUTAWAY_TASK_STATUS` | `5` | 异常 | PUTAWAY_TASK_STATUS 的第 5 个枚举项 |
| `QC_RESULT` | `1` | 待检 | QC_RESULT 的第 1 个枚举项 |
| `QC_RESULT` | `2` | 合格 | QC_RESULT 的第 2 个枚举项 |
| `QC_RESULT` | `3` | 不合格 | QC_RESULT 的第 3 个枚举项 |
| `QC_RESULT` | `4` | 部分合格 | QC_RESULT 的第 4 个枚举项 |
| `QUALITY_STATUS` | `1` | 待检 | QUALITY_STATUS 的第 1 个枚举项 |
| `QUALITY_STATUS` | `2` | 合格 | QUALITY_STATUS 的第 2 个枚举项 |
| `QUALITY_STATUS` | `3` | 不合格 | QUALITY_STATUS 的第 3 个枚举项 |
| `QUALITY_STATUS` | `4` | 免检 | QUALITY_STATUS 的第 4 个枚举项 |
| `QUALITY_STATUS` | `5` | 待处理 | QUALITY_STATUS 的第 5 个枚举项 |
| `RECEIPT_STATUS` | `1` | 待收货 | RECEIPT_STATUS 的第 1 个枚举项 |
| `RECEIPT_STATUS` | `2` | 收货中 | RECEIPT_STATUS 的第 2 个枚举项 |
| `RECEIPT_STATUS` | `3` | 部分收货 | RECEIPT_STATUS 的第 3 个枚举项 |
| `RECEIPT_STATUS` | `4` | 已收货 | RECEIPT_STATUS 的第 4 个枚举项 |
| `RECEIPT_STATUS` | `5` | 异常 | RECEIPT_STATUS 的第 5 个枚举项 |
| `RECEIPT_STATUS` | `6` | 已关闭 | RECEIPT_STATUS 的第 6 个枚举项 |
| `RESPONSIBLE_PARTY` | `1` | 供应商 | RESPONSIBLE_PARTY 的第 1 个枚举项 |
| `RESPONSIBLE_PARTY` | `2` | 仓库 | RESPONSIBLE_PARTY 的第 2 个枚举项 |
| `RESPONSIBLE_PARTY` | `3` | 物流 | RESPONSIBLE_PARTY 的第 3 个枚举项 |
| `RESPONSIBLE_PARTY` | `4` | 客户 | RESPONSIBLE_PARTY 的第 4 个枚举项 |
| `RESPONSIBLE_PARTY` | `5` | 系统 | RESPONSIBLE_PARTY 的第 5 个枚举项 |
| `WAVE_STATUS` | `1` | 草稿 | WAVE_STATUS 的第 1 个枚举项 |
| `WAVE_STATUS` | `2` | 已释放 | WAVE_STATUS 的第 2 个枚举项 |
| `WAVE_STATUS` | `3` | 拣货中 | WAVE_STATUS 的第 3 个枚举项 |
| `WAVE_STATUS` | `4` | 已完成 | WAVE_STATUS 的第 4 个枚举项 |
| `WAVE_STATUS` | `5` | 已取消 | WAVE_STATUS 的第 5 个枚举项 |
| `WAVE_TYPE` | `1` | 单单拣 | WAVE_TYPE 的第 1 个枚举项 |
| `WAVE_TYPE` | `2` | 批量拣 | WAVE_TYPE 的第 2 个枚举项 |
| `WAVE_TYPE` | `3` | 边拣边分 | WAVE_TYPE 的第 3 个枚举项 |
| `WMS_EXCEPTION_SOURCE` | `1` | 收货 | WMS_EXCEPTION_SOURCE 的第 1 个枚举项 |
| `WMS_EXCEPTION_SOURCE` | `2` | 质检 | WMS_EXCEPTION_SOURCE 的第 2 个枚举项 |
| `WMS_EXCEPTION_SOURCE` | `3` | 上架 | WMS_EXCEPTION_SOURCE 的第 3 个枚举项 |
| `WMS_EXCEPTION_SOURCE` | `4` | 分配 | WMS_EXCEPTION_SOURCE 的第 4 个枚举项 |
| `WMS_EXCEPTION_SOURCE` | `5` | 拣货 | WMS_EXCEPTION_SOURCE 的第 5 个枚举项 |
| `WMS_EXCEPTION_SOURCE` | `6` | 复核 | WMS_EXCEPTION_SOURCE 的第 6 个枚举项 |
| `WMS_EXCEPTION_SOURCE` | `7` | 发货 | WMS_EXCEPTION_SOURCE 的第 7 个枚举项 |
| `WMS_EXCEPTION_SOURCE` | `8` | 盘点 | WMS_EXCEPTION_SOURCE 的第 8 个枚举项 |
| `WMS_EXCEPTION_TYPE` | `1` | 短收 | WMS_EXCEPTION_TYPE 的第 1 个枚举项 |
| `WMS_EXCEPTION_TYPE` | `2` | 超收 | WMS_EXCEPTION_TYPE 的第 2 个枚举项 |
| `WMS_EXCEPTION_TYPE` | `3` | 错货 | WMS_EXCEPTION_TYPE 的第 3 个枚举项 |
| `WMS_EXCEPTION_TYPE` | `4` | 破损 | WMS_EXCEPTION_TYPE 的第 4 个枚举项 |
| `WMS_EXCEPTION_TYPE` | `5` | 库位不符 | WMS_EXCEPTION_TYPE 的第 5 个枚举项 |
| `WMS_EXCEPTION_TYPE` | `6` | 复核差异 | WMS_EXCEPTION_TYPE 的第 6 个枚举项 |
| `WMS_EXCEPTION_TYPE` | `7` | 盘点差异 | WMS_EXCEPTION_TYPE 的第 7 个枚举项 |
| `WMS_OUTBOUND_LINE_STATUS` | `1` | 待分配 | WMS_OUTBOUND_LINE_STATUS 的第 1 个枚举项 |
| `WMS_OUTBOUND_LINE_STATUS` | `2` | 已分配 | WMS_OUTBOUND_LINE_STATUS 的第 2 个枚举项 |
| `WMS_OUTBOUND_LINE_STATUS` | `3` | 拣货中 | WMS_OUTBOUND_LINE_STATUS 的第 3 个枚举项 |
| `WMS_OUTBOUND_LINE_STATUS` | `4` | 已拣货 | WMS_OUTBOUND_LINE_STATUS 的第 4 个枚举项 |
| `WMS_OUTBOUND_LINE_STATUS` | `5` | 已发货 | WMS_OUTBOUND_LINE_STATUS 的第 5 个枚举项 |
| `WMS_OUTBOUND_LINE_STATUS` | `6` | 已取消 | WMS_OUTBOUND_LINE_STATUS 的第 6 个枚举项 |
| `WMS_OUTBOUND_STATUS` | `1` | 待接单 | WMS_OUTBOUND_STATUS 的第 1 个枚举项 |
| `WMS_OUTBOUND_STATUS` | `2` | 待分配 | WMS_OUTBOUND_STATUS 的第 2 个枚举项 |
| `WMS_OUTBOUND_STATUS` | `3` | 分配失败 | WMS_OUTBOUND_STATUS 的第 3 个枚举项 |
| `WMS_OUTBOUND_STATUS` | `4` | 待拣货 | WMS_OUTBOUND_STATUS 的第 4 个枚举项 |
| `WMS_OUTBOUND_STATUS` | `5` | 拣货中 | WMS_OUTBOUND_STATUS 的第 5 个枚举项 |
| `WMS_OUTBOUND_STATUS` | `6` | 待复核 | WMS_OUTBOUND_STATUS 的第 6 个枚举项 |
| `WMS_OUTBOUND_STATUS` | `7` | 复核异常 | WMS_OUTBOUND_STATUS 的第 7 个枚举项 |
| `WMS_OUTBOUND_STATUS` | `8` | 待发货 | WMS_OUTBOUND_STATUS 的第 8 个枚举项 |
| `WMS_OUTBOUND_STATUS` | `9` | 已发货 | WMS_OUTBOUND_STATUS 的第 9 个枚举项 |
| `WMS_OUTBOUND_STATUS` | `10` | 已关闭 | WMS_OUTBOUND_STATUS 的第 10 个枚举项 |
| `WMS_OUTBOUND_STATUS` | `11` | 已取消 | WMS_OUTBOUND_STATUS 的第 11 个枚举项 |
| `WMS_STOCK_STATUS` | `1` | 可拣 | WMS_STOCK_STATUS 的第 1 个枚举项 |
| `WMS_STOCK_STATUS` | `2` | 冻结 | WMS_STOCK_STATUS 的第 2 个枚举项 |
| `WMS_STOCK_STATUS` | `3` | 不合格 | WMS_STOCK_STATUS 的第 3 个枚举项 |
| `WMS_STOCK_STATUS` | `4` | 待退供 | WMS_STOCK_STATUS 的第 4 个枚举项 |
| `WMS_STOCK_STATUS` | `5` | 待报废 | WMS_STOCK_STATUS 的第 5 个枚举项 |

## 6. 事件、事件表与审计表字段

事件采用本地消息表模式：应用服务在同一事务内保存业务表和领域事件表，异步发布后更新事件状态；消费外部事件时先写消费日志，使用幂等键避免重复处理。

### 6.1 本系统领域事件

| 事件 | 来源聚合 | 触发动作 | 主要载荷 | 订阅/用途 |
| --- | --- | --- | --- | --- |
| 入库单已接收 | 02-入库单聚合CQRS设计 | 接收入库单 | 入库单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 到货已登记 | 02-入库单聚合CQRS设计 | 登记到货 | 入库单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 入库单已开始收货 | 02-入库单聚合CQRS设计 | 开始收货 | 入库单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 入库单已取消 | 02-入库单聚合CQRS设计 | 取消入库单 | 入库单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 入库单已关闭 | 02-入库单聚合CQRS设计 | 关闭入库单 | 入库单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| ASN已提交 | 02-入库单聚合CQRS设计 | 外部事件消费服务 | 创建或更新入库单来源快照 | 来源上下文+事件编号+业务主键 |
| SKU已停用 | 02-入库单聚合CQRS设计 | 主数据事件消费服务 | 标记相关作业明细不可继续执行并生成异常 | 主数据上下文+事件编号+skuId |
| 库位已更新 | 02-入库单聚合CQRS设计 | 主数据事件消费服务 | 刷新库位用途、容量、启停用和质量状态限制 | 主数据上下文+事件编号+locationId |
| 仓库已停用 | 02-入库单聚合CQRS设计 | 主数据事件消费服务 | 阻断新作业接单并保留已开始作业处理入口 | 主数据上下文+事件编号+warehouseId |
| 收货已开始 | 03-收货单聚合CQRS设计 | 开始收货 | 收货单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 收货明细已扫描 | 03-收货单聚合CQRS设计 | 扫描收货 | 收货单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 收货差异已记录 | 03-收货单聚合CQRS设计 | 登记收货差异 | 收货单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 收货已完成 | 03-收货单聚合CQRS设计 | 提交收货 | 收货单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 收货单已关闭 | 03-收货单聚合CQRS设计 | 关闭收货 | 收货单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 质检单已创建 | 04-质检单聚合CQRS设计 | 创建质检单 | 质检单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 质检已开始 | 04-质检单聚合CQRS设计 | 开始质检 | 质检单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 质检已完成 | 04-质检单聚合CQRS设计 | 提交质检结果 | 质检单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 不合格品已判定 | 04-质检单聚合CQRS设计 | 冻结不合格品 | 质检单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 质检单已关闭 | 04-质检单聚合CQRS设计 | 关闭质检单 | 质检单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 上架任务已创建 | 05-上架任务聚合CQRS设计 | 生成上架任务 | 上架任务ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 上架任务已领取 | 05-上架任务聚合CQRS设计 | 领取上架任务 | 上架任务ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 上架已完成 | 05-上架任务聚合CQRS设计 | 确认上架 | 上架任务ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 不合格品已暂存 | 05-上架任务聚合CQRS设计 | 暂存不合格品 | 上架任务ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 上架异常已创建 | 05-上架任务聚合CQRS设计 | 标记上架异常 | 上架任务ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 库内库存已增加 | 06-库内库存聚合CQRS设计 | 入库上账 | 库内库存ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 库内库存已移动 | 06-库内库存聚合CQRS设计 | 库内移动 | 库内库存ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 库内库存已冻结 | 06-库内库存聚合CQRS设计 | 库存冻结 | 库内库存ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 库内库存已解冻 | 06-库内库存聚合CQRS设计 | 库存解冻 | 库内库存ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 库内库存已调整 | 06-库内库存聚合CQRS设计 | 盘点调整 | 库内库存ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 出库单已接收 | 07-出库单聚合CQRS设计 | 接收出库单 | 出库单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 出库已分配 | 07-出库单聚合CQRS设计 | 分配库位 | 出库单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 出库已发货 | 07-出库单聚合CQRS设计 | 确认发货 | 出库单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 出库单已取消 | 07-出库单聚合CQRS设计 | 取消出库单 | 出库单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 库存已预占 | 07-出库单聚合CQRS设计 | 外部事件消费服务 | 记录预占结果并允许分配库位 | 来源上下文+事件编号+业务主键 |
| 波次已创建 | 08-波次单聚合CQRS设计 | 生成波次 | 波次单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 波次已释放 | 08-波次单聚合CQRS设计 | 释放波次 | 波次单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 波次拣货单已生成 | 08-波次单聚合CQRS设计 | 生成拣货单 | 波次单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 波次已完成 | 08-波次单聚合CQRS设计 | 完成波次 | 波次单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 波次已取消 | 08-波次单聚合CQRS设计 | 取消波次 | 波次单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 拣货单已创建 | 09-拣货单聚合CQRS设计 | 生成拣货单 | 拣货单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 拣货单已领取 | 09-拣货单聚合CQRS设计 | 领取拣货 | 拣货单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 拣货任务已完成 | 09-拣货单聚合CQRS设计 | 确认拣货任务 | 拣货单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 拣货异常已创建 | 09-拣货单聚合CQRS设计 | 登记拣货异常 | 拣货单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 拣货单已交接复核 | 09-拣货单聚合CQRS设计 | 交接复核 | 拣货单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 容器已绑定 | 10-周转容器聚合CQRS设计 | 绑定容器 | 周转容器ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 容器已装货 | 10-周转容器聚合CQRS设计 | 容器装货 | 周转容器ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 容器已交接复核 | 10-周转容器聚合CQRS设计 | 交接复核 | 周转容器ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 容器已清空 | 10-周转容器聚合CQRS设计 | 清空容器 | 周转容器ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 容器已停用 | 10-周转容器聚合CQRS设计 | 停用容器 | 周转容器ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 包装单已创建 | 11-复核包装单聚合CQRS设计 | 创建复核包装单 | 复核包装单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 复核已完成 | 11-复核包装单聚合CQRS设计 | 复核商品 | 复核包装单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 复核异常已创建 | 11-复核包装单聚合CQRS设计 | 登记复核差异 | 复核包装单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 包裹已创建 | 11-复核包装单聚合CQRS设计 | 打包称重 | 复核包装单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 包装已完成 | 11-复核包装单聚合CQRS设计 | 确认包装完成 | 复核包装单ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 发货交接已创建 | 12-发货交接聚合CQRS设计 | 创建发货交接 | 发货交接ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 包裹已扫描交接 | 12-发货交接聚合CQRS设计 | 扫描包裹 | 发货交接ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 发货异常已创建 | 12-发货交接聚合CQRS设计 | 登记交接异常 | 发货交接ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 发货交接已关闭 | 12-发货交接聚合CQRS设计 | 关闭交接 | 发货交接ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 盘点计划已创建 | 13-盘点计划聚合CQRS设计 | 创建盘点计划 | 盘点计划ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 盘点计划已下发 | 13-盘点计划聚合CQRS设计 | 下发盘点任务 | 盘点计划ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 盘点任务已完成 | 13-盘点计划聚合CQRS设计 | 提交实盘 | 盘点计划ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 盘点差异已创建 | 13-盘点计划聚合CQRS设计 | 确认盘点差异 | 盘点计划ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 盘点计划已完成 | 13-盘点计划聚合CQRS设计 | 完成盘点计划 | 盘点计划ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 仓内异常已创建 | 14-仓内异常聚合CQRS设计 | 创建异常 | 仓内异常ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 仓内异常已分派 | 14-仓内异常聚合CQRS设计 | 分派异常 | 仓内异常ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 仓内异常已处理 | 14-仓内异常聚合CQRS设计 | 处理异常 | 仓内异常ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 仓内异常已关闭 | 14-仓内异常聚合CQRS设计 | 关闭异常 | 仓内异常ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |
| 仓内异常已升级 | 14-仓内异常聚合CQRS设计 | 升级异常 | 仓内异常ID、仓库、SKU、批次、库位、数量、状态 | 中央库存、采购、OMS、BMS、读模型、审计日志 |

### 6.2 `wms_domain_event`

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

### 6.3 `wms_event_consume_log`

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

### 6.4 `wms_operation_audit_log`

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

- [`ddl/03-WMS系统.sql`](ddl/03-WMS系统.sql)

DDL 包含业务表、枚举类型表、枚举项初始化数据、领域事件发布表、事件消费日志表和操作日志表。

当前结论：数据库表围绕聚合当前状态、内部实体明细、事件日志和读模型支撑设计；枚举字段使用数值编码落库，通过枚举配置表维护显示名和启停状态。
