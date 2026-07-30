package com.chaobo.scm.inventory.infrastructure.persistence;

import com.chaobo.scm.inventory.application.InventoryOperationReadModelPort;
import com.chaobo.scm.inventory.application.export.InventoryExportTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 库存导出数据上限测试。
 */
class InventoryExportDataAdapterTest {

    @Test
    void failsInsteadOfSilentlyTruncatingRowsBeyondLimit() {
        InventoryExportDataAdapter adapter = new InventoryExportDataAdapter(
                new OversizedReadModel(),
                new ObjectMapper());

        assertThatThrownBy(() -> adapter.load(task(), 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("超过上限");
    }

    private static InventoryExportTask task() {
        LocalDateTime now = LocalDateTime.now();
        return new InventoryExportTask(
                1L, "EXP-1", "RESERVATION", "{}", "[\"*\"]", "[\"*\"]",
                1001L, 1, 0, null, null, null, null, null, null, 0, now, now);
    }

    private static final class OversizedReadModel implements InventoryOperationReadModelPort {

        @Override
        public Page<ReservationView> reservations(QueryScope scope, OperationFilter query) {
            return new Page<>(2L, List.of());
        }

        @Override
        public Page<FreezeView> freezes(QueryScope scope, OperationFilter query) {
            return new Page<>(0L, List.of());
        }

        @Override
        public Page<AdjustmentView> adjustments(QueryScope scope, OperationFilter query) {
            return new Page<>(0L, List.of());
        }

        @Override
        public Page<EventLogView> eventLogs(QueryScope scope, OperationFilter query) {
            return new Page<>(0L, List.of());
        }

        @Override
        public Page<OperationLogView> operationLogs(QueryScope scope, OperationFilter query) {
            return new Page<>(0L, List.of());
        }

        @Override
        public Page<MetricView> metrics(
                MetricType metricType,
                QueryScope scope,
                MetricFilter query) {
            return new Page<>(0L, List.of());
        }
    }
}
