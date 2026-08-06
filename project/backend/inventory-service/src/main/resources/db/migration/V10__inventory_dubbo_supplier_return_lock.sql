CREATE TABLE inventory_supplier_return_lock (
    return_id BIGINT NOT NULL,
    lock_no VARCHAR(64) NOT NULL,
    return_no VARCHAR(64) NOT NULL,
    supplier_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    request_fingerprint VARCHAR(1000) NOT NULL,
    lock_status TINYINT NOT NULL DEFAULT 1,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (return_id),
    UNIQUE KEY uk_inventory_return_lock_no (lock_no)
);

CREATE TABLE inventory_supplier_return_lock_line (
    return_id BIGINT NOT NULL,
    source_line_id BIGINT NOT NULL,
    freeze_no VARCHAR(64) NOT NULL,
    locked_quantity DECIMAL(18,6) NOT NULL,
    freeze_version INT NOT NULL,
    PRIMARY KEY (return_id, source_line_id),
    UNIQUE KEY uk_inventory_return_freeze (freeze_no)
);

CREATE TABLE inventory_dubbo_receipt (
    idempotency_key VARCHAR(128) NOT NULL,
    command_type VARCHAR(64) NOT NULL,
    request_fingerprint VARCHAR(1000) NOT NULL,
    reference_no VARCHAR(64) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (idempotency_key)
);
