#!/bin/bash
set -Eeuo pipefail

for required_name in MYSQL_ROOT_PASSWORD MYSQL_APP_USER MYSQL_APP_PASSWORD MYSQL_NACOS_USER MYSQL_NACOS_PASSWORD; do
  if [[ -z "${!required_name:-}" ]]; then
    echo "缺少环境变量: ${required_name}" >&2
    exit 1
  fi
done

# 一键脚本只生成字母数字密码。这里再次限制，避免 SQL 插值产生转义歧义。
for safe_value in "$MYSQL_APP_USER" "$MYSQL_APP_PASSWORD" "$MYSQL_NACOS_USER" "$MYSQL_NACOS_PASSWORD"; do
  if [[ ! "$safe_value" =~ ^[A-Za-z0-9_]+$ ]]; then
    echo "MySQL 初始化账号和密码只允许字母、数字、下划线" >&2
    exit 1
  fi
done

mysql=(mysql --protocol=socket -uroot "-p${MYSQL_ROOT_PASSWORD}")

"${mysql[@]}" <<SQL
CREATE DATABASE IF NOT EXISTS scm_supplier DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS scm_purchase DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS scm_wms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS scm_inventory DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS scm_iam DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS scm_mdm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS scm_oms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS scm_tms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS scm_bms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS nacos_config DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS '${MYSQL_APP_USER}'@'%' IDENTIFIED BY '${MYSQL_APP_PASSWORD}';
CREATE USER IF NOT EXISTS '${MYSQL_NACOS_USER}'@'%' IDENTIFIED BY '${MYSQL_NACOS_PASSWORD}';

GRANT SELECT, INSERT, UPDATE, DELETE ON scm_supplier.* TO '${MYSQL_APP_USER}'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON scm_purchase.* TO '${MYSQL_APP_USER}'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON scm_wms.* TO '${MYSQL_APP_USER}'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON scm_inventory.* TO '${MYSQL_APP_USER}'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON scm_iam.* TO '${MYSQL_APP_USER}'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON scm_mdm.* TO '${MYSQL_APP_USER}'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON scm_oms.* TO '${MYSQL_APP_USER}'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON scm_tms.* TO '${MYSQL_APP_USER}'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON scm_bms.* TO '${MYSQL_APP_USER}'@'%';
GRANT ALL PRIVILEGES ON nacos_config.* TO '${MYSQL_NACOS_USER}'@'%';
FLUSH PRIVILEGES;
SQL

test -s /opt/nacos-schema/mysql-schema.sql
"${mysql[@]}" nacos_config < /opt/nacos-schema/mysql-schema.sql

for service in supplier purchase wms inventory iam mdm oms tms bms; do
  schema_file="/opt/scm-backend/${service}-service/src/main/resources/db/schema.sql"
  database="scm_${service}"
  test -s "${schema_file}"
  echo "导入 ${database} 完整 schema"
  "${mysql[@]}" "${database}" <"${schema_file}"
done
