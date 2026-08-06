#!/usr/bin/env bash
set -Eeuo pipefail
export LC_ALL=C

DEPLOY_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TMP_DIR="$(mktemp -d "${TMPDIR:-/tmp}/scm-deploy-test.XXXXXX")"
trap 'rm -rf "${TMP_DIR}"' EXIT

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

cat >"${TMP_DIR}/curl" <<'EOF'
#!/usr/bin/env bash
body_file=""
url=""
while [[ "$#" -gt 0 ]]; do
  case "$1" in
    -o) body_file="$2"; shift 2 ;;
    -w|--connect-timeout|--max-time) shift 2 ;;
    -s|-S|-sS) shift ;;
    http://*|https://*) url="$1"; shift ;;
    *) shift ;;
  esac
done
status=200
body='{"status":"UP"}'
if [[ -n "${SCM_FAKE_UNREADY_PORT:-}" && "${url}" == *":${SCM_FAKE_UNREADY_PORT}/actuator/health/readiness" ]]; then
  status=503
  body='{"status":"DOWN"}'
fi
printf '%s' "${body}" >"${body_file}"
printf '%s' "${status}"
EOF
chmod +x "${TMP_DIR}/curl"

dry_output="${TMP_DIR}/dry.out"
"${DEPLOY_DIR}/bin/health-check.sh" --dry-run >"${dry_output}"
grep -q 'iam-service.*DRY-RUN' "${dry_output}" || fail "dry-run 缺 IAM"
grep -q 'bms-service.*DRY-RUN' "${dry_output}" || fail "dry-run 缺 BMS"

success_output="${TMP_DIR}/success.out"
SCM_CURL_BIN="${TMP_DIR}/curl" "${DEPLOY_DIR}/bin/health-check.sh" >"${success_output}"
grep -q '九服务 liveness/readiness 全部通过' "${success_output}" || fail "成功场景未通过"

failure_output="${TMP_DIR}/failure.out"
if SCM_FAKE_UNREADY_PORT=8103 SCM_CURL_BIN="${TMP_DIR}/curl" \
    "${DEPLOY_DIR}/bin/health-check.sh" >"${failure_output}" 2>&1; then
  fail "WMS readiness 失败时脚本必须失败"
fi
grep -q 'wms-service.*PASS.*FAIL' "${failure_output}" || fail "失败矩阵未定位 WMS readiness"

for script in "${DEPLOY_DIR}"/bin/*.sh; do
  bash -n "${script}" || fail "Shell 语法失败：${script}"
done

for required in \
  alerts/scm-alert-rules.yml \
  metrics/observability-catalog.md \
  runbooks/alert-response.md \
  runbooks/backup-and-recovery.md \
  runbooks/release-and-rollback.md \
  drills/failure-drills.md; do
  [[ -s "${DEPLOY_DIR}/${required}" ]] || fail "缺少运维资产：${required}"
done

grep -q 'severity: page' "${DEPLOY_DIR}/alerts/scm-alert-rules.yml" || fail "告警缺 page 等级"
grep -q 'severity: ticket' "${DEPLOY_DIR}/alerts/scm-alert-rules.yml" || fail "告警缺 ticket 等级"
if grep -q 'severity: warning\|severity: critical' "${DEPLOY_DIR}/alerts/scm-alert-rules.yml"; then
  fail "告警只允许 page/ticket 两级"
fi
alert_count="$(grep -c '^      - alert:' "${DEPLOY_DIR}/alerts/scm-alert-rules.yml")"
runbook_count="$(grep -c 'runbook:' "${DEPLOY_DIR}/alerts/scm-alert-rules.yml")"
[[ "${alert_count}" == "${runbook_count}" ]] || fail "每条告警必须配置 runbook"

backup_plan="${TMP_DIR}/backup-plan.out"
"${DEPLOY_DIR}/bin/backup-mysql.sh" --output-dir "${TMP_DIR}/backup" >"${backup_plan}"
grep -q 'DRY-RUN' "${backup_plan}" || fail "备份默认必须为预览"
[[ ! -e "${TMP_DIR}/backup" ]] || fail "备份预览不得创建目录"

echo "deploy asset tests passed"
