package com.chaobo.scm.iam.infrastructure.cache;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.iam.application.TokenCachePort;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Redis-backed online session registry. There is intentionally no fallback adapter.
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class RedisTokenCacheAdapter implements TokenCachePort {

    private static final String SESSION_PREFIX = "scm:iam:session:";
    private static final String ACCESS_PREFIX = "scm:iam:access:";
    private static final String REFRESH_PREFIX = "scm:iam:refresh:";

    private static final DefaultRedisScript<Long> ROTATE_SCRIPT = new DefaultRedisScript<>("""
            local sessionKey = KEYS[1]
            local refreshLookupKey = KEYS[2]
            if redis.call('EXISTS', sessionKey) == 0 then return 0 end
            if redis.call('HGET', sessionKey, 'active') ~= 'true' then return 0 end
            local expectedSessionId = ARGV[10]
            local lookupOwner = redis.call('GET', refreshLookupKey)
            if lookupOwner ~= expectedSessionId then return -2 end
            if redis.call('HGET', sessionKey, 'sessionId') ~= expectedSessionId then return -2 end
            if redis.call('HGET', sessionKey, 'userId') ~= ARGV[11] then return -2 end
            local currentGeneration = tonumber(redis.call('HGET', sessionKey, 'generation'))
            local requestedGeneration = tonumber(ARGV[4])
            if currentGeneration == nil or requestedGeneration == nil or currentGeneration + 1 ~= requestedGeneration then
              return -2
            end
            local currentRefresh = redis.call('HGET', sessionKey, 'refreshJti')
            if currentRefresh ~= ARGV[1] then
              local currentAccess = redis.call('HGET', sessionKey, 'accessJti')
              redis.call('HSET', sessionKey, 'active', 'false')
              if currentAccess then redis.call('DEL', ARGV[8] .. currentAccess) end
              if currentRefresh then redis.call('DEL', ARGV[9] .. currentRefresh) end
              return -1
            end
            local oldAccess = redis.call('HGET', sessionKey, 'accessJti')
            if oldAccess then redis.call('DEL', ARGV[8] .. oldAccess) end
            redis.call('HSET', sessionKey,
              'accessJti', ARGV[2], 'refreshJti', ARGV[3], 'generation', ARGV[4],
              'accessExpiresAt', ARGV[5], 'refreshExpiresAt', ARGV[6], 'active', 'true')
            redis.call('EXPIRE', sessionKey, ARGV[7])
            redis.call('SETEX', ARGV[8] .. ARGV[2], ARGV[7], ARGV[10])
            redis.call('SETEX', ARGV[9] .. ARGV[3], ARGV[7], ARGV[10])
            return 1
            """, Long.class);

    private final StringRedisTemplate redis;

    public RedisTokenCacheAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void store(OnlineSession session) {
        execute(() -> {
            String sessionKey = sessionKey(session.sessionId());
            redis.opsForHash().putAll(sessionKey, values(session));
            Duration ttl = ttl(session.refreshExpiresAtEpochSecond());
            redis.expire(sessionKey, ttl);
            redis.opsForValue().set(accessKey(session.accessJti()), Long.toString(session.sessionId()), ttl);
            redis.opsForValue().set(refreshKey(session.refreshJti()), Long.toString(session.sessionId()), ttl);
            return null;
        });
    }

    @Override
    public Optional<OnlineSession> findByAccessJti(String accessJti) {
        return find(accessKey(accessJti), accessJti, true);
    }

    @Override
    public Optional<OnlineSession> findByRefreshJti(String refreshJti) {
        return find(refreshKey(refreshJti), refreshJti, false);
    }

    @Override
    public RotationResult rotate(String presentedRefreshJti, OnlineSession replacement) {
        return execute(() -> {
            String sessionId = redis.opsForValue().get(refreshKey(presentedRefreshJti));
            if (sessionId == null) {
                return RotationResult.NOT_FOUND;
            }
            if (!lookupBelongsToReplacement(sessionId, replacement)) {
                return RotationResult.REPLAY_DETECTED;
            }
            long ttl = Math.max(1, replacement.refreshExpiresAtEpochSecond() - Instant.now().getEpochSecond());
            Long result = redis.execute(ROTATE_SCRIPT,
                    List.of(sessionKey(replacement.sessionId()), refreshKey(presentedRefreshJti)),
                    presentedRefreshJti, replacement.accessJti(), replacement.refreshJti(),
                    Long.toString(replacement.generation()),
                    Long.toString(replacement.accessExpiresAtEpochSecond()),
                    Long.toString(replacement.refreshExpiresAtEpochSecond()), Long.toString(ttl),
                    ACCESS_PREFIX, REFRESH_PREFIX, sessionId, Long.toString(replacement.userId()));
            if (result == null || result == 0) {
                return RotationResult.NOT_FOUND;
            }
            return result < 0 ? RotationResult.REPLAY_DETECTED : RotationResult.ROTATED;
        });
    }

    @Override
    public void revoke(long sessionId) {
        execute(() -> {
            String key = sessionKey(sessionId);
            Map<Object, Object> session = redis.opsForHash().entries(key);
            if (session.isEmpty()) {
                return null;
            }
            redis.opsForHash().put(key, "active", "false");
            redis.delete(accessKey(text(session, "accessJti")));
            redis.delete(refreshKey(text(session, "refreshJti")));
            return null;
        });
    }

    private Optional<OnlineSession> find(String lookupKey, String presentedJti, boolean access) {
        return execute(() -> {
            String sessionId = redis.opsForValue().get(lookupKey);
            if (sessionId == null) {
                return Optional.empty();
            }
            Map<Object, Object> values = redis.opsForHash().entries(sessionKey(Long.parseLong(sessionId)));
            if (values.isEmpty()) {
                return Optional.empty();
            }
            OnlineSession session = from(values);
            String current = access ? session.accessJti() : session.refreshJti();
            if (!presentedJti.equals(current) && access) {
                return Optional.empty();
            }
            return Optional.of(session);
        });
    }

    private static Map<String, String> values(OnlineSession session) {
        return Map.of("sessionId", Long.toString(session.sessionId()),
                "userId", Long.toString(session.userId()), "accessJti", session.accessJti(),
                "refreshJti", session.refreshJti(), "generation", Long.toString(session.generation()),
                "accessExpiresAt", Long.toString(session.accessExpiresAtEpochSecond()),
                "refreshExpiresAt", Long.toString(session.refreshExpiresAtEpochSecond()),
                "active", Boolean.toString(session.active()));
    }

    private static OnlineSession from(Map<Object, Object> values) {
        return new OnlineSession(Long.parseLong(text(values, "sessionId")),
                Long.parseLong(text(values, "userId")), text(values, "accessJti"),
                text(values, "refreshJti"), Long.parseLong(text(values, "generation")),
                Long.parseLong(text(values, "accessExpiresAt")),
                Long.parseLong(text(values, "refreshExpiresAt")),
                Boolean.parseBoolean(text(values, "active")));
    }

    private static String text(Map<Object, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? "" : value.toString();
    }

    private static Duration ttl(long expiresAtEpochSecond) {
        return Duration.ofSeconds(Math.max(1, expiresAtEpochSecond - Instant.now().getEpochSecond()));
    }

    static String rotationScriptText() {
        return ROTATE_SCRIPT.getScriptAsString();
    }

    static boolean lookupBelongsToReplacement(String lookupSessionId, OnlineSession replacement) {
        return lookupSessionId != null
                && lookupSessionId.equals(Long.toString(replacement.sessionId()));
    }

    private static String sessionKey(long sessionId) { return SESSION_PREFIX + sessionId; }
    private static String accessKey(String jti) { return ACCESS_PREFIX + jti; }
    private static String refreshKey(String jti) { return REFRESH_PREFIX + jti; }

    private static <T> T execute(CacheOperation<T> operation) {
        try {
            return operation.execute();
        } catch (DataAccessException | IllegalStateException exception) {
            throw new BusinessException(ErrorCode.EXTERNAL_CALL_FAILED,
                    "Redis TokenCache unavailable; authentication failed closed");
        }
    }

    @FunctionalInterface
    private interface CacheOperation<T> {

        /**
         * Executes one fail-closed Redis cache operation.
         *
         * @return operation result
         */
        T execute();
    }
}
