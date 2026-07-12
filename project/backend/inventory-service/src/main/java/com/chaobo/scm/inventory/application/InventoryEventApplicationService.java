package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.inventory.infrastructure.persistence.InventoryEventMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InventoryEventApplicationService {
    private final InventoryEventMapper events;
    private final InventoryApplicationService inventory;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    public InventoryEventApplicationService(InventoryEventMapper events, InventoryApplicationService inventory) {
        this.events = events;
        this.inventory = inventory;
    }

    public void publish(String type, String aggregateType, String aggregateId, String payload) {
        long id = ids.incrementAndGet();
        events.insertOutbox(id, "INV-" + type + "-" + id, type, aggregateType, aggregateId, payload);
    }

    @Transactional
    public DispatchResult dispatch(int limit) {
        int published = 0;
        int failed = 0;
        for (var event : events.pending(limit <= 0 ? 50 : Math.min(limit, 200))) {
            try {
                events.markPublished(event.id());
                published++;
            } catch (RuntimeException ex) {
                events.markFailed(event.id());
                failed++;
            }
        }
        return new DispatchResult(published, failed);
    }

    @Transactional
    public ConsumeResult consumeWmsEvent(EventEnvelope envelope) {
        validate(envelope);
        var existed = events.findInbox(envelope.sourceSystem(), envelope.eventCode());
        if (existed == null) {
            events.insertInbox(envelope.sourceSystem(), envelope.eventCode(), envelope.eventType(), envelope.payload());
            existed = events.findInbox(envelope.sourceSystem(), envelope.eventCode());
        }
        if (existed.status() == 2) {
            return new ConsumeResult(true, "事件已处理");
        }
        try {
            dispatchWms(envelope);
            events.markInboxSucceeded(existed.id());
            return new ConsumeResult(false, "处理成功");
        } catch (RuntimeException ex) {
            events.markInboxFailed(existed.id(), ex.getMessage());
            throw ex;
        }
    }

    private void dispatchWms(EventEnvelope envelope) {
        var payload = SimplePayload.parse(envelope.payload());
        var command = new InventoryApplicationService.AccountCommand(
                payload.longValue("ownerId"),
                payload.longValue("warehouseId"),
                payload.text("sku"),
                payload.optional("batchNo"),
                payload.decimal("qty"),
                envelope.sourceSystem(),
                payload.text("sourceNo")
        );
        switch (envelope.eventType()) {
            case "WmsPutawayCompleted" -> inventory.inbound(command);
            case "WmsShipmentHandedOver" -> inventory.outbound(command);
            case "WmsStocktakeDifferenceConfirmed" -> inventory.adjust(command);
            default -> throw new BusinessException(ErrorCode.VALIDATION_FAILED, "不支持的WMS库存事件");
        }
        publish("InventoryChanged", "INVENTORY_ACCOUNT", command.sourceNo(), envelope.payload());
    }

    private static void validate(EventEnvelope envelope) {
        if (envelope.sourceSystem() == null || envelope.sourceSystem().isBlank()
                || envelope.eventCode() == null || envelope.eventCode().isBlank()
                || envelope.eventType() == null || envelope.eventType().isBlank()
                || envelope.payload() == null || envelope.payload().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "库存入站事件信封不完整");
        }
    }

    public record EventEnvelope(String sourceSystem, String eventCode, String eventType, String payload) {}
    public record ConsumeResult(boolean duplicated, String message) {}
    public record DispatchResult(int published, int failed) {}

    static class SimplePayload {
        private final java.util.Map<String, String> values;
        private SimplePayload(java.util.Map<String, String> values) { this.values = values; }
        static SimplePayload parse(String json) {
            var map = new java.util.HashMap<String, String>();
            var body = json.trim().replaceAll("^\\{", "").replaceAll("}$", "");
            if (!body.isBlank()) {
                for (var part : body.split(",")) {
                    var idx = part.indexOf(':');
                    if (idx > 0) {
                        var key = part.substring(0, idx).trim().replace("\"", "");
                        var value = part.substring(idx + 1).trim().replace("\"", "");
                        map.put(key, value);
                    }
                }
            }
            return new SimplePayload(map);
        }
        String text(String key) {
            var value = values.get(key);
            if (value == null || value.isBlank()) throw new BusinessException(ErrorCode.VALIDATION_FAILED, "载荷缺少字段: " + key);
            return value;
        }
        String optional(String key) { return values.get(key); }
        long longValue(String key) { return Long.parseLong(text(key)); }
        BigDecimal decimal(String key) { return new BigDecimal(text(key)); }
    }
}
