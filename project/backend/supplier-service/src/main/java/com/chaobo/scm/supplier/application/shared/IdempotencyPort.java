package com.chaobo.scm.supplier.application.shared;

import java.time.Duration;
import java.util.Optional;

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
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Optional<StoredCommandResult>}
     */
    Optional<StoredCommandResult> find(String key);

    /**
     * 执行命令 {@code reserve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param requestHash 接口请求参数，类型为 {@code String}
     * @param ttl 业务处理参数或成员，类型为 {@code Duration}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    boolean reserve(String key, String requestHash, Duration ttl);

    /**
     * 执行命令 {@code complete}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param requestHash 接口请求参数，类型为 {@code String}
     * @param result 处理结果，类型为 {@code CommandResult}
     * @param ttl 业务处理参数或成员，类型为 {@code Duration}
     */
    void complete(String key, String requestHash, CommandResult result, Duration ttl);

    /**
     * 执行命令 {@code release}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param requestHash 接口请求参数，类型为 {@code String}
     */
    void release(String key, String requestHash);

    /**
     * StoredCommandResult。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record StoredCommandResult(String requestHash, CommandResult result) {
    }
}
