alter table bms_domain_event
    add column retry_count int not null default 0 after status,
    add column last_error varchar(1000) null after retry_count,
    add column next_retry_at datetime(3) null after last_error,
    add column published_at datetime(3) null after next_retry_at,
    add column updated_at datetime(3) null after created_at,
    add key idx_bms_event_retry (status,next_retry_at,id);

update bms_domain_event set updated_at=created_at where updated_at is null;
