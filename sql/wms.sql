CREATE TABLE purchase_storage (
                                  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '采购入库单ID',
                                  storage_no VARCHAR(50) NOT NULL COMMENT '入库单号',
                                  start_storage_time DATETIME COMMENT '开始入库时间',
                                  finish_storage_time DATETIME COMMENT '完成入库时间',
                                  start_inspection_time DATETIME COMMENT '开始验收时间',
                                  finish_inspection_time DATETIME COMMENT '完成验收时间',
                                  start_shelving_time DATETIME COMMENT '开始上架时间',
                                  finish_shelving_time DATETIME COMMENT '完成上架时间',
                                  receipt_no VARCHAR(50) NOT NULL COMMENT '收货单号，对应 purchase_receipt.receipt_no',
                                  operator_emp_id BIGINT NOT NULL COMMENT '入库操作员ID',
                                  created_by BIGINT COMMENT '创建人（员工ID）',
                                  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  updated_by BIGINT COMMENT '修改人（员工ID）',
                                  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
                                  PRIMARY KEY (id),
                                  UNIQUE KEY uk_storage_no (storage_no),
                                  KEY idx_start_storage_time (start_storage_time),
                                  KEY idx_finish_storage_time (finish_storage_time),
                                  KEY idx_start_inspection_time (start_inspection_time),
                                  KEY idx_finish_inspection_time (finish_inspection_time),
                                  KEY idx_start_shelving_time (start_shelving_time),
                                  KEY idx_finish_shelving_time (finish_shelving_time),
                                  KEY idx_receipt_no (receipt_no),
                                  KEY idx_operator_emp_id (operator_emp_id)
) COMMENT = '采购入库单表';

CREATE TABLE purchase_storage_item (
                                       id BIGINT NOT NULL AUTO_INCREMENT COMMENT '入库详情ID',
                                       storage_no VARCHAR(50) NOT NULL COMMENT '入库单号，对应 purchase_storage.storage_no',
                                       batch_no VARCHAR(50) COMMENT '批次号',
                                       sku VARCHAR(50) NOT NULL COMMENT '商品SKU',
                                       unit_id INT NOT NULL COMMENT '计量单位ID',
                                       production_time DATETIME COMMENT '生产时间',
                                       expiration_time DATETIME COMMENT '保质期',
                                       quantity INT NOT NULL COMMENT '数量',
                                       storage_quantity INT COMMENT '入库数量',
                                       inspection_quantity INT COMMENT '验收数量',
                                       shelving_quantity INT COMMENT '上架数量',
                                       good_quantity INT COMMENT '良品数量',
                                       defective_quantity INT COMMENT '次品数量',
                                       unit_price DECIMAL(10,2) COMMENT '单价',
                                       total_price DECIMAL(10,2) COMMENT '总价',
                                       created_by BIGINT COMMENT '创建人（员工ID）',
                                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       updated_by BIGINT COMMENT '修改人（员工ID）',
                                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                       is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
                                       PRIMARY KEY (id),
                                       KEY idx_storage_no (storage_no),
                                       KEY idx_sku (sku),
                                       KEY idx_batch_no (batch_no)
) COMMENT = '采购入库单详情表';

