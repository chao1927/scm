package com.chaobo.scm.purchase.application.integration;

import com.chaobo.scm.purchase.infrastructure.persistence.integration.IntegrationCommandMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * PurchaseIntegrationCommandDispatcher。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class PurchaseIntegrationCommandDispatcher {

    /**
     * mapper（类型：{@code IntegrationCommandMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final IntegrationCommandMapper mapper;

    /**
     * gateway（类型：{@code IntegrationCommandGateway}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final IntegrationCommandGateway gateway;

    /**
     * tx（类型：{@code TransactionTemplate}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final TransactionTemplate tx;

    /**
     * batchSize（类型：{@code int}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final int batchSize;

    /**
     * maxRetries（类型：{@code int}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final int maxRetries;

    /**
     * 创建 PurchaseIntegrationCommandDispatcher。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code IntegrationCommandMapper}
     * @param gateway 应用或外部协作依赖，类型为 {@code IntegrationCommandGateway}
     * @param manager 业务处理参数或成员，类型为 {@code PlatformTransactionManager}
     * @param batchSize 业务处理参数或成员，类型为 {@code int}
     * @param maxRetries 业务处理参数或成员，类型为 {@code int}
     */
    public PurchaseIntegrationCommandDispatcher(IntegrationCommandMapper mapper, IntegrationCommandGateway gateway, PlatformTransactionManager manager, @Value("${scm.integration.batch-size:50}") int batchSize, @Value("${scm.integration.max-retries:8}") int maxRetries) {
        this.mapper = mapper;
        this.gateway = gateway;
        this.tx = new TransactionTemplate(manager);
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
    }

    /**
     * 执行命令 {@code dispatch}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Scheduled(fixedDelayString = "${scm.integration.fixed-delay:1000}")
    public void dispatch() {
        List<IntegrationCommandMapper.CommandRow> rows = tx.execute(status -> mapper.lockDispatchable(batchSize).stream().filter(row -> mapper.markExecuting(row.commandId()) == 1).toList());
        if (rows != null) {
            rows.forEach(this::send);
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code send}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code IntegrationCommandMapper.CommandRow}
     */
    void send(IntegrationCommandMapper.CommandRow row) {
        try {
            IntegrationCommandGateway.DispatchReceipt receipt = gateway.dispatch(row);
            tx.executeWithoutResult(status -> mapper.markSucceeded(row.commandId(), receipt.remoteReference()));
        } catch (RuntimeException exception) {
            String reason = message(exception);
            OffsetDateTime next = OffsetDateTime.now().plusSeconds(backoffSeconds(row.retryCount()));
            tx.executeWithoutResult(status -> mapper.markRetry(row.commandId(), row.retryCount(), next, reason, maxRetries));
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code backoffSeconds}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param retryCount 数量值，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    private static long backoffSeconds(int retryCount) {
        return Math.min(300, 1L << Math.min(8, Math.max(0, retryCount)));
    }

    /**
     * 处理当前类型职责中的操作 {@code message}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param exception 业务处理参数或成员，类型为 {@code RuntimeException}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String message(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.substring(0, Math.min(1000, message.length()));
    }
}
