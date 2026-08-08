#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
BACKEND_DIR="${PROJECT_DIR}/backend"

SERVICES=(supplier purchase wms inventory iam mdm oms tms bms)
for service in "${SERVICES[@]}"; do
  schema="${BACKEND_DIR}/${service}-service/src/main/resources/db/schema.sql"
  [[ -s "${schema}" ]] || {
    echo "[FAIL] 缺少 ${service} 完整 schema: ${schema}" >&2
    exit 1
  }
  grep -Eiq 'CREATE[[:space:]]+TABLE' "${schema}" || {
    echo "[FAIL] ${schema} 没有建表语句" >&2
    exit 1
  }
done

legacy_tool_pattern='fly''way'
if rg -ni "${legacy_tool_pattern}|db/migration" "${BACKEND_DIR}" \
    --glob '!**/target/**' --glob '!**/db/schema.sql'; then
  echo "[FAIL] 后端仍包含旧数据库迁移工具或版本迁移目录引用" >&2
  exit 1
fi

grep -q 'initialize_database_schemas' "${SCRIPT_DIR}/nine-service-local-smoke.sh" || {
  echo "[FAIL] 九服务联调脚本没有显式初始化完整 schema" >&2
  exit 1
}

[[ -x "${SCRIPT_DIR}/test-schema-import-mysql.sh" ]] || {
  echo "[FAIL] 缺少真实 MySQL schema 导入门禁" >&2
  exit 1
}

if rg -n 'GRANT ALL PRIVILEGES ON scm_(supplier|purchase|wms|inventory|iam|mdm|oms|tms|bms)' \
    "${PROJECT_DIR}/../middleware-stack/init/mysql/01-init.sh"; then
  echo "[FAIL] 应用账号仍持有数据库结构变更权限" >&2
  exit 1
fi

echo "[PASS] 九服务仅使用显式导入的完整 schema"
