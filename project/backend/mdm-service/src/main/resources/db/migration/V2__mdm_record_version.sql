create table mdm_master_data_record (
  id bigint primary key auto_increment,
  record_no varchar(64) not null,
  type_code varchar(64) not null,
  data_code varchar(128) not null,
  data_name varchar(256) not null,
  data_payload text not null,
  record_status tinyint not null,
  current_version_no int not null,
  reason varchar(512),
  version bigint not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_mdm_record_no (record_no),
  unique key uk_mdm_record_type_code (type_code, data_code),
  key idx_mdm_record_status (record_status, id)
);

create table mdm_master_data_version (
  id bigint primary key auto_increment,
  version_no varchar(64) not null,
  record_no varchar(64) not null,
  type_code varchar(64) not null,
  data_code varchar(128) not null,
  version_number int not null,
  snapshot_payload text not null,
  change_summary varchar(512),
  created_at datetime not null,
  unique key uk_mdm_version_no (version_no),
  unique key uk_mdm_version_record_number (record_no, version_number),
  key idx_mdm_version_type_code (type_code, data_code)
);
