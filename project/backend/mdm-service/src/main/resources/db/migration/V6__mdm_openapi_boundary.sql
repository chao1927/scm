create table mdm_openapi_client (
  id bigint primary key auto_increment,
  app_code varchar(64) not null,
  secret_value varchar(256) not null,
  type_scope text not null,
  data_code_prefixes text,
  field_allowlist text not null,
  enabled bit not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_mdm_openapi_client_app (app_code)
);

create table mdm_openapi_snapshot (
  id bigint primary key auto_increment,
  record_no varchar(64) not null,
  type_code varchar(64) not null,
  data_code varchar(128) not null,
  data_name varchar(256) not null,
  data_payload text not null,
  record_status tinyint not null,
  current_version_no int not null,
  record_version bigint not null,
  projected_at datetime not null,
  unique key uk_mdm_openapi_snapshot_code (type_code, data_code),
  key idx_mdm_openapi_snapshot_version (type_code, record_version)
);
