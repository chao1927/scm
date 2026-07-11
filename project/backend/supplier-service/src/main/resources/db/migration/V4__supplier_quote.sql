CREATE TABLE IF NOT EXISTS sup_supplier_quote (
  quote_id bigint NOT NULL, quote_no varchar(64) NOT NULL, supplier_id bigint NOT NULL,
  rfq_id bigint NULL, rfq_no varchar(64) NULL, currency varchar(8) NOT NULL,
  valid_from date NOT NULL, valid_to date NOT NULL, quote_status smallint NOT NULL COMMENT '1草稿 2已提交 3已确认 4已采纳 5已拒绝 6已作废 7已过期',
  rejection_reason varchar(512) NULL, price_agreement_ref varchar(64) NULL, attachment_url varchar(512) NULL,
  created_by bigint NOT NULL, created_at datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), updated_by bigint NULL,
  updated_at datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3), version int NOT NULL DEFAULT 0, deleted tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (quote_id), UNIQUE KEY uk_sup_quote_no (quote_no), KEY idx_sup_quote_supplier_status (supplier_id,quote_status,updated_at), KEY idx_sup_quote_rfq (rfq_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商报价聚合';
CREATE TABLE IF NOT EXISTS sup_supplier_quote_line (
  quote_line_id bigint NOT NULL, quote_id bigint NOT NULL, sku_code varchar(64) NOT NULL, quote_qty decimal(18,4) NOT NULL,
  unit_price decimal(18,6) NOT NULL, tax_rate decimal(5,2) NOT NULL, delivery_days int NOT NULL, moq decimal(18,4) NOT NULL,
  created_at datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), updated_at datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3), version int NOT NULL DEFAULT 0, deleted tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (quote_line_id), UNIQUE KEY uk_sup_quote_line_sku (quote_id,sku_code), KEY idx_sup_quote_line_quote (quote_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商报价行';
