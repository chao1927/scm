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

