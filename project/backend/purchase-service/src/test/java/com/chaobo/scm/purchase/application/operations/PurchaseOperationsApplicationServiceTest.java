package com.chaobo.scm.purchase.application.operations;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import com.chaobo.scm.purchase.infrastructure.persistence.integration.PurchaseOperationsMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PurchaseOperationsApplicationServiceTest。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class PurchaseOperationsApplicationServiceTest {

    /**
     * 处理当前类型职责中的操作 {@code finalFailedCommandRequiresPermissionAndCanBeReplayed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void finalFailedCommandRequiresPermissionAndCanBeReplayed() {
        MemoryMapper mapper = new MemoryMapper();
        PurchaseOperationsApplicationService service = new PurchaseOperationsApplicationService(mapper);
        assertThatThrownBy(() -> service.replayCommand(1, context(Set.of("purchase:po:read")))).isInstanceOf(BusinessException.class);
        service.replayCommand(1, context(Set.of("purchase:integration-command:replay")));
        assertThat(mapper.replayed).isTrue();
    }

    /**
     * 处理当前类型职责中的操作 {@code context}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param permissions 业务处理参数或成员，类型为 {@code Set<String>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandContext}
     */
    private static CommandContext context(Set<String> permissions) {
        return new CommandContext(1, "operator", 1, 1L, "request", "trace", "replay-1",
                permissions, "replay-request-digest");
    }

    /**
     * MemoryMapper。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static final class MemoryMapper implements PurchaseOperationsMapper {

        /**
         * replayed（类型：{@code boolean}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        boolean replayed;

        /**
         * 处理当前类型职责中的操作 {@code failedInboundEvents}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PurchaseOperationsViews.FailedEvent>}
         */
        @Override
        public List<PurchaseOperationsViews.FailedEvent> failedInboundEvents(int limit) {
            return List.of();
        }

        /**
         * 处理当前类型职责中的操作 {@code failedCommands}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PurchaseOperationsViews.FailedCommand>}
         */
        @Override
        public List<PurchaseOperationsViews.FailedCommand> failedCommands(int limit) {
            return List.of();
        }

        /**
         * 执行命令 {@code replayCommand}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param commandId 业务或技术标识，类型为 {@code long}
         * @return 执行命令的结果，类型为 {@code int}
         */
        @Override
        public int replayCommand(long commandId) {
            replayed = true;
            return 1;
        }
    }
}
