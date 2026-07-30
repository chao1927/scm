package com.chaobo.scm.supplier.application.workbench;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.supplier.infrastructure.persistence.workbench.SupplierWorkbenchMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SupplierWorkbenchApplicationServiceTest。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class SupplierWorkbenchApplicationServiceTest {

    /**
     * mapper（类型：{@code FakeWorkbenchMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final FakeWorkbenchMapper mapper = new FakeWorkbenchMapper();

    /**
     * service（类型：{@code SupplierWorkbenchApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierWorkbenchApplicationService service = new SupplierWorkbenchApplicationService(mapper);

    /**
     * 处理当前类型职责中的操作 {@code summaryUsesSupplierScopeAndAggregatesDashboardNumbers}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void summaryUsesSupplierScopeAndAggregatesDashboardNumbers() {
        var result = service.summary(9999L, 3001L, 30);
        assertThat(result.pendingQuotes()).isEqualTo(2);
        assertThat(result.pendingPurchaseOrderConfirms()).isEqualTo(3);
        assertThat(result.latestScore()).isEqualByComparingTo("88.50");
        assertThat(result.todoGroups()).extracting("type").containsExactly("QUOTE");
        assertThat(mapper.lastSupplierId).isEqualTo(3001L);
    }

    /**
     * 处理当前类型职责中的操作 {@code summaryRejectsInvalidRecentDays}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void summaryRejectsInvalidRecentDays() {
        assertThatThrownBy(() -> service.summary(null, null, 0)).isInstanceOf(BusinessException.class).hasMessageContaining("统计天数");
    }

    /**
     * FakeWorkbenchMapper。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static final class FakeWorkbenchMapper implements SupplierWorkbenchMapper {

        /**
         * lastSupplierId（类型：{@code Long}）。
         *
         * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
         */
        private Long lastSupplierId;

        /**
         * 处理当前类型职责中的操作 {@code pendingQuotes}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param supplierId 业务或技术标识，类型为 {@code Long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        @Override
        public long pendingQuotes(Long supplierId) {
            lastSupplierId = supplierId;
            return 2;
        }

        /**
         * 处理当前类型职责中的操作 {@code pendingPurchaseOrderConfirms}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param supplierId 业务或技术标识，类型为 {@code Long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        @Override
        public long pendingPurchaseOrderConfirms(Long supplierId) {
            return 3;
        }

        /**
         * 处理当前类型职责中的操作 {@code pendingAsns}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param supplierId 业务或技术标识，类型为 {@code Long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        @Override
        public long pendingAsns(Long supplierId) {
            return 4;
        }

        /**
         * 处理当前类型职责中的操作 {@code pendingReconciliations}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param supplierId 业务或技术标识，类型为 {@code Long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        @Override
        public long pendingReconciliations(Long supplierId) {
            return 5;
        }

        /**
         * 处理当前类型职责中的操作 {@code pendingRectifications}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param supplierId 业务或技术标识，类型为 {@code Long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        @Override
        public long pendingRectifications(Long supplierId) {
            return 6;
        }

        /**
         * 处理当前类型职责中的操作 {@code openWarnings}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param supplierId 业务或技术标识，类型为 {@code Long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        @Override
        public long openWarnings(Long supplierId) {
            return 7;
        }

        /**
         * 处理当前类型职责中的操作 {@code failedEvents}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        @Override
        public long failedEvents() {
            return 8;
        }

        /**
         * 处理当前类型职责中的操作 {@code openReturns}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param supplierId 业务或技术标识，类型为 {@code Long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        @Override
        public long openReturns(Long supplierId) {
            return 9;
        }

        /**
         * 处理当前类型职责中的操作 {@code latestScore}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param supplierId 业务或技术标识，类型为 {@code Long}
         * @param since 业务处理参数或成员，类型为 {@code OffsetDateTime}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
         */
        @Override
        public BigDecimal latestScore(Long supplierId, OffsetDateTime since) {
            return new BigDecimal("88.50");
        }

        /**
         * 转换数据模型 {@code todoGroups}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param supplierId 业务或技术标识，类型为 {@code Long}
         * @return 转换数据模型的结果，类型为 {@code List<SupplierWorkbenchView.TodoGroup>}
         */
        @Override
        public List<SupplierWorkbenchView.TodoGroup> todoGroups(Long supplierId) {
            return List.of(new SupplierWorkbenchView.TodoGroup("QUOTE", 2));
        }

        /**
         * 处理当前类型职责中的操作 {@code warningGroups}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param supplierId 业务或技术标识，类型为 {@code Long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<SupplierWorkbenchView.WarningGroup>}
         */
        @Override
        public List<SupplierWorkbenchView.WarningGroup> warningGroups(Long supplierId) {
            return List.of(new SupplierWorkbenchView.WarningGroup("预警", 7));
        }
    }
}
