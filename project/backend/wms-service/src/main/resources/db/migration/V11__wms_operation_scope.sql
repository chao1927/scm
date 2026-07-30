-- 盘点和异常作业必须具备仓库/货主双维度，才能安全暴露列表、详情和处置入口。
ALTER TABLE wms_stocktake
    ADD COLUMN owner_id BIGINT NULL COMMENT '货主ID' AFTER warehouse_id,
    ADD KEY idx_wms_stocktake_scope_status
        (owner_id, warehouse_id, stocktake_status, updated_at);

ALTER TABLE wms_warehouse_exception
    ADD COLUMN warehouse_id BIGINT NULL COMMENT '仓库ID' AFTER exception_no,
    ADD COLUMN owner_id BIGINT NULL COMMENT '货主ID' AFTER warehouse_id,
    ADD KEY idx_wms_exception_scope_status
        (owner_id, warehouse_id, exception_status, updated_at);
