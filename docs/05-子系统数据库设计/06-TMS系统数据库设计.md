# 06-TMS系统数据库设计

> 本文根据 `docs/03-核心业务模型/06-TMS领域模型` 的聚合、不变量、命令事件，以及 `docs/04-子系统功能设计/06-TMS系统/01-TMS系统产品功能设计.md` 的页面、状态、权限和操作场景整理。本文是 TMS 数据库表字段设计入口；领域模型和功能设计文档不维护字段清单。

## 1. 设计口径

| 项目 | 口径 |
| --- | --- |
| 所属系统 | TMS系统 |
| 表名前缀 | `tms_` |
| 主键策略 | 业务表使用 bigint 主键，建议雪花 ID；业务单号另设唯一索引 |
| 时间类型 | 业务时间使用 `datetime`，日期使用 `date` |
| 数量类型 | 件数使用 `int`，重量、体积、数量使用 `decimal(18,4)` |
| 金额类型 | 金额使用 `decimal(18,2)`，费率使用 `decimal(18,6)` |
| 状态字段 | 状态机字段使用 `smallint` 数值编码，只能由聚合命令推进 |
| 枚举字段 | 核心枚举值固定为 `1,2,3...`；页面可配置显示名、排序、颜色和启停用 |
| 事件落库 | 使用本地消息表模式，同一事务保存业务表和领域事件表 |

## 2. 通用字段

| 字段 | 类型 | 是否必填 | 约束 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | bigint | 是 | 主键，自增或雪花ID | 技术主键，实际表按业务对象命名 |
| `tenant_id` | bigint | 否 | 多租户时启用 | 租户ID |
| `org_id` | bigint | 否 | 按组织隔离时启用 | 组织ID |
| `owner_id` | bigint | 否 | 多货主时启用 | 货主ID |
| `warehouse_id` | bigint | 否 | 仓库相关单据必填 | 仓库ID |
| `created_by` | bigint | 是 | 写入时填充 | 创建人 |
| `created_at` | datetime | 是 | 默认当前时间 | 创建时间 |
| `updated_by` | bigint | 否 | 更新时填充 | 更新人 |
| `updated_at` | datetime | 是 | 默认当前时间并自动更新 | 更新时间 |
| `version` | int | 是 | 默认 0，乐观锁 | 数据版本 |
| `deleted` | tinyint | 是 | 0 否，1 是 | 逻辑删除标记 |

## 3. 表清单

| 表名 | 业务对象 | 来源聚合/模型 | 说明 |
| --- | --- | --- | --- |
| `tms_transport_task` | 运输任务 | 运输任务聚合 | 来源系统提出的运输需求，是 TMS 执行入口 |
| `tms_waybill` | 运单 | 运单聚合 | 承运商运输事实主表，记录运输状态和来源单据关系 |
| `tms_waybill_package` | 运单包裹 | 运单聚合内部实体 | 运单下的包裹、重量体积、面单和交接信息 |
| `tms_shipping_label` | 面单 | 面单模型 | 电子面单生成、打印、补打、作废记录 |
| `tms_tracking_event` | 物流轨迹 | 轨迹模型 | 承运商回调、主动同步和人工补录的轨迹明细 |
| `tms_delivery_receipt` | 签收回单 | 签收模型 | 签收、拒收、部分签收、签收证明和修正记录 |
| `tms_logistics_exception` | 物流异常 | 物流异常聚合 | 延误、破损、丢失、拒收、接口失败等异常处理 |
| `tms_fee_source` | 物流费用来源 | 费用来源模型 | 生成给 BMS 的物流费用来源，不做最终结算 |
| `tms_carrier_integration_log` | 承运商接口日志 | 基础设施读写日志 | 记录下单、取消、轨迹同步、面单、回调等接口请求响应 |
| `tms_domain_event` | 领域事件发布表 | 本地消息表 | TMS 对外发布领域事件 |
| `tms_event_consume_log` | 事件消费日志 | 消费幂等日志 | TMS 消费外部事件的幂等和重试记录 |
| `tms_operation_audit_log` | 操作审计日志 | 审计日志 | 用户写操作、敏感查询和补偿操作记录 |
| `tms_enum_type` | 枚举类型 | 枚举配置 | 枚举类型定义 |
| `tms_enum_item` | 枚举项 | 枚举配置 | 枚举值、显示名、排序、颜色和启停用 |

## 4. 表字段设计

### 4.1 `tms_transport_task`

