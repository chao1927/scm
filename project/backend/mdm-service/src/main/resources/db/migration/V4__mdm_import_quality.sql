create table mdm_import_task (
  id bigint primary key auto_increment,
  import_task_no varchar(64) not null,
  type_code varchar(64) not null,
  file_name varchar(256) not null,
  file_url varchar(512) not null,
  file_hash varchar(128) not null,
  import_mode varchar(32) not null,
  validate_only bit not null,
  duplicate_policy varchar(32) not null,
  task_status tinyint not null,
  total_count int not null,
  success_count int not null,
  failed_count int not null,
  error_file_url varchar(512),
  reason varchar(512),
  version bigint not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_mdm_import_task_no (import_task_no),
  unique key uk_mdm_import_idempotent (type_code, file_hash, import_mode),
  key idx_mdm_import_status (task_status, id)
);

create table mdm_import_error (
  id bigint primary key auto_increment,
  import_task_no varchar(64) not null,
  row_no int not null,
  field_code varchar(128),
  error_code varchar(64) not null,
  error_message varchar(512) not null,
  raw_payload text,
  created_at datetime not null,
  key idx_mdm_import_error_task (import_task_no, row_no)
);

create table mdm_export_task (
  id bigint primary key auto_increment,
  export_task_no varchar(64) not null,
  type_code varchar(64) not null,
  filter_payload text,
  field_payload text,
  mask_sensitive_fields bit not null,
  export_status tinyint not null,
  file_url varchar(512),
  version bigint not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_mdm_export_task_no (export_task_no),
  key idx_mdm_export_type (type_code, id)
);

create table mdm_data_quality_issue (
  id bigint primary key auto_increment,
  issue_no varchar(64) not null,
  type_code varchar(64) not null,
  data_code varchar(128) not null,
  issue_type varchar(64) not null,
  issue_description varchar(512) not null,
  issue_status tinyint not null,
  assignee_id bigint,
  resolution varchar(512),
  version bigint not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_mdm_quality_issue_no (issue_no),
  key idx_mdm_quality_type_status (type_code, issue_status)
);
