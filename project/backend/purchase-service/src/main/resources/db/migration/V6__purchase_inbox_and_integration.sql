create table purchase_inbox_event (
    inbox_id bigint not null auto_increment primary key,
    source_system varchar(64) not null,
    event_code varchar(128) not null,
    event_type varchar(128) not null,
    consumer_name varchar(128) not null,
    idempotent_key varchar(256) not null,
    payload_json json not null,
    status tinyint not null comment '1处理中 2成功 3失败 4忽略',
    retry_count int not null default 0,
    last_error varchar(1024) null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_purchase_inbox_event (source_system, event_code, consumer_name),
    key idx_purchase_inbox_status (status, updated_at),
    key idx_purchase_inbox_consumer (consumer_name, status, updated_at)
) comment '采购系统入站事件收件箱';

create table purchase_integration_command (
    command_id bigint not null auto_increment primary key,
    command_type varchar(128) not null,
    target_system varchar(64) not null,
    business_type varchar(64) not null,
    business_id varchar(64) not null,
    business_no varchar(64) null,
    payload_json json not null,
    status tinyint not null comment '1待发送 2发送中 3已发送 4失败 5已取消',
    retry_count int not null default 0,
    last_error varchar(1024) null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    key idx_purchase_integration_dispatch (status, retry_count, created_at),
    key idx_purchase_integration_business (business_type, business_id)
) comment '采购系统跨系统主动命令';
