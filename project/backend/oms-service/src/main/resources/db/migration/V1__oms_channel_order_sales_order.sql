create table oms_sales_order (
  id bigint primary key auto_increment,
  order_no varchar(64) not null,
  channel_code varchar(64) not null,
  channel_order_no varchar(128) not null,
  customer_id bigint not null,
  receiver_address varchar(512) not null,
  line_payload text not null,
  total_amount decimal(18,2) not null,
  order_status tinyint not null,
  review_remark varchar(512),
  version bigint not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_oms_order_no (order_no),
  unique key uk_oms_channel_order (channel_code, channel_order_no)
);

create table oms_channel_order (
  id bigint primary key auto_increment,
  channel_code varchar(64) not null,
  channel_order_no varchar(128) not null,
  order_no varchar(64) not null,
  raw_payload text,
  created_at datetime not null,
  unique key uk_oms_channel_mapping (channel_code, channel_order_no)
);

create table oms_outbox_event (
  id bigint primary key auto_increment,
  event_type varchar(128) not null,
  business_no varchar(128) not null,
  payload text not null,
  event_status tinyint not null,
  occurred_at datetime not null,
  created_at datetime not null,
  key idx_oms_outbox_status (event_status, id)
);

create table oms_operation_log (
  id bigint primary key auto_increment,
  operation_type varchar(64) not null,
  business_no varchar(128) not null,
  operator_id bigint,
  idempotency_key varchar(128),
  created_at datetime not null,
  key idx_oms_operation_business (business_no)
);
