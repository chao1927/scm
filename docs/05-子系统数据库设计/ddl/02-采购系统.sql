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

