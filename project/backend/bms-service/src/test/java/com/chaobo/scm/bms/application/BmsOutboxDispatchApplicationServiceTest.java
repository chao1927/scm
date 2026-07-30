package com.chaobo.scm.bms.application;

import com.chaobo.scm.bms.application.outbox.BmsMessageBrokerPort;
import com.chaobo.scm.bms.application.outbox.BmsOutboxDispatchApplicationService;
import com.chaobo.scm.bms.infrastructure.persistence.BmsOutboxMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BmsOutboxDispatchApplicationServiceTest {

    @Test
    void brokerAcknowledgementControlsOutboxStatus() {
        MemoryMapper mapper = new MemoryMapper(List.of(
            new BmsOutboxMapper.OutboxEvent(
                "BE1", "InvoiceIssued", "IV1", "BL1", "{}")));
        List<BmsMessageBrokerPort.OutboundMessage> sent = new ArrayList<>();
        var service = new BmsOutboxDispatchApplicationService(mapper, sent::add);

        var result = service.dispatch(100, 16);

        assertThat(result.published()).isEqualTo(1);
        assertThat(mapper.published).containsExactly("BE1");
        assertThat(sent).extracting(BmsMessageBrokerPort.OutboundMessage::eventCode)
            .containsExactly("BE1");
    }

    private static final class MemoryMapper implements BmsOutboxMapper {
        private final List<OutboxEvent> events;
        private final List<String> published = new ArrayList<>();

        private MemoryMapper(List<OutboxEvent> events) {
            this.events = events;
        }

        @Override
        public List<OutboxEvent> pending(int limit, int maxRetries) {
            return events;
        }

        @Override
        public int markPublished(String eventNo) {
            published.add(eventNo);
            return 1;
        }

        @Override
        public int markFailed(String eventNo, String reason) {
            return 1;
        }
    }
}
