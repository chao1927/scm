package com.chaobo.scm.tms.application.outbox;

import com.chaobo.scm.tms.infrastructure.persistence.TmsOutboxMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TmsOutboxDispatchApplicationServiceTest {

    @Test
    void marksPublishedOnlyAfterBrokerAcknowledges() {
        MemoryMapper mapper = new MemoryMapper(List.of(
            new TmsOutboxMapper.OutboxEvent(
                1L, "TMS-1", "TransportSigned", "WB1", "{\"waybillNo\":\"WB1\"}")));
        List<TmsMessageBrokerPort.OutboundMessage> sent = new ArrayList<>();
        var service = new TmsOutboxDispatchApplicationService(mapper, sent::add);

        var result = service.dispatch(100, 16);

        assertThat(result.published()).isEqualTo(1);
        assertThat(mapper.published).containsExactly(1L);
        assertThat(sent).extracting(TmsMessageBrokerPort.OutboundMessage::eventCode)
            .containsExactly("TMS-1");
    }

    @Test
    void preservesFailedEventForRetry() {
        MemoryMapper mapper = new MemoryMapper(List.of(
            new TmsOutboxMapper.OutboxEvent(
                2L, "TMS-2", "TransportRejected", "WB2", "{}")));
        var service = new TmsOutboxDispatchApplicationService(
            mapper, message -> { throw new IllegalStateException("broker unavailable"); });

        var result = service.dispatch(100, 16);

        assertThat(result.failed()).isEqualTo(1);
        assertThat(mapper.failed).containsExactly(2L);
        assertThat(mapper.lastError).contains("broker unavailable");
    }

    private static final class MemoryMapper implements TmsOutboxMapper {
        private final List<OutboxEvent> events;
        private final List<Long> published = new ArrayList<>();
        private final List<Long> failed = new ArrayList<>();
        private String lastError;

        private MemoryMapper(List<OutboxEvent> events) {
            this.events = events;
        }

        @Override
        public List<OutboxEvent> pending(int limit, int maxRetries) {
            return events;
        }

        @Override
        public int markPublished(long id) {
            published.add(id);
            return 1;
        }

        @Override
        public int markFailed(long id, String reason) {
            failed.add(id);
            lastError = reason;
            return 1;
        }
    }
}
