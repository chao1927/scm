alter table purchase_supplier_confirm_fact
    add column processed_status tinyint not null default 1 comment '1待处理 2已接受差异 3协商中 4已取消订单' after payload_json,
    add column process_comment varchar(512) null after processed_status,
    add column processed_by bigint null after process_comment,
    add column processed_at datetime(3) null after processed_by,
    add column process_version int not null default 0 after processed_at,
    add key idx_purchase_confirm_fact_process (processed_status, occurred_at);
