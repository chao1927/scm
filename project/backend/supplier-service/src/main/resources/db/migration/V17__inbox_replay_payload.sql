ALTER TABLE sup_event_consume_log
  ADD COLUMN payload_json JSON NULL AFTER idempotent_key,
  ADD COLUMN replay_count INT NOT NULL DEFAULT 0 AFTER retry_count,
  ADD COLUMN last_replayed_by BIGINT NULL AFTER fail_reason,
  ADD COLUMN last_replayed_at DATETIME(3) NULL AFTER last_replayed_by;
