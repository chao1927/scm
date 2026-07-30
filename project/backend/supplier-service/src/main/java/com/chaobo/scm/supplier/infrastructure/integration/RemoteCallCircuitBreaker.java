package com.chaobo.scm.supplier.infrastructure.integration;

import com.chaobo.scm.common.error.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * RemoteCallCircuitBreaker。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class RemoteCallCircuitBreaker {

    /**
     * threshold（类型：{@code int}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final int threshold;

    /**
     * openDuration（类型：{@code Duration}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final Duration openDuration;

    /**
     * states（类型：{@code ConcurrentHashMap<String,State>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ConcurrentHashMap<String, State> states = new ConcurrentHashMap<>();

    /**
     * 创建 RemoteCallCircuitBreaker。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param threshold 业务处理参数或成员，类型为 {@code int}
     * @param seconds 业务处理参数或成员，类型为 {@code long}
     */
    public RemoteCallCircuitBreaker(@Value("${scm.integration.circuit.failure-threshold:5}") int threshold, @Value("${scm.integration.circuit.open-seconds:30}") long seconds) {
        this.threshold = threshold;
        this.openDuration = Duration.ofSeconds(seconds);
    }

    /**
     * 处理当前类型职责中的操作 {@code execute}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param target 业务处理参数或成员，类型为 {@code String}
     * @param action 业务处理参数或成员，类型为 {@code Supplier<T>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code T}
     */
    public <T> T execute(String target, Supplier<T> action) {
        var state = states.computeIfAbsent(target, k -> new State());
        synchronized (state) {
            if (state.openedAt != null && state.openedAt.plus(openDuration).isAfter(Instant.now())) {
                throw new BusinessException(ErrorCode.EXTERNAL_CALL_FAILED, target + "远程调用已熔断");
            }
            if (state.openedAt != null) {
                state.openedAt = null;
            }
        }
        try {
            T result = action.get();
            synchronized (state) {
                state.failures = 0;
                state.openedAt = null;
            }
            return result;
        } catch (RuntimeException e) {
            synchronized (state) {
                if (++state.failures >= threshold) {
                    state.openedAt = Instant.now();
                }
            }
            throw e;
        }
    }

    /**
     * State。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static final class State {

        /**
         * failures（类型：{@code int}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        int failures;

        /**
         * openedAt（类型：{@code Instant}）。
         *
         * <p>保存当前对象所需的业务时间；其具体生命周期由所属对象统一管理。
         */
        Instant openedAt;
    }
}
