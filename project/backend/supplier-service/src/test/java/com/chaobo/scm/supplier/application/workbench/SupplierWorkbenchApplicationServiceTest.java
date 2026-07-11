package com.chaobo.scm.supplier.application.workbench;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.supplier.infrastructure.persistence.workbench.SupplierWorkbenchMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupplierWorkbenchApplicationServiceTest {
    private final FakeWorkbenchMapper mapper = new FakeWorkbenchMapper();
    private final SupplierWorkbenchApplicationService service = new SupplierWorkbenchApplicationService(mapper);

    @Test
    void summaryUsesSupplierScopeAndAggregatesDashboardNumbers() {
        var result = service.summary(9999L, 3001L, 30);

        assertThat(result.pendingQuotes()).isEqualTo(2);
        assertThat(result.pendingPurchaseOrderConfirms()).isEqualTo(3);
        assertThat(result.latestScore()).isEqualByComparingTo("88.50");
        assertThat(result.todoGroups()).extracting("type").containsExactly("QUOTE");
        assertThat(mapper.lastSupplierId).isEqualTo(3001L);
    }

    @Test
    void summaryRejectsInvalidRecentDays() {
        assertThatThrownBy(() -> service.summary(null, null, 0))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("统计天数");
    }

    private static final class FakeWorkbenchMapper implements SupplierWorkbenchMapper {
        private Long lastSupplierId;

        @Override
        public long pendingQuotes(Long supplierId) {
            lastSupplierId = supplierId;
            return 2;
        }

        @Override
        public long pendingPurchaseOrderConfirms(Long supplierId) {
            return 3;
        }

        @Override
        public long pendingAsns(Long supplierId) {
            return 4;
        }

        @Override
        public long pendingReconciliations(Long supplierId) {
            return 5;
        }

        @Override
        public long pendingRectifications(Long supplierId) {
            return 6;
        }

        @Override
        public long openWarnings(Long supplierId) {
            return 7;
        }

        @Override
        public long failedEvents() {
            return 8;
        }

        @Override
        public long openReturns(Long supplierId) {
            return 9;
        }

        @Override
        public BigDecimal latestScore(Long supplierId, OffsetDateTime since) {
            return new BigDecimal("88.50");
        }

        @Override
        public List<SupplierWorkbenchView.TodoGroup> todoGroups(Long supplierId) {
            return List.of(new SupplierWorkbenchView.TodoGroup("QUOTE", 2));
        }

        @Override
        public List<SupplierWorkbenchView.WarningGroup> warningGroups(Long supplierId) {
            return List.of(new SupplierWorkbenchView.WarningGroup("预警", 7));
        }
    }
}
