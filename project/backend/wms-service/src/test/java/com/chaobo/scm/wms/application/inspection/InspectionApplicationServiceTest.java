package com.chaobo.scm.wms.application.inspection;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.infrastructure.persistence.inspection.InspectionMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InspectionApplicationServiceTest {
    private final InMemoryInspectionMapper mapper = new InMemoryInspectionMapper();
    private final RecordingEventPublisher events = new RecordingEventPublisher();
    private final InspectionApplicationService service = new InspectionApplicationService(mapper, events);

    @Test
    void createAndSubmitInspectionPublishesCompletedEvent() {
        var created = service.create("QC-001", 10L, BigDecimal.TEN, 99L);

        assertThat(created.duplicated()).isFalse();
        assertThat(created.completed()).isFalse();

        var result = service.result("QC-001", 0, new BigDecimal("8"), new BigDecimal("2"), 99L);

        assertThat(result.completed()).isTrue();
        assertThat(result.version()).isEqualTo(1);
        assertThat(events.types()).containsExactly("WmsQualityInspectionCompleted");
    }

    @Test
    void repeatedCreateReturnsExistingInspectionWithoutPublishingEvent() {
        service.create("QC-002", 10L, BigDecimal.TEN, 99L);

        var duplicated = service.create("QC-002", 10L, BigDecimal.TEN, 99L);

        assertThat(duplicated.duplicated()).isTrue();
        assertThat(events.types()).isEmpty();
    }

    @Test
    void resultRequiresExpectedVersion() {
        service.create("QC-003", 10L, BigDecimal.TEN, 99L);

        assertThatThrownBy(() -> service.result("QC-003", 7, BigDecimal.TEN, BigDecimal.ZERO, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("版本冲突");
    }

    private static class InMemoryInspectionMapper implements InspectionMapper {
        private final Map<String, Row> rows = new HashMap<>();

        @Override
        public Row find(String no) {
            return rows.get(no);
        }

        @Override
        public void insert(
                long id,
                String no,
                long receipt,
                BigDecimal qty,
                BigDecimal qualified,
                BigDecimal unqualified,
                int status,
                int version,
                long operator
        ) {
            rows.put(no, new Row(id, no, receipt, qty, qualified, unqualified, status, version));
        }

        @Override
        public int update(
                long id,
                BigDecimal qualified,
                BigDecimal unqualified,
                int status,
                int version,
                int expected,
                long operator
        ) {
            var row = rows.values().stream().filter(value -> value.id() == id).findFirst().orElse(null);
            if (row == null || row.version() != expected) {
                return 0;
            }
            rows.put(row.no(), new Row(id, row.no(), row.receiptId(), row.qty(), qualified, unqualified, status, version));
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
