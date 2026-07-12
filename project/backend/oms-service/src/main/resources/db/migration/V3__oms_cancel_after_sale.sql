create table oms_cancel_request (
  id bigint primary key auto_increment,
  cancellation_no varchar(64) not null,
  sales_order_no varchar(64) not null,
  fulfillment_no varchar(64) not null,
  outbound_no varchar(64),
  reservation_ref_no varchar(64),
  reason varchar(512) not null,
  cancel_status tinyint not null,
  wms_cancelled tinyint not null default 0,
  stock_released tinyint not null default 0,
  version bigint not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_oms_cancellation_no (cancellation_no),
  key idx_oms_cancel_order (sales_order_no),
  key idx_oms_cancel_status (cancel_status, updated_at)
);

create table oms_after_sale (
  id bigint primary key auto_increment,
  after_sale_no varchar(64) not null,
  sales_order_no varchar(64) not null,
  fulfillment_no varchar(64) not null,
  refund_amount decimal(18,2) not null,
  refunded_amount decimal(18,2) not null default 0,
  reason varchar(512) not null,
  after_sale_status tinyint not null,
  version bigint not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_oms_after_sale_no (after_sale_no),
  key idx_oms_after_sale_order (sales_order_no),
  key idx_oms_after_sale_status (after_sale_status, updated_at)
);
