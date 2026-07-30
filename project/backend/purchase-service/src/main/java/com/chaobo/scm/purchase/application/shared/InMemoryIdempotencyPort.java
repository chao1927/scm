package com.chaobo.scm.purchase.application.shared;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * InMemoryIdempotencyPort。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。定义跨进程或跨层协作端口，隔离调用方与具体技术实现。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class InMemoryIdempotencyPort implements IdempotencyPort {

    /**
     * cache（类型：{@code Map<String,CommandResult>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final Map<String, CommandResult> cache = new ConcurrentHashMap<>();

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Optional<CommandResult>}
     */
    @Override
    public Optional<CommandResult> find(String businessType, String idempotencyKey) {
        return Optional.ofNullable(cache.get(key(businessType, idempotencyKey)));
    }

    /**
     * 处理当前类型职责中的操作 {@code execute}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param action 业务处理参数或成员，类型为 {@code Supplier<CommandResult>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Override
    public CommandResult execute(String businessType, CommandContext context, Supplier<CommandResult> action) {
        var key = key(businessType, context.requiredIdempotencyKey());
        var existing = cache.get(key);
        if (existing != null) {
            return new CommandResult(existing.id(), existing.businessNo(), existing.status(), existing.statusName(), existing.version(), existing.eventCode(), true);
        }
        var result = action.get();
        cache.put(key, result);
        return result;
    }

    /**
     * 处理当前类型职责中的操作 {@code key}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String key(String businessType, String idempotencyKey) {
        return businessType + ":" + idempotencyKey;
    }
}
