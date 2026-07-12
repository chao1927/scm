# SCM Middleware Stack

包含：

- MySQL 8.x，单机
- Redis Cluster，单容器 3 节点，无副本，适合开发机资源有限场景
- RocketMQ 5.x，NameServer + 单 Broker
- Nacos 2.x，standalone，嵌入式存储
- Nginx，反向代理 Nacos

## 启动

```bash
docker compose up -d
```

查看状态：

```bash
docker compose ps
```

查看日志：

```bash
docker logs -f scm-mysql
docker logs -f scm-redis-cluster
docker logs -f scm-rocketmq-namesrv
docker logs -f scm-rocketmq-broker
docker logs -f scm-nacos
docker logs -f scm-nginx
```

## 访问地址

### MySQL

```text
host: localhost
port: 3306
root password: Root@123456
database: scm
user: scm
password: Scm@123456
```

### Redis Cluster

```text
nodes:
127.0.0.1:7000
127.0.0.1:7001
127.0.0.1:7002

password:
Redis@123456
```

测试：

```bash
docker exec -it scm-redis-cluster redis-cli -a Redis@123456 -c -p 7000 cluster nodes
```

注意：当前 Redis Cluster 的 `cluster-announce-ip=127.0.0.1`，适合宿主机应用连接。如果 Java 应用也放在 Docker 网络中，需要调整 announce IP。

### RocketMQ

```text
namesrv: localhost:9876
broker: localhost:10911
```

测试：

```bash
docker exec -it scm-rocketmq-namesrv sh mqadmin clusterList -n 127.0.0.1:9876
```

注意：`broker.conf` 中 `brokerIP1=127.0.0.1`，适合宿主机 Java 应用连接。如果应用在 Docker 网络里，需要改成容器可达地址。

### Nacos

直连：

```text
http://localhost:8848/nacos
```

通过 Nginx：

```text
http://localhost/nacos/
```

Nginx Basic Auth：

```text
username: admin
password: Admin@123456
```

Nacos 自身账号通常初始化为：

```text
username: nacos
password: nacos
```

如果你启用了新版本 Nacos 的强制密码策略，请在控制台首次登录后修改。

### Nginx

```text
http://localhost
http://localhost/health
```

## 停止

```bash
docker compose down
```

保留数据。

完全清理数据：

```bash
docker compose down
rm -rf ./data ./logs
```

## 目录说明

```text
docker-compose.yml
.env
config/
  mysql/my.cnf
  redis/redis-7000.conf
  redis/redis-7001.conf
  redis/redis-7002.conf
  rocketmq/broker.conf
  nacos/application.properties
  nginx/nginx.conf
  nginx/conf.d/default.conf
  nginx/htpasswd
init/
  mysql/01-init.sql
scripts/
  redis/start-redis-cluster.sh
data/
logs/
```

## 安全提醒

当前账号密码是开发环境示例，生产或共享环境必须修改：

- MYSQL_ROOT_PASSWORD
- MYSQL_PASSWORD
- REDIS_PASSWORD
- NACOS_AUTH_TOKEN
- Nginx htpasswd

## Spring Boot 示例配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/scm?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: scm
    password: Scm@123456

  data:
    redis:
      password: Redis@123456
      cluster:
        nodes:
          - 127.0.0.1:7000
          - 127.0.0.1:7001
          - 127.0.0.1:7002

rocketmq:
  name-server: localhost:9876
```

## 镜像拉取慢

如果 Docker Hub 拉不下来，先测试：

```bash
docker pull hello-world
```

如果失败，说明 Docker Hub 网络或镜像源有问题，需要配置 Docker Desktop 的 registry mirrors。
