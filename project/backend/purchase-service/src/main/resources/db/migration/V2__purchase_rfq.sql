create table purchase_rfq (
    id bigint not null primary key,
    rfq_no varchar(64) not null,
    rfq_type tinyint not null comment '1公开询价 2定向询价 3议价',
    purchase_org_id bigint not null,
    category_code varchar(64) null,
    source_requisition_no varchar(64) null,
    quote_deadline datetime(3) not null,
    status tinyint not null comment '1草稿 2已发布 3报价中 4已截标 5已定标 6已取消 7已关闭',
    published_at datetime(3) null,
    close_reason varchar(512) null,
    version int not null,
    deleted tinyint not null default 0,
    created_by bigint not null,
    updated_by bigint not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_purchase_rfq_no (rfq_no),
    key idx_purchase_rfq_org_status (purchase_org_id, status, updated_at),
    key idx_purchase_rfq_deadline (quote_deadline),
    key idx_purchase_rfq_category (category_code, status)
) comment '采购询价单';

create table purchase_rfq_line (
    line_id bigint not null primary key,
    rfq_id bigint not null,
    sku_code varchar(64) not null,
    target_qty decimal(18, 6) not null,
    uom varchar(32) not null,
    required_delivery_date date null,
    quality_requirement varchar(512) null,
    deleted tinyint not null default 0,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_purchase_rfq_line_sku (rfq_id, sku_code),
    key idx_purchase_rfq_line_sku (sku_code)
) comment '采购询价行';

create table purchase_rfq_invitation (
    invitation_id bigint not null primary key,
    rfq_id bigint not null,
    supplier_id bigint not null,
    quote_status tinyint not null comment '1待报价 2已报价 3已确认 4已关闭',
    deleted tinyint not null default 0,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_purchase_rfq_invitation_supplier (rfq_id, supplier_id),
    key idx_purchase_rfq_invitation_supplier (supplier_id, quote_status)
) comment '采购询价邀请供应商';
