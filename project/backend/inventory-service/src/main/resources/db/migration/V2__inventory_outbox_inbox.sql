create table inv_outbox_event (
    event_id bigint not null primary key,
    event_code varchar(128) not null,
    event_type varchar(128) not null,
    aggregate_type varchar(64) not null,
    aggregate_id varchar(64) not null,
    payload_json json not null,
    status tinyint not null comment '1待投递 2已投递 3失败',
    retry_count int not null default 0,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_inv_outbox_event_code(event_code),
    key idx_inv_outbox_status(status, created_at)
) comment '中央库存Outbox事件';

create table inv_inbox_event (
    inbox_id bigint not null auto_increment primary key,
    source_system varchar(64) not null,
    event_code varchar(128) not null,
    event_type varchar(128) not null,
    payload_json json not null,
    status tinyint not null comment '1处理中 2成功 3失败',
    retry_count int not null default 0,
    last_error varchar(1024) null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_inv_inbox(source_system, event_code),
    key idx_inv_inbox_status(status, updated_at)
) comment '中央库存Inbox事件';
