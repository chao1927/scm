#!/bin/sh
set -e

mkdir -p /data/7000 /data/7001 /data/7002 /var/log/redis

redis-server /usr/local/etc/redis/redis-7000.conf &
redis-server /usr/local/etc/redis/redis-7001.conf &
redis-server /usr/local/etc/redis/redis-7002.conf &

sleep 5

if [ ! -f /data/7000/nodes-7000.conf ] || ! redis-cli -a "$REDIS_PASSWORD" -p 7000 cluster info 2>/dev/null | grep -q "cluster_state:ok"; then
  echo "Creating Redis Cluster..."
  yes yes | redis-cli -a "$REDIS_PASSWORD" --cluster create \
    127.0.0.1:7000 \
    127.0.0.1:7001 \
    127.0.0.1:7002 \
    --cluster-replicas 0
else
  echo "Redis Cluster already initialized."
fi

wait
