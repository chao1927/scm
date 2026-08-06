CREATE TABLE bms_supplier_return_settlement (
    settlement_ref VARCHAR(64) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    request_fingerprint VARCHAR(1000) NOT NULL,
    return_id BIGINT NOT NULL,
    return_no VARCHAR(64) NOT NULL,
    supplier_id BIGINT NOT NULL,
    offset_amount DECIMAL(18,2) NOT NULL,
    claim_amount DECIMAL(18,2) NOT NULL,
    settlement_reason VARCHAR(500) NOT NULL,
    settlement_status TINYINT NOT NULL DEFAULT 1,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (settlement_ref),
    UNIQUE KEY uk_bms_return_settlement_idempotency (idempotency_key),
    UNIQUE KEY uk_bms_return_settlement_return (return_id)
);
