create table purchase_supplier_quote_fact (
    fact_id bigint not null auto_increment primary key,
    event_code varchar(128) not null,
    quote_no varchar(64) null,
    rfq_no varchar(64) null,
    supplier_id bigint not null,
    sku_code varchar(64) null,
    quote_qty decimal(18, 6) not null default 0,
    quote_amount decimal(18, 2) not null default 0,
    currency varchar(16) null,
    quote_status varchar(64) not null,
    payload_json json not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_purchase_quote_fact_event (event_code),
    key idx_purchase_quote_fact_rfq (rfq_no, supplier_id),
    key idx_purchase_quote_fact_supplier (supplier_id, quote_status)
) comment '采购系统供应商报价事实投影';

create table purchase_supplier_confirm_fact (
    fact_id bigint not null auto_increment primary key,
    event_code varchar(128) not null,
    order_no varchar(64) null,
    supplier_id bigint not null,
    confirm_status varchar(64) not null,
    reason varchar(512) null,
    source_version int not null default 0,
    occurred_at datetime(3) not null,
    payload_json json not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_purchase_confirm_fact_event (event_code),
    key idx_purchase_confirm_fact_order (order_no, supplier_id)
) comment '采购系统供应商订单确认事实投影';

create table purchase_wms_inbound_fact (
    fact_id bigint not null auto_increment primary key,
    event_code varchar(128) not null,
    inbound_no varchar(64) null,
    order_no varchar(64) null,
    asn_no varchar(64) null,
    warehouse_code varchar(64) null,
    event_type varchar(128) not null,
    received_qty decimal(18, 6) not null default 0,
    qualified_qty decimal(18, 6) not null default 0,
    unqualified_qty decimal(18, 6) not null default 0,
    putaway_qty decimal(18, 6) not null default 0,
    reason varchar(512) null,
    occurred_at datetime(3) not null,
    payload_json json not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_purchase_wms_fact_event (event_code),
    key idx_purchase_wms_fact_inbound (inbound_no, event_type),
    key idx_purchase_wms_fact_order (order_no, asn_no)
) comment '采购系统WMS入库执行事实投影';

create table purchase_transport_fact (
    fact_id bigint not null auto_increment primary key,
    event_code varchar(128) not null,
    order_no varchar(64) null,
    inbound_no varchar(64) null,
    asn_no varchar(64) null,
    shipment_id varchar(64) null,
    waybill_no varchar(64) null,
    carrier_code varchar(64) null,
    transport_status varchar(64) not null,
    transport_node varchar(256) null,
    exception_reason varchar(512) null,
    occurred_at datetime(3) not null,
    payload_json json not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_purchase_transport_fact_event (event_code),
    key idx_purchase_transport_fact_order (order_no, inbound_no),
    key idx_purchase_transport_fact_waybill (waybill_no, transport_status)
) comment '采购系统TMS运输事实投影';

create table purchase_bms_fact (
    fact_id bigint not null auto_increment primary key,
    event_code varchar(128) not null,
    order_no varchar(64) null,
    supplier_id bigint not null,
    event_type varchar(128) not null,
    currency varchar(16) null,
    amount decimal(18, 2) not null default 0,
    source_version int not null default 0,
    payload_json json not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_purchase_bms_fact_event (event_code),
    key idx_purchase_bms_fact_order (order_no, supplier_id)
) comment '采购系统BMS应付结算事实投影';
