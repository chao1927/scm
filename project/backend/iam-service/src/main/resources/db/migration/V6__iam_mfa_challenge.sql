create table iam_mfa_challenge (
    challenge_id bigint not null primary key,
    challenge_no varchar(64) not null,
    user_id bigint not null,
    app_code varchar(64) not null,
    factor_type varchar(32) not null comment 'TOTP',
    secret_ciphertext varchar(1024) not null comment 'AES-GCM ciphertext; never plaintext',
    challenge_status varchar(32) not null comment 'PENDING/VERIFIED/LOCKED/EXPIRED',
    failed_attempts int not null,
    max_attempts int not null,
    expires_at datetime(3) not null,
    verified_at datetime(3) null,
    idempotency_key varchar(128) not null,
    version int not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_iam_mfa_challenge_no(challenge_no),
    unique key uk_iam_mfa_idempotency(idempotency_key),
    key idx_iam_mfa_user_status(user_id, challenge_status, expires_at)
) comment 'IAM MFA challenge and audit state';

create table iam_mfa_recovery_code (
    recovery_code_id bigint not null primary key,
    user_id bigint not null,
    code_hash varchar(128) not null comment 'SHA-256 hash; never plaintext',
    consumed_at datetime(3) null,
    consumed_challenge_no varchar(64) null,
    created_at datetime(3) not null,
    unique key uk_iam_mfa_recovery_hash(user_id, code_hash),
    key idx_iam_mfa_recovery_unused(user_id, consumed_at)
) comment 'One-time MFA recovery codes';
