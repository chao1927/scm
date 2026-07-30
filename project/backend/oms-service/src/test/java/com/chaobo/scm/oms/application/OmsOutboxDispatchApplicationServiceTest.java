package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.infrastructure.persistence.OmsOutboxMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Outbox 可靠投递状态迁移测试。
 */
class OmsOutboxDispatchApplicationServiceTest {

    @Test
    void marksPublishedOnlyAfterBrokerAcknowledges() {
        MemoryOutboxMapper mapper = new MemoryOutboxMapper();
        mapper.rows.add(new OmsOutboxMapper.OutboxMessage(
                1, "OMS-E1", "SalesOrderCreated", "SO-1", "{}", 0));
        List<OmsMessageBroker.OutboundMessage> sent = new ArrayList<>();
        var service = new OmsOutboxDispatchApplicationService(mapper, sent::add);

        var result = service.dispatch(50);

        assertThat(result.published()).isEqualTo(1);
        assertThat(result.failed()).isZero();
        assertThat(sent).extracting(OmsMessageBroker.OutboundMessage::eventCode)
                .containsExactly("OMS-E1");
        assertThat(mapper.published).containsExactly(1L);
        assertThat(mapper.failed).isEmpty();
    }

    @Test
    void recordsFailureAndLeavesMessageForRetry() {
        MemoryOutboxMapper mapper = new MemoryOutboxMapper();
        mapper.rows.add(new OmsOutboxMapper.OutboxMessage(
                2, "OMS-E2", "SalesOrderReviewed", "SO-2", "{}", 0));
        OmsMessageBroker unavailable = message -> {
            throw new IllegalStateException("broker unavailable");
        };
        var service = new OmsOutboxDispatchApplicationService(mapper, unavailable);

        var result = service.dispatch(50);

        assertThat(result.published()).isZero();
        assertThat(result.failed()).isEqualTo(1);
        assertThat(mapper.published).isEmpty();
        assertThat(mapper.failed).containsExactly(2L);
        assertThat(mapper.lastError).contains("broker unavailable");
    }

    private static final class MemoryOutboxMapper implements OmsOutboxMapper {
        private final List<OutboxMessage> rows = new ArrayList<>();
        private final List<Long> published = new ArrayList<>();
        private final List<Long> failed = new ArrayList<>();
        private String lastError;

        @Override
        public List<OutboxMessage> pending(int limit) {
            return rows.stream().limit(limit).toList();
        }

        @Override
        public int claim(long id) {
            return 1;
        }

        @Override
        public int markPublished(long id) {
            published.add(id);
            return 1;
        }

        @Override
        public int markFailed(long id, String error) {
            failed.add(id);
            lastError = error;
            return 1;
        }
    }
}