业务对象：运输任务。

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `transport_task_id` | bigint | 是 | 主键 | 技术主键 | 运输任务ID |
| `transport_task_no` | varchar(64) | 是 | 唯一 | 标识 | 运输任务号 |
| `source_system` | smallint | 是 | `TMS_SOURCE_SYSTEM` | 来源 | OMS、WMS、采购、供应商、库存、人工 |
| `source_order_no` | varchar(64) | 是 | 幂等组合 | 引用 | 来源单号 |
| `source_order_type` | smallint | 是 | `TMS_SOURCE_ORDER_TYPE` | 分类 | 销售出库、销售退货、采购到货、退供应商、调拨、其它 |
| `transport_scenario` | smallint | 是 | `TRANSPORT_SCENARIO` | 分类 | 采购到货、销售发货、销售退货、退供应商、调拨 |
| `task_status` | smallint | 是 | `TRANSPORT_TASK_STATUS` | 状态 | 待接单、已接单、已创建运单、运输中、已签收、已拒收、已取消、异常中 |
| `carrier_id` | bigint | 否 | 主数据引用 | 引用 | 物流商ID |
| `carrier_code` | varchar(64) | 否 | 快照 | 引用 | 物流商编码 |
| `logistics_product_code` | varchar(64) | 否 | 快照 | 引用 | 物流产品编码 |
| `shipper_name` | varchar(128) | 是 |  | 地址值对象 | 发货方名称 |
| `shipper_phone` | varchar(64) | 否 |  | 地址值对象 | 发货方电话 |
| `shipper_address` | varchar(512) | 是 |  | 地址值对象 | 发货地址 |
| `receiver_name` | varchar(128) | 是 |  | 地址值对象 | 收货方名称 |
| `receiver_phone` | varchar(64) | 否 |  | 地址值对象 | 收货方电话 |
| `receiver_address` | varchar(512) | 是 |  | 地址值对象 | 收货地址 |
| `package_count` | int | 是 | >= 0 | 数量 | 包裹数 |
| `total_weight` | decimal(18,4) | 否 | >= 0 | 数量 | 总重量 |
| `total_volume` | decimal(18,4) | 否 | >= 0 | 数量 | 总体积 |
| `expected_ship_at` | datetime | 否 |  | 时间 | 预计发货时间 |
| `expected_arrive_at` | datetime | 否 |  | 时间 | 预计到达时间 |
| `accepted_at` | datetime | 否 |  | 时间 | 接单时间 |
| `cancel_reason` | varchar(512) | 否 |  | 业务属性 | 取消原因 |
| `remark` | varchar(512) | 否 |  | 业务属性 | 备注 |
| `tenant_id` | bigint | 否 |  | 数据权限 | 租户ID |
| `org_id` | bigint | 否 |  | 数据权限 | 组织ID |
| `owner_id` | bigint | 否 |  | 数据权限 | 货主ID |
| `warehouse_id` | bigint | 否 |  | 数据权限 | 仓库ID |
| `created_by` | bigint | 是 |  | 审计 | 创建人 |
| `created_at` | datetime | 是 |  | 审计 | 创建时间 |
| `updated_by` | bigint | 否 |  | 审计 | 更新人 |
| `updated_at` | datetime | 是 |  | 审计 | 更新时间 |
| `version` | int | 是 | 默认0 | 并发 | 乐观锁 |
| `deleted` | tinyint | 是 | 0/1 | 治理 | 逻辑删除 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_tms_transport_task_no` | `transport_task_no` | 运输任务号唯一 |
| `uk_tms_transport_task_source` | `source_system`,`source_order_no`,`transport_scenario` | 来源幂等 |
| `idx_tms_transport_task_status_time` | `task_status`,`updated_at` | 待办和列表 |
| `idx_tms_transport_task_scope` | `org_id`,`warehouse_id`,`owner_id` | 数据权限过滤 |

### 4.2 `tms_waybill`

业务对象：运单。

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `waybill_id` | bigint | 是 | 主键 | 技术主键 | 运单ID |
| `waybill_no` | varchar(64) | 是 | 唯一 | 标识 | TMS运单号 |
| `transport_task_id` | bigint | 否 | 引用 | 引用 | 运输任务ID |
| `transport_task_no` | varchar(64) | 否 | 快照 | 引用 | 运输任务号 |
| `carrier_id` | bigint | 是 | 主数据引用 | 引用 | 物流商ID |
| `carrier_code` | varchar(64) | 是 | 快照 | 引用 | 物流商编码 |
| `carrier_waybill_no` | varchar(128) | 否 | 承运商返回 | 标识 | 承运商单号 |
| `logistics_product_code` | varchar(64) | 否 | 快照 | 引用 | 物流产品 |
| `source_system` | smallint | 是 | `TMS_SOURCE_SYSTEM` | 来源 | 来源系统 |
| `source_order_no` | varchar(64) | 是 |  | 引用 | 来源单号 |
| `transport_scenario` | smallint | 是 | `TRANSPORT_SCENARIO` | 分类 | 运输场景 |
| `waybill_status` | smallint | 是 | `WAYBILL_STATUS` | 状态 | 待下单、已下单、已揽收、运输中、已到达、已签收、已拒收、已取消、异常中 |
| `latest_track_desc` | varchar(512) | 否 |  | 读模型 | 最新轨迹描述 |
| `latest_track_at` | datetime | 否 |  | 时间 | 最新轨迹时间 |
| `shipper_address` | varchar(512) | 是 |  | 地址值对象 | 发货地址快照 |
| `receiver_address` | varchar(512) | 是 |  | 地址值对象 | 收货地址快照 |
| `package_count` | int | 是 | >= 0 | 数量 | 包裹数 |
| `charge_weight` | decimal(18,4) | 否 | >= 0 | 费用 | 计费重量 |
| `total_weight` | decimal(18,4) | 否 | >= 0 | 数量 | 总重量 |
| `total_volume` | decimal(18,4) | 否 | >= 0 | 数量 | 总体积 |
| `ordered_at` | datetime | 否 |  | 时间 | 承运商下单时间 |
| `pickup_at` | datetime | 否 |  | 时间 | 揽收时间 |
| `shipped_at` | datetime | 否 |  | 时间 | 发货时间 |
| `arrived_at` | datetime | 否 |  | 时间 | 到达时间 |
| `signed_at` | datetime | 否 |  | 时间 | 签收时间 |
| `cancelled_at` | datetime | 否 |  | 时间 | 取消时间 |
| `cancel_reason` | varchar(512) | 否 |  | 业务属性 | 取消原因 |
| `exception_flag` | tinyint | 是 | 0/1 | 状态 | 是否存在未关闭异常 |
| `fee_source_status` | smallint | 是 | `FEE_SOURCE_STATUS` | 状态 | 费用来源状态 |
| `tenant_id` | bigint | 否 |  | 数据权限 | 租户ID |
| `org_id` | bigint | 否 |  | 数据权限 | 组织ID |
| `owner_id` | bigint | 否 |  | 数据权限 | 货主ID |
| `warehouse_id` | bigint | 否 |  | 数据权限 | 仓库ID |
| `created_by` | bigint | 是 |  | 审计 | 创建人 |
| `created_at` | datetime | 是 |  | 审计 | 创建时间 |
| `updated_by` | bigint | 否 |  | 审计 | 更新人 |
| `updated_at` | datetime | 是 |  | 审计 | 更新时间 |
| `version` | int | 是 | 默认0 | 并发 | 乐观锁 |
| `deleted` | tinyint | 是 | 0/1 | 治理 | 逻辑删除 |

建议索引：

| 索引 | 字段 | 说明 |
| --- | --- | --- |
| `uk_tms_waybill_no` | `waybill_no` | TMS运单号唯一 |
| `uk_tms_waybill_carrier_no` | `carrier_code`,`carrier_waybill_no` | 承运商单号唯一，允许未返回前为空 |
| `idx_tms_waybill_source` | `source_system`,`source_order_no` | 来源追踪 |
| `idx_tms_waybill_status_time` | `waybill_status`,`updated_at` | 列表和待办 |

### 4.3 `tms_waybill_package`

业务对象：运单包裹。

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `package_id` | bigint | 是 | 主键 | 技术主键 | 包裹ID |
| `package_no` | varchar(64) | 是 | 唯一 | 标识 | TMS包裹号 |
| `waybill_id` | bigint | 是 | 引用 | 聚合内部实体 | 运单ID |
| `waybill_no` | varchar(64) | 是 | 快照 | 引用 | 运单号 |
| `external_package_no` | varchar(128) | 否 | 来源快照 | 引用 | WMS/来源系统包裹号 |
| `package_status` | smallint | 是 | `PACKAGE_STATUS` | 状态 | 待面单、已生成面单、已打印、已交接、已取消 |
| `weight` | decimal(18,4) | 否 | >= 0 | 数量 | 实重 |
| `volume` | decimal(18,4) | 否 | >= 0 | 数量 | 体积 |
| `length_cm` | decimal(18,4) | 否 | >= 0 | 数量 | 长 |
| `width_cm` | decimal(18,4) | 否 | >= 0 | 数量 | 宽 |
| `height_cm` | decimal(18,4) | 否 | >= 0 | 数量 | 高 |
| `label_id` | bigint | 否 | 引用 | 引用 | 面单ID |
| `label_no` | varchar(64) | 否 | 快照 | 引用 | 面单号 |
| `handover_batch_no` | varchar(64) | 否 |  | 引用 | 交接批次 |
| `handover_at` | datetime | 否 |  | 时间 | 交接时间 |
| `created_by` | bigint | 是 |  | 审计 | 创建人 |
| `created_at` | datetime | 是 |  | 审计 | 创建时间 |
| `updated_by` | bigint | 否 |  | 审计 | 更新人 |
| `updated_at` | datetime | 是 |  | 审计 | 更新时间 |
| `version` | int | 是 | 默认0 | 并发 | 乐观锁 |
| `deleted` | tinyint | 是 | 0/1 | 治理 | 逻辑删除 |

### 4.4 `tms_shipping_label`

业务对象：面单。

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `label_id` | bigint | 是 | 主键 | 技术主键 | 面单ID |
| `label_no` | varchar(64) | 是 | 唯一 | 标识 | TMS面单号 |
| `waybill_id` | bigint | 是 | 引用 | 引用 | 运单ID |
| `waybill_no` | varchar(64) | 是 | 快照 | 引用 | 运单号 |
| `package_no` | varchar(64) | 否 | 快照 | 引用 | 包裹号 |
| `carrier_code` | varchar(64) | 是 | 快照 | 引用 | 物流商编码 |
| `carrier_waybill_no` | varchar(128) | 否 |  | 标识 | 承运商单号 |
| `label_status` | smallint | 是 | `LABEL_STATUS` | 状态 | 待生成、已生成、已打印、已作废、生成失败 |
| `label_url` | varchar(512) | 否 |  | 业务属性 | 面单文件地址 |
| `label_data_json` | json | 否 |  | 业务属性 | 面单原始数据 |
| `print_count` | int | 是 | 默认0 | 数量 | 打印次数 |
| `last_printed_by` | bigint | 否 |  | 审计 | 最后打印人 |
| `last_printed_at` | datetime | 否 |  | 时间 | 最后打印时间 |
| `void_reason` | varchar(512) | 否 |  | 业务属性 | 作废原因 |
| `created_by` | bigint | 是 |  | 审计 | 创建人 |
| `created_at` | datetime | 是 |  | 审计 | 创建时间 |
| `updated_by` | bigint | 否 |  | 审计 | 更新人 |
| `updated_at` | datetime | 是 |  | 审计 | 更新时间 |
| `version` | int | 是 | 默认0 | 并发 | 乐观锁 |
| `deleted` | tinyint | 是 | 0/1 | 治理 | 逻辑删除 |

### 4.5 `tms_tracking_event`

业务对象：物流轨迹。

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `tracking_event_id` | bigint | 是 | 主键 | 技术主键 | 轨迹ID |
| `waybill_id` | bigint | 是 | 引用 | 引用 | 运单ID |
| `waybill_no` | varchar(64) | 是 | 快照 | 引用 | 运单号 |
| `carrier_code` | varchar(64) | 是 | 快照 | 引用 | 物流商编码 |
| `carrier_waybill_no` | varchar(128) | 否 |  | 引用 | 承运商单号 |
| `track_node` | smallint | 是 | `TRACK_NODE` | 分类 | 下单、揽收、发出、到达、派送、签收、拒收、异常 |
| `track_desc` | varchar(512) | 是 |  | 业务属性 | 轨迹描述 |
| `track_location` | varchar(256) | 否 |  | 地址值对象 | 轨迹地点 |
| `track_at` | datetime | 是 | 幂等组合 | 时间 | 轨迹发生时间 |
| `sync_source` | smallint | 是 | `TRACK_SYNC_SOURCE` | 分类 | 承运商回调、主动拉取、人工补录 |
| `raw_payload_json` | json | 否 |  | 技术字段 | 原始报文 |
| `idempotent_key` | varchar(256) | 是 | 唯一 | 幂等 | 承运商+单号+节点+时间 |
| `created_by` | bigint | 是 |  | 审计 | 创建人 |
| `created_at` | datetime | 是 |  | 审计 | 创建时间 |

### 4.6 `tms_delivery_receipt`

业务对象：签收回单。

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `receipt_id` | bigint | 是 | 主键 | 技术主键 | 签收回单ID |
| `receipt_no` | varchar(64) | 是 | 唯一 | 标识 | 签收回单号 |
| `waybill_id` | bigint | 是 | 引用 | 引用 | 运单ID |
| `waybill_no` | varchar(64) | 是 | 快照 | 引用 | 运单号 |
| `receipt_result` | smallint | 是 | `RECEIPT_RESULT` | 状态 | 已签收、已拒收、部分签收、签收冲突 |
| `signed_by` | varchar(128) | 否 |  | 业务属性 | 签收人 |
| `signed_at` | datetime | 否 |  | 时间 | 签收时间 |
| `reject_reason` | varchar(512) | 否 |  | 业务属性 | 拒收原因 |
| `proof_url` | varchar(512) | 否 |  | 业务属性 | 签收证明 |
| `corrected_flag` | tinyint | 是 | 0/1 | 审计 | 是否修正过 |
| `correct_reason` | varchar(512) | 否 |  | 审计 | 修正原因 |
| `notified_source_status` | smallint | 是 | `NOTIFY_STATUS` | 状态 | 待通知、已通知、通知失败 |
| `created_by` | bigint | 是 |  | 审计 | 创建人 |
| `created_at` | datetime | 是 |  | 审计 | 创建时间 |
| `updated_by` | bigint | 否 |  | 审计 | 更新人 |
| `updated_at` | datetime | 是 |  | 审计 | 更新时间 |
| `version` | int | 是 | 默认0 | 并发 | 乐观锁 |
| `deleted` | tinyint | 是 | 0/1 | 治理 | 逻辑删除 |

### 4.7 `tms_logistics_exception`

业务对象：物流异常。

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `exception_id` | bigint | 是 | 主键 | 技术主键 | 异常ID |
| `exception_no` | varchar(64) | 是 | 唯一 | 标识 | 异常单号 |
| `waybill_id` | bigint | 是 | 引用 | 引用 | 运单ID |
| `waybill_no` | varchar(64) | 是 | 快照 | 引用 | 运单号 |
| `source_order_no` | varchar(64) | 否 | 快照 | 引用 | 来源单号 |
| `exception_type` | smallint | 是 | `LOGISTICS_EXCEPTION_TYPE` | 分类 | 延误、破损、丢失、拒收、接口失败、费用异常 |
| `exception_level` | smallint | 是 | `EXCEPTION_LEVEL` | 分类 | 一般、严重、紧急 |
| `responsible_party` | smallint | 否 | `RESPONSIBLE_PARTY` | 分类 | 物流商、仓库、客户、供应商、系统 |
| `exception_status` | smallint | 是 | `EXCEPTION_STATUS` | 状态 | 已创建、处理中、已升级、已关闭 |
| `description` | varchar(1024) | 否 |  | 业务属性 | 异常说明 |
| `handler_id` | bigint | 否 |  | 业务属性 | 当前处理人 |
| `handle_result` | varchar(1024) | 否 |  | 业务属性 | 处理结果 |
| `fee_impact_flag` | tinyint | 是 | 0/1 | 费用 | 是否影响费用 |
| `closed_at` | datetime | 否 |  | 时间 | 关闭时间 |
| `created_by` | bigint | 是 |  | 审计 | 创建人 |
| `created_at` | datetime | 是 |  | 审计 | 创建时间 |
| `updated_by` | bigint | 否 |  | 审计 | 更新人 |
| `updated_at` | datetime | 是 |  | 审计 | 更新时间 |
| `version` | int | 是 | 默认0 | 并发 | 乐观锁 |
| `deleted` | tinyint | 是 | 0/1 | 治理 | 逻辑删除 |

### 4.8 `tms_fee_source`

业务对象：物流费用来源。

| 字段 | 类型 | 是否必填 | 约束/枚举 | 领域归类 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `fee_source_id` | bigint | 是 | 主键 | 技术主键 | 费用来源ID |
| `fee_source_no` | varchar(64) | 是 | 唯一 | 标识 | 费用来源号 |
| `waybill_id` | bigint | 是 | 引用 | 引用 | 运单ID |
| `waybill_no` | varchar(64) | 是 | 快照 | 引用 | 运单号 |
| `carrier_id` | bigint | 是 | 主数据引用 | 引用 | 物流商ID |
| `carrier_code` | varchar(64) | 是 | 快照 | 引用 | 物流商编码 |
| `source_order_no` | varchar(64) | 否 | 快照 | 引用 | 来源单号 |
| `transport_scenario` | smallint | 是 | `TRANSPORT_SCENARIO` | 分类 | 运输场景 |
| `fee_source_status` | smallint | 是 | `FEE_SOURCE_STATUS` | 状态 | 待生成、已生成、已推送、推送失败、已作废、已采集、对账差异、已修正 |
| `settlement_direction` | smallint | 是 | `SETTLEMENT_DIRECTION` | 分类 | 应付、应收 |
| `charge_weight` | decimal(18,4) | 否 | >=0 | 费用 | 计费重量 |
| `base_fee` | decimal(18,2) | 是 | 默认0 | 金额 | 基础运费 |
| `extra_fee` | decimal(18,2) | 是 | 默认0 | 金额 | 附加费 |
| `deduction_fee` | decimal(18,2) | 是 | 默认0 | 金额 | 扣罚金额 |
| `total_amount` | decimal(18,2) | 是 |  | 金额 | 总金额 |
| `currency` | varchar(16) | 是 | 默认 CNY | 金额 | 币种 |
| `fee_items_json` | json | 否 |  | 费用 | 费用项明细 |
| `generated_at` | datetime | 否 |  | 时间 | 生成时间 |
| `pushed_at` | datetime | 否 |  | 时间 | 推送BMS时间 |
| `bms_receive_no` | varchar(64) | 否 |  | 引用 | BMS接收编号 |
| `bms_collected_at` | datetime | 否 |  | 时间 | BMS采集时间 |
| `bms_billing_item_no` | varchar(64) | 否 |  | 引用 | BMS费用明细号 |
| `reconciliation_status` | smallint | 否 | `RECONCILIATION_STATUS` | 状态 | 未对账、对账中、已确认、对账差异 |
| `correction_version` | int | 是 | 默认0 | 并发 | 费用来源修正版本 |
| `correction_reason` | varchar(512) | 否 |  | 审计 | 修正原因 |
| `approval_id` | bigint | 否 | 权限审批引用 | 审计 | 费用修正、重推、责任改判对应审批实例 |
| `push_fail_reason` | varchar(1024) | 否 |  | 技术字段 | 推送失败原因 |
| `created_by` | bigint | 是 |  | 审计 | 创建人 |
| `created_at` | datetime | 是 |  | 审计 | 创建时间 |
| `updated_by` | bigint | 否 |  | 审计 | 更新人 |
| `updated_at` | datetime | 是 |  | 审计 | 更新时间 |
| `version` | int | 是 | 默认0 | 并发 | 乐观锁 |
| `deleted` | tinyint | 是 | 0/1 | 治理 | 逻辑删除 |

### 4.9 日志表字段

`tms_carrier_integration_log` 记录承运商接口交互：`integration_log_id`、`carrier_code`、`interface_type`、`biz_no`、`request_id`、`request_payload`、`response_payload`、`call_status`、`http_status`、`fail_reason`、`called_at`、`created_at`。

`tms_domain_event` 字段同其它系统：`event_id`、`event_code`、`event_name`、`event_type`、`aggregate_type`、`aggregate_id`、`aggregate_no`、`source_system`、`payload_json`、`event_status`、`retry_count`、`fail_reason`、`occurred_at`、`published_at`、`created_at`、`updated_at`。

`tms_event_consume_log` 字段同其它系统：`consume_log_id`、`event_code`、`source_system`、`consumer_name`、`idempotent_key`、`consume_status`、`retry_count`、`fail_reason`、`consumed_at`、`created_at`、`updated_at`。

`tms_operation_audit_log` 字段同其它系统：`operation_log_id`、`operator_id`、`operator_name`、`operation_type`、`target_type`、`target_id`、`target_no`、`approval_id`、`reason`、`before_snapshot`、`after_snapshot`、`result`、`fail_reason`、`request_id`、`operation_at`、`created_at`。TMS 高危动作包括换承运商、取消运单、作废面单、轨迹补录、签收冲正、异常关闭、费用来源修正和重推。

## 5. 枚举设计

| 枚举类型 | 值 | 名称 | 说明 |
| --- | --- | --- | --- |
| `TMS_SOURCE_SYSTEM` | `1` | OMS | 来源系统：OMS |
| `TMS_SOURCE_SYSTEM` | `2` | WMS | 来源系统：WMS |
| `TMS_SOURCE_SYSTEM` | `3` | 采购系统 | 来源系统：采购系统 |
| `TMS_SOURCE_SYSTEM` | `4` | 供应商系统 | 来源系统：供应商系统 |
| `TMS_SOURCE_SYSTEM` | `5` | 中央库存系统 | 来源系统：中央库存系统 |
| `TMS_SOURCE_SYSTEM` | `6` | 人工创建 | 来源系统：人工创建 |
| `TMS_SOURCE_ORDER_TYPE` | `1` | 销售出库 | 来源单类型 |
| `TMS_SOURCE_ORDER_TYPE` | `2` | 销售退货 | 来源单类型 |
| `TMS_SOURCE_ORDER_TYPE` | `3` | 采购到货 | 来源单类型 |
| `TMS_SOURCE_ORDER_TYPE` | `4` | 退供应商 | 来源单类型 |
| `TMS_SOURCE_ORDER_TYPE` | `5` | 调拨 | 来源单类型 |
| `TRANSPORT_SCENARIO` | `1` | 采购到货 | 供应商发货到仓 |
| `TRANSPORT_SCENARIO` | `2` | 销售发货 | 仓库发货给客户 |
| `TRANSPORT_SCENARIO` | `3` | 销售退货 | 客户退货回仓 |
| `TRANSPORT_SCENARIO` | `4` | 退供应商 | 仓库退货给供应商 |
| `TRANSPORT_SCENARIO` | `5` | 调拨运输 | 仓间调拨 |
| `TRANSPORT_TASK_STATUS` | `1` | 待接单 | 初始状态 |
| `TRANSPORT_TASK_STATUS` | `2` | 已接单 | TMS已接收 |
| `TRANSPORT_TASK_STATUS` | `3` | 已创建运单 | 已生成运单 |
| `TRANSPORT_TASK_STATUS` | `4` | 运输中 | 已发货或揽收 |
| `TRANSPORT_TASK_STATUS` | `5` | 已签收 | 运输完成 |
| `TRANSPORT_TASK_STATUS` | `6` | 已拒收 | 收货方拒收 |
| `TRANSPORT_TASK_STATUS` | `7` | 已取消 | 任务取消 |
| `TRANSPORT_TASK_STATUS` | `8` | 异常中 | 存在未关闭异常 |
| `WAYBILL_STATUS` | `1` | 待下单 | 未向承运商下单 |
| `WAYBILL_STATUS` | `2` | 已下单 | 承运商已接单 |
| `WAYBILL_STATUS` | `3` | 已揽收 | 承运商已揽收 |
| `WAYBILL_STATUS` | `4` | 运输中 | 在途 |
| `WAYBILL_STATUS` | `5` | 已到达 | 到达目的地 |
| `WAYBILL_STATUS` | `6` | 已签收 | 签收完成 |
| `WAYBILL_STATUS` | `7` | 已拒收 | 拒收 |
| `WAYBILL_STATUS` | `8` | 已取消 | 取消 |
| `WAYBILL_STATUS` | `9` | 异常中 | 存在未关闭异常 |
| `PACKAGE_STATUS` | `1` | 待面单 | 包裹待生成面单 |
| `PACKAGE_STATUS` | `2` | 已生成面单 | 面单已生成 |
| `PACKAGE_STATUS` | `3` | 已打印 | 面单已打印 |
| `PACKAGE_STATUS` | `4` | 已交接 | 已交给承运商 |
| `PACKAGE_STATUS` | `5` | 已取消 | 包裹取消 |
| `LABEL_STATUS` | `1` | 待生成 | 待生成面单 |
| `LABEL_STATUS` | `2` | 已生成 | 面单生成成功 |
| `LABEL_STATUS` | `3` | 已打印 | 已打印 |
| `LABEL_STATUS` | `4` | 已作废 | 已作废 |
| `LABEL_STATUS` | `5` | 生成失败 | 承运商返回失败 |
| `TRACK_NODE` | `1` | 下单 | 运单已下单 |
| `TRACK_NODE` | `2` | 揽收 | 承运商揽收 |
| `TRACK_NODE` | `3` | 发出 | 离开发货地 |
| `TRACK_NODE` | `4` | 到达 | 到达目的地 |
| `TRACK_NODE` | `5` | 派送 | 末端派送 |
| `TRACK_NODE` | `6` | 签收 | 已签收 |
| `TRACK_NODE` | `7` | 拒收 | 已拒收 |
| `TRACK_NODE` | `8` | 异常 | 运输异常 |
| `TRACK_SYNC_SOURCE` | `1` | 承运商回调 | 回调写入 |
| `TRACK_SYNC_SOURCE` | `2` | 主动拉取 | 定时同步 |
| `TRACK_SYNC_SOURCE` | `3` | 人工补录 | 人工录入 |
| `RECEIPT_RESULT` | `1` | 已签收 | 正常签收 |
| `RECEIPT_RESULT` | `2` | 已拒收 | 收货方拒收 |
| `RECEIPT_RESULT` | `3` | 部分签收 | 部分签收 |
| `RECEIPT_RESULT` | `4` | 签收冲突 | 轨迹冲突需人工处理 |
| `LOGISTICS_EXCEPTION_TYPE` | `1` | 延误 | 运输延误 |
| `LOGISTICS_EXCEPTION_TYPE` | `2` | 破损 | 商品或包裹破损 |
| `LOGISTICS_EXCEPTION_TYPE` | `3` | 丢失 | 运输丢失 |
| `LOGISTICS_EXCEPTION_TYPE` | `4` | 拒收 | 收货方拒收 |
| `LOGISTICS_EXCEPTION_TYPE` | `5` | 接口失败 | 承运商接口失败 |
| `LOGISTICS_EXCEPTION_TYPE` | `6` | 费用异常 | 费用异常 |
| `EXCEPTION_LEVEL` | `1` | 一般 | 一般异常 |
| `EXCEPTION_LEVEL` | `2` | 严重 | 严重异常 |
| `EXCEPTION_LEVEL` | `3` | 紧急 | 紧急异常 |
| `EXCEPTION_STATUS` | `1` | 已创建 | 初始状态 |
| `EXCEPTION_STATUS` | `2` | 处理中 | 已分派处理 |
| `EXCEPTION_STATUS` | `3` | 已升级 | 升级处理 |
| `EXCEPTION_STATUS` | `4` | 已关闭 | 处理完成 |
| `RESPONSIBLE_PARTY` | `1` | 物流商 | 物流商责任 |
| `RESPONSIBLE_PARTY` | `2` | 仓库 | 仓库责任 |
| `RESPONSIBLE_PARTY` | `3` | 客户 | 客户责任 |
| `RESPONSIBLE_PARTY` | `4` | 供应商 | 供应商责任 |
| `RESPONSIBLE_PARTY` | `5` | 系统 | 系统责任 |
| `FEE_SOURCE_STATUS` | `1` | 待生成 | 待生成费用来源 |
| `FEE_SOURCE_STATUS` | `2` | 已生成 | 已生成 |
| `FEE_SOURCE_STATUS` | `3` | 已推送 | 已推送BMS |
| `FEE_SOURCE_STATUS` | `4` | 推送失败 | 推送BMS失败 |
| `FEE_SOURCE_STATUS` | `5` | 已作废 | 已作废 |
| `FEE_SOURCE_STATUS` | `6` | 已采集 | BMS已采集 |
| `FEE_SOURCE_STATUS` | `7` | 对账差异 | BMS对账出现差异 |
| `FEE_SOURCE_STATUS` | `8` | 已修正 | TMS产生修正版本 |
| `SETTLEMENT_DIRECTION` | `1` | 应付 | 应付物流商 |
| `SETTLEMENT_DIRECTION` | `2` | 应收 | 向客户/货主收取 |
| `RECONCILIATION_STATUS` | `1` | 未对账 | BMS尚未进入对账 |
| `RECONCILIATION_STATUS` | `2` | 对账中 | BMS对账处理中 |
| `RECONCILIATION_STATUS` | `3` | 已确认 | BMS对账已确认 |
| `RECONCILIATION_STATUS` | `4` | 对账差异 | BMS对账存在差异 |
| `NOTIFY_STATUS` | `1` | 待通知 | 待通知来源系统 |
| `NOTIFY_STATUS` | `2` | 已通知 | 通知成功 |
| `NOTIFY_STATUS` | `3` | 通知失败 | 通知失败 |
| `EVENT_STATUS` | `1` | 待发布 | 事件待发布 |
| `EVENT_STATUS` | `2` | 发布中 | 发布中 |
| `EVENT_STATUS` | `3` | 已发布 | 发布成功 |
| `EVENT_STATUS` | `4` | 发布失败 | 发布失败 |
| `EVENT_STATUS` | `5` | 已取消 | 不再发布 |
| `CONSUME_STATUS` | `1` | 待消费 | 待消费 |
| `CONSUME_STATUS` | `2` | 处理中 | 消费中 |
| `CONSUME_STATUS` | `3` | 消费成功 | 消费成功 |
| `CONSUME_STATUS` | `4` | 消费失败 | 消费失败 |
| `CONSUME_STATUS` | `5` | 已忽略 | 幂等忽略 |

## 6. 事件、事件表与审计表字段

事件采用本地消息表模式：应用服务在同一事务内保存业务表和领域事件表，异步发布后更新事件状态；消费外部事件时先写消费日志，使用幂等键避免重复处理。

| 事件 | 来源对象 | 触发动作 | 主要载荷 | 订阅/用途 |
| --- | --- | --- | --- | --- |
| 运输任务已创建 | 运输任务 | 创建运输任务 | taskId、sourceSystem、sourceOrderNo、scenario、warehouseId、ownerId | 来源系统、WMS、读模型 |
| 运单已创建 | 运单 | 承运商下单成功 | waybillId、carrierId、carrierWaybillNo、sourceOrderNo、packages | OMS、WMS、采购、供应商 |
| 面单已生成 | 面单 | 生成面单成功 | labelId、waybillId、packageNo、labelUrl | WMS |
| 运输已发出 | 运单 | 发货交接/承运商揽收 | waybillId、handoverTime、carrierId、warehouseId | OMS、采购、BMS |
| 轨迹已追加 | 轨迹 | 回调/同步/补录轨迹 | waybillId、trackNode、trackTime、trackDesc、location | OMS、采购、供应商、客服读模型 |
| 运输已到达 | 运单 | 到达目的地 | waybillId、arrivalTime、location | WMS、采购、OMS |
| 运输已签收 | 签收回单 | 签收成功 | waybillId、signedTime、signedBy、proofUrl | OMS、采购、供应商、BMS |
| 运输已拒收 | 签收回单 | 拒收 | waybillId、rejectReason、rejectTime | OMS、采购、供应商、BMS |
| 物流异常已登记 | 物流异常 | 创建异常 | exceptionId、waybillId、exceptionType、level、responsibleParty | OMS、采购、WMS、BMS |
| 物流异常已关闭 | 物流异常 | 关闭异常 | exceptionId、closeResult、closeTime | 来源系统、BMS |
| 物流费用来源已生成 | 费用来源 | 生成费用来源 | feeSourceId、waybillId、carrierId、feeItems、currency | BMS |
| 物流费用来源已推送 | 费用来源 | 推送BMS成功 | feeSourceId、bmsReceiveNo、pushTime | BMS、TMS读模型 |
| 物流费用来源推送失败 | 费用来源 | 推送BMS失败 | feeSourceId、failReason、retryCount | TMS补偿、审计 |
| 物流费用来源已采集 | 费用来源 | 消费BMS采集回写 | feeSourceId、bmsBillingItemNo、collectedAt | TMS读模型、审计 |
| 物流费用来源已修正 | 费用来源 | 审批后修正费用来源 | feeSourceId、correctionVersion、reason、approvalId | BMS、审计 |
| BMS对账差异已发生 | 外部事件消费 | BMS回传对账差异 | feeSourceId、differenceReason、bmsBillNo | TMS费用来源、物流异常 |

## 7. DDL 文件

完整 MySQL 8.0 DDL 已生成到：

- [`ddl/06-TMS系统.sql`](ddl/06-TMS系统.sql)

DDL 包含业务表、枚举类型表、枚举项初始化数据、领域事件发布表、事件消费日志表、操作日志表和承运商接口日志表。

当前结论：TMS 数据库围绕运输事实源设计，库存变化和结算终审不落在 TMS；TMS 只保留运输状态、轨迹、签收、异常和物流费用来源。
