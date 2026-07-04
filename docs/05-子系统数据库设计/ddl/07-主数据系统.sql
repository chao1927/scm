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

