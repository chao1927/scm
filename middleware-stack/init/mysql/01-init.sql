CREATE DATABASE IF NOT EXISTS scm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'scm'@'%' IDENTIFIED BY 'Scm@123456';
GRANT ALL PRIVILEGES ON scm.* TO 'scm'@'%';

-- 如果后续希望 Nacos 使用 MySQL 持久化，可以单独创建 nacos_config，并导入 Nacos 官方 mysql-schema.sql。
CREATE DATABASE IF NOT EXISTS nacos_config DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

FLUSH PRIVILEGES;
