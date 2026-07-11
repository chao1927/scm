CREATE TABLE sup_export_task (
  export_task_id BIGINT PRIMARY KEY,
  export_type VARCHAR(32) NOT NULL,
  supplier_id BIGINT NULL,
  query_json JSON NOT NULL,
  status TINYINT NOT NULL COMMENT '1处理中 2已完成 3失败',
  file_url VARCHAR(500) NULL,
  fail_reason VARCHAR(500) NULL,
  created_by BIGINT NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  version INT NOT NULL DEFAULT 0,
  KEY idx_sup_export_supplier_status (supplier_id, status, created_at),
  KEY idx_sup_export_type_status (export_type, status)
) COMMENT '供应商运营导出任务';
