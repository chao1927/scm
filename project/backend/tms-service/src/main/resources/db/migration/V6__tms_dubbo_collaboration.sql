CREATE TABLE tms_dubbo_transport_request (
    request_id VARCHAR(64) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    command_type VARCHAR(64) NOT NULL,
    request_fingerprint VARCHAR(1000) NOT NULL,
    business_type VARCHAR(32) NOT NULL,
    business_id BIGINT NOT NULL,
    business_no VARCHAR(64) NOT NULL,
    shipper_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    carrier_code VARCHAR(64) NULL,
    tracking_no VARCHAR(128) NULL,
    request_payload TEXT NOT NULL,
    request_status TINYINT NOT NULL DEFAULT 1,
    cancel_reason VARCHAR(500) NULL,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (request_id),
    UNIQUE KEY uk_tms_dubbo_idempotency (idempotency_key),
    UNIQUE KEY uk_tms_dubbo_business (business_type, business_id)
);

CREATE TABLE tms_dubbo_command_receipt (
    idempotency_key VARCHAR(128) NOT NULL,
    command_type VARCHAR(64) NOT NULL,
    request_fingerprint VARCHAR(1000) NOT NULL,
    reference_no VARCHAR(64) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (idempotency_key)
);
