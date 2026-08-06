#!/usr/bin/env bash
set -Eeuo pipefail
export LC_ALL=C

DATABASES=(scm_iam scm_mdm scm_oms scm_tms scm_supplier scm_purchase scm_wms scm_inventory scm_bms)
OUTPUT_DIR=""
EXECUTE=0
MYSQL_HOST_VALUE="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT_VALUE="${MYSQL_PORT:-3306}"
MYSQL_USER_VALUE="${MYSQL_USER:-root}"

usage() {
  echo "用法：backup-mysql.sh --output-dir DIR [--execute]" >&2
  echo "执行时密码仅从 MYSQL_PWD 读取；默认只打印备份计划。" >&2
}

while [[ "$#" -gt 0 ]]; do
  case "$1" in
    --output-dir) OUTPUT_DIR="${2:-}"; shift 2 ;;
    --execute) EXECUTE=1; shift ;;
    --help|-h) usage; exit 0 ;;
    *) echo "未知参数：$1" >&2; usage; exit 2 ;;
  esac
done

[[ -n "${OUTPUT_DIR}" ]] || { usage; exit 2; }
printf '目标：%s:%s，用户：%s，数据库：%s\n' "${MYSQL_HOST_VALUE}" "${MYSQL_PORT_VALUE}" \
  "${MYSQL_USER_VALUE}" "${DATABASES[*]}"
if [[ "${EXECUTE}" != "1" ]]; then
  echo "DRY-RUN：未连接数据库、未创建备份。增加 --execute 后执行。"
  exit 0
fi
[[ -n "${MYSQL_PWD:-}" ]] || { echo "必须通过 MYSQL_PWD 提供密码。" >&2; exit 2; }
command -v mysqldump >/dev/null || { echo "找不到 mysqldump。" >&2; exit 2; }
command -v shasum >/dev/null || { echo "找不到 shasum。" >&2; exit 2; }

mkdir -p "${OUTPUT_DIR}"
manifest="${OUTPUT_DIR%/}/manifest.tsv"
: >"${manifest}"
printf 'database\tfile\tsha256\tbytes\n' >>"${manifest}"
for database in "${DATABASES[@]}"; do
  target="${OUTPUT_DIR%/}/${database}.sql.gz"
  mysqldump --host="${MYSQL_HOST_VALUE}" --port="${MYSQL_PORT_VALUE}" \
    --user="${MYSQL_USER_VALUE}" --single-transaction --routines --triggers --events \
    --set-gtid-purged=OFF --databases "${database}" | gzip -c >"${target}"
  digest="$(shasum -a 256 "${target}" | awk '{print $1}')"
  bytes="$(wc -c <"${target}" | tr -d ' ')"
  printf '%s\t%s\t%s\t%s\n' "${database}" "$(basename "${target}")" "${digest}" "${bytes}" >>"${manifest}"
done
echo "备份完成：${manifest}。请按保留策略转移到不可变存储。"
