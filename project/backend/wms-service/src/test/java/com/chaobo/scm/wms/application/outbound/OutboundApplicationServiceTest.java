package com.chaobo.scm.wms.application.outbound;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.infrastructure.persistence.outbound.OutboundMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboundApplicationServiceTest {
    private final InMemoryOutboundMapper mapper = new InMemoryOutboundMapper();
    private final RecordingEventPublisher events = new RecordingEventPublisher();
    private final OutboundApplicationService service = new OutboundApplicationService(mapper, events);

    @Test
    void createAllocateAndCancelOutboundOrderPublishesEvents() {
        var created = service.create("OMS", "SO-001", 1L, 99L);

        assertThat(created.duplicated()).isFalse();
        assertThat(created.status()).isEqualTo(1);

        var allocated = service.allocate("OMS", "SO-001", 1L, 0, 99L);

        assertThat(allocated.status()).isEqualTo(2);
        assertThat(allocated.version()).isEqualTo(1);

        var cancelled = service.cancel("OMS", "SO-001", 1L, 1, "客户取消", 99L);

        assertThat(cancelled.status()).isEqualTo(9);
        assertThat(events.types()).containsExactly(
                "WmsOutboundOrderCreated",
                "WmsOutboundAllocated",
                "WmsOutboundCancelled"
        );
    }

    @Test
    void repeatedCreateReturnsExistingOutboundWithoutDuplicateEvent() {
        service.create("OMS", "SO-002", 1L, 99L);

        var duplicated = service.create("OMS", "SO-002", 1L, 99L);

        assertThat(duplicated.duplicated()).isTrue();
        assertThat(events.types()).containsExactly("WmsOutboundOrderCreated");
    }

    @Test
    void allocateRequiresExpectedVersion() {
        service.create("OMS", "SO-003", 1L, 99L);

        assertThatThrownBy(() -> service.allocate("OMS", "SO-003", 1L, 7, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("版本冲突");
    }

    private static class InMemoryOutboundMapper implements OutboundMapper {
        private final List<Row> rows = new ArrayList<>();

        @Override
        public Row source(String type, String sourceNo, long warehouseId) {
            return rows.stream()
                    .filter(row -> row.sourceType().equals(type)
                            && row.sourceNo().equals(sourceNo)
                            && row.warehouseId() == warehouseId)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void insert(long id, String no, String type, String sourceNo, long warehouseId, long operator) {
            rows.add(new Row(id, no, type, sourceNo, warehouseId, 1, 0));
        }

        @Override
        public int update(long id, int status, int version, int oldVersion, long operator) {
            var row = rows.stream().filter(value -> value.id() == id && value.version() == oldVersion).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            rows.set(rows.indexOf(row), new Row(
                    row.id(),
                    row.no(),
                    row.sourceType(),
                    row.sourceNo(),
                    row.warehouseId(),
                    status,
                    version
            ));
            return 1;
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
