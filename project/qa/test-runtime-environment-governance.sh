#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
BACKEND_DIR="${PROJECT_DIR}/backend"

fail() {
  echo "环境配置门禁失败：$1" >&2
  exit 1
}

[[ "$(tr -d '[:space:]' <"${BACKEND_DIR}/.java-version")" == "17" ]] \
  || fail "backend/.java-version 必须固定为 17"

grep -q '<version>\[17,18)</version>' "${BACKEND_DIR}/pom.xml" \
  || fail "Maven 必须拒绝 JDK 17 以外的运行时"

if ! awk '
    /build_services\(\)/ { in_build = 1 }
    in_build && /use_java_17/ { selected = NR }
    in_build && /mvn -o -X validate/ { validated = NR; exit }
    END { exit selected > 0 && selected < validated ? 0 : 1 }
  ' "${BACKEND_DIR}/bin/dev.sh"; then
  fail "dev.sh 必须在首次 Maven 调用前切换到 JDK 17"
fi

if grep -q 'set -a' "${BACKEND_DIR}/bin/dev.sh" "${SCRIPT_DIR}/nine-service-local-smoke.sh"; then
  fail "Java 启动脚本不得整包导出 middleware-stack/.env"
fi

if grep -R '\${ROCKETMQ_ENABLED' \
    "${BACKEND_DIR}"/*-service/src/main/resources/application*.yml >/dev/null; then
  fail "RocketMQ 总开关只允许使用 SCM_ROCKETMQ_ENABLED"
fi

if grep -R '\${ROCKETMQ_ENDPOINTS' \
    "${BACKEND_DIR}"/*-service/src/main/resources/application*.yml >/dev/null; then
  fail "RocketMQ 地址只允许使用 SCM_ROCKETMQ_ENDPOINTS"
fi

if grep -qE '(^|[[:space:]])ROCKETMQ_ENABLED=' \
    "${BACKEND_DIR}/bin/dev.sh" "${SCRIPT_DIR}/nine-service-local-smoke.sh" \
    "${SCRIPT_DIR}/dubbo-local-smoke.sh"; then
  fail "启动脚本不得再注入旧的 ROCKETMQ_ENABLED"
fi

if grep -q 'IAM_JWT_SECRET="${NACOS_AUTH_TOKEN}"' "${BACKEND_DIR}/bin/dev.sh"; then
  fail "IAM JWT 密钥不得与 Nacos 认证 Token 复用"
fi

grep -q -- '-XX:ActiveProcessorCount=2' "${BACKEND_DIR}/bin/dev.sh" \
  || fail "九服务本地启动必须限制单 JVM 的处理器视图，避免线程池按整机核数膨胀"

if ! awk '
    /^up\(\)/ { in_up = 1 }
    in_up && /start_service/ { started = NR }
    in_up && /wait_for_service/ { waited = NR; exit }
    END { exit started > 0 && waited == started + 1 ? 0 : 1 }
  ' "${BACKEND_DIR}/bin/dev.sh"; then
  fail "九服务必须逐个启动并等待健康，禁止并发冷启动耗尽本机线程"
fi

for script in "${BACKEND_DIR}/bin/dev.sh" "${SCRIPT_DIR}/nine-service-local-smoke.sh"; do
  grep -q 'env -i' "${script}" || fail "${script} 必须以环境白名单启动 Java"
  grep -q 'SPRING_CLOUD_NACOS_CONFIG_ENABLED="true"' "${script}" \
    || fail "${script} 必须显式启用 Nacos Config"
  grep -q 'SCM_ROCKETMQ_ENABLED="true"' "${script}" \
    || fail "${script} 必须显式启用 RocketMQ"
done

for script in "${BACKEND_DIR}/bin/dev.sh" "${SCRIPT_DIR}/nine-service-local-smoke.sh"; do
  if awk '
      /env -i/ { in_java_env = 1 }
      in_java_env && /(MYSQL_ROOT_PASSWORD|MYSQL_NACOS_PASSWORD|NACOS_AUTH_TOKEN)=/ { found = 1 }
      in_java_env && /java.*-jar/ { in_java_env = 0 }
      END { exit found ? 0 : 1 }
    ' "${script}"; then
    fail "${script} 向 Java 进程注入了管理员或基础设施凭据"
  fi
done

echo "JDK 17、Nacos、RocketMQ 和 Java 进程环境白名单门禁通过。"
