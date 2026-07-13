create table int_endpoint (
  id bigint primary key auto_increment,
  endpoint_no varchar(64) not null,
  target_system varchar(64) not null,
  channel_type varchar(32) not null,
  endpoint_url varchar(512) not null,
  timeout_millis int not null,
  failure_threshold int not null,
  consecutive_failures int not null,
  endpoint_status tinyint not null,
  version bigint not null,
  created_at datetime(3) not null,
  updated_at datetime(3) not null,
  unique key uk_int_endpoint_no(endpoint_no),
  key idx_int_endpoint_target_channel(target_system, channel_type, endpoint_status)
);
