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
