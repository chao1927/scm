package com.chaobo.scm.iam.application.outbox;

import com.chaobo.scm.iam.infrastructure.persistence.IamOutboxMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IamOutboxDispatchApplicationServiceTest {

    @Test
    void brokerAcknowledgementControlsOutboxStatus() {
        MemoryMapper mapper = new MemoryMapper(List.of(
            new IamOutboxMapper.OutboxEvent(11, "MfaEnrolled", "1001", "{}")));
        List<IamMessageBrokerPort.OutboundMessage> sent = new ArrayList<>();
        var service = new IamOutboxDispatchApplicationService(mapper, sent::add);

        var result = service.dispatch(100, 16);

        assertThat(result.published()).isEqualTo(1);
        assertThat(mapper.published).containsExactly(11L);
        assertThat(sent).extracting(IamMessageBrokerPort.OutboundMessage::eventCode)
            .containsExactly("11");
    }

    private static final class MemoryMapper implements IamOutboxMapper {
        private final List<OutboxEvent> events;
        private final List<Long> published = new ArrayList<>();

        private MemoryMapper(List<OutboxEvent> events) {
            this.events = events;
        }

        public List<OutboxEvent> pending(int limit, int maxRetries) {
            return events;
        }

        public int markPublished(long eventId) {
            published.add(eventId);
            return 1;
        }

        public int markFailed(long eventId, String reason) {
            return 1;
        }
    }
}
