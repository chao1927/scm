alter table oms_sales_order
  add column organization_id bigint null after id,
  add column owner_id bigint null after organization_id,
  add key idx_oms_order_scope (organization_id, owner_id, updated_at);

alter table oms_outbox_event
  add column event_code varchar(128) null after id,
  add column retry_count int not null default 0 after event_status,
  add column last_error varchar(512) null after retry_count,
  add column last_attempt_at datetime null after last_error,
  add column published_at datetime null after occurred_at,
  add unique key uk_oms_outbox_event_code (event_code);

update oms_outbox_event
set event_code = concat('OMS-', id)
where event_code is null;

alter table oms_outbox_event
  modify column event_code varchar(128) not null;
