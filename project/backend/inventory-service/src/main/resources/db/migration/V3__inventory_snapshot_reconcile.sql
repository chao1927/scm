create table inv_stock_snapshot (
    snapshot_id bigint not null primary key,
    snapshot_no varchar(64) not null,
    stock_id bigint not null,
    on_hand_qty decimal(18,4) not null,
    available_qty decimal(18,4) not null,
    created_at datetime(3) not null,
    unique key uk_inv_snapshot_no(snapshot_no),
    key idx_inv_snapshot_stock(stock_id, created_at)
) comment '中央库存快照';

create table inv_stock_reconcile (
    reconcile_id bigint not null primary key,
    reconcile_no varchar(64) not null,
    stock_id bigint not null,
    system_qty decimal(18,4) not null,
    wms_qty decimal(18,4) not null,
    difference_qty decimal(18,4) not null,
    reconcile_status tinyint not null comment '1待确认 2已确认',
    version int not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_inv_reconcile_no(reconcile_no),
    key idx_inv_reconcile_stock(stock_id, reconcile_status)
) comment '中央库存对账单';
