create table mdm_master_data_type (
  id bigint primary key auto_increment,
  type_code varchar(64) not null,
  type_name varchar(128) not null,
  domain_code varchar(64) not null,
  type_status tinyint not null,
  version bigint not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_mdm_type_code (type_code)
);

create table mdm_field_template (
  id bigint primary key auto_increment,
  template_code varchar(64) not null,
  type_code varchar(64) not null,
  field_payload text not null,
  template_status tinyint not null,
  version bigint not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_mdm_template_code (template_code),
  key idx_mdm_template_type (type_code)
);

create table mdm_code_rule (
  id bigint primary key auto_increment,
  rule_code varchar(64) not null,
  type_code varchar(64) not null,
  prefix varchar(32) not null,
  serial_length int not null,
  rule_status tinyint not null,
  current_serial bigint not null,
  version bigint not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_mdm_rule_code (rule_code),
  key idx_mdm_rule_type (type_code)
);

create table mdm_outbox_event (
  id bigint primary key auto_increment,
  event_type varchar(128) not null,
  business_no varchar(128) not null,
  payload text not null,
  event_status tinyint not null,
  occurred_at datetime not null,
  created_at datetime not null,
  key idx_mdm_outbox_status (event_status, id)
);

create table mdm_operation_log (
  id bigint primary key auto_increment,
  operation_type varchar(64) not null,
  business_no varchar(128) not null,
  operator_id bigint,
  idempotency_key varchar(128),
  created_at datetime not null,
  key idx_mdm_operation_business (business_no)
);
