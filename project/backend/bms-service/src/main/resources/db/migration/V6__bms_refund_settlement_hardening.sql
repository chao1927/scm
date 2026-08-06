alter table bms_refund_settlement
  add column after_sale_no varchar(64) null after bill_no,
  add column payment_no varchar(64) null after after_sale_no,
  add column currency varchar(8) not null default 'CNY' after refund_amount,
  add column merchant_no varchar(64) not null default 'DEFAULT' after currency,
  add column request_idempotency_key varchar(128) null after merchant_no,
  add column request_digest varchar(256) null after request_idempotency_key,
  add column attempt_no int not null default 1 after request_digest,
  add column evidence_ref varchar(256) null after failure_reason,
  add column reviewer_id bigint null after evidence_ref;

update bms_refund_settlement
set request_idempotency_key = concat('legacy:', refund_no),
    request_digest = concat(bill_no, '|', refund_amount)
where request_idempotency_key is null;

alter table bms_refund_settlement
  modify request_idempotency_key varchar(128) not null,
  modify request_digest varchar(256) not null,
  add unique key uk_bms_refund_request_idempotency (request_idempotency_key),
  add key idx_bms_refund_occupation (bill_no, status);

alter table bms_refund_receipt
  add column refund_amount decimal(18,2) null after receipt_status,
  add column currency varchar(8) null after refund_amount,
  add column merchant_no varchar(64) null after currency,
  add column payment_txn_no varchar(128) null after merchant_no,
  add column failure_reason varchar(512) null after payment_txn_no;

create table bms_refund_exception (
  id bigint primary key auto_increment,
  refund_no varchar(64) not null,
  receipt_no varchar(128),
  exception_type varchar(64) not null,
  detail varchar(512) not null,
  payload text,
  status int not null,
  created_at datetime(3) not null,
  key idx_bms_refund_exception_status (status, created_at),
  key idx_bms_refund_exception_refund (refund_no, created_at)
);
