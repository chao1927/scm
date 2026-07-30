UPDATE sup_export_task
SET status = CASE status
  WHEN 1 THEN 1
  WHEN 2 THEN 3
  WHEN 3 THEN 4
  ELSE status
END;

ALTER TABLE sup_export_task
  MODIFY COLUMN status TINYINT NOT NULL COMMENT '1待处理 2处理中 3已完成 4失败',
  ADD COLUMN object_key VARCHAR(500) NULL AFTER file_url,
  ADD COLUMN file_name VARCHAR(255) NULL AFTER object_key,
  ADD COLUMN content_type VARCHAR(128) NULL AFTER file_name,
  ADD COLUMN file_size BIGINT NULL AFTER content_type,
  ADD COLUMN retry_count INT NOT NULL DEFAULT 0 AFTER fail_reason,
  ADD COLUMN next_retry_at DATETIME(3) NULL AFTER retry_count,
  ADD COLUMN started_at DATETIME(3) NULL AFTER next_retry_at,
  ADD COLUMN completed_at DATETIME(3) NULL AFTER started_at,
  ADD COLUMN idempotency_key VARCHAR(128) NULL AFTER created_by,
  ADD UNIQUE KEY uk_sup_export_creator_idem (created_by, idempotency_key),
  ADD KEY idx_sup_export_dispatch (status, next_retry_at, started_at, created_at);
