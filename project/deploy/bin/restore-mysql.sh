#!/usr/bin/env bash
set -Eeuo pipefail
export LC_ALL=C

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_DIR=""
EXECUTE=0
MYSQL_HOST_VALUE="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT_VALUE="${MYSQL_PORT:-3306}"
MYSQL_USER_VALUE="${MYSQL_USER:-root}"
TARGET_ENV="${SCM_TARGET_ENV:-test}"

usage() {
  echo "用法：restore-mysql.sh --backup-dir DIR [--execute]" >&2
  echo "执行要求 MYSQL_PWD，且 SCM_RESTORE_CONFIRM 必须等于目标主机。" >&2
}
while [[ "$#" -gt 0 ]]; do
  case "$1" in
    --backup-dir) BACKUP_DIR="${2:-}"; shift 2 ;;
    --execute) EXECUTE=1; shift ;;
    --help|-h) usage; exit 0 ;;
    *) echo "未知参数：$1" >&2; usage; exit 2 ;;
  esac
done
[[ -n "${BACKUP_DIR}" ]] || { usage; exit 2; }
"${SCRIPT_DIR}/verify-backup.sh" "${BACKUP_DIR}"
echo "恢复目标：${MYSQL_HOST_VALUE}:${MYSQL_PORT_VALUE}，环境：${TARGET_ENV}"
if [[ "${EXECUTE}" != "1" ]]; then
  echo "DRY-RUN：仅完成备份校验，未恢复数据。"
  exit 0
fi
[[ -n "${MYSQL_PWD:-}" ]] || { echo "必须通过 MYSQL_PWD 提供密码。" >&2; exit 2; }
[[ "${SCM_RESTORE_CONFIRM:-}" == "${MYSQL_HOST_VALUE}" ]] || {
  echo "SCM_RESTORE_CONFIRM 必须精确等于目标主机。" >&2; exit 2;
}
if [[ "${TARGET_ENV}" == "prod" && "${SCM_ALLOW_PRODUCTION_RESTORE:-}" != "YES" ]]; then
  echo "生产恢复还需要 SCM_ALLOW_PRODUCTION_RESTORE=YES。" >&2
  exit 2
fi
while IFS=$'\t' read -r database file _ _; do
  [[ "${database}" != "database" ]] || continue
  echo "恢复 ${database} ..."
  gzip -dc "${BACKUP_DIR%/}/${file}" | mysql --host="${MYSQL_HOST_VALUE}" \
    --port="${MYSQL_PORT_VALUE}" --user="${MYSQL_USER_VALUE}"
done <"${BACKUP_DIR%/}/manifest.tsv"
echo "恢复导入完成。必须继续执行完整 schema 核验、行数抽样和业务只读冒烟。"
