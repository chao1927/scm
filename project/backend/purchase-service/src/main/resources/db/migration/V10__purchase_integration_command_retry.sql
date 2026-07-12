alter table purchase_integration_command add column next_retry_at datetime(3) null after retry_count,
    add column remote_reference varchar(128) null after next_retry_at,
    add column completed_at datetime(3) null after remote_reference;
