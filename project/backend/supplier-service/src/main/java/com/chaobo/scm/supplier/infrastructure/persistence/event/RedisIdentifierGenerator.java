package com.chaobo.scm.supplier.infrastructure.persistence.event;

import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * RedisIdentifierGenerator。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class RedisIdentifierGenerator implements IdentifierGenerator {

    /**
     * BUSINESS_TIME（类型：{@code DateTimeFormatter}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final DateTimeFormatter BUSINESS_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * redisTemplate（类型：{@code StringRedisTemplate}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final StringRedisTemplate redisTemplate;

    /**
     * 创建 RedisIdentifierGenerator。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param redisTemplate 业务处理参数或成员，类型为 {@code StringRedisTemplate}
     */
    public RedisIdentifierGenerator(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 处理当前类型职责中的操作 {@code nextId}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Override
    public long nextId() {
        Long sequence = redisTemplate.opsForValue().increment("scm:id:sequence");
        if (sequence == null) {
            throw new IllegalStateException("Redis ID 序列生成失败");
        }
        return sequence.longValue();
    }

    /**
     * 处理当前类型职责中的操作 {@code nextBusinessNo}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param prefix 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    @Override
    public String nextBusinessNo(String prefix) {
        return prefix + LocalDateTime.now().format(BUSINESS_TIME) + String.format("%08d", nextId() % 100_000_000);
    }
}
