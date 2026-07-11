CREATE TABLE IF NOT EXISTS sup_asn_receipt_line (
 receipt_line_id BIGINT NOT NULL, asn_id BIGINT NOT NULL, asn_line_id BIGINT NOT NULL, received_qty DECIMAL(18,4) NOT NULL, rejected_qty DECIMAL(18,4) NOT NULL,
 quality_status TINYINT NOT NULL COMMENT '1待检 2合格 3不合格', quality_reason VARCHAR(512) NULL, source_event_code VARCHAR(128) NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 PRIMARY KEY(receipt_line_id), UNIQUE KEY uk_sup_asn_receipt_source(source_event_code,asn_line_id), KEY idx_sup_asn_receipt_asn(asn_id,asn_line_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ASN行级收货与质检事实';
