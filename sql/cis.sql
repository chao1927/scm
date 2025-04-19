CREATE TABLE central_inventory (
                                   id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
                                   sku VARCHAR(50) NOT NULL COMMENT '商品SKU',
                                   total_quantity INT NOT NULL COMMENT '总库存数量',
                                   available_quantity INT NOT NULL COMMENT '可用库存数量',
                                   in_transit_quantity INT COMMENT '在途库存数量',
                                   locked_quantity INT COMMENT '锁定库存数量',
                                   frozen_quantity INT COMMENT '冻结库存数量',
                                   created_by BIGINT COMMENT '创建人（员工ID）',
                                   created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   updated_by BIGINT COMMENT '修改人（员工ID）',
                                   updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
                                   PRIMARY KEY (id),
                                   UNIQUE KEY uk_central_sku (sku),
                                   KEY idx_sku (sku)
) COMMENT = '中央库存表';

CREATE TABLE central_warehouse_inventory (
                                             id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
                                             warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
                                             sku VARCHAR(50) NOT NULL COMMENT '商品SKU',
                                             total_quantity INT NOT NULL COMMENT '仓库总库存数量',
                                             available_quantity INT NOT NULL COMMENT '仓库可用库存数量',
                                             in_transit_quantity INT COMMENT '在途库存数量',
                                             locked_quantity INT COMMENT '仓库锁定库存数量',
                                             frozen_quantity INT COMMENT '仓库冻结库存数量',
                                             created_by BIGINT COMMENT '创建人（员工ID）',
                                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             updated_by BIGINT COMMENT '修改人（员工ID）',
                                             updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                             is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
                                             PRIMARY KEY (id),
                                             UNIQUE KEY uk_warehouse_inventory (warehouse_id, sku),
                                             KEY idx_warehouse_id (warehouse_id),
                                             KEY idx_sku (sku)
) COMMENT = '仓库库存表';

