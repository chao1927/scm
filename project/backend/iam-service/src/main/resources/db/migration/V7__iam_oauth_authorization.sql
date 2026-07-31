create table iam_oauth_client (
    client_id varchar(128) not null primary key,
    app_code varchar(64) not null,
    client_type varchar(32) not null comment 'PUBLIC or CONFIDENTIAL',
    secret_hash varchar(128),
    redirect_uris text not null comment 'exact-match URI values separated by whitespace',
    grant_types varchar(256) not null,
    allowed_scopes text not null,
    access_ttl_seconds int not null,
    id_token_ttl_seconds int not null,
    client_status tinyint not null comment '1 enabled 2 disabled',
    version int not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    key idx_iam_oauth_client_app(app_code, client_status)
) comment 'OAuth registered client';

create table iam_oauth_authorization_code (
    code_hash varchar(64) not null primary key,
    client_id varchar(128) not null,
    user_id bigint not null,
    redirect_uri varchar(1024) not null,
    scopes text not null,
    code_challenge varchar(128) not null,
    nonce varchar(256),
    request_id varchar(128) not null,
    issued_at datetime(3) not null,
    expires_at datetime(3) not null,
    consumed_at datetime(3),
    created_at datetime(3) not null,
    unique key uk_iam_oauth_code_request(request_id),
    key idx_iam_oauth_code_expiry(client_id, expires_at, consumed_at)
) comment 'hashed one-time OAuth authorization code';

create table iam_oauth_audit (
    audit_id bigint not null primary key,
    action varchar(128) not null,
    client_id varchar(128) not null,
    user_id bigint,
    request_id varchar(128) not null,
    detail varchar(1024) not null,
    occurred_at datetime(3) not null,
    key idx_iam_oauth_audit_request(request_id, occurred_at),
    key idx_iam_oauth_audit_client(client_id, occurred_at)
) comment 'OAuth security audit trail';

create table iam_oauth_grant (
    grant_id varchar(64) not null primary key,
    client_id varchar(128) not null,
    user_id bigint not null,
    scopes text not null,
    grant_status tinyint not null comment '1 active 2 revoked',
    revoked_at datetime(3),
    created_at datetime(3) not null,
    key idx_iam_oauth_grant_subject(client_id, user_id, grant_status)
) comment 'OAuth durable authorization chain';

create table iam_oauth_refresh_token (
    token_hash varchar(64) not null primary key,
    grant_id varchar(64) not null,
    generation int not null,
    expires_at datetime(3) not null,
    consumed_at datetime(3),
    created_at datetime(3) not null,
    key idx_iam_oauth_refresh_grant(grant_id, generation),
    key idx_iam_oauth_refresh_expiry(expires_at, consumed_at)
) comment 'hashed rotating OAuth refresh token';
