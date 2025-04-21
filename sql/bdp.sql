-- 用户表
CREATE TABLE user (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      username VARCHAR(50) NOT NULL UNIQUE,
                      password VARCHAR(100) NOT NULL,
                      nickname VARCHAR(50),
                      email VARCHAR(100),
                      phone VARCHAR(20),
                      status TINYINT DEFAULT 1 COMMENT '状态：1启用，0禁用',
                      created_by INT,
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                      updated_by INT,
                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      is_deleted TINYINT DEFAULT 0
) COMMENT='用户表';


CREATE TABLE role (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      name VARCHAR(50) NOT NULL,
                      code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
                      remark VARCHAR(255),
                      created_by INT,
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                      updated_by INT,
                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      is_deleted TINYINT DEFAULT 0
) COMMENT='角色表';

CREATE TABLE permission (
            id BIGINT PRIMARY KEY AUTO_INCREMENT,
            name VARCHAR(100) NOT NULL,
            code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
            path VARCHAR(255) NOT NULL COMMENT '接口路径',
            method VARCHAR(10) NOT NULL COMMENT '请求方式，如GET、POST',
            description VARCHAR(255),
            parent_id BIGINT DEFAULT NULL COMMENT '父权限ID，用于组织结构',
            created_by INT,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            updated_by INT,
            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            is_deleted TINYINT DEFAULT 0
) COMMENT='权限表';

CREATE TABLE user_role (
       `id` bigint NOT NULL,
       user_id BIGINT,
       role_id BIGINT,
       `created_by` INT,
       `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
       `updated_by` INT,
       `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
       `is_deleted` TINYINT DEFAULT 0,
       PRIMARY KEY (`id`) USING BTREE
) COMMENT = '用户角色表';


CREATE TABLE role_permission (
         `id` bigint NOT NULL,
         `role_id` bigint NOT NULL,
         `permission_id` bigint NOT NULL,
         `created_by` INT,
         `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
         `updated_by` INT,
         `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
         `is_deleted` TINYINT DEFAULT 0,
         PRIMARY KEY (`id`) USING BTREE
) COMMENT = '角色权限表';


CREATE TABLE `product_category` (
                                    `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
                                    `name` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
                                    `parent_id` int DEFAULT NULL COMMENT '父分类ID',
                                    `attributes` json DEFAULT NULL COMMENT '分类属性（JSON格式）',
                                    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1（启用）/2（停用）',
                                    `sort_order` int DEFAULT NULL COMMENT '排序序号',
                                    `created_by` INT DEFAULT NULL COMMENT '创建用户',
                                    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updated_by` INT DEFAULT NULL COMMENT '修改用户',
                                    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0（未删除）/1（已删除）',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_name` (`name`)
) COMMENT='商品分类表';

CREATE TABLE `product` (
                           `id` int NOT NULL AUTO_INCREMENT COMMENT '商品ID',
                           `sku` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'SKU',
                           `spu` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'SPU',
                           `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
                           `description` text COLLATE utf8mb4_general_ci COMMENT '商品描述',
                           `category_id` int DEFAULT NULL COMMENT '分类ID',
                           `unit_id` int DEFAULT NULL COMMENT '单位ID',
                           `key_attributes` json DEFAULT NULL COMMENT '关键属性（JSON格式）',
                           `sales_attributes` json DEFAULT NULL COMMENT '销售属性（JSON格式）',
                           `reference_purchase_price` decimal(10,2) DEFAULT NULL COMMENT '参考采购价',
                           `reference_sales_price` decimal(10,2) DEFAULT NULL COMMENT '参考销售价',
                           `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1（启用）/2（停用）',
                           `created_by` INT DEFAULT NULL COMMENT '创建用户',
                           `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_by` INT DEFAULT NULL COMMENT '修改用户',
                           `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                           `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0（未删除）/1（已删除）',
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `uk_sku` (`sku`),
                           UNIQUE KEY `uk_name` (`name`)
) COMMENT='商品表';

CREATE TABLE `supplier_category` (
                                     `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
                                     `name` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
                                     `description` text COLLATE utf8mb4_general_ci COMMENT '描述',
                                     `created_by` INT DEFAULT NULL COMMENT '创建用户',
                                     `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `updated_by` INT DEFAULT NULL COMMENT '修改用户',
                                     `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                     `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0（未删除）/1（已删除）',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_name` (`name`)
) COMMENT='供应商分类表';

