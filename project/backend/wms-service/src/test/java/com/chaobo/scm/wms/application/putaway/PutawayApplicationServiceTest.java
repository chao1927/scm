package com.chaobo.scm.wms.application.putaway;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.infrastructure.persistence.putaway.PutawayMapper;
import com.chaobo.scm.wms.infrastructure.persistence.stock.StockLedgerMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PutawayApplicationServiceTest {
    private final InMemoryPutawayMapper mapper = new InMemoryPutawayMapper();
    private final RecordingStockLedgerMapper ledger = new RecordingStockLedgerMapper();
    private final RecordingEventPublisher events = new RecordingEventPublisher();
    private final PutawayApplicationService service = new PutawayApplicationService(mapper, ledger, events);

    @Test
    void scanPutawayWritesLedgerAndPublishesEventWhenCompleted() {
        service.create("PUT-001", 10L, BigDecimal.TEN, 99L);

        var first = service.scan("PUT-001", 0, 1L, "A-01-01", "SKU-001", "B001", new BigDecimal("6"), 99L);

        assertThat(first.completed()).isFalse();
        assertThat(ledger.entries).hasSize(1);
        assertThat(events.types()).isEmpty();

        var completed = service.scan("PUT-001", 1, 1L, "A-01-02", "SKU-001", "B001", new BigDecimal("4"), 99L);

        assertThat(completed.completed()).isTrue();
        assertThat(ledger.entries).hasSize(2);
        assertThat(events.types()).containsExactly("WmsPutawayCompleted");
    }

    @Test
    void repeatedCreateReturnsExistingTask() {
        service.create("PUT-002", 10L, BigDecimal.TEN, 99L);

        var duplicated = service.create("PUT-002", 10L, BigDecimal.TEN, 99L);

        assertThat(duplicated.duplicated()).isTrue();
    }

    @Test
    void scanRequiresExpectedVersion() {
        service.create("PUT-003", 10L, BigDecimal.TEN, 99L);

        assertThatThrownBy(() -> service.scan(
                "PUT-003",
                7,
                1L,
                "A-01-01",
                "SKU-001",
                null,
                BigDecimal.ONE,
                99L
        )).isInstanceOf(BusinessException.class)
                .hasMessageContaining("版本冲突");
    }

    private static class InMemoryPutawayMapper implements PutawayMapper {
        private final Map<String, Row> rows = new HashMap<>();

        @Override
        public Row find(String no) {
            return rows.get(no);
        }

        @Override
        public void insert(
                long id,
                String no,
                long inspection,
                BigDecimal required,
                BigDecimal putaway,
                int status,
                int version,
                long operator
        ) {
            rows.put(no, new Row(id, no, inspection, required, putaway, status, version));
        }

        @Override
        public int update(long id, BigDecimal putaway, int status, int version, int expected, long operator) {
            var row = rows.values().stream().filter(value -> value.id() == id).findFirst().orElse(null);
            if (row == null || row.version() != expected) {
                return 0;
            }
            rows.put(row.no(), new Row(id, row.no(), row.inspectionId(), row.required(), putaway, status, version));
            return 1;
        }
    }

    private static class RecordingStockLedgerMapper implements StockLedgerMapper {
        private final List<String> entries = new ArrayList<>();

        @Override
        public void insert(
                long id,
                long warehouse,
                String location,
                String sku,
                String batch,
                String type,
                BigDecimal qty,
                String sourceType,
                String sourceNo
        ) {
            entries.add(type + ":" + sourceNo + ":" + location + ":" + qty);
        }
    }

    private static class RecordingEventPublisher implements WmsEventPublisher {
        private final List<String> eventTypes = new ArrayList<>();

        @Override
        public void publish(String eventType, String aggregateType, String aggregateId, int version, String payload) {
            eventTypes.add(eventType);
        }

        List<String> types() {
            return eventTypes;
        }
    }
}
