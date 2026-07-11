CREATE TABLE IF NOT EXISTS `sup_sku_availability_snapshot` (
  `sku_code` varchar(64) NOT NULL,
  `sku_name` varchar(256) NOT NULL,
  `sku_status` smallint NOT NULL COMMENT '1已启用 2已停用',
  `base_unit` varchar(32) NULL,
  `category_id` bigint NULL,
  `snapshot_json` json NOT NULL COMMENT '主数据系统SKU本地快照',
  `source_version` bigint NOT NULL DEFAULT 0,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`sku_code`),
  KEY `idx_sup_sku_available` (`sku_status`,`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SKU可用性本地快照';

CREATE TABLE IF NOT EXISTS `sup_event_consume_log` (
  `consume_log_id` bigint NOT NULL AUTO_INCREMENT,
  `source_system` varchar(64) NOT NULL,
  `event_code` varchar(128) NOT NULL,
  `event_type` varchar(128) NOT NULL,
  `consumer_name` varchar(128) NOT NULL,
  `idempotent_key` varchar(256) NOT NULL,
  `consume_status` smallint NOT NULL COMMENT '1处理中 2成功 3失败 4忽略',
  `retry_count` int NOT NULL DEFAULT 0,
  `fail_reason` varchar(1000) NULL,
  `consumed_at` datetime(3) NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`consume_log_id`),
  UNIQUE KEY `uk_sup_consume_event` (`source_system`,`event_code`,`consumer_name`),
  UNIQUE KEY `uk_sup_consume_idempotent` (`consumer_name`,`idempotent_key`),
  KEY `idx_sup_consume_status` (`consume_status`,`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外部事件消费收件箱';