CREATE TABLE sales_outbound_order (
                                      id BIGINT NOT NULL AUTO_INCREMENT COMMENT '出库单ID',
                                      outbound_no VARCHAR(50) NOT NULL COMMENT '出库单单号',
                                      delivery_no VARCHAR(50) NOT NULL COMMENT '发货单号',
                                      wave_no VARCHAR(50) COMMENT '波次单号',
                                      sorting_no VARCHAR(50) COMMENT '分拣单号',
                                      warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
                                      oms_order_no VARCHAR(50) NOT NULL COMMENT 'OMS订单号',
                                      expected_delivery_time DATETIME COMMENT '预计发货时间',
                                      delivery_time DATETIME COMMENT '发货时间',
                                      logistic_channel_id BIGINT COMMENT '物流渠道ID',
                                      logistic_cost DECIMAL(10,2) COMMENT '物流费用',
                                      remark TEXT COMMENT '备注',
                                      outbound_status INT NOT NULL COMMENT '出库单状态，如1-待出库,2-出库完成',
                                      created_by BIGINT COMMENT '创建人（员工ID）',
                                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      updated_by BIGINT COMMENT '修改人（员工ID）',
                                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                      is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
                                      PRIMARY KEY (id),
                                      UNIQUE KEY uk_outbound_no (outbound_no),
                                      KEY idx_delivery_no (delivery_no),
                                      KEY idx_oms_order_no (oms_order_no),
                                      KEY idx_outbound_status (outbound_status),
                                      KEY idx_warehouse_id (warehouse_id)
) COMMENT='销售出库单';



CREATE TABLE sales_outbound_order_item (
                                           id BIGINT NOT NULL AUTO_INCREMENT COMMENT '出库单明细ID',
                                           outbound_no VARCHAR(50) NOT NULL COMMENT '出库单单号，对应 sales_outbound_order.outbound_no',
                                           sku VARCHAR(50) NOT NULL COMMENT '商品SKU',
                                           product_name VARCHAR(100) COMMENT '商品名称',
                                           order_quantity INT NOT NULL COMMENT '订单商品数量',
                                           shipped_quantity INT COMMENT '发货数量',
                                           unit_id INT NOT NULL COMMENT '计量单位ID',  -- 新增计量单位字段
                                           batch_no VARCHAR(50) COMMENT '批次号',     -- 新增批次号字段
                                           location_id BIGINT COMMENT '库位ID',           -- 新增库位记录字段
                                           remark TEXT COMMENT '备注',
                                           created_by BIGINT COMMENT '创建人（员工ID）',
                                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           updated_by BIGINT COMMENT '修改人（员工ID）',
                                           updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                           is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
                                           PRIMARY KEY (id),
                                           KEY idx_outbound_no (outbound_no),
                                           KEY idx_sku (sku),
                                           KEY idx_batch_no (batch_no),
                                           KEY idx_location_id (location_id)
) COMMENT='销售出库单明细';


CREATE TABLE return_inbound_order (
                                      id BIGINT NOT NULL AUTO_INCREMENT COMMENT '退货入库单ID',
                                      inbound_no VARCHAR(50) NOT NULL COMMENT '退货入库单单号',
                                      order_no VARCHAR(50) NOT NULL COMMENT '关联原订单号（销售出库单对应的订单号）',
                                      sales_return_no VARCHAR(50) NOT NULL COMMENT '关联销售退货单号，对应 sales_return_order.sales_return_no',
                                      operator_id BIGINT NOT NULL COMMENT '验收操作人（员工ID）',
                                      refund_reason VARCHAR(200) COMMENT '退款原因',
                                      remark TEXT COMMENT '备注',
                                      logistics_no VARCHAR(50) COMMENT '退货物流单号',
                                      inbound_time DATETIME COMMENT '入库时间',
                                      shelving_time DATETIME COMMENT '上架时间',
                                      inbound_status TINYINT NOT NULL COMMENT '状态：1-等待物流送货, 2-收货中, 3-验收中, 4-验收完成, 5-上架中, 6-入库完成',
                                      created_by BIGINT COMMENT '创建人（员工ID）',
                                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      updated_by BIGINT COMMENT '修改人（员工ID）',
                                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                      is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
                                      PRIMARY KEY (id),
                                      UNIQUE KEY uk_inbound_no (inbound_no),
                                      KEY idx_order_no (order_no),
                                      KEY idx_sales_return_no (sales_return_no),
                                      KEY idx_inbound_time (inbound_time),
                                      KEY idx_inbound_status (inbound_status)
) COMMENT='退货入库单';


