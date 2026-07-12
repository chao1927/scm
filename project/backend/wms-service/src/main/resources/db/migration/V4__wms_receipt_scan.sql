create table wms_receipt_scan (
    scan_id bigint not null primary key, receipt_id bigint not null, idempotency_key varchar(128) not null,
    received_qty decimal(18,6) not null, rejected_qty decimal(18,6) not null, reject_reason varchar(512) null,
    operator_id bigint not null, scanned_at datetime(3) not null,
    unique key uk_wms_receipt_scan_key(receipt_id,idempotency_key), key idx_wms_receipt_scan_receipt(receipt_id,scanned_at)
) comment 'WMS收货PDA扫码流水';
