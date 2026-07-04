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

