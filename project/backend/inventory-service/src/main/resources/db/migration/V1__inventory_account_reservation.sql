create table inv_stock_balance (
    stock_id bigint not null primary key,
    owner_id bigint not null,
    warehouse_id bigint not null,
    sku_code varchar(64) not null,
    batch_no varchar(128) null,
    on_hand_qty decimal(18,4) not null,
    available_qty decimal(18,4) not null,
    reserved_qty decimal(18,4) not null,
    frozen_qty decimal(18,4) not null,
    version int not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_inv_stock_dim(owner_id, warehouse_id, sku_code, batch_no)
) comment '中央库存账户余额';

create table inv_stock_ledger (
    ledger_id bigint not null primary key,
    ledger_no varchar(64) not null,
    stock_id bigint not null,
    ledger_type varchar(32) not null,
    qty_delta decimal(18,4) not null,
    source_system varchar(32) not null,
    source_order_no varchar(64) not null,
    created_at datetime(3) not null,
    unique key uk_inv_ledger_no(ledger_no),
    key idx_inv_ledger_stock(stock_id, created_at),
    key idx_inv_ledger_source(source_system, source_order_no)
) comment '中央库存流水';

create table inv_reservation (
    reservation_id bigint not null primary key,
    reservation_no varchar(64) not null,
    stock_id bigint not null,
    source_system varchar(32) not null,
    source_order_no varchar(64) not null,
    reserved_qty decimal(18,4) not null,
    released_qty decimal(18,4) not null,
    reservation_status tinyint not null comment '1已预占 2已释放 3已关闭',
    version int not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_inv_reservation_no(reservation_no),
    unique key uk_inv_reservation_source(source_system, source_order_no)
) comment '中央库存预占单';
