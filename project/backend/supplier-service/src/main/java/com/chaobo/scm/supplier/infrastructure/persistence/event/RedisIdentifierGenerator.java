package com.chaobo.scm.supplier.infrastructure.persistence.event;

import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdentifierGenerator implements IdentifierGenerator {
    private static final DateTimeFormatter BUSINESS_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final StringRedisTemplate redisTemplate;

    public RedisIdentifierGenerator(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public long nextId() {
        Long sequence = redisTemplate.opsForValue().increment("scm:id:sequence");
        if (sequence == null) {
            throw new IllegalStateException("Redis ID 序列生成失败");
        }
        return sequence;
    }

    @Override
    public String nextBusinessNo(String prefix) {
        return prefix + LocalDateTime.now().format(BUSINESS_TIME) + String.format("%08d", nextId() % 100_000_000);
    }
}