CREATE TABLE return_inbound_order_item (
                                           id BIGINT NOT NULL AUTO_INCREMENT COMMENT '退货入库单明细ID',
                                           inbound_no VARCHAR(50) NOT NULL COMMENT '退货入库单单号，对应 return_inbound_order.inbound_no',
                                           sku VARCHAR(50) NOT NULL COMMENT '商品SKU',
                                           quantity INT NOT NULL COMMENT '退货数量',
                                           unit_id INT NOT NULL COMMENT '计量单位ID',
                                           acceptance_result TINYINT NOT NULL COMMENT '验收结果：1-通过, 2-不通过',
                                           rejection_reason VARCHAR(200) COMMENT '不通过原因',
                                           batch_no VARCHAR(50) COMMENT '批次号',  -- 重命名后并加索引
                                           location_id BIGINT COMMENT '库位ID',       -- 关联库位
                                           created_by BIGINT COMMENT '创建人（员工ID）',
                                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           updated_by BIGINT COMMENT '修改人（员工ID）',
                                           updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                           is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
                                           PRIMARY KEY (id),
                                           KEY idx_inbound_no (inbound_no),
                                           KEY idx_sku (sku),
                                           KEY idx_batch_no (batch_no)
) COMMENT='退货入库单明细表';

CREATE TABLE warehouse_inventory (
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
                                     UNIQUE KEY uk_warehouse_sku (warehouse_id, sku),
                                     KEY idx_warehouse_id (warehouse_id),
                                     KEY idx_sku (sku)
) COMMENT = '仓库库存表';


CREATE TABLE location_inventory (
                                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
                                    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
                                    location_id BIGINT NOT NULL COMMENT '库位ID',
                                    sku VARCHAR(50) NOT NULL COMMENT '商品SKU',
                                    batch_no VARCHAR(50) COMMENT '批次号',
                                    unit_id INT NOT NULL COMMENT '计量单位ID',
                                    purchase_price DECIMAL(10,2) COMMENT '采购价',
                                    production_time DATETIME COMMENT '生产时间',
                                    expiration_time DATETIME COMMENT '保质期',
                                    available_quantity INT NOT NULL COMMENT '可用库存数量',
                                    locked_quantity INT COMMENT '锁定库存数量',
                                    storage_time DATETIME COMMENT '入库时间',
                                    shelving_time DATETIME COMMENT '上架时间',
                                    created_by BIGINT COMMENT '创建人（员工ID）',
                                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    updated_by BIGINT COMMENT '修改人（员工ID）',
                                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
                                    PRIMARY KEY (id),
                                    UNIQUE KEY uk_location (warehouse_id, location_id, sku),
                                    KEY idx_warehouse_id (warehouse_id),
                                    KEY idx_storage_time (storage_time),
                                    KEY idx_shelving_time (shelving_time),
                                    KEY idx_sku (sku),
                                    KEY idx_batch_no (batch_no)
) COMMENT = '库位库存表';


CREATE TABLE wave_order (
                            id BIGINT NOT NULL AUTO_INCREMENT COMMENT '波次单ID',
                            wave_no VARCHAR(50) NOT NULL COMMENT '波次单号',
                            warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
                            delivery_nos JSON COMMENT '包含的发货单号列表，建议使用 JSON 格式',
                            total_sku_count INT COMMENT '总SKU数量',
                            total_product_quantity INT COMMENT '总商品数量',
                            wave_status INT NOT NULL COMMENT '波次状态，如1-待分拣,2-拣货中,3-拣货完成,4-波次完成',
                            wave_priority INT COMMENT '波次优先级',
                            remark TEXT COMMENT '备注',
                            created_by BIGINT COMMENT '创建人（员工ID）',
                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            updated_by BIGINT COMMENT '修改人（员工ID）',
                            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                            is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_wave_no (wave_no),
                            KEY idx_warehouse_id (warehouse_id),
                            KEY idx_wave_status (wave_status)
) COMMENT='波次单';

