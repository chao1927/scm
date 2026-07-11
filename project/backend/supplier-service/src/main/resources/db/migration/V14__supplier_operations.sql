CREATE TABLE sup_work_item (
  work_item_id BIGINT PRIMARY KEY, work_type VARCHAR(32) NOT NULL, supplier_id BIGINT NOT NULL, business_type VARCHAR(32) NOT NULL, business_id BIGINT NOT NULL, business_no VARCHAR(64) NULL,
  title VARCHAR(200) NOT NULL, assignee_type TINYINT NOT NULL COMMENT '1内部角色 2供应商', due_at DATETIME(3) NULL, status TINYINT NOT NULL COMMENT '1待处理 2处理中 3已完成 4已关闭', source_event_code VARCHAR(64) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3), version INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_sup_work_event(source_event_code,work_type), KEY idx_sup_work_supplier(supplier_id,status,due_at)
);
CREATE TABLE sup_warning (
  warning_id BIGINT PRIMARY KEY, warning_type VARCHAR(32) NOT NULL, supplier_id BIGINT NOT NULL, business_type VARCHAR(32) NOT NULL, business_id BIGINT NOT NULL,
  warning_level TINYINT NOT NULL COMMENT '1提醒 2预警 3严重', warning_message VARCHAR(500) NOT NULL, occurred_at DATETIME(3) NOT NULL, status TINYINT NOT NULL COMMENT '1待确认 2已确认 3已关闭', source_event_code VARCHAR(64) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3), version INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_sup_warning_event(source_event_code,warning_type), KEY idx_sup_warning_supplier(supplier_id,status,warning_level)
);
CREATE TABLE sup_data_reconciliation (
  reconciliation_job_id BIGINT PRIMARY KEY, reconciliation_type VARCHAR(32) NOT NULL, target_system VARCHAR(32) NOT NULL, business_date DATE NOT NULL,
  local_count BIGINT NOT NULL, remote_count BIGINT NOT NULL, local_amount DECIMAL(18,2) NULL, remote_amount DECIMAL(18,2) NULL,
  difference_detail VARCHAR(1000) NULL, status TINYINT NOT NULL COMMENT '1一致 2存在差异 3已处理', created_by BIGINT NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3), version INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_sup_data_reconciliation(reconciliation_type,target_system,business_date)
);
