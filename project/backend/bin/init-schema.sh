#!/usr/bin/env bash
set -Eeuo pipefail

BACKEND_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_ADMIN_USER="${MYSQL_ADMIN_USER:-root}"
MYSQL_ADMIN_PASSWORD="${MYSQL_ADMIN_PASSWORD:-${MYSQL_ROOT_PASSWORD:-}}"
DATABASE_SUFFIX="${DATABASE_SUFFIX:-}"
SERVICES=(supplier purchase wms inventory iam mdm oms tms bms)

[[ -n "${MYSQL_ADMIN_PASSWORD}" ]] || {
  echo "请通过 MYSQL_ADMIN_PASSWORD 或 MYSQL_ROOT_PASSWORD 提供 MySQL 管理密码" >&2
  exit 1
}
command -v mysql >/dev/null 2>&1 || {
  echo "未找到 mysql 客户端" >&2
  exit 1
}
[[ "${DATABASE_SUFFIX}" =~ ^[A-Za-z0-9_]*$ ]] || {
  echo "DATABASE_SUFFIX 只允许字母、数字和下划线" >&2
  exit 1
}

mysql_admin=(mysql --protocol=TCP -h "${MYSQL_HOST}" -P "${MYSQL_PORT}" \
  -u "${MYSQL_ADMIN_USER}" "-p${MYSQL_ADMIN_PASSWORD}")

for service in "${SERVICES[@]}"; do
  database="scm_${service}${DATABASE_SUFFIX}"
  schema_file="${BACKEND_DIR}/${service}-service/src/main/resources/db/schema.sql"
  [[ -s "${schema_file}" ]] || {
    echo "缺少完整 schema：${schema_file}" >&2
    exit 1
  }

  table_count="$("${mysql_admin[@]}" -Nse \
    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${database}'")"
  if [[ "${table_count}" != "0" ]]; then
    echo "拒绝覆盖非空数据库 ${database}；完整 schema 只能导入空库" >&2
    exit 1
  fi
done

restore_empty_databases() {
  local failure_code="$?" service database
  trap - ERR
  echo "schema 导入失败，恢复九个目标数据库为空库状态" >&2
  for service in "${SERVICES[@]}"; do
    database="scm_${service}${DATABASE_SUFFIX}"
    "${mysql_admin[@]}" -e \
      "DROP DATABASE IF EXISTS \`${database}\`; CREATE DATABASE \`${database}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;" \
      >/dev/null 2>&1 || true
  done
  return "${failure_code}"
}
trap restore_empty_databases ERR

for service in "${SERVICES[@]}"; do
  database="scm_${service}${DATABASE_SUFFIX}"
  schema_file="${BACKEND_DIR}/${service}-service/src/main/resources/db/schema.sql"
  "${mysql_admin[@]}" -e \
    "CREATE DATABASE IF NOT EXISTS \`${database}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
  "${mysql_admin[@]}" "${database}" <"${schema_file}"
  echo "  ✓ ${database}"
done
trap - ERR

echo "九个数据库完整 schema 导入完成。"
