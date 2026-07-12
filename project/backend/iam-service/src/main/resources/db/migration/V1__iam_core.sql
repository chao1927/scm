create table iam_user (
    user_id bigint not null primary key,
    username varchar(64) not null,
    password_hash varchar(128) not null,
    user_status tinyint not null comment '1启用 2停用 3锁定',
    failed_attempts int not null,
    version int not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_iam_user_username(username)
) comment 'IAM用户';

create table iam_session (
    session_id bigint not null primary key,
    user_id bigint not null,
    access_token varchar(128) not null,
    refresh_token varchar(128) not null,
    session_status tinyint not null comment '1有效 2失效',
    version int not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_iam_session_access(access_token),
    unique key uk_iam_session_refresh(refresh_token)
) comment 'IAM会话';

create table iam_role (
    role_id bigint not null primary key,
    role_code varchar(64) not null,
    role_name varchar(128) not null,
    role_status tinyint not null,
    version int not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_iam_role_code(role_code)
) comment 'IAM角色';

create table iam_user_role (
    user_id bigint not null,
    role_id bigint not null,
    created_at datetime(3) not null,
    unique key uk_iam_user_role(user_id, role_id)
) comment 'IAM用户角色';

create table iam_permission (
    permission_id bigint not null primary key,
    app_code varchar(64) not null,
    permission_code varchar(128) not null,
    permission_name varchar(128) not null,
    created_at datetime(3) not null,
    unique key uk_iam_permission_code(permission_code)
) comment 'IAM权限点';

create table iam_role_permission (
    role_id bigint not null,
    permission_code varchar(128) not null,
    created_at datetime(3) not null,
    unique key uk_iam_role_permission(role_id, permission_code)
) comment 'IAM角色权限';

create table iam_data_scope (
    scope_id bigint not null primary key,
    role_id bigint not null,
    scope_type varchar(32) not null,
    scope_value varchar(128) not null,
    created_at datetime(3) not null,
    key idx_iam_data_scope_role(role_id)
) comment 'IAM数据权限';

create table iam_approval (
    approval_id bigint not null primary key,
    approval_no varchar(64) not null,
    business_type varchar(64) not null,
    business_no varchar(64) not null,
    approval_status tinyint not null comment '1待审批 2已通过 3已驳回',
    version int not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_iam_approval_no(approval_no)
) comment 'IAM审批实例';

create table iam_operation_log (
    log_id bigint not null primary key,
    operation varchar(128) not null,
    target_no varchar(128) not null,
    created_at datetime(3) not null,
    key idx_iam_operation_target(target_no, created_at)
) comment 'IAM操作日志';

create table iam_security_policy (
    policy_id bigint not null primary key,
    policy_code varchar(64) not null,
    policy_value varchar(512) not null,
    version int not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_iam_policy_code(policy_code)
) comment 'IAM安全策略';
