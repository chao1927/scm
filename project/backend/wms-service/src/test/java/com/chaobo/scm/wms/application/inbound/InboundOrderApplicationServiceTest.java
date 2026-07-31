package com.chaobo.scm.wms.application.inbound;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.domain.inbound.InboundOrderAggregate;
import com.chaobo.scm.wms.domain.inbound.InboundOrderRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InboundOrderApplicationServiceTest {

    private final InMemoryRepository repository = new InMemoryRepository();
    private final RecordingEvents events = new RecordingEvents();
    private final InboundOrderApplicationService service = new InboundOrderApplicationService(repository, events);

    @Test
    void createsThreeCanonicalInboundTypesAndNormalizesExternalAliases() {
        var purchase = service.create(command("SUPPLIER", "ASN", "PO-1", "1", 10, 20, "10"), 99);
        var transfer = service.create(command("INVENTORY", "INVENTORY_TRANSFER", "TRF-1", "SKU-1", 11, 20, "8"), 99);
        var salesReturn = service.create(command("OMS", "AFTERSALE_RETURN", "AS-1", "RMA-1", 12, 20, "3"), 99);

        assertThat(purchase.duplicated()).isFalse();
        assertThat(transfer.duplicated()).isFalse();
        assertThat(salesReturn.duplicated()).isFalse();
        assertThat(repository.values.values()).extracting(InboundOrderAggregate::sourceType)
            .containsExactlyInAnyOrder("PURCHASE", "TRANSFER", "SALES_RETURN");
        assertThat(events.types).containsExactly("WmsInboundOrderCreated", "WmsInboundOrderCreated",
            "WmsInboundOrderCreated");
        assertThat(events.payloads).allSatisfy(payload -> assertThat(payload)
            .contains("\"sourceSystem\"").contains("\"sourceLineNo\"").contains("\"allowedQty\""));
    }

    @Test
    void exactDuplicateIsIdempotentButChangedSnapshotConflicts() {
        var command = command("INVENTORY", "TRANSFER", "TRF-2", "SKU-2", 11, 20, "8");
        service.create(command, 99);

        assertThat(service.create(command, 99).duplicated()).isTrue();
        assertThat(events.types).containsExactly("WmsInboundOrderCreated");
        assertThatThrownBy(() -> service.create(command("INVENTORY", "TRANSFER", "TRF-2",
            "SKU-2", 99, 20, "8"), 99)).isInstanceOf(BusinessException.class)
            .hasMessageContaining("快照不一致");
    }

    @Test
    void rejectsCrossContextTypeSpoofing() {
        assertThatThrownBy(() -> service.create(command("OMS", "PURCHASE", "PO-9", "1",
            10, 20, "1"), 99)).isInstanceOf(BusinessException.class)
            .hasMessageContaining("来源系统不匹配");
    }

    private static InboundOrderApplicationService.Create command(String sourceSystem, String inboundType,
                                                                  String sourceNo, String sourceLineNo,
                                                                  long warehouseId, long ownerId,
                                                                  String allowedQty) {
        return new InboundOrderApplicationService.Create(sourceSystem, inboundType, sourceNo, sourceLineNo,
            warehouseId, ownerId, new BigDecimal(allowedQty), null,
            sourceSystem + ":" + sourceNo + ":" + sourceLineNo);
    }

    private static final class InMemoryRepository implements InboundOrderRepository {
        private final Map<String, InboundOrderAggregate> values = new HashMap<>();

        @Override
        public Optional<InboundOrderAggregate> findById(long id) {
            return values.values().stream().filter(order -> order.id() == id).findFirst();
        }

        @Override
        public Optional<InboundOrderAggregate> findBySource(String sourceSystem, String sourceNo,
                                                             String sourceLineNo, String inboundType) {
            return Optional.ofNullable(values.get(key(sourceSystem, sourceNo, sourceLineNo, inboundType)));
        }

        @Override
        public void save(InboundOrderAggregate order, long operatorId) {
            values.put(key(order.sourceSystem(), order.sourceNo(), order.sourceLineNo(), order.sourceType()), order);
        }

        private static String key(String sourceSystem, String sourceNo, String sourceLineNo,
                                  String inboundType) {
            return sourceSystem + ":" + sourceNo + ":" + sourceLineNo + ":" + inboundType;
        }
    }

    private static final class RecordingEvents implements WmsEventPublisher {
        private final List<String> types = new ArrayList<>();
        private final List<String> payloads = new ArrayList<>();

        @Override
        public void publish(String eventType, String aggregateType, String aggregateId, int version,
                            String payload) {
            types.add(eventType);
            payloads.add(payload);
        }
    }
}
