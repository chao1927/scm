package com.chaobo.scm.wms.application.outbox;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.wms.infrastructure.persistence.event.WmsEventMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WmsOutboxDispatchApplicationServiceTest {
    private final InMemoryWmsEventMapper mapper = new InMemoryWmsEventMapper();

    @Test
    void dispatchMarksPublishedAndFailedEvents() {
        mapper.rows.add(row(1, "E1", "WmsReceiptCompleted", 1));
        mapper.rows.add(row(2, "E2", "WmsPutawayCompleted", 1));
        var broker = new SelectiveBroker("E2");
        var service = new WmsOutboxDispatchApplicationService(mapper, broker);

        var result = service.dispatchPending(10);

        assertThat(result.published()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(1);
        assertThat(mapper.rows.get(0).status()).isEqualTo(2);
        assertThat(mapper.rows.get(1).status()).isEqualTo(3);
    }

    @Test
    void failedEventsCanBeRetried() {
        mapper.rows.add(row(1, "E1", "WmsReceiptCompleted", 3));
        var service = new WmsOutboxDispatchApplicationService(mapper, new SelectiveBroker(null));

        assertThat(service.failedEvents(10)).hasSize(1);
        service.retry(1);

        assertThat(mapper.rows.get(0).status()).isEqualTo(1);
    }

    @Test
    void retryRejectsNonFailedEvent() {
        mapper.rows.add(row(1, "E1", "WmsReceiptCompleted", 2));
        var service = new WmsOutboxDispatchApplicationService(mapper, new SelectiveBroker(null));

        assertThatThrownBy(() -> service.retry(1)).isInstanceOf(BusinessException.class);
    }

    private static WmsEventMapper.Row row(long id, String code, String type, int status) {
        return new WmsEventMapper.Row(id, code, type, "RECEIPT", "REC-001", 1, "{}", status, 0);
    }

    private static class SelectiveBroker implements WmsMessageBrokerPort {
        private final String failingCode;

        SelectiveBroker(String failingCode) {
            this.failingCode = failingCode;
        }

        @Override
        public void publish(String eventCode, String eventType, String payload) {
            if (eventCode.equals(failingCode)) {
                throw new IllegalStateException("broker unavailable");
            }
        }
    }

    private static class InMemoryWmsEventMapper implements WmsEventMapper {
        private final List<Row> rows = new ArrayList<>();

        @Override
        public void insert(long id, String code, String type, String aggregateType, String aggregateId, int version, String payload) {
            rows.add(new Row(id, code, type, aggregateType, aggregateId, version, payload, 1, 0));
        }

        @Override
        public List<Row> pending(int limit) {
            return rows.stream().filter(row -> row.status() == 1 || row.status() == 3).limit(limit).toList();
        }

        @Override
        public int markPublished(long id) {
            return replaceStatus(id, 2);
        }

        @Override
        public int markFailed(long id) {
            return replaceStatus(id, 3);
        }

        @Override
        public List<Row> failed(int limit) {
            return rows.stream().filter(row -> row.status() == 3).limit(limit).toList();
        }

        @Override
        public int retry(long id) {
            var row = rows.stream().filter(value -> value.id() == id && value.status() == 3).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            rows.set(rows.indexOf(row), new Row(
                    row.id(),
                    row.code(),
                    row.type(),
                    row.aggregateType(),
                    row.aggregateId(),
                    row.version(),
                    row.payload(),
                    1,
                    row.retryCount()
            ));
            return 1;
        }

        private int replaceStatus(long id, int status) {
            var row = rows.stream().filter(value -> value.id() == id).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            rows.set(rows.indexOf(row), new Row(
                    row.id(),
                    row.code(),
                    row.type(),
                    row.aggregateType(),
                    row.aggregateId(),
                    row.version(),
                    row.payload(),
                    status,
                    status == 3 ? row.retryCount() + 1 : row.retryCount()
            ));
            return 1;
        }
    }
}
