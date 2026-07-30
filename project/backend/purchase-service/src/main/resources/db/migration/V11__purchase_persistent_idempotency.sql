create table purchase_idempotency (
    id bigint primary key auto_increment,
    business_type varchar(128) not null,
    idempotency_key varchar(128) not null,
    request_digest char(64) not null,
    process_status tinyint not null comment '1 processing, 2 completed, 3 failed',
    result_id bigint null,
    result_business_no varchar(128) null,
    result_status int null,
    result_status_name varchar(128) null,
    result_version int null,
    result_event_code varchar(128) null,
    failure_reason varchar(500) null,
    completed_at datetime(3) null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_purchase_idempotency (business_type, idempotency_key),
    key idx_purchase_idempotency_status (process_status, updated_at)
);