CREATE TABLE sorting_order (
                               id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分拣单ID',
                               sorting_no VARCHAR(50) NOT NULL COMMENT '分拣单号',
                               wave_no VARCHAR(50) NOT NULL COMMENT '对应波次单号',
                               sorting_person BIGINT NOT NULL COMMENT '分拣人（员工ID）',
                               sorting_area VARCHAR(100) COMMENT '分拣区域',
                               sorting_vehicle_no VARCHAR(50) COMMENT '分拣车编号',
                               picking_start_time DATETIME COMMENT '拣货开始时间',
                               picking_complete_time DATETIME COMMENT '拣货完成时间',
                               sorting_status INT NOT NULL COMMENT '分拣状态，如1-待拣货,2-拣货中,3-拣货完成,4-异常',
                               suggested_route TEXT COMMENT '建议行走路线图',
                               remark TEXT COMMENT '备注',
                               created_by BIGINT COMMENT '创建人（员工ID）',
                               created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               updated_by BIGINT COMMENT '修改人（员工ID）',
                               updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                               is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
                               PRIMARY KEY (id),
                               UNIQUE KEY uk_sorting_no (sorting_no),
                               KEY idx_wave_no (wave_no),
                               KEY idx_sorting_status (sorting_status)
) COMMENT='分拣单';

CREATE TABLE sorting_order_item (
                                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分拣单明细ID',
                                    sorting_no VARCHAR(50) NOT NULL COMMENT '分拣单号，对应 sorting_order.sorting_no',
                                    sku VARCHAR(50) NOT NULL COMMENT '商品SKU',
                                    product_name VARCHAR(100) COMMENT '商品名称',
                                    order_quantity INT NOT NULL COMMENT '订单商品数量',
                                    picked_quantity INT COMMENT '已拣数量',
                                    pending_quantity INT COMMENT '待拣数量',
                                    unit_id INT NOT NULL COMMENT '计量单位ID',  -- 新增计量单位字段
                                    batch_number VARCHAR(50) COMMENT '批次号',   -- 新增批次号字段
                                    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
                                    location_id BIGINT NOT NULL COMMENT '库位ID',
                                    picking_time DATETIME COMMENT '拣货时间',
                                    remark TEXT COMMENT '备注',
                                    created_by BIGINT COMMENT '创建人（员工ID）',
                                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    updated_by BIGINT COMMENT '修改人（员工ID）',
                                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
                                    PRIMARY KEY (id),
                                    KEY idx_sorting_no (sorting_no),
                                    KEY idx_sku (sku)
) COMMENT='分拣单明细';

CREATE TABLE warehouse_inventory_flow (
                                          id BIGINT NOT NULL AUTO_INCREMENT COMMENT '流水ID',
                                          warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
                                          sku VARCHAR(50) NOT NULL COMMENT '商品SKU',
                                          flow_type INT NOT NULL COMMENT '库存变化类型：1-采购入库，2-退货入库，3-销售出库，4-退供出库',
                                          flow_time DATETIME NOT NULL COMMENT '流水时间',
                                          quantity_change INT NOT NULL COMMENT '变化数量',
                                          related_order_id VARCHAR(50) COMMENT '关联单号',
                                          created_by BIGINT COMMENT '创建人（员工ID）',
                                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          updated_by BIGINT COMMENT '修改人（员工ID）',
                                          updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                          is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
                                          PRIMARY KEY (id),
                                          KEY idx_flow_time (flow_time),
                                          KEY idx_warehouse_id (warehouse_id),
                                          KEY idx_flow_type (flow_type),
                                          KEY idx_sku (sku)
) COMMENT = '仓库库存流水表';

