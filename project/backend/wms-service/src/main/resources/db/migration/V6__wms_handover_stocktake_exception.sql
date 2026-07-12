create table wms_shipment_handover (
    handover_id bigint not null primary key,
    handover_no varchar(64) not null,
    outbound_id bigint not null,
    handover_status tinyint not null comment '1待交接 2已交接',
    version int not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_wms_handover_no(handover_no)
) comment 'WMS发货交接单';

create table wms_stocktake (
    stocktake_id bigint not null primary key,
    stocktake_no varchar(64) not null,
    warehouse_id bigint not null,
    sku_code varchar(64) not null,
    difference_qty decimal(18,6) not null,
    stocktake_status tinyint not null comment '1待确认 2已确认',
    version int not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_wms_stocktake_no(stocktake_no)
) comment 'WMS盘点差异';

create table wms_warehouse_exception (
    exception_id bigint not null primary key,
    exception_no varchar(64) not null,
    reason varchar(512) not null,
    exception_status tinyint not null comment '1处理中 2已关闭',
    version int not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_wms_exception_no(exception_no)
) comment 'WMS仓内异常';
