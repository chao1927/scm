package com.chaobo.scm.oms.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.oms.infrastructure.persistence.OmsOperationsQueryMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OMS 运营读模型的数据权限和无副作用查询测试。
 */
class OmsOperationsQueryApplicationServiceTest {

    @Test
    void auditListUsesOrganizationAndOwnerScopesAndKeyword() {
        LocalDateTime now = LocalDateTime.now();
        var allowed = new OmsOperationsQueryMapper.AuditRow(
                "1", "SO-ALLOWED", 10L, 20L, "TMALL", 88L,
                "ORDER_REVIEW", "PASSED", null, null, 2,
                1L, now, now, now);
        var denied = new OmsOperationsQueryMapper.AuditRow(
                "2", "SO-DENIED", 11L, 20L, "JD", 99L,
                "ORDER_REVIEW", "BLOCKED", null, "风控命中", 1,
                null, null, now, now);
        var service = new OmsOperationsQueryApplicationService(mapper(
                Map.of("listAudits", List.of(allowed, denied),
                        "findAudit", denied)));

        var page = service.audits(
                new OmsOperationsQueryApplicationService.PageQuery(
                        "allowed", null, 1, 10),
                access("oms:audit:read", Set.of("10"),
                        Set.of("20"), Set.of("*")));

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.records()).extracting(
                OmsOperationsQueryApplicationService.AuditView::salesOrderNo)
                .containsExactly("SO-ALLOWED");
        assertThatThrownBy(() -> service.audit("2",
                access("oms:audit:read", Set.of("10"),
                        Set.of("20"), Set.of("*"))))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.code())
                                .isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void reservationRequiresWarehouseScopeAsWellAsOrganizationAndOwner() {
        LocalDateTime now = LocalDateTime.now();
        var reservation = new OmsOperationsQueryMapper.ReservationRow(
                "REF-1", "INV-1", "SO-1", "FUL-1",
                10L, 20L, 30L, "WH-30",
                new BigDecimal("2"), new BigDecimal("2"),
                2, null, 1, now, now);
        var service = new OmsOperationsQueryApplicationService(mapper(
                Map.of("listReservations", List.of(reservation))));

        var allowed = service.reservations(
                new OmsOperationsQueryApplicationService.PageQuery(
                        null, null, 1, 10),
                access("oms:reservation:read", Set.of("10"),
                        Set.of("20"), Set.of("30")));
        var denied = service.reservations(
                new OmsOperationsQueryApplicationService.PageQuery(
                        null, null, 1, 10),
                access("oms:reservation:read", Set.of("10"),
                        Set.of("20"), Set.of("31")));

        assertThat(allowed.total()).isEqualTo(1);
        assertThat(denied.total()).isZero();
    }

    @Test
    void cancellationAfterSaleExceptionAndLogUseTheSameScopeRules() {
        LocalDateTime now = LocalDateTime.now();
        var cancellation = new OmsOperationsQueryMapper.CancellationRow(
                "CAN-1", "SO-1", "FUL-1", "OUT-1", "RES-1", "客户取消",
                10L, 20L, 30L, "WH-30", 4, true, true, 2, now, now);
        var afterSale = new OmsOperationsQueryMapper.AfterSaleRow(
                "AS-1", "RETURN_REFUND", "SO-1", "FUL-1",
                10L, 20L, 30L, "WH-30", "商品破损",
                new BigDecimal("18.00"), new BigDecimal("18.00"),
                8, 3, now, now);
        var exception = new OmsOperationsQueryMapper.ExceptionRow(
                "EX-1", "SO-1", "FUL-1", "OUT-1",
                10L, 20L, 30L, "WH-30", "WMS_OUTBOUND", "WMS",
                "仓内出库失败", 1, 1, now, now);
        var log = new OmsOperationsQueryMapper.OperationLogRow(
                1, "CANCEL_COMPLETED", "CAN-1", 9L, "idem-1",
                "SO-1", 10L, 20L, 30L, "WH-30", now);
        var service = new OmsOperationsQueryApplicationService(mapper(Map.of(
                "listCancellations", List.of(cancellation),
                "listAfterSales", List.of(afterSale),
                "listExceptions", List.of(exception),
                "listOperationLogs", List.of(log))));

        assertThat(service.cancellations(page(), access(
                "oms:cancel:read", Set.of("10"), Set.of("20"), Set.of("30"))).total())
                .isEqualTo(1);
        assertThat(service.afterSales(page(), access(
                "oms:after_sale:read", Set.of("10"), Set.of("20"), Set.of("30"))).total())
                .isEqualTo(1);
        assertThat(service.exceptions(page(), access(
                "oms:exception:read", Set.of("10"), Set.of("20"), Set.of("30"))).total())
                .isEqualTo(1);
        assertThat(service.operationLogs(page(), access(
                "oms:operation_log:read", Set.of("10"), Set.of("20"), Set.of("30"))).total())
                .isEqualTo(1);
        assertThat(service.afterSales(page(), access(
                "oms:after_sale:read", Set.of("10"), Set.of("20"), Set.of("31"))).total())
                .isZero();
    }

    @Test
    void validatesSortWhitelistAndSortsBeforePaging() {
        LocalDateTime earlier = LocalDateTime.now().minusHours(1);
        LocalDateTime later = LocalDateTime.now();
        var older = new OmsOperationsQueryMapper.AuditRow(
                "1", "SO-1", 10L, 20L, "TMALL", 88L,
                "ORDER_REVIEW", "PASSED", null, null, 2,
                1L, earlier, earlier, earlier);
        var newer = new OmsOperationsQueryMapper.AuditRow(
                "2", "SO-2", 10L, 20L, "TMALL", 89L,
                "ORDER_REVIEW", "BLOCKED", null, "风控命中", 1,
                null, null, later, later);
        var service = new OmsOperationsQueryApplicationService(mapper(
                Map.of("listAudits", List.of(older, newer),
                        "listOperationLogs", List.of())));
        var access = access("oms:audit:read", Set.of("10"),
                Set.of("20"), Set.of("*"));

        var page = service.audits(new OmsOperationsQueryApplicationService.PageQuery(
                null, null, 1, 10, "updatedAt", "desc"), access);

        assertThat(page.records()).extracting(
                OmsOperationsQueryApplicationService.AuditView::salesOrderNo)
                .containsExactly("SO-2", "SO-1");
        assertThatThrownBy(() -> service.audits(
                new OmsOperationsQueryApplicationService.PageQuery(
                        null, null, 1, 10, "drop table", "desc"), access))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.code())
                                .isEqualTo(ErrorCode.VALIDATION_FAILED));
        assertThatThrownBy(() -> service.operationLogs(
                new OmsOperationsQueryApplicationService.PageQuery(
                        null, 1, 1, 10, "createdAt", "desc"),
                access("oms:operation_log:read", Set.of("10"),
                        Set.of("20"), Set.of("*"))))
                .isInstanceOf(BusinessException.class);
    }

    private static OmsOperationsQueryApplicationService.PageQuery page() {
        return new OmsOperationsQueryApplicationService.PageQuery(
                null, null, 1, 10);
    }

    private static ScmAccessContext access(
            String permission, Set<String> organizations,
            Set<String> owners, Set<String> warehouses) {
        return new ScmAccessContext(1, "tester", "OMS", Set.of(permission),
                Map.of("ORGANIZATION", organizations, "OWNER", owners,
                        "WAREHOUSE", warehouses));
    }

    private static OmsOperationsQueryMapper mapper(Map<String, Object> results) {
        return (OmsOperationsQueryMapper) Proxy.newProxyInstance(
                OmsOperationsQueryMapper.class.getClassLoader(),
                new Class<?>[]{OmsOperationsQueryMapper.class},
                (proxy, method, arguments) -> results.get(method.getName()));
    }
}
