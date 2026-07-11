CREATE TABLE sup_admission_registration (
  admission_id BIGINT PRIMARY KEY,
  supplier_id BIGINT NOT NULL,
  supplier_code VARCHAR(64) NOT NULL,
  registration_status TINYINT NOT NULL COMMENT '1已建档 2建档失败',
  source_event_code VARCHAR(128) NOT NULL,
  source_version BIGINT NOT NULL,
  failure_reason VARCHAR(500) NULL,
  registered_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_sup_admission_registration_supplier(supplier_id),
  UNIQUE KEY uk_sup_admission_registration_event(source_event_code)
);
