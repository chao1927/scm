alter table iam_session modify access_token varchar(768) not null;
alter table iam_session modify refresh_token varchar(768) not null;

create table iam_permission_snapshot (
    user_id bigint not null,
    app_code varchar(64) not null,
    role_payload text not null,
    permission_payload text not null,
    data_scope_payload text not null,
    snapshot_status tinyint not null comment '1有效 2失效',
    version bigint not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    primary key(user_id, app_code)
) comment 'IAM权限快照缓存';

create table iam_outbox_event (
    event_id bigint not null primary key,
    event_type varchar(128) not null,
    business_no varchar(128) not null,
    payload text not null,
    event_status tinyint not null comment '1待投递 2已投递 3失败',
    occurred_at datetime(3) not null,
    created_at datetime(3) not null,
    key idx_iam_outbox_status(event_status, event_id)
) comment 'IAM出站事件';