CREATE TABLE `supplier` (
                            `id` int NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
                            `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '供应商名称',
                            `category_id` int DEFAULT NULL COMMENT '供应商分类ID',
                            `contact_person` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系人',
                            `contact_phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系电话',
                            `address` text COLLATE utf8mb4_general_ci COMMENT '地址',
                            `business_license_number` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '营业执照编号',
                            `business_license_photo` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '营业执照照片',
                            `organization_code` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '统一组织代码',
                            `performance_score` decimal(3,2) DEFAULT NULL COMMENT '绩效评分',
                            `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1（启用）/2（停用）',
                            `created_by` INT DEFAULT NULL COMMENT '创建用户',
                            `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updated_by` INT DEFAULT NULL COMMENT '修改用户',
                            `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                            `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0（未删除）/1（已删除）',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_name` (`name`)
) COMMENT='供应商表';

CREATE TABLE `warehouse` (
                             `id` int NOT NULL AUTO_INCREMENT COMMENT '仓库ID',
                             `name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '仓库名称',
                             `description` text COLLATE utf8mb4_general_ci COMMENT '参考描述',
                             `address` text COLLATE utf8mb4_general_ci COMMENT '地址',
                             `type` tinyint NOT NULL COMMENT '仓库类型：1（国内仓）/2（海外仓）/3（冷藏仓）/4（保税仓）',
                             `area` decimal(10,2) DEFAULT NULL COMMENT '仓库面积',
                             `manager` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '仓库管理员',
                             `manager_phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '仓库管理员联系电话',
                             `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1（启用）/2（停用）',
                             `created_by` INT DEFAULT NULL COMMENT '创建用户',
                             `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updated_by` INT DEFAULT NULL COMMENT '修改用户',
                             `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                             `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0（未删除）/1（已删除）',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_name` (`name`)
) COMMENT='仓库表';

CREATE TABLE `storage_location` (
                                    `id` int NOT NULL AUTO_INCREMENT COMMENT '库位ID',
                                    `warehouse_id` int DEFAULT NULL COMMENT '仓库ID',
                                    `code` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '库位编码',
                                    `max_volume` decimal(10,2) DEFAULT NULL COMMENT '最大容积（立方米）',
                                    `max_weight` decimal(10,2) DEFAULT NULL COMMENT '最大承重（千克）',
                                    `mixing_strategy` tinyint NOT NULL COMMENT '混放策略：1（允许批次混放）/2（允许SKU混放）/3（禁止混放）',
                                    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1（启用）/2（停用）',
                                    `created_by` INT DEFAULT NULL COMMENT '创建用户',
                                    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updated_by` INT DEFAULT NULL COMMENT '修改用户',
                                    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0（未删除）/1（已删除）',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_code` (`code`)
) COMMENT='库位表';

CREATE TABLE `unit_of_measure` (
                                   `id` int NOT NULL AUTO_INCREMENT COMMENT '单位ID',
                                   `name` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '单位名称',
                                   `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1（启用）/2（停用）',
                                   `created_by` INT DEFAULT NULL COMMENT '创建用户',
                                   `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updated_by` INT DEFAULT NULL COMMENT '修改用户',
                                   `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0（未删除）/1（已删除）',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_name` (`name`)
) COMMENT='计量单位表';

CREATE TABLE `address` (
                           `id` int NOT NULL AUTO_INCREMENT COMMENT '地址ID',
                           `province` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '省份',
                           `city` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '城市',
                           `district` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '区县',
                           `detailed_address` text COLLATE utf8mb4_general_ci COMMENT '详细地址',
                           `zip_code` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮编',
                           `longitude` decimal(20,8) DEFAULT NULL COMMENT '经度',
                           `latitude` decimal(20,8) DEFAULT NULL COMMENT '纬度',
                           `created_by` INT DEFAULT NULL COMMENT '创建用户',
                           `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_by` INT DEFAULT NULL COMMENT '修改用户',
                           `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                           `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0（未删除）/1（已删除）',
                           PRIMARY KEY (`id`)
) COMMENT='地址表';

CREATE TABLE `logistics_channel` (
                                     `id` int NOT NULL AUTO_INCREMENT COMMENT '渠道ID',
                                     `name` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '渠道名称',
                                     `service_type` tinyint NOT NULL COMMENT '服务类型：1（快递）/2（快运）/3（整车）',
                                     `coverage_area` json DEFAULT NULL COMMENT '覆盖区域（JSON格式）',
                                     `freight_calculation_rules` json DEFAULT NULL COMMENT '运费计算规则（JSON格式）',
                                     `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1（启用）/2（停用）',
                                     `created_by` INT DEFAULT NULL COMMENT '创建用户',
                                     `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `updated_by` INT DEFAULT NULL COMMENT '修改用户',
                                     `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                     `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0（未删除）/1（已删除）',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_name` (`name`)
) COMMENT='物流渠道表';
