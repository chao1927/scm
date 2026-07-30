create table wms_return_operation (
 operation_id bigint primary key, after_sale_no varchar(64) not null, rma_no varchar(64) not null,
 owner_id bigint not null, warehouse_id bigint not null, sku_code varchar(64) not null, batch_no varchar(64),
 expected_qty decimal(18,4) not null, received_qty decimal(18,4) not null default 0,
 sellable_qty decimal(18,4) not null default 0, defective_qty decimal(18,4) not null default 0,
 frozen_qty decimal(18,4) not null default 0, scrapped_qty decimal(18,4) not null default 0,
 unmatched_qty decimal(18,4) not null default 0, operation_status tinyint not null, version int not null,
 created_at datetime(3) not null, updated_at datetime(3) not null,
 unique key uk_wms_return_after_sale(after_sale_no), unique key uk_wms_return_rma(rma_no),
 key idx_wms_return_work(warehouse_id,operation_status,updated_at)
);
