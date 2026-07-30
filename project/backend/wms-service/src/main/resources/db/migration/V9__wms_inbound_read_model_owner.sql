-- 入库工作台按仓库和货主双维度隔离数据。
-- 历史数据无法可靠推断货主，因此保留 NULL；仅拥有 OWNER=* 的管理员可查看这些遗留记录。
ALTER TABLE wms_inbound
    ADD COLUMN owner_id BIGINT NULL COMMENT '货主ID' AFTER warehouse_id,
    ADD KEY idx_wms_inbound_owner_warehouse_status
        (owner_id, warehouse_id, inbound_status, updated_at);
