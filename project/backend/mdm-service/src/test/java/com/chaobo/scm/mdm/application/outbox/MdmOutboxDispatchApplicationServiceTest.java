package com.chaobo.scm.mdm.application.outbox;

import com.chaobo.scm.mdm.infrastructure.persistence.MdmOutboxMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MdmOutboxDispatchApplicationServiceTest {

    private static final String FAILED_EVENT_CODE = "2";

    @Test
    void brokerConfirmationMarksPublishedAndFailureSchedulesRetry() {
        MemoryMapper mapper = new MemoryMapper();
        MdmOutboxDispatchApplicationService service =
            new MdmOutboxDispatchApplicationService(mapper, message -> {
                if (FAILED_EVENT_CODE.equals(message.eventCode())) {
                    throw new IllegalStateException("broker unavailable");
                }
            }, "mdm-domain-event");

        var result = service.dispatch(20, 8);

        assertThat(result.published()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(1);
        assertThat(mapper.published).isEqualTo(1L);
        assertThat(mapper.failed).isEqualTo(2L);
        assertThat(mapper.error).contains("broker unavailable");
    }

    private static final class MemoryMapper implements MdmOutboxMapper {
        private long published;
        private long failed;
        private String error;

        @Override
        public List<OutboxEvent> pending(int limit, int maxRetries) {
            return List.of(
                new OutboxEvent(1L, "MasterDataChanged", "MD-1", "{}", null),
                new OutboxEvent(2L, "MasterDataPublished", "PUB-2", "{}", "target-topic"));
        }

        @Override
        public int markPublished(long eventId) {
            published = eventId;
            return 1;
        }

        @Override
        public int markFailed(long eventId, String reason) {
            failed = eventId;
            error = reason;
            return 1;
        }
    }
}
