create table iam_mfa_configuration (
    config_id bigint not null primary key,
    user_id bigint not null,
    secret_ciphertext varchar(1024) not null,
    config_status tinyint not null comment '0 pending 1 active 2 disabled',
    version int not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_iam_mfa_configuration_user(user_id)
) comment 'encrypted user MFA configuration';

alter table iam_mfa_challenge
    add column session_id bigint not null after app_code,
    add column purpose varchar(64) not null after session_id,
    add column device_digest varchar(128) not null after purpose;

create table iam_mfa_audit (
    audit_id bigint not null primary key,
    user_id bigint not null,
    action varchar(64) not null,
    challenge_no varchar(64),
    operator_id bigint,
    reason varchar(512) not null,
    occurred_at datetime(3) not null,
    key idx_iam_mfa_audit_user(user_id, occurred_at),
    key idx_iam_mfa_audit_challenge(challenge_no)
) comment 'append-only MFA security audit';
