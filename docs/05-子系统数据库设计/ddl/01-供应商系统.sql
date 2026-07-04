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

