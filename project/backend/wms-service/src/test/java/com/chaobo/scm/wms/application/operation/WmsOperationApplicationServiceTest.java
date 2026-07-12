package com.chaobo.scm.wms.application.operation;

import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.infrastructure.persistence.operation.ShipmentHandoverMapper;
import com.chaobo.scm.wms.infrastructure.persistence.operation.StocktakeMapper;
import com.chaobo.scm.wms.infrastructure.persistence.operation.WarehouseExceptionMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WmsOperationApplicationServiceTest {
    private final HandoverMemory handovers = new HandoverMemory();
    private final StocktakeMemory stocktakes = new StocktakeMemory();
    private final ExceptionMemory exceptions = new ExceptionMemory();
    private final RecordingEventPublisher events = new RecordingEventPublisher();
    private final WmsOperationApplicationService service = new WmsOperationApplicationService(handovers, stocktakes, exceptions, events);

    @Test
    void confirmHandoverStocktakeAndExceptionPublishesEvents() {
        service.createHandover("HO-001", 10L);
        service.confirmHandover("HO-001", 0);
        service.createStocktake("ST-001", 1L, "SKU-001", BigDecimal.ONE);
        service.confirmStocktake("ST-001", 0);
        service.createException("EX-001", "少拣");
        service.closeException("EX-001", 0);

        assertThat(events.types()).containsExactly(
                "WmsShipmentHandedOver",
                "WmsStocktakeDifferenceConfirmed",
                "WmsWarehouseExceptionCreated",
                "WmsWarehouseExceptionClosed"
        );
    }

    @Test
    void repeatedCreateIsIdempotent() {
        service.createHandover("HO-002", 10L);
        service.createStocktake("ST-002", 1L, "SKU-001", BigDecimal.ONE);
        service.createException("EX-002", "错货");

        assertThat(service.createHandover("HO-002", 10L).duplicated()).isTrue();
        assertThat(service.createStocktake("ST-002", 1L, "SKU-001", BigDecimal.ONE).duplicated()).isTrue();
        assertThat(service.createException("EX-002", "错货").duplicated()).isTrue();
    }

    private static class HandoverMemory implements ShipmentHandoverMapper {
        private final List<Row> rows = new ArrayList<>();
        public Row find(String no) { return rows.stream().filter(row -> row.no().equals(no)).findFirst().orElse(null); }
        public void insert(long id, String no, long outboundId, int status, int version) { rows.add(new Row(id, no, outboundId, status, version)); }
        public int update(long id, int status, int version, int oldVersion) { var row = rows.stream().filter(v -> v.id() == id && v.version() == oldVersion).findFirst().orElse(null); if (row == null) return 0; rows.set(rows.indexOf(row), new Row(row.id(), row.no(), row.outboundId(), status, version)); return 1; }
    }

    private static class StocktakeMemory implements StocktakeMapper {
        private final List<Row> rows = new ArrayList<>();
        public Row find(String no) { return rows.stream().filter(row -> row.no().equals(no)).findFirst().orElse(null); }
        public void insert(long id, String no, long warehouseId, String sku, BigDecimal differenceQty, int status, int version) { rows.add(new Row(id, no, warehouseId, sku, differenceQty, status, version)); }
        public int update(long id, int status, int version, int oldVersion) { var row = rows.stream().filter(v -> v.id() == id && v.version() == oldVersion).findFirst().orElse(null); if (row == null) return 0; rows.set(rows.indexOf(row), new Row(row.id(), row.no(), row.warehouseId(), row.sku(), row.differenceQty(), status, version)); return 1; }
    }

    private static class ExceptionMemory implements WarehouseExceptionMapper {
        private final List<Row> rows = new ArrayList<>();
        public Row find(String no) { return rows.stream().filter(row -> row.no().equals(no)).findFirst().orElse(null); }
        public void insert(long id, String no, String reason, int status, int version) { rows.add(new Row(id, no, reason, status, version)); }
        public int update(long id, int status, int version, int oldVersion) { var row = rows.stream().filter(v -> v.id() == id && v.version() == oldVersion).findFirst().orElse(null); if (row == null) return 0; rows.set(rows.indexOf(row), new Row(row.id(), row.no(), row.reason(), status, version)); return 1; }
    }

    private static class RecordingEventPublisher implements WmsEventPublisher {
        private final List<String> eventTypes = new ArrayList<>();
        public void publish(String eventType, String aggregateType, String aggregateId, int version, String payload) { eventTypes.add(eventType); }
        List<String> types() { return eventTypes; }
    }
}
