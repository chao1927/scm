create table oms_fulfillment (
  id bigint primary key auto_increment,
  fulfillment_no varchar(64) not null,
  sales_order_no varchar(64) not null,
  channel_code varchar(64) not null,
  customer_id bigint not null,
  warehouse_id bigint not null,
  warehouse_code varchar(64) not null,
  logistics_product_code varchar(64),
  line_payload text not null,
  fulfillment_status tinyint not null,
  reservation_ref_no varchar(64),
  reservation_no varchar(64),
  outbound_order_no varchar(64),
  failure_reason varchar(512),
  split_reason varchar(512),
  version bigint not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_oms_fulfillment_no (fulfillment_no),
  unique key uk_oms_fulfillment_sales_order (sales_order_no, fulfillment_no),
  key idx_oms_fulfillment_status (fulfillment_status, updated_at),
  key idx_oms_fulfillment_order (sales_order_no)
);

create table oms_stock_reservation (
  id bigint primary key auto_increment,
  reservation_ref_no varchar(64) not null,
  fulfillment_no varchar(64) not null,
  reservation_no varchar(64),
  reserve_qty decimal(18,4) not null,
  reserved_qty decimal(18,4) not null default 0,
  reservation_status tinyint not null,
  fail_reason varchar(512),
  version bigint not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_oms_reservation_ref (reservation_ref_no),
  key idx_oms_reservation_fulfillment (fulfillment_no),
  key idx_oms_reservation_status (reservation_status, updated_at)
);

create table oms_outbound (
  id bigint primary key auto_increment,
  outbound_order_no varchar(64) not null,
  fulfillment_no varchar(64) not null,
  sales_order_no varchar(64) not null,
  warehouse_id bigint not null,
  warehouse_code varchar(64) not null,
  wms_order_no varchar(64),
  outbound_status tinyint not null,
  cancel_reason varchar(512),
  retry_count int not null default 0,
  version bigint not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_oms_outbound_no (outbound_order_no),
  unique key uk_oms_outbound_fulfillment (fulfillment_no),
  key idx_oms_outbound_status (outbound_status, updated_at)
);

create table oms_integration_command (
  id bigint primary key auto_increment,
  command_type varchar(128) not null,
  target_system varchar(32) not null,
  business_no varchar(128) not null,
  idempotency_key varchar(128) not null,
  payload text not null,
  command_status tinyint not null,
  retry_count int not null default 0,
  last_error varchar(512),
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_oms_integration_command (target_system, command_type, idempotency_key),
  key idx_oms_integration_command_status (command_status, updated_at)
);

create table oms_event_inbox (
  id bigint primary key auto_increment,
  event_id varchar(128) not null,
  event_type varchar(128) not null,
  business_no varchar(128) not null,
  payload text not null,
  event_status tinyint not null,
  fail_reason varchar(512),
  created_at datetime not null,
  processed_at datetime,
  unique key uk_oms_event_inbox_event (event_id),
  key idx_oms_event_inbox_status (event_status, created_at)
);
