package com.chaobo.scm.iam.infrastructure.cache;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.iam.application.TokenCachePort;
import org.junit.jupiter.api.Test;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.HashOperations;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedisTokenCacheAdapterTest {

    @Test
    void convertsRedisOutageToExplicitFailClosedError() {
        StringRedisTemplate redis = new StringRedisTemplate() {
            @Override
            public <HK, HV> HashOperations<String, HK, HV> opsForHash() {
                throw new QueryTimeoutException("redis down");
            }
        };
        RedisTokenCacheAdapter adapter = new RedisTokenCacheAdapter(redis);
        long now = Instant.now().getEpochSecond();

        assertThatThrownBy(() -> adapter.store(new TokenCachePort.OnlineSession(
                1, 2, "access", "refresh", 0, now + 60, now + 120, true)))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> org.assertj.core.api.Assertions.assertThat(
                        ((BusinessException) error).code()).isEqualTo(ErrorCode.EXTERNAL_CALL_FAILED))
                .hasMessageContaining("failed closed");
    }
}
