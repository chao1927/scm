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

