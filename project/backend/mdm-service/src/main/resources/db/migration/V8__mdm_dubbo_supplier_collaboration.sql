CREATE TABLE mdm_supplier_rpc_mapping (
    supplier_id BIGINT NOT NULL,
    admission_id BIGINT NOT NULL,
    supplier_code VARCHAR(64) NOT NULL,
    record_no VARCHAR(64) NOT NULL,
    request_fingerprint VARCHAR(1000) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (supplier_id),
    UNIQUE KEY uk_mdm_supplier_rpc_admission (admission_id),
    UNIQUE KEY uk_mdm_supplier_rpc_code (supplier_code),
    UNIQUE KEY uk_mdm_supplier_rpc_record (record_no)
);

CREATE TABLE mdm_dubbo_receipt (
    idempotency_key VARCHAR(128) NOT NULL,
    command_type VARCHAR(64) NOT NULL,
    request_fingerprint VARCHAR(1000) NOT NULL,
    reference_no VARCHAR(64) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (idempotency_key)
);
