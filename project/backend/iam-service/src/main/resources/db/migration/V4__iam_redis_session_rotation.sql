alter table iam_session
    add column access_jti varchar(128) null after refresh_token,
    add column refresh_jti varchar(128) null after access_jti,
    add column refresh_generation bigint not null default 0 after refresh_jti,
    add column access_expires_at datetime(3) null after refresh_generation,
    add column refresh_expires_at datetime(3) null after access_expires_at,
    add column revoked_at datetime(3) null after refresh_expires_at,
    add column revocation_reason varchar(256) null after revoked_at;

update iam_session
set access_jti = concat('LEGACY-AT-', session_id),
    refresh_jti = concat('LEGACY-RT-', session_id),
    access_expires_at = date_add(updated_at, interval 1 hour),
    refresh_expires_at = date_add(updated_at, interval 1 day)
where access_jti is null;

alter table iam_session
    modify access_jti varchar(128) not null,
    modify refresh_jti varchar(128) not null,
    modify access_expires_at datetime(3) not null,
    modify refresh_expires_at datetime(3) not null,
    add unique key uk_iam_session_access_jti(access_jti),
    add unique key uk_iam_session_refresh_jti(refresh_jti),
    add key idx_iam_session_online(user_id, session_status, refresh_expires_at);
