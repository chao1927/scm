package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.domain.TransportTaskAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TrackingMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * TMS 跨上下文业务事件消费应用服务。
 *
 * <p>真实 RocketMQ 消费者把标准信封交给本服务。服务先声明 Inbox，再执行领域命令；
 * 成功记录完成，失败记录原因并抛出，让 RocketMQ Broker 重投。
 *
 * @author SCM Team
 */
@Service
public class TmsInboundEventApplicationService {

    private static final Set<String> TASK_REQUEST_EVENTS = Set.of(
        "SalesDeliveryRequested", "ReturnPickupRequested", "AsnSubmitted",
        "SupplierAsnSubmitted", "SupplierReturnApproved", "TransferOutboundShipped");
    private static final int INBOX_PROCESSING = 1;
    private static final int INBOX_SUCCEEDED = 2;
    private static final int INBOX_FAILED = 3;
    private final TrackingMapper inbox;
    private final TransportTaskApplicationService taskService;
    private final ObjectMapper objectMapper;

    public TmsInboundEventApplicationService(TrackingMapper inbox,
                                             TransportTaskApplicationService taskService,
                                             ObjectMapper objectMapper) {
        this.inbox = inbox;
        this.taskService = taskService;
        this.objectMapper = objectMapper;
    }

    /**
     * 幂等消费标准业务事件。
     */
    @Transactional(rollbackFor = Exception.class)
    public void consume(EventEnvelope event) {
        TrackingMapper.EventInboxRow existing = inbox.findEvent(event.eventCode());
        if (existing != null && existing.status() == INBOX_SUCCEEDED) {
            return;
        }
        int claimed = existing == null
            ? inbox.claimEvent(new TrackingMapper.EventInboxRow(
                event.eventCode(), event.eventType(), event.aggregateNo(),
                event.data().toString(), INBOX_PROCESSING, null))
            : inbox.reclaimFailedEvent(event.eventCode());
        if (claimed == 0) {
            throw new IllegalStateException("TMS event is already being processed");
        }
        try {
            dispatch(event);
            inbox.updateEvent(new TrackingMapper.EventInboxRow(
                event.eventCode(), event.eventType(), event.aggregateNo(),
                event.data().toString(), INBOX_SUCCEEDED, null));
        } catch (RuntimeException exception) {
            inbox.updateEvent(new TrackingMapper.EventInboxRow(
                event.eventCode(), event.eventType(), event.aggregateNo(),
                event.data().toString(), INBOX_FAILED, exception.getMessage()));
            throw exception;
        }
    }

    private void dispatch(EventEnvelope event) {
        if (!TASK_REQUEST_EVENTS.contains(event.eventType())) {
            throw new IllegalArgumentException(
                "unsupported TMS inbound event: " + event.eventType());
        }
        JsonNode data = event.data();
        String sourceOrderNo = text(data, "sourceOrderNo", event.aggregateNo());
        var origin = objectMapper.convertValue(
            required(data, "originAddress"), TransportTaskAggregate.Address.class);
        var destination = objectMapper.convertValue(
            required(data, "destinationAddress"), TransportTaskAggregate.Address.class);
        var packageType = objectMapper.getTypeFactory().constructCollectionType(
            List.class, TransportTaskAggregate.PackageItem.class);
        List<TransportTaskAggregate.PackageItem> packages =
            objectMapper.convertValue(required(data, "packages"), packageType);
        taskService.createFromSource(new TransportTaskApplicationService.CreateCommand(
            event.sourceSystem(), sourceOrderNo, text(data, "sourceLineNo", null),
            text(data, "scenario", defaultScenario(event.eventType())),
            number(data, "shipperId"), number(data, "warehouseId"),
            origin, destination, packages,
            text(data, "logisticsProductCode", "STANDARD"),
            text(data, "feeResponsibility", "SHIPPER"),
            null, event.eventCode()));
    }

    private static JsonNode required(JsonNode data, String name) {
        JsonNode value = data.get(name);
        if (value == null || value.isNull()) {
            throw new IllegalArgumentException("TMS inbound event data missing: " + name);
        }
        return value;
    }

    private static Long number(JsonNode data, String name) {
        JsonNode value = required(data, name);
        if (!value.canConvertToLong()) {
            throw new IllegalArgumentException("TMS inbound event data is not integer: " + name);
        }
        return value.longValue();
    }

    private static String text(JsonNode data, String name, String fallback) {
        JsonNode value = data.get(name);
        String result = value == null || value.isNull() ? fallback : value.asText();
        if (result == null || result.isBlank()) {
            throw new IllegalArgumentException("TMS inbound event data missing: " + name);
        }
        return result;
    }

    private static String defaultScenario(String eventType) {
        return switch (eventType) {
            case "SalesDeliveryRequested" -> "SALES_DELIVERY";
            case "ReturnPickupRequested" -> "SALES_RETURN";
            case "AsnSubmitted", "SupplierAsnSubmitted" -> "PURCHASE_INBOUND";
            case "SupplierReturnApproved" -> "SUPPLIER_RETURN";
            case "TransferOutboundShipped" -> "TRANSFER";
            default -> throw new IllegalArgumentException(
                "unsupported TMS task request event: " + eventType);
        };
    }

    public record EventEnvelope(String sourceSystem, String eventCode, String eventType,
                                String aggregateNo, JsonNode data) {
    }
}
