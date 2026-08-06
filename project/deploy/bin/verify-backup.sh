#!/usr/bin/env bash
set -Eeuo pipefail
export LC_ALL=C

BACKUP_DIR="${1:-}"
[[ -n "${BACKUP_DIR}" && -f "${BACKUP_DIR%/}/manifest.tsv" ]] || {
  echo "用法：verify-backup.sh BACKUP_DIR" >&2
  exit 2
}

failed=0
while IFS=$'\t' read -r database file expected bytes; do
  [[ "${database}" != "database" ]] || continue
  target="${BACKUP_DIR%/}/${file}"
  if [[ ! -f "${target}" ]]; then
    echo "缺少：${file}" >&2
    failed=1
    continue
  fi
  actual="$(shasum -a 256 "${target}" | awk '{print $1}')"
  actual_bytes="$(wc -c <"${target}" | tr -d ' ')"
  if [[ "${actual}" != "${expected}" || "${actual_bytes}" != "${bytes}" ]]; then
    echo "校验失败：${file}" >&2
    failed=1
  else
    echo "PASS ${database} ${file}"
  fi
done <"${BACKUP_DIR%/}/manifest.tsv"
[[ "${failed}" == "0" ]] || exit 1
echo "备份清单、大小和 SHA-256 全部通过。"
