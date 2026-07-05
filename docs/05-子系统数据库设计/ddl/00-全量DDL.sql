-- 供应链系统全量 DDL

SET NAMES utf8mb4;



-- source: docs/05-子系统数据库设计/ddl/01-供应商系统.sql

-- 供应商系统 数据库 DDL

-- 口径：MySQL 8.0 / InnoDB / utf8mb4

SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;



CREATE TABLE `sup_enum_type` (
  `enum_type_id` bigint NOT NULL COMMENT '枚举类型ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_type_name` varchar(128) NOT NULL COMMENT '枚举类型名称',
  `configurable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许页面配置',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_type_id`),
  UNIQUE KEY `uk_sup_enum_type_code` (`enum_type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='枚举类型';

CREATE TABLE `sup_enum_item` (
  `enum_item_id` bigint NOT NULL COMMENT '枚举项ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_value` smallint NOT NULL COMMENT '枚举数值，从1开始',
  `enum_label` varchar(128) NOT NULL COMMENT '枚举显示名',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_item_id`),
  UNIQUE KEY `uk_sup_enum_item` (`enum_type_code`, `enum_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='枚举项';

CREATE TABLE `sup_domain_event` (
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
  UNIQUE KEY `uk_sup_domain_event_code` (`event_code`),
  KEY `idx_sup_domain_event_status` (`event_status`, `created_at`),
  KEY `idx_sup_domain_event_aggregate` (`aggregate_type`, `aggregate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='领域事件发布表';

CREATE TABLE `sup_event_consume_log` (
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
  UNIQUE KEY `uk_sup_event_consume` (`source_system`, `event_code`, `consumer_name`),
  KEY `idx_sup_event_consume_status` (`consume_status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='事件消费幂等日志';

CREATE TABLE `sup_operation_audit_log` (
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
  KEY `idx_sup_operation_audit_target` (`target_type`, `target_id`),
  KEY `idx_sup_operation_audit_operator` (`operator_id`, `operation_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';



-- 枚举初始化数据
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (1, 'APPROVAL_STATUS', 'APPROVAL_STATUS', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (1, 'APPROVAL_STATUS', 1, '草稿', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (2, 'APPROVAL_STATUS', 2, '待审批', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (3, 'APPROVAL_STATUS', 3, '已批准', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (4, 'APPROVAL_STATUS', 4, '已驳回', 4, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (2, 'ASN_STATUS', 'ASN_STATUS', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (5, 'ASN_STATUS', 1, '草稿', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (6, 'ASN_STATUS', 2, '已提交', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (7, 'ASN_STATUS', 3, '已预约', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (8, 'ASN_STATUS', 4, '已发货', 4, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (9, 'ASN_STATUS', 5, '已到仓', 5, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (10, 'ASN_STATUS', 6, '已收货', 6, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (11, 'ASN_STATUS', 7, '已取消', 7, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (12, 'ASN_STATUS', 8, '已关闭', 8, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (3, 'COMMON_STATUS', 'COMMON_STATUS', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (13, 'COMMON_STATUS', 1, '启用', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (14, 'COMMON_STATUS', 2, '停用', 2, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (4, 'CONSUME_STATUS', 'CONSUME_STATUS', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (15, 'CONSUME_STATUS', 1, '待消费', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (16, 'CONSUME_STATUS', 2, '处理中', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (17, 'CONSUME_STATUS', 3, '消费成功', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (18, 'CONSUME_STATUS', 4, '消费失败', 4, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (19, 'CONSUME_STATUS', 5, '已忽略', 5, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (5, 'CURRENCY', 'CURRENCY', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (20, 'CURRENCY', 1, '人民币', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (21, 'CURRENCY', 2, '美元', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (22, 'CURRENCY', 3, '欧元', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (23, 'CURRENCY', 4, '港币', 4, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (24, 'CURRENCY', 5, '日元', 5, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (6, 'EVENT_STATUS', 'EVENT_STATUS', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (25, 'EVENT_STATUS', 1, '待发布', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (26, 'EVENT_STATUS', 2, '发布中', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (27, 'EVENT_STATUS', 3, '已发布', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (28, 'EVENT_STATUS', 4, '发布失败', 4, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (29, 'EVENT_STATUS', 5, '已取消', 5, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (7, 'ISSUE_SEVERITY', 'ISSUE_SEVERITY', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (30, 'ISSUE_SEVERITY', 1, '轻微', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (31, 'ISSUE_SEVERITY', 2, '一般', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (32, 'ISSUE_SEVERITY', 3, '严重', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (33, 'ISSUE_SEVERITY', 4, '致命', 4, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (8, 'OPERATION_RESULT', 'OPERATION_RESULT', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (34, 'OPERATION_RESULT', 1, '成功', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (35, 'OPERATION_RESULT', 2, '失败', 2, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (9, 'PERFORMANCE_FACT_TYPE', 'PERFORMANCE_FACT_TYPE', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (36, 'PERFORMANCE_FACT_TYPE', 1, '质检', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (37, 'PERFORMANCE_FACT_TYPE', 2, '收货', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (38, 'PERFORMANCE_FACT_TYPE', 3, '报价', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (39, 'PERFORMANCE_FACT_TYPE', 4, '退供', 4, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (40, 'PERFORMANCE_FACT_TYPE', 5, '对账', 5, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (41, 'PERFORMANCE_FACT_TYPE', 6, '订单', 6, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (10, 'PO_CONFIRM_LINE_STATUS', 'PO_CONFIRM_LINE_STATUS', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (42, 'PO_CONFIRM_LINE_STATUS', 1, '待确认', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (43, 'PO_CONFIRM_LINE_STATUS', 2, '已确认', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (44, 'PO_CONFIRM_LINE_STATUS', 3, '有差异', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (45, 'PO_CONFIRM_LINE_STATUS', 4, '已拒绝', 4, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (11, 'PO_CONFIRM_STATUS', 'PO_CONFIRM_STATUS', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (46, 'PO_CONFIRM_STATUS', 1, '待确认', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (47, 'PO_CONFIRM_STATUS', 2, '已确认', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (48, 'PO_CONFIRM_STATUS', 3, '差异待处理', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (49, 'PO_CONFIRM_STATUS', 4, '已拒绝', 4, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (50, 'PO_CONFIRM_STATUS', 5, '已关闭', 5, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (12, 'PO_DIFF_TYPE', 'PO_DIFF_TYPE', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (51, 'PO_DIFF_TYPE', 1, '数量', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (52, 'PO_DIFF_TYPE', 2, '交期', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (53, 'PO_DIFF_TYPE', 3, '价格', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (54, 'PO_DIFF_TYPE', 4, '其他', 4, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (13, 'QUALITY_ISSUE_STATUS', 'QUALITY_ISSUE_STATUS', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (55, 'QUALITY_ISSUE_STATUS', 1, '待处理', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (56, 'QUALITY_ISSUE_STATUS', 2, '整改中', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (57, 'QUALITY_ISSUE_STATUS', 3, '待审核', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (58, 'QUALITY_ISSUE_STATUS', 4, '已关闭', 4, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (14, 'QUALITY_ISSUE_TYPE', 'QUALITY_ISSUE_TYPE', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (59, 'QUALITY_ISSUE_TYPE', 1, '外观', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (60, 'QUALITY_ISSUE_TYPE', 2, '规格', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (61, 'QUALITY_ISSUE_TYPE', 3, '包装', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (62, 'QUALITY_ISSUE_TYPE', 4, '性能', 4, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (63, 'QUALITY_ISSUE_TYPE', 5, '证照', 5, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (15, 'QUALITY_SOURCE_DOC_TYPE', 'QUALITY_SOURCE_DOC_TYPE', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (64, 'QUALITY_SOURCE_DOC_TYPE', 1, '质检单', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (65, 'QUALITY_SOURCE_DOC_TYPE', 2, '退供单', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (66, 'QUALITY_SOURCE_DOC_TYPE', 3, '客诉', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (67, 'QUALITY_SOURCE_DOC_TYPE', 4, '抽检', 4, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (16, 'RECON_CONFIRM_STATUS', 'RECON_CONFIRM_STATUS', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (68, 'RECON_CONFIRM_STATUS', 1, '待确认', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (69, 'RECON_CONFIRM_STATUS', 2, '已确认', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (70, 'RECON_CONFIRM_STATUS', 3, '差异待处理', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (71, 'RECON_CONFIRM_STATUS', 4, '已开票', 4, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (72, 'RECON_CONFIRM_STATUS', 5, '已关闭', 5, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (17, 'RECTIFICATION_SOURCE_TYPE', 'RECTIFICATION_SOURCE_TYPE', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (73, 'RECTIFICATION_SOURCE_TYPE', 1, '评分预警', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (74, 'RECTIFICATION_SOURCE_TYPE', 2, '质量问题', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (75, 'RECTIFICATION_SOURCE_TYPE', 3, '人工发起', 3, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (18, 'RECTIFICATION_STATUS', 'RECTIFICATION_STATUS', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (76, 'RECTIFICATION_STATUS', 1, '待提交', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (77, 'RECTIFICATION_STATUS', 2, '已提交', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (78, 'RECTIFICATION_STATUS', 3, '审核通过', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (79, 'RECTIFICATION_STATUS', 4, '驳回', 4, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (80, 'RECTIFICATION_STATUS', 5, '已关闭', 5, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (19, 'SCORE_DIMENSION', 'SCORE_DIMENSION', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (81, 'SCORE_DIMENSION', 1, '质量', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (82, 'SCORE_DIMENSION', 2, '价格', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (83, 'SCORE_DIMENSION', 3, '交付', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (84, 'SCORE_DIMENSION', 4, '响应', 4, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (85, 'SCORE_DIMENSION', 5, '异常', 5, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (86, 'SCORE_DIMENSION', 6, '评分维度', 6, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (20, 'SCORE_FORMULA_TYPE', 'SCORE_FORMULA_TYPE', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (87, 'SCORE_FORMULA_TYPE', 1, '阈值', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (88, 'SCORE_FORMULA_TYPE', 2, '比例', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (89, 'SCORE_FORMULA_TYPE', 3, '扣分', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (90, 'SCORE_FORMULA_TYPE', 4, '人工', 4, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (21, 'SCORE_PERIOD_TYPE', 'SCORE_PERIOD_TYPE', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (91, 'SCORE_PERIOD_TYPE', 1, '月度', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (92, 'SCORE_PERIOD_TYPE', 2, '季度', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (93, 'SCORE_PERIOD_TYPE', 3, '半年', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (94, 'SCORE_PERIOD_TYPE', 4, '年度', 4, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (22, 'SOURCE_SYSTEM', 'SOURCE_SYSTEM', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (95, 'SOURCE_SYSTEM', 1, '采购', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (96, 'SOURCE_SYSTEM', 2, '供应商系统', 2, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (23, 'SUPPLIER_REASON_CODE', 'SUPPLIER_REASON_CODE', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (97, 'SUPPLIER_REASON_CODE', 1, '拒绝或差异原因', 1, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (24, 'SUPPLIER_RETURN_CONFIRM_STATUS', 'SUPPLIER_RETURN_CONFIRM_STATUS', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (98, 'SUPPLIER_RETURN_CONFIRM_STATUS', 1, '待确认', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (99, 'SUPPLIER_RETURN_CONFIRM_STATUS', 2, '已确认', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (100, 'SUPPLIER_RETURN_CONFIRM_STATUS', 3, '已拒绝', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (101, 'SUPPLIER_RETURN_CONFIRM_STATUS', 4, '已签收', 4, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (102, 'SUPPLIER_RETURN_CONFIRM_STATUS', 5, '差异待处理', 5, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (103, 'SUPPLIER_RETURN_CONFIRM_STATUS', 6, '已关闭', 6, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (25, 'SUPPLIER_SCORE_LEVEL', 'SUPPLIER_SCORE_LEVEL', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (104, 'SUPPLIER_SCORE_LEVEL', 1, '待定义1', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (105, 'SUPPLIER_SCORE_LEVEL', 2, '待定义2', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (106, 'SUPPLIER_SCORE_LEVEL', 3, '待定义3', 3, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (26, 'SUPPLIER_SCORE_STATUS', 'SUPPLIER_SCORE_STATUS', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (107, 'SUPPLIER_SCORE_STATUS', 1, '待计算', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (108, 'SUPPLIER_SCORE_STATUS', 2, '已计算', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (109, 'SUPPLIER_SCORE_STATUS', 3, '正常', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (110, 'SUPPLIER_SCORE_STATUS', 4, '预警', 4, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (111, 'SUPPLIER_SCORE_STATUS', 5, '整改中', 5, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (112, 'SUPPLIER_SCORE_STATUS', 6, '冻结建议', 6, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (113, 'SUPPLIER_SCORE_STATUS', 7, '已冻结', 7, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (27, 'SUPPLIER_USER_ROLE', 'SUPPLIER_USER_ROLE', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (114, 'SUPPLIER_USER_ROLE', 1, '业务', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (115, 'SUPPLIER_USER_ROLE', 2, '财务', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (116, 'SUPPLIER_USER_ROLE', 3, '质量', 3, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (117, 'SUPPLIER_USER_ROLE', 4, '管理员', 4, 1);
INSERT INTO `sup_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (28, 'SUPPLY_STATUS', 'SUPPLY_STATUS', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (118, 'SUPPLY_STATUS', 1, '可供', 1, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (119, 'SUPPLY_STATUS', 2, '暂停', 2, 1);
INSERT INTO `sup_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (120, 'SUPPLY_STATUS', 3, '停供', 3, 1);



-- 领域事件类型参考数据
-- 1. 潜在供应商已创建: 载荷 supplierId、supplierCode、名称、创建人
-- 2. 供应商准入已提交: 载荷 supplierId、资质摘要、提交人
-- 3. 供应商已启用: 载荷 supplierId、supplierCode、启用时间
-- 4. 供应商资料变更已生效: 载荷 supplierId、变更字段、前后值摘要
-- 5. 供应商已冻结: 载荷 supplierId、冻结原因、冻结范围
-- 6. 供应商已停用: 载荷 supplierId、停用原因
-- 7. 供应商黑名单已命中: 载荷 风险等级=黑名单，若启用则自动冻结或生成冻结待办
-- 8. 供应商评分预警已产生: 载荷 风险等级上调，可能触发冻结建议
-- 9. 供应商未完业务已清理: 载荷 解除停用阻塞，允许执行停用命令
-- 10. 供应商商品已启用: 载荷 supplierSkuId、supplierId、skuId、供货条件
-- 11. 供应商商品供货条件已变更: 载荷 supplierSkuId、变更字段、前后值
-- 12. 供应商商品已暂停: 载荷 supplierSkuId、原因、恢复条件
-- 13. 供应商商品已恢复: 载荷 supplierSkuId、恢复时间
-- 14. 供应商商品已停供: 载荷 supplierSkuId、停供原因
-- 15. SKU已启用: 载荷 更新SKU可用快照，允许创建供货关系
-- 16. SKU已停用: 载荷 相关供应商商品自动暂停或生成停供待办
-- 17. 供应商报价已采纳: 载荷 更新价格快照，满足可采购前置条件
-- 18. 供应商报价已创建: 载荷 quoteId、供应商、报价行摘要
-- 19. 供应商报价已提交: 载荷 quoteId、报价行、有效期
-- 20. 供应商报价已确认: 载荷 quoteId、确认人、确认时间
-- 21. 供应商报价已拒绝: 载荷 quoteId、拒绝原因
-- 22. 供应商报价已过期: 载荷 quoteId、过期时间
-- 23. 询价单已发布: 载荷 创建供应商报价待办或草稿
-- 24. 询价已截标: 载荷 未提交报价自动作废或禁止提交
-- 25. 供应商合同已创建: 载荷 contractId、supplierId、合同类型
-- 26. 供应商合同已提交审批: 载荷 contractId、条款摘要、提交人
-- 27. 供应商合同已生效: 载荷 contractId、有效期、条款摘要
-- 28. 供应商合同已变更: 载荷 contractId、版本号、变更字段
-- 29. 供应商合同已续签: 载荷 contractId、新有效期
-- 30. 供应商合同已终止: 载荷 contractId、终止原因
-- 31. 审批已完成: 载荷 根据审批结果推动合同生效或驳回
-- 32. 合同即将到期任务已触发: 载荷 生成续签待办或到期预警状态
-- 33. 采购订单确认待办已创建: 载荷 poConfirmId、purchaseOrderId、supplierId
-- 34. 供应商订单已确认: 载荷 purchaseOrderId、承诺交期、确认行
-- 35. 供应商订单已拒绝: 载荷 purchaseOrderId、拒绝原因
-- 36. 供应商订单差异已反馈: 载荷 purchaseOrderId、差异类型、差异行
-- 37. 供应商承诺交期已变更: 载荷 purchaseOrderId、新交期、原因
-- 38. 采购订单确认已关闭: 载荷 poConfirmId、关闭原因
-- 39. 采购订单已发布: 载荷 创建订单确认待办
-- 40. 采购订单变更已发布: 载荷 更新订单行快照，已确认时要求重新确认
-- 41. 采购订单已取消: 载荷 关闭确认记录，禁止继续确认
-- 42. 采购订单已关闭: 载荷 确认聚合状态->已关闭
-- 43. ASN已创建: 载荷 asnId、purchaseOrderId、供应商
-- 44. ASN已提交: 载荷 asnId、预约时间、通知数量
-- 45. ASN已修改: 载荷 asnId、变更字段、通知数量
-- 46. ASN已取消: 载荷 asnId、取消原因
-- 47. ASN已发货: 载荷 asnId、物流信息、发货时间
-- 48. ASN已到仓: 载荷 asnId、到仓时间
-- 49. ASN已收货: 载荷 asnId、实收数量、差异
-- 50. 采购订单已确认: 载荷 允许创建ASN，并建立可发货数量快照
-- 51. WMS到货已登记: 载荷 ASN状态更新为已到仓，记录到仓时间
-- 52. WMS收货已完成: 载荷 更新实收、拒收和差异，状态部分收货/已收货
-- 53. 质检已完成: 载荷 更新质检结果快照，为质量问题和评分提供事实
-- 54. 供应商质量问题已创建: 载荷 qualityIssueId、supplierId、来源、严重等级
-- 55. 供应商整改已发起: 载荷 qualityIssueId、整改要求、截止时间
-- 56. 供应商整改已提交: 载荷 qualityIssueId、整改方案摘要
-- 57. 供应商整改已通过: 载荷 qualityIssueId、验证结论
-- 58. 供应商整改已驳回: 载荷 qualityIssueId、驳回原因
-- 59. 供应商整改已逾期: 载荷 qualityIssueId、逾期天数
-- 60. 供应商质量风险已升级: 载荷 qualityIssueId、风险等级
-- 61. 客诉已确认: 载荷 供应商责任客诉创建质量问题
-- 62. 退供已关闭: 载荷 补充退供数量、原因和损失金额
-- 63. 供应商绩效事实已采集: 载荷 supplierId、事实类型、指标值、来源事件
-- 64. 供应商评分已计算: 载荷 supplierId、period、维度分、总分
-- 65. 供应商评分已修正: 载荷 supplierId、period、修正前后值、原因
-- 66. 供应商评分已发布: 载荷 supplierId、period、总分、等级
-- 67. 供应商冻结建议已生成: 载荷 supplierId、建议原因、证据
-- 68. 供应商对账已确认: 载荷 采集对账配合度和差异率
-- 69. 供应商对账确认待办已创建: 载荷 reconcileConfirmId、对账单、金额
-- 70. 供应商对账差异已反馈: 载荷 对账单ID、差异类型、差异金额
-- 71. 供应商对账差异已撤回: 载荷 对账单ID、撤回原因
-- 72. 供应商发票资料已提交: 载荷 对账单ID、发票号、金额
-- 73. 供应商对账确认已关闭: 载荷 对账单ID、关闭原因
-- 74. 对账单已生成: 载荷 创建供应商确认待办
-- 75. 对账单已调整: 载荷 更新对账单快照，已确认时要求重新确认
-- 76. 发票已校验: 载荷 更新发票校验状态，失败时进入发票待补充
-- 77. 应付已完成: 载荷 关闭对账确认，标记结算完成
-- 78. 退供应商单已创建: 载荷 returnSupplierId、供应商、原因、申请数量
-- 79. 退供应商单已提交审核: 载荷 returnSupplierId、提交人、证据摘要
-- 80. 退供应商单已审核: 载荷 returnSupplierId、审核结果、责任方
-- 81. 退供库存锁定已请求: 载荷 returnSupplierId、SKU、批次、数量
-- 82. 退供供应商确认已请求: 载荷 returnSupplierId、退供行
-- 83. 供应商已确认退货: 载荷 returnSupplierId、确认时间
-- 84. 供应商退供差异已反馈: 载荷 returnSupplierId、差异类型、差异数量
-- 85. 退供出库已请求: 载荷 returnSupplierId、仓库、退供行
-- 86. 退供已出库: 载荷 returnSupplierId、实际出库数量
-- 87. 供应商已签收: 载荷 returnSupplierId、签收数量、差异
-- 88. 退供对账已完成: 载荷 returnSupplierId、冲减金额、索赔金额
-- 89. 退供应商单已关闭: 载荷 returnSupplierId、关闭原因
-- 90. 质检不合格品已入不良区: 载荷 创建或补充退供候选行
-- 91. 退供库存已锁定: 载荷 记录锁定成功，状态进入待供应商确认
-- 92. 退供出库已完成: 载荷 记录实际出库数量和批次，状态进入已出库
-- 93. 退供运输异常已发生: 载荷 记录运输异常，阻塞签收和关闭
-- 94. 应付已冲减: 载荷 记录结算影响，满足关闭条件
-- 95. 索赔已确认: 载荷 记录索赔结果，作为退供结算闭环依据



-- 业务表

CREATE TABLE `sup_supplier_user` (
  `supplier_user_id` bigint NOT NULL COMMENT '供应商用户绑定主键',
  `binding_role` smallint NOT NULL COMMENT '业务、财务、质量、管理员',
  `is_primary` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否主账号',
  `status` smallint NOT NULL COMMENT '启用、停用',
  `bound_by` bigint NOT NULL COMMENT '绑定人',
  `bound_at` datetime NOT NULL COMMENT '绑定时间',
  `unbound_at` datetime NULL COMMENT '解绑时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`supplier_user_id`),
  KEY `idx_sup_supplier_user_status_time` (`status`, `updated_at`),
  KEY `idx_sup_supplier_user_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商用户绑定';

CREATE TABLE `sup_supplier_item` (
  `supplier_item_id` bigint NOT NULL COMMENT '供应商商品主键',
  `sku_code` varchar(64) NOT NULL COMMENT '内部 SKU 编码',
  `supplier_sku_code` varchar(128) NULL COMMENT '供应商商品编码',
  `moq` decimal(18,4) NULL COMMENT '最小起订量',
  `mpq` decimal(18,4) NULL COMMENT '最小包装量',
  `lead_time_days` int NULL COMMENT '供货周期',
  `supply_status` smallint NOT NULL COMMENT '可供、暂停、停供',
  `effective_from` date NULL COMMENT '生效日期',
  `effective_to` date NULL COMMENT '失效日期',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`supplier_item_id`),
  KEY `idx_sup_supplier_item_status_time` (`supply_status`, `updated_at`),
  KEY `idx_sup_supplier_item_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商商品';

CREATE TABLE `sup_order` (
  `order_id` bigint NOT NULL COMMENT '采购订单协同单主键',
  `confirm_no` varchar(64) NOT NULL COMMENT '协同单号',
  `purchase_order_no` varchar(64) NOT NULL COMMENT '采购订单号',
  `confirm_status` smallint NOT NULL COMMENT '待确认、已确认、差异待处理、已拒绝、已关闭',
  `confirm_deadline` datetime NULL COMMENT '确认截止时间',
  `confirmed_at` datetime NULL COMMENT '确认时间',
  `diff_type` smallint NULL COMMENT '数量、交期、价格、其他',
  `reason_code` smallint NULL COMMENT '拒绝或差异原因',
  `remark` varchar(512) NULL COMMENT '备注',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`order_id`),
  KEY `idx_sup_order_status_time` (`confirm_status`, `updated_at`),
  KEY `idx_sup_order_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购订单协同单';

CREATE TABLE `sup_order_line` (
  `order_line_id` bigint NOT NULL COMMENT '采购订单协同行主键',
  `sku_code` varchar(64) NOT NULL COMMENT 'SKU 编码',
  `order_qty` decimal(18,4) NOT NULL COMMENT '采购数量',
  `confirmed_qty` decimal(18,4) NULL COMMENT '确认数量',
  `requested_delivery_date` date NULL COMMENT '要求交期',
  `confirmed_delivery_date` date NULL COMMENT '确认交期',
  `line_status` smallint NOT NULL COMMENT '待确认、已确认、有差异、已拒绝',
  `diff_reason` varchar(512) NULL COMMENT '行差异说明',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`order_line_id`),
  KEY `idx_sup_order_line_status_time` (`line_status`, `updated_at`),
  KEY `idx_sup_order_line_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购订单协同行';

CREATE TABLE `sup_asn` (
  `asn_id` bigint NOT NULL COMMENT 'ASN 主表主键',
  `asn_no` varchar(64) NOT NULL COMMENT 'ASN 单号',
  `eta` datetime NOT NULL COMMENT '预计到仓时间',
  `ship_at` datetime NULL COMMENT '发货时间',
  `carrier_name` varchar(128) NULL COMMENT '承运商',
  `tracking_no` varchar(128) NULL COMMENT '运单号',
  `asn_status` smallint NOT NULL COMMENT '草稿、已提交、已预约、已发货、已到仓、已收货、已取消、已关闭',
  `cancel_reason` varchar(512) NULL COMMENT '取消原因',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`asn_id`),
  KEY `idx_sup_asn_status_time` (`asn_status`, `updated_at`),
  KEY `idx_sup_asn_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ASN 主表';

CREATE TABLE `sup_asn_line` (
  `asn_line_id` bigint NOT NULL COMMENT 'ASN 行主键',
  `sku_code` varchar(64) NOT NULL COMMENT 'SKU 编码',
  `planned_qty` decimal(18,4) NOT NULL COMMENT '计划发货数量',
  `received_qty` decimal(18,4) NULL COMMENT 'WMS 回告实收数量',
  `batch_no` varchar(128) NULL COMMENT '批次号',
  `production_date` date NULL COMMENT '生产日期',
  `expire_date` date NULL COMMENT '失效日期',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`asn_line_id`),
  KEY `idx_sup_asn_line_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ASN 行';

CREATE TABLE `sup_quality` (
  `quality_id` bigint NOT NULL COMMENT '质量问题主键',
  `issue_no` varchar(64) NOT NULL COMMENT '问题单号',
  `source_doc_type` smallint NOT NULL COMMENT '质检单、退供单、客诉、抽检',
  `source_doc_no` varchar(64) NOT NULL COMMENT '来源单号',
  `issue_type` smallint NOT NULL COMMENT '外观、规格、包装、性能、证照',
  `severity` smallint NOT NULL COMMENT '轻微、一般、严重、致命',
  `issue_status` smallint NOT NULL COMMENT '待处理、整改中、待审核、已关闭',
  `deadline` datetime NULL COMMENT '整改截止时间',
  `closed_at` datetime NULL COMMENT '关闭时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`quality_id`),
  KEY `idx_sup_quality_status_time` (`issue_status`, `updated_at`),
  KEY `idx_sup_quality_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='质量问题';

CREATE TABLE `sup_record` (
  `record_id` bigint NOT NULL COMMENT '整改记录主键',
  `rectification_no` varchar(64) NOT NULL COMMENT '整改单号',
  `source_type` smallint NOT NULL COMMENT '评分预警、质量问题、人工发起',
  `issue_desc` varchar(1024) NOT NULL COMMENT '问题描述',
  `deadline` datetime NOT NULL COMMENT '整改截止时间',
  `rectification_status` smallint NOT NULL COMMENT '待提交、已提交、审核通过、驳回、已关闭',
  `submitted_at` datetime NULL COMMENT '提交时间',
  `reviewed_by` bigint NULL COMMENT '审核人',
  `reviewed_at` datetime NULL COMMENT '审核时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`record_id`),
  KEY `idx_sup_record_status_time` (`rectification_status`, `updated_at`),
  KEY `idx_sup_record_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='整改记录';

CREATE TABLE `sup_supplier_score` (
  `supplier_score_id` bigint NOT NULL COMMENT '供应商评分主键',
  `score_period` varchar(32) NOT NULL COMMENT '评分周期，如 `2026-06`',
  `period_type` smallint NOT NULL COMMENT '月度、季度、半年、年度',
  `total_score` decimal(5,2) NOT NULL COMMENT '综合评分',
  `score_level` smallint NOT NULL COMMENT 'A/B/C/D/E',
  `score_status` smallint NOT NULL COMMENT '待计算、已计算、正常、预警、整改中、冻结建议、已冻结',
  `warning_reason` varchar(512) NULL COMMENT '预警原因',
  `calculated_at` datetime NOT NULL COMMENT '计算时间',
  `manual_adjusted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否人工修正',
  `adjust_reason` varchar(512) NULL COMMENT '修正原因',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`supplier_score_id`),
  KEY `idx_sup_supplier_score_status_time` (`score_status`, `updated_at`),
  KEY `idx_sup_supplier_score_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商评分';

CREATE TABLE `sup_score_line` (
  `score_line_id` bigint NOT NULL COMMENT '评分明细主键',
  `dimension` smallint NOT NULL COMMENT '质量、价格、交付、响应、异常',
  `raw_score` decimal(5,2) NOT NULL COMMENT '原始分',
  `weight` decimal(5,2) NOT NULL COMMENT '权重',
  `weighted_score` decimal(5,2) NOT NULL COMMENT '加权分',
  `metric_summary` text NULL COMMENT '指标摘要',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`score_line_id`),
  KEY `idx_sup_score_line_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评分明细';

CREATE TABLE `sup_business_object` (
  `business_object_id` bigint NOT NULL COMMENT '绩效事实主键',
  `fact_type` smallint NOT NULL COMMENT '质检、收货、报价、退供、对账、订单',
  `source_system` smallint NOT NULL COMMENT '采购、WMS、BMS、供应商系统',
  `source_doc_type` varchar(32) NOT NULL COMMENT '来源单据类型',
  `source_doc_no` varchar(64) NOT NULL COMMENT '来源单号',
  `metric_code` varchar(64) NOT NULL COMMENT '指标编码',
  `metric_value` decimal(18,4) NOT NULL COMMENT '指标值',
  `occurred_at` datetime NOT NULL COMMENT '事实发生时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`business_object_id`),
  KEY `idx_sup_business_object_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='绩效事实';

CREATE TABLE `sup_score_rule` (
  `score_rule_id` bigint NOT NULL COMMENT '评分规则主键',
  `rule_code` varchar(64) NOT NULL COMMENT '规则编码',
  `dimension` smallint NOT NULL COMMENT '评分维度',
  `weight` decimal(5,2) NOT NULL COMMENT '维度权重',
  `formula_type` smallint NOT NULL COMMENT '阈值、比例、扣分、人工',
  `formula_config` text NOT NULL COMMENT '公式配置',
  `warning_threshold` decimal(5,2) NULL COMMENT '预警阈值',
  `effective_from` date NOT NULL COMMENT '生效日期',
  `effective_to` date NULL COMMENT '失效日期',
  `status` smallint NOT NULL COMMENT '启用、停用',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`score_rule_id`),
  KEY `idx_sup_score_rule_status_time` (`status`, `updated_at`),
  KEY `idx_sup_score_rule_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评分规则';

CREATE TABLE `sup_reconcile` (
  `reconcile_id` bigint NOT NULL COMMENT '对账协同单主键',
  `reconciliation_no` varchar(64) NOT NULL COMMENT '对账单号',
  `bill_amount` decimal(18,2) NOT NULL COMMENT '应付金额',
  `tax_amount` decimal(18,2) NULL COMMENT '税额',
  `diff_amount` decimal(18,2) NULL COMMENT '差异金额',
  `confirm_status` smallint NOT NULL COMMENT '待确认、已确认、差异待处理、已开票、已关闭',
  `invoice_no` varchar(128) NULL COMMENT '发票号',
  `invoice_file_url` varchar(512) NULL COMMENT '发票附件',
  `confirmed_at` datetime NULL COMMENT '确认时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`reconcile_id`),
  KEY `idx_sup_reconcile_status_time` (`confirm_status`, `updated_at`),
  KEY `idx_sup_reconcile_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对账协同单';

CREATE TABLE `sup_supplier_return` (
  `supplier_return_id` bigint NOT NULL COMMENT '退供协同单主键',
  `return_confirm_no` varchar(64) NOT NULL COMMENT '退供协同单号',
  `return_status` smallint NOT NULL COMMENT '待确认、已确认、已拒绝、已签收、差异待处理、已关闭',
  `return_address` varchar(512) NULL COMMENT '退货地址',
  `confirmed_qty` decimal(18,4) NULL COMMENT '确认接收数量',
  `signed_qty` decimal(18,4) NULL COMMENT '签收数量',
  `diff_reason` varchar(512) NULL COMMENT '差异原因',
  `confirmed_at` datetime NULL COMMENT '确认时间',
  `signed_at` datetime NULL COMMENT '签收时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`supplier_return_id`),
  KEY `idx_sup_supplier_return_status_time` (`return_status`, `updated_at`),
  KEY `idx_sup_supplier_return_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退供协同单';



SET FOREIGN_KEY_CHECKS = 1;



-- source: docs/05-子系统数据库设计/ddl/02-采购系统.sql

-- 采购系统 数据库 DDL

-- 口径：MySQL 8.0 / InnoDB / utf8mb4

SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;



CREATE TABLE `pur_enum_type` (
  `enum_type_id` bigint NOT NULL COMMENT '枚举类型ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_type_name` varchar(128) NOT NULL COMMENT '枚举类型名称',
  `configurable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许页面配置',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_type_id`),
  UNIQUE KEY `uk_pur_enum_type_code` (`enum_type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='枚举类型';

CREATE TABLE `pur_enum_item` (
  `enum_item_id` bigint NOT NULL COMMENT '枚举项ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_value` smallint NOT NULL COMMENT '枚举数值，从1开始',
  `enum_label` varchar(128) NOT NULL COMMENT '枚举显示名',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_item_id`),
  UNIQUE KEY `uk_pur_enum_item` (`enum_type_code`, `enum_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='枚举项';

CREATE TABLE `pur_domain_event` (
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
  UNIQUE KEY `uk_pur_domain_event_code` (`event_code`),
  KEY `idx_pur_domain_event_status` (`event_status`, `created_at`),
  KEY `idx_pur_domain_event_aggregate` (`aggregate_type`, `aggregate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='领域事件发布表';

CREATE TABLE `pur_event_consume_log` (
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
  UNIQUE KEY `uk_pur_event_consume` (`source_system`, `event_code`, `consumer_name`),
  KEY `idx_pur_event_consume_status` (`consume_status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='事件消费幂等日志';

CREATE TABLE `pur_operation_audit_log` (
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
  KEY `idx_pur_operation_audit_target` (`target_type`, `target_id`),
  KEY `idx_pur_operation_audit_operator` (`operator_id`, `operation_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';



-- 枚举初始化数据
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (1, 'APPROVAL_STATUS', 'APPROVAL_STATUS', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (1, 'APPROVAL_STATUS', 1, '草稿', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (2, 'APPROVAL_STATUS', 2, '待审批', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (3, 'APPROVAL_STATUS', 3, '已批准', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (4, 'APPROVAL_STATUS', 4, '已驳回', 4, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (2, 'COMMON_STATUS', 'COMMON_STATUS', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (5, 'COMMON_STATUS', 1, '启用', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (6, 'COMMON_STATUS', 2, '停用', 2, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (3, 'COMPARE_STATUS', 'COMPARE_STATUS', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (7, 'COMPARE_STATUS', 1, '待比价', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (8, 'COMPARE_STATUS', 2, '已推荐', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (9, 'COMPARE_STATUS', 3, '已定标', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (10, 'COMPARE_STATUS', 4, '已驳回', 4, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (4, 'CONFIRM_PROCESS_STATUS', 'CONFIRM_PROCESS_STATUS', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (11, 'CONFIRM_PROCESS_STATUS', 1, '待处理', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (12, 'CONFIRM_PROCESS_STATUS', 2, '已接受', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (13, 'CONFIRM_PROCESS_STATUS', 3, '已重新协商', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (14, 'CONFIRM_PROCESS_STATUS', 4, '已关闭', 4, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (5, 'CONSUME_STATUS', 'CONSUME_STATUS', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (15, 'CONSUME_STATUS', 1, '待消费', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (16, 'CONSUME_STATUS', 2, '处理中', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (17, 'CONSUME_STATUS', 3, '消费成功', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (18, 'CONSUME_STATUS', 4, '消费失败', 4, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (19, 'CONSUME_STATUS', 5, '已忽略', 5, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (6, 'CURRENCY', 'CURRENCY', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (20, 'CURRENCY', 1, '人民币', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (21, 'CURRENCY', 2, '美元', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (22, 'CURRENCY', 3, '欧元', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (23, 'CURRENCY', 4, '港币', 4, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (24, 'CURRENCY', 5, '日元', 5, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (7, 'EFFECTIVE_STATUS', 'EFFECTIVE_STATUS', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (25, 'EFFECTIVE_STATUS', 1, '待生效', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (26, 'EFFECTIVE_STATUS', 2, '已生效', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (27, 'EFFECTIVE_STATUS', 3, '已作废', 3, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (8, 'EVENT_STATUS', 'EVENT_STATUS', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (28, 'EVENT_STATUS', 1, '待发布', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (29, 'EVENT_STATUS', 2, '发布中', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (30, 'EVENT_STATUS', 3, '已发布', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (31, 'EVENT_STATUS', 4, '发布失败', 4, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (32, 'EVENT_STATUS', 5, '已取消', 5, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (9, 'INBOUND_TRACK_STATUS', 'INBOUND_TRACK_STATUS', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (33, 'INBOUND_TRACK_STATUS', 1, '已通知', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (34, 'INBOUND_TRACK_STATUS', 2, '已到货', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (35, 'INBOUND_TRACK_STATUS', 3, '已收货', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (36, 'INBOUND_TRACK_STATUS', 4, '已质检', 4, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (37, 'INBOUND_TRACK_STATUS', 5, '已上架', 5, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (38, 'INBOUND_TRACK_STATUS', 6, '异常', 6, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (10, 'OPERATION_RESULT', 'OPERATION_RESULT', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (39, 'OPERATION_RESULT', 1, '成功', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (40, 'OPERATION_RESULT', 2, '失败', 2, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (11, 'PO_CHANGE_TYPE', 'PO_CHANGE_TYPE', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (41, 'PO_CHANGE_TYPE', 1, '数量', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (42, 'PO_CHANGE_TYPE', 2, '价格', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (43, 'PO_CHANGE_TYPE', 3, '交期', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (44, 'PO_CHANGE_TYPE', 4, '供应商', 4, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (45, 'PO_CHANGE_TYPE', 5, '取消', 5, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (46, 'PO_CHANGE_TYPE', 6, '关闭', 6, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (12, 'PO_DIFF_TYPE', 'PO_DIFF_TYPE', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (47, 'PO_DIFF_TYPE', 1, '数量', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (48, 'PO_DIFF_TYPE', 2, '交期', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (49, 'PO_DIFF_TYPE', 3, '价格', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (50, 'PO_DIFF_TYPE', 4, '其他', 4, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (13, 'PRICE_TYPE', 'PRICE_TYPE', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (51, 'PRICE_TYPE', 1, '标准价', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (52, 'PRICE_TYPE', 2, '协议价', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (53, 'PRICE_TYPE', 3, '临时价', 3, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (14, 'PURCHASE_ORDER_LINE_STATUS', 'PURCHASE_ORDER_LINE_STATUS', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (54, 'PURCHASE_ORDER_LINE_STATUS', 1, '草稿', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (55, 'PURCHASE_ORDER_LINE_STATUS', 2, '待确认', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (56, 'PURCHASE_ORDER_LINE_STATUS', 3, '已确认', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (57, 'PURCHASE_ORDER_LINE_STATUS', 4, '部分入库', 4, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (58, 'PURCHASE_ORDER_LINE_STATUS', 5, '已完成', 5, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (59, 'PURCHASE_ORDER_LINE_STATUS', 6, '已取消', 6, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (60, 'PURCHASE_ORDER_LINE_STATUS', 7, '已关闭', 7, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (15, 'PURCHASE_ORDER_STATUS', 'PURCHASE_ORDER_STATUS', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (61, 'PURCHASE_ORDER_STATUS', 1, '草稿', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (62, 'PURCHASE_ORDER_STATUS', 2, '待审批', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (63, 'PURCHASE_ORDER_STATUS', 3, '已审批', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (64, 'PURCHASE_ORDER_STATUS', 4, '待供应商确认', 4, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (65, 'PURCHASE_ORDER_STATUS', 5, '供应商已确认', 5, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (66, 'PURCHASE_ORDER_STATUS', 6, '供应商差异', 6, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (67, 'PURCHASE_ORDER_STATUS', 7, '部分入库', 7, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (68, 'PURCHASE_ORDER_STATUS', 8, '已完成', 8, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (69, 'PURCHASE_ORDER_STATUS', 9, '已取消', 9, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (70, 'PURCHASE_ORDER_STATUS', 10, '已关闭', 10, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (16, 'PURCHASE_TYPE', 'PURCHASE_TYPE', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (71, 'PURCHASE_TYPE', 1, '常规', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (72, 'PURCHASE_TYPE', 2, '紧急', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (73, 'PURCHASE_TYPE', 3, '补货', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (74, 'PURCHASE_TYPE', 4, '项目', 4, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (17, 'QUOTE_STATUS', 'QUOTE_STATUS', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (75, 'QUOTE_STATUS', 1, '草稿', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (76, 'QUOTE_STATUS', 2, '已提交', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (77, 'QUOTE_STATUS', 3, '已确认', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (78, 'QUOTE_STATUS', 4, '已作废', 4, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (79, 'QUOTE_STATUS', 5, '未中标', 5, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (80, 'QUOTE_STATUS', 6, '中标', 6, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (18, 'REQUISITION_LINE_STATUS', 'REQUISITION_LINE_STATUS', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (81, 'REQUISITION_LINE_STATUS', 1, '草稿', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (82, 'REQUISITION_LINE_STATUS', 2, '待审批', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (83, 'REQUISITION_LINE_STATUS', 3, '已批准', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (84, 'REQUISITION_LINE_STATUS', 4, '已转采购', 4, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (85, 'REQUISITION_LINE_STATUS', 5, '已关闭', 5, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (19, 'REQUISITION_STATUS', 'REQUISITION_STATUS', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (86, 'REQUISITION_STATUS', 1, '草稿', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (87, 'REQUISITION_STATUS', 2, '待审批', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (88, 'REQUISITION_STATUS', 3, '已批准', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (89, 'REQUISITION_STATUS', 4, '已转采购', 4, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (90, 'REQUISITION_STATUS', 5, '已关闭', 5, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (20, 'REQUISITION_TYPE', 'REQUISITION_TYPE', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (91, 'REQUISITION_TYPE', 1, '常规', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (92, 'REQUISITION_TYPE', 2, '紧急', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (93, 'REQUISITION_TYPE', 3, '项目', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (94, 'REQUISITION_TYPE', 4, '补货', 4, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (21, 'RFQ_STATUS', 'RFQ_STATUS', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (95, 'RFQ_STATUS', 1, '草稿', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (96, 'RFQ_STATUS', 2, '已发布', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (97, 'RFQ_STATUS', 3, '报价中', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (98, 'RFQ_STATUS', 4, '已截标', 4, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (99, 'RFQ_STATUS', 5, '已定标', 5, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (100, 'RFQ_STATUS', 6, '已取消', 6, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (101, 'RFQ_STATUS', 7, '已关闭', 7, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (22, 'RFQ_TYPE', 'RFQ_TYPE', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (102, 'RFQ_TYPE', 1, '公开询价', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (103, 'RFQ_TYPE', 2, '定向询价', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (104, 'RFQ_TYPE', 3, '议价', 3, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (23, 'SUPPLIER_CONFIRM_RESULT', 'SUPPLIER_CONFIRM_RESULT', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (105, 'SUPPLIER_CONFIRM_RESULT', 1, '确认', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (106, 'SUPPLIER_CONFIRM_RESULT', 2, '拒绝', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (107, 'SUPPLIER_CONFIRM_RESULT', 3, '差异', 3, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (24, 'SUPPLIER_CONFIRM_STATUS', 'SUPPLIER_CONFIRM_STATUS', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (108, 'SUPPLIER_CONFIRM_STATUS', 1, '待确认', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (109, 'SUPPLIER_CONFIRM_STATUS', 2, '已确认', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (110, 'SUPPLIER_CONFIRM_STATUS', 3, '差异', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (111, 'SUPPLIER_CONFIRM_STATUS', 4, '已拒绝', 4, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (25, 'SUPPLIER_RETURN_LINE_STATUS', 'SUPPLIER_RETURN_LINE_STATUS', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (112, 'SUPPLIER_RETURN_LINE_STATUS', 1, '草稿', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (113, 'SUPPLIER_RETURN_LINE_STATUS', 2, '待退货', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (114, 'SUPPLIER_RETURN_LINE_STATUS', 3, '退货中', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (115, 'SUPPLIER_RETURN_LINE_STATUS', 4, '已完成', 4, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (116, 'SUPPLIER_RETURN_LINE_STATUS', 5, '已取消', 5, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (26, 'SUPPLIER_RETURN_REASON', 'SUPPLIER_RETURN_REASON', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (117, 'SUPPLIER_RETURN_REASON', 1, '质检不合格', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (118, 'SUPPLIER_RETURN_REASON', 2, '错发', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (119, 'SUPPLIER_RETURN_REASON', 3, '超收', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (120, 'SUPPLIER_RETURN_REASON', 4, '包装破损', 4, 1);
INSERT INTO `pur_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (27, 'SUPPLIER_RETURN_STATUS', 'SUPPLIER_RETURN_STATUS', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (121, 'SUPPLIER_RETURN_STATUS', 1, '草稿', 1, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (122, 'SUPPLIER_RETURN_STATUS', 2, '待审批', 2, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (123, 'SUPPLIER_RETURN_STATUS', 3, '已批准', 3, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (124, 'SUPPLIER_RETURN_STATUS', 4, '已通知供应商', 4, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (125, 'SUPPLIER_RETURN_STATUS', 5, '退货中', 5, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (126, 'SUPPLIER_RETURN_STATUS', 6, '已完成', 6, 1);
INSERT INTO `pur_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (127, 'SUPPLIER_RETURN_STATUS', 7, '已取消', 7, 1);



-- 领域事件类型参考数据
-- 1. 采购申请已创建: 载荷 requisitionId、申请人、申请行摘要
-- 2. 采购申请已提交: 载荷 requisitionId、提交人、预算摘要
-- 3. 采购申请已批准: 载荷 requisitionId、批准数量、批准金额
-- 4. 采购申请已驳回: 载荷 requisitionId、驳回原因
-- 5. 采购申请已转采购: 载荷 requisitionId、转采购方式、目标单据
-- 6. 采购申请已关闭: 载荷 requisitionId、关闭原因
-- 7. SKU已启用: 载荷 更新SKU可请购快照
-- 8. SKU已停用: 载荷 未转采购申请行生成异常提示
-- 9. 预算已冻结: 载荷 标记预算已占用，允许继续审批
-- 10. 采购订单已创建: 载荷 记录请购已被PO引用
-- 11. 询价单已创建: 载荷 rfqId、询价行、来源
-- 12. 询价单已发布: 载荷 rfqId、邀请供应商、截止时间
-- 13. 询价单已修改: 载荷 rfqId、变更字段
-- 14. 询价已截标: 载荷 rfqId、截标时间
-- 15. 询价单已取消: 载荷 rfqId、取消原因
-- 16. 询价单已关闭: 载荷 rfqId、关闭原因
-- 17. 供应商商品已启用: 载荷 更新可邀请供应商快照
-- 18. 供应商已冻结: 载荷 未发布询价移除或标记供应商不可邀请
-- 19. 供应商报价已提交: 载荷 更新邀请供应商报价状态
-- 20. 供应商报价已创建: 载荷 供应商报价ID、业务状态、关键明细摘要
-- 21. 供应商报价已确认: 载荷 供应商报价ID、业务状态、关键明细摘要
-- 22. 供应商报价已中标: 载荷 供应商报价ID、业务状态、关键明细摘要
-- 23. 供应商报价已失标: 载荷 供应商报价ID、业务状态、关键明细摘要
-- 24. 比价已生成: 载荷 比价结果ID、业务状态、关键明细摘要
-- 25. 供应商已推荐: 载荷 比价结果ID、业务状态、关键明细摘要
-- 26. 比价已定标: 载荷 比价结果ID、业务状态、关键明细摘要
-- 27. 定标已驳回: 载荷 比价结果ID、业务状态、关键明细摘要
-- 28. 采购价格已创建: 载荷 采购价格ID、业务状态、关键明细摘要
-- 29. 采购价格已提交审批: 载荷 采购价格ID、业务状态、关键明细摘要
-- 30. 采购价格已生效: 载荷 采购价格ID、业务状态、关键明细摘要
-- 31. 采购价格已失效: 载荷 采购价格ID、业务状态、关键明细摘要
-- 32. 采购订单已提交: 载荷 采购订单ID、业务状态、关键明细摘要
-- 33. 采购订单已批准: 载荷 采购订单ID、业务状态、关键明细摘要
-- 34. 采购订单已发布: 载荷 采购订单ID、业务状态、关键明细摘要
-- 35. 采购订单已部分入库: 载荷 采购订单ID、业务状态、关键明细摘要
-- 36. 采购订单已关闭: 载荷 采购订单ID、业务状态、关键明细摘要
-- 37. 供应商订单已确认: 载荷 记录供应商确认结果并推进待到货
-- 38. 采购订单变更已创建: 载荷 采购订单变更ID、业务状态、关键明细摘要
-- 39. 采购订单变更已提交: 载荷 采购订单变更ID、业务状态、关键明细摘要
-- 40. 采购订单变更已批准: 载荷 采购订单变更ID、业务状态、关键明细摘要
-- 41. 采购订单变更已生效: 载荷 采购订单变更ID、业务状态、关键明细摘要
-- 42. 采购订单变更已作废: 载荷 采购订单变更ID、业务状态、关键明细摘要
-- 43. 采购ASN已记录: 载荷 入库跟踪ID、业务状态、关键明细摘要
-- 44. 采购货品已收货: 载荷 入库跟踪ID、业务状态、关键明细摘要
-- 45. 采购质检已完成: 载荷 入库跟踪ID、业务状态、关键明细摘要
-- 46. 采购货品已上架: 载荷 入库跟踪ID、业务状态、关键明细摘要
-- 47. 采购入库异常已产生: 载荷 入库跟踪ID、业务状态、关键明细摘要
-- 48. ASN已提交: 载荷 创建或更新到货跟踪记录
-- 49. 退供申请已创建: 载荷 退供申请ID、业务状态、关键明细摘要
-- 50. 退供申请已提交: 载荷 退供申请ID、业务状态、关键明细摘要
-- 51. 退供申请已批准: 载荷 退供申请ID、业务状态、关键明细摘要
-- 52. 退供申请已通知执行: 载荷 退供申请ID、业务状态、关键明细摘要
-- 53. 退供申请已关闭: 载荷 退供申请ID、业务状态、关键明细摘要
-- 54. 质检已完成: 载荷 不合格时创建退供候选申请



-- 业务表

CREATE TABLE `pur_requisition` (
  `requisition_id` bigint NOT NULL COMMENT '采购申请主键',
  `requisition_no` varchar(64) NOT NULL COMMENT '请购单号',
  `requisition_type` smallint NOT NULL COMMENT '常规、紧急、项目、补货',
  `expected_arrival_date` date NULL COMMENT '期望到货日期',
  `budget_amount` decimal(18,2) NULL COMMENT '预算金额',
  `currency` smallint NOT NULL COMMENT '币种',
  `purpose` varchar(512) NULL COMMENT '采购用途',
  `approval_status` smallint NOT NULL COMMENT '草稿、待审批、已批准、已驳回',
  `requisition_status` smallint NOT NULL COMMENT '草稿、待审批、已批准、已转采购、已关闭',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`requisition_id`),
  KEY `idx_pur_requisition_status_time` (`approval_status`, `updated_at`),
  KEY `idx_pur_requisition_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购申请';

CREATE TABLE `pur_requisition_line` (
  `requisition_line_id` bigint NOT NULL COMMENT '采购申请行主键',
  `sku_code` varchar(64) NOT NULL COMMENT 'SKU 编码快照',
  `sku_name` varchar(256) NOT NULL COMMENT 'SKU 名称快照',
  `request_qty` decimal(18,4) NOT NULL COMMENT '申请数量',
  `approved_qty` decimal(18,4) NULL COMMENT '批准数量',
  `uom` varchar(32) NOT NULL COMMENT '单位',
  `line_status` smallint NOT NULL COMMENT '草稿、待审批、已批准、已转采购、已关闭',
  `remark` varchar(512) NULL COMMENT '备注',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`requisition_line_id`),
  KEY `idx_pur_requisition_line_status_time` (`line_status`, `updated_at`),
  KEY `idx_pur_requisition_line_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购申请行';

CREATE TABLE `pur_rfq` (
  `rfq_id` bigint NOT NULL COMMENT '询价单主键',
  `rfq_no` varchar(64) NOT NULL COMMENT '询价单号',
  `rfq_type` smallint NOT NULL COMMENT '公开询价、定向询价、议价',
  `supplier_ids` text NOT NULL COMMENT '邀请供应商',
  `quote_deadline` datetime NOT NULL COMMENT '报价截止时间',
  `rfq_status` smallint NOT NULL COMMENT '草稿、已发布、报价中、已截标、已定标、已取消、已关闭',
  `published_at` datetime NULL COMMENT '发布时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`rfq_id`),
  KEY `idx_pur_rfq_status_time` (`rfq_status`, `updated_at`),
  KEY `idx_pur_rfq_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='询价单';

CREATE TABLE `pur_rfq_line` (
  `rfq_line_id` bigint NOT NULL COMMENT '询价行主键',
  `target_qty` decimal(18,4) NOT NULL COMMENT '询价数量',
  `uom` varchar(32) NOT NULL COMMENT '单位',
  `required_delivery_date` date NULL COMMENT '要求交期',
  `quality_requirement` varchar(512) NULL COMMENT '质量要求',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`rfq_line_id`),
  KEY `idx_pur_rfq_line_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='询价行';

CREATE TABLE `pur_supplier_quote` (
  `supplier_quote_id` bigint NOT NULL COMMENT '供应商报价主键',
  `quotation_no` varchar(64) NOT NULL COMMENT '报价单号',
  `quote_status` smallint NOT NULL COMMENT '草稿、已提交、已确认、已作废、未中标、中标',
  `total_amount` decimal(18,2) NULL COMMENT '报价总额',
  `currency` smallint NOT NULL COMMENT '币种',
  `valid_from` date NULL COMMENT '有效开始',
  `valid_to` date NULL COMMENT '有效结束',
  `submitted_at` datetime NULL COMMENT '提交时间',
  `attachment_url` varchar(512) NULL COMMENT '报价附件',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`supplier_quote_id`),
  KEY `idx_pur_supplier_quote_status_time` (`quote_status`, `updated_at`),
  KEY `idx_pur_supplier_quote_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商报价';

CREATE TABLE `pur_supplier_quote_line` (
  `supplier_quote_line_id` bigint NOT NULL COMMENT '供应商报价行主键',
  `quote_qty` decimal(18,4) NOT NULL COMMENT '报价数量',
  `unit_price` decimal(18,6) NOT NULL COMMENT '未税单价',
  `tax_rate` decimal(8,4) NOT NULL COMMENT '税率',
  `tax_included_price` decimal(18,6) NULL COMMENT '含税单价',
  `delivery_days` int NULL COMMENT '承诺交期天数',
  `moq` decimal(18,4) NULL COMMENT '最小起订量',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`supplier_quote_line_id`),
  KEY `idx_pur_supplier_quote_line_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商报价行';

CREATE TABLE `pur_compare_result` (
  `compare_result_id` bigint NOT NULL COMMENT '比价结果主键',
  `compare_no` varchar(64) NOT NULL COMMENT '比价单号',
  `decision_reason` varchar(512) NULL COMMENT '定标理由',
  `compare_status` smallint NOT NULL COMMENT '待比价、已推荐、已定标、已驳回',
  `decided_by` bigint NULL COMMENT '定标人',
  `decided_at` datetime NULL COMMENT '定标时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`compare_result_id`),
  KEY `idx_pur_compare_result_status_time` (`compare_status`, `updated_at`),
  KEY `idx_pur_compare_result_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='比价结果';

CREATE TABLE `pur_price` (
  `price_id` bigint NOT NULL COMMENT '采购价格主键',
  `price_type` smallint NOT NULL COMMENT '标准价、协议价、临时价',
  `unit_price` decimal(18,6) NOT NULL COMMENT '未税单价',
  `tax_rate` decimal(8,4) NOT NULL COMMENT '税率',
  `tax_included_price` decimal(18,6) NOT NULL COMMENT '含税单价',
  `currency` smallint NOT NULL COMMENT '币种',
  `effective_from` date NOT NULL COMMENT '生效日期',
  `effective_to` date NULL COMMENT '失效日期',
  `status` smallint NOT NULL COMMENT '启用、停用',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`price_id`),
  KEY `idx_pur_price_status_time` (`status`, `updated_at`),
  KEY `idx_pur_price_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购价格';

CREATE TABLE `pur_order` (
  `order_id` bigint NOT NULL COMMENT '采购订单主键',
  `purchase_order_no` varchar(64) NOT NULL COMMENT '采购订单号',
  `purchase_type` smallint NOT NULL COMMENT '常规、紧急、补货、项目',
  `supplier_code` varchar(64) NOT NULL COMMENT '供应商编码',
  `supplier_name` varchar(256) NOT NULL COMMENT '供应商名称',
  `currency` smallint NOT NULL COMMENT '币种',
  `total_amount` decimal(18,2) NOT NULL COMMENT '未税总额',
  `tax_amount` decimal(18,2) NOT NULL COMMENT '税额',
  `tax_included_amount` decimal(18,2) NOT NULL COMMENT '含税总额',
  `approval_status` smallint NOT NULL COMMENT '草稿、待审批、已批准、已驳回',
  `po_status` smallint NOT NULL COMMENT '草稿、待审批、已审批、待供应商确认、供应商已确认、供应商差异、部分入库、已完成、已取消、已关闭',
  `confirm_status` smallint NOT NULL COMMENT '待确认、已确认、差异、已拒绝',
  `version_no` int NOT NULL COMMENT '版本号',
  `released_at` datetime NULL COMMENT '发布时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`order_id`),
  KEY `idx_pur_order_status_time` (`approval_status`, `updated_at`),
  KEY `idx_pur_order_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购订单';

CREATE TABLE `pur_order_line` (
  `order_line_id` bigint NOT NULL COMMENT '采购订单行主键',
  `sku_code` varchar(64) NOT NULL COMMENT 'SKU 编码',
  `sku_name` varchar(256) NOT NULL COMMENT 'SKU 名称',
  `order_qty` decimal(18,4) NOT NULL COMMENT '采购数量',
  `confirmed_qty` decimal(18,4) NULL COMMENT '供应商确认数量',
  `notified_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT 'ASN 通知数量',
  `received_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '收货数量',
  `qualified_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '合格数量',
  `unqualified_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '不合格数量',
  `putaway_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '上架完成数量',
  `unit_price` decimal(18,6) NOT NULL COMMENT '未税单价',
  `tax_rate` decimal(8,4) NOT NULL COMMENT '税率',
  `tax_included_price` decimal(18,6) NOT NULL COMMENT '含税单价',
  `required_delivery_date` date NULL COMMENT '要求交期',
  `confirmed_delivery_date` date NULL COMMENT '确认交期',
  `line_status` smallint NOT NULL COMMENT '草稿、待确认、已确认、部分入库、已完成、已取消、已关闭',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`order_line_id`),
  KEY `idx_pur_order_line_status_time` (`line_status`, `updated_at`),
  KEY `idx_pur_order_line_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购订单行';

CREATE TABLE `pur_supplier_confirm_result` (
  `supplier_confirm_result_id` bigint NOT NULL COMMENT '供应商确认结果主键',
  `confirm_result` smallint NOT NULL COMMENT '确认、拒绝、差异',
  `diff_type` smallint NULL COMMENT '数量、交期、价格、其他',
  `diff_detail` varchar(1024) NULL COMMENT '差异说明',
  `processed_status` smallint NOT NULL COMMENT '待处理、已接受、已重新协商、已关闭',
  `processed_at` datetime NULL COMMENT '处理时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`supplier_confirm_result_id`),
  KEY `idx_pur_supplier_confirm_result_status_time` (`processed_status`, `updated_at`),
  KEY `idx_pur_supplier_confirm_result_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商确认结果';

CREATE TABLE `pur_order_change` (
  `order_id` bigint NOT NULL COMMENT '采购订单变更主键',
  `change_no` varchar(64) NOT NULL COMMENT '变更单号',
  `change_type` smallint NOT NULL COMMENT '数量、价格、交期、供应商、取消、关闭',
  `before_snapshot` text NOT NULL COMMENT '变更前摘要',
  `after_snapshot` text NOT NULL COMMENT '变更后摘要',
  `change_reason` varchar(512) NOT NULL COMMENT '变更原因',
  `approval_status` smallint NOT NULL COMMENT '草稿、待审批、已批准、已驳回',
  `effective_status` smallint NOT NULL COMMENT '待生效、已生效、已作废',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`order_id`),
  KEY `idx_pur_order_change_status_time` (`approval_status`, `updated_at`),
  KEY `idx_pur_order_change_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购订单变更';

CREATE TABLE `pur_inbound` (
  `inbound_id` bigint NOT NULL COMMENT '入库跟踪主键',
  `asn_no` varchar(64) NULL COMMENT 'ASN 单号',
  `inbound_order_no` varchar(64) NULL COMMENT 'WMS 入库单号',
  `notified_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '通知数量',
  `received_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '收货数量',
  `qualified_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '合格数量',
  `unqualified_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '不合格数量',
  `putaway_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '上架数量',
  `track_status` smallint NOT NULL COMMENT '已通知、已到货、已收货、已质检、已上架、异常',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`inbound_id`),
  KEY `idx_pur_inbound_status_time` (`track_status`, `updated_at`),
  KEY `idx_pur_inbound_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入库跟踪';

CREATE TABLE `pur_supplier_return` (
  `supplier_return_id` bigint NOT NULL COMMENT '退供申请主键',
  `supplier_return_no` varchar(64) NOT NULL COMMENT '退供申请号',
  `return_reason` smallint NOT NULL COMMENT '质检不合格、错发、超收、包装破损等',
  `return_status` smallint NOT NULL COMMENT '草稿、待审批、已批准、已通知供应商、退货中、已完成、已取消',
  `approval_status` smallint NOT NULL COMMENT '草稿、待审批、已批准、已驳回',
  `approved_at` datetime NULL COMMENT '审批时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`supplier_return_id`),
  KEY `idx_pur_supplier_return_status_time` (`return_status`, `updated_at`),
  KEY `idx_pur_supplier_return_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退供申请';

CREATE TABLE `pur_supplier_return_line` (
  `supplier_return_line_id` bigint NOT NULL COMMENT '退供申请行主键',
  `sku_code` varchar(64) NOT NULL COMMENT 'SKU 编码',
  `return_qty` decimal(18,4) NOT NULL COMMENT '退供数量',
  `batch_no` varchar(128) NULL COMMENT '批次号',
  `line_status` smallint NOT NULL COMMENT '草稿、待退货、退货中、已完成、已取消',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`supplier_return_line_id`),
  KEY `idx_pur_supplier_return_line_status_time` (`line_status`, `updated_at`),
  KEY `idx_pur_supplier_return_line_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退供申请行';



SET FOREIGN_KEY_CHECKS = 1;



-- source: docs/05-子系统数据库设计/ddl/03-WMS系统.sql

-- WMS系统 数据库 DDL

-- 口径：MySQL 8.0 / InnoDB / utf8mb4

SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;



CREATE TABLE `wms_enum_type` (
  `enum_type_id` bigint NOT NULL COMMENT '枚举类型ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_type_name` varchar(128) NOT NULL COMMENT '枚举类型名称',
  `configurable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许页面配置',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_type_id`),
  UNIQUE KEY `uk_wms_enum_type_code` (`enum_type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='枚举类型';

CREATE TABLE `wms_enum_item` (
  `enum_item_id` bigint NOT NULL COMMENT '枚举项ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_value` smallint NOT NULL COMMENT '枚举数值，从1开始',
  `enum_label` varchar(128) NOT NULL COMMENT '枚举显示名',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_item_id`),
  UNIQUE KEY `uk_wms_enum_item` (`enum_type_code`, `enum_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='枚举项';

CREATE TABLE `wms_domain_event` (
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
  UNIQUE KEY `uk_wms_domain_event_code` (`event_code`),
  KEY `idx_wms_domain_event_status` (`event_status`, `created_at`),
  KEY `idx_wms_domain_event_aggregate` (`aggregate_type`, `aggregate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='领域事件发布表';

CREATE TABLE `wms_event_consume_log` (
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
  UNIQUE KEY `uk_wms_event_consume` (`source_system`, `event_code`, `consumer_name`),
  KEY `idx_wms_event_consume_status` (`consume_status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='事件消费幂等日志';

CREATE TABLE `wms_operation_audit_log` (
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
  KEY `idx_wms_operation_audit_target` (`target_type`, `target_id`),
  KEY `idx_wms_operation_audit_operator` (`operator_id`, `operation_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';



-- 枚举初始化数据
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (1, 'APPROVAL_STATUS', 'APPROVAL_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (1, 'APPROVAL_STATUS', 1, '草稿', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (2, 'APPROVAL_STATUS', 2, '待审批', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (3, 'APPROVAL_STATUS', 3, '已批准', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (4, 'APPROVAL_STATUS', 4, '已驳回', 4, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (2, 'COMMON_STATUS', 'COMMON_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (5, 'COMMON_STATUS', 1, '启用', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (6, 'COMMON_STATUS', 2, '停用', 2, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (3, 'CONSUME_STATUS', 'CONSUME_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (7, 'CONSUME_STATUS', 1, '待消费', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (8, 'CONSUME_STATUS', 2, '处理中', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (9, 'CONSUME_STATUS', 3, '消费成功', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (10, 'CONSUME_STATUS', 4, '消费失败', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (11, 'CONSUME_STATUS', 5, '已忽略', 5, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (4, 'CONTAINER_BIND_TYPE', 'CONTAINER_BIND_TYPE', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (12, 'CONTAINER_BIND_TYPE', 1, '拣货单', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (13, 'CONTAINER_BIND_TYPE', 2, '出库单', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (14, 'CONTAINER_BIND_TYPE', 3, '包裹', 3, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (5, 'CONTAINER_STATUS', 'CONTAINER_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (15, 'CONTAINER_STATUS', 1, '空闲', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (16, 'CONTAINER_STATUS', 2, '已绑定', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (17, 'CONTAINER_STATUS', 3, '拣货中', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (18, 'CONTAINER_STATUS', 4, '待复核', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (19, 'CONTAINER_STATUS', 5, '已清空', 5, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (20, 'CONTAINER_STATUS', 6, '停用', 6, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (6, 'CONTAINER_TYPE', 'CONTAINER_TYPE', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (21, 'CONTAINER_TYPE', 1, '周转箱', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (22, 'CONTAINER_TYPE', 2, '播种车', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (23, 'CONTAINER_TYPE', 3, '格口', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (24, 'CONTAINER_TYPE', 4, '托盘', 4, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (7, 'COUNT_PLAN_STATUS', 'COUNT_PLAN_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (25, 'COUNT_PLAN_STATUS', 1, '草稿', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (26, 'COUNT_PLAN_STATUS', 2, '已下发', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (27, 'COUNT_PLAN_STATUS', 3, '盘点中', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (28, 'COUNT_PLAN_STATUS', 4, '差异处理中', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (29, 'COUNT_PLAN_STATUS', 5, '已完成', 5, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (30, 'COUNT_PLAN_STATUS', 6, '已取消', 6, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (8, 'COUNT_TYPE', 'COUNT_TYPE', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (31, 'COUNT_TYPE', 1, '全盘', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (32, 'COUNT_TYPE', 2, '动盘', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (33, 'COUNT_TYPE', 3, '循环盘', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (34, 'COUNT_TYPE', 4, '抽盘', 4, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (9, 'CURRENCY', 'CURRENCY', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (35, 'CURRENCY', 1, '人民币', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (36, 'CURRENCY', 2, '美元', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (37, 'CURRENCY', 3, '欧元', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (38, 'CURRENCY', 4, '港币', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (39, 'CURRENCY', 5, '日元', 5, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (10, 'EVENT_STATUS', 'EVENT_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (40, 'EVENT_STATUS', 1, '待发布', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (41, 'EVENT_STATUS', 2, '发布中', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (42, 'EVENT_STATUS', 3, '已发布', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (43, 'EVENT_STATUS', 4, '发布失败', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (44, 'EVENT_STATUS', 5, '已取消', 5, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (11, 'EXCEPTION_STATUS', 'EXCEPTION_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (45, 'EXCEPTION_STATUS', 1, '待处理', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (46, 'EXCEPTION_STATUS', 2, '处理中', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (47, 'EXCEPTION_STATUS', 3, '已关闭', 3, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (12, 'INBOUND_SOURCE_TYPE', 'INBOUND_SOURCE_TYPE', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (48, 'INBOUND_SOURCE_TYPE', 1, '采购', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (49, 'INBOUND_SOURCE_TYPE', 2, '调拨', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (50, 'INBOUND_SOURCE_TYPE', 3, '销售退货', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (51, 'INBOUND_SOURCE_TYPE', 4, '其他', 4, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (13, 'INBOUND_STATUS', 'INBOUND_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (52, 'INBOUND_STATUS', 1, '待到货', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (53, 'INBOUND_STATUS', 2, '到货中', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (54, 'INBOUND_STATUS', 3, '收货中', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (55, 'INBOUND_STATUS', 4, '待质检', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (56, 'INBOUND_STATUS', 5, '待上架', 5, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (57, 'INBOUND_STATUS', 6, '部分上架', 6, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (58, 'INBOUND_STATUS', 7, '已上架', 7, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (59, 'INBOUND_STATUS', 8, '已关闭', 8, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (60, 'INBOUND_STATUS', 9, '已取消', 9, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (14, 'OPERATION_RESULT', 'OPERATION_RESULT', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (61, 'OPERATION_RESULT', 1, '成功', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (62, 'OPERATION_RESULT', 2, '失败', 2, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (15, 'OUTBOUND_SOURCE_TYPE', 'OUTBOUND_SOURCE_TYPE', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (63, 'OUTBOUND_SOURCE_TYPE', 1, '销售', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (64, 'OUTBOUND_SOURCE_TYPE', 2, '调拨', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (65, 'OUTBOUND_SOURCE_TYPE', 3, '退供', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (66, 'OUTBOUND_SOURCE_TYPE', 4, '其他', 4, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (16, 'PACKAGE_STATUS', 'PACKAGE_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (67, 'PACKAGE_STATUS', 1, '已打包', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (68, 'PACKAGE_STATUS', 2, '待交接', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (69, 'PACKAGE_STATUS', 3, '已交接', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (70, 'PACKAGE_STATUS', 4, '已取消', 4, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (17, 'PACK_STATUS', 'PACK_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (71, 'PACK_STATUS', 1, '待复核', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (72, 'PACK_STATUS', 2, '复核中', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (73, 'PACK_STATUS', 3, '复核异常', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (74, 'PACK_STATUS', 4, '待打包', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (75, 'PACK_STATUS', 5, '已打包', 5, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (76, 'PACK_STATUS', 6, '已关闭', 6, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (18, 'PICKING_MODE', 'PICKING_MODE', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (77, 'PICKING_MODE', 1, '单单拣', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (78, 'PICKING_MODE', 2, '批量拣', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (79, 'PICKING_MODE', 3, '边拣边分', 3, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (19, 'PICKING_STATUS', 'PICKING_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (80, 'PICKING_STATUS', 1, '待分配', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (81, 'PICKING_STATUS', 2, '待拣货', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (82, 'PICKING_STATUS', 3, '拣货中', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (83, 'PICKING_STATUS', 4, '部分拣货', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (84, 'PICKING_STATUS', 5, '拣货异常', 5, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (85, 'PICKING_STATUS', 6, '已拣货', 6, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (86, 'PICKING_STATUS', 7, '已交接复核', 7, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (20, 'PICKING_TASK_STATUS', 'PICKING_TASK_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (87, 'PICKING_TASK_STATUS', 1, '待拣货', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (88, 'PICKING_TASK_STATUS', 2, '拣货中', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (89, 'PICKING_TASK_STATUS', 3, '已完成', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (90, 'PICKING_TASK_STATUS', 4, '异常', 4, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (21, 'PUTAWAY_TASK_STATUS', 'PUTAWAY_TASK_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (91, 'PUTAWAY_TASK_STATUS', 1, '待上架', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (92, 'PUTAWAY_TASK_STATUS', 2, '上架中', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (93, 'PUTAWAY_TASK_STATUS', 3, '部分上架', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (94, 'PUTAWAY_TASK_STATUS', 4, '已完成', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (95, 'PUTAWAY_TASK_STATUS', 5, '异常', 5, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (22, 'QC_RESULT', 'QC_RESULT', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (96, 'QC_RESULT', 1, '待检', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (97, 'QC_RESULT', 2, '合格', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (98, 'QC_RESULT', 3, '不合格', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (99, 'QC_RESULT', 4, '部分合格', 4, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (23, 'QUALITY_STATUS', 'QUALITY_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (100, 'QUALITY_STATUS', 1, '待检', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (101, 'QUALITY_STATUS', 2, '合格', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (102, 'QUALITY_STATUS', 3, '不合格', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (103, 'QUALITY_STATUS', 4, '免检', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (104, 'QUALITY_STATUS', 5, '待处理', 5, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (24, 'RECEIPT_STATUS', 'RECEIPT_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (105, 'RECEIPT_STATUS', 1, '待收货', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (106, 'RECEIPT_STATUS', 2, '收货中', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (107, 'RECEIPT_STATUS', 3, '部分收货', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (108, 'RECEIPT_STATUS', 4, '已收货', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (109, 'RECEIPT_STATUS', 5, '异常', 5, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (110, 'RECEIPT_STATUS', 6, '已关闭', 6, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (25, 'RESPONSIBLE_PARTY', 'RESPONSIBLE_PARTY', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (111, 'RESPONSIBLE_PARTY', 1, '供应商', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (112, 'RESPONSIBLE_PARTY', 2, '仓库', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (113, 'RESPONSIBLE_PARTY', 3, '物流', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (114, 'RESPONSIBLE_PARTY', 4, '客户', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (115, 'RESPONSIBLE_PARTY', 5, '系统', 5, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (26, 'WAVE_STATUS', 'WAVE_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (116, 'WAVE_STATUS', 1, '草稿', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (117, 'WAVE_STATUS', 2, '已释放', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (118, 'WAVE_STATUS', 3, '拣货中', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (119, 'WAVE_STATUS', 4, '已完成', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (120, 'WAVE_STATUS', 5, '已取消', 5, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (27, 'WAVE_TYPE', 'WAVE_TYPE', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (121, 'WAVE_TYPE', 1, '单单拣', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (122, 'WAVE_TYPE', 2, '批量拣', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (123, 'WAVE_TYPE', 3, '边拣边分', 3, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (28, 'WMS_EXCEPTION_SOURCE', 'WMS_EXCEPTION_SOURCE', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (124, 'WMS_EXCEPTION_SOURCE', 1, '收货', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (125, 'WMS_EXCEPTION_SOURCE', 2, '质检', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (126, 'WMS_EXCEPTION_SOURCE', 3, '上架', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (127, 'WMS_EXCEPTION_SOURCE', 4, '分配', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (128, 'WMS_EXCEPTION_SOURCE', 5, '拣货', 5, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (129, 'WMS_EXCEPTION_SOURCE', 6, '复核', 6, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (130, 'WMS_EXCEPTION_SOURCE', 7, '发货', 7, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (131, 'WMS_EXCEPTION_SOURCE', 8, '盘点', 8, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (29, 'WMS_EXCEPTION_TYPE', 'WMS_EXCEPTION_TYPE', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (132, 'WMS_EXCEPTION_TYPE', 1, '短收', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (133, 'WMS_EXCEPTION_TYPE', 2, '超收', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (134, 'WMS_EXCEPTION_TYPE', 3, '错货', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (135, 'WMS_EXCEPTION_TYPE', 4, '破损', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (136, 'WMS_EXCEPTION_TYPE', 5, '库位不符', 5, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (137, 'WMS_EXCEPTION_TYPE', 6, '复核差异', 6, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (138, 'WMS_EXCEPTION_TYPE', 7, '盘点差异', 7, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (30, 'WMS_OUTBOUND_LINE_STATUS', 'WMS_OUTBOUND_LINE_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (139, 'WMS_OUTBOUND_LINE_STATUS', 1, '待分配', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (140, 'WMS_OUTBOUND_LINE_STATUS', 2, '已分配', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (141, 'WMS_OUTBOUND_LINE_STATUS', 3, '拣货中', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (142, 'WMS_OUTBOUND_LINE_STATUS', 4, '已拣货', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (143, 'WMS_OUTBOUND_LINE_STATUS', 5, '已发货', 5, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (144, 'WMS_OUTBOUND_LINE_STATUS', 6, '已取消', 6, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (31, 'WMS_OUTBOUND_STATUS', 'WMS_OUTBOUND_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (145, 'WMS_OUTBOUND_STATUS', 1, '待接单', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (146, 'WMS_OUTBOUND_STATUS', 2, '待分配', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (147, 'WMS_OUTBOUND_STATUS', 3, '分配失败', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (148, 'WMS_OUTBOUND_STATUS', 4, '待拣货', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (149, 'WMS_OUTBOUND_STATUS', 5, '拣货中', 5, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (150, 'WMS_OUTBOUND_STATUS', 6, '待复核', 6, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (151, 'WMS_OUTBOUND_STATUS', 7, '复核异常', 7, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (152, 'WMS_OUTBOUND_STATUS', 8, '待发货', 8, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (153, 'WMS_OUTBOUND_STATUS', 9, '已发货', 9, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (154, 'WMS_OUTBOUND_STATUS', 10, '已关闭', 10, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (155, 'WMS_OUTBOUND_STATUS', 11, '已取消', 11, 1);
INSERT INTO `wms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (32, 'WMS_STOCK_STATUS', 'WMS_STOCK_STATUS', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (156, 'WMS_STOCK_STATUS', 1, '可拣', 1, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (157, 'WMS_STOCK_STATUS', 2, '冻结', 2, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (158, 'WMS_STOCK_STATUS', 3, '不合格', 3, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (159, 'WMS_STOCK_STATUS', 4, '待退供', 4, 1);
INSERT INTO `wms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (160, 'WMS_STOCK_STATUS', 5, '待报废', 5, 1);



-- 领域事件类型参考数据
-- 1. 入库单已接收: 载荷 入库单ID、仓库、SKU、批次、库位、数量、状态
-- 2. 到货已登记: 载荷 入库单ID、仓库、SKU、批次、库位、数量、状态
-- 3. 入库单已开始收货: 载荷 入库单ID、仓库、SKU、批次、库位、数量、状态
-- 4. 入库单已取消: 载荷 入库单ID、仓库、SKU、批次、库位、数量、状态
-- 5. 入库单已关闭: 载荷 入库单ID、仓库、SKU、批次、库位、数量、状态
-- 6. ASN已提交: 载荷 创建或更新入库单来源快照
-- 7. SKU已停用: 载荷 标记相关作业明细不可继续执行并生成异常
-- 8. 库位已更新: 载荷 刷新库位用途、容量、启停用和质量状态限制
-- 9. 仓库已停用: 载荷 阻断新作业接单并保留已开始作业处理入口
-- 10. 收货已开始: 载荷 收货单ID、仓库、SKU、批次、库位、数量、状态
-- 11. 收货明细已扫描: 载荷 收货单ID、仓库、SKU、批次、库位、数量、状态
-- 12. 收货差异已记录: 载荷 收货单ID、仓库、SKU、批次、库位、数量、状态
-- 13. 收货已完成: 载荷 收货单ID、仓库、SKU、批次、库位、数量、状态
-- 14. 收货单已关闭: 载荷 收货单ID、仓库、SKU、批次、库位、数量、状态
-- 15. 质检单已创建: 载荷 质检单ID、仓库、SKU、批次、库位、数量、状态
-- 16. 质检已开始: 载荷 质检单ID、仓库、SKU、批次、库位、数量、状态
-- 17. 质检已完成: 载荷 质检单ID、仓库、SKU、批次、库位、数量、状态
-- 18. 不合格品已判定: 载荷 质检单ID、仓库、SKU、批次、库位、数量、状态
-- 19. 质检单已关闭: 载荷 质检单ID、仓库、SKU、批次、库位、数量、状态
-- 20. 上架任务已创建: 载荷 上架任务ID、仓库、SKU、批次、库位、数量、状态
-- 21. 上架任务已领取: 载荷 上架任务ID、仓库、SKU、批次、库位、数量、状态
-- 22. 上架已完成: 载荷 上架任务ID、仓库、SKU、批次、库位、数量、状态
-- 23. 不合格品已暂存: 载荷 上架任务ID、仓库、SKU、批次、库位、数量、状态
-- 24. 上架异常已创建: 载荷 上架任务ID、仓库、SKU、批次、库位、数量、状态
-- 25. 库内库存已增加: 载荷 库内库存ID、仓库、SKU、批次、库位、数量、状态
-- 26. 库内库存已移动: 载荷 库内库存ID、仓库、SKU、批次、库位、数量、状态
-- 27. 库内库存已冻结: 载荷 库内库存ID、仓库、SKU、批次、库位、数量、状态
-- 28. 库内库存已解冻: 载荷 库内库存ID、仓库、SKU、批次、库位、数量、状态
-- 29. 库内库存已调整: 载荷 库内库存ID、仓库、SKU、批次、库位、数量、状态
-- 30. 出库单已接收: 载荷 出库单ID、仓库、SKU、批次、库位、数量、状态
-- 31. 出库已分配: 载荷 出库单ID、仓库、SKU、批次、库位、数量、状态
-- 32. 出库已发货: 载荷 出库单ID、仓库、SKU、批次、库位、数量、状态
-- 33. 出库单已取消: 载荷 出库单ID、仓库、SKU、批次、库位、数量、状态
-- 34. 库存已预占: 载荷 记录预占结果并允许分配库位
-- 35. 波次已创建: 载荷 波次单ID、仓库、SKU、批次、库位、数量、状态
-- 36. 波次已释放: 载荷 波次单ID、仓库、SKU、批次、库位、数量、状态
-- 37. 波次拣货单已生成: 载荷 波次单ID、仓库、SKU、批次、库位、数量、状态
-- 38. 波次已完成: 载荷 波次单ID、仓库、SKU、批次、库位、数量、状态
-- 39. 波次已取消: 载荷 波次单ID、仓库、SKU、批次、库位、数量、状态
-- 40. 拣货单已创建: 载荷 拣货单ID、仓库、SKU、批次、库位、数量、状态
-- 41. 拣货单已领取: 载荷 拣货单ID、仓库、SKU、批次、库位、数量、状态
-- 42. 拣货任务已完成: 载荷 拣货单ID、仓库、SKU、批次、库位、数量、状态
-- 43. 拣货异常已创建: 载荷 拣货单ID、仓库、SKU、批次、库位、数量、状态
-- 44. 拣货单已交接复核: 载荷 拣货单ID、仓库、SKU、批次、库位、数量、状态
-- 45. 容器已绑定: 载荷 周转容器ID、仓库、SKU、批次、库位、数量、状态
-- 46. 容器已装货: 载荷 周转容器ID、仓库、SKU、批次、库位、数量、状态
-- 47. 容器已交接复核: 载荷 周转容器ID、仓库、SKU、批次、库位、数量、状态
-- 48. 容器已清空: 载荷 周转容器ID、仓库、SKU、批次、库位、数量、状态
-- 49. 容器已停用: 载荷 周转容器ID、仓库、SKU、批次、库位、数量、状态
-- 50. 包装单已创建: 载荷 复核包装单ID、仓库、SKU、批次、库位、数量、状态
-- 51. 复核已完成: 载荷 复核包装单ID、仓库、SKU、批次、库位、数量、状态
-- 52. 复核异常已创建: 载荷 复核包装单ID、仓库、SKU、批次、库位、数量、状态
-- 53. 包裹已创建: 载荷 复核包装单ID、仓库、SKU、批次、库位、数量、状态
-- 54. 包装已完成: 载荷 复核包装单ID、仓库、SKU、批次、库位、数量、状态
-- 55. 发货交接已创建: 载荷 发货交接ID、仓库、SKU、批次、库位、数量、状态
-- 56. 包裹已扫描交接: 载荷 发货交接ID、仓库、SKU、批次、库位、数量、状态
-- 57. 发货异常已创建: 载荷 发货交接ID、仓库、SKU、批次、库位、数量、状态
-- 58. 发货交接已关闭: 载荷 发货交接ID、仓库、SKU、批次、库位、数量、状态
-- 59. 盘点计划已创建: 载荷 盘点计划ID、仓库、SKU、批次、库位、数量、状态
-- 60. 盘点计划已下发: 载荷 盘点计划ID、仓库、SKU、批次、库位、数量、状态
-- 61. 盘点任务已完成: 载荷 盘点计划ID、仓库、SKU、批次、库位、数量、状态
-- 62. 盘点差异已创建: 载荷 盘点计划ID、仓库、SKU、批次、库位、数量、状态
-- 63. 盘点计划已完成: 载荷 盘点计划ID、仓库、SKU、批次、库位、数量、状态
-- 64. 仓内异常已创建: 载荷 仓内异常ID、仓库、SKU、批次、库位、数量、状态
-- 65. 仓内异常已分派: 载荷 仓内异常ID、仓库、SKU、批次、库位、数量、状态
-- 66. 仓内异常已处理: 载荷 仓内异常ID、仓库、SKU、批次、库位、数量、状态
-- 67. 仓内异常已关闭: 载荷 仓内异常ID、仓库、SKU、批次、库位、数量、状态
-- 68. 仓内异常已升级: 载荷 仓内异常ID、仓库、SKU、批次、库位、数量、状态



-- 业务表

CREATE TABLE `wms_inbound` (
  `inbound_id` bigint NOT NULL COMMENT '入库单主键',
  `inbound_order_no` varchar(64) NOT NULL COMMENT 'WMS 入库单号',
  `source_order_no` varchar(64) NOT NULL COMMENT '来源单号，如 ASN、调拨入库、售后退货',
  `source_type` smallint NOT NULL COMMENT '采购、调拨、销售退货、其他',
  `inbound_status` smallint NOT NULL COMMENT '待到货、到货中、收货中、待质检、待上架、部分上架、已上架、已关闭、已取消',
  `expected_arrival_at` datetime NULL COMMENT '预计到仓时间',
  `arrived_at` datetime NULL COMMENT '到货时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`inbound_id`),
  KEY `idx_wms_inbound_status_time` (`inbound_status`, `updated_at`),
  KEY `idx_wms_inbound_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入库单';

CREATE TABLE `wms_receive` (
  `receive_id` bigint NOT NULL COMMENT '收货单主键',
  `receipt_order_no` varchar(64) NOT NULL COMMENT '收货单号',
  `receipt_status` smallint NOT NULL COMMENT '待收货、收货中、部分收货、已收货、异常、已关闭',
  `started_at` datetime NULL COMMENT '开始收货时间',
  `completed_at` datetime NULL COMMENT '收货完成时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`receive_id`),
  KEY `idx_wms_receive_status_time` (`receipt_status`, `updated_at`),
  KEY `idx_wms_receive_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收货单';

CREATE TABLE `wms_receive_line` (
  `receive_line_id` bigint NOT NULL COMMENT '收货行主键',
  `sku_code` varchar(64) NOT NULL COMMENT 'SKU 编码',
  `expected_qty` decimal(18,4) NOT NULL COMMENT '应收数量',
  `received_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '实收数量',
  `short_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '短收数量',
  `over_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '超收数量',
  `batch_no` varchar(128) NULL COMMENT '批次号',
  `production_date` date NULL COMMENT '生产日期',
  `expire_date` date NULL COMMENT '效期',
  `quality_status` smallint NOT NULL COMMENT '待检、合格、不合格、免检、待处理',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`receive_line_id`),
  KEY `idx_wms_receive_line_status_time` (`quality_status`, `updated_at`),
  KEY `idx_wms_receive_line_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收货行';

CREATE TABLE `wms_qc` (
  `qc_id` bigint NOT NULL COMMENT '质检单主键',
  `qc_order_no` varchar(64) NOT NULL COMMENT '质检单号',
  `sample_qty` decimal(18,4) NULL COMMENT '抽检数量',
  `accepted_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '合格数量',
  `rejected_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '不合格数量',
  `qc_result` smallint NOT NULL COMMENT '待检、合格、不合格、部分合格',
  `reject_reason` varchar(512) NULL COMMENT '不合格原因',
  `completed_at` datetime NULL COMMENT '质检完成时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`qc_id`),
  KEY `idx_wms_qc_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='质检单';

CREATE TABLE `wms_putaway` (
  `putaway_id` bigint NOT NULL COMMENT '上架任务主键',
  `batch_no` varchar(128) NULL COMMENT '批次',
  `quality_status` smallint NOT NULL COMMENT '合格、不合格、待处理',
  `required_qty` decimal(18,4) NOT NULL COMMENT '应上架数量',
  `putaway_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '已上架数量',
  `task_status` smallint NOT NULL COMMENT '待上架、上架中、部分上架、已完成、异常',
  `putaway_at` datetime NULL COMMENT '上架完成时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`putaway_id`),
  KEY `idx_wms_putaway_status_time` (`quality_status`, `updated_at`),
  KEY `idx_wms_putaway_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='上架任务';

CREATE TABLE `wms_stock` (
  `stock_id` bigint NOT NULL COMMENT '仓内库存主键',
  `batch_no` varchar(128) NULL COMMENT '批次',
  `stock_status` smallint NOT NULL COMMENT '可拣、冻结、不合格、待退供、待报废',
  `qty` decimal(18,4) NOT NULL COMMENT '库位数量',
  `locked_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '作业锁定数量',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`stock_id`),
  KEY `idx_wms_stock_status_time` (`stock_status`, `updated_at`),
  KEY `idx_wms_stock_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仓内库存';

CREATE TABLE `wms_outbound` (
  `outbound_id` bigint NOT NULL COMMENT '出库单主键',
  `outbound_order_no` varchar(64) NOT NULL COMMENT 'WMS 出库单号',
  `source_order_no` varchar(64) NOT NULL COMMENT '来源单号',
  `source_type` smallint NOT NULL COMMENT '销售、调拨、退供、其他',
  `priority` int NOT NULL COMMENT '优先级',
  `outbound_status` smallint NOT NULL COMMENT '待接单、待分配、分配失败、待拣货、拣货中、待复核、复核异常、待发货、已发货、已关闭、已取消',
  `shipped_at` datetime NULL COMMENT '发货时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`outbound_id`),
  KEY `idx_wms_outbound_status_time` (`outbound_status`, `updated_at`),
  KEY `idx_wms_outbound_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出库单';

CREATE TABLE `wms_outbound_line` (
  `outbound_line_id` bigint NOT NULL COMMENT '出库单行主键',
  `sku_code` varchar(64) NOT NULL COMMENT 'SKU 编码',
  `planned_qty` decimal(18,4) NOT NULL COMMENT '计划出库数量',
  `allocated_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '已分配数量',
  `picked_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '已拣数量',
  `shipped_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '已发货数量',
  `line_status` smallint NOT NULL COMMENT '待分配、已分配、拣货中、已拣货、已发货、已取消',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`outbound_line_id`),
  KEY `idx_wms_outbound_line_status_time` (`line_status`, `updated_at`),
  KEY `idx_wms_outbound_line_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出库单行';

CREATE TABLE `wms_wave` (
  `wave_id` bigint NOT NULL COMMENT '波次单主键',
  `wave_no` varchar(64) NOT NULL COMMENT '波次号',
  `wave_type` smallint NOT NULL COMMENT '单单拣、批量拣、边拣边分',
  `order_count` int NOT NULL COMMENT '出库单数',
  `sku_count` int NOT NULL COMMENT 'SKU 数',
  `wave_status` smallint NOT NULL COMMENT '草稿、已释放、拣货中、已完成、已取消',
  `released_at` datetime NULL COMMENT '释放时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`wave_id`),
  KEY `idx_wms_wave_status_time` (`wave_status`, `updated_at`),
  KEY `idx_wms_wave_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='波次单';

CREATE TABLE `wms_pick` (
  `pick_id` bigint NOT NULL COMMENT '拣货单主键',
  `picking_order_no` varchar(64) NOT NULL COMMENT '拣货单号',
  `picking_mode` smallint NOT NULL COMMENT '单单拣、批量拣、边拣边分',
  `picking_status` smallint NOT NULL COMMENT '待分配、待拣货、拣货中、部分拣货、拣货异常、已拣货、已交接复核',
  `started_at` datetime NULL COMMENT '开始时间',
  `completed_at` datetime NULL COMMENT '完成时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`pick_id`),
  KEY `idx_wms_pick_status_time` (`picking_status`, `updated_at`),
  KEY `idx_wms_pick_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拣货单';

CREATE TABLE `wms_pick_task` (
  `pick_id` bigint NOT NULL COMMENT '拣货任务主键',
  `batch_no` varchar(128) NULL COMMENT '批次',
  `required_qty` decimal(18,4) NOT NULL COMMENT '应拣数量',
  `picked_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '已拣数量',
  `task_status` smallint NOT NULL COMMENT '待拣货、拣货中、已完成、异常',
  `picked_at` datetime NULL COMMENT '拣货完成时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`pick_id`),
  KEY `idx_wms_pick_task_status_time` (`task_status`, `updated_at`),
  KEY `idx_wms_pick_task_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拣货任务';

CREATE TABLE `wms_container` (
  `container_id` bigint NOT NULL COMMENT '周转容器主键',
  `container_code` varchar(64) NOT NULL COMMENT '容器编码',
  `container_type` smallint NOT NULL COMMENT '周转箱、播种车、格口、托盘',
  `bind_object_type` smallint NULL COMMENT '拣货单、出库单、包裹',
  `container_status` smallint NOT NULL COMMENT '空闲、已绑定、拣货中、待复核、已清空、停用',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`container_id`),
  KEY `idx_wms_container_status_time` (`container_status`, `updated_at`),
  KEY `idx_wms_container_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='周转容器';

CREATE TABLE `wms_review_pack` (
  `review_pack_id` bigint NOT NULL COMMENT '复核包装单主键',
  `pack_order_no` varchar(64) NOT NULL COMMENT '包装单号',
  `pack_status` smallint NOT NULL COMMENT '待复核、复核中、复核异常、待打包、已打包、已关闭',
  `weight` decimal(18,4) NULL COMMENT '重量',
  `volume` decimal(18,4) NULL COMMENT '体积',
  `completed_at` datetime NULL COMMENT '完成时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`review_pack_id`),
  KEY `idx_wms_review_pack_status_time` (`pack_status`, `updated_at`),
  KEY `idx_wms_review_pack_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='复核包装单';

CREATE TABLE `wms_package` (
  `business_object_id` bigint NOT NULL COMMENT '包裹主键',
  `package_no` varchar(64) NOT NULL COMMENT '包裹号',
  `tracking_no` varchar(128) NULL COMMENT '运单号',
  `weight` decimal(18,4) NULL COMMENT '重量',
  `package_status` smallint NOT NULL COMMENT '已打包、待交接、已交接、已取消',
  `shipped_at` datetime NULL COMMENT '发货时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`business_object_id`),
  KEY `idx_wms_package_status_time` (`package_status`, `updated_at`),
  KEY `idx_wms_package_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='包裹';

CREATE TABLE `wms_count` (
  `count_id` bigint NOT NULL COMMENT '盘点计划主键',
  `count_plan_no` varchar(64) NOT NULL COMMENT '盘点计划号',
  `count_type` smallint NOT NULL COMMENT '全盘、动盘、循环盘、抽盘',
  `scope_config` text NOT NULL COMMENT '盘点范围',
  `plan_status` smallint NOT NULL COMMENT '草稿、已下发、盘点中、差异处理中、已完成、已取消',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`count_id`),
  KEY `idx_wms_count_status_time` (`plan_status`, `updated_at`),
  KEY `idx_wms_count_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='盘点计划';

CREATE TABLE `wms_exception_record` (
  `exception_record_id` bigint NOT NULL COMMENT '异常记录主键',
  `exception_no` varchar(64) NOT NULL COMMENT '异常单号',
  `source_type` smallint NOT NULL COMMENT '收货、质检、上架、分配、拣货、复核、发货、盘点',
  `exception_type` smallint NOT NULL COMMENT '短收、超收、错货、破损、库位不符、复核差异、盘点差异',
  `responsible_party` smallint NULL COMMENT '供应商、仓库、物流、客户、系统',
  `exception_status` smallint NOT NULL COMMENT '待处理、处理中、已关闭',
  `description` varchar(1024) NULL COMMENT '异常说明',
  `closed_at` datetime NULL COMMENT '关闭时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`exception_record_id`),
  KEY `idx_wms_exception_record_status_time` (`exception_status`, `updated_at`),
  KEY `idx_wms_exception_record_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='异常记录';



SET FOREIGN_KEY_CHECKS = 1;



-- source: docs/05-子系统数据库设计/ddl/04-中央库存系统.sql

-- 中央库存系统 数据库 DDL

-- 口径：MySQL 8.0 / InnoDB / utf8mb4

SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;



CREATE TABLE `inv_enum_type` (
  `enum_type_id` bigint NOT NULL COMMENT '枚举类型ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_type_name` varchar(128) NOT NULL COMMENT '枚举类型名称',
  `configurable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许页面配置',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_type_id`),
  UNIQUE KEY `uk_inv_enum_type_code` (`enum_type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='枚举类型';

CREATE TABLE `inv_enum_item` (
  `enum_item_id` bigint NOT NULL COMMENT '枚举项ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_value` smallint NOT NULL COMMENT '枚举数值，从1开始',
  `enum_label` varchar(128) NOT NULL COMMENT '枚举显示名',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_item_id`),
  UNIQUE KEY `uk_inv_enum_item` (`enum_type_code`, `enum_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='枚举项';

CREATE TABLE `inv_domain_event` (
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
  UNIQUE KEY `uk_inv_domain_event_code` (`event_code`),
  KEY `idx_inv_domain_event_status` (`event_status`, `created_at`),
  KEY `idx_inv_domain_event_aggregate` (`aggregate_type`, `aggregate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='领域事件发布表';

CREATE TABLE `inv_event_consume_log` (
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
  UNIQUE KEY `uk_inv_event_consume` (`source_system`, `event_code`, `consumer_name`),
  KEY `idx_inv_event_consume_status` (`consume_status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='事件消费幂等日志';

CREATE TABLE `inv_operation_audit_log` (
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
  KEY `idx_inv_operation_audit_target` (`target_type`, `target_id`),
  KEY `idx_inv_operation_audit_operator` (`operator_id`, `operation_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';



-- 枚举初始化数据
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (1, 'ADJUSTMENT_STATUS', 'ADJUSTMENT_STATUS', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (1, 'ADJUSTMENT_STATUS', 1, '草稿', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (2, 'ADJUSTMENT_STATUS', 2, '待审批', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (3, 'ADJUSTMENT_STATUS', 3, '已批准', 3, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (4, 'ADJUSTMENT_STATUS', 4, '已执行', 4, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (5, 'ADJUSTMENT_STATUS', 5, '已驳回', 5, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (6, 'ADJUSTMENT_STATUS', 6, '已取消', 6, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (2, 'ADJUSTMENT_TYPE', 'ADJUSTMENT_TYPE', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (7, 'ADJUSTMENT_TYPE', 1, '盘盈', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (8, 'ADJUSTMENT_TYPE', 2, '盘亏', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (9, 'ADJUSTMENT_TYPE', 3, '差异修正', 3, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (10, 'ADJUSTMENT_TYPE', 4, '红冲', 4, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (3, 'APPROVAL_STATUS', 'APPROVAL_STATUS', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (11, 'APPROVAL_STATUS', 1, '草稿', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (12, 'APPROVAL_STATUS', 2, '待审批', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (13, 'APPROVAL_STATUS', 3, '已批准', 3, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (14, 'APPROVAL_STATUS', 4, '已驳回', 4, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (4, 'CHANGE_DIRECTION', 'CHANGE_DIRECTION', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (15, 'CHANGE_DIRECTION', 1, '增加', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (16, 'CHANGE_DIRECTION', 2, '减少', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (17, 'CHANGE_DIRECTION', 3, '占用', 3, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (18, 'CHANGE_DIRECTION', 4, '释放', 4, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (5, 'COMMON_STATUS', 'COMMON_STATUS', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (19, 'COMMON_STATUS', 1, '启用', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (20, 'COMMON_STATUS', 2, '停用', 2, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (6, 'CONSUME_STATUS', 'CONSUME_STATUS', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (21, 'CONSUME_STATUS', 1, '待消费', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (22, 'CONSUME_STATUS', 2, '处理中', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (23, 'CONSUME_STATUS', 3, '消费成功', 3, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (24, 'CONSUME_STATUS', 4, '消费失败', 4, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (25, 'CONSUME_STATUS', 5, '已忽略', 5, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (7, 'CURRENCY', 'CURRENCY', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (26, 'CURRENCY', 1, '人民币', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (27, 'CURRENCY', 2, '美元', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (28, 'CURRENCY', 3, '欧元', 3, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (29, 'CURRENCY', 4, '港币', 4, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (30, 'CURRENCY', 5, '日元', 5, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (8, 'EVENT_PROCESS_STATUS', 'EVENT_PROCESS_STATUS', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (31, 'EVENT_PROCESS_STATUS', 1, '待处理', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (32, 'EVENT_PROCESS_STATUS', 2, '成功', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (33, 'EVENT_PROCESS_STATUS', 3, '失败', 3, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (34, 'EVENT_PROCESS_STATUS', 4, '已忽略', 4, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (9, 'EVENT_STATUS', 'EVENT_STATUS', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (35, 'EVENT_STATUS', 1, '待发布', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (36, 'EVENT_STATUS', 2, '发布中', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (37, 'EVENT_STATUS', 3, '已发布', 3, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (38, 'EVENT_STATUS', 4, '发布失败', 4, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (39, 'EVENT_STATUS', 5, '已取消', 5, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (10, 'FREEZE_REASON', 'FREEZE_REASON', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (40, 'FREEZE_REASON', 1, '质检', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (41, 'FREEZE_REASON', 2, '盘点', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (42, 'FREEZE_REASON', 3, '风控', 3, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (43, 'FREEZE_REASON', 4, '异常', 4, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (44, 'FREEZE_REASON', 5, '人工', 5, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (11, 'FREEZE_STATUS', 'FREEZE_STATUS', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (45, 'FREEZE_STATUS', 1, '草稿', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (46, 'FREEZE_STATUS', 2, '待审批', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (47, 'FREEZE_STATUS', 3, '已冻结', 3, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (48, 'FREEZE_STATUS', 4, '部分解冻', 4, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (49, 'FREEZE_STATUS', 5, '已解冻', 5, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (50, 'FREEZE_STATUS', 6, '已取消', 6, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (12, 'INV_STOCK_STATUS', 'INV_STOCK_STATUS', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (51, 'INV_STOCK_STATUS', 1, '可用', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (52, 'INV_STOCK_STATUS', 2, '冻结', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (53, 'INV_STOCK_STATUS', 3, '不合格', 3, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (54, 'INV_STOCK_STATUS', 4, '在途', 4, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (55, 'INV_STOCK_STATUS', 5, '待退供', 5, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (13, 'LEDGER_TYPE', 'LEDGER_TYPE', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (56, 'LEDGER_TYPE', 1, '入库', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (57, 'LEDGER_TYPE', 2, '出库', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (58, 'LEDGER_TYPE', 3, '预占', 3, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (59, 'LEDGER_TYPE', 4, '释放', 4, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (60, 'LEDGER_TYPE', 5, '冻结', 5, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (61, 'LEDGER_TYPE', 6, '解冻', 6, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (62, 'LEDGER_TYPE', 7, '调整', 7, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (63, 'LEDGER_TYPE', 8, '红冲', 8, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (14, 'OPERATION_RESULT', 'OPERATION_RESULT', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (64, 'OPERATION_RESULT', 1, '成功', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (65, 'OPERATION_RESULT', 2, '失败', 2, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (15, 'RECON_STATUS', 'RECON_STATUS', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (66, 'RECON_STATUS', 1, '草稿', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (67, 'RECON_STATUS', 2, '对账中', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (68, 'RECON_STATUS', 3, '有差异', 3, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (69, 'RECON_STATUS', 4, '已确认', 4, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (70, 'RECON_STATUS', 5, '已关闭', 5, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (16, 'RESERVATION_LINE_STATUS', 'RESERVATION_LINE_STATUS', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (71, 'RESERVATION_LINE_STATUS', 1, '已预占', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (72, 'RESERVATION_LINE_STATUS', 2, '部分消耗', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (73, 'RESERVATION_LINE_STATUS', 3, '已消耗', 3, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (74, 'RESERVATION_LINE_STATUS', 4, '已释放', 4, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (75, 'RESERVATION_LINE_STATUS', 5, '失败', 5, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (17, 'RESERVATION_STATUS', 'RESERVATION_STATUS', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (76, 'RESERVATION_STATUS', 1, '已创建', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (77, 'RESERVATION_STATUS', 2, '已预占', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (78, 'RESERVATION_STATUS', 3, '部分消耗', 3, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (79, 'RESERVATION_STATUS', 4, '已消耗', 4, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (80, 'RESERVATION_STATUS', 5, '已释放', 5, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (81, 'RESERVATION_STATUS', 6, '已关闭', 6, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (82, 'RESERVATION_STATUS', 7, '失败', 7, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (18, 'RESERVATION_TYPE', 'RESERVATION_TYPE', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (83, 'RESERVATION_TYPE', 1, '销售', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (84, 'RESERVATION_TYPE', 2, '调拨', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (85, 'RESERVATION_TYPE', 3, '退供', 3, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (19, 'SNAPSHOT_STATUS', 'SNAPSHOT_STATUS', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (86, 'SNAPSHOT_STATUS', 1, '生成中', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (87, 'SNAPSHOT_STATUS', 2, '已生成', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (88, 'SNAPSHOT_STATUS', 3, '失败', 3, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (89, 'SNAPSHOT_STATUS', 4, '已关闭', 4, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (20, 'SNAPSHOT_TYPE', 'SNAPSHOT_TYPE', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (90, 'SNAPSHOT_TYPE', 1, '日结', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (91, 'SNAPSHOT_TYPE', 2, '月结', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (92, 'SNAPSHOT_TYPE', 3, '手工', 3, 1);
INSERT INTO `inv_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (21, 'SOURCE_SYSTEM', 'SOURCE_SYSTEM', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (93, 'SOURCE_SYSTEM', 1, '来源系统', 1, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (94, 'SOURCE_SYSTEM', 2, '调拨', 2, 1);
INSERT INTO `inv_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (95, 'SOURCE_SYSTEM', 3, '采购', 3, 1);



-- 领域事件类型参考数据
-- 1. 库存已入库: 载荷 库存账户ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 2. 库存已扣减: 载荷 库存账户ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 3. 库存已冻结: 载荷 库存账户ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 4. 库存已解冻: 载荷 库存账户ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 5. 库存已调整: 载荷 库存账户ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 6. WMS上架已完成: 载荷 增加库存余额并追加入库流水
-- 7. SKU已启用: 载荷 初始化或刷新SKU库存维度
-- 8. 仓库已启用: 载荷 初始化或刷新仓库库存维度
-- 9. 审批已通过: 载荷 推进冻结、调整或对账处理
-- 10. 库存已预占: 载荷 库存预占ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 11. 预占已释放: 载荷 库存预占ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 12. 预占已扣减: 载荷 库存预占ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 13. 预占已关闭: 载荷 库存预占ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 14. 预占已过期: 载荷 库存预占ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 15. 销售订单已审核: 载荷 创建库存预占并锁定可用数量
-- 16. 冻结单已创建: 载荷 冻结单ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 17. 冻结单已提交审批: 载荷 冻结单ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 18. 冻结单已关闭: 载荷 冻结单ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 19. 质检不合格已判定: 载荷 创建冻结单并冻结对应库存数量
-- 20. 库存调整单已创建: 载荷 库存调整单ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 21. 库存调整单已提交审批: 载荷 库存调整单ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 22. 库存调整单已批准: 载荷 库存调整单ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 23. 库存调整单已取消: 载荷 库存调整单ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 24. 盘点差异已确认: 载荷 创建库存调整单并等待审批
-- 25. 库存快照已生成: 载荷 库存快照ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 26. 库存快照已锁定: 载荷 库存快照ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 27. 库存快照已重建: 载荷 库存快照ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 28. 库存快照已归档: 载荷 库存快照ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 29. 库存账期已关闭: 载荷 生成指定账期库存快照
-- 30. 库存对账单已生成: 载荷 库存对账单ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 31. 库存对账差异已创建: 载荷 库存对账单ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 32. 库存对账差异已确认: 载荷 库存对账单ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 33. 库存调整已发起: 载荷 库存对账单ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 34. 库存对账单已关闭: 载荷 库存对账单ID、库存维度、来源单、变化数量、变化前后数量、流水批次
-- 35. WMS库存快照已生成: 载荷 生成中央库存与WMS差异明细



-- 业务表

CREATE TABLE `inv_stock_balance` (
  `stock_id` bigint NOT NULL COMMENT '库存余额主键',
  `batch_no` varchar(128) NULL COMMENT '批次',
  `stock_status` smallint NOT NULL COMMENT '可用、冻结、不合格、在途、待退供',
  `on_hand_qty` decimal(18,4) NOT NULL COMMENT '实物数量',
  `available_qty` decimal(18,4) NOT NULL COMMENT '可用数量',
  `reserved_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '预占数量',
  `frozen_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '冻结数量',
  `in_transit_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '在途数量',
  `version_no` bigint NOT NULL COMMENT '版本号',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`stock_id`),
  KEY `idx_inv_stock_balance_status_time` (`stock_status`, `updated_at`),
  KEY `idx_inv_stock_balance_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存余额';

CREATE TABLE `inv_stock_ledger` (
  `stock_id` bigint NOT NULL COMMENT '库存流水主键',
  `ledger_no` varchar(64) NOT NULL COMMENT '流水号',
  `ledger_type` smallint NOT NULL COMMENT '入库、出库、预占、释放、冻结、解冻、调整、红冲',
  `change_direction` smallint NOT NULL COMMENT '增加、减少、占用、释放',
  `qty_delta` decimal(18,4) NOT NULL COMMENT '变化数量',
  `before_on_hand_qty` decimal(18,4) NOT NULL COMMENT '变化前实物',
  `after_on_hand_qty` decimal(18,4) NOT NULL COMMENT '变化后实物',
  `before_available_qty` decimal(18,4) NOT NULL COMMENT '变化前可用',
  `after_available_qty` decimal(18,4) NOT NULL COMMENT '变化后可用',
  `source_system` smallint NOT NULL COMMENT '来源系统',
  `source_order_no` varchar(64) NOT NULL COMMENT '来源单号',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`stock_id`),
  KEY `idx_inv_stock_ledger_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存流水';

CREATE TABLE `inv_stock_event_log` (
  `stock_id` bigint NOT NULL COMMENT '库存事件日志主键',
  `event_name` varchar(128) NOT NULL COMMENT '事件名称',
  `source_system` smallint NOT NULL COMMENT '来源系统',
  `source_order_no` varchar(64) NULL COMMENT '来源单号',
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
  PRIMARY KEY (`stock_id`),
  KEY `idx_inv_stock_event_log_status_time` (`process_status`, `updated_at`),
  KEY `idx_inv_stock_event_log_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存事件日志';

CREATE TABLE `inv_reservation` (
  `reservation_id` bigint NOT NULL COMMENT '预占单主键',
  `reservation_no` varchar(64) NOT NULL COMMENT '预占单号',
  `source_system` smallint NOT NULL COMMENT 'OMS、调拨、采购等',
  `source_order_no` varchar(64) NOT NULL COMMENT '来源单号',
  `reservation_type` smallint NOT NULL COMMENT '销售、调拨、退供',
  `reservation_status` smallint NOT NULL COMMENT '已创建、已预占、部分消耗、已消耗、已释放、已关闭、失败',
  `expire_at` datetime NULL COMMENT '预占过期时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`reservation_id`),
  KEY `idx_inv_reservation_status_time` (`reservation_status`, `updated_at`),
  KEY `idx_inv_reservation_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预占单';

CREATE TABLE `inv_reservation_line` (
  `reservation_line_id` bigint NOT NULL COMMENT '预占行主键',
  `batch_no` varchar(128) NULL COMMENT '批次',
  `requested_qty` decimal(18,4) NOT NULL COMMENT '请求预占数量',
  `reserved_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '成功预占数量',
  `consumed_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '已消耗数量',
  `released_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '已释放数量',
  `line_status` smallint NOT NULL COMMENT '已预占、部分消耗、已消耗、已释放、失败',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`reservation_line_id`),
  KEY `idx_inv_reservation_line_status_time` (`line_status`, `updated_at`),
  KEY `idx_inv_reservation_line_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预占行';

CREATE TABLE `inv_freeze` (
  `freeze_id` bigint NOT NULL COMMENT '冻结单主键',
  `freeze_no` varchar(64) NOT NULL COMMENT '冻结单号',
  `freeze_reason` smallint NOT NULL COMMENT '质检、盘点、风控、异常、人工',
  `freeze_status` smallint NOT NULL COMMENT '草稿、待审批、已冻结、部分解冻、已解冻、已取消',
  `approval_status` smallint NOT NULL COMMENT '草稿、待审批、已批准、已驳回',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`freeze_id`),
  KEY `idx_inv_freeze_status_time` (`freeze_status`, `updated_at`),
  KEY `idx_inv_freeze_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='冻结单';

CREATE TABLE `inv_stock_adjustment` (
  `stock_adjustment_id` bigint NOT NULL COMMENT '库存调整单主键',
  `adjustment_no` varchar(64) NOT NULL COMMENT '调整单号',
  `adjustment_type` smallint NOT NULL COMMENT '盘盈、盘亏、差异修正、红冲',
  `adjustment_reason` varchar(512) NOT NULL COMMENT '调整原因',
  `adjustment_status` smallint NOT NULL COMMENT '草稿、待审批、已批准、已执行、已驳回、已取消',
  `approval_status` smallint NOT NULL COMMENT '草稿、待审批、已批准、已驳回',
  `executed_at` datetime NULL COMMENT '执行时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`stock_adjustment_id`),
  KEY `idx_inv_stock_adjustment_status_time` (`adjustment_status`, `updated_at`),
  KEY `idx_inv_stock_adjustment_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存调整单';

CREATE TABLE `inv_stock_snapshot` (
  `stock_snapshot_id` bigint NOT NULL COMMENT '库存快照主键',
  `snapshot_no` varchar(64) NOT NULL COMMENT '快照号',
  `snapshot_date` date NOT NULL COMMENT '快照日期',
  `snapshot_type` smallint NOT NULL COMMENT '日结、月结、手工',
  `snapshot_status` smallint NOT NULL COMMENT '生成中、已生成、失败、已关闭',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`stock_snapshot_id`),
  KEY `idx_inv_stock_snapshot_status_time` (`snapshot_status`, `updated_at`),
  KEY `idx_inv_stock_snapshot_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存快照';

CREATE TABLE `inv_stock_reconcile` (
  `stock_reconcile_id` bigint NOT NULL COMMENT '库存对账单主键',
  `reconciliation_no` varchar(64) NOT NULL COMMENT '对账单号',
  `recon_date` date NOT NULL COMMENT '对账日期',
  `recon_status` smallint NOT NULL COMMENT '草稿、对账中、有差异、已确认、已关闭',
  `diff_count` int NOT NULL DEFAULT 0 COMMENT '差异行数',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`stock_reconcile_id`),
  KEY `idx_inv_stock_reconcile_status_time` (`recon_status`, `updated_at`),
  KEY `idx_inv_stock_reconcile_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存对账单';



SET FOREIGN_KEY_CHECKS = 1;



-- source: docs/05-子系统数据库设计/ddl/05-OMS系统.sql

-- OMS系统 数据库 DDL

-- 口径：MySQL 8.0 / InnoDB / utf8mb4

SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;



CREATE TABLE `oms_enum_type` (
  `enum_type_id` bigint NOT NULL COMMENT '枚举类型ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_type_name` varchar(128) NOT NULL COMMENT '枚举类型名称',
  `configurable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许页面配置',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_type_id`),
  UNIQUE KEY `uk_oms_enum_type_code` (`enum_type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='枚举类型';

CREATE TABLE `oms_enum_item` (
  `enum_item_id` bigint NOT NULL COMMENT '枚举项ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_value` smallint NOT NULL COMMENT '枚举数值，从1开始',
  `enum_label` varchar(128) NOT NULL COMMENT '枚举显示名',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_item_id`),
  UNIQUE KEY `uk_oms_enum_item` (`enum_type_code`, `enum_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='枚举项';

CREATE TABLE `oms_domain_event` (
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
  UNIQUE KEY `uk_oms_domain_event_code` (`event_code`),
  KEY `idx_oms_domain_event_status` (`event_status`, `created_at`),
  KEY `idx_oms_domain_event_aggregate` (`aggregate_type`, `aggregate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='领域事件发布表';

CREATE TABLE `oms_event_consume_log` (
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
  UNIQUE KEY `uk_oms_event_consume` (`source_system`, `event_code`, `consumer_name`),
  KEY `idx_oms_event_consume_status` (`consume_status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='事件消费幂等日志';

CREATE TABLE `oms_operation_audit_log` (
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
  KEY `idx_oms_operation_audit_target` (`target_type`, `target_id`),
  KEY `idx_oms_operation_audit_operator` (`operator_id`, `operation_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';



-- 枚举初始化数据
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (1, 'AFTER_SALE_LINE_STATUS', 'AFTER_SALE_LINE_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (1, 'AFTER_SALE_LINE_STATUS', 1, '待审核', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (2, 'AFTER_SALE_LINE_STATUS', 2, '待退货', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (3, 'AFTER_SALE_LINE_STATUS', 3, '待验收', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (4, 'AFTER_SALE_LINE_STATUS', 4, '待退款', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (5, 'AFTER_SALE_LINE_STATUS', 5, '待补发', 5, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (6, 'AFTER_SALE_LINE_STATUS', 6, '已完成', 6, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (2, 'AFTER_SALE_REASON', 'AFTER_SALE_REASON', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (7, 'AFTER_SALE_REASON', 1, '质量问题', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (8, 'AFTER_SALE_REASON', 2, '错发', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (9, 'AFTER_SALE_REASON', 3, '少发', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (10, 'AFTER_SALE_REASON', 4, '不想要', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (11, 'AFTER_SALE_REASON', 5, '其他', 5, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (3, 'AFTER_SALE_STATUS', 'AFTER_SALE_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (12, 'AFTER_SALE_STATUS', 1, '已创建', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (13, 'AFTER_SALE_STATUS', 2, '待审核', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (14, 'AFTER_SALE_STATUS', 3, '审核驳回', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (15, 'AFTER_SALE_STATUS', 4, '待退货', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (16, 'AFTER_SALE_STATUS', 5, '待验收', 5, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (17, 'AFTER_SALE_STATUS', 6, '待退款', 6, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (18, 'AFTER_SALE_STATUS', 7, '待补发', 7, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (19, 'AFTER_SALE_STATUS', 8, '异常待处理', 8, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (20, 'AFTER_SALE_STATUS', 9, '已完成', 9, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (21, 'AFTER_SALE_STATUS', 10, '已关闭', 10, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (4, 'AFTER_SALE_TYPE', 'AFTER_SALE_TYPE', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (22, 'AFTER_SALE_TYPE', 1, '仅退款', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (23, 'AFTER_SALE_TYPE', 2, '退货退款', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (24, 'AFTER_SALE_TYPE', 3, '换货补发', 3, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (5, 'APPROVAL_STATUS', 'APPROVAL_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (25, 'APPROVAL_STATUS', 1, '草稿', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (26, 'APPROVAL_STATUS', 2, '待审批', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (27, 'APPROVAL_STATUS', 3, '已批准', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (28, 'APPROVAL_STATUS', 4, '已驳回', 4, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (6, 'AUDIT_PROCESS_STATUS', 'AUDIT_PROCESS_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (29, 'AUDIT_PROCESS_STATUS', 1, '待处理', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (30, 'AUDIT_PROCESS_STATUS', 2, '已放行', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (31, 'AUDIT_PROCESS_STATUS', 3, '已驳回', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (32, 'AUDIT_PROCESS_STATUS', 4, '已修正', 4, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (7, 'AUDIT_RESULT', 'AUDIT_RESULT', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (33, 'AUDIT_RESULT', 1, '通过', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (34, 'AUDIT_RESULT', 2, '拦截', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (35, 'AUDIT_RESULT', 3, '警告', 3, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (8, 'AUDIT_TYPE', 'AUDIT_TYPE', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (36, 'AUDIT_TYPE', 1, '商品', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (37, 'AUDIT_TYPE', 2, '客户', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (38, 'AUDIT_TYPE', 3, '地址', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (39, 'AUDIT_TYPE', 4, '价格', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (40, 'AUDIT_TYPE', 5, '风控', 5, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (41, 'AUDIT_TYPE', 6, '信用', 6, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (9, 'CANCEL_REASON', 'CANCEL_REASON', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (42, 'CANCEL_REASON', 1, '不想要', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (43, 'CANCEL_REASON', 2, '地址错误', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (44, 'CANCEL_REASON', 3, '缺货', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (45, 'CANCEL_REASON', 4, '风控', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (46, 'CANCEL_REASON', 5, '其他', 5, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (10, 'CANCEL_SOURCE', 'CANCEL_SOURCE', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (47, 'CANCEL_SOURCE', 1, '客户', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (48, 'CANCEL_SOURCE', 2, '客服', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (49, 'CANCEL_SOURCE', 3, '渠道', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (50, 'CANCEL_SOURCE', 4, '系统', 4, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (11, 'CANCEL_STATUS', 'CANCEL_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (51, 'CANCEL_STATUS', 1, '待审核', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (52, 'CANCEL_STATUS', 2, '已同意', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (53, 'CANCEL_STATUS', 3, '已拒绝', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (54, 'CANCEL_STATUS', 4, '取消中', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (55, 'CANCEL_STATUS', 5, '已完成', 5, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (56, 'CANCEL_STATUS', 6, '转售后', 6, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (12, 'COMMON_STATUS', 'COMMON_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (57, 'COMMON_STATUS', 1, '启用', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (58, 'COMMON_STATUS', 2, '停用', 2, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (13, 'CONSUME_STATUS', 'CONSUME_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (59, 'CONSUME_STATUS', 1, '待消费', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (60, 'CONSUME_STATUS', 2, '处理中', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (61, 'CONSUME_STATUS', 3, '消费成功', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (62, 'CONSUME_STATUS', 4, '消费失败', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (63, 'CONSUME_STATUS', 5, '已忽略', 5, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (14, 'CURRENCY', 'CURRENCY', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (64, 'CURRENCY', 1, '人民币', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (65, 'CURRENCY', 2, '美元', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (66, 'CURRENCY', 3, '欧元', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (67, 'CURRENCY', 4, '港币', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (68, 'CURRENCY', 5, '日元', 5, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (15, 'EVENT_STATUS', 'EVENT_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (69, 'EVENT_STATUS', 1, '待发布', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (70, 'EVENT_STATUS', 2, '发布中', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (71, 'EVENT_STATUS', 3, '已发布', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (72, 'EVENT_STATUS', 4, '发布失败', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (73, 'EVENT_STATUS', 5, '已取消', 5, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (16, 'FULFILLMENT_LINE_STATUS', 'FULFILLMENT_LINE_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (74, 'FULFILLMENT_LINE_STATUS', 1, '待预占', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (75, 'FULFILLMENT_LINE_STATUS', 2, '已预占', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (76, 'FULFILLMENT_LINE_STATUS', 3, '待出库', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (77, 'FULFILLMENT_LINE_STATUS', 4, '出库中', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (78, 'FULFILLMENT_LINE_STATUS', 5, '已发货', 5, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (79, 'FULFILLMENT_LINE_STATUS', 6, '已取消', 6, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (17, 'FULFILLMENT_ORDER_STATUS', 'FULFILLMENT_ORDER_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (80, 'FULFILLMENT_ORDER_STATUS', 1, '待预占', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (81, 'FULFILLMENT_ORDER_STATUS', 2, '已预占', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (82, 'FULFILLMENT_ORDER_STATUS', 3, '待出库', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (83, 'FULFILLMENT_ORDER_STATUS', 4, '已下发', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (84, 'FULFILLMENT_ORDER_STATUS', 5, '出库中', 5, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (85, 'FULFILLMENT_ORDER_STATUS', 6, '已发货', 6, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (86, 'FULFILLMENT_ORDER_STATUS', 7, '已取消', 7, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (87, 'FULFILLMENT_ORDER_STATUS', 8, '失败', 8, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (18, 'FULFILLMENT_STATUS', 'FULFILLMENT_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (88, 'FULFILLMENT_STATUS', 1, '未履约', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (89, 'FULFILLMENT_STATUS', 2, '部分履约', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (90, 'FULFILLMENT_STATUS', 3, '履约中', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (91, 'FULFILLMENT_STATUS', 4, '已履约', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (92, 'FULFILLMENT_STATUS', 5, '履约失败', 5, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (19, 'OMS_RULE_TYPE', 'OMS_RULE_TYPE', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (93, 'OMS_RULE_TYPE', 1, '审单', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (94, 'OMS_RULE_TYPE', 2, '分仓', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (95, 'OMS_RULE_TYPE', 3, '取消', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (96, 'OMS_RULE_TYPE', 4, '售后', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (97, 'OMS_RULE_TYPE', 5, '承运商', 5, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (20, 'OPERATION_RESULT', 'OPERATION_RESULT', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (98, 'OPERATION_RESULT', 1, '成功', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (99, 'OPERATION_RESULT', 2, '失败', 2, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (21, 'ORDER_AUDIT_STATUS', 'ORDER_AUDIT_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (100, 'ORDER_AUDIT_STATUS', 1, '待审核', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (101, 'ORDER_AUDIT_STATUS', 2, '通过', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (102, 'ORDER_AUDIT_STATUS', 3, '异常', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (103, 'ORDER_AUDIT_STATUS', 4, '驳回', 4, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (22, 'OUTBOUND_LINE_STATUS', 'OUTBOUND_LINE_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (104, 'OUTBOUND_LINE_STATUS', 1, '待下发', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (105, 'OUTBOUND_LINE_STATUS', 2, '已下发', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (106, 'OUTBOUND_LINE_STATUS', 3, '拣货中', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (107, 'OUTBOUND_LINE_STATUS', 4, '已发货', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (108, 'OUTBOUND_LINE_STATUS', 5, '已取消', 5, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (23, 'OUTBOUND_STATUS', 'OUTBOUND_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (109, 'OUTBOUND_STATUS', 1, '草稿', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (110, 'OUTBOUND_STATUS', 2, '已下发', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (111, 'OUTBOUND_STATUS', 3, 'WMS已接单', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (112, 'OUTBOUND_STATUS', 4, '拣货中', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (113, 'OUTBOUND_STATUS', 5, '已发货', 5, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (114, 'OUTBOUND_STATUS', 6, '已取消', 6, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (115, 'OUTBOUND_STATUS', 7, '异常', 7, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (24, 'OUTBOUND_TYPE', 'OUTBOUND_TYPE', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (116, 'OUTBOUND_TYPE', 1, '销售出库', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (117, 'OUTBOUND_TYPE', 2, '换货补发', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (118, 'OUTBOUND_TYPE', 3, '手工补发', 3, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (25, 'PAY_STATUS', 'PAY_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (119, 'PAY_STATUS', 1, '未支付', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (120, 'PAY_STATUS', 2, '已支付', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (121, 'PAY_STATUS', 3, '部分退款', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (122, 'PAY_STATUS', 4, '已退款', 4, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (26, 'RESERVATION_STATUS', 'RESERVATION_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (123, 'RESERVATION_STATUS', 1, '待预占', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (124, 'RESERVATION_STATUS', 2, '预占成功', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (125, 'RESERVATION_STATUS', 3, '预占失败', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (126, 'RESERVATION_STATUS', 4, '已释放', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (127, 'RESERVATION_STATUS', 5, '已扣减', 5, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (27, 'SALES_ORDER_LINE_STATUS', 'SALES_ORDER_LINE_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (128, 'SALES_ORDER_LINE_STATUS', 1, '待审核', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (129, 'SALES_ORDER_LINE_STATUS', 2, '待预占', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (130, 'SALES_ORDER_LINE_STATUS', 3, '已预占', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (131, 'SALES_ORDER_LINE_STATUS', 4, '出库中', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (132, 'SALES_ORDER_LINE_STATUS', 5, '已发货', 5, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (133, 'SALES_ORDER_LINE_STATUS', 6, '已完成', 6, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (134, 'SALES_ORDER_LINE_STATUS', 7, '已取消', 7, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (28, 'SALES_ORDER_STATUS', 'SALES_ORDER_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (135, 'SALES_ORDER_STATUS', 1, '已创建', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (136, 'SALES_ORDER_STATUS', 2, '待审核', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (137, 'SALES_ORDER_STATUS', 3, '异常待处理', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (138, 'SALES_ORDER_STATUS', 4, '待预占', 4, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (139, 'SALES_ORDER_STATUS', 5, '缺货待处理', 5, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (140, 'SALES_ORDER_STATUS', 6, '已预占', 6, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (141, 'SALES_ORDER_STATUS', 7, '已下发仓库', 7, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (142, 'SALES_ORDER_STATUS', 8, '出库中', 8, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (143, 'SALES_ORDER_STATUS', 9, '已发货', 9, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (144, 'SALES_ORDER_STATUS', 10, '已签收', 10, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (145, 'SALES_ORDER_STATUS', 11, '已完成', 11, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (146, 'SALES_ORDER_STATUS', 12, '已取消', 12, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (29, 'SALES_ORDER_TYPE', 'SALES_ORDER_TYPE', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (147, 'SALES_ORDER_TYPE', 1, '普通', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (148, 'SALES_ORDER_TYPE', 2, '预售', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (149, 'SALES_ORDER_TYPE', 3, '换货补发', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (150, 'SALES_ORDER_TYPE', 4, '手工', 4, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (30, 'STOCK_RELEASE_STATUS', 'STOCK_RELEASE_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (151, 'STOCK_RELEASE_STATUS', 1, '未释放', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (152, 'STOCK_RELEASE_STATUS', 2, '释放中', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (153, 'STOCK_RELEASE_STATUS', 3, '已释放', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (154, 'STOCK_RELEASE_STATUS', 4, '释放失败', 4, 1);
INSERT INTO `oms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (31, 'WMS_CANCEL_STATUS', 'WMS_CANCEL_STATUS', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (155, 'WMS_CANCEL_STATUS', 1, '未请求', 1, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (156, 'WMS_CANCEL_STATUS', 2, '请求中', 2, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (157, 'WMS_CANCEL_STATUS', 3, '成功', 3, 1);
INSERT INTO `oms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (158, 'WMS_CANCEL_STATUS', 4, '失败', 4, 1);



-- 领域事件类型参考数据
-- 1. 销售订单已创建: 载荷 销售订单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 2. 订单已审核: 载荷 销售订单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 3. 订单异常已标记: 载荷 销售订单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 4. 订单已发货: 载荷 销售订单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 5. 订单已签收: 载荷 销售订单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 6. 订单已完成: 载荷 销售订单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 7. 订单已取消: 载荷 销售订单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 8. WMS已发货: 载荷 记录发货事实并推进订单状态
-- 9. 库存已预占: 载荷 记录预占成功并推进履约
-- 10. 退款已完成: 载荷 更新售后和订单退款状态
-- 11. 履约单已创建: 载荷 履约单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 12. 库存预占已请求: 载荷 履约单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 13. 履约库存已预占: 载荷 履约单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 14. 履约出库已请求: 载荷 履约单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 15. 履约单已关闭: 载荷 履约单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 16. 出库单已创建: 载荷 出库单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 17. 出库单已下发WMS: 载荷 出库单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 18. WMS已接单: 载荷 出库单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 19. 出库单已取消: 载荷 出库单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 20. 出库单已关闭: 载荷 出库单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 21. 取消申请已创建: 载荷 取消申请ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 22. 取消申请已审核: 载荷 取消申请ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 23. WMS取消已请求: 载荷 取消申请ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 24. 取消库存释放已请求: 载荷 取消申请ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 25. 取消申请已关闭: 载荷 取消申请ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 26. 取消请求已提交: 载荷 创建取消申请并判断可取消路径
-- 27. 售后单已创建: 载荷 售后单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 28. 售后已审核: 载荷 售后单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 29. 退货已验收: 载荷 售后单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 30. 退款已请求: 载荷 售后单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 31. 补发已请求: 载荷 售后单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 32. 售后已完成: 载荷 售后单ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 33. OMS规则已创建: 载荷 OMS规则配置ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 34. OMS规则已提交审批: 载荷 OMS规则配置ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 35. OMS规则已发布: 载荷 OMS规则配置ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 36. OMS规则已停用: 载荷 OMS规则配置ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 37. OMS规则已回滚: 载荷 OMS规则配置ID、订单号、渠道、客户、SKU、数量、金额、状态
-- 38. 仓库已启用: 载荷 刷新规则可选仓库范围



-- 业务表

CREATE TABLE `oms_sales_order` (
  `sales_order_id` bigint NOT NULL COMMENT '销售订单主键',
  `sales_order_no` varchar(64) NOT NULL COMMENT '内部销售订单号',
  `external_order_no` varchar(128) NOT NULL COMMENT '外部订单号',
  `customer_name` varchar(256) NULL COMMENT '客户名称',
  `order_type` smallint NOT NULL COMMENT '普通、预售、换货补发、手工',
  `pay_status` smallint NOT NULL COMMENT '未支付、已支付、部分退款、已退款',
  `audit_status` smallint NOT NULL COMMENT '待审核、通过、异常、驳回',
  `order_status` smallint NOT NULL COMMENT '已创建、待审核、异常待处理、待预占、缺货待处理、已预占、已下发仓库、出库中、已发货、已签收、已完成、已取消',
  `fulfillment_status` smallint NOT NULL COMMENT '未履约、部分履约、履约中、已履约、履约失败',
  `total_amount` decimal(18,2) NOT NULL COMMENT '订单总额',
  `discount_amount` decimal(18,2) NOT NULL COMMENT '优惠金额',
  `pay_amount` decimal(18,2) NOT NULL COMMENT '实付金额',
  `receiver_name` varchar(128) NOT NULL COMMENT '收货人',
  `receiver_mobile` varchar(32) NOT NULL COMMENT '收货电话',
  `receiver_address` varchar(512) NOT NULL COMMENT '收货地址',
  `paid_at` datetime NULL COMMENT '支付时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`sales_order_id`),
  KEY `idx_oms_sales_order_status_time` (`pay_status`, `updated_at`),
  KEY `idx_oms_sales_order_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='销售订单';

CREATE TABLE `oms_sales_order_line` (
  `sales_order_line_id` bigint NOT NULL COMMENT '销售订单行主键',
  `sku_code` varchar(64) NOT NULL COMMENT 'SKU 编码',
  `sku_name` varchar(256) NOT NULL COMMENT 'SKU 名称',
  `order_qty` decimal(18,4) NOT NULL COMMENT '下单数量',
  `reserved_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '已预占数量',
  `outbound_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '已下发出库数量',
  `shipped_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '已发货数量',
  `returned_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '已退货数量',
  `unit_price` decimal(18,6) NOT NULL COMMENT '商品单价',
  `line_amount` decimal(18,2) NOT NULL COMMENT '行金额',
  `line_status` smallint NOT NULL COMMENT '待审核、待预占、已预占、出库中、已发货、已完成、已取消',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`sales_order_line_id`),
  KEY `idx_oms_sales_order_line_status_time` (`line_status`, `updated_at`),
  KEY `idx_oms_sales_order_line_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='销售订单行';

CREATE TABLE `oms_order_result` (
  `order_result_id` bigint NOT NULL COMMENT '订单审单结果主键',
  `audit_type` smallint NOT NULL COMMENT '商品、客户、地址、价格、风控、信用',
  `audit_result` smallint NOT NULL COMMENT '通过、拦截、警告',
  `exception_reason` varchar(512) NULL COMMENT '异常原因',
  `processed_status` smallint NOT NULL COMMENT '待处理、已放行、已驳回、已修正',
  `processed_by` bigint NULL COMMENT '处理人',
  `processed_at` datetime NULL COMMENT '处理时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`order_result_id`),
  KEY `idx_oms_order_result_status_time` (`processed_status`, `updated_at`),
  KEY `idx_oms_order_result_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单审单结果';

CREATE TABLE `oms_fulfillment` (
  `fulfillment_id` bigint NOT NULL COMMENT '履约单主键',
  `fulfillment_order_no` varchar(64) NOT NULL COMMENT '履约单号',
  `logistics_product_code` varchar(64) NULL COMMENT '物流产品',
  `promise_ship_at` datetime NULL COMMENT '承诺发货时间',
  `promise_arrive_at` datetime NULL COMMENT '承诺送达时间',
  `fulfillment_status` smallint NOT NULL COMMENT '待预占、已预占、待出库、已下发、出库中、已发货、已取消、失败',
  `split_reason` varchar(512) NULL COMMENT '拆单原因',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`fulfillment_id`),
  KEY `idx_oms_fulfillment_status_time` (`fulfillment_status`, `updated_at`),
  KEY `idx_oms_fulfillment_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='履约单';

CREATE TABLE `oms_fulfillment_line` (
  `fulfillment_line_id` bigint NOT NULL COMMENT '履约单行主键',
  `fulfillment_qty` decimal(18,4) NOT NULL COMMENT '履约数量',
  `reserved_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '预占数量',
  `shipped_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '发货数量',
  `line_status` smallint NOT NULL COMMENT '待预占、已预占、待出库、出库中、已发货、已取消',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`fulfillment_line_id`),
  KEY `idx_oms_fulfillment_line_status_time` (`line_status`, `updated_at`),
  KEY `idx_oms_fulfillment_line_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='履约单行';

CREATE TABLE `oms_stock_reservation` (
  `stock_reservation_id` bigint NOT NULL COMMENT '库存预占引用主键',
  `reservation_no` varchar(64) NULL COMMENT '中央库存预占号',
  `reserve_qty` decimal(18,4) NOT NULL COMMENT '请求预占数量',
  `reserved_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '实际预占数量',
  `reservation_status` smallint NOT NULL COMMENT '待预占、预占成功、预占失败、已释放、已扣减',
  `fail_reason` varchar(512) NULL COMMENT '失败原因',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`stock_reservation_id`),
  KEY `idx_oms_stock_reservation_status_time` (`reservation_status`, `updated_at`),
  KEY `idx_oms_stock_reservation_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存预占引用';

CREATE TABLE `oms_outbound` (
  `outbound_id` bigint NOT NULL COMMENT '出库单主键',
  `outbound_order_no` varchar(64) NOT NULL COMMENT 'OMS 出库单号',
  `outbound_type` smallint NOT NULL COMMENT '销售出库、换货补发、手工补发',
  `wms_order_no` varchar(64) NULL COMMENT 'WMS 出库单号',
  `outbound_status` smallint NOT NULL COMMENT '草稿、已下发、WMS已接单、拣货中、已发货、已取消、异常',
  `released_at` datetime NULL COMMENT '下发时间',
  `shipped_at` datetime NULL COMMENT '发货时间',
  `cancel_reason` varchar(512) NULL COMMENT '取消原因',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`outbound_id`),
  KEY `idx_oms_outbound_status_time` (`outbound_status`, `updated_at`),
  KEY `idx_oms_outbound_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出库单';

CREATE TABLE `oms_outbound_line` (
  `outbound_line_id` bigint NOT NULL COMMENT '出库单行主键',
  `planned_qty` decimal(18,4) NOT NULL COMMENT '计划出库数量',
  `shipped_qty` decimal(18,4) NOT NULL DEFAULT 0 COMMENT '实际发货数量',
  `line_status` smallint NOT NULL COMMENT '待下发、已下发、拣货中、已发货、已取消',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`outbound_line_id`),
  KEY `idx_oms_outbound_line_status_time` (`line_status`, `updated_at`),
  KEY `idx_oms_outbound_line_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出库单行';

CREATE TABLE `oms_cancel` (
  `cancel_id` bigint NOT NULL COMMENT '取消申请主键',
  `cancel_request_no` varchar(64) NOT NULL COMMENT '取消申请号',
  `cancel_source` smallint NOT NULL COMMENT '客户、客服、渠道、系统',
  `cancel_reason` smallint NOT NULL COMMENT '不想要、地址错误、缺货、风控、其他',
  `cancel_status` smallint NOT NULL COMMENT '待审核、已同意、已拒绝、取消中、已完成、转售后',
  `wms_cancel_status` smallint NULL COMMENT '未请求、请求中、成功、失败',
  `stock_release_status` smallint NULL COMMENT '未释放、释放中、已释放、释放失败',
  `processed_at` datetime NULL COMMENT '处理时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`cancel_id`),
  KEY `idx_oms_cancel_status_time` (`cancel_status`, `updated_at`),
  KEY `idx_oms_cancel_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='取消申请';

CREATE TABLE `oms_after_sale` (
  `after_sale_id` bigint NOT NULL COMMENT '售后单主键',
  `after_sale_no` varchar(64) NOT NULL COMMENT '售后单号',
  `after_sale_type` smallint NOT NULL COMMENT '仅退款、退货退款、换货补发',
  `after_sale_reason` smallint NOT NULL COMMENT '质量问题、错发、少发、不想要、其他',
  `refund_amount` decimal(18,2) NULL COMMENT '退款金额',
  `after_sale_status` smallint NOT NULL COMMENT '已创建、待审核、审核驳回、待退货、待验收、待退款、待补发、异常待处理、已完成、已关闭',
  `approved_at` datetime NULL COMMENT '审核时间',
  `completed_at` datetime NULL COMMENT '完成时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`after_sale_id`),
  KEY `idx_oms_after_sale_status_time` (`after_sale_status`, `updated_at`),
  KEY `idx_oms_after_sale_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='售后单';

CREATE TABLE `oms_after_sale_line` (
  `after_sale_line_id` bigint NOT NULL COMMENT '售后单行主键',
  `apply_qty` decimal(18,4) NOT NULL COMMENT '申请数量',
  `received_qty` decimal(18,4) NULL COMMENT '退货入库数量',
  `accepted_qty` decimal(18,4) NULL COMMENT '验收通过数量',
  `reship_qty` decimal(18,4) NULL COMMENT '补发数量',
  `line_status` smallint NOT NULL COMMENT '待审核、待退货、待验收、待退款、待补发、已完成',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`after_sale_line_id`),
  KEY `idx_oms_after_sale_line_status_time` (`line_status`, `updated_at`),
  KEY `idx_oms_after_sale_line_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='售后单行';

CREATE TABLE `oms_oms_rule` (
  `oms_rule_id` bigint NOT NULL COMMENT 'OMS 规则配置主键',
  `rule_code` varchar(64) NOT NULL COMMENT '规则编码',
  `rule_name` varchar(128) NOT NULL COMMENT '规则名称',
  `rule_type` smallint NOT NULL COMMENT '审单、分仓、取消、售后、承运商',
  `priority` int NOT NULL COMMENT '优先级',
  `condition_config` text NOT NULL COMMENT '条件配置',
  `action_config` text NOT NULL COMMENT '动作配置',
  `status` smallint NOT NULL COMMENT '启用、停用',
  `effective_from` datetime NULL COMMENT '生效时间',
  `effective_to` datetime NULL COMMENT '失效时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`oms_rule_id`),
  KEY `idx_oms_oms_rule_status_time` (`status`, `updated_at`),
  KEY `idx_oms_oms_rule_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OMS 规则配置';



SET FOREIGN_KEY_CHECKS = 1;



-- source: docs/05-子系统数据库设计/ddl/06-BMS系统.sql

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



-- source: docs/05-子系统数据库设计/ddl/09-TMS系统.sql

-- TMS系统 数据库 DDL
-- 口径：MySQL 8.0 / InnoDB / utf8mb4

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `tms_enum_type` (
  `enum_type_id` bigint NOT NULL COMMENT '枚举类型ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_type_name` varchar(128) NOT NULL COMMENT '枚举类型名称',
  `configurable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许页面配置显示名、排序、颜色和启停用',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_type_id`),
  UNIQUE KEY `uk_tms_enum_type_code` (`enum_type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='TMS枚举类型';

CREATE TABLE `tms_enum_item` (
  `enum_item_id` bigint NOT NULL COMMENT '枚举项ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_value` smallint NOT NULL COMMENT '枚举数值，从1开始',
  `enum_label` varchar(128) NOT NULL COMMENT '枚举显示名',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_item_id`),
  UNIQUE KEY `uk_tms_enum_item` (`enum_type_code`, `enum_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='TMS枚举项';

CREATE TABLE `tms_transport_task` (
  `transport_task_id` bigint NOT NULL COMMENT '运输任务ID',
  `transport_task_no` varchar(64) NOT NULL COMMENT '运输任务号',
  `source_system` smallint NOT NULL COMMENT '来源系统：1 OMS 2 WMS 3 采购 4 供应商 5 中央库存 6 人工',
  `source_order_no` varchar(64) NOT NULL COMMENT '来源单号',
  `source_order_type` smallint NOT NULL COMMENT '来源单类型：1销售出库 2销售退货 3采购到货 4退供应商 5调拨',
  `transport_scenario` smallint NOT NULL COMMENT '运输场景：1采购到货 2销售发货 3销售退货 4退供应商 5调拨运输',
  `task_status` smallint NOT NULL COMMENT '任务状态：1待接单 2已接单 3已创建运单 4运输中 5已签收 6已拒收 7已取消 8异常中',
  `carrier_id` bigint NULL COMMENT '物流商ID',
  `carrier_code` varchar(64) NULL COMMENT '物流商编码',
  `logistics_product_code` varchar(64) NULL COMMENT '物流产品编码',
  `shipper_name` varchar(128) NOT NULL COMMENT '发货方名称',
  `shipper_phone` varchar(64) NULL COMMENT '发货方电话',
  `shipper_address` varchar(512) NOT NULL COMMENT '发货地址',
  `receiver_name` varchar(128) NOT NULL COMMENT '收货方名称',
  `receiver_phone` varchar(64) NULL COMMENT '收货方电话',
  `receiver_address` varchar(512) NOT NULL COMMENT '收货地址',
  `package_count` int NOT NULL DEFAULT 0 COMMENT '包裹数',
  `total_weight` decimal(18,4) NULL COMMENT '总重量',
  `total_volume` decimal(18,4) NULL COMMENT '总体积',
  `expected_ship_at` datetime NULL COMMENT '预计发货时间',
  `expected_arrive_at` datetime NULL COMMENT '预计到达时间',
  `accepted_at` datetime NULL COMMENT '接单时间',
  `cancel_reason` varchar(512) NULL COMMENT '取消原因',
  `remark` varchar(512) NULL COMMENT '备注',
  `tenant_id` bigint NULL COMMENT '租户ID',
  `org_id` bigint NULL COMMENT '组织ID',
  `owner_id` bigint NULL COMMENT '货主ID',
  `warehouse_id` bigint NULL COMMENT '仓库ID',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`transport_task_id`),
  UNIQUE KEY `uk_tms_transport_task_no` (`transport_task_no`),
  UNIQUE KEY `uk_tms_transport_task_source` (`source_system`, `source_order_no`, `transport_scenario`),
  KEY `idx_tms_transport_task_status_time` (`task_status`, `updated_at`),
  KEY `idx_tms_transport_task_scope` (`org_id`, `warehouse_id`, `owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='运输任务';

CREATE TABLE `tms_waybill` (
  `waybill_id` bigint NOT NULL COMMENT '运单ID',
  `waybill_no` varchar(64) NOT NULL COMMENT 'TMS运单号',
  `transport_task_id` bigint NULL COMMENT '运输任务ID',
  `transport_task_no` varchar(64) NULL COMMENT '运输任务号',
  `carrier_id` bigint NOT NULL COMMENT '物流商ID',
  `carrier_code` varchar(64) NOT NULL COMMENT '物流商编码',
  `carrier_waybill_no` varchar(128) NULL COMMENT '承运商单号',
  `logistics_product_code` varchar(64) NULL COMMENT '物流产品编码',
  `source_system` smallint NOT NULL COMMENT '来源系统',
  `source_order_no` varchar(64) NOT NULL COMMENT '来源单号',
  `transport_scenario` smallint NOT NULL COMMENT '运输场景',
  `waybill_status` smallint NOT NULL COMMENT '运单状态：1待下单 2已下单 3已揽收 4运输中 5已到达 6已签收 7已拒收 8已取消 9异常中',
  `latest_track_desc` varchar(512) NULL COMMENT '最新轨迹描述',
  `latest_track_at` datetime NULL COMMENT '最新轨迹时间',
  `shipper_address` varchar(512) NOT NULL COMMENT '发货地址快照',
  `receiver_address` varchar(512) NOT NULL COMMENT '收货地址快照',
  `package_count` int NOT NULL DEFAULT 0 COMMENT '包裹数',
  `charge_weight` decimal(18,4) NULL COMMENT '计费重量',
  `total_weight` decimal(18,4) NULL COMMENT '总重量',
  `total_volume` decimal(18,4) NULL COMMENT '总体积',
  `ordered_at` datetime NULL COMMENT '承运商下单时间',
  `pickup_at` datetime NULL COMMENT '揽收时间',
  `shipped_at` datetime NULL COMMENT '发货时间',
  `arrived_at` datetime NULL COMMENT '到达时间',
  `signed_at` datetime NULL COMMENT '签收时间',
  `cancelled_at` datetime NULL COMMENT '取消时间',
  `cancel_reason` varchar(512) NULL COMMENT '取消原因',
  `exception_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否存在未关闭异常',
  `fee_source_status` smallint NOT NULL DEFAULT 1 COMMENT '费用来源状态：1待生成 2已生成 3已推送 4推送失败 5已作废',
  `tenant_id` bigint NULL COMMENT '租户ID',
  `org_id` bigint NULL COMMENT '组织ID',
  `owner_id` bigint NULL COMMENT '货主ID',
  `warehouse_id` bigint NULL COMMENT '仓库ID',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`waybill_id`),
  UNIQUE KEY `uk_tms_waybill_no` (`waybill_no`),
  UNIQUE KEY `uk_tms_waybill_carrier_no` (`carrier_code`, `carrier_waybill_no`),
  KEY `idx_tms_waybill_source` (`source_system`, `source_order_no`),
  KEY `idx_tms_waybill_status_time` (`waybill_status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='运单';

CREATE TABLE `tms_waybill_package` (
  `package_id` bigint NOT NULL COMMENT '包裹ID',
  `package_no` varchar(64) NOT NULL COMMENT 'TMS包裹号',
  `waybill_id` bigint NOT NULL COMMENT '运单ID',
  `waybill_no` varchar(64) NOT NULL COMMENT '运单号',
  `external_package_no` varchar(128) NULL COMMENT '来源系统包裹号',
  `package_status` smallint NOT NULL COMMENT '包裹状态：1待面单 2已生成面单 3已打印 4已交接 5已取消',
  `weight` decimal(18,4) NULL COMMENT '实重',
  `volume` decimal(18,4) NULL COMMENT '体积',
  `length_cm` decimal(18,4) NULL COMMENT '长',
  `width_cm` decimal(18,4) NULL COMMENT '宽',
  `height_cm` decimal(18,4) NULL COMMENT '高',
  `label_id` bigint NULL COMMENT '面单ID',
  `label_no` varchar(64) NULL COMMENT '面单号',
  `handover_batch_no` varchar(64) NULL COMMENT '交接批次',
  `handover_at` datetime NULL COMMENT '交接时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`package_id`),
  UNIQUE KEY `uk_tms_waybill_package_no` (`package_no`),
  KEY `idx_tms_waybill_package_waybill` (`waybill_id`),
  KEY `idx_tms_waybill_package_status_time` (`package_status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='运单包裹';

CREATE TABLE `tms_shipping_label` (
  `label_id` bigint NOT NULL COMMENT '面单ID',
  `label_no` varchar(64) NOT NULL COMMENT 'TMS面单号',
  `waybill_id` bigint NOT NULL COMMENT '运单ID',
  `waybill_no` varchar(64) NOT NULL COMMENT '运单号',
  `package_no` varchar(64) NULL COMMENT '包裹号',
  `carrier_code` varchar(64) NOT NULL COMMENT '物流商编码',
  `carrier_waybill_no` varchar(128) NULL COMMENT '承运商单号',
  `label_status` smallint NOT NULL COMMENT '面单状态：1待生成 2已生成 3已打印 4已作废 5生成失败',
  `label_url` varchar(512) NULL COMMENT '面单文件地址',
  `label_data_json` json NULL COMMENT '面单原始数据',
  `print_count` int NOT NULL DEFAULT 0 COMMENT '打印次数',
  `last_printed_by` bigint NULL COMMENT '最后打印人',
  `last_printed_at` datetime NULL COMMENT '最后打印时间',
  `void_reason` varchar(512) NULL COMMENT '作废原因',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`label_id`),
  UNIQUE KEY `uk_tms_shipping_label_no` (`label_no`),
  KEY `idx_tms_shipping_label_waybill` (`waybill_id`),
  KEY `idx_tms_shipping_label_status_time` (`label_status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='面单';

CREATE TABLE `tms_tracking_event` (
  `tracking_event_id` bigint NOT NULL COMMENT '轨迹ID',
  `waybill_id` bigint NOT NULL COMMENT '运单ID',
  `waybill_no` varchar(64) NOT NULL COMMENT '运单号',
  `carrier_code` varchar(64) NOT NULL COMMENT '物流商编码',
  `carrier_waybill_no` varchar(128) NULL COMMENT '承运商单号',
  `track_node` smallint NOT NULL COMMENT '轨迹节点：1下单 2揽收 3发出 4到达 5派送 6签收 7拒收 8异常',
  `track_desc` varchar(512) NOT NULL COMMENT '轨迹描述',
  `track_location` varchar(256) NULL COMMENT '轨迹地点',
  `track_at` datetime NOT NULL COMMENT '轨迹发生时间',
  `sync_source` smallint NOT NULL COMMENT '同步来源：1承运商回调 2主动拉取 3人工补录',
  `raw_payload_json` json NULL COMMENT '原始报文',
  `idempotent_key` varchar(256) NOT NULL COMMENT '幂等键',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`tracking_event_id`),
  UNIQUE KEY `uk_tms_tracking_event_idem` (`idempotent_key`),
  KEY `idx_tms_tracking_event_waybill_time` (`waybill_id`, `track_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物流轨迹';

CREATE TABLE `tms_delivery_receipt` (
  `receipt_id` bigint NOT NULL COMMENT '签收回单ID',
  `receipt_no` varchar(64) NOT NULL COMMENT '签收回单号',
  `waybill_id` bigint NOT NULL COMMENT '运单ID',
  `waybill_no` varchar(64) NOT NULL COMMENT '运单号',
  `receipt_result` smallint NOT NULL COMMENT '签收结果：1已签收 2已拒收 3部分签收 4签收冲突',
  `signed_by` varchar(128) NULL COMMENT '签收人',
  `signed_at` datetime NULL COMMENT '签收时间',
  `reject_reason` varchar(512) NULL COMMENT '拒收原因',
  `proof_url` varchar(512) NULL COMMENT '签收证明',
  `corrected_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否修正过',
  `correct_reason` varchar(512) NULL COMMENT '修正原因',
  `notified_source_status` smallint NOT NULL DEFAULT 1 COMMENT '通知来源系统状态：1待通知 2已通知 3通知失败',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`receipt_id`),
  UNIQUE KEY `uk_tms_delivery_receipt_no` (`receipt_no`),
  KEY `idx_tms_delivery_receipt_waybill` (`waybill_id`),
  KEY `idx_tms_delivery_receipt_result_time` (`receipt_result`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='签收回单';

CREATE TABLE `tms_logistics_exception` (
  `exception_id` bigint NOT NULL COMMENT '异常ID',
  `exception_no` varchar(64) NOT NULL COMMENT '异常单号',
  `waybill_id` bigint NOT NULL COMMENT '运单ID',
  `waybill_no` varchar(64) NOT NULL COMMENT '运单号',
  `source_order_no` varchar(64) NULL COMMENT '来源单号',
  `exception_type` smallint NOT NULL COMMENT '异常类型：1延误 2破损 3丢失 4拒收 5接口失败 6费用异常',
  `exception_level` smallint NOT NULL COMMENT '异常等级：1一般 2严重 3紧急',
  `responsible_party` smallint NULL COMMENT '责任方：1物流商 2仓库 3客户 4供应商 5系统',
  `exception_status` smallint NOT NULL COMMENT '异常状态：1已创建 2处理中 3已升级 4已关闭',
  `description` varchar(1024) NULL COMMENT '异常说明',
  `handler_id` bigint NULL COMMENT '当前处理人',
  `handle_result` varchar(1024) NULL COMMENT '处理结果',
  `fee_impact_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否影响费用',
  `closed_at` datetime NULL COMMENT '关闭时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`exception_id`),
  UNIQUE KEY `uk_tms_logistics_exception_no` (`exception_no`),
  KEY `idx_tms_logistics_exception_waybill` (`waybill_id`),
  KEY `idx_tms_logistics_exception_status_time` (`exception_status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物流异常';

CREATE TABLE `tms_fee_source` (
  `fee_source_id` bigint NOT NULL COMMENT '费用来源ID',
  `fee_source_no` varchar(64) NOT NULL COMMENT '费用来源号',
  `waybill_id` bigint NOT NULL COMMENT '运单ID',
  `waybill_no` varchar(64) NOT NULL COMMENT '运单号',
  `carrier_id` bigint NOT NULL COMMENT '物流商ID',
  `carrier_code` varchar(64) NOT NULL COMMENT '物流商编码',
  `source_order_no` varchar(64) NULL COMMENT '来源单号',
  `transport_scenario` smallint NOT NULL COMMENT '运输场景',
  `fee_source_status` smallint NOT NULL COMMENT '费用来源状态：1待生成 2已生成 3已推送 4推送失败 5已作废',
  `settlement_direction` smallint NOT NULL COMMENT '结算方向：1应付 2应收',
  `charge_weight` decimal(18,4) NULL COMMENT '计费重量',
  `base_fee` decimal(18,2) NOT NULL DEFAULT 0 COMMENT '基础运费',
  `extra_fee` decimal(18,2) NOT NULL DEFAULT 0 COMMENT '附加费',
  `deduction_fee` decimal(18,2) NOT NULL DEFAULT 0 COMMENT '扣罚金额',
  `total_amount` decimal(18,2) NOT NULL COMMENT '总金额',
  `currency` varchar(16) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `fee_items_json` json NULL COMMENT '费用项明细',
  `generated_at` datetime NULL COMMENT '生成时间',
  `pushed_at` datetime NULL COMMENT '推送BMS时间',
  `bms_receive_no` varchar(64) NULL COMMENT 'BMS接收编号',
  `push_fail_reason` varchar(1024) NULL COMMENT '推送失败原因',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`fee_source_id`),
  UNIQUE KEY `uk_tms_fee_source_no` (`fee_source_no`),
  UNIQUE KEY `uk_tms_fee_source_waybill` (`waybill_id`),
  KEY `idx_tms_fee_source_status_time` (`fee_source_status`, `updated_at`),
  KEY `idx_tms_fee_source_carrier` (`carrier_id`, `generated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物流费用来源';

CREATE TABLE `tms_carrier_integration_log` (
  `integration_log_id` bigint NOT NULL COMMENT '承运商接口日志ID',
  `carrier_code` varchar(64) NOT NULL COMMENT '物流商编码',
  `interface_type` smallint NOT NULL COMMENT '接口类型：1下单 2取消 3面单 4轨迹同步 5回调 6费用',
  `biz_no` varchar(128) NULL COMMENT '业务单号',
  `request_id` varchar(128) NULL COMMENT '请求ID',
  `request_payload` json NULL COMMENT '请求报文',
  `response_payload` json NULL COMMENT '响应报文',
  `call_status` smallint NOT NULL COMMENT '调用状态：1成功 2失败',
  `http_status` int NULL COMMENT 'HTTP状态码',
  `fail_reason` varchar(1024) NULL COMMENT '失败原因',
  `called_at` datetime NOT NULL COMMENT '调用时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`integration_log_id`),
  KEY `idx_tms_carrier_integration_biz` (`biz_no`, `interface_type`),
  KEY `idx_tms_carrier_integration_time` (`carrier_code`, `called_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='承运商接口日志';

CREATE TABLE `tms_domain_event` (
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
  UNIQUE KEY `uk_tms_domain_event_code` (`event_code`),
  KEY `idx_tms_domain_event_status` (`event_status`, `created_at`),
  KEY `idx_tms_domain_event_aggregate` (`aggregate_type`, `aggregate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='TMS领域事件发布表';

CREATE TABLE `tms_event_consume_log` (
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
  UNIQUE KEY `uk_tms_event_consume` (`source_system`, `event_code`, `consumer_name`),
  UNIQUE KEY `uk_tms_event_consume_idem` (`idempotent_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='TMS事件消费日志';

CREATE TABLE `tms_operation_audit_log` (
  `operation_log_id` bigint NOT NULL COMMENT '操作日志ID',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(128) NULL COMMENT '操作人名称',
  `operation_type` varchar(64) NOT NULL COMMENT '操作类型',
  `target_type` varchar(128) NOT NULL COMMENT '操作对象类型',
  `target_id` bigint NULL COMMENT '操作对象ID',
  `target_no` varchar(128) NULL COMMENT '操作对象单号或编码',
  `before_snapshot` json NULL COMMENT '操作前快照',
  `after_snapshot` json NULL COMMENT '操作后快照',
  `result` smallint NOT NULL COMMENT '结果：1成功 2失败',
  `fail_reason` varchar(1024) NULL COMMENT '失败原因',
  `request_id` varchar(128) NULL COMMENT '请求ID',
  `operation_at` datetime NOT NULL COMMENT '操作时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`operation_log_id`),
  KEY `idx_tms_operation_audit_target` (`target_type`, `target_no`),
  KEY `idx_tms_operation_audit_operator` (`operator_id`, `operation_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='TMS操作审计日志';

INSERT INTO `tms_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES
(9001, 'TMS_SOURCE_SYSTEM', 'TMS来源系统', 1, 1),
(9002, 'TMS_SOURCE_ORDER_TYPE', 'TMS来源单类型', 1, 1),
(9003, 'TRANSPORT_SCENARIO', '运输场景', 1, 1),
(9004, 'TRANSPORT_TASK_STATUS', '运输任务状态', 0, 1),
(9005, 'WAYBILL_STATUS', '运单状态', 0, 1),
(9006, 'PACKAGE_STATUS', '包裹状态', 0, 1),
(9007, 'LABEL_STATUS', '面单状态', 0, 1),
(9008, 'TRACK_NODE', '轨迹节点', 1, 1),
(9009, 'TRACK_SYNC_SOURCE', '轨迹同步来源', 1, 1),
(9010, 'RECEIPT_RESULT', '签收结果', 0, 1),
(9011, 'LOGISTICS_EXCEPTION_TYPE', '物流异常类型', 1, 1),
(9012, 'EXCEPTION_LEVEL', '异常等级', 1, 1),
(9013, 'EXCEPTION_STATUS', '异常状态', 0, 1),
(9014, 'RESPONSIBLE_PARTY', '责任方', 1, 1),
(9015, 'FEE_SOURCE_STATUS', '费用来源状态', 0, 1),
(9016, 'SETTLEMENT_DIRECTION', '结算方向', 1, 1),
(9017, 'NOTIFY_STATUS', '通知状态', 0, 1),
(9018, 'EVENT_STATUS', '事件状态', 0, 1),
(9019, 'CONSUME_STATUS', '消费状态', 0, 1);

INSERT INTO `tms_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES
(900101, 'TMS_SOURCE_SYSTEM', 1, 'OMS', 1, 1),
(900102, 'TMS_SOURCE_SYSTEM', 2, 'WMS', 2, 1),
(900103, 'TMS_SOURCE_SYSTEM', 3, '采购系统', 3, 1),
(900104, 'TMS_SOURCE_SYSTEM', 4, '供应商系统', 4, 1),
(900105, 'TMS_SOURCE_SYSTEM', 5, '中央库存系统', 5, 1),
(900106, 'TMS_SOURCE_SYSTEM', 6, '人工创建', 6, 1),
(900201, 'TMS_SOURCE_ORDER_TYPE', 1, '销售出库', 1, 1),
(900202, 'TMS_SOURCE_ORDER_TYPE', 2, '销售退货', 2, 1),
(900203, 'TMS_SOURCE_ORDER_TYPE', 3, '采购到货', 3, 1),
(900204, 'TMS_SOURCE_ORDER_TYPE', 4, '退供应商', 4, 1),
(900205, 'TMS_SOURCE_ORDER_TYPE', 5, '调拨', 5, 1),
(900301, 'TRANSPORT_SCENARIO', 1, '采购到货', 1, 1),
(900302, 'TRANSPORT_SCENARIO', 2, '销售发货', 2, 1),
(900303, 'TRANSPORT_SCENARIO', 3, '销售退货', 3, 1),
(900304, 'TRANSPORT_SCENARIO', 4, '退供应商', 4, 1),
(900305, 'TRANSPORT_SCENARIO', 5, '调拨运输', 5, 1),
(900401, 'TRANSPORT_TASK_STATUS', 1, '待接单', 1, 1),
(900402, 'TRANSPORT_TASK_STATUS', 2, '已接单', 2, 1),
(900403, 'TRANSPORT_TASK_STATUS', 3, '已创建运单', 3, 1),
(900404, 'TRANSPORT_TASK_STATUS', 4, '运输中', 4, 1),
(900405, 'TRANSPORT_TASK_STATUS', 5, '已签收', 5, 1),
(900406, 'TRANSPORT_TASK_STATUS', 6, '已拒收', 6, 1),
(900407, 'TRANSPORT_TASK_STATUS', 7, '已取消', 7, 1),
(900408, 'TRANSPORT_TASK_STATUS', 8, '异常中', 8, 1),
(900501, 'WAYBILL_STATUS', 1, '待下单', 1, 1),
(900502, 'WAYBILL_STATUS', 2, '已下单', 2, 1),
(900503, 'WAYBILL_STATUS', 3, '已揽收', 3, 1),
(900504, 'WAYBILL_STATUS', 4, '运输中', 4, 1),
(900505, 'WAYBILL_STATUS', 5, '已到达', 5, 1),
(900506, 'WAYBILL_STATUS', 6, '已签收', 6, 1),
(900507, 'WAYBILL_STATUS', 7, '已拒收', 7, 1),
(900508, 'WAYBILL_STATUS', 8, '已取消', 8, 1),
(900509, 'WAYBILL_STATUS', 9, '异常中', 9, 1),
(900601, 'PACKAGE_STATUS', 1, '待面单', 1, 1),
(900602, 'PACKAGE_STATUS', 2, '已生成面单', 2, 1),
(900603, 'PACKAGE_STATUS', 3, '已打印', 3, 1),
(900604, 'PACKAGE_STATUS', 4, '已交接', 4, 1),
(900605, 'PACKAGE_STATUS', 5, '已取消', 5, 1),
(900701, 'LABEL_STATUS', 1, '待生成', 1, 1),
(900702, 'LABEL_STATUS', 2, '已生成', 2, 1),
(900703, 'LABEL_STATUS', 3, '已打印', 3, 1),
(900704, 'LABEL_STATUS', 4, '已作废', 4, 1),
(900705, 'LABEL_STATUS', 5, '生成失败', 5, 1),
(900801, 'TRACK_NODE', 1, '下单', 1, 1),
(900802, 'TRACK_NODE', 2, '揽收', 2, 1),
(900803, 'TRACK_NODE', 3, '发出', 3, 1),
(900804, 'TRACK_NODE', 4, '到达', 4, 1),
(900805, 'TRACK_NODE', 5, '派送', 5, 1),
(900806, 'TRACK_NODE', 6, '签收', 6, 1),
(900807, 'TRACK_NODE', 7, '拒收', 7, 1),
(900808, 'TRACK_NODE', 8, '异常', 8, 1),
(900901, 'TRACK_SYNC_SOURCE', 1, '承运商回调', 1, 1),
(900902, 'TRACK_SYNC_SOURCE', 2, '主动拉取', 2, 1),
(900903, 'TRACK_SYNC_SOURCE', 3, '人工补录', 3, 1),
(901001, 'RECEIPT_RESULT', 1, '已签收', 1, 1),
(901002, 'RECEIPT_RESULT', 2, '已拒收', 2, 1),
(901003, 'RECEIPT_RESULT', 3, '部分签收', 3, 1),
(901004, 'RECEIPT_RESULT', 4, '签收冲突', 4, 1),
(901101, 'LOGISTICS_EXCEPTION_TYPE', 1, '延误', 1, 1),
(901102, 'LOGISTICS_EXCEPTION_TYPE', 2, '破损', 2, 1),
(901103, 'LOGISTICS_EXCEPTION_TYPE', 3, '丢失', 3, 1),
(901104, 'LOGISTICS_EXCEPTION_TYPE', 4, '拒收', 4, 1),
(901105, 'LOGISTICS_EXCEPTION_TYPE', 5, '接口失败', 5, 1),
(901106, 'LOGISTICS_EXCEPTION_TYPE', 6, '费用异常', 6, 1),
(901201, 'EXCEPTION_LEVEL', 1, '一般', 1, 1),
(901202, 'EXCEPTION_LEVEL', 2, '严重', 2, 1),
(901203, 'EXCEPTION_LEVEL', 3, '紧急', 3, 1),
(901301, 'EXCEPTION_STATUS', 1, '已创建', 1, 1),
(901302, 'EXCEPTION_STATUS', 2, '处理中', 2, 1),
(901303, 'EXCEPTION_STATUS', 3, '已升级', 3, 1),
(901304, 'EXCEPTION_STATUS', 4, '已关闭', 4, 1),
(901401, 'RESPONSIBLE_PARTY', 1, '物流商', 1, 1),
(901402, 'RESPONSIBLE_PARTY', 2, '仓库', 2, 1),
(901403, 'RESPONSIBLE_PARTY', 3, '客户', 3, 1),
(901404, 'RESPONSIBLE_PARTY', 4, '供应商', 4, 1),
(901405, 'RESPONSIBLE_PARTY', 5, '系统', 5, 1),
(901501, 'FEE_SOURCE_STATUS', 1, '待生成', 1, 1),
(901502, 'FEE_SOURCE_STATUS', 2, '已生成', 2, 1),
(901503, 'FEE_SOURCE_STATUS', 3, '已推送', 3, 1),
(901504, 'FEE_SOURCE_STATUS', 4, '推送失败', 4, 1),
(901505, 'FEE_SOURCE_STATUS', 5, '已作废', 5, 1),
(901601, 'SETTLEMENT_DIRECTION', 1, '应付', 1, 1),
(901602, 'SETTLEMENT_DIRECTION', 2, '应收', 2, 1),
(901701, 'NOTIFY_STATUS', 1, '待通知', 1, 1),
(901702, 'NOTIFY_STATUS', 2, '已通知', 2, 1),
(901703, 'NOTIFY_STATUS', 3, '通知失败', 3, 1),
(901801, 'EVENT_STATUS', 1, '待发布', 1, 1),
(901802, 'EVENT_STATUS', 2, '发布中', 2, 1),
(901803, 'EVENT_STATUS', 3, '已发布', 3, 1),
(901804, 'EVENT_STATUS', 4, '发布失败', 4, 1),
(901805, 'EVENT_STATUS', 5, '已取消', 5, 1),
(901901, 'CONSUME_STATUS', 1, '待消费', 1, 1),
(901902, 'CONSUME_STATUS', 2, '处理中', 2, 1),
(901903, 'CONSUME_STATUS', 3, '消费成功', 3, 1),
(901904, 'CONSUME_STATUS', 4, '消费失败', 4, 1),
(901905, 'CONSUME_STATUS', 5, '已忽略', 5, 1);

SET FOREIGN_KEY_CHECKS = 1;


-- source: docs/05-子系统数据库设计/ddl/07-主数据系统.sql

-- 主数据系统 数据库 DDL

-- 口径：MySQL 8.0 / InnoDB / utf8mb4

SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;



CREATE TABLE `mdm_enum_type` (
  `enum_type_id` bigint NOT NULL COMMENT '枚举类型ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_type_name` varchar(128) NOT NULL COMMENT '枚举类型名称',
  `configurable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许页面配置',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_type_id`),
  UNIQUE KEY `uk_mdm_enum_type_code` (`enum_type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='枚举类型';

CREATE TABLE `mdm_enum_item` (
  `enum_item_id` bigint NOT NULL COMMENT '枚举项ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_value` smallint NOT NULL COMMENT '枚举数值，从1开始',
  `enum_label` varchar(128) NOT NULL COMMENT '枚举显示名',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_item_id`),
  UNIQUE KEY `uk_mdm_enum_item` (`enum_type_code`, `enum_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='枚举项';

CREATE TABLE `mdm_domain_event` (
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
  UNIQUE KEY `uk_mdm_domain_event_code` (`event_code`),
  KEY `idx_mdm_domain_event_status` (`event_status`, `created_at`),
  KEY `idx_mdm_domain_event_aggregate` (`aggregate_type`, `aggregate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='领域事件发布表';

CREATE TABLE `mdm_event_consume_log` (
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
  UNIQUE KEY `uk_mdm_event_consume` (`source_system`, `event_code`, `consumer_name`),
  KEY `idx_mdm_event_consume_status` (`consume_status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='事件消费幂等日志';

CREATE TABLE `mdm_operation_audit_log` (
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
  KEY `idx_mdm_operation_audit_target` (`target_type`, `target_id`),
  KEY `idx_mdm_operation_audit_operator` (`operator_id`, `operation_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';



-- 枚举初始化数据
INSERT INTO `mdm_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (1, 'APPROVAL_STATUS', 'APPROVAL_STATUS', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (1, 'APPROVAL_STATUS', 1, '草稿', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (2, 'APPROVAL_STATUS', 2, '待审批', 2, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (3, 'APPROVAL_STATUS', 3, '已批准', 3, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (4, 'APPROVAL_STATUS', 4, '已驳回', 4, 1);
INSERT INTO `mdm_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (2, 'COMMON_STATUS', 'COMMON_STATUS', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (5, 'COMMON_STATUS', 1, '启用', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (6, 'COMMON_STATUS', 2, '停用', 2, 1);
INSERT INTO `mdm_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (3, 'CONSUME_STATUS', 'CONSUME_STATUS', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (7, 'CONSUME_STATUS', 1, '待消费', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (8, 'CONSUME_STATUS', 2, '处理中', 2, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (9, 'CONSUME_STATUS', 3, '消费成功', 3, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (10, 'CONSUME_STATUS', 4, '消费失败', 4, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (11, 'CONSUME_STATUS', 5, '已忽略', 5, 1);
INSERT INTO `mdm_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (4, 'CURRENCY', 'CURRENCY', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (12, 'CURRENCY', 1, '人民币', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (13, 'CURRENCY', 2, '美元', 2, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (14, 'CURRENCY', 3, '欧元', 3, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (15, 'CURRENCY', 4, '港币', 4, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (16, 'CURRENCY', 5, '日元', 5, 1);
INSERT INTO `mdm_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (5, 'EVENT_STATUS', 'EVENT_STATUS', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (17, 'EVENT_STATUS', 1, '待发布', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (18, 'EVENT_STATUS', 2, '发布中', 2, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (19, 'EVENT_STATUS', 3, '已发布', 3, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (20, 'EVENT_STATUS', 4, '发布失败', 4, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (21, 'EVENT_STATUS', 5, '已取消', 5, 1);
INSERT INTO `mdm_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (6, 'FIELD_TYPE', 'FIELD_TYPE', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (22, 'FIELD_TYPE', 1, '字符串', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (23, 'FIELD_TYPE', 2, '数字', 2, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (24, 'FIELD_TYPE', 3, '日期', 3, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (25, 'FIELD_TYPE', 4, '枚举', 4, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (26, 'FIELD_TYPE', 5, '布尔', 5, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (27, 'FIELD_TYPE', 6, '引用', 6, 1);
INSERT INTO `mdm_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (7, 'IMPORT_MODE', 'IMPORT_MODE', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (28, 'IMPORT_MODE', 1, '新增', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (29, 'IMPORT_MODE', 2, '更新', 2, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (30, 'IMPORT_MODE', 3, '覆盖', 3, 1);
INSERT INTO `mdm_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (8, 'IMPORT_STATUS', 'IMPORT_STATUS', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (31, 'IMPORT_STATUS', 1, '待处理', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (32, 'IMPORT_STATUS', 2, '处理中', 2, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (33, 'IMPORT_STATUS', 3, '成功', 3, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (34, 'IMPORT_STATUS', 4, '部分成功', 4, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (35, 'IMPORT_STATUS', 5, '失败', 5, 1);
INSERT INTO `mdm_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (9, 'MASTER_CHANGE_TYPE', 'MASTER_CHANGE_TYPE', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (36, 'MASTER_CHANGE_TYPE', 1, '变更类型', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (37, 'MASTER_CHANGE_TYPE', 2, '新增', 2, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (38, 'MASTER_CHANGE_TYPE', 3, '修改', 3, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (39, 'MASTER_CHANGE_TYPE', 4, '启用', 4, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (40, 'MASTER_CHANGE_TYPE', 5, '停用', 5, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (41, 'MASTER_CHANGE_TYPE', 6, '淘汰', 6, 1);
INSERT INTO `mdm_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (10, 'MASTER_DATA_DOMAIN', 'MASTER_DATA_DOMAIN', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (42, 'MASTER_DATA_DOMAIN', 1, '商品', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (43, 'MASTER_DATA_DOMAIN', 2, '伙伴', 2, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (44, 'MASTER_DATA_DOMAIN', 3, '仓储', 3, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (45, 'MASTER_DATA_DOMAIN', 4, '物流', 4, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (46, 'MASTER_DATA_DOMAIN', 5, '组织', 5, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (47, 'MASTER_DATA_DOMAIN', 6, '财务', 6, 1);
INSERT INTO `mdm_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (11, 'MASTER_DATA_STATUS', 'MASTER_DATA_STATUS', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (48, 'MASTER_DATA_STATUS', 1, '草稿', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (49, 'MASTER_DATA_STATUS', 2, '待审核', 2, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (50, 'MASTER_DATA_STATUS', 3, '已启用', 3, 1);
INSERT INTO `mdm_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (12, 'OPERATION_RESULT', 'OPERATION_RESULT', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (51, 'OPERATION_RESULT', 1, '成功', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (52, 'OPERATION_RESULT', 2, '失败', 2, 1);
INSERT INTO `mdm_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (13, 'PUBLISH_MODE', 'PUBLISH_MODE', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (53, 'PUBLISH_MODE', 1, '事件', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (54, 'PUBLISH_MODE', 2, '批量', 2, 1);
INSERT INTO `mdm_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (14, 'PUBLISH_STATUS', 'PUBLISH_STATUS', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (55, 'PUBLISH_STATUS', 1, '待发布', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (56, 'PUBLISH_STATUS', 2, '发布中', 2, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (57, 'PUBLISH_STATUS', 3, '成功', 3, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (58, 'PUBLISH_STATUS', 4, '失败', 4, 1);
INSERT INTO `mdm_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (15, 'RESET_CYCLE', 'RESET_CYCLE', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (59, 'RESET_CYCLE', 1, '不重置', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (60, 'RESET_CYCLE', 2, '每日', 2, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (61, 'RESET_CYCLE', 3, '每月', 3, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (62, 'RESET_CYCLE', 4, '每年', 4, 1);
INSERT INTO `mdm_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (16, 'TARGET_SYSTEM', 'TARGET_SYSTEM', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (63, 'TARGET_SYSTEM', 1, '库存', 1, 1);
INSERT INTO `mdm_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (64, 'TARGET_SYSTEM', 2, '目标系统', 2, 1);



-- 领域事件类型参考数据
-- 1. 主数据类型已创建: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`草稿`、变更摘要、目标系统、操作者、幂等键
-- 2. 主数据类型已启用: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已启用`、变更摘要、目标系统、操作者、幂等键
-- 3. 主数据类型已变更: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已启用`、变更摘要、目标系统、操作者、幂等键
-- 4. 主数据类型已停用: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已停用`、变更摘要、目标系统、操作者、幂等键
-- 5. 审批已完成: 载荷 根据审批结果启用或驳回类型配置
-- 6. 下游回执已返回: 载荷 更新类型发布确认状态
-- 7. 字段模板已创建: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`草稿`、变更摘要、目标系统、操作者、幂等键
-- 8. 字段模板已发布: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已发布`、变更摘要、目标系统、操作者、幂等键
-- 9. 字段模板已变更: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已发布`、变更摘要、目标系统、操作者、幂等键
-- 10. 字段模板已停用: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已停用`、变更摘要、目标系统、操作者、幂等键
-- 11. 枚举值已变更: 载荷 标记引用枚举字段需要复核
-- 12. 编码规则已创建: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`草稿`、变更摘要、目标系统、操作者、幂等键
-- 13. 编码规则已启用: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已启用`、变更摘要、目标系统、操作者、幂等键
-- 14. 主数据编码已生成: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已使用`、变更摘要、目标系统、操作者、幂等键
-- 15. 编码规则已停用: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已停用`、变更摘要、目标系统、操作者、幂等键
-- 16. 导入任务已创建: 载荷 为缺失编码的导入行分配编码
-- 17. 主数据草稿已创建: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`草稿`、变更摘要、目标系统、操作者、幂等键
-- 18. 主数据已提交审核: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`待审核`、变更摘要、目标系统、操作者、幂等键
-- 19. 主数据已启用: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已启用`、变更摘要、目标系统、操作者、幂等键
-- 20. 主数据已驳回: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已驳回`、变更摘要、目标系统、操作者、幂等键
-- 21. 主数据变更已提交: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`变更待审核`、变更摘要、目标系统、操作者、幂等键
-- 22. 主数据已冻结: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已冻结`、变更摘要、目标系统、操作者、幂等键
-- 23. 主数据已停用: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已停用`、变更摘要、目标系统、操作者、幂等键
-- 24. 数据质量问题已发现: 载荷 标记风险字段或生成治理待办
-- 25. 主数据版本已生成: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`待发布`、变更摘要、目标系统、操作者、幂等键
-- 26. 主数据版本已发布: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已发布`、变更摘要、目标系统、操作者、幂等键
-- 27. 主数据版本已重新发布: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已发布`、变更摘要、目标系统、操作者、幂等键
-- 28. 主数据变更已生效: 载荷 生成变更版本快照
-- 29. 订阅已创建: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已启用`、变更摘要、目标系统、操作者、幂等键
-- 30. 主数据已发布: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`发布中`、变更摘要、目标系统、操作者、幂等键
-- 31. 主数据发布已确认: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已确认`、变更摘要、目标系统、操作者、幂等键
-- 32. 主数据已重新发布: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`发布中`、变更摘要、目标系统、操作者、幂等键
-- 33. 订阅已停用: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已停用`、变更摘要、目标系统、操作者、幂等键
-- 34. 导入文件已校验: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`待执行`、变更摘要、目标系统、操作者、幂等键
-- 35. 导入任务已执行: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`执行中`、变更摘要、目标系统、操作者、幂等键
-- 36. 导入任务已完成: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已完成`、变更摘要、目标系统、操作者、幂等键
-- 37. 导入任务已取消: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已取消`、变更摘要、目标系统、操作者、幂等键
-- 38. 数据质量问题已分派: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`处理中`、变更摘要、目标系统、操作者、幂等键
-- 39. 数据质量问题已修复: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`待验证`、变更摘要、目标系统、操作者、幂等键
-- 40. 数据质量问题已关闭: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`已关闭`、变更摘要、目标系统、操作者、幂等键
-- 41. 数据质量问题已重新打开: 载荷 聚合ID、类型编码、主数据编码、版本号、状态`处理中`、变更摘要、目标系统、操作者、幂等键
-- 42. 主数据已变更: 载荷 重新检测该记录是否仍存在质量风险



-- 业务表

CREATE TABLE `mdm_master_data_type` (
  `master_data_type_id` bigint NOT NULL COMMENT '主数据类型主键',
  `type_code` varchar(64) NOT NULL COMMENT '类型编码，如 `SKU`、`SUPPLIER`',
  `type_name` varchar(128) NOT NULL COMMENT '类型名称',
  `domain` smallint NOT NULL COMMENT '商品、伙伴、仓储、物流、组织、财务',
  `parent_type_code` varchar(64) NULL COMMENT '上级类型，如 SKU 的上级是 SPU',
  `approval_required` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否需要审核',
  `version_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用版本',
  `publish_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否发布给子系统',
  `status` smallint NOT NULL COMMENT '启用、停用',
  `sort_no` int NULL COMMENT '排序',
  `remark` varchar(512) NULL COMMENT '说明',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`master_data_type_id`),
  KEY `idx_mdm_master_data_type_status_time` (`status`, `updated_at`),
  KEY `idx_mdm_master_data_type_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='主数据类型';

CREATE TABLE `mdm_field_definition` (
  `business_object_id` bigint NOT NULL COMMENT '字段定义主键',
  `type_code` varchar(64) NOT NULL COMMENT '主数据类型',
  `field_code` varchar(64) NOT NULL COMMENT '字段编码',
  `field_name` varchar(128) NOT NULL COMMENT '字段名称',
  `field_type` smallint NOT NULL COMMENT '字符串、数字、日期、枚举、布尔、引用',
  `data_length` int NULL COMMENT '字符串长度',
  `decimal_scale` int NULL COMMENT '小数位',
  `required` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否必填',
  `unique_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否唯一',
  `enum_type_code` varchar(64) NULL COMMENT '枚举类型',
  `reference_type_code` varchar(64) NULL COMMENT '引用的主数据类型',
  `default_value` varchar(256) NULL COMMENT '默认值',
  `editable_after_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '启用后是否可改',
  `critical_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否关键字段，关键变更需审批',
  `visible_in_list` tinyint(1) NOT NULL DEFAULT 0 COMMENT '列表是否展示',
  `visible_in_form` tinyint(1) NOT NULL DEFAULT 0 COMMENT '表单是否展示',
  `sort_no` int NOT NULL COMMENT '表单排序',
  `status` smallint NOT NULL COMMENT '启用、停用',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`business_object_id`),
  KEY `idx_mdm_field_definition_status_time` (`status`, `updated_at`),
  KEY `idx_mdm_field_definition_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字段定义';

CREATE TABLE `mdm_code_rule` (
  `code_rule_id` bigint NOT NULL COMMENT '编码规则主键',
  `rule_code` varchar(64) NOT NULL COMMENT '规则编码',
  `rule_name` varchar(128) NOT NULL COMMENT '规则名称',
  `prefix` varchar(32) NULL COMMENT '前缀',
  `date_pattern` varchar(32) NULL COMMENT '日期格式',
  `sequence_length` int NOT NULL COMMENT '流水号长度',
  `current_sequence` bigint NOT NULL DEFAULT 0 COMMENT '当前流水',
  `reset_cycle` smallint NOT NULL COMMENT '不重置、每日、每月、每年',
  `status` smallint NOT NULL COMMENT '启用、停用',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`code_rule_id`),
  KEY `idx_mdm_code_rule_status_time` (`status`, `updated_at`),
  KEY `idx_mdm_code_rule_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='编码规则';

CREATE TABLE `mdm_master_data_record` (
  `master_data_record_id` bigint NOT NULL COMMENT '主数据记录主键',
  `type_code` varchar(64) NOT NULL COMMENT '主数据类型',
  `data_code` varchar(128) NOT NULL COMMENT '主数据编码',
  `data_name` varchar(256) NOT NULL COMMENT '主数据名称',
  `data_payload` text NOT NULL COMMENT '业务字段值',
  `data_status` smallint NOT NULL COMMENT '草稿、待审核、已启用等',
  `approval_status` smallint NOT NULL COMMENT '未提交、审批中、通过、驳回',
  `current_version` int NOT NULL COMMENT '当前版本',
  `effective_from` datetime NULL COMMENT '生效时间',
  `effective_to` datetime NULL COMMENT '失效时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`master_data_record_id`),
  KEY `idx_mdm_master_data_record_status_time` (`data_status`, `updated_at`),
  KEY `idx_mdm_master_data_record_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='主数据记录';

CREATE TABLE `mdm_master_data_change_log` (
  `master_data_id` bigint NOT NULL COMMENT '主数据变更日志主键',
  `type_code` varchar(64) NOT NULL COMMENT '主数据类型',
  `field_code` varchar(64) NULL COMMENT '变更字段',
  `old_value` text NULL COMMENT '旧值',
  `new_value` text NULL COMMENT '新值',
  `critical_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否关键字段',
  `change_type` smallint NOT NULL COMMENT '变更类型',
  `operated_at` datetime NOT NULL COMMENT '操作时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`master_data_id`),
  KEY `idx_mdm_master_data_change_log_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='主数据变更日志';

CREATE TABLE `mdm_master_data_version` (
  `master_data_version_id` bigint NOT NULL COMMENT '主数据版本主键',
  `version_no` int NOT NULL COMMENT '版本号',
  `snapshot_payload` text NOT NULL COMMENT '版本快照',
  `change_type` smallint NOT NULL COMMENT '新增、修改、启用、停用、淘汰',
  `change_summary` varchar(1024) NULL COMMENT '变更摘要',
  `effective_from` datetime NULL COMMENT '生效时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`master_data_version_id`),
  KEY `idx_mdm_master_data_version_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='主数据版本';

CREATE TABLE `mdm_publish_subscribe` (
  `publish_subscribe_id` bigint NOT NULL COMMENT '发布订阅配置主键',
  `type_code` varchar(64) NOT NULL COMMENT '主数据类型',
  `target_system` smallint NOT NULL COMMENT 'OMS、WMS、库存、BMS 等',
  `event_topic` varchar(128) NOT NULL COMMENT '事件主题',
  `publish_mode` smallint NOT NULL COMMENT '事件、API、批量',
  `filter_rule` text NULL COMMENT '发布过滤规则',
  `status` smallint NOT NULL COMMENT '启用、停用',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`publish_subscribe_id`),
  KEY `idx_mdm_publish_subscribe_status_time` (`status`, `updated_at`),
  KEY `idx_mdm_publish_subscribe_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='发布订阅配置';

CREATE TABLE `mdm_publish_log` (
  `business_object_id` bigint NOT NULL COMMENT '发布日志主键',
  `type_code` varchar(64) NOT NULL COMMENT '主数据类型',
  `version_no` int NOT NULL COMMENT '发布版本',
  `target_system` smallint NOT NULL COMMENT '目标系统',
  `event_name` varchar(128) NOT NULL COMMENT '事件名',
  `publish_status` smallint NOT NULL COMMENT '待发布、发布中、成功、失败',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
  `failure_reason` varchar(1024) NULL COMMENT '失败原因',
  `published_at` datetime NULL COMMENT '发布时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`business_object_id`),
  KEY `idx_mdm_publish_log_status_time` (`publish_status`, `updated_at`),
  KEY `idx_mdm_publish_log_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='发布日志';

CREATE TABLE `mdm_import_task` (
  `import_task_id` bigint NOT NULL COMMENT '导入任务主键',
  `type_code` varchar(64) NOT NULL COMMENT '主数据类型',
  `file_name` varchar(256) NOT NULL COMMENT '文件名',
  `import_mode` smallint NOT NULL COMMENT '新增、更新、覆盖',
  `task_status` smallint NOT NULL COMMENT '待处理、处理中、成功、部分成功、失败',
  `total_count` int NOT NULL DEFAULT 0 COMMENT '总行数',
  `success_count` int NOT NULL DEFAULT 0 COMMENT '成功数',
  `failed_count` int NOT NULL DEFAULT 0 COMMENT '失败数',
  `error_file_url` varchar(512) NULL COMMENT '错误文件',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`import_task_id`),
  KEY `idx_mdm_import_task_status_time` (`task_status`, `updated_at`),
  KEY `idx_mdm_import_task_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='导入任务';



SET FOREIGN_KEY_CHECKS = 1;



-- source: docs/05-子系统数据库设计/ddl/08-权限系统.sql

-- 权限系统 数据库 DDL

-- 口径：MySQL 8.0 / InnoDB / utf8mb4

SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;



CREATE TABLE `iam_enum_type` (
  `enum_type_id` bigint NOT NULL COMMENT '枚举类型ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_type_name` varchar(128) NOT NULL COMMENT '枚举类型名称',
  `configurable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许页面配置',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_type_id`),
  UNIQUE KEY `uk_iam_enum_type_code` (`enum_type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='枚举类型';

CREATE TABLE `iam_enum_item` (
  `enum_item_id` bigint NOT NULL COMMENT '枚举项ID',
  `enum_type_code` varchar(64) NOT NULL COMMENT '枚举类型编码',
  `enum_value` smallint NOT NULL COMMENT '枚举数值，从1开始',
  `enum_label` varchar(128) NOT NULL COMMENT '枚举显示名',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` smallint NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`enum_item_id`),
  UNIQUE KEY `uk_iam_enum_item` (`enum_type_code`, `enum_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='枚举项';

CREATE TABLE `iam_domain_event` (
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
  UNIQUE KEY `uk_iam_domain_event_code` (`event_code`),
  KEY `idx_iam_domain_event_status` (`event_status`, `created_at`),
  KEY `idx_iam_domain_event_aggregate` (`aggregate_type`, `aggregate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='领域事件发布表';

CREATE TABLE `iam_event_consume_log` (
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
  UNIQUE KEY `uk_iam_event_consume` (`source_system`, `event_code`, `consumer_name`),
  KEY `idx_iam_event_consume_status` (`consume_status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='事件消费幂等日志';

CREATE TABLE `iam_operation_audit_log` (
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
  KEY `idx_iam_operation_audit_target` (`target_type`, `target_id`),
  KEY `idx_iam_operation_audit_operator` (`operator_id`, `operation_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';



-- 枚举初始化数据
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (1, 'ACTION_TYPE', 'ACTION_TYPE', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (1, 'ACTION_TYPE', 1, '待定义1', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (2, 'ACTION_TYPE', 2, '待定义2', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (3, 'ACTION_TYPE', 3, '待定义3', 3, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (2, 'ALL', 'ALL', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (4, 'ALL', 1, '角色适用应用', 1, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (3, 'APPROVAL_STATUS', 'APPROVAL_STATUS', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (5, 'APPROVAL_STATUS', 1, '草稿', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (6, 'APPROVAL_STATUS', 2, '待审批', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (7, 'APPROVAL_STATUS', 3, '已批准', 3, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (8, 'APPROVAL_STATUS', 4, '已驳回', 4, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (4, 'APP_TYPE', 'APP_TYPE', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (9, 'APP_TYPE', 1, '移动端', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (10, 'APP_TYPE', 2, '后台服务', 2, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (5, 'COMMON_STATUS', 'COMMON_STATUS', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (11, 'COMMON_STATUS', 1, '启用', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (12, 'COMMON_STATUS', 2, '停用', 2, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (6, 'CONSUME_STATUS', 'CONSUME_STATUS', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (13, 'CONSUME_STATUS', 1, '待消费', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (14, 'CONSUME_STATUS', 2, '处理中', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (15, 'CONSUME_STATUS', 3, '消费成功', 3, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (16, 'CONSUME_STATUS', 4, '消费失败', 4, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (17, 'CONSUME_STATUS', 5, '已忽略', 5, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (7, 'CURRENCY', 'CURRENCY', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (18, 'CURRENCY', 1, '人民币', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (19, 'CURRENCY', 2, '美元', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (20, 'CURRENCY', 3, '欧元', 3, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (21, 'CURRENCY', 4, '港币', 4, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (22, 'CURRENCY', 5, '日元', 5, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (8, 'DATA_RESOURCE_TYPE', 'DATA_RESOURCE_TYPE', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (23, 'DATA_RESOURCE_TYPE', 1, '组织', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (24, 'DATA_RESOURCE_TYPE', 2, '仓库', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (25, 'DATA_RESOURCE_TYPE', 3, '供应商', 3, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (26, 'DATA_RESOURCE_TYPE', 4, '客户', 4, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (27, 'DATA_RESOURCE_TYPE', 5, '货主', 5, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (9, 'DATA_SCOPE_TYPE', 'DATA_SCOPE_TYPE', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (28, 'DATA_SCOPE_TYPE', 1, '默认数据范围', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (29, 'DATA_SCOPE_TYPE', 2, '全部', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (30, 'DATA_SCOPE_TYPE', 3, '本组织', 3, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (31, 'DATA_SCOPE_TYPE', 4, '本组织及下级', 4, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (32, 'DATA_SCOPE_TYPE', 5, '自定义', 5, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (33, 'DATA_SCOPE_TYPE', 6, '本人', 6, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (10, 'EVENT_STATUS', 'EVENT_STATUS', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (34, 'EVENT_STATUS', 1, '待发布', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (35, 'EVENT_STATUS', 2, '发布中', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (36, 'EVENT_STATUS', 3, '已发布', 3, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (37, 'EVENT_STATUS', 4, '发布失败', 4, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (38, 'EVENT_STATUS', 5, '已取消', 5, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (11, 'GRANT_STATUS', 'GRANT_STATUS', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (39, 'GRANT_STATUS', 1, '已授权', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (40, 'GRANT_STATUS', 2, '已取消', 2, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (12, 'GRANT_TYPE', 'GRANT_TYPE', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (41, 'GRANT_TYPE', 1, '授权码', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (42, 'GRANT_TYPE', 2, '密码', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (43, 'GRANT_TYPE', 3, '刷新', 3, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (13, 'GRANT_TYPE_USER_ROLE', 'GRANT_TYPE_USER_ROLE', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (44, 'GRANT_TYPE_USER_ROLE', 1, '手工', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (45, 'GRANT_TYPE_USER_ROLE', 2, '组织继承', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (46, 'GRANT_TYPE_USER_ROLE', 3, '岗位继承', 3, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (47, 'GRANT_TYPE_USER_ROLE', 4, '系统默认', 4, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (14, 'HTTP_METHOD', 'HTTP_METHOD', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (48, 'HTTP_METHOD', 1, '待定义1', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (49, 'HTTP_METHOD', 2, '待定义2', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (50, 'HTTP_METHOD', 3, '待定义3', 3, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (15, 'LOGIN_FAILURE_REASON', 'LOGIN_FAILURE_REASON', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (51, 'LOGIN_FAILURE_REASON', 1, '密码错误', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (52, 'LOGIN_FAILURE_REASON', 2, '用户停用', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (53, 'LOGIN_FAILURE_REASON', 3, '过期', 3, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (16, 'LOGIN_RESULT', 'LOGIN_RESULT', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (54, 'LOGIN_RESULT', 1, '成功', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (55, 'LOGIN_RESULT', 2, '失败', 2, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (17, 'MENU_TYPE', 'MENU_TYPE', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (56, 'MENU_TYPE', 1, '目录', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (57, 'MENU_TYPE', 2, '页面', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (58, 'MENU_TYPE', 3, '外链', 3, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (18, 'OPERATION_RESULT', 'OPERATION_RESULT', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (59, 'OPERATION_RESULT', 1, '成功', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (60, 'OPERATION_RESULT', 2, '失败', 2, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (19, 'OPERATION_TYPE', 'OPERATION_TYPE', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (61, 'OPERATION_TYPE', 1, '待定义1', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (62, 'OPERATION_TYPE', 2, '待定义2', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (63, 'OPERATION_TYPE', 3, '待定义3', 3, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (20, 'PERMISSION_TYPE', 'PERMISSION_TYPE', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (64, 'PERMISSION_TYPE', 1, '菜单', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (65, 'PERMISSION_TYPE', 2, '按钮', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (66, 'PERMISSION_TYPE', 3, '字段', 3, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (21, 'RESOURCE_TYPE', 'RESOURCE_TYPE', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (67, 'RESOURCE_TYPE', 1, '页面', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (68, 'RESOURCE_TYPE', 2, '按钮', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (69, 'RESOURCE_TYPE', 3, '接口', 3, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (70, 'RESOURCE_TYPE', 4, '字段', 4, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (22, 'ROLE_TYPE', 'ROLE_TYPE', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (71, 'ROLE_TYPE', 1, '系统角色', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (72, 'ROLE_TYPE', 2, '业务角色', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (73, 'ROLE_TYPE', 3, '外部角色', 3, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (23, 'SUBJECT_TYPE', 'SUBJECT_TYPE', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (74, 'SUBJECT_TYPE', 1, '用户', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (75, 'SUBJECT_TYPE', 2, '角色', 2, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (24, 'TOKEN_STATUS', 'TOKEN_STATUS', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (76, 'TOKEN_STATUS', 1, '有效', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (77, 'TOKEN_STATUS', 2, '已刷新', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (78, 'TOKEN_STATUS', 3, '已撤销', 3, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (79, 'TOKEN_STATUS', 4, '已过期', 4, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (25, 'USER_STATUS', 'USER_STATUS', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (80, 'USER_STATUS', 1, '待激活', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (81, 'USER_STATUS', 2, '已启用', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (82, 'USER_STATUS', 3, '已锁定', 3, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (83, 'USER_STATUS', 4, '已停用', 4, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (84, 'USER_STATUS', 5, '已注销', 5, 1);
INSERT INTO `iam_enum_type` (`enum_type_id`, `enum_type_code`, `enum_type_name`, `configurable`, `status`) VALUES (26, 'USER_TYPE', 'USER_TYPE', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (85, 'USER_TYPE', 1, '内部员工', 1, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (86, 'USER_TYPE', 2, '供应商用户', 2, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (87, 'USER_TYPE', 3, '客户用户', 3, 1);
INSERT INTO `iam_enum_item` (`enum_item_id`, `enum_type_code`, `enum_value`, `enum_label`, `sort_no`, `status`) VALUES (88, 'USER_TYPE', 4, '系统账号', 4, 1);



-- 领域事件类型参考数据
-- 1. 应用已创建: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`草稿`、操作者、原因、幂等键
-- 2. 应用已启用: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已启用`、操作者、原因、幂等键
-- 3. SSO客户端已配置: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已启用`、操作者、原因、幂等键
-- 4. 应用已停用: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已停用`、操作者、原因、幂等键
-- 5. 子系统接入申请已提交: 载荷 创建应用接入待办
-- 6. 安全风险已识别: 载荷 冻结高风险客户端或要求重置密钥
-- 7. 主数据已变更: 载荷 更新组织、仓库、货主、供应商、客户等数据范围对象快照
-- 8. 用户已创建: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`待激活`、操作者、原因、幂等键
-- 9. 用户已激活: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已启用`、操作者、原因、幂等键
-- 10. 用户已锁定: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已锁定`、操作者、原因、幂等键
-- 11. 用户已停用: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已停用`、操作者、原因、幂等键
-- 12. 用户角色已分配: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已启用`、操作者、原因、幂等键
-- 13. 用户角色已取消: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已启用`、操作者、原因、幂等键
-- 14. 员工已入职: 载荷 创建内部用户草稿或待激活账号
-- 15. 员工已离职: 载荷 停用用户并触发会话失效
-- 16. 角色已创建: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`草稿`、操作者、原因、幂等键
-- 17. 角色已启用: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已启用`、操作者、原因、幂等键
-- 18. 角色权限已分配: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已启用`、操作者、原因、幂等键
-- 19. 角色权限已取消: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已启用`、操作者、原因、幂等键
-- 20. 角色已停用: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已停用`、操作者、原因、幂等键
-- 21. 权限点已注册: 载荷 允许角色引用新增权限点
-- 22. 权限点已变更: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已启用`、操作者、原因、幂等键
-- 23. 权限点API已绑定: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已启用`、操作者、原因、幂等键
-- 24. 权限点已停用: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已停用`、操作者、原因、幂等键
-- 25. API资源已扫描: 载荷 生成或更新权限点建议
-- 26. 数据权限已创建: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已启用`、操作者、原因、幂等键
-- 27. 数据范围已分配: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已启用`、操作者、原因、幂等键
-- 28. 数据范围已变更: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已启用`、操作者、原因、幂等键
-- 29. 数据范围已取消: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已启用`、操作者、原因、幂等键
-- 30. 数据权限已停用: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已停用`、操作者、原因、幂等键
-- 31. 用户已登录: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`有效`、操作者、原因、幂等键
-- 32. Token已刷新: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`有效`、操作者、原因、幂等键
-- 33. Token校验已通过: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`有效`、操作者、原因、幂等键
-- 34. Token已失效: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已失效`、操作者、原因、幂等键
-- 35. 会话已踢出: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已踢出`、操作者、原因、幂等键
-- 36. 角色权限已变更: 载荷 标记相关会话权限版本过期
-- 37. 审批已开始: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`审批中`、操作者、原因、幂等键
-- 38. 审批节点已通过: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`审批中`、操作者、原因、幂等键
-- 39. 审批已驳回: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已驳回`、操作者、原因、幂等键
-- 40. 审批已撤回: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已撤回`、操作者、原因、幂等键
-- 41. 审批已取消: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已取消`、操作者、原因、幂等键
-- 42. 审批已完成: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已通过`、操作者、原因、幂等键
-- 43. 审批请求已提交: 载荷 创建审批实例和首个待办任务
-- 44. 审批请求已取消: 载荷 取消未完成审批实例
-- 45. 审计日志已创建: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已记录`、操作者、原因、幂等键
-- 46. 敏感日志已标记: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已记录`、操作者、原因、幂等键
-- 47. 审计日志已归档: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已归档`、操作者、原因、幂等键
-- 48. 审计日志已查询: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已记录`、操作者、原因、幂等键
-- 49. 敏感操作已发生: 载荷 写入敏感操作审计日志
-- 50. 权限校验已拒绝: 载荷 写入拒绝访问日志
-- 51. 安全策略已创建: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`草稿`、操作者、原因、幂等键
-- 52. 安全策略已发布: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已发布`、操作者、原因、幂等键
-- 53. 安全风险已处置: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已处置`、操作者、原因、幂等键
-- 54. 安全策略已停用: 载荷 聚合ID、应用、用户、角色、权限点、数据范围、权限版本、状态`已停用`、操作者、原因、幂等键
-- 55. 登录失败已记录: 载荷 累计失败次数并判断是否锁定



-- 业务表

CREATE TABLE `iam_app` (
  `app_id` bigint NOT NULL COMMENT '应用系统主键',
  `app_code` varchar(64) NOT NULL COMMENT '应用编码，如 `OMS`、`WMS`',
  `app_name` varchar(128) NOT NULL COMMENT '应用名称',
  `app_type` smallint NOT NULL COMMENT 'Web、OpenAPI、移动端、后台服务',
  `base_url` varchar(512) NULL COMMENT '应用首页地址',
  `status` smallint NOT NULL COMMENT '启用、停用',
  `sort_no` int NULL COMMENT '排序',
  `remark` varchar(512) NULL COMMENT '备注',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`app_id`),
  KEY `idx_iam_app_status_time` (`status`, `updated_at`),
  KEY `idx_iam_app_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='应用系统';

CREATE TABLE `iam_sso` (
  `sso_id` bigint NOT NULL COMMENT 'SSO 客户端主键',
  `client_code` varchar(64) NOT NULL COMMENT 'OAuth/OIDC client id',
  `client_secret_hash` varchar(256) NOT NULL COMMENT '客户端密钥哈希',
  `redirect_uris` text NOT NULL COMMENT '允许回调地址',
  `grant_types` smallint NOT NULL COMMENT '授权码、密码、刷新 Token 等',
  `token_ttl_seconds` int NOT NULL COMMENT 'access token 有效期',
  `refresh_ttl_seconds` int NOT NULL COMMENT 'refresh token 有效期',
  `status` smallint NOT NULL COMMENT '启用、停用',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`sso_id`),
  KEY `idx_iam_sso_status_time` (`status`, `updated_at`),
  KEY `idx_iam_sso_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SSO 客户端';

CREATE TABLE `iam_user` (
  `user_id` bigint NOT NULL COMMENT '用户主键',
  `username` varchar(64) NOT NULL COMMENT '登录账号',
  `password_hash` varchar(256) NULL COMMENT '本地账号密码哈希；外部 SSO 可为空',
  `real_name` varchar(128) NOT NULL COMMENT '姓名',
  `nickname` varchar(128) NULL COMMENT '昵称',
  `mobile` varchar(32) NULL COMMENT '手机号',
  `email` varchar(128) NULL COMMENT '邮箱',
  `user_type` smallint NOT NULL COMMENT '内部员工、供应商用户、客户用户、系统账号',
  `employee_no` varchar(64) NULL COMMENT '工号',
  `mfa_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用 MFA',
  `last_login_at` datetime NULL COMMENT '最近登录时间',
  `login_fail_count` int NOT NULL DEFAULT 0 COMMENT '连续失败次数',
  `locked_until` datetime NULL COMMENT '锁定到期时间',
  `status` smallint NOT NULL COMMENT '待激活、已启用、已锁定、已停用、已注销',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`user_id`),
  KEY `idx_iam_user_status_time` (`status`, `updated_at`),
  KEY `idx_iam_user_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户';

CREATE TABLE `iam_user_role` (
  `user_role_id` bigint NOT NULL COMMENT '用户角色关系主键',
  `grant_type` smallint NOT NULL COMMENT '手工、组织继承、岗位继承、系统默认',
  `effective_from` datetime NULL COMMENT '生效时间',
  `effective_to` datetime NULL COMMENT '失效时间',
  `status` smallint NOT NULL COMMENT '启用、停用',
  `granted_by` bigint NOT NULL COMMENT '授权人',
  `granted_at` datetime NOT NULL COMMENT '授权时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`user_role_id`),
  KEY `idx_iam_user_role_status_time` (`status`, `updated_at`),
  KEY `idx_iam_user_role_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关系';

CREATE TABLE `iam_role` (
  `role_id` bigint NOT NULL COMMENT '角色主键',
  `role_code` varchar(64) NOT NULL COMMENT '角色编码',
  `role_name` varchar(128) NOT NULL COMMENT '角色名称',
  `role_type` smallint NOT NULL COMMENT '系统角色、业务角色、外部角色',
  `app_scope` smallint NULL COMMENT '角色适用应用',
  `data_scope_type` smallint NOT NULL COMMENT '默认数据范围',
  `description` varchar(512) NULL COMMENT '角色说明',
  `status` smallint NOT NULL COMMENT '启用、停用',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`role_id`),
  KEY `idx_iam_role_status_time` (`status`, `updated_at`),
  KEY `idx_iam_role_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色';

CREATE TABLE `iam_role_permission` (
  `role_id` bigint NOT NULL COMMENT '角色权限关系主键',
  `grant_status` smallint NOT NULL COMMENT '已授权、已取消',
  `granted_by` bigint NOT NULL COMMENT '授权人',
  `granted_at` datetime NOT NULL COMMENT '授权时间',
  `revoked_by` bigint NULL COMMENT '取消授权人',
  `revoked_at` datetime NULL COMMENT '取消授权时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`role_id`),
  KEY `idx_iam_role_permission_status_time` (`grant_status`, `updated_at`),
  KEY `idx_iam_role_permission_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关系';

CREATE TABLE `iam_menu_page` (
  `business_object_id` bigint NOT NULL COMMENT '菜单/页面主键',
  `menu_code` varchar(128) NOT NULL COMMENT '菜单编码',
  `menu_name` varchar(128) NOT NULL COMMENT '菜单名称',
  `menu_type` smallint NOT NULL COMMENT '目录、页面、外链',
  `route_path` varchar(256) NULL COMMENT '前端路由',
  `component_path` varchar(256) NULL COMMENT '前端组件路径',
  `icon` varchar(64) NULL COMMENT '图标',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `visible` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否在菜单展示',
  `status` smallint NOT NULL COMMENT '启用、停用',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`business_object_id`),
  KEY `idx_iam_menu_page_status_time` (`status`, `updated_at`),
  KEY `idx_iam_menu_page_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单/页面';

CREATE TABLE `iam_permission` (
  `permission_id` bigint NOT NULL COMMENT '权限点主键',
  `permission_code` varchar(128) NOT NULL COMMENT '权限编码，如 `wms:outbound:create`',
  `permission_name` varchar(128) NOT NULL COMMENT '权限名称',
  `permission_type` smallint NOT NULL COMMENT '菜单、按钮、API、字段',
  `resource_type` smallint NOT NULL COMMENT '页面、按钮、接口、字段',
  `action_type` smallint NOT NULL COMMENT 'CREATE、READ、UPDATE、DELETE、EXPORT、IMPORT、APPROVE 等',
  `api_method` smallint NULL COMMENT 'GET、POST、PUT、DELETE',
  `api_path` varchar(256) NULL COMMENT 'API 路径',
  `field_code` varchar(128) NULL COMMENT '字段权限适用',
  `status` smallint NOT NULL COMMENT '启用、停用',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`permission_id`),
  KEY `idx_iam_permission_status_time` (`status`, `updated_at`),
  KEY `idx_iam_permission_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限点';

CREATE TABLE `iam_data_scope` (
  `data_scope_id` bigint NOT NULL COMMENT '数据权限范围主键',
  `subject_type` smallint NOT NULL COMMENT '用户、角色',
  `scope_type` smallint NOT NULL COMMENT '全部、本组织、本组织及下级、自定义、本人',
  `resource_type` smallint NOT NULL COMMENT '组织、仓库、供应商、客户、货主',
  `resource_ids` text NULL COMMENT '自定义资源 ID',
  `status` smallint NOT NULL COMMENT '启用、停用',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`data_scope_id`),
  KEY `idx_iam_data_scope_status_time` (`status`, `updated_at`),
  KEY `idx_iam_data_scope_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据权限范围';

CREATE TABLE `iam_user_token` (
  `user_token_id` bigint NOT NULL COMMENT '用户会话/Token主键',
  `access_token_jti` varchar(128) NOT NULL COMMENT 'access token 唯一 ID',
  `refresh_token_jti` varchar(128) NULL COMMENT 'refresh token 唯一 ID',
  `token_status` smallint NOT NULL COMMENT '有效、已刷新、已撤销、已过期',
  `login_ip` varchar(64) NULL COMMENT '登录 IP',
  `user_agent` varchar(512) NULL COMMENT '浏览器/客户端信息',
  `login_at` datetime NOT NULL COMMENT '登录时间',
  `expires_at` datetime NOT NULL COMMENT 'access token 过期时间',
  `refresh_expires_at` datetime NULL COMMENT 'refresh token 过期时间',
  `logout_at` datetime NULL COMMENT '登出时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`user_token_id`),
  KEY `idx_iam_user_token_status_time` (`token_status`, `updated_at`),
  KEY `idx_iam_user_token_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户会话/Token';

CREATE TABLE `iam_login_log` (
  `business_object_id` bigint NOT NULL COMMENT '登录日志主键',
  `username` varchar(64) NOT NULL COMMENT '登录账号',
  `login_result` smallint NOT NULL COMMENT '成功、失败',
  `failure_reason` smallint NULL COMMENT '密码错误、用户停用、Token 过期等',
  `login_ip` varchar(64) NULL COMMENT 'IP',
  `user_agent` varchar(512) NULL COMMENT '客户端信息',
  `login_at` datetime NOT NULL COMMENT '登录时间',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`business_object_id`),
  KEY `idx_iam_login_log_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志';

CREATE TABLE `iam_operation_log` (
  `operation_log_id` bigint NOT NULL COMMENT '操作日志主键',
  `operator_name` varchar(128) NOT NULL COMMENT '操作人姓名',
  `app_code` varchar(64) NOT NULL COMMENT '所属应用',
  `module_code` varchar(64) NOT NULL COMMENT '模块编码',
  `operation_type` smallint NOT NULL COMMENT 'CREATE、UPDATE、DELETE、APPROVE、LOGIN 等',
  `permission_code` varchar(128) NULL COMMENT '对应权限点',
  `biz_type` varchar(64) NULL COMMENT '业务对象类型，如 USER、ROLE',
  `biz_no` varchar(128) NULL COMMENT '业务单号',
  `operation_desc` varchar(512) NULL COMMENT '简要说明',
  `before_snapshot` text NULL COMMENT '修改前摘要',
  `after_snapshot` text NULL COMMENT '修改后摘要',
  `operation_result` smallint NOT NULL COMMENT '成功、失败',
  `failure_reason` varchar(512) NULL COMMENT '失败原因',
  `ip` varchar(64) NULL COMMENT 'IP',
  `user_agent` varchar(512) NULL COMMENT '客户端',
  `created_by` bigint NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '数据版本',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`operation_log_id`),
  KEY `idx_iam_operation_log_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';



SET FOREIGN_KEY_CHECKS = 1;

