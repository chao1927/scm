-- 员工主表
CREATE TABLE employee (
                          id INT AUTO_INCREMENT COMMENT '员工ID（系统生成）',
                          employee_no VARCHAR(20) NOT NULL COMMENT '员工编号（企业规则生成，如EMP20240001）',
                          name VARCHAR(50) NOT NULL COMMENT '姓名',
                          gender TINYINT COMMENT '性别：0（未知）/1（男）/2（女）',
                          id_card VARCHAR(20) NOT NULL COMMENT '身份证号（需加密存储）',
                          mobile VARCHAR(20) COMMENT '手机号',
                          email VARCHAR(100) COMMENT '企业邮箱',
                          status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1（在职）/2（离职）/3（停薪留职）',
                          created_by INT COMMENT '创建人ID（关联employee.id）',
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          updated_by INT COMMENT '修改人ID（关联employee.id）',
                          updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                          is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0（未删除）/1（已删除）',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY uk_employee_no (employee_no),
                          UNIQUE KEY uk_id_card (id_card)
) COMMENT='员工主表';


-- 员工职位表
CREATE TABLE employee_job (
                              id INT AUTO_INCREMENT COMMENT '员工职位表ID（系统生成）',
                              employee_id INT NOT NULL COMMENT '员工ID（关联employee.id）',
                              position_id INT NOT NULL COMMENT '职位ID（关联position.id）',
                              job_level VARCHAR(20) COMMENT '职级（如P6/M2）',
                              entry_date DATE NOT NULL COMMENT '入职日期',
                              probation_end_date DATE COMMENT '试用期截止日期',
                              regular_date DATE COMMENT '转正日期',
                              created_by INT COMMENT '创建人ID（关联employee.id）',
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              updated_by INT COMMENT '修改人ID（关联employee.id）',
                              updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                              is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0（未删除）/1（已删除）',
                              PRIMARY KEY (`id`)
) COMMENT='员工工作信息表';


-- 部门表（支持树形结构）
CREATE TABLE department (
                            id INT AUTO_INCREMENT COMMENT '部门ID',
                            name VARCHAR(50) NOT NULL COMMENT '部门名称',
                            code VARCHAR(20) NOT NULL COMMENT '部门编码（唯一，如：DEPT-001）',
                            parent_id INT COMMENT '上级部门ID（关联department.id，根部门为NULL）',
                            manager_employee_id INT COMMENT '部门负责人ID（关联employee.id）',
                            sort_order INT DEFAULT 0 COMMENT '排序序号',
                            status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1（启用）/2（停用）',
                            created_by INT COMMENT '创建人ID（关联employee.id）',
                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            updated_by INT COMMENT '修改人ID（关联employee.id）',
                            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                            is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0（未删除）/1（已删除）',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY uk_code (code),
                            INDEX idx_parent (parent_id)
) COMMENT='部门表';


-- 职位表（支持职级体系）
CREATE TABLE position (
                          id INT AUTO_INCREMENT COMMENT '职位ID',
                          name VARCHAR(50) NOT NULL COMMENT '职位名称',
                          code VARCHAR(20) NOT NULL COMMENT '职位编码（唯一，如：POS-ENG-001）',
                          department_id INT NOT NULL COMMENT '所属部门ID（关联department.id）',
                          grade_type TINYINT NOT NULL COMMENT '职级类型：1（管理序列）/2（技术序列）/3（专业序列）',
                          grade_level VARCHAR(10) COMMENT '职级（如：M2/P3，根据类型动态约束）',
                          description TEXT COMMENT '职位描述',
                          status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1（启用）/2（停用）',
                          created_by INT COMMENT '创建人ID（关联employee.id）',
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          updated_by INT COMMENT '修改人ID（关联employee.id）',
                          updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                          is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0（未删除）/1（已删除）',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY uk_department_name (department_id, name),
                          UNIQUE KEY uk_code (code)
) COMMENT='职位表';

CREATE TABLE `product_category` (
                                    `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
                                    `name` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
                                    `parent_id` int DEFAULT NULL COMMENT '父分类ID',
                                    `attributes` json DEFAULT NULL COMMENT '分类属性（JSON格式）',
                                    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1（启用）/2（停用）',
                                    `sort_order` int DEFAULT NULL COMMENT '排序序号',
                                    `created_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建用户',
                                    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updated_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改用户',
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
                           `created_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建用户',
                           `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改用户',
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
                                     `created_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建用户',
                                     `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `updated_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改用户',
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
                            `created_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建用户',
                            `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updated_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改用户',
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
                             `created_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建用户',
                             `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updated_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改用户',
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
                                    `created_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建用户',
                                    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updated_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改用户',
                                    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0（未删除）/1（已删除）',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_code` (`code`)
) COMMENT='库位表';

CREATE TABLE `unit_of_measure` (
                                   `id` int NOT NULL AUTO_INCREMENT COMMENT '单位ID',
                                   `name` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '单位名称',
                                   `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1（启用）/2（停用）',
                                   `created_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建用户',
                                   `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updated_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改用户',
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
                           `created_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建用户',
                           `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改用户',
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
                                     `created_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建用户',
                                     `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `updated_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改用户',
                                     `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                     `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0（未删除）/1（已删除）',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_name` (`name`)
) COMMENT='物流渠道表';
