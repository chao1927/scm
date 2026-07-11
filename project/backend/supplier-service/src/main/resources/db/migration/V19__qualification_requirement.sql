CREATE TABLE sup_qualification_requirement (
  requirement_id BIGINT PRIMARY KEY,
  supplier_type VARCHAR(32) NULL,
  category_id BIGINT NULL,
  qualification_type VARCHAR(32) NOT NULL,
  mandatory TINYINT NOT NULL DEFAULT 1,
  status TINYINT NOT NULL DEFAULT 1,
  created_by BIGINT NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_by BIGINT NOT NULL,
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  version INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_sup_qualification_requirement(supplier_type,category_id,qualification_type),
  KEY idx_sup_qualification_requirement_scope(status,category_id)
);
