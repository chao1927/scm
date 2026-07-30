package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.security.ScmAccessContext;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 库存运营读模型范围与口径测试。
 *
 * @author SCM Team
 */
class InventoryOperationsQueryApplicationServiceTest {

    @Test
    void reservationQueryPassesOnlyAuthorizedOwnerAndWarehouseScope() {
        CapturingReadModel readModel = new CapturingReadModel();
        InventoryOperationsQueryApplicationService service =
                new InventoryOperationsQueryApplicationService(readModel);
        ScmAccessContext access = access(
                Set.of("inventory:reservation:read"),
                Map.of("OWNER", Set.of("10", "11"), "WAREHOUSE", Set.of("20")));

        service.reservations(
                new InventoryOperationsQueryApplicationService.OperationQuery(
                        10L, 20L, "SKU-1", "B-1", 1, 1, 20),
                access);

        assertThat(readModel.scope.ownerIds()).containsExactly(10L);
        assertThat(readModel.scope.warehouseIds()).containsExactly(20L);
        assertThat(readModel.query.sku()).isEqualTo("SKU-1");
    }

    @Test
    void requestedOwnerOutsideTokenScopeFailsClosed() {
        InventoryOperationsQueryApplicationService service =
                new InventoryOperationsQueryApplicationService(new CapturingReadModel());
        ScmAccessContext access = access(
                Set.of("inventory:freeze:read"),
                Map.of("OWNER", Set.of("10"), "WAREHOUSE", Set.of("20")));

        assertThatThrownBy(() -> service.freezes(
                new InventoryOperationsQueryApplicationService.OperationQuery(
                        99L, 20L, null, null, null, 1, 20),
                access))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("货主");
    }

    @Test
    void metricTypeKeepsBookPhysicalAgeSlowMovingAndExpiryDefinitionsExplicit() {
        assertThat(InventoryOperationReadModelPort.MetricType.values())
                .containsExactly(
                        InventoryOperationReadModelPort.MetricType.BOOK_PHYSICAL,
                        InventoryOperationReadModelPort.MetricType.STOCK_AGE,
                        InventoryOperationReadModelPort.MetricType.SLOW_MOVING,
                        InventoryOperationReadModelPort.MetricType.EXPIRY);
    }

    private static ScmAccessContext access(
            Set<String> permissions,
            Map<String, Set<String>> scopes) {
        return new ScmAccessContext(1001L, "inventory-user", "INVENTORY", permissions, scopes);
    }

    private static final class CapturingReadModel implements InventoryOperationReadModelPort {

        private QueryScope scope;
        private OperationFilter query;

        @Override
        public Page<ReservationView> reservations(
                QueryScope scope,
                OperationFilter query) {
            this.scope = scope;
            this.query = query;
            return new Page<>(0L, java.util.List.of());
        }

        @Override
        public Page<FreezeView> freezes(QueryScope scope, OperationFilter query) {
            this.scope = scope;
            this.query = query;
            return new Page<>(0L, java.util.List.of());
        }

        @Override
        public Page<AdjustmentView> adjustments(QueryScope scope, OperationFilter query) {
            return new Page<>(0L, java.util.List.of());
        }

        @Override
        public Page<EventLogView> eventLogs(QueryScope scope, OperationFilter query) {
            return new Page<>(0L, java.util.List.of());
        }

        @Override
        public Page<OperationLogView> operationLogs(
                QueryScope scope,
                OperationFilter query) {
            return new Page<>(0L, java.util.List.of());
        }

        @Override
        public Page<MetricView> metrics(
                MetricType metricType,
                QueryScope scope,
                MetricFilter query) {
            return new Page<>(0L, java.util.List.of());
        }
    }
}
