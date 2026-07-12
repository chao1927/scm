create table wms_inbound (
    inbound_id bigint not null primary key,
    inbound_order_no varchar(64) not null,
    source_type varchar(32) not null,
    source_order_no varchar(64) not null,
    warehouse_id bigint not null,
    inbound_status tinyint not null comment '1待到货 2已到货 3收货中 4待质检 9已取消',
    expected_arrival_at datetime(3) null,
    cancel_reason varchar(512) null,
    version int not null,
    deleted tinyint not null default 0,
    created_by bigint not null,
    updated_by bigint not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_wms_inbound_no (inbound_order_no),
    unique key uk_wms_inbound_source (source_type, source_order_no, warehouse_id),
    key idx_wms_inbound_warehouse_status (warehouse_id, inbound_status, updated_at)
) comment 'WMS入库单';
