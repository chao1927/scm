package com.chaobo.scm.supplier.application.integration;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * IntegrationCommandRepository。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface IntegrationCommandRepository {

    /**
     * 执行命令 {@code save}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code IntegrationCommand}
     */
    void save(IntegrationCommand command);

    /**
     * 处理当前类型职责中的操作 {@code lockDispatchable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<IntegrationCommand>}
     */
    List<IntegrationCommand> lockDispatchable(int size);

    /**
     * 处理当前类型职责中的操作 {@code markExecuting}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    boolean markExecuting(long id);

    /**
     * 处理当前类型职责中的操作 {@code markSucceeded}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param reference 业务处理参数或成员，类型为 {@code String}
     */
    void markSucceeded(long id, String reference);

    /**
     * 处理当前类型职责中的操作 {@code markRetry}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param expectedRetry 业务处理参数或成员，类型为 {@code int}
     * @param nextRetry 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param maxRetries 业务处理参数或成员，类型为 {@code int}
     */
    void markRetry(long id, int expectedRetry, OffsetDateTime nextRetry, String reason, int maxRetries);

    /**
     * 执行命令 {@code retryManually}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    void retryManually(long id, String reason);
}
