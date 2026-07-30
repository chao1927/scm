package com.chaobo.scm.supplier.infrastructure.persistence.event;

import com.chaobo.scm.supplier.application.shared.CommandResult;
import com.chaobo.scm.supplier.application.shared.IdempotencyPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * RedisIdempotencyAdapter。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class RedisIdempotencyAdapter implements IdempotencyPort {

    /**
     * redisTemplate（类型：{@code StringRedisTemplate}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final StringRedisTemplate redisTemplate;

    /**
     * 创建 RedisIdempotencyAdapter。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param redisTemplate 业务处理参数或成员，类型为 {@code StringRedisTemplate}
     */
    public RedisIdempotencyAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Optional<StoredCommandResult>}
     */
    @Override
    public Optional<StoredCommandResult> find(String key) {
        Map<Object, Object> values = redisTemplate.opsForHash().entries(key);
        if (values.isEmpty()) {
            return Optional.empty();
        }
        String hash = text(values, "requestHash");
        if (!COMPLETED.equals(text(values, STATE))) {
            return Optional.of(new StoredCommandResult(hash, null));
        }
        CommandResult result = new CommandResult(Long.parseLong(text(values, "aggregateId")), text(values, "businessNo"), Integer.parseInt(text(values, "status")), text(values, "statusName"), Integer.parseInt(text(values, "version")), text(values, "eventCode"), true);
        return Optional.of(new StoredCommandResult(hash, result));
    }

    /**
     * 执行命令 {@code reserve}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param requestHash 接口请求参数，类型为 {@code String}
     * @param ttl 业务处理参数或成员，类型为 {@code Duration}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
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

    /**
     * 执行命令 {@code complete}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param requestHash 接口请求参数，类型为 {@code String}
     * @param result 处理结果，类型为 {@code CommandResult}
     * @param ttl 业务处理参数或成员，类型为 {@code Duration}
     */
    @Override
    public void complete(String key, String requestHash, CommandResult result, Duration ttl) {
        redisTemplate.opsForHash().putAll(key, Map.of("requestHash", requestHash, "state", "COMPLETED", "aggregateId", Long.toString(result.aggregateId()), "businessNo", result.businessNo(), "status", Integer.toString(result.status()), "statusName", result.statusName(), "version", Integer.toString(result.version()), "eventCode", result.eventCode() == null ? "" : result.eventCode()));
        redisTemplate.expire(key, ttl);
        redisTemplate.delete(key + ":lock");
    }

    /**
     * 执行命令 {@code release}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param requestHash 接口请求参数，类型为 {@code String}
     */
    @Override
    public void release(String key, String requestHash) {
        Object storedHash = redisTemplate.opsForHash().get(key, "requestHash");
        if (requestHash.equals(storedHash)) {
            redisTemplate.delete(key);
            redisTemplate.delete(key + ":lock");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code text}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param values 业务处理参数或成员，类型为 {@code Map<Object,Object>}
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String text(Map<Object, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? "" : value.toString();
    }

    /**
     * 业务常量 {@code COMPLETED}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String COMPLETED = "COMPLETED";

    /**
     * 业务常量 {@code STATE}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String STATE = "state";
}
