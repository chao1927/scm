create table tms_waybill (
    id bigint primary key auto_increment,
    waybill_no varchar(40) not null,
    task_no varchar(40) not null,
    carrier_code varchar(40) not null,
    carrier_name varchar(120) not null,
    carrier_waybill_no varchar(80) not null,
    logistics_product_code varchar(40) not null,
    receipt_payload varchar(1200) null,
    waybill_status tinyint not null,
    void_reason varchar(300) null,
    approval_no varchar(80) null,
    version bigint not null,
    created_at datetime not null,
    updated_at datetime not null,
    unique key uk_tms_waybill_no (waybill_no),
    key idx_tms_waybill_task (task_no, waybill_status),
    key idx_tms_waybill_carrier (carrier_code, carrier_waybill_no)
);

create table tms_shipping_label (
    id bigint primary key auto_increment,
    label_no varchar(40) not null,
    waybill_no varchar(40) not null,
    package_no varchar(80) not null,
    template_version varchar(40) not null,
    label_url varchar(500) not null,
    label_status tinyint not null,
    print_count int not null,
    last_print_device varchar(80) null,
    void_reason varchar(300) null,
    version bigint not null,
    created_at datetime not null,
    updated_at datetime not null,
    unique key uk_tms_label_no (label_no),
    key idx_tms_label_waybill (waybill_no, package_no, label_status)
);
