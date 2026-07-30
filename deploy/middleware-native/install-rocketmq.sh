#!/usr/bin/env bash
set -Eeuo pipefail

readonly ROCKETMQ_VERSION="5.5.0"
readonly ROCKETMQ_CLUSTER="DefaultCluster"
readonly ROCKETMQ_HOME="/opt/rocketmq"
readonly ROCKETMQ_ADMIN_USER="scm_rmq_admin"
readonly ROCKETMQ_APP_USER="scm_app"

log() {
  printf '\n[%s] %s\n' "$(date '+%F %T')" "$*"
}

die() {
  printf '\n错误：%s\n' "$*" >&2
  exit 1
}

[[ "${EUID}" -eq 0 ]] || die "请通过 sudo 执行"
[[ $# -eq 2 ]] || die "用法：install-rocketmq.sh <配置文件> <RocketMQ 节点 IP>"

CONFIG_FILE="$1"
ROCKETMQ_IP="$2"
trap 'rm -f "${CONFIG_FILE}"' EXIT

[[ -r "${CONFIG_FILE}" ]] || die "读取不到配置文件：${CONFIG_FILE}"
sed -i 's/\r$//' "${CONFIG_FILE}"
# shellcheck disable=SC1090
source "${CONFIG_FILE}"

decode_secret() {
  printf '%s' "$1" | base64 --decode
}

ROCKETMQ_ADMIN_PASSWORD="$(decode_secret "${ROCKETMQ_ADMIN_PASSWORD_B64:-}")"
ROCKETMQ_APP_PASSWORD="$(decode_secret "${ROCKETMQ_APP_PASSWORD_B64:-}")"

validate_password() {
  local name="$1" value="$2"
  [[ ${#value} -ge 24 && ${#value} -le 64 ]] || die "${name} 长度必须为 24～64 位"
  [[ "${value}" =~ ^[A-Za-z0-9]+$ ]] || die "${name} 只能包含大小写字母和数字"
}

validate_password "RocketMQ 管理员密码" "${ROCKETMQ_ADMIN_PASSWORD}"
validate_password "RocketMQ 应用密码" "${ROCKETMQ_APP_PASSWORD}"
[[ "${ROCKETMQ_IP}" =~ ^[0-9]{1,3}(\.[0-9]{1,3}){3}$ ]] || die "RocketMQ IP 格式错误"
ip -4 -o addr show scope global | awk '{print $4}' | cut -d/ -f1 | grep -Fxq "${ROCKETMQ_IP}" \
  || die "本机不存在 IP ${ROCKETMQ_IP}"

if command -v ufw >/dev/null 2>&1 && ufw status | grep -q '^Status: active'; then
  die "UFW 已启用；请先按文档放行可信来源到 TCP 9876、10909、10911，或在受信实验 VMnet 中停用 UFW"
fi

log "安装 OpenJDK 17、下载与校验工具"
export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y openjdk-17-jdk-headless curl unzip
JAVA_BIN_PATH="$(dpkg -L openjdk-17-jdk-headless | grep '/bin/java$' | head -n 1)"
[[ -x "${JAVA_BIN_PATH}" ]] || die "找不到 OpenJDK 17 java"
JAVA_HOME_PATH="$(dirname "$(dirname "${JAVA_BIN_PATH}")")"

if [[ ! -f "/opt/rocketmq-${ROCKETMQ_VERSION}/bin/mqbroker" ]]; then
  log "从 Apache 官方镜像下载 RocketMQ ${ROCKETMQ_VERSION} 二进制包并校验 SHA-512"
  TMP_DIR="$(mktemp -d)"
  trap 'rm -rf "${TMP_DIR}"; rm -f "${CONFIG_FILE}"' EXIT
  ARCHIVE="rocketmq-all-${ROCKETMQ_VERSION}-bin-release.zip"
  BASE_URL="https://downloads.apache.org/rocketmq/${ROCKETMQ_VERSION}"
  curl --fail --location --retry 3 --proto '=https' --tlsv1.2 \
    --output "${TMP_DIR}/${ARCHIVE}" "${BASE_URL}/${ARCHIVE}"
  curl --fail --location --retry 3 --proto '=https' --tlsv1.2 \
    --output "${TMP_DIR}/${ARCHIVE}.sha512" "${BASE_URL}/${ARCHIVE}.sha512"
  (
    cd "${TMP_DIR}"
    EXPECTED_SHA512="$(sed '1s/^[^:]*://' "${ARCHIVE}.sha512" | tr -d '[:space:]' | tr '[:upper:]' '[:lower:]')"
    ACTUAL_SHA512="$(sha512sum "${ARCHIVE}" | awk '{print $1}')"
    [[ "${ACTUAL_SHA512}" == "${EXPECTED_SHA512}" ]] || die "RocketMQ SHA-512 校验失败"
    printf '%s: OK\n' "${ARCHIVE}"
    unzip -q "${ARCHIVE}"
  )
  EXTRACTED_DIR="$(find "${TMP_DIR}" -mindepth 1 -maxdepth 1 -type d | head -n 1)"
  [[ -f "${EXTRACTED_DIR}/bin/mqbroker" ]] || die "RocketMQ 解压目录不完整"
  mv "${EXTRACTED_DIR}" "/opt/rocketmq-${ROCKETMQ_VERSION}"
fi
ln -sfn "/opt/rocketmq-${ROCKETMQ_VERSION}" "${ROCKETMQ_HOME}"

log "创建 RocketMQ 系统账号、数据目录和 ACL 2.0 配置"
if ! id rocketmq >/dev/null 2>&1; then
  useradd --system --home-dir /var/lib/rocketmq --create-home --shell /usr/sbin/nologin rocketmq
fi
install -d -o rocketmq -g rocketmq -m 0750 /var/lib/rocketmq/store /var/lib/rocketmq/logs
install -d -o root -g rocketmq -m 0750 /etc/rocketmq

cat >/etc/rocketmq/broker.conf <<EOF
brokerClusterName = ${ROCKETMQ_CLUSTER}
brokerName = broker-a
brokerId = 0
brokerIP1 = ${ROCKETMQ_IP}
namesrvAddr = ${ROCKETMQ_IP}:9876
listenPort = 10911
deleteWhen = 04
fileReservedTime = 72
brokerRole = ASYNC_MASTER
flushDiskType = ASYNC_FLUSH
storePathRootDir = /var/lib/rocketmq/store
storePathCommitLog = /var/lib/rocketmq/store/commitlog
autoCreateTopicEnable = false
authenticationEnabled = true
authenticationMetadataProvider = org.apache.rocketmq.auth.authentication.provider.LocalAuthenticationMetadataProvider
authorizationEnabled = true
authorizationMetadataProvider = org.apache.rocketmq.auth.authorization.provider.LocalAuthorizationMetadataProvider
initAuthenticationUser = {"username":"${ROCKETMQ_ADMIN_USER}","password":"${ROCKETMQ_ADMIN_PASSWORD}"}
EOF
chown root:rocketmq /etc/rocketmq/broker.conf
chmod 0640 /etc/rocketmq/broker.conf

cat >"${ROCKETMQ_HOME}/conf/tools.yml" <<EOF
accessKey: "${ROCKETMQ_ADMIN_USER}"
secretKey: "${ROCKETMQ_ADMIN_PASSWORD}"
EOF
chown root:rocketmq "${ROCKETMQ_HOME}/conf/tools.yml"
chmod 0640 "${ROCKETMQ_HOME}/conf/tools.yml"
chown -R root:rocketmq "/opt/rocketmq-${ROCKETMQ_VERSION}"
find "/opt/rocketmq-${ROCKETMQ_VERSION}/bin" -type f -exec chmod 0755 {} +

log "创建低内存实验环境 systemd 服务"
cat >/etc/systemd/system/rocketmq-namesrv.service <<EOF
[Unit]
Description=Apache RocketMQ NameServer
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=rocketmq
Group=rocketmq
WorkingDirectory=${ROCKETMQ_HOME}
Environment=JAVA_HOME=${JAVA_HOME_PATH}
Environment=HOME=/var/lib/rocketmq
Environment="JAVA_OPT_EXT=-Xms512m -Xmx512m"
ExecStart=${ROCKETMQ_HOME}/bin/mqnamesrv
ExecStop=${ROCKETMQ_HOME}/bin/mqshutdown namesrv
Restart=on-failure
RestartSec=5
TimeoutStopSec=30
LimitNOFILE=655350

[Install]
WantedBy=multi-user.target
EOF

cat >/etc/systemd/system/rocketmq-broker.service <<EOF
[Unit]
Description=Apache RocketMQ Broker
After=network-online.target rocketmq-namesrv.service
Requires=rocketmq-namesrv.service

[Service]
Type=simple
User=rocketmq
Group=rocketmq
WorkingDirectory=${ROCKETMQ_HOME}
Environment=JAVA_HOME=${JAVA_HOME_PATH}
Environment=HOME=/var/lib/rocketmq
Environment="JAVA_OPT_EXT=-Xms2g -Xmx2g -XX:MaxDirectMemorySize=1g -XX:-AlwaysPreTouch"
ExecStart=${ROCKETMQ_HOME}/bin/mqbroker -c /etc/rocketmq/broker.conf
ExecStop=${ROCKETMQ_HOME}/bin/mqshutdown broker
Restart=on-failure
RestartSec=5
TimeoutStartSec=180
TimeoutStopSec=60
LimitNOFILE=655350

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable --now rocketmq-namesrv
systemctl restart rocketmq-namesrv
systemctl enable --now rocketmq-broker
systemctl restart rocketmq-broker

mqadmin() {
  runuser -u rocketmq -- env JAVA_HOME="${JAVA_HOME_PATH}" "${ROCKETMQ_HOME}/bin/mqadmin" "$@"
}

log "等待 Broker 注册并验证管理员认证（最长 3 分钟）"
READY=false
for _ in $(seq 1 36); do
  if mqadmin clusterList -n "${ROCKETMQ_IP}:9876" >/dev/null 2>&1; then
    READY=true
    break
  fi
  sleep 5
done
[[ "${READY}" == "true" ]] || {
  journalctl -u rocketmq-broker -n 100 --no-pager || true
  die "Broker 未就绪，或管理员密码与已存在的 ACL 元数据不一致"
}

log "创建/更新 scm_app，并授予 scm_* Topic/Group 的发布订阅权限"
if mqadmin getUser -n "${ROCKETMQ_IP}:9876" -c "${ROCKETMQ_CLUSTER}" -u "${ROCKETMQ_APP_USER}" >/dev/null 2>&1; then
  mqadmin updateUser -n "${ROCKETMQ_IP}:9876" -c "${ROCKETMQ_CLUSTER}" -u "${ROCKETMQ_APP_USER}" -p "${ROCKETMQ_APP_PASSWORD}" -t Normal
else
  mqadmin createUser -n "${ROCKETMQ_IP}:9876" -c "${ROCKETMQ_CLUSTER}" -u "${ROCKETMQ_APP_USER}" -p "${ROCKETMQ_APP_PASSWORD}" -t Normal
fi

upsert_acl() {
  local resource="$1" actions="$2"
  if mqadmin getAcl -n "${ROCKETMQ_IP}:9876" -c "${ROCKETMQ_CLUSTER}" -s "User:${ROCKETMQ_APP_USER}" -r "${resource}" >/dev/null 2>&1; then
    mqadmin updateAcl -n "${ROCKETMQ_IP}:9876" -c "${ROCKETMQ_CLUSTER}" -s "User:${ROCKETMQ_APP_USER}" -r "${resource}" -a "${actions}" -d Allow
  else
    mqadmin createAcl -n "${ROCKETMQ_IP}:9876" -c "${ROCKETMQ_CLUSTER}" -s "User:${ROCKETMQ_APP_USER}" -r "${resource}" -a "${actions}" -d Allow
  fi
}

upsert_acl 'Topic:scm_*' 'Pub,Sub'
upsert_acl 'Group:scm_*' 'Sub'
mqadmin updateTopic -n "${ROCKETMQ_IP}:9876" -c "${ROCKETMQ_CLUSTER}" -t scm_test_topic

log "最终验收"
systemctl is-active --quiet rocketmq-namesrv || die "NameServer 未运行"
systemctl is-active --quiet rocketmq-broker || die "Broker 未运行"
ss -lnt | awk '{print $4}' | grep -q ':9876$' || die "NameServer 未监听 9876"
ss -lnt | awk '{print $4}' | grep -q ':10911$' || die "Broker 未监听 10911"
NAMESRV_PID="$(pgrep -f 'org.apache.rocketmq.namesrv.NamesrvStartup' | head -n 1)"
BROKER_PID="$(pgrep -f 'org.apache.rocketmq.broker.BrokerStartup' | head -n 1)"
[[ -n "${NAMESRV_PID}" && -n "${BROKER_PID}" ]] || die "找不到 RocketMQ Java 进程"
NAMESRV_FLAGS="$(runuser -u rocketmq -- "${JAVA_HOME_PATH}/bin/jcmd" "${NAMESRV_PID}" VM.flags)"
BROKER_FLAGS="$(runuser -u rocketmq -- "${JAVA_HOME_PATH}/bin/jcmd" "${BROKER_PID}" VM.flags)"
grep -q 'InitialHeapSize=536870912' <<<"${NAMESRV_FLAGS}" || die "NameServer JVM Heap 未覆盖为 512 MB"
grep -q 'MaxHeapSize=536870912' <<<"${NAMESRV_FLAGS}" || die "NameServer JVM Max Heap 未覆盖为 512 MB"
grep -q 'InitialHeapSize=2147483648' <<<"${BROKER_FLAGS}" || die "Broker JVM Heap 未覆盖为 2 GB"
grep -q 'MaxHeapSize=2147483648' <<<"${BROKER_FLAGS}" || die "Broker JVM Max Heap 未覆盖为 2 GB"
grep -q 'MaxDirectMemorySize=1073741824' <<<"${BROKER_FLAGS}" || die "Broker Direct Memory 未覆盖为 1 GB"
printf '%s\n%s\n' "${NAMESRV_FLAGS}" "${BROKER_FLAGS}"
mqadmin clusterList -n "${ROCKETMQ_IP}:9876"
mqadmin getUser -n "${ROCKETMQ_IP}:9876" -c "${ROCKETMQ_CLUSTER}" -u "${ROCKETMQ_APP_USER}"
mqadmin getAcl -n "${ROCKETMQ_IP}:9876" -c "${ROCKETMQ_CLUSTER}" -s "User:${ROCKETMQ_APP_USER}"

printf '\n========== RocketMQ 安装成功 ==========\n'
printf 'NameServer：%s:9876\nBroker：%s:10911\n集群：%s\n管理员：%s (Super)\n应用账号：%s (Normal)\n' \
  "${ROCKETMQ_IP}" "${ROCKETMQ_IP}" "${ROCKETMQ_CLUSTER}" "${ROCKETMQ_ADMIN_USER}" "${ROCKETMQ_APP_USER}"
