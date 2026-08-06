CREATE TABLE wms_dubbo_receipt (
    idempotency_key VARCHAR(128) NOT NULL,
    command_type VARCHAR(64) NOT NULL,
    request_fingerprint VARCHAR(1000) NOT NULL,
    reference_no VARCHAR(64) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (idempotency_key)
);

CREATE TABLE wms_supplier_appointment (
    asn_id BIGINT NOT NULL,
    appointment_no VARCHAR(64) NOT NULL,
    asn_no VARCHAR(64) NOT NULL,
    supplier_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    appointment_status TINYINT NOT NULL DEFAULT 1,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (asn_id),
    UNIQUE KEY uk_wms_supplier_appointment_no (appointment_no)
);

CREATE TABLE wms_supplier_appointment_line (
    asn_id BIGINT NOT NULL,
    source_line_no VARCHAR(64) NOT NULL,
    inbound_order_id BIGINT NOT NULL,
    inbound_order_version INT NOT NULL,
    PRIMARY KEY (asn_id, source_line_no),
    UNIQUE KEY uk_wms_supplier_appointment_inbound (inbound_order_id)
);
