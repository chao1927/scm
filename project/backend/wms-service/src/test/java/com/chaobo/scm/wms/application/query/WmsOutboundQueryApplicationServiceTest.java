package com.chaobo.scm.wms.application.query;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.security.ScmAccessContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 出库工作台权限、范围和查询白名单测试。
 */
class WmsOutboundQueryApplicationServiceTest {

    @Test
    void warehouseAndOwnerScopesReachReadModel() {
        var port = new RecordingPort();
        var service = new WmsOutboundQueryApplicationService(port);
        service.pageOutbounds(query("SO", "status"), context(Set.of("wms:outbound:read"),
            Map.of("WAREHOUSE", Set.of("10"), "OWNER", Set.of("20"))));
        assertThat(port.scope.warehouseIds()).containsExactly(10L);
        assertThat(port.scope.ownerIds()).containsExactly(20L);
    }

    @Test
    void emptyOwnerScopeReturnsEmptyWithoutQueryingDatabase() {
        var port = new RecordingPort();
        var page = new WmsOutboundQueryApplicationService(port).pagePicks(
            query(null, "updatedAt"), context(Set.of("wms:picking:read"),
                Map.of("WAREHOUSE", Set.of("10"), "OWNER", Set.of())));
        assertThat(page.records()).isEmpty();
        assertThat(port.scope).isNull();
    }

    @Test
    void clientCannotInjectSortExpression() {
        var service = new WmsOutboundQueryApplicationService(new RecordingPort());
        assertThatThrownBy(() -> service.pageShipments(query(null, "updatedAt desc; delete"),
            context(Set.of("wms:shipping:read"),
                Map.of("WAREHOUSE", Set.of("*"), "OWNER", Set.of("*")))))
            .isInstanceOf(BusinessException.class).hasMessageContaining("排序字段");
    }

    private static WmsOutboundQueryApplicationService.PageQuery query(String keyword, String sortBy) {
        return new WmsOutboundQueryApplicationService.PageQuery(keyword, null, 1, 20, sortBy, "desc");
    }

    private static ScmAccessContext context(Set<String> permissions, Map<String, Set<String>> scopes) {
        return new ScmAccessContext(1, "wms-user", "SCM_WEB", permissions, scopes);
    }

    private static final class RecordingPort implements WmsOutboundReadModelPort {
        private Scope scope;
        private <T> PageResult<T> page(Query query, Scope value) {
            scope = value;
            return new PageResult<>(query.pageNo(), query.pageSize(), 0, List.of());
        }
        public PageResult<OutboundSummary> pageOutbounds(Query q, Scope s) { return page(q, s); }
        public Optional<OutboundSummary> outboundDetail(String no, Scope s) { scope = s; return Optional.empty(); }
        public PageResult<WaveSummary> pageWaves(Query q, Scope s) { return page(q, s); }
        public Optional<WaveSummary> waveDetail(String no, Scope s) { scope = s; return Optional.empty(); }
        public PageResult<PickSummary> pagePicks(Query q, Scope s) { return page(q, s); }
        public Optional<PickSummary> pickDetail(String no, Scope s) { scope = s; return Optional.empty(); }
        public PageResult<PackingSummary> pagePackings(Query q, Scope s) { return page(q, s); }
        public Optional<PackingSummary> packingDetail(String no, Scope s) { scope = s; return Optional.empty(); }
        public PageResult<ShipmentSummary> pageShipments(Query q, Scope s) { return page(q, s); }
        public Optional<ShipmentSummary> shipmentDetail(String no, Scope s) { scope = s; return Optional.empty(); }
    }
}
