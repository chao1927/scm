#!/usr/bin/env bash
set -Eeuo pipefail

log() {
  printf '\n[%s] %s\n' "$(date '+%F %T')" "$*"
}

die() {
  printf '\n错误：%s\n' "$*" >&2
  exit 1
}

[[ "${EUID}" -eq 0 ]] || die "请通过 sudo 执行"
[[ $# -eq 3 ]] || die "用法：install-mysql.sh <配置文件> <MySQL 监听 IP> <VMware 网段/掩码>"

CONFIG_FILE="$1"
MYSQL_BIND_IP="$2"
VMWARE_MYSQL_HOST="$3"
trap 'rm -f "${CONFIG_FILE}" /root/.my-scm.cnf /tmp/scm-mysql-app.cnf' EXIT

[[ -r "${CONFIG_FILE}" ]] || die "读取不到配置文件：${CONFIG_FILE}"
sed -i 's/\r$//' "${CONFIG_FILE}"
# shellcheck disable=SC1090
source "${CONFIG_FILE}"

decode_secret() {
  printf '%s' "$1" | base64 --decode
}

MYSQL_ROOT_PASSWORD="$(decode_secret "${MYSQL_ROOT_PASSWORD_B64:-}")"
MYSQL_APP_PASSWORD="$(decode_secret "${MYSQL_APP_PASSWORD_B64:-}")"
readonly MYSQL_APP_USER="scm_app"
readonly MYSQL_DATABASES=(
  scm_supplier scm_purchase scm_wms scm_inventory scm_iam
  scm_mdm scm_oms scm_tms scm_bms
)
readonly MYSQL_DATABASE="${MYSQL_DATABASES[0]}"

validate_password() {
  local name="$1" value="$2"
  [[ ${#value} -ge 12 && ${#value} -le 64 ]] || die "${name} 长度必须为 12～64 位"
  [[ "${value}" =~ ^[A-Za-z0-9!@#%^*_.-]+$ ]] \
    || die "${name} 只能包含字母、数字和 !@#%^*_.-"
}

validate_password "MySQL root 密码" "${MYSQL_ROOT_PASSWORD}"
validate_password "MySQL 应用密码" "${MYSQL_APP_PASSWORD}"
[[ "${MYSQL_BIND_IP}" =~ ^[0-9]{1,3}(\.[0-9]{1,3}){3}$ ]] || die "MySQL IP 格式错误"
[[ "${VMWARE_MYSQL_HOST}" =~ ^[0-9]{1,3}(\.[0-9]{1,3}){3}/255\.255\.255\.0$ ]] \
  || die "VMware MySQL Host 格式应类似 192.168.80.0/255.255.255.0"
ip -4 -o addr show scope global | awk '{print $4}' | cut -d/ -f1 | grep -Fxq "${MYSQL_BIND_IP}" \
  || die "本机不存在 IP ${MYSQL_BIND_IP}"

database_and_grants_sql() {
  local database
  for database in "${MYSQL_DATABASES[@]}"; do
    printf '%s\n' \
      "CREATE DATABASE IF NOT EXISTS \`${database}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;" \
      "GRANT SELECT, INSERT, UPDATE, DELETE ON \`${database}\`.* TO '${MYSQL_APP_USER}'@'10.244.0.0/255.255.0.0';" \
      "GRANT SELECT, INSERT, UPDATE, DELETE ON \`${database}\`.* TO '${MYSQL_APP_USER}'@'${VMWARE_MYSQL_HOST}';"
  done
}

DATABASE_AND_GRANTS_SQL="$(database_and_grants_sql)"

if command -v ufw >/dev/null 2>&1 && ufw status | grep -q '^Status: active'; then
  die "UFW 已启用；请先按文档放行可信来源到 TCP 3306，或在受信实验 VMnet 中停用 UFW"
fi

log "从 Ubuntu 官方仓库安装 MySQL Server"
export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y mysql-server
systemctl enable --now mysql

log "写入 MySQL 监听、字符集和实验环境内存配置"
cat >/etc/mysql/mysql.conf.d/99-scm-native.cnf <<EOF
[mysqld]
bind-address = ${MYSQL_BIND_IP}
mysqlx-bind-address = 127.0.0.1
port = 3306
character-set-server = utf8mb4
collation-server = utf8mb4_0900_ai_ci
innodb_buffer_pool_size = 1G
max_connections = 300
EOF
chmod 0644 /etc/mysql/mysql.conf.d/99-scm-native.cnf
mysqld --validate-config
systemctl restart mysql

cat >/root/.my-scm.cnf <<EOF
[client]
user=root
password='${MYSQL_ROOT_PASSWORD}'
socket=/var/run/mysqld/mysqld.sock
EOF
chmod 0600 /root/.my-scm.cnf

if mysql --protocol=socket -uroot -e 'SELECT 1' >/dev/null 2>&1; then
  log "配置 root 密码、九个 SCM 数据库和最小权限应用账号"
  mysql --protocol=socket -uroot <<EOF
ALTER USER 'root'@'localhost' IDENTIFIED WITH caching_sha2_password BY '${MYSQL_ROOT_PASSWORD}';
CREATE USER IF NOT EXISTS '${MYSQL_APP_USER}'@'10.244.0.0/255.255.0.0' IDENTIFIED WITH caching_sha2_password BY '${MYSQL_APP_PASSWORD}';
CREATE USER IF NOT EXISTS '${MYSQL_APP_USER}'@'${VMWARE_MYSQL_HOST}' IDENTIFIED WITH caching_sha2_password BY '${MYSQL_APP_PASSWORD}';
ALTER USER '${MYSQL_APP_USER}'@'10.244.0.0/255.255.0.0' IDENTIFIED WITH caching_sha2_password BY '${MYSQL_APP_PASSWORD}';
ALTER USER '${MYSQL_APP_USER}'@'${VMWARE_MYSQL_HOST}' IDENTIFIED WITH caching_sha2_password BY '${MYSQL_APP_PASSWORD}';
${DATABASE_AND_GRANTS_SQL}
FLUSH PRIVILEGES;
EOF
elif mysql --defaults-extra-file=/root/.my-scm.cnf -e 'SELECT 1' >/dev/null 2>&1; then
  log "root 已使用输入的密码认证，更新应用账号密码和权限"
  mysql --defaults-extra-file=/root/.my-scm.cnf <<EOF
CREATE USER IF NOT EXISTS '${MYSQL_APP_USER}'@'10.244.0.0/255.255.0.0' IDENTIFIED WITH caching_sha2_password BY '${MYSQL_APP_PASSWORD}';
CREATE USER IF NOT EXISTS '${MYSQL_APP_USER}'@'${VMWARE_MYSQL_HOST}' IDENTIFIED WITH caching_sha2_password BY '${MYSQL_APP_PASSWORD}';
ALTER USER '${MYSQL_APP_USER}'@'10.244.0.0/255.255.0.0' IDENTIFIED WITH caching_sha2_password BY '${MYSQL_APP_PASSWORD}';
ALTER USER '${MYSQL_APP_USER}'@'${VMWARE_MYSQL_HOST}' IDENTIFIED WITH caching_sha2_password BY '${MYSQL_APP_PASSWORD}';
${DATABASE_AND_GRANTS_SQL}
FLUSH PRIVILEGES;
EOF
else
  die "root 已不是 auth_socket 认证，且输入的 root 密码不正确；未修改现有账号"
fi

cat >/tmp/scm-mysql-app.cnf <<EOF
[client]
user=${MYSQL_APP_USER}
password='${MYSQL_APP_PASSWORD}'
host=${MYSQL_BIND_IP}
port=3306
protocol=TCP
EOF
chmod 0600 /tmp/scm-mysql-app.cnf

log "验证 MySQL 服务、监听地址、账号和数据库"
systemctl is-active --quiet mysql || die "MySQL 服务未运行"
ss -lnt | awk '{print $4}' | grep -Eq "(^|:)${MYSQL_BIND_IP}:3306$|${MYSQL_BIND_IP}:3306$" \
  || die "MySQL 未监听 ${MYSQL_BIND_IP}:3306"
mysql --defaults-extra-file=/tmp/scm-mysql-app.cnf "${MYSQL_DATABASE}" -e \
  "CREATE TABLE IF NOT EXISTS installation_probe (id INT PRIMARY KEY, checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP); INSERT INTO installation_probe(id) VALUES (1) ON DUPLICATE KEY UPDATE checked_at=CURRENT_TIMESTAMP; SELECT * FROM installation_probe;"

printf '\n========== MySQL 安装成功 ==========\n'
printf '地址：%s:3306\n数据库：%s\nroot：仅 localhost 管理\n应用账号：%s（允许 Pod 网段与 %s）\n' \
  "${MYSQL_BIND_IP}" "${MYSQL_DATABASES[*]}" "${MYSQL_APP_USER}" "${VMWARE_MYSQL_HOST}"
mysql --defaults-extra-file=/root/.my-scm.cnf -Nse \
  "SELECT user,host,plugin FROM mysql.user WHERE user IN ('root','${MYSQL_APP_USER}') ORDER BY user,host;"
