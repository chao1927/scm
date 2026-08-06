#!/usr/bin/env bash
set -Eeuo pipefail
export LC_ALL=C

MYSQL_HOST_VALUE="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT_VALUE="${MYSQL_PORT:-3306}"
MYSQL_USER_VALUE="${MYSQL_USER:-root}"
MYSQL_BIN="${SCM_MYSQL_BIN:-mysql}"
[[ -n "${MYSQL_PWD:-}" ]] || { echo "必须通过 MYSQL_PWD 提供密码。" >&2; exit 2; }

LEDGERS='scm_supplier|sup_domain_event|event_status|1,2|4|created_at
scm_purchase|purchase_outbox_event|status|1,2|4|created_at
scm_wms|wms_operation_event|status|1,2|3|created_at
scm_inventory|inv_outbox_event|status|1|3|created_at
scm_iam|iam_outbox_event|event_status|1|3|created_at
scm_mdm|mdm_outbox_event|event_status|1|3|created_at
scm_oms|oms_outbox_event|event_status|1|3|created_at
scm_tms|tms_domain_event|event_status|1|3|created_at
scm_bms|bms_domain_event|status|1|3|created_at'

printf '%-16s %-28s %10s %10s %12s\n' DATABASE LEDGER PENDING FAILED OLDEST_SEC
while IFS='|' read -r database table status_column pending failed created_column; do
  [[ -n "${database}" ]] || continue
  exists="$(${MYSQL_BIN} --batch --skip-column-names --host="${MYSQL_HOST_VALUE}" \
    --port="${MYSQL_PORT_VALUE}" --user="${MYSQL_USER_VALUE}" \
    -e "select count(*) from information_schema.tables where table_schema='${database}' and table_name='${table}'")"
  if [[ "${exists}" != "1" ]]; then
    printf '%-16s %-28s %10s %10s %12s\n' "${database}" "${table}" MISSING MISSING MISSING
    continue
  fi
  result="$(${MYSQL_BIN} --batch --skip-column-names --host="${MYSQL_HOST_VALUE}" \
    --port="${MYSQL_PORT_VALUE}" --user="${MYSQL_USER_VALUE}" "${database}" \
    -e "select sum(${status_column} in (${pending})),sum(${status_column} in (${failed})),coalesce(timestampdiff(second,min(case when ${status_column} in (${pending}) then ${created_column} end),now()),0) from ${table}")"
  read -r pending_count failed_count oldest_seconds <<<"${result}"
  printf '%-16s %-28s %10s %10s %12s\n' "${database}" "${table}" \
    "${pending_count:-0}" "${failed_count:-0}" "${oldest_seconds:-0}"
done <<<"${LEDGERS}"
echo "只读盘点完成；任何 FAILED 或持续增长的 OLDEST_SEC 都必须按消息处置手册处理。"
