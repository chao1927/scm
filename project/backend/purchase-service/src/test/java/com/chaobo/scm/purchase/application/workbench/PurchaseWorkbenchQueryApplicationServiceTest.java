package com.chaobo.scm.purchase.application.workbench;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.security.ScmAccessContext;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 采购工作台查询应用服务测试。
 *
 * <p>验证功能权限、采购组织、采购组和本人数据范围，以及分页排序白名单。
 */
class PurchaseWorkbenchQueryApplicationServiceTest {

    @Test
    void queriesOrganizationSummaryAndKeepsMetricFactTrace() {
        var port = new CapturingReadModel();
        port.metrics = List.of(
                new PurchaseWorkbenchMetricView("TODO", "REQUISITION_APPROVAL", "待审批请购",
                        2L, "purchase_requisition", "/purchase/requisition-approvals"),
                new PurchaseWorkbenchMetricView("EXCEPTION", "INBOUND_EXCEPTION", "到货异常",
                        1L, "purchase_inbound_tracking", "/purchase/inbound-tracks")
        );
        var service = new PurchaseWorkbenchQueryApplicationService(port);
        var access = access(Set.of("purchase:workbench:read"),
                Map.of("PURCHASE_ORG", Set.of("1001", "1002")));

        var result = service.summary(
                new PurchaseWorkbenchQueries.SummaryQuery(
                        1001L, null, "ORGANIZATION", null, null),
                access
        );

        assertThat(port.summaryCriteria.scope().purchaseOrgIds()).containsExactly(1001L);
        assertThat(port.summaryCriteria.scope().unrestrictedOrganizations()).isFalse();
        assertThat(result.pendingApprovalCount()).isEqualTo(2L);
        assertThat(result.inboundExceptionCount()).isEqualTo(1L);
        assertThat(result.metrics()).extracting(PurchaseWorkbenchMetricView::factSource)
                .containsExactly("purchase_requisition", "purchase_inbound_tracking");
    }

    @Test
    void appliesPurchaseGroupAndSelfScopeWithoutTrustingRequestIdentity() {
        var port = new CapturingReadModel();
        var service = new PurchaseWorkbenchQueryApplicationService(port);
        var access = access(Set.of("purchase:workbench:read"),
                Map.of(
                        "PURCHASE_ORG", Set.of("1001"),
                        "PURCHASE_GROUP", Set.of("7001")
                ));

        service.todos(new PurchaseWorkbenchQueries.TodoPageQuery(
                1001L, 7001L, "PURCHASE_GROUP", null, null,
                null, 1, 20, "dueDate", "asc"), access);
        assertThat(port.todoCriteria.scope().purchaseGroupId()).isEqualTo(7001L);
        assertThat(port.todoCriteria.scope().ownerId()).isNull();

        service.todos(new PurchaseWorkbenchQueries.TodoPageQuery(
                1001L, null, "SELF", null, null,
                null, 1, 20, "updatedAt", "desc"), access);
        assertThat(port.todoCriteria.scope().ownerId()).isEqualTo(42L);
        assertThat(port.todoCriteria.scope().purchaseGroupId()).isNull();
    }

    @Test
    void rejectsMissingPermissionForgedScopesAndUnsafeSortValues() {
        var service = new PurchaseWorkbenchQueryApplicationService(new CapturingReadModel());

        assertThatThrownBy(() -> service.summary(
                new PurchaseWorkbenchQueries.SummaryQuery(1001L, null, "ORGANIZATION", null, null),
                access(Set.of("purchase:po:read"), Map.of("PURCHASE_ORG", Set.of("1001")))))
                .isInstanceOf(BusinessException.class);

        assertThatThrownBy(() -> service.todos(
                new PurchaseWorkbenchQueries.TodoPageQuery(
                        9999L, null, "ORGANIZATION", null, null,
                        null, 1, 20, "updatedAt", "desc"),
                access(Set.of("purchase:workbench:read"), Map.of("PURCHASE_ORG", Set.of("1001")))))
                .isInstanceOf(BusinessException.class);

        assertThatThrownBy(() -> service.todos(
                new PurchaseWorkbenchQueries.TodoPageQuery(
                        1001L, null, "ORGANIZATION", null, null,
                        null, 1, 20, "updated_at desc; drop table purchase_order", "desc"),
                access(Set.of("purchase:workbench:read"), Map.of("PURCHASE_ORG", Set.of("1001")))))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void validatesPageAndTimeRangeBeforeCallingReadModel() {
        var port = new CapturingReadModel();
        var service = new PurchaseWorkbenchQueryApplicationService(port);
        var access = access(Set.of("*"), Map.of("PURCHASE_ORG", Set.of("*")));
        var from = OffsetDateTime.parse("2026-07-31T00:00:00+08:00");
        var to = OffsetDateTime.parse("2026-07-30T00:00:00+08:00");

        assertThatThrownBy(() -> service.todos(
                new PurchaseWorkbenchQueries.TodoPageQuery(
                        null, null, "ORGANIZATION", null, null,
                        null, 1, 51, "updatedAt", "desc"), access))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.summary(
                new PurchaseWorkbenchQueries.SummaryQuery(null, null, "ORGANIZATION", from, to),
                access))
                .isInstanceOf(BusinessException.class);
        assertThat(port.summaryCriteria).isNull();
        assertThat(port.todoCriteria).isNull();
    }

    private static ScmAccessContext access(Set<String> permissions,
                                           Map<String, Set<String>> scopes) {
        return new ScmAccessContext(42L, "buyer", "SCM", permissions, scopes);
    }

    private static final class CapturingReadModel implements PurchaseWorkbenchReadModelPort {

        private PurchaseWorkbenchReadCriteria summaryCriteria;
        private PurchaseTodoReadCriteria todoCriteria;
        private List<PurchaseWorkbenchMetricView> metrics = List.of();

        @Override
        public List<PurchaseWorkbenchMetricView> summarize(PurchaseWorkbenchReadCriteria criteria) {
            summaryCriteria = criteria;
            return metrics;
        }

        @Override
        public PageResult<PurchaseTodoView> pageTodos(PurchaseTodoReadCriteria criteria) {
            todoCriteria = criteria;
            return new PageResult<>(criteria.pageNo(), criteria.pageSize(), 0L, List.of());
        }
    }
}
