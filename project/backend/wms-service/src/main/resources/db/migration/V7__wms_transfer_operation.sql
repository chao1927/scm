create table wms_transfer_operation (
    operation_id bigint not null primary key,
    transfer_no varchar(64) not null,
    owner_id bigint not null,
    source_warehouse_id bigint not null,
    target_warehouse_id bigint not null,
    sku_code varchar(64) not null,
    batch_no varchar(64) null,
    requested_qty decimal(18,6) not null,
    outbound_qty decimal(18,6) not null default 0,
    received_qty decimal(18,6) not null default 0,
    operation_status tinyint not null,
    version int not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_wms_transfer_no(transfer_no),
    key idx_wms_transfer_warehouse(source_warehouse_id,target_warehouse_id,operation_status)
) comment 'WMS调拨执行任务';
