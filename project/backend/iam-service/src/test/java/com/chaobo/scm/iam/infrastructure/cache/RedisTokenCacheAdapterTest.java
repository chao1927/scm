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
import static org.assertj.core.api.Assertions.assertThat;

class RedisTokenCacheAdapterTest {

    @Test
    void rejectsRefreshLookupThatBelongsToAnotherSessionBeforeRunningLua() {
        long now = Instant.now().getEpochSecond();
        TokenCachePort.OnlineSession replacement = new TokenCachePort.OnlineSession(
                42, 2, "access-next", "refresh-next", 1, now + 60, now + 120, true);

        assertThat(RedisTokenCacheAdapter.lookupBelongsToReplacement("41", replacement)).isFalse();
        assertThat(RedisTokenCacheAdapter.lookupBelongsToReplacement("42", replacement)).isTrue();
    }

    @Test
    void luaGuardsRefreshOwnershipSessionIdentityAndNextGenerationAtomically() {
        String script = RedisTokenCacheAdapter.rotationScriptText();

        assertThat(script)
                .contains("redis.call('GET', refreshLookupKey)")
                .contains("redis.call('HGET', sessionKey, 'sessionId')")
                .contains("redis.call('HGET', sessionKey, 'generation')")
                .contains("currentGeneration + 1 ~= requestedGeneration");
    }

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
