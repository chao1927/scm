package com.chaobo.scm.purchase.application.integration;

import com.chaobo.scm.purchase.infrastructure.persistence.integration.IntegrationCommandMapper;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;
import java.time.OffsetDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * PurchaseIntegrationCommandDispatcherTest。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class PurchaseIntegrationCommandDispatcherTest {

    /**
     * 处理当前类型职责中的操作 {@code successfulGatewayReceiptCompletesCommand}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void successfulGatewayReceiptCompletesCommand() {
        MemoryMapper mapper = new MemoryMapper(row(0));
        PurchaseIntegrationCommandDispatcher dispatcher = dispatcher(mapper, command -> new IntegrationCommandGateway.DispatchReceipt("IM-100"), 3);
        dispatcher.dispatch();
        assertThat(mapper.successReference).isEqualTo("IM-100");
        assertThat(mapper.retryReason).isNull();
    }

    /**
     * 处理当前类型职责中的操作 {@code gatewayFailureSchedulesRetryAndEventuallyMarksFinalFailure}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void gatewayFailureSchedulesRetryAndEventuallyMarksFinalFailure() {
        MemoryMapper mapper = new MemoryMapper(row(2));
        PurchaseIntegrationCommandDispatcher dispatcher = dispatcher(mapper, command -> {
            throw new IllegalStateException("timeout");
        }, 3);
        dispatcher.dispatch();
        assertThat(mapper.retryReason).isEqualTo("timeout");
        assertThat(mapper.finalFailure).isTrue();
        assertThat(mapper.nextRetryAt).isAfter(OffsetDateTime.now());
    }

    /**
     * 执行命令 {@code dispatcher}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param mapper 持久化访问依赖，类型为 {@code MemoryMapper}
     * @param gateway 应用或外部协作依赖，类型为 {@code IntegrationCommandGateway}
     * @param maxRetries 业务处理参数或成员，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code PurchaseIntegrationCommandDispatcher}
     */
    private static PurchaseIntegrationCommandDispatcher dispatcher(MemoryMapper mapper, IntegrationCommandGateway gateway, int maxRetries) {
        PlatformTransactionManager manager = new PlatformTransactionManager() {

            /**
             * 查询并返回 {@code getTransaction}。
             *
             * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
             * @param definition 业务处理参数或成员，类型为 {@code TransactionDefinition}
             * @return 查询并返回的结果，类型为 {@code TransactionStatus}
             */
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) {
                return new SimpleTransactionStatus();
            }

            /**
             * 处理当前类型职责中的操作 {@code commit}。
             *
             * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
             * @param status 生命周期状态，类型为 {@code TransactionStatus}
             */
            @Override
            public void commit(TransactionStatus status) {
            }

            /**
             * 处理当前类型职责中的操作 {@code rollback}。
             *
             * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
             * @param status 生命周期状态，类型为 {@code TransactionStatus}
             */
            @Override
            public void rollback(TransactionStatus status) {
            }
        };
        return new PurchaseIntegrationCommandDispatcher(mapper, gateway, manager, 10, maxRetries);
    }

    /**
     * 处理当前类型职责中的操作 {@code row}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param retryCount 数量值，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code IntegrationCommandMapper.CommandRow}
     */
    private static IntegrationCommandMapper.CommandRow row(int retryCount) {
        return new IntegrationCommandMapper.CommandRow(1, "CreateInboundOrder", "WMS", "PURCHASE_ORDER", "10", "PO-10", "{}", 1, retryCount);
    }

    /**
     * MemoryMapper。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static final class MemoryMapper implements IntegrationCommandMapper {

        /**
         * row（类型：{@code CommandRow}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final CommandRow row;

        /**
         * successReference（类型：{@code String}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        String successReference;

        /**
         * retryReason（类型：{@code String}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        String retryReason;

        /**
         * nextRetryAt（类型：{@code OffsetDateTime}）。
         *
         * <p>保存当前对象所需的业务时间；其具体生命周期由所属对象统一管理。
         */
        OffsetDateTime nextRetryAt;

        /**
         * finalFailure（类型：{@code boolean}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        boolean finalFailure;

        /**
         * 创建 MemoryMapper。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param row 业务处理参数或成员，类型为 {@code CommandRow}
         */
        MemoryMapper(CommandRow row) {
            this.row = row;
        }

        /**
         * 处理当前类型职责中的操作 {@code insert}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param commandId 业务或技术标识，类型为 {@code long}
         * @param commandType 用例输入命令，类型为 {@code String}
         * @param targetSystem 业务处理参数或成员，类型为 {@code String}
         * @param businessType 业务处理参数或成员，类型为 {@code String}
         * @param businessId 业务或技术标识，类型为 {@code String}
         * @param businessNo 可追踪业务编码，类型为 {@code String}
         * @param payloadJson 业务处理参数或成员，类型为 {@code String}
         */
        @Override
        public void insert(long commandId, String commandType, String targetSystem, String businessType, String businessId, String businessNo, String payloadJson) {
        }

        /**
         * 处理当前类型职责中的操作 {@code lockDispatchable}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param size 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<CommandRow>}
         */
        @Override
        public List<CommandRow> lockDispatchable(int size) {
            return List.of(row);
        }

        /**
         * 处理当前类型职责中的操作 {@code markExecuting}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        @Override
        public int markExecuting(long id) {
            return 1;
        }

        /**
         * 处理当前类型职责中的操作 {@code markSucceeded}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param reference 业务处理参数或成员，类型为 {@code String}
         */
        @Override
        public void markSucceeded(long id, String reference) {
            successReference = reference;
        }

        /**
         * 处理当前类型职责中的操作 {@code markRetry}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param expected 业务处理参数或成员，类型为 {@code int}
         * @param next 业务处理参数或成员，类型为 {@code OffsetDateTime}
         * @param reason 业务处理参数或成员，类型为 {@code String}
         * @param max 业务处理参数或成员，类型为 {@code int}
         */
        @Override
        public void markRetry(long id, int expected, OffsetDateTime next, String reason, int max) {
            retryReason = reason;
            nextRetryAt = next;
            finalFailure = expected + 1 >= max;
        }
    }
}
