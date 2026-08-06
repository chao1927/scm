CREATE TABLE iam_user_supplier_scope (
    user_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (user_id, supplier_id),
    KEY idx_iam_user_supplier_scope_supplier (supplier_id)
);

CREATE TABLE iam_dubbo_receipt (
    idempotency_key VARCHAR(128) NOT NULL,
    command_type VARCHAR(64) NOT NULL,
    request_fingerprint VARCHAR(1000) NOT NULL,
    reference_no VARCHAR(64) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (idempotency_key)
);
