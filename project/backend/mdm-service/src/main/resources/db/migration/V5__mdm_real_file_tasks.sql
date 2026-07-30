alter table mdm_import_task
  add column processing_started_at datetime null after reason,
  add column attempt_count int not null default 0 after processing_started_at,
  add column next_retry_at datetime null after attempt_count,
  add key idx_mdm_import_dispatch (task_status, next_retry_at, processing_started_at);

create table mdm_import_staging (
  id bigint primary key auto_increment,
  import_task_no varchar(64) not null,
  row_no int not null,
  data_code varchar(128) not null,
  data_name varchar(256) not null,
  data_payload text not null,
  created_at datetime not null,
  unique key uk_mdm_import_staging_row (import_task_no, row_no),
  key idx_mdm_import_staging_task (import_task_no)
);

alter table mdm_export_task
  add column failure_reason varchar(512) null after file_url,
  add column retry_count int not null default 0 after failure_reason,
  add column processing_started_at datetime null after retry_count,
  add column next_retry_at datetime null after processing_started_at,
  add key idx_mdm_export_dispatch (export_status, next_retry_at, processing_started_at);
