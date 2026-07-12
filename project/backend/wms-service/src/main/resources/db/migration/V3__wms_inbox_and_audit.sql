create table wms_inbox_event (
    inbox_id bigint not null auto_increment primary key, source_system varchar(64) not null, event_code varchar(128) not null,
    event_type varchar(128) not null, payload_json json not null, status tinyint not null comment '1处理中 2成功 3失败',
    retry_count int not null default 0, last_error varchar(1024) null, created_at datetime(3) not null, updated_at datetime(3) not null,
    unique key uk_wms_inbox(source_system,event_code), key idx_wms_inbox_status(status,updated_at)
) comment 'WMS入站事件收件箱';
create table wms_operation_log (
    log_id bigint not null auto_increment primary key, request_id varchar(128) null, trace_id varchar(128) null,
    operator_id bigint not null, operation varchar(128) not null, target_type varchar(64) not null, target_no varchar(64) not null,
    before_snapshot json null, after_snapshot json null, created_at datetime(3) not null,
    key idx_wms_operation_target(target_type,target_no,created_at)
) comment 'WMS操作审计日志';
