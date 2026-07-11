ALTER TABLE sup_order ADD COLUMN source_version INT NOT NULL DEFAULT 1 AFTER remark;
CREATE INDEX idx_sup_order_source_version ON sup_order(purchase_order_id,source_version);
