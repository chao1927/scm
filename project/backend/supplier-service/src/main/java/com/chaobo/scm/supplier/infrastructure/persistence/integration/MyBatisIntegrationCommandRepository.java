package com.chaobo.scm.supplier.infrastructure.persistence.integration;

import com.chaobo.scm.supplier.application.integration.*;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * MyBatisIntegrationCommandRepository。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisIntegrationCommandRepository implements IntegrationCommandRepository {

    /**
     * mapper（类型：{@code IntegrationCommandMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final IntegrationCommandMapper mapper;

    /**
     * 创建 MyBatisIntegrationCommandRepository。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code IntegrationCommandMapper}
     */
    public MyBatisIntegrationCommandRepository(IntegrationCommandMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param c 业务处理参数或成员，类型为 {@code IntegrationCommand}
     */
    public void save(IntegrationCommand c) {
        mapper.insert(c);
    }

    /**
     * 处理当前类型职责中的操作 {@code lockDispatchable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<IntegrationCommand>}
     */
    public List<IntegrationCommand> lockDispatchable(int size) {
        return mapper.lockDispatchable(size);
    }

    /**
     * 处理当前类型职责中的操作 {@code markExecuting}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    public boolean markExecuting(long id) {
        return mapper.markExecuting(id) == 1;
    }

    /**
     * 处理当前类型职责中的操作 {@code markSucceeded}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param reference 业务处理参数或成员，类型为 {@code String}
     */
    public void markSucceeded(long id, String reference) {
        mapper.markSucceeded(id, reference);
    }

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
    public void markRetry(long id, int expectedRetry, OffsetDateTime nextRetry, String reason, int maxRetries) {
        mapper.markRetry(id, expectedRetry, nextRetry, reason, maxRetries);
    }

    /**
     * 执行命令 {@code retryManually}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    public void retryManually(long id, String reason) {
        mapper.retryManually(id, reason);
    }
}
