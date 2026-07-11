package com.chaobo.scm.supplier.infrastructure.persistence.event;

import com.chaobo.scm.supplier.application.shared.CommandResult;
import com.chaobo.scm.supplier.application.shared.IdempotencyPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Component
public class RedisIdempotencyAdapter implements IdempotencyPort {
    private final StringRedisTemplate redisTemplate;

    public RedisIdempotencyAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<StoredCommandResult> find(String key) {
        Map<Object, Object> values = redisTemplate.opsForHash().entries(key);
        if (values.isEmpty()) {
            return Optional.empty();
        }
        String hash = text(values, "requestHash");
        if (!"COMPLETED".equals(text(values, "state"))) {
            return Optional.of(new StoredCommandResult(hash, null));
        }
        CommandResult result = new CommandResult(
                Long.parseLong(text(values, "aggregateId")),
                text(values, "businessNo"),
                Integer.parseInt(text(values, "status")),
                text(values, "statusName"),
                Integer.parseInt(text(values, "version")),
                text(values, "eventCode"),
                true);
        return Optional.of(new StoredCommandResult(hash, result));
    }

    @Override
    public boolean reserve(String key, String requestHash, Duration ttl) {
        Boolean created = redisTemplate.opsForValue().setIfAbsent(key + ":lock", requestHash, ttl);
        if (!Boolean.TRUE.equals(created)) {
            return false;
        }
        redisTemplate.opsForHash().putAll(key, Map.of("requestHash", requestHash, "state", "PROCESSING"));
        redisTemplate.expire(key, ttl);
        return true;
    }

    @Override
    public void complete(String key, String requestHash, CommandResult result, Duration ttl) {
        redisTemplate.opsForHash().putAll(key, Map.of(
                "requestHash", requestHash,
                "state", "COMPLETED",
                "aggregateId", Long.toString(result.aggregateId()),
                "businessNo", result.businessNo(),
                "status", Integer.toString(result.status()),
                "statusName", result.statusName(),
                "version", Integer.toString(result.version()),
                "eventCode", result.eventCode() == null ? "" : result.eventCode()));
        redisTemplate.expire(key, ttl);
        redisTemplate.delete(key + ":lock");
    }

    @Override
    public void release(String key, String requestHash) {
        Object storedHash = redisTemplate.opsForHash().get(key, "requestHash");
        if (requestHash.equals(storedHash)) {
            redisTemplate.delete(key);
            redisTemplate.delete(key + ":lock");
        }
    }

    private String text(Map<Object, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? "" : value.toString();
    }
}
