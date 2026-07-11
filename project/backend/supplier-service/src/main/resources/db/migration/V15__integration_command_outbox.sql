CREATE TABLE sup_integration_command (
  command_id BIGINT PRIMARY KEY, command_code VARCHAR(64) NOT NULL, command_type VARCHAR(64) NOT NULL,
  aggregate_type VARCHAR(32) NOT NULL, aggregate_id BIGINT NOT NULL, aggregate_version INT NOT NULL,
  target_system VARCHAR(32) NOT NULL, payload_json JSON NOT NULL,
  command_status TINYINT NOT NULL COMMENT '1待执行 2执行中 3成功 4失败 5待人工处理 6已取消',
  retry_count INT NOT NULL DEFAULT 0, next_retry_at DATETIME(3) NULL, remote_reference VARCHAR(128) NULL,
  fail_reason VARCHAR(1000) NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3), completed_at DATETIME(3) NULL,
  UNIQUE KEY uk_sup_integration_command_code(command_code),
  UNIQUE KEY uk_sup_integration_business(command_type,aggregate_id,aggregate_version),
  KEY idx_sup_integration_dispatch(command_status,next_retry_at,created_at)
);
