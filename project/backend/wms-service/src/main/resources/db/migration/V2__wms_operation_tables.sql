create table wms_receipt (
    receipt_id bigint not null primary key,
    receipt_no varchar(64) not null,
    inbound_id bigint not null,
    sku_code varchar(64) not null,
    expected_qty decimal(18,6) not null,
    received_qty decimal(18,6) not null default 0,
    rejected_qty decimal(18,6) not null default 0,
    receipt_status tinyint not null comment '1收货中 2已收货 3异常',
    version int not null, created_by bigint not null, updated_by bigint not null,
    created_at datetime(3) not null, updated_at datetime(3) not null,
    unique key uk_wms_receipt_no(receipt_no), key idx_wms_receipt_inbound(inbound_id, receipt_status)
) comment 'WMS收货单';

create table wms_inspection (
    inspection_id bigint not null primary key, inspection_no varchar(64) not null, receipt_id bigint not null,
    inspect_qty decimal(18,6) not null, qualified_qty decimal(18,6) not null default 0,
    unqualified_qty decimal(18,6) not null default 0, inspection_status tinyint not null comment '1待检 2已完成',
    version int not null, created_by bigint not null, updated_by bigint not null, created_at datetime(3) not null, updated_at datetime(3) not null,
    unique key uk_wms_inspection_no(inspection_no), key idx_wms_inspection_receipt(receipt_id, inspection_status)
) comment 'WMS质检单';

create table wms_putaway_task (
    task_id bigint not null primary key, task_no varchar(64) not null, inspection_id bigint not null,
    required_qty decimal(18,6) not null, putaway_qty decimal(18,6) not null default 0,
    task_status tinyint not null comment '1待上架 2已完成', version int not null,
    created_by bigint not null, updated_by bigint not null, created_at datetime(3) not null, updated_at datetime(3) not null,
    unique key uk_wms_putaway_no(task_no), key idx_wms_putaway_inspection(inspection_id, task_status)
) comment 'WMS上架任务';

create table wms_stock_ledger (
    ledger_id bigint not null primary key, warehouse_id bigint not null, location_code varchar(64) not null,
    sku_code varchar(64) not null, batch_no varchar(64) null, transaction_type varchar(32) not null,
    quantity decimal(18,6) not null, source_type varchar(32) not null, source_no varchar(64) not null,
    occurred_at datetime(3) not null, created_at datetime(3) not null,
    key idx_wms_ledger_stock(warehouse_id, location_code, sku_code, occurred_at), key idx_wms_ledger_source(source_type, source_no)
) comment 'WMS仓内库存流水';

create table wms_outbound (
    outbound_id bigint not null primary key, outbound_no varchar(64) not null, source_type varchar(32) not null,
    source_no varchar(64) not null, warehouse_id bigint not null, outbound_status tinyint not null,
    version int not null, created_by bigint not null, updated_by bigint not null, created_at datetime(3) not null, updated_at datetime(3) not null,
    unique key uk_wms_outbound_no(outbound_no), unique key uk_wms_outbound_source(source_type, source_no, warehouse_id)
) comment 'WMS出库单';

create table wms_wave (wave_id bigint not null primary key, wave_no varchar(64) not null, warehouse_id bigint not null,
    wave_status tinyint not null, version int not null, created_at datetime(3) not null, updated_at datetime(3) not null,
    unique key uk_wms_wave_no(wave_no)) comment 'WMS波次单';

create table wms_pick_task (task_id bigint not null primary key, task_no varchar(64) not null, wave_id bigint not null,
    outbound_id bigint not null, sku_code varchar(64) not null, required_qty decimal(18,6) not null, picked_qty decimal(18,6) not null default 0,
    task_status tinyint not null, version int not null, created_at datetime(3) not null, updated_at datetime(3) not null,
    unique key uk_wms_pick_task_no(task_no), key idx_wms_pick_wave(wave_id, task_status)) comment 'WMS拣货任务';

create table wms_operation_event (event_id bigint not null primary key, event_code varchar(128) not null, event_type varchar(128) not null,
    aggregate_type varchar(64) not null, aggregate_id varchar(64) not null, aggregate_version int not null, payload_json json not null,
    status tinyint not null default 1, retry_count int not null default 0, created_at datetime(3) not null, updated_at datetime(3) not null,
    unique key uk_wms_event_code(event_code), key idx_wms_event_status(status, created_at)) comment 'WMS Outbox事件';
