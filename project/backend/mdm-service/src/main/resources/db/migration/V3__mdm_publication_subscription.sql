create table mdm_publication_subscription (
  id bigint primary key auto_increment,
  subscription_no varchar(64) not null,
  type_code varchar(64) not null,
  target_system varchar(64) not null,
  event_topic varchar(128) not null,
  filter_rule text,
  subscription_status tinyint not null,
  version bigint not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_mdm_subscription_no (subscription_no),
  key idx_mdm_subscription_active (type_code, subscription_status)
);

create table mdm_publication_log (
  id bigint primary key auto_increment,
  publication_no varchar(64) not null,
  version_no varchar(64) not null,
  type_code varchar(64) not null,
  data_code varchar(128) not null,
  target_system varchar(64) not null,
  event_topic varchar(128) not null,
  publish_status tinyint not null,
  retry_count int not null,
  failure_reason varchar(512),
  version bigint not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_mdm_publication_no (publication_no),
  key idx_mdm_publication_version (version_no),
  key idx_mdm_publication_status (publish_status, id)
);

create table mdm_event_inbox (
  event_id varchar(128) primary key,
  event_type varchar(128) not null,
  business_no varchar(128) not null,
  payload text,
  event_status tinyint not null,
  error_message varchar(512),
  created_at datetime not null,
  updated_at datetime not null,
  key idx_mdm_inbox_business (business_no)
);
