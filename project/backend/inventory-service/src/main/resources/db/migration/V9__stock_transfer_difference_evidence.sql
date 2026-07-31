alter table inv_stock_transfer
    add column difference_reason varchar(500) null after difference_qty,
    add column responsible_party varchar(64) null after difference_reason,
    add column evidence_ref varchar(255) null after responsible_party;
