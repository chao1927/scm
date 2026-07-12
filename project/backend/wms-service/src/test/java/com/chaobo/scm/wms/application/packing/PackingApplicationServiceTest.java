package com.chaobo.scm.wms.application.packing;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.infrastructure.persistence.packing.ContainerMapper;
import com.chaobo.scm.wms.infrastructure.persistence.packing.PackingMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PackingApplicationServiceTest {
    private final InMemoryContainerMapper containers = new InMemoryContainerMapper();
    private final InMemoryPackingMapper packings = new InMemoryPackingMapper();
    private final RecordingEventPublisher events = new RecordingEventPublisher();
    private final PackingApplicationService service = new PackingApplicationService(containers, packings, events);

    @Test
    void bindSealCreateAndVerifyPackingPublishesEvents() {
        var bound = service.bindContainer("CT-001", 10L, 20L);
        var sealed = service.sealContainer("CT-001", 0);
        var packing = service.createPacking("PKG-001", 10L, "CT-001");
        var verified = service.verifyPacking("PKG-001", 0);

        assertThat(bound.duplicated()).isFalse();
        assertThat(sealed.status()).isEqualTo(2);
        assertThat(packing.duplicated()).isFalse();
        assertThat(verified.status()).isEqualTo(2);
        assertThat(events.types()).containsExactly("WmsContainerBound", "WmsContainerSealed", "WmsPackingVerified");
    }

    @Test
    void repeatedCreateReturnsExistingRows() {
        service.bindContainer("CT-002", 10L, 20L);
        service.createPacking("PKG-002", 10L, "CT-002");

        assertThat(service.bindContainer("CT-002", 10L, 20L).duplicated()).isTrue();
        assertThat(service.createPacking("PKG-002", 10L, "CT-002").duplicated()).isTrue();
    }

    @Test
    void sealAndVerifyRequireExpectedVersion() {
        service.bindContainer("CT-003", 10L, 20L);
        service.createPacking("PKG-003", 10L, "CT-003");

        assertThatThrownBy(() -> service.sealContainer("CT-003", 9)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.verifyPacking("PKG-003", 9)).isInstanceOf(BusinessException.class);
    }

    private static class InMemoryContainerMapper implements ContainerMapper {
        private final List<Row> rows = new ArrayList<>();

        @Override
        public Row find(String containerNo) {
            return rows.stream().filter(row -> row.containerNo().equals(containerNo)).findFirst().orElse(null);
        }

        @Override
        public void insert(long id, String containerNo, long outboundId, long pickTaskId, int status, int version) {
            rows.add(new Row(id, containerNo, outboundId, pickTaskId, status, version));
        }

        @Override
        public int update(long id, int status, int version, int oldVersion) {
            var row = rows.stream().filter(value -> value.id() == id && value.version() == oldVersion).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            rows.set(rows.indexOf(row), new Row(row.id(), row.containerNo(), row.outboundId(), row.pickTaskId(), status, version));
            return 1;
        }
    }

    private static class InMemoryPackingMapper implements PackingMapper {
        private final List<Row> rows = new ArrayList<>();

        @Override
        public Row find(String packingNo) {
            return rows.stream().filter(row -> row.packingNo().equals(packingNo)).findFirst().orElse(null);
        }

        @Override
        public void insert(long id, String packingNo, long outboundId, String containerNo, int status, int version) {
            rows.add(new Row(id, packingNo, outboundId, containerNo, status, version));
        }

        @Override
        public int update(long id, int status, int version, int oldVersion) {
            var row = rows.stream().filter(value -> value.id() == id && value.version() == oldVersion).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            rows.set(rows.indexOf(row), new Row(row.id(), row.packingNo(), row.outboundId(), row.containerNo(), status, version));
            return 1;
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
}
