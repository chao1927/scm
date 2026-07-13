create table tms_logistics_exception (
    id bigint primary key auto_increment,
    exception_no varchar(40) not null,
    waybill_no varchar(40) not null,
    exception_type varchar(40) not null,
    exception_level varchar(20) not null,
    description varchar(500) not null,
    responsible_party varchar(40) null,
    exception_status tinyint not null,
    close_result varchar(500) null,
    version bigint not null,
    created_at datetime not null,
    updated_at datetime not null,
    unique key uk_tms_exception_no (exception_no),
    key idx_tms_exception_waybill (waybill_no, exception_status)
);

create table tms_logistics_fee_source (
    id bigint primary key auto_increment,
    fee_source_no varchar(40) not null,
    waybill_no varchar(40) not null,
    carrier_code varchar(40) not null,
    logistics_product_code varchar(40) not null,
    fee_item_code varchar(40) not null,
    amount decimal(18, 6) not null,
    currency varchar(10) not null,
    billing_period varchar(20) not null,
    responsible_party varchar(40) not null,
    push_status tinyint not null,
    bms_receive_no varchar(80) null,
    failure_reason varchar(500) null,
    version bigint not null,
    created_at datetime not null,
    updated_at datetime not null,
    unique key uk_tms_fee_source_no (fee_source_no),
    key idx_tms_fee_source_waybill_item (waybill_no, fee_item_code),
    key idx_tms_fee_source_push (push_status, id)
);
