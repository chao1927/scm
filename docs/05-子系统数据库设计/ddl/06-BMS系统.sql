-- BMS系统 数据库 DDL

-- 口径：MySQL 8.0 / InnoDB / utf8mb4

SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;



CREATE TABLE `bms_enum_type` (
  `enum_type_id` bigint NOT NULL COMMENT '枚举类型ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_type_name` varchar(128) NOT NULL COMMENT '枚举类型名称',
  `configurable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许页面配置',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_type_id`),
  UNIQUE KEY `uk_bms_enum_type_code` (`enum_type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='枚举类型';

CREATE TABLE `bms_enum_item` (
  `enum_item_id` bigint NOT NULL COMMENT '枚举项ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_value` smallint NOT NULL COMMENT '枚举数值，从1开始',
  `enum_label` varchar(128) NOT NULL COMMENT '枚举显示名',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_item_id`),
  UNIQUE KEY `uk_bms_enum_item` (`enum_type_code`, `enum_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='枚举项';

CREATE TABLE `bms_domain_event` (
  `event_id` bigint NOT NULL COMMENT '事件ID',
  `event_code` varchar(128) NOT NULL COMMENT '事件编码',
  `event_name` varchar(128) NOT NULL COMMENT '事件名称',
  `event_type` varchar(128) NOT NULL COMMENT '事件类型',
  `aggregate_type` varchar(128) NOT NULL COMMENT '聚合类型',
  `aggregate_id` bigint NOT NULL COMMENT '聚合ID',
  `aggregate_no` varchar(128) NULL COMMENT '业务单号或编码',
  `source_system` varchar(64) NOT NULL COMMENT '来源系统',
  `payload_json` json NOT NULL COMMENT '事件载荷',
  `event_status` smallint NOT NULL DEFAULT 1 COMMENT '事件状态：1待发布 2发布中 3已发布 4发布失败 5已取消',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
  `fail_reason` varchar(1024) NULL COMMENT '失败原因',
  `occurred_at` datetime NOT NULL COMMENT '业务发生时间',
  `published_at` datetime NULL COMMENT '发布时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`event_id`),
  UNIQUE KEY `uk_bms_domain_event_code` (`event_code`),
  KEY `idx_bms_domain_event_status` (`event_status`, `created_at`),
  KEY `idx_bms_domain_event_aggregate` (`aggregate_type`, `aggregate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='领域事件发布表';

CREATE TABLE `bms_event_consume_log` (
  `consume_log_id` bigint NOT NULL COMMENT '消费日志ID',
  `event_code` varchar(128) NOT NULL COMMENT '事件编码',
  `source_system` varchar(64) NOT NULL COMMENT '来源系统',
  `consumer_name` varchar(128) NOT NULL COMMENT '消费者名称',
  `idempotent_key` varchar(256) NOT NULL COMMENT '幂等键',
  `consume_status` smallint NOT NULL DEFAULT 1 COMMENT '消费状态：1待消费 2处理中 3消费成功 4消费失败 5已忽略',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
  `fail_reason` varchar(1024) NULL COMMENT '失败原因',
  `consumed_at` datetime NULL COMMENT '消费完成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`consume_log_id`),
  UNIQUE KEY `uk_bms_event_consume` (`source_system`, `event_code`, `consumer_name`),
  KEY `idx_bms_event_consume_status` (`consume_status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='事件消费幂等日志';

CREATE TABLE `bms_operation_audit_log` (
  `operation_log_id` bigint NOT NULL COMMENT '操作日志ID',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(128) NULL COMMENT '操作人名称',
  `operation_type` varchar(64) NOT NULL COMMENT '操作类型',
  `target_type` varchar(128) NOT NULL COMMENT '操作对象类型',
  `target_id` bigint NULL COMMENT '操作对象ID',
  `target_no` varchar(128) NULL COMMENT '操作对象单号或编码',
  `before_snapshot` json NULL COMMENT '操作前快照',
  `after_snapshot` json NULL COMMENT '操作后快照',
  `result` smallint NOT NULL DEFAULT 1 COMMENT '结果：1成功 2失败',
  `fail_reason` varchar(1024) NULL COMMENT '失败原因',
  `request_id` varchar(128) NULL COMMENT '请求ID',
  `operation_at` datetime NOT NULL COMMENT '操作时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`operation_log_id`),
  KEY `idx_bms_operation_audit_target` (`target_type`, `target_id`),
  KEY `idx_bms_operation_audit_operator` (`operator_id`, `operation_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';



-- 枚举初始化数据
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (1, 'ADJUSTMENT_STATUS', 'ADJUSTMENT_STATUS', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (1, 'ADJUSTMENT_STATUS', 1, '草稿', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (2, 'ADJUSTMENT_STATUS', 2, '待审批', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (3, 'ADJUSTMENT_STATUS', 3, '已执行', 3, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (4, 'ADJUSTMENT_STATUS', 4, '已驳回', 4, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (5, 'ADJUSTMENT_STATUS', 5, '已取消', 5, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (2, 'APPROVAL_STATUS', 'APPROVAL_STATUS', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (6, 'APPROVAL_STATUS', 1, '草稿', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (7, 'APPROVAL_STATUS', 2, '待审批', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (8, 'APPROVAL_STATUS', 3, '已批准', 3, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (9, 'APPROVAL_STATUS', 4, '已驳回', 4, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (3, 'BILLING_ADJUSTMENT_TYPE', 'BILLING_ADJUSTMENT_TYPE', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (10, 'BILLING_ADJUSTMENT_TYPE', 1, '减免', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (11, 'BILLING_ADJUSTMENT_TYPE', 2, '补收', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (12, 'BILLING_ADJUSTMENT_TYPE', 3, '冲减', 3, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (13, 'BILLING_ADJUSTMENT_TYPE', 4, '修正', 4, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (4, 'BILLING_BIZ_TYPE', 'BILLING_BIZ_TYPE', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (14, 'BILLING_BIZ_TYPE', 1, '入库', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (15, 'BILLING_BIZ_TYPE', 2, '出库', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (16, 'BILLING_BIZ_TYPE', 3, '存储', 3, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (17, 'BILLING_BIZ_TYPE', 4, '运输', 4, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (18, 'BILLING_BIZ_TYPE', 5, '退货', 5, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (19, 'BILLING_BIZ_TYPE', 6, '售后', 6, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (5, 'BILLING_ITEM_STATUS', 'BILLING_ITEM_STATUS', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (20, 'BILLING_ITEM_STATUS', 1, '待计算', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (21, 'BILLING_ITEM_STATUS', 2, '计算异常', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (22, 'BILLING_ITEM_STATUS', 3, '待对账', 3, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (23, 'BILLING_ITEM_STATUS', 4, '对账差异', 4, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (24, 'BILLING_ITEM_STATUS', 5, '已确认', 5, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (25, 'BILLING_ITEM_STATUS', 6, '已入账', 6, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (26, 'BILLING_ITEM_STATUS', 7, '已作废', 7, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (6, 'BILLING_OBJECT_TYPE', 'BILLING_OBJECT_TYPE', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (27, 'BILLING_OBJECT_TYPE', 1, '客户', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (28, 'BILLING_OBJECT_TYPE', 2, '货主', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (29, 'BILLING_OBJECT_TYPE', 3, '供应商', 3, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (30, 'BILLING_OBJECT_TYPE', 4, '物流商', 4, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (7, 'BILL_STATUS', 'BILL_STATUS', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (31, 'BILL_STATUS', 1, '草稿', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (32, 'BILL_STATUS', 2, '待确认', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (33, 'BILL_STATUS', 3, '差异处理中', 3, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (34, 'BILL_STATUS', 4, '已确认', 4, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (35, 'BILL_STATUS', 5, '待开票', 5, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (36, 'BILL_STATUS', 6, '已开票', 6, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (37, 'BILL_STATUS', 7, '待入账', 7, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (38, 'BILL_STATUS', 8, '已入账', 8, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (39, 'BILL_STATUS', 9, '已关闭', 9, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (8, 'COMMON_STATUS', 'COMMON_STATUS', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (40, 'COMMON_STATUS', 1, '启用', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (41, 'COMMON_STATUS', 2, '停用', 2, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (9, 'CONSUME_STATUS', 'CONSUME_STATUS', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (42, 'CONSUME_STATUS', 1, '待消费', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (43, 'CONSUME_STATUS', 2, '处理中', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (44, 'CONSUME_STATUS', 3, '消费成功', 3, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (45, 'CONSUME_STATUS', 4, '消费失败', 4, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (46, 'CONSUME_STATUS', 5, '已忽略', 5, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (10, 'CURRENCY', 'CURRENCY', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (47, 'CURRENCY', 1, '人民币', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (48, 'CURRENCY', 2, '美元', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (49, 'CURRENCY', 3, '欧元', 3, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (50, 'CURRENCY', 4, '港币', 4, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (51, 'CURRENCY', 5, '日元', 5, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (11, 'EVENT_PROCESS_STATUS', 'EVENT_PROCESS_STATUS', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (52, 'EVENT_PROCESS_STATUS', 1, '待处理', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (53, 'EVENT_PROCESS_STATUS', 2, '成功', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (54, 'EVENT_PROCESS_STATUS', 3, '失败', 3, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (55, 'EVENT_PROCESS_STATUS', 4, '已忽略', 4, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (12, 'EVENT_STATUS', 'EVENT_STATUS', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (56, 'EVENT_STATUS', 1, '待发布', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (57, 'EVENT_STATUS', 2, '发布中', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (58, 'EVENT_STATUS', 3, '已发布', 3, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (59, 'EVENT_STATUS', 4, '发布失败', 4, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (60, 'EVENT_STATUS', 5, '已取消', 5, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (13, 'FEE_TYPE', 'FEE_TYPE', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (61, 'FEE_TYPE', 1, '入库费', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (62, 'FEE_TYPE', 2, '出库费', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (63, 'FEE_TYPE', 3, '仓储费', 3, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (64, 'FEE_TYPE', 4, '物流费', 4, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (65, 'FEE_TYPE', 5, '耗材费', 5, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (66, 'FEE_TYPE', 6, '增值服务', 6, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (67, 'FEE_TYPE', 7, '费用类型', 7, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (14, 'FINANCE_HANDOVER_STATUS', 'FINANCE_HANDOVER_STATUS', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (68, 'FINANCE_HANDOVER_STATUS', 1, '待交接', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (69, 'FINANCE_HANDOVER_STATUS', 2, '已交接', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (70, 'FINANCE_HANDOVER_STATUS', 3, '已入账', 3, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (71, 'FINANCE_HANDOVER_STATUS', 4, '失败', 4, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (15, 'INVOICE_STATUS', 'INVOICE_STATUS', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (72, 'INVOICE_STATUS', 1, '待申请', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (73, 'INVOICE_STATUS', 2, '已申请', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (74, 'INVOICE_STATUS', 3, '已开票', 3, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (75, 'INVOICE_STATUS', 4, '已作废', 4, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (16, 'INVOICE_TYPE', 'INVOICE_TYPE', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (76, 'INVOICE_TYPE', 1, '普票', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (77, 'INVOICE_TYPE', 2, '专票', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (78, 'INVOICE_TYPE', 3, '电子票', 3, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (17, 'OPERATION_RESULT', 'OPERATION_RESULT', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (79, 'OPERATION_RESULT', 1, '成功', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (80, 'OPERATION_RESULT', 2, '失败', 2, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (18, 'PRICING_METHOD', 'PRICING_METHOD', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (81, 'PRICING_METHOD', 1, '按件', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (82, 'PRICING_METHOD', 2, '按重量', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (83, 'PRICING_METHOD', 3, '按体积', 3, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (84, 'PRICING_METHOD', 4, '按天', 4, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (85, 'PRICING_METHOD', 5, '阶梯', 5, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (86, 'PRICING_METHOD', 6, '固定价', 6, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (19, 'RECON_STATUS', 'RECON_STATUS', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (87, 'RECON_STATUS', 1, '草稿', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (88, 'RECON_STATUS', 2, '待确认', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (89, 'RECON_STATUS', 3, '差异处理中', 3, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (90, 'RECON_STATUS', 4, '已确认', 4, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (91, 'RECON_STATUS', 5, '已关闭', 5, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (20, 'RULE_STATUS', 'RULE_STATUS', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (92, 'RULE_STATUS', 1, '草稿', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (93, 'RULE_STATUS', 2, '已发布', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (94, 'RULE_STATUS', 3, '已停用', 3, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (21, 'SETTLEMENT_CYCLE', 'SETTLEMENT_CYCLE', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (95, 'SETTLEMENT_CYCLE', 1, '日结', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (96, 'SETTLEMENT_CYCLE', 2, '周结', 2, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (97, 'SETTLEMENT_CYCLE', 3, '月结', 3, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (98, 'SETTLEMENT_CYCLE', 4, '单结', 4, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (22, 'SETTLEMENT_DIRECTION', 'SETTLEMENT_DIRECTION', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (99, 'SETTLEMENT_DIRECTION', 1, '应收', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (100, 'SETTLEMENT_DIRECTION', 2, '应付', 2, 1);
INSERT INTO `bms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (23, 'SOURCE_SYSTEM', 'SOURCE_SYSTEM', 1, 1);
INSERT INTO `bms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (101, 'SOURCE_SYSTEM', 1, '库存', 1, 1);



-- 领域事件类型参考数据
-- 1. 计费对象已创建: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`草稿`、规则版本、来源事件、幂等键
-- 2. 计费对象已启用: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已启用`、规则版本、来源事件、幂等键
-- 3. 计费关系已变更: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已启用`、规则版本、来源事件、幂等键
-- 4. 计费对象已停用: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已停用`、规则版本、来源事件、幂等键
-- 5. 客户已启用: 载荷 创建或更新客户计费对象快照
-- 6. 供应商已启用: 载荷 创建或更新供应商计费对象快照
-- 7. 计费规则已创建: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`草稿`、规则版本、来源事件、幂等键
-- 8. 计费规则已提交审批: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`审批中`、规则版本、来源事件、幂等键
-- 9. 计费规则已发布: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已发布`、规则版本、来源事件、幂等键
-- 10. 计费规则已停用: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已停用`、规则版本、来源事件、幂等键
-- 11. 计费规则版本已复制: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`草稿`、规则版本、来源事件、幂等键
-- 12. 税率已变更: 载荷 标记受影响规则需要复核
-- 13. 费用来源事件已采集: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已采集`、规则版本、来源事件、幂等键
-- 14. 可计费事实已识别: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待计算`、规则版本、来源事件、幂等键
-- 15. 费用来源异常已标记: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`异常`、规则版本、来源事件、幂等键
-- 16. 费用来源事件已忽略: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已忽略`、规则版本、来源事件、幂等键
-- 17. WMS已发货: 载荷 生成出库操作费或包装费来源事实
-- 18. WMS上架已完成: 载荷 生成入库操作费或上架费来源事实
-- 19. 费用明细已生成: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待对账`、规则版本、来源事件、幂等键
-- 20. 费用已确认: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已确认`、规则版本、来源事件、幂等键
-- 21. 费用已作废: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已作废`、规则版本、来源事件、幂等键
-- 22. 费用已重算: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待对账`、规则版本、来源事件、幂等键
-- 23. 费用调整单已创建: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`草稿`、规则版本、来源事件、幂等键
-- 24. 费用调整已提交审批: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`审批中`、规则版本、来源事件、幂等键
-- 25. 费用调整已审批: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待执行`、规则版本、来源事件、幂等键
-- 26. 费用已调整: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已执行`、规则版本、来源事件、幂等键
-- 27. 费用调整已驳回: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已驳回`、规则版本、来源事件、幂等键
-- 28. 对账差异已创建: 载荷 生成待处理调整建议
-- 29. 账单已入账: 载荷 限制原费用直接调整并要求冲减补差
-- 30. 对账单已生成: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待提交`、规则版本、来源事件、幂等键
-- 31. 对账单已提交: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待确认`、规则版本、来源事件、幂等键
-- 32. 对账单已确认: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已确认`、规则版本、来源事件、幂等键
-- 33. 对账单已关闭: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已关闭`、规则版本、来源事件、幂等键
-- 34. 账单已生成: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待确认`、规则版本、来源事件、幂等键
-- 35. 账单已确认: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已确认`、规则版本、来源事件、幂等键
-- 36. 开票已请求: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待开票`、规则版本、来源事件、幂等键
-- 37. 财务交接已请求: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待入账`、规则版本、来源事件、幂等键
-- 38. 账单已关闭: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已关闭`、规则版本、来源事件、幂等键
-- 39. 发票已开具: 载荷 更新账单开票状态
-- 40. 发票交接已创建: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待开票`、规则版本、来源事件、幂等键
-- 41. 发票已请求: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`开票中`、规则版本、来源事件、幂等键
-- 42. 发票已作废: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已作废`、规则版本、来源事件、幂等键
-- 43. 发票交接已关闭: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已关闭`、规则版本、来源事件、幂等键
-- 44. 开票系统已回调: 载荷 更新发票号、金额和附件
-- 45. 财务交接已创建: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`待交接`、规则版本、来源事件、幂等键
-- 46. 财务交接已提交: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`交接中`、规则版本、来源事件、幂等键
-- 47. 财务交接已完成: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已入账`、规则版本、来源事件、幂等键
-- 48. 财务交接已失败: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`失败`、规则版本、来源事件、幂等键
-- 49. 财务交接已关闭: 载荷 聚合ID、单号、计费对象、账期、费用类型、方向、金额、税额、状态`已关闭`、规则版本、来源事件、幂等键



-- 业务表

CREATE TABLE `bms_billing` (
  `billing_id` bigint NOT NULL COMMENT '计费对象主键',
  `object_code` varchar(64) NOT NULL COMMENT '对象编码',
  `object_name` varchar(256) NOT NULL COMMENT '对象名称',
  `object_type` smallint NOT NULL COMMENT '客户、货主、供应商、物流商',
  `settlement_direction` smallint NOT NULL COMMENT '应收、应付',
  `settlement_cycle` smallint NOT NULL COMMENT '日结、周结、月结、单结',
  `tax_rate` decimal(8,4) NULL COMMENT '默认税率',
  `currency` smallint NOT NULL COMMENT '币种',
  `status` smallint NOT NULL COMMENT '启用、停用',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`billing_id`),
  KEY `idx_bms_billing_status_time` (`status`, `updated_at`),
  KEY `idx_bms_billing_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='计费对象';

CREATE TABLE `bms_billing_rule` (
  `billing_rule_id` bigint NOT NULL COMMENT '计费规则主键',
  `rule_code` varchar(64) NOT NULL COMMENT '规则编码',
  `rule_name` varchar(128) NOT NULL COMMENT '规则名称',
  `fee_type` smallint NOT NULL COMMENT '入库费、出库费、仓储费、物流费、耗材费、增值服务',
  `pricing_method` smallint NOT NULL COMMENT '按件、按重量、按体积、按天、阶梯、固定价',
  `price_config` text NOT NULL COMMENT '价格配置',
  `tax_rate` decimal(8,4) NULL COMMENT '税率',
  `effective_from` date NOT NULL COMMENT '生效日期',
  `effective_to` date NULL COMMENT '失效日期',
  `rule_version` int NOT NULL COMMENT '规则版本',
  `status` smallint NOT NULL COMMENT '草稿、已发布、已停用',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`billing_rule_id`),
  KEY `idx_bms_billing_rule_status_time` (`status`, `updated_at`),
  KEY `idx_bms_billing_rule_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='计费规则';

CREATE TABLE `bms_fee` (
  `fee_id` bigint NOT NULL COMMENT '费用来源事件主键',
  `event_name` varchar(128) NOT NULL COMMENT '事件名称',
  `source_system` smallint NOT NULL COMMENT 'WMS、OMS、库存、TMS 等',
  `source_order_no` varchar(64) NULL COMMENT '来源单号',
  `biz_type` smallint NOT NULL COMMENT '入库、出库、存储、运输、退货、售后',
  `process_status` smallint NOT NULL COMMENT '待处理、成功、失败、已忽略',
  `fail_reason` varchar(1024) NULL COMMENT '失败原因',
  `received_at` datetime NOT NULL COMMENT '接收时间',
  `processed_at` datetime NULL COMMENT '处理时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`fee_id`),
  KEY `idx_bms_fee_status_time` (`process_status`, `updated_at`),
  KEY `idx_bms_fee_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='费用来源事件';

CREATE TABLE `bms_fee_line` (
  `fee_line_id` bigint NOT NULL COMMENT '费用明细主键',
  `billing_item_no` varchar(64) NOT NULL COMMENT '费用明细号',
  `settlement_direction` smallint NOT NULL COMMENT '应收、应付',
  `fee_type` smallint NOT NULL COMMENT '费用类型',
  `source_order_no` varchar(64) NULL COMMENT '来源单号',
  `rule_version` int NULL COMMENT '规则版本',
  `quantity` decimal(18,4) NOT NULL COMMENT '计费数量',
  `unit_price` decimal(18,6) NOT NULL COMMENT '单价',
  `amount` decimal(18,2) NOT NULL COMMENT '未税金额',
  `tax_rate` decimal(8,4) NULL COMMENT '税率',
  `tax_amount` decimal(18,2) NULL COMMENT '税额',
  `tax_included_amount` decimal(18,2) NOT NULL COMMENT '含税金额',
  `billing_period` varchar(32) NOT NULL COMMENT '账期，如 `2026-06`',
  `billing_status` smallint NOT NULL COMMENT '待计算、计算异常、待对账、对账差异、已确认、已入账、已作废',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`fee_line_id`),
  KEY `idx_bms_fee_line_status_time` (`billing_status`, `updated_at`),
  KEY `idx_bms_fee_line_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='费用明细';

CREATE TABLE `bms_adjustment` (
  `adjustment_id` bigint NOT NULL COMMENT '调整单主键',
  `adjustment_no` varchar(64) NOT NULL COMMENT '调整单号',
  `adjustment_type` smallint NOT NULL COMMENT '减免、补收、冲减、修正',
  `adjustment_amount` decimal(18,2) NOT NULL COMMENT '调整金额',
  `adjustment_reason` varchar(512) NOT NULL COMMENT '调整原因',
  `approval_status` smallint NOT NULL COMMENT '草稿、待审批、已批准、已驳回',
  `adjustment_status` smallint NOT NULL COMMENT '草稿、待审批、已执行、已驳回、已取消',
  `executed_at` datetime NULL COMMENT '执行时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`adjustment_id`),
  KEY `idx_bms_adjustment_status_time` (`approval_status`, `updated_at`),
  KEY `idx_bms_adjustment_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调整单';

CREATE TABLE `bms_reconcile` (
  `reconcile_id` bigint NOT NULL COMMENT '对账单主键',
  `reconciliation_no` varchar(64) NOT NULL COMMENT '对账单号',
  `settlement_direction` smallint NOT NULL COMMENT '应收、应付',
  `billing_period` varchar(32) NOT NULL COMMENT '账期',
  `total_amount` decimal(18,2) NOT NULL COMMENT '未税金额',
  `tax_amount` decimal(18,2) NULL COMMENT '税额',
  `tax_included_amount` decimal(18,2) NOT NULL COMMENT '含税金额',
  `diff_amount` decimal(18,2) NOT NULL DEFAULT 0 COMMENT '差异金额',
  `recon_status` smallint NOT NULL COMMENT '草稿、待确认、差异处理中、已确认、已关闭',
  `confirmed_at` datetime NULL COMMENT '确认时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`reconcile_id`),
  KEY `idx_bms_reconcile_status_time` (`recon_status`, `updated_at`),
  KEY `idx_bms_reconcile_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对账单';

CREATE TABLE `bms_bill` (
  `bill_id` bigint NOT NULL COMMENT '账单主键',
  `bill_no` varchar(64) NOT NULL COMMENT '账单号',
  `settlement_direction` smallint NOT NULL COMMENT '应收、应付',
  `billing_period` varchar(32) NOT NULL COMMENT '账期',
  `total_amount` decimal(18,2) NOT NULL COMMENT '未税金额',
  `tax_amount` decimal(18,2) NULL COMMENT '税额',
  `tax_included_amount` decimal(18,2) NOT NULL COMMENT '含税金额',
  `invoice_required` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否需要开票',
  `bill_status` smallint NOT NULL COMMENT '草稿、待确认、差异处理中、已确认、待开票、已开票、待入账、已入账、已关闭',
  `confirmed_at` datetime NULL COMMENT '确认时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`bill_id`),
  KEY `idx_bms_bill_status_time` (`bill_status`, `updated_at`),
  KEY `idx_bms_bill_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='账单';

CREATE TABLE `bms_invoice` (
  `invoice_id` bigint NOT NULL COMMENT '发票交接主键',
  `invoice_type` smallint NOT NULL COMMENT '普票、专票、电子票',
  `invoice_no` varchar(128) NULL COMMENT '发票号',
  `invoice_amount` decimal(18,2) NOT NULL COMMENT '开票金额',
  `invoice_status` smallint NOT NULL COMMENT '待申请、已申请、已开票、已作废',
  `invoice_file_url` varchar(512) NULL COMMENT '发票附件',
  `requested_at` datetime NULL COMMENT '申请时间',
  `issued_at` datetime NULL COMMENT '开票时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`invoice_id`),
  KEY `idx_bms_invoice_status_time` (`invoice_status`, `updated_at`),
  KEY `idx_bms_invoice_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='发票交接';

CREATE TABLE `bms_finance` (
  `finance_id` bigint NOT NULL COMMENT '财务交接主键',
  `handover_no` varchar(64) NOT NULL COMMENT '交接单号',
  `handover_status` smallint NOT NULL COMMENT '待交接、已交接、已入账、失败',
  `voucher_no` varchar(128) NULL COMMENT '财务凭证号',
  `handover_at` datetime NULL COMMENT '交接时间',
  `posted_at` datetime NULL COMMENT '入账时间',
  `fail_reason` varchar(512) NULL COMMENT '失败原因',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`finance_id`),
  KEY `idx_bms_finance_status_time` (`handover_status`, `updated_at`),
  KEY `idx_bms_finance_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='财务交接';



SET FOREIGN_KEY_CHECKS = 1;

