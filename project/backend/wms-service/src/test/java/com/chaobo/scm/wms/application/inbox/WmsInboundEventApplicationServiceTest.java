package com.chaobo.scm.wms.application.inbox;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.wms.application.inbound.InboundOrderApplicationService;
import com.chaobo.scm.wms.application.outbound.OutboundApplicationService;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.domain.inbound.InboundOrderAggregate;
import com.chaobo.scm.wms.domain.inbound.InboundOrderRepository;
import com.chaobo.scm.wms.infrastructure.persistence.event.WmsInboxMapper;
import com.chaobo.scm.wms.infrastructure.persistence.outbound.OutboundMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WmsInboundEventApplicationServiceTest {
    private final InMemoryInboxMapper inbox = new InMemoryInboxMapper();
    private final InMemoryInboundRepository inboundRepository = new InMemoryInboundRepository();
    private final InMemoryOutboundMapper outboundMapper = new InMemoryOutboundMapper();
    private final WmsInboundEventApplicationService service = new WmsInboundEventApplicationService(
            inbox,
            new InboundOrderApplicationService(inboundRepository),
            new OutboundApplicationService(outboundMapper, new NoopEventPublisher()),
            new ObjectMapper()
    );

    @Test
    void consumeInboundCreateCommandIsIdempotent() {
        var envelope = new WmsInboundEventApplicationService.EventEnvelope(
                "PURCHASE",
                "EVT-001",
                "CreateInboundOrderRequested",
                """
                        {"sourceType":"PURCHASE","sourceNo":"PO-001","warehouseId":1,"expectedArrivalAt":"2026-07-12T10:00:00Z"}
                        """
        );

        var first = service.consume(envelope, 99L);
        var duplicated = service.consume(envelope, 99L);

        assertThat(first.duplicated()).isFalse();
        assertThat(duplicated.duplicated()).isTrue();
        assertThat(inboundRepository.values).hasSize(1);
        assertThat(inbox.rows.get(0).status()).isEqualTo(2);
    }

    @Test
    void consumeOutboundCreateCommandCreatesOutboundOrder() {
        var envelope = new WmsInboundEventApplicationService.EventEnvelope(
                "OMS",
                "EVT-002",
                "CreateOutboundOrderRequested",
                """
                        {"sourceType":"OMS","sourceNo":"SO-001","warehouseId":1}
                        """
        );

        service.consume(envelope, 99L);

        assertThat(outboundMapper.rows).hasSize(1);
        assertThat(outboundMapper.rows.get(0).sourceNo()).isEqualTo("SO-001");
    }

    @Test
    void failedEventCanBeQueriedAndReplayed() {
        var envelope = new WmsInboundEventApplicationService.EventEnvelope(
                "OMS",
                "EVT-003",
                "UnsupportedEvent",
                "{}"
        );

        assertThatThrownBy(() -> service.consume(envelope, 99L)).isInstanceOf(BusinessException.class);
        assertThat(service.failedEvents(10)).hasSize(1);

        inbox.rows.set(0, new WmsInboxMapper.Row(
                1,
                "OMS",
                "EVT-003",
                "CreateOutboundOrderRequested",
                """
                        {"sourceType":"OMS","sourceNo":"SO-REPLAY","warehouseId":1}
                        """,
                3,
                1,
                "不支持的WMS入站事件类型"
        ));

        var replayed = service.replay(1, 99L);

        assertThat(replayed.message()).isEqualTo("重放成功");
        assertThat(outboundMapper.rows).hasSize(1);
        assertThat(inbox.rows.get(0).status()).isEqualTo(2);
    }

    private static class InMemoryInboxMapper implements WmsInboxMapper {
        private final List<Row> rows = new ArrayList<>();
        private long ids;

        @Override
        public Row find(String sourceSystem, String eventCode) {
            return rows.stream()
                    .filter(row -> row.sourceSystem().equals(sourceSystem) && row.eventCode().equals(eventCode))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void insert(String sourceSystem, String eventCode, String eventType, String payload) {
            rows.add(new Row(++ids, sourceSystem, eventCode, eventType, payload, 1, 0, null));
        }

        @Override
        public int markSucceeded(long id) {
            return replace(id, 2, null);
        }

        @Override
        public int markFailed(long id, String message) {
            return replace(id, 3, message);
        }

        @Override
        public List<Row> failed(int limit) {
            return rows.stream().filter(row -> row.status() == 3).limit(limit).toList();
        }

        private int replace(long id, int status, String message) {
            var row = rows.stream().filter(value -> value.id() == id).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            rows.set(rows.indexOf(row), new Row(
                    row.id(),
                    row.sourceSystem(),
                    row.eventCode(),
                    row.eventType(),
                    row.payload(),
                    status,
                    status == 3 ? row.retryCount() + 1 : row.retryCount(),
                    message
            ));
            return 1;
        }
    }

    private static class InMemoryInboundRepository implements InboundOrderRepository {
        private final Map<String, InboundOrderAggregate> values = new HashMap<>();

        @Override
        public Optional<InboundOrderAggregate> findById(long id) {
            return values.values().stream().filter(value -> value.id() == id).findFirst();
        }

        @Override
        public Optional<InboundOrderAggregate> findBySource(String sourceType, String sourceNo, long warehouseId) {
            return Optional.ofNullable(values.get(sourceType + ":" + sourceNo + ":" + warehouseId));
        }

        @Override
        public void save(InboundOrderAggregate order, long operatorId) {
            values.put(order.sourceType() + ":" + order.sourceNo() + ":" + order.warehouseId(), order);
        }
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
        public int update(long id, int status, int version, int old, long operator) {
            return 0;
        }
    }

    private static class NoopEventPublisher implements WmsEventPublisher {
        @Override
        public void publish(String eventType, String aggregateType, String aggregateId, int version, String payload) {
        }
    }
}
