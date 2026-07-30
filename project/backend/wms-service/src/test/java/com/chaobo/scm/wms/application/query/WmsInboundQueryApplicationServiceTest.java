package com.chaobo.scm.wms.application.query;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.security.ScmAccessContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * WMS 入库作业读模型应用服务测试。
 *
 * <p>验证查询不会修改聚合，并且功能权限、仓库/货主数据范围、分页与排序白名单在进入持久化端口前
 * 已经收敛为明确契约。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class WmsInboundQueryApplicationServiceTest {

    /**
     * 受限用户的仓库和货主范围必须原样传入读模型端口。
     */
    @Test
    void restrictedScopesArePassedToReadModel() {
        var port = new RecordingReadModelPort();
        var service = new WmsInboundQueryApplicationService(port);
        var context = context(
                Set.of("wms:inbound:read"),
                Map.of("WAREHOUSE", Set.of("100", "200"), "OWNER", Set.of("300")));

        service.pageInbounds(
                new WmsInboundQueryApplicationService.PageQuery(
                        "ASN", 1, 2, 50, "status", "asc"),
                context);

        assertThat(port.lastScope.warehouseIds()).containsExactlyInAnyOrder(100L, 200L);
        assertThat(port.lastScope.ownerIds()).containsExactly(300L);
        assertThat(port.lastScope.allWarehouses()).isFalse();
        assertThat(port.lastScope.allOwners()).isFalse();
        assertThat(port.lastCriteria.pageNo()).isEqualTo(2);
        assertThat(port.lastCriteria.pageSize()).isEqualTo(50);
        assertThat(port.lastCriteria.sortField())
                .isEqualTo(WmsInboundReadModelPort.SortField.STATUS);
        assertThat(port.lastCriteria.sortDirection())
                .isEqualTo(WmsInboundReadModelPort.SortDirection.ASC);
    }

    /**
     * 通配数据范围必须显式传递，不能伪造成任意具体仓库或货主。
     */
    @Test
    void wildcardScopesRemainExplicit() {
        var port = new RecordingReadModelPort();
        var service = new WmsInboundQueryApplicationService(port);

        service.pageStocks(
                new WmsInboundQueryApplicationService.PageQuery(
                        null, null, 1, 20, "quantity", "desc"),
                context(Set.of("wms:stock:read"),
                        Map.of("WAREHOUSE", Set.of("*"), "OWNER", Set.of("*"))));

        assertThat(port.lastScope.allWarehouses()).isTrue();
        assertThat(port.lastScope.allOwners()).isTrue();
        assertThat(port.lastScope.warehouseIds()).isEmpty();
        assertThat(port.lastScope.ownerIds()).isEmpty();
    }

    /**
     * 未声明的排序字段必须失败关闭，避免把客户端输入拼接进 SQL。
     */
    @Test
    void unknownSortFieldIsRejected() {
        var service = new WmsInboundQueryApplicationService(
                new RecordingReadModelPort());

        assertThatThrownBy(() -> service.pageReceipts(
                new WmsInboundQueryApplicationService.PageQuery(
                        null, null, 1, 20, "drop table", "desc"),
                context(Set.of("wms:receiving:read"),
                        Map.of("WAREHOUSE", Set.of("*"), "OWNER", Set.of("*")))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("排序字段");
    }

    /**
     * 详情查询找不到可见记录时统一返回不存在，避免泄露越权对象是否真实存在。
     */
    @Test
    void invisibleDetailIsReportedAsNotFound() {
        var service = new WmsInboundQueryApplicationService(
                new RecordingReadModelPort());

        assertThatThrownBy(() -> service.inboundDetail(
                "WIB-NOT-VISIBLE",
                context(Set.of("wms:inbound:read"),
                        Map.of("WAREHOUSE", Set.of("100"), "OWNER", Set.of("300")))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不存在");
    }

    /**
     * 任一数据维度没有授权时，详情查询必须在应用层失败关闭，不能把空集合传给 SQL。
     */
    @Test
    void emptyScopeDoesNotReachDetailPort() {
        var port = new RecordingReadModelPort();
        var service = new WmsInboundQueryApplicationService(port);

        assertThatThrownBy(() -> service.receiptDetail(
                "REC-NOT-AUTHORIZED",
                context(Set.of("wms:receiving:read"),
                        Map.of("WAREHOUSE", Set.of("100"), "OWNER", Set.of()))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不存在");
        assertThat(port.lastScope).isNull();
    }

    /**
     * 构造测试所需的已验证访问上下文。
     *
     * @param permissions 功能权限
     * @param scopes 数据范围
     * @return 已验证访问上下文
     */
    private static ScmAccessContext context(
            Set<String> permissions,
            Map<String, Set<String>> scopes) {
        return new ScmAccessContext(
                9001L, "wms-user", "SCM_WEB", permissions, scopes);
    }

    /**
     * 记录查询参数的端口替身；只验证应用层契约，不模拟数据库行为。
     */
    private static final class RecordingReadModelPort
            implements WmsInboundReadModelPort {

        /**
         * 最近一次数据范围。
         */
        private DataScope lastScope;

        /**
         * 最近一次查询条件。
         */
        private PageCriteria lastCriteria;

        @Override
        public PageResult<InboundSummary> pageInbounds(
                PageCriteria criteria,
                DataScope scope) {
            record(criteria, scope);
            return new PageResult<>(
                    criteria.pageNo(), criteria.pageSize(), 0, List.of());
        }

        @Override
        public Optional<InboundDetail> inboundDetail(
                String inboundOrderNo,
                DataScope scope) {
            lastScope = scope;
            return Optional.empty();
        }

        @Override
        public PageResult<ReceiptSummary> pageReceipts(
                PageCriteria criteria,
                DataScope scope) {
            record(criteria, scope);
            return new PageResult<>(
                    criteria.pageNo(), criteria.pageSize(), 0, List.of());
        }

        @Override
        public Optional<ReceiptSummary> receiptDetail(
                String receiptNo,
                DataScope scope) {
            lastScope = scope;
            return Optional.empty();
        }

        @Override
        public PageResult<InspectionSummary> pageInspections(
                PageCriteria criteria,
                DataScope scope) {
            record(criteria, scope);
            return new PageResult<>(
                    criteria.pageNo(), criteria.pageSize(), 0, List.of());
        }

        @Override
        public Optional<InspectionSummary> inspectionDetail(
                String inspectionNo,
                DataScope scope) {
            lastScope = scope;
            return Optional.empty();
        }

        @Override
        public PageResult<PutawaySummary> pagePutawayTasks(
                PageCriteria criteria,
                DataScope scope) {
            record(criteria, scope);
            return new PageResult<>(
                    criteria.pageNo(), criteria.pageSize(), 0, List.of());
        }

        @Override
        public Optional<PutawaySummary> putawayDetail(
                String taskNo,
                DataScope scope) {
            lastScope = scope;
            return Optional.empty();
        }

        @Override
        public PageResult<StockSummary> pageStocks(
                PageCriteria criteria,
                DataScope scope) {
            record(criteria, scope);
            return new PageResult<>(
                    criteria.pageNo(), criteria.pageSize(), 0, List.of());
        }

        @Override
        public Optional<StockSummary> stockDetail(
                String stockKey,
                DataScope scope) {
            lastScope = scope;
            return Optional.empty();
        }

        /**
         * 记录端口输入。
         *
         * @param criteria 查询条件
         * @param scope 数据范围
         */
        private void record(PageCriteria criteria, DataScope scope) {
            lastCriteria = criteria;
            lastScope = scope;
        }

        /**
         * 创建一个未使用的视图，防止 IDE 将业务字段误判为无引用。
         *
         * @return 入库摘要
         */
        @SuppressWarnings("unused")
        private InboundSummary sample() {
            return new InboundSummary(
                    1L,
                    "WIB-1",
                    "PURCHASE",
                    "PO-1",
                    100L,
                    300L,
                    1,
                    "待到货",
                    null,
                    BigDecimal.TEN,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    OffsetDateTime.now());
        }
    }
}
