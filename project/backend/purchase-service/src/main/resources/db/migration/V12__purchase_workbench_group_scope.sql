create table purchase_group_member_scope (
    scope_id bigint not null auto_increment primary key,
    purchase_group_id bigint not null,
    purchase_org_id bigint not null,
    member_id bigint not null comment '采购事实的负责人标识，当前对应各事实表created_by或申请人',
    status tinyint not null default 1 comment '1生效 2失效',
    effective_from date null,
    effective_to date null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_purchase_group_member (purchase_group_id, purchase_org_id, member_id),
    key idx_purchase_group_member_effective (
        purchase_group_id, status, effective_from, effective_to
    )
) comment '采购工作台采购组成员数据范围投影';
