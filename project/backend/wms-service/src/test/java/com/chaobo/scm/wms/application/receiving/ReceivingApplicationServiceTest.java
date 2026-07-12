package com.chaobo.scm.wms.application.receiving;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.domain.receiving.ReceiptAggregate;
import com.chaobo.scm.wms.domain.receiving.ReceiptRepository;
import com.chaobo.scm.wms.infrastructure.persistence.receiving.ReceiptScanMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReceivingApplicationServiceTest {
    private final InMemoryReceiptRepository receipts = new InMemoryReceiptRepository();
    private final RecordingEventPublisher events = new RecordingEventPublisher();
    private final InMemoryReceiptScanMapper scans = new InMemoryReceiptScanMapper();
    private final ReceivingApplicationService service = new ReceivingApplicationService(receipts, events, scans);

    @Test
    void openScanAndSubmitReceiptPublishesArrivalAndCompletedEvents() {
        var opened = service.open(new ReceivingApplicationService.Open(
                "REC-001",
                10L,
                "SKU-001",
                new BigDecimal("10")
        ), 99L);

        assertThat(opened.duplicated()).isFalse();
        assertThat(opened.version()).isZero();
        assertThat(events.types()).containsExactly("WmsArrivalRegistered");

        var scanned = service.scan(new ReceivingApplicationService.Scan(
                "REC-001",
                0,
                new BigDecimal("8"),
                new BigDecimal("2"),
                "外箱破损",
                "scan-key-1"
        ), 99L);

        assertThat(scanned.version()).isEqualTo(1);

        var submitted = service.submit("REC-001", 1, 99L);

        assertThat(submitted.statusName()).isEqualTo("已收货");
        assertThat(events.types()).containsExactly("WmsArrivalRegistered", "WmsReceiptCompleted");
    }

    @Test
    void repeatedScanWithSameIdempotencyKeyReturnsCurrentReceiptWithoutDoubleCounting() {
        service.open(new ReceivingApplicationService.Open("REC-002", 10L, "SKU-001", BigDecimal.TEN), 99L);
        service.scan(new ReceivingApplicationService.Scan(
                "REC-002",
                0,
                BigDecimal.ONE,
                BigDecimal.ZERO,
                null,
                "scan-key-2"
        ), 99L);

        var duplicated = service.scan(new ReceivingApplicationService.Scan(
                "REC-002",
                1,
                BigDecimal.ONE,
                BigDecimal.ZERO,
                null,
                "scan-key-2"
        ), 99L);

        var receipt = receipts.findByNo("REC-002").orElseThrow();
        assertThat(duplicated.duplicated()).isTrue();
        assertThat(receipt.receivedQty()).isEqualByComparingTo("1");
        assertThat(scans.insertCount).isEqualTo(1);
    }

    @Test
    void scanRequiresIdempotencyKeyAndVersionMatch() {
        service.open(new ReceivingApplicationService.Open("REC-003", 10L, "SKU-001", BigDecimal.TEN), 99L);

        assertThatThrownBy(() -> service.scan(new ReceivingApplicationService.Scan(
                "REC-003",
                0,
                BigDecimal.ONE,
                BigDecimal.ZERO,
                null,
                null
        ), 99L)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("幂等键");

        assertThatThrownBy(() -> service.scan(new ReceivingApplicationService.Scan(
                "REC-003",
                9,
                BigDecimal.ONE,
                BigDecimal.ZERO,
                null,
                "scan-key-3"
        ), 99L)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("版本冲突");
    }

    private static class InMemoryReceiptRepository implements ReceiptRepository {
        private final Map<String, ReceiptAggregate> values = new HashMap<>();

        @Override
        public Optional<ReceiptAggregate> findByNo(String no) {
            return Optional.ofNullable(values.get(no));
        }

        @Override
        public void save(ReceiptAggregate receipt, long operator) {
            values.put(receipt.receiptNo(), receipt);
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

    private static class InMemoryReceiptScanMapper implements ReceiptScanMapper {
        private final Map<String, Long> keys = new HashMap<>();
        private int insertCount;

        @Override
        public int exists(long receiptId, String key) {
            return keys.containsKey(receiptId + ":" + key) ? 1 : 0;
        }

        @Override
        public void insert(
                long id,
                long receiptId,
                String key,
                BigDecimal received,
                BigDecimal rejected,
                String reason,
                long operator
        ) {
            keys.put(receiptId + ":" + key, id);
            insertCount++;
        }
    }
}
