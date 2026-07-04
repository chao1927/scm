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

