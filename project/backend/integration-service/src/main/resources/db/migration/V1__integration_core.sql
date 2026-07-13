create table int_route (
  id bigint primary key auto_increment,
  route_no varchar(64) not null,
  message_type varchar(128) not null,
  source_system varchar(64) not null,
  target_system varchar(64) not null,
  channel_type varchar(32) not null,
  route_status tinyint not null,
  version bigint not null,
  created_at datetime(3) not null,
  updated_at datetime(3) not null,
  unique key uk_int_route_no(route_no),
  key idx_int_route_match(message_type, source_system, route_status)
);

create table int_message (
  id bigint primary key auto_increment,
  message_no varchar(64) not null,
  message_type varchar(128) not null,
  source_system varchar(64) not null,
  target_system varchar(64) not null,
  business_no varchar(128) not null,
  idempotency_key varchar(128) not null,
  payload text not null,
  message_status tinyint not null,
  retry_count int not null,
  failure_reason varchar(512),
  version bigint not null,
  created_at datetime(3) not null,
  updated_at datetime(3) not null,
  unique key uk_int_message_no(message_no),
  key idx_int_message_idempotency(source_system, idempotency_key),
  key idx_int_message_status(message_status, id)
);

create table int_dead_letter (
  id bigint primary key auto_increment,
  dead_letter_no varchar(64) not null,
  message_no varchar(64) not null,
  message_type varchar(128) not null,
  source_system varchar(64) not null,
  target_system varchar(64) not null,
  business_no varchar(128) not null,
  payload text not null,
  failure_reason varchar(512) not null,
  replayed bit not null,
  created_at datetime(3) not null,
  updated_at datetime(3) not null,
  unique key uk_int_dead_letter_no(dead_letter_no),
  key idx_int_dead_letter_message(message_no)
);

create table int_operation_log (
  id bigint primary key auto_increment,
  operation_type varchar(64) not null,
  business_no varchar(128) not null,
  operator_id bigint,
  idempotency_key varchar(128),
  created_at datetime(3) not null,
  key idx_int_operation_business(business_no)
);
