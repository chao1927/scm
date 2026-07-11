create table purchase_requisition (
    id bigint not null primary key,
    requisition_no varchar(64) not null,
    applicant_id bigint not null,
    purchase_org_id bigint not null,
    demand_department_id bigint not null,
    status tinyint not null comment '1草稿 2审批中 3已批准 4已驳回 5部分转采购 6已转采购 7已关闭',
    reason varchar(512) null,
    version int not null,
    deleted tinyint not null default 0,
    created_by bigint not null,
    updated_by bigint not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_purchase_requisition_no (requisition_no),
    key idx_purchase_requisition_org_status (purchase_org_id, status, updated_at),
    key idx_purchase_requisition_applicant (applicant_id, updated_at)
) comment '采购请购单';

create table purchase_requisition_line (
    line_id bigint not null primary key,
    requisition_id bigint not null,
    sku_code varchar(64) not null,
    requested_qty decimal(18, 6) not null,
    approved_qty decimal(18, 6) not null default 0,
    converted_qty decimal(18, 6) not null default 0,
    purchase_unit varchar(32) null,
    required_date date not null,
    remark varchar(512) null,
    deleted tinyint not null default 0,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_purchase_requisition_line_sku_date (requisition_id, sku_code, required_date),
    key idx_purchase_requisition_line_sku (sku_code, required_date)
) comment '采购请购单行';

create table purchase_outbox_event (
    event_id bigint not null auto_increment primary key,
    event_code varchar(128) not null,
    event_type varchar(128) not null,
    aggregate_type varchar(64) not null,
    aggregate_id varchar(64) not null,
    aggregate_version int not null,
    payload_json json not null,
    status tinyint not null comment '1待发布 2发布中 3已发布 4失败',
    retry_count int not null default 0,
    last_error varchar(1024) null,
    occurred_at datetime(3) not null,
    published_at datetime(3) null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_purchase_outbox_event_code (event_code),
    key idx_purchase_outbox_dispatch (status, retry_count, created_at)
) comment '采购系统领域事件Outbox';

create table purchase_operation_log (
    log_id bigint not null auto_increment primary key,
    request_id varchar(128) null,
    trace_id varchar(128) null,
    operator_id bigint not null,
    operator_name varchar(128) null,
    operation varchar(64) not null,
    target_type varchar(64) not null,
    target_id bigint not null,
    target_no varchar(64) not null,
    before_snapshot json null,
    after_snapshot json null,
    created_at datetime(3) not null,
    key idx_purchase_operation_target (target_type, target_id, created_at),
    key idx_purchase_operation_trace (trace_id)
) comment '采购系统操作日志';