CREATE TABLE location_inventory_flow (
                                         id BIGINT NOT NULL AUTO_INCREMENT COMMENT '流水ID',
                                         warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
                                         location_id BIGINT NOT NULL COMMENT '库位ID',
                                         sku VARCHAR(50) NOT NULL COMMENT '商品SKU',
                                         flow_type INT NOT NULL COMMENT '库存变化类型：1-采购入库，2-退货入库，3-销售出库，4-退供出库',
                                         quantity_change INT NOT NULL COMMENT '变化数量',
                                         flow_time DATETIME NOT NULL COMMENT '流水时间',
                                         related_order_id VARCHAR(50) COMMENT '关联单号',
                                         created_by BIGINT COMMENT '创建人（员工ID）',
                                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         updated_by BIGINT COMMENT '修改人（员工ID）',
                                         updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                         is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
                                         PRIMARY KEY (id),
                                         KEY idx_flow_time (flow_time),
                                         KEY idx_warehouse_id (warehouse_id),
                                         KEY idx_flow_type (flow_type),
                                         KEY idx_sku (sku)
) COMMENT = '库位库存流水表';

CREATE TABLE return_supply_delivery_order (
                                              id BIGINT NOT NULL AUTO_INCREMENT COMMENT '退供发货单ID',
                                              delivery_no VARCHAR(50) NOT NULL COMMENT '退供发货单单号',
                                              order_no VARCHAR(50) NOT NULL COMMENT '关联退供单单号（return_supply_order.order_no）',
                                              supplier_id INT NOT NULL COMMENT '供应商ID',
                                              warehouse_id INT NOT NULL COMMENT '退供发货仓库ID',
                                              expected_delivery_time DATETIME COMMENT '预计发货时间',
                                              delivery_time DATETIME COMMENT '实际发货时间',
                                              logistic_channel_id INT COMMENT '物流渠道ID',
                                              logistic_no VARCHAR(50) COMMENT '物流单号',
                                              logistic_cost DECIMAL(10,2) COMMENT '物流费用',
                                              status TINYINT NOT NULL COMMENT '退供发货单状态：如1-待发货，2-运输中，3-已发货',
                                              remark TEXT COMMENT '备注',

                                              created_by INT COMMENT '创建人（关联 employee.id）',
                                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                              updated_by INT COMMENT '修改人（关联 employee.id）',
                                              updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                              is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
                                              PRIMARY KEY (id),
                                              UNIQUE KEY uk_delivery_no (delivery_no),
                                              KEY idx_order_no (order_no),
                                              KEY idx_expected_delivery_time (expected_delivery_time),
                                              KEY idx_delivery_time (delivery_time),
                                              KEY idx_status (status),
                                              KEY idx_logistic_no (logistic_no)
) COMMENT = '退供发货单主表';

CREATE TABLE return_supply_delivery_order_item (
                                                   id BIGINT NOT NULL AUTO_INCREMENT COMMENT '退供发货单明细ID',
                                                   delivery_no VARCHAR(50) NOT NULL COMMENT '退供发货单单号，对应 return_supply_delivery_order.delivery_no',
                                                   sku VARCHAR(50) NOT NULL COMMENT '商品SKU',
                                                   product_name VARCHAR(100) COMMENT '商品名称',
                                                   supplier_id INT NOT NULL COMMENT '供应商ID',
                                                   warehouse_id INT NOT NULL COMMENT '仓库ID',
                                                   batch_no VARCHAR(50) COMMENT '批次号',
                                                   purchase_order_no VARCHAR(50) COMMENT '采购单单号（自动关联）',
                                                   quantity INT NOT NULL COMMENT '发货数量',
                                                   unit_id INT NOT NULL COMMENT '计量单位ID',
                                                   remark TEXT COMMENT '备注',

                                                   created_by INT COMMENT '创建人（关联 employee.id）',
                                                   created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                   updated_by INT COMMENT '修改人（关联 employee.id）',
                                                   updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                                   is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
                                                   PRIMARY KEY (id),
                                                   KEY idx_delivery_no (delivery_no),
                                                   KEY idx_sku (sku),
                                                   KEY idx_batch_no (batch_no)
)
    COMMENT = '退供发货单明细表';