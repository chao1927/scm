alter table inv_outbox_event
    add column event_version varchar(16) not null default '1.0' after event_type,
    add column last_error varchar(1024) null after retry_count,
    add column next_retry_at datetime(3) null after last_error,
    add column published_at datetime(3) null after next_retry_at;

alter table inv_inbox_event
    drop index uk_inv_inbox,
    add column event_version varchar(16) not null default '1.0' after event_type,
    add column aggregate_type varchar(64) null after event_version,
    add column aggregate_id varchar(128) null after aggregate_type,
    add column aggregate_version bigint not null default 0 after aggregate_id,
    add column consumer_name varchar(128) not null default 'inventory-domain-event'
        after aggregate_version,
    add column envelope_json json null after payload_json,
    add column ignored_reason varchar(512) null after last_error,
    add unique key uk_inv_inbox_consumer(source_system, event_code, consumer_name);

create table inv_event_aggregate_cursor (
    source_system varchar(64) not null,
    aggregate_type varchar(64) not null,
    aggregate_id varchar(128) not null,
    consumer_name varchar(128) not null,
    aggregate_version bigint not null,
    event_code varchar(128) not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    primary key(source_system, aggregate_type, aggregate_id, consumer_name)
) comment '库存入站事件聚合顺序游标';

create table inv_event_replay_log (
    replay_id bigint not null auto_increment primary key,
    idempotency_key varchar(128) not null,
    direction varchar(16) not null comment 'INBOUND或OUTBOUND',
    event_code varchar(128) not null,
    replay_reason varchar(512) not null,
    operator_id bigint not null,
    replay_status tinyint not null comment '1处理中 2成功 3失败',
    replay_error varchar(1024) null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_inv_event_replay_idempotency(idempotency_key),
    key idx_inv_event_replay_event(direction, event_code, created_at)
) comment '库存事件人工重放审计';
