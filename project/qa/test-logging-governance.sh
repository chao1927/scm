#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="${ROOT_DIR}/backend"
COMMON_BASE="${BACKEND_DIR}/scm-common/src/main/resources/com/chaobo/scm/common/logging/logback-base.xml"
SERVICES=(supplier purchase wms inventory iam mdm oms tms bms)

require_pattern() {
  local pattern="$1"
  local file="$2"
  local message="$3"
  if ! rg -q "${pattern}" "${file}"; then
    echo "日志治理门禁失败：${message} (${file})" >&2
    exit 1
  fi
}

require_pattern 'INFO_FILE' "${COMMON_BASE}" '缺少 INFO 独立文件'
require_pattern 'ERROR_FILE' "${COMMON_BASE}" '缺少 ERROR 独立文件'
require_pattern 'SizeAndTimeBasedRollingPolicy' "${COMMON_BASE}" '缺少按日期和大小滚动策略'
require_pattern '<maxFileSize>\$\{SCM_LOG_MAX_FILE_SIZE\}</maxFileSize>' "${COMMON_BASE}" '缺少单文件大小上限'
require_pattern '<maxHistory>\$\{SCM_LOG_MAX_HISTORY\}</maxHistory>' "${COMMON_BASE}" '缺少保留天数限制'
require_pattern '<totalSizeCap>\$\{SCM_LOG_INFO_TOTAL_SIZE_CAP\}</totalSizeCap>' "${COMMON_BASE}" '缺少 INFO 总容量上限'
require_pattern '<totalSizeCap>\$\{SCM_LOG_ERROR_TOTAL_SIZE_CAP\}</totalSizeCap>' "${COMMON_BASE}" '缺少 ERROR 总容量上限'
require_pattern '<cleanHistoryOnStart>true</cleanHistoryOnStart>' "${COMMON_BASE}" '启动时未清理过期日志'
require_pattern 'requestId=.*operatorId=.*operator=' "${COMMON_BASE}" '日志格式缺少关联标识或操作人'

for service in "${SERVICES[@]}"; do
  config="${BACKEND_DIR}/${service}-service/src/main/resources/logback-spring.xml"
  [[ -f "${config}" ]] || { echo "日志治理门禁失败：${service}-service 缺少 logback-spring.xml" >&2; exit 1; }
  require_pattern 'defaultValue="100MB"' "${config}" "${service}-service 单文件默认上限不是 100MB"
  require_pattern 'defaultValue="7"' "${config}" "${service}-service 默认保留期不是 7 天"
  require_pattern 'defaultValue="2GB"' "${config}" "${service}-service INFO 总容量默认值缺失"
  require_pattern 'defaultValue="1GB"' "${config}" "${service}-service ERROR 总容量默认值缺失"
done

request_filter="${BACKEND_DIR}/scm-common/src/main/java/com/chaobo/scm/common/logging/ScmRequestLoggingFilter.java"
require_pattern 'event=http_request_completed' "${request_filter}" '缺少 HTTP 成功/拒绝日志'
require_pattern 'event=http_request_failed' "${request_filter}" '缺少 HTTP 异常日志'
require_pattern 'doesNotContain\("password"' "${BACKEND_DIR}/scm-common/src/test/java/com/chaobo/scm/common/logging/ScmRequestLoggingFilterTest.java" '缺少敏感查询参数不入日志测试'

mq_consumers="$(rg -l 'ConsumeResult consume\(MessageView' "${BACKEND_DIR}" --glob 'RocketMq*.java' --glob '!**/target/**')"
consumer_count="$(printf '%s\n' "${mq_consumers}" | sed '/^$/d' | wc -l | tr -d ' ')"
[[ "${consumer_count}" -eq 11 ]] || { echo "日志治理门禁失败：预期 11 个 RocketMQ 消费入口，实际 ${consumer_count}" >&2; exit 1; }
while IFS= read -r consumer; do
  [[ -z "${consumer}" ]] && continue
  require_pattern 'event=rocketmq_consume' "${consumer}" 'RocketMQ 消费入口缺少结构化结果日志'
done <<< "${mq_consumers}"

if rg -n 'LOG\.(info|warn|error)|log\.(info|warn|error)' "${BACKEND_DIR}" --glob '*.java' --glob '!**/target/**' \
    | rg -i 'password|passwd|access-token|authorization|cookie|secret'; then
  echo '日志治理门禁失败：日志语句疑似记录凭据字段' >&2
  exit 1
fi

echo "日志治理门禁通过：9 个服务、${consumer_count} 个 RocketMQ 消费入口。"
