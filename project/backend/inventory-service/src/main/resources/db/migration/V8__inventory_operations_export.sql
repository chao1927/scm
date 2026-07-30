create table inv_inventory_batch_fact (
    stock_id bigint not null primary key,
    expiry_date date null,
    expiry_source_event varchar(128) not null,
    expiry_fact_at datetime(3) not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    key idx_inv_batch_expiry(expiry_date, stock_id)
) comment 'WMS批次效期事实投影';

create table inv_export_task (
    export_task_id bigint not null auto_increment primary key,
    task_no varchar(64) not null,
    export_type varchar(32) not null,
    query_json json not null,
    owner_scope_json json not null,
    warehouse_scope_json json not null,
    created_by bigint not null,
    idempotency_key varchar(128) not null,
    request_fingerprint varchar(1024) not null,
    task_status tinyint not null comment '1待执行 2处理中 3已完成 4失败',
    retry_count int not null default 0,
    next_retry_at datetime(3) null,
    started_at datetime(3) null,
    completed_at datetime(3) null,
    object_key varchar(512) null,
    file_name varchar(255) null,
    content_type varchar(128) null,
    file_size bigint null,
    last_error varchar(1024) null,
    version int not null default 0,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_inv_export_task_no(task_no),
    unique key uk_inv_export_idempotency(created_by, idempotency_key),
    key idx_inv_export_dispatch(task_status, next_retry_at, updated_at),
    key idx_inv_export_creator(created_by, created_at)
) comment '库存异步导出任务';

alter table inv_reservation
    add key idx_inv_reservation_scope(owner_id, warehouse_id, reservation_status, updated_at);

alter table inv_freeze
    add key idx_inv_freeze_scope(owner_id, warehouse_id, freeze_status, updated_at);

alter table inv_stock_adjustment
    add key idx_inv_adjustment_scope(owner_id, warehouse_id, adjustment_status, updated_at);
