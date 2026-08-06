package com.chaobo.scm.tms.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.integration.TmsCollaborationApi;
import com.chaobo.scm.tms.infrastructure.persistence.TmsCollaborationMapper;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 验证 Dubbo 运输协作命令的持久化、幂等与取消状态边界。 */
class TmsCollaborationApplicationServiceTest {

    @Test
    void shouldReturnSameRequestForRepeatedInboundCommand() {
        InMemoryMapper mapper = new InMemoryMapper();
        TmsCollaborationApplicationService service = new TmsCollaborationApplicationService(mapper);
        TmsCollaborationApi.InboundTransportCommand command = inbound("idem-1", "TRACK-1");

        TmsCollaborationApi.TransportResult first = service.createInbound(command);
        TmsCollaborationApi.TransportResult repeated = service.createInbound(command);

        assertThat(repeated).isEqualTo(first);
        assertThat(mapper.requests).hasSize(1);
    }

    @Test
    void shouldRejectSameIdempotencyKeyWithDifferentContent() {
        InMemoryMapper mapper = new InMemoryMapper();
        TmsCollaborationApplicationService service = new TmsCollaborationApplicationService(mapper);
        service.createInbound(inbound("idem-1", "TRACK-1"));

        assertThatThrownBy(() -> service.createInbound(inbound("idem-1", "TRACK-2")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.code())
                                .isEqualTo(ErrorCode.IDEMPOTENCY_CONFLICT));
    }

    @Test
    void shouldPersistCancelReceiptAndReplaySafely() {
        InMemoryMapper mapper = new InMemoryMapper();
        TmsCollaborationApplicationService service = new TmsCollaborationApplicationService(mapper);
        service.createInbound(inbound("idem-create", "TRACK-1"));
        TmsCollaborationApi.CancelTransportCommand cancel =
                new TmsCollaborationApi.CancelTransportCommand("idem-cancel", "ASN", 101L,
                        "供应商取消发运");

        service.cancel(cancel);
        service.cancel(cancel);

        assertThat(mapper.findByBusiness("ASN", 101L).status()).isEqualTo(3);
        assertThat(mapper.receipts).hasSize(1);
    }

    private static TmsCollaborationApi.InboundTransportCommand inbound(String key,
                                                                        String trackingNo) {
        return new TmsCollaborationApi.InboundTransportCommand(key, 101L, "ASN-101", 201L,
                301L, OffsetDateTime.parse("2026-08-06T10:00:00+08:00"), "SF", trackingNo);
    }

    /** 使用内存集合精确模拟 Mapper 契约，不引入 Mockito 的 JVM attach 依赖。 */
    private static final class InMemoryMapper implements TmsCollaborationMapper {
        private final Map<String, Request> requests = new HashMap<>();
        private final Map<String, Receipt> receipts = new HashMap<>();

        @Override
        public Request findByIdempotency(String key) {
            return requests.values().stream()
                    .filter(request -> request.idempotencyKey().equals(key))
                    .findFirst().orElse(null);
        }

        @Override
        public Request findByBusiness(String type, long id) {
            return requests.values().stream()
                    .filter(request -> request.businessType().equals(type)
                            && request.businessId() == id)
                    .findFirst().orElse(null);
        }

        @Override
        public void insert(Request request) {
            requests.put(request.requestId(), request);
        }

        @Override
        public int cancel(String requestId, String reason) {
            Request current = requests.get(requestId);
            if (current == null || (current.status() != 1 && current.status() != 2)) {
                return 0;
            }
            requests.put(requestId, new Request(current.requestId(), current.idempotencyKey(),
                    current.commandType(), current.requestFingerprint(), current.businessType(),
                    current.businessId(), current.businessNo(), current.shipperId(),
                    current.warehouseId(), current.carrierCode(), current.trackingNo(),
                    current.requestPayload(), 3, reason, current.version() + 1));
            return 1;
        }

        @Override
        public Receipt findReceipt(String key) {
            return receipts.get(key);
        }

        @Override
        public void insertReceipt(Receipt receipt) {
            receipts.put(receipt.idempotencyKey(), receipt);
        }
    }
}
