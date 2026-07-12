package com.chaobo.scm.purchase.application.integration;

import com.chaobo.scm.purchase.infrastructure.persistence.integration.PurchaseExternalFactMapper;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseExternalEventConsumerApplicationServiceTest {
    private final FakeInbox inbox = new FakeInbox();
    private final FakeFacts facts = new FakeFacts();
    private final PurchaseExternalEventConsumerApplicationService service =
            new PurchaseExternalEventConsumerApplicationService(inbox,
                    new InboundEventPayloadStore(inbox, new ObjectMapper()), facts.proxy(), null, null,
                    new ObjectMapper());

    @Test
    void supplierQuoteEventWritesQuoteFactAndMarksInboxSucceeded() {
        service.consume(new PurchaseExternalEvent("SUPPLIER", "EVT-1", "SupplierQuoteSubmitted",
                null, null, "RFQ001", "Q001", null, null, 3001L, null,
                null, "SKU-01", new BigDecimal("10"), null, null, null, null,
                new BigDecimal("99.00"), "CNY", null, null, null, null,
                null, null, 1, null, Map.of("score", 90)));

        assertThat(facts.quoteNo).isEqualTo("Q001");
        assertThat(facts.rfqNo).isEqualTo("RFQ001");
        assertThat(facts.supplierId).isEqualTo(3001L);
        assertThat(inbox.status).isEqualTo(2);
        assertThat(inbox.payloadJson).contains("SupplierQuoteSubmitted");
    }

    @Test
    void duplicateSucceededEventIsIgnored() {
        inbox.claimResult = InboundEventLogPort.ClaimResult.ALREADY_SUCCEEDED;

        service.consume(new PurchaseExternalEvent("SUPPLIER", "EVT-1", "SupplierQuoteSubmitted",
                null, null, "RFQ001", "Q001", null, null, 3001L, null,
                null, "SKU-01", BigDecimal.ONE, null, null, null, null,
                BigDecimal.TEN, "CNY", null, null, null, null,
                null, null, 1, null, Map.of()));

        assertThat(facts.quoteNo).isNull();
    }

    private static final class FakeInbox implements InboundEventLogPort {
        private ClaimResult claimResult = ClaimResult.CLAIMED;
        private int status;
        private String payloadJson;

        @Override
        public ClaimResult claim(String sourceSystem, String eventCode, String eventType,
                                 String consumerName, String idempotentKey) {
            return claimResult;
        }

        @Override
        public void savePayload(String sourceSystem, String eventCode, String consumerName, String payloadJson) {
            this.payloadJson = payloadJson;
        }

        @Override
        public void markSucceeded(String sourceSystem, String eventCode, String consumerName, boolean ignored) {
            this.status = ignored ? 4 : 2;
        }

        @Override
        public void recordFailure(String sourceSystem, String eventCode, String eventType,
                                  String consumerName, String idempotentKey, String reason) {
            this.status = 3;
        }

        @Override
        public Optional<ReplayEvent> findForReplay(long consumeLogId) {
            return Optional.empty();
        }

        @Override
        public void markReplayRequested(long consumeLogId, long operatorId, String reason) {
        }
    }

    private static final class FakeFacts {
        private String quoteNo;
        private String rfqNo;
        private long supplierId;

        PurchaseExternalFactMapper proxy() {
            return (PurchaseExternalFactMapper) Proxy.newProxyInstance(
                    PurchaseExternalFactMapper.class.getClassLoader(),
                    new Class<?>[]{PurchaseExternalFactMapper.class},
                    (target, method, args) -> {
                        if ("upsertQuote".equals(method.getName())) {
                            quoteNo = (String) args[1];
                            rfqNo = (String) args[2];
                            supplierId = (Long) args[3];
                            return null;
                        }
                        if (method.getReturnType().equals(Void.TYPE)) {
                            return null;
                        }
                        throw new UnsupportedOperationException(method.getName());
                    });
        }
    }
}
