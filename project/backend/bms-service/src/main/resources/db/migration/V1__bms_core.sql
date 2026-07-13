create table bms_billing_object (
    id bigint primary key auto_increment,
    object_code varchar(64) not null,
    object_name varchar(128) not null,
    object_type varchar(32) not null,
    direction varchar(16) not null,
    currency varchar(16) not null,
    status int not null,
    version bigint not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_bms_billing_object_code (object_code)
);

create table bms_billing_rule (
    id bigint primary key auto_increment,
    rule_no varchar(64) not null,
    object_code varchar(64) not null,
    fee_type varchar(64) not null,
    unit_price decimal(18,4) not null,
    tax_rate decimal(8,4) not null,
    effective_from date not null,
    effective_to date not null,
    status int not null,
    rule_version int not null,
    version bigint not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_bms_billing_rule_no (rule_no),
    key idx_bms_billing_rule_object_fee (object_code, fee_type, status)
);

create table bms_charge_source (
    id bigint primary key auto_increment,
    source_no varchar(64) not null,
    source_system varchar(64) not null,
    source_event_id varchar(128) not null,
    idempotency_key varchar(128) not null,
    billing_object_code varchar(64) not null,
    fee_type varchar(64) not null,
    quantity decimal(18,4) not null,
    billing_period varchar(16) not null,
    payload text not null,
    status int not null,
    failure_reason varchar(512),
    version bigint not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_bms_charge_source_no (source_no),
    unique key uk_bms_charge_source_idem (source_system, idempotency_key),
    key idx_bms_charge_source_event (source_system, source_event_id)
);

create table bms_charge_detail (
    id bigint primary key auto_increment,
    charge_no varchar(64) not null,
    source_no varchar(64) not null,
    billing_object_code varchar(64) not null,
    fee_type varchar(64) not null,
    rule_no varchar(64) not null,
    quantity decimal(18,4) not null,
    unit_price decimal(18,4) not null,
    amount decimal(18,2) not null,
    tax_amount decimal(18,2) not null,
    total_amount decimal(18,2) not null,
    billing_period varchar(16) not null,
    status int not null,
    version bigint not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_bms_charge_detail_no (charge_no),
    key idx_bms_charge_detail_source (source_no),
    key idx_bms_charge_detail_period (billing_object_code, billing_period, status)
);

create table bms_adjustment (
    id bigint primary key auto_increment,
    adjustment_no varchar(64) not null,
    original_charge_no varchar(64) not null,
    adjust_amount decimal(18,2) not null,
    reason varchar(512) not null,
    status int not null,
    version bigint not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_bms_adjustment_no (adjustment_no)
);

create table bms_reconciliation (
    id bigint primary key auto_increment,
    reconciliation_no varchar(64) not null,
    billing_object_code varchar(64) not null,
    billing_period varchar(16) not null,
    total_amount decimal(18,2) not null,
    status int not null,
    version bigint not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_bms_reconciliation_no (reconciliation_no),
    key idx_bms_reconciliation_object_period (billing_object_code, billing_period)
);

create table bms_bill (
    id bigint primary key auto_increment,
    bill_no varchar(64) not null,
    reconciliation_no varchar(64) not null,
    billing_object_code varchar(64) not null,
    total_amount decimal(18,2) not null,
    status int not null,
    version bigint not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_bms_bill_no (bill_no),
    key idx_bms_bill_reconciliation (reconciliation_no)
);

create table bms_invoice (
    id bigint primary key auto_increment,
    invoice_no varchar(64) not null,
    bill_no varchar(64) not null,
    invoice_amount decimal(18,2) not null,
    status int not null,
    version bigint not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_bms_invoice_no (invoice_no),
    key idx_bms_invoice_bill (bill_no)
);

create table bms_finance_handover (
    id bigint primary key auto_increment,
    handover_no varchar(64) not null,
    bill_no varchar(64) not null,
    status int not null,
    voucher_no varchar(128),
    failure_reason varchar(512),
    version bigint not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_bms_finance_handover_no (handover_no),
    key idx_bms_finance_handover_bill (bill_no)
);

create table bms_refund_settlement (
    id bigint primary key auto_increment,
    refund_no varchar(64) not null,
    bill_no varchar(64) not null,
    refund_amount decimal(18,2) not null,
    status int not null,
    failure_reason varchar(512),
    version bigint not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_bms_refund_no (refund_no),
    key idx_bms_refund_bill (bill_no)
);

create table bms_domain_event (
    id bigint primary key auto_increment,
    event_no varchar(64) not null,
    event_type varchar(128) not null,
    aggregate_no varchar(64) not null,
    business_no varchar(64) not null,
    payload text not null,
    status int not null,
    created_at datetime(3) not null,
    unique key uk_bms_event_no (event_no),
    key idx_bms_event_status (status, id)
);

create table bms_event_consume_log (
    id bigint primary key auto_increment,
    inbox_no varchar(64) not null,
    source_system varchar(64) not null,
    source_event_id varchar(128) not null,
    event_type varchar(128) not null,
    business_no varchar(64) not null,
    payload text not null,
    status int not null,
    failure_reason varchar(512),
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_bms_inbox_source_event (source_system, source_event_id),
    unique key uk_bms_inbox_no (inbox_no)
);

create table bms_operation_audit_log (
    id bigint primary key auto_increment,
    operation_type varchar(128) not null,
    business_no varchar(64) not null,
    operator_id bigint,
    idempotency_key varchar(128),
    created_at datetime(3) not null,
    key idx_bms_operation_business (business_no)
);
