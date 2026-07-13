create table tms_tracking_node (
    id bigint primary key auto_increment,
    track_no varchar(40) not null,
    waybill_no varchar(40) not null,
    node_code varchar(40) not null,
    description varchar(300) not null,
    location varchar(200) null,
    track_at datetime not null,
    source_type varchar(60) not null,
    raw_event_id varchar(120) null,
    manual_reason varchar(300) null,
    created_at datetime not null,
    unique key uk_tms_track_no (track_no),
    key idx_tms_track_waybill (waybill_no, track_at),
    key idx_tms_track_dedupe (waybill_no, node_code, track_at)
);

create table tms_delivery_receipt (
    id bigint primary key auto_increment,
    receipt_no varchar(40) not null,
    waybill_no varchar(40) not null,
    receipt_result tinyint not null,
    signed_by varchar(80) null,
    signed_at datetime not null,
    reject_reason varchar(300) null,
    proof_url varchar(500) null,
    created_at datetime not null,
    unique key uk_tms_receipt_no (receipt_no),
    unique key uk_tms_receipt_waybill (waybill_no)
);

create table tms_event_inbox (
    id bigint primary key auto_increment,
    event_id varchar(160) not null,
    event_type varchar(80) not null,
    business_no varchar(80) not null,
    payload varchar(2000) null,
    event_status tinyint not null,
    error_message varchar(500) null,
    created_at datetime not null,
    updated_at datetime not null,
    unique key uk_tms_inbox_event (event_id),
    key idx_tms_inbox_status (event_status, id)
);
