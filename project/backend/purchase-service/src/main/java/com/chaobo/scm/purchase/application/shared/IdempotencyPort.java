package com.chaobo.scm.purchase.application.shared;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * IdempotencyPort。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。定义跨进程或跨层协作端口，隔离调用方与具体技术实现。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface IdempotencyPort {

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Optional<CommandResult>}
     */
    Optional<CommandResult> find(String businessType, String idempotencyKey);

    /**
     * 处理当前类型职责中的操作 {@code execute}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param action 业务处理参数或成员，类型为 {@code Supplier<CommandResult>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    CommandResult execute(String businessType, CommandContext context, Supplier<CommandResult> action);
}
