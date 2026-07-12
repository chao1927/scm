package com.chaobo.scm.wms.application.wave;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.infrastructure.persistence.picking.PickTaskMapper;
import com.chaobo.scm.wms.infrastructure.persistence.wave.WaveMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WavePickingApplicationServiceTest {
    private final InMemoryWaveMapper waves = new InMemoryWaveMapper();
    private final InMemoryPickTaskMapper picks = new InMemoryPickTaskMapper();
    private final RecordingEventPublisher events = new RecordingEventPublisher();
    private final WavePickingApplicationService service = new WavePickingApplicationService(waves, picks, events);

    @Test
    void createReleaseWaveAndCompletePickTaskPublishesEvents() {
        var wave = service.createWave("WAV-001", 1L);
        var released = service.releaseWave("WAV-001", 0);
        var task = service.createPickTask("PICK-001", wave.id(), 10L, "SKU-001", BigDecimal.TEN);

        assertThat(released.status()).isEqualTo(2);
        assertThat(task.duplicated()).isFalse();

        service.scanPick("PICK-001", 0, new BigDecimal("4"));
        var completed = service.scanPick("PICK-001", 1, new BigDecimal("6"));

        assertThat(completed.status()).isEqualTo(3);
        assertThat(events.types()).containsExactly("WmsWaveCreated", "WmsWaveReleased", "WmsPickCompleted");
    }

    @Test
    void repeatedCreateReturnsExistingWaveAndPickTask() {
        service.createWave("WAV-002", 1L);
        var duplicatedWave = service.createWave("WAV-002", 1L);
        service.createPickTask("PICK-002", duplicatedWave.id(), 10L, "SKU-001", BigDecimal.TEN);
        var duplicatedPick = service.createPickTask("PICK-002", duplicatedWave.id(), 10L, "SKU-001", BigDecimal.TEN);

        assertThat(duplicatedWave.duplicated()).isTrue();
        assertThat(duplicatedPick.duplicated()).isTrue();
    }

    @Test
    void releaseAndPickRequireExpectedVersion() {
        service.createWave("WAV-003", 1L);
        service.createPickTask("PICK-003", 1L, 10L, "SKU-001", BigDecimal.TEN);

        assertThatThrownBy(() -> service.releaseWave("WAV-003", 9)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.scanPick("PICK-003", 9, BigDecimal.ONE)).isInstanceOf(BusinessException.class);
    }

    private static class InMemoryWaveMapper implements WaveMapper {
        private final List<Row> rows = new ArrayList<>();

        @Override
        public Row find(String no) {
            return rows.stream().filter(row -> row.no().equals(no)).findFirst().orElse(null);
        }

        @Override
        public void insert(long id, String no, long warehouseId, int status, int version) {
            rows.add(new Row(id, no, warehouseId, status, version));
        }

        @Override
        public int update(long id, int status, int version, int oldVersion) {
            var row = rows.stream().filter(value -> value.id() == id && value.version() == oldVersion).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            rows.set(rows.indexOf(row), new Row(row.id(), row.no(), row.warehouseId(), status, version));
            return 1;
        }
    }

    private static class InMemoryPickTaskMapper implements PickTaskMapper {
        private final List<Row> rows = new ArrayList<>();

        @Override
        public Row find(String no) {
            return rows.stream().filter(row -> row.no().equals(no)).findFirst().orElse(null);
        }

        @Override
        public void insert(
                long id,
                String no,
                long waveId,
                long outboundId,
                String sku,
                BigDecimal required,
                BigDecimal picked,
                int status,
                int version
        ) {
            rows.add(new Row(id, no, waveId, outboundId, sku, required, picked, status, version));
        }

        @Override
        public int update(long id, BigDecimal picked, int status, int version, int oldVersion) {
            var row = rows.stream().filter(value -> value.id() == id && value.version() == oldVersion).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            rows.set(rows.indexOf(row), new Row(
                    row.id(),
                    row.no(),
                    row.waveId(),
                    row.outboundId(),
                    row.sku(),
                    row.required(),
                    picked,
                    status,
                    version
            ));
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
