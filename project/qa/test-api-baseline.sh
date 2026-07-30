#!/usr/bin/env bash
set -Eeuo pipefail
export LC_ALL=C

QA_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPT="${QA_DIR}/api-baseline.sh"
TMP_DIR="$(mktemp -d "${TMPDIR:-/tmp}/scm-qa-baseline-test.XXXXXX")"
trap 'rm -rf "${TMP_DIR}"' EXIT

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

cat >"${TMP_DIR}/tcp-check" <<'EOF'
#!/usr/bin/env bash
[[ "${SCM_FAKE_TCP_FAIL_PORT:-}" != "${2:-}" ]]
EOF

cat >"${TMP_DIR}/curl" <<'EOF'
#!/usr/bin/env bash
body_file=""
header_file=""
authorization=""
url=""
while [[ "$#" -gt 0 ]]; do
  case "$1" in
    -o) body_file="$2"; shift 2 ;;
    -D) header_file="$2"; shift 2 ;;
    -H)
      [[ "$2" == Authorization:* ]] && authorization="${2#Authorization: Bearer }"
      shift 2
      ;;
    -w|--connect-timeout|--max-time) shift 2 ;;
    -s|-S|-sS) shift ;;
    http://*|https://*) url="$1"; shift ;;
    *) shift ;;
  esac
done

status=200
body='{"code":"0","data":[]}'
if [[ -z "${authorization}" ]]; then
  status=401
  body='{"code":"UNAUTHORIZED"}'
elif [[ "${authorization}" == "forbidden-token" ]]; then
  status=403
  body='{"code":"FORBIDDEN"}'
elif [[ -n "${SCM_FAKE_QUERY_FAIL_SERVICE:-}" && "${url}" == *":${SCM_FAKE_QUERY_FAIL_PORT}/"* ]]; then
  status=503
  body='{"code":"UNAVAILABLE"}'
fi
printf 'HTTP/1.1 %s Test\r\nContent-Type: application/json\r\n\r\n' "${status}" >"${header_file}"
printf '%s' "${body}" >"${body_file}"
printf '%s' "${status}"
EOF

chmod +x "${TMP_DIR}/tcp-check" "${TMP_DIR}/curl"

success_output="${TMP_DIR}/success.out"
SCM_CURL_BIN="${TMP_DIR}/curl" SCM_TCP_CHECK_BIN="${TMP_DIR}/tcp-check" \
  "${SCRIPT}" --token access-token --forbidden-token forbidden-token \
  --mysql-address 127.0.0.1:3306 >"${success_output}"

grep -q '^iam-service' "${success_output}" || fail "success matrix misses iam-service"
grep -q '^bms-service' "${success_output}" || fail "success matrix misses bms-service"
grep -q '^mysql' "${success_output}" || fail "success matrix misses mysql"
grep -q 'PASS' "${success_output}" || fail "success matrix contains no PASS result"

failure_output="${TMP_DIR}/failure.out"
if SCM_FAKE_QUERY_FAIL_SERVICE=iam SCM_FAKE_QUERY_FAIL_PORT=8097 SCM_CURL_BIN="${TMP_DIR}/curl" \
    SCM_TCP_CHECK_BIN="${TMP_DIR}/tcp-check" "${SCRIPT}" \
    --token access-token --forbidden-token forbidden-token \
    --mysql-address 127.0.0.1:3306 >"${failure_output}" 2>&1; then
  fail "query failure must produce a non-zero exit"
fi
grep -q '^iam-service.*FAIL(503)' "${failure_output}" \
  || fail "failure matrix does not identify the failed real query"

dry_run_output="${TMP_DIR}/dry-run.out"
"${SCRIPT}" --dry-run --mysql-address db.internal:3306 >"${dry_run_output}"
grep -q 'DRY-RUN' "${dry_run_output}" || fail "dry-run matrix is missing"
grep -q '/api/inventory/v1/stocks' "${dry_run_output}" \
  || fail "dry-run does not expose the inventory query contract"

echo "api-baseline self-test passed"
