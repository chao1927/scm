create table bms_refund_receipt (
  id bigint primary key auto_increment,
  receipt_no varchar(128) not null,
  refund_no varchar(64) not null,
  receipt_status varchar(32) not null,
  payload text,
  created_at datetime(3) not null,
  unique key uk_bms_refund_receipt_no(receipt_no),
  key idx_bms_refund_receipt_refund(refund_no,created_at)
);
