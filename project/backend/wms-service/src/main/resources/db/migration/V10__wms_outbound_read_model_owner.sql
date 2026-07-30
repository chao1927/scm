-- 出库作业链以出库单的货主作为统一数据隔离维度。
-- 历史记录不做猜测性回填，仅 OWNER=* 管理员能够查看 owner_id 为空的遗留数据。
ALTER TABLE wms_outbound
    ADD COLUMN owner_id BIGINT NULL COMMENT '货主ID' AFTER warehouse_id,
    ADD KEY idx_wms_outbound_owner_warehouse_status
        (owner_id, warehouse_id, outbound_status, updated_at);
