alter table mdm_outbox_event
    add column retry_count int not null default 0 after event_status,
    add column last_error varchar(1000) null after retry_count,
    add column next_retry_at datetime null after last_error,
    add column published_at datetime null after next_retry_at,
    add column updated_at datetime null after created_at,
    add key idx_mdm_outbox_retry (event_status, next_retry_at, id);

update mdm_outbox_event set updated_at=created_at where updated_at is null;
