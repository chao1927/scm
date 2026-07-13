create table tms_transport_task (
    id bigint primary key auto_increment,
    task_no varchar(40) not null,
    source_system varchar(40) not null,
    source_order_no varchar(80) not null,
    source_line_no varchar(80) null,
    scenario varchar(40) not null,
    shipper_id bigint not null,
    warehouse_id bigint not null,
    origin_address varchar(800) not null,
    destination_address varchar(800) not null,
    package_payload varchar(1200) not null,
    task_status tinyint not null,
    carrier_code varchar(40) null,
    carrier_name varchar(120) null,
    logistics_product_code varchar(40) not null,
    fee_responsibility varchar(40) not null,
    version bigint not null,
    created_at datetime not null,
    updated_at datetime not null,
    unique key uk_tms_transport_task_no (task_no),
    key idx_tms_transport_source (source_system, source_order_no, scenario),
    key idx_tms_transport_query (warehouse_id, carrier_code, task_status)
);

create table tms_domain_event (
    id bigint primary key auto_increment,
    event_type varchar(80) not null,
    business_no varchar(80) not null,
    payload varchar(2000) not null,
    event_status tinyint not null,
    occurred_at datetime not null,
    created_at datetime not null,
    key idx_tms_event_status (event_status, id),
    key idx_tms_event_business (business_no)
);

create table tms_operation_log (
    id bigint primary key auto_increment,
    operation_type varchar(80) not null,
    business_no varchar(80) not null,
    operator_id bigint null,
    idempotency_key varchar(120) null,
    created_at datetime not null,
    key idx_tms_operation_business (business_no),
    key idx_tms_operation_idempotency (idempotency_key)
);
