alter table tms_domain_event
    add column event_code varchar(120) null after id,
    add column retry_count int not null default 0 after event_status,
    add column last_error varchar(1000) null after retry_count,
    add column next_retry_at datetime(3) null after last_error,
    add column published_at datetime(3) null after next_retry_at,
    add column updated_at datetime(3) null after created_at;

update tms_domain_event
   set event_code=concat('TMS-',id),updated_at=created_at
 where event_code is null;

alter table tms_domain_event
    add unique key uk_tms_domain_event_code (event_code),
    add key idx_tms_event_retry (event_status,next_retry_at,id);
