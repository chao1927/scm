#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
MYSQL_CONTAINER="${SCHEMA_MYSQL_CONTAINER:-scm-mysql}"
PREFIX="scm_schema_verify_"
SERVICES=(supplier purchase wms inventory iam mdm oms tms bms)
EXPECTED_TABLE_COUNTS=(44 25 22 20 28 20 13 13 18)

cleanup() {
  local service database
  for service in "${SERVICES[@]}"; do
    database="${PREFIX}${service}"
    docker exec "${MYSQL_CONTAINER}" sh -c \
      'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "DROP DATABASE IF EXISTS '"${database}"'"' \
      >/dev/null
  done
}
trap cleanup EXIT

for index in "${!SERVICES[@]}"; do
  service="${SERVICES[${index}]}"
  expected_table_count="${EXPECTED_TABLE_COUNTS[${index}]}"
  database="${PREFIX}${service}"
  schema="${PROJECT_DIR}/backend/${service}-service/src/main/resources/db/schema.sql"
  [[ -s "${schema}" ]] || {
    echo "[FAIL] 缺少 ${schema}" >&2
    exit 1
  }
  docker exec "${MYSQL_CONTAINER}" sh -c \
    'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "CREATE DATABASE '"${database}"' CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci"' \
    >/dev/null
  docker exec -i "${MYSQL_CONTAINER}" sh -c \
    'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" '"${database}" <"${schema}"
  table_count="$(docker exec "${MYSQL_CONTAINER}" sh -c \
    'exec mysql -N -uroot -p"$MYSQL_ROOT_PASSWORD" -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=\"'"${database}"'\""')"
  [[ "${table_count}" -ge "${expected_table_count}" ]] || {
    echo "[FAIL] ${database} 预期至少 ${expected_table_count} 张表，实际 ${table_count}" >&2
    exit 1
  }
  echo "  ✓ ${database}: ${table_count} tables"
done

echo "[PASS] 九服务完整 schema 均可导入 MySQL 8 空库"
