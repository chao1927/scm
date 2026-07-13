create table int_delivery_attempt (
  id bigint primary key auto_increment,
  attempt_no varchar(64) not null,
  message_no varchar(64) not null,
  message_type varchar(128) not null,
  source_system varchar(64) not null,
  target_system varchar(64) not null,
  channel_type varchar(32) not null,
  success bit not null,
  failure_reason varchar(512),
  duration_millis bigint not null,
  created_at datetime(3) not null,
  unique key uk_int_delivery_attempt_no(attempt_no),
  key idx_int_delivery_attempt_message(message_no, id),
  key idx_int_delivery_attempt_created(created_at)
);
