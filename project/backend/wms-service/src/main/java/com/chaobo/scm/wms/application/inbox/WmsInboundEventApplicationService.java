package com.chaobo.scm.wms.application.inbox;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.application.inbound.InboundOrderApplicationService;
import com.chaobo.scm.wms.application.outbound.OutboundApplicationService;
import com.chaobo.scm.wms.infrastructure.persistence.event.WmsInboxMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class WmsInboundEventApplicationService {
    private final WmsInboxMapper inbox;
    private final InboundOrderApplicationService inboundOrders;
    private final OutboundApplicationService outboundOrders;
    private final ObjectMapper objectMapper;

    public WmsInboundEventApplicationService(
            WmsInboxMapper inbox,
            InboundOrderApplicationService inboundOrders,
            OutboundApplicationService outboundOrders,
            ObjectMapper objectMapper
    ) {
        this.inbox = inbox;
        this.inboundOrders = inboundOrders;
        this.outboundOrders = outboundOrders;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ConsumeResult consume(EventEnvelope envelope, long operatorId) {
        validate(envelope);
        var existed = inbox.find(envelope.sourceSystem(), envelope.eventCode());
        if (existed == null) {
            inbox.insert(envelope.sourceSystem(), envelope.eventCode(), envelope.eventType(), envelope.payload());
            existed = inbox.find(envelope.sourceSystem(), envelope.eventCode());
        }
        if (existed.status() == 2) {
            return new ConsumeResult(true, "事件已处理");
        }

        try {
            dispatch(existed, operatorId);
            inbox.markSucceeded(existed.id());
            return new ConsumeResult(false, "处理成功");
        } catch (RuntimeException ex) {
            inbox.markFailed(existed.id(), trim(ex.getMessage()));
            throw ex;
        }
    }

    public List<FailedEventView> failedEvents(int limit) {
        int batchSize = limit <= 0 ? 50 : Math.min(limit, 200);
        return inbox.failed(batchSize).stream().map(FailedEventView::from).toList();
    }

    @Transactional
    public ConsumeResult replay(long inboxId, long operatorId) {
        var row = inbox.failed(200).stream()
                .filter(event -> event.id() == inboxId)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "失败入站事件不存在"));
        try {
            dispatch(row, operatorId);
            inbox.markSucceeded(row.id());
            return new ConsumeResult(false, "重放成功");
        } catch (RuntimeException ex) {
            inbox.markFailed(row.id(), trim(ex.getMessage()));
            throw ex;
        }
    }

    private void dispatch(WmsInboxMapper.Row row, long operatorId) {
        var payload = readPayload(row.payload());
        switch (row.eventType()) {
            case "CreateInboundOrderRequested" -> inboundOrders.create(new InboundOrderApplicationService.Create(
                    text(payload, "sourceType"),
                    text(payload, "sourceNo"),
                    longValue(payload, "warehouseId"),
                    offsetDateTime(payload, "expectedArrivalAt"),
                    row.eventCode()
            ), operatorId);
            case "CreateOutboundOrderRequested" -> outboundOrders.create(
                    text(payload, "sourceType"),
                    text(payload, "sourceNo"),
                    longValue(payload, "warehouseId"),
                    operatorId
            );
            default -> throw new BusinessException(ErrorCode.VALIDATION_FAILED, "不支持的WMS入站事件类型");
        }
    }

    private JsonNode readPayload(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "入站事件载荷不是合法JSON");
        }
    }

    private static void validate(EventEnvelope envelope) {
        if (envelope.sourceSystem() == null || envelope.sourceSystem().isBlank()
                || envelope.eventCode() == null || envelope.eventCode().isBlank()
                || envelope.eventType() == null || envelope.eventType().isBlank()
                || envelope.payload() == null || envelope.payload().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "入站事件信封缺少必填字段");
        }
    }

    private static String text(JsonNode payload, String field) {
        var value = payload.get(field);
        if (value == null || value.asText().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "入站事件载荷缺少字段: " + field);
        }
        return value.asText();
    }

    private static long longValue(JsonNode payload, String field) {
        var value = payload.get(field);
        if (value == null || !value.canConvertToLong() || value.asLong() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "入站事件载荷字段不合法: " + field);
        }
        return value.asLong();
    }

    private static OffsetDateTime offsetDateTime(JsonNode payload, String field) {
        var value = payload.get(field);
        if (value == null || value.asText().isBlank()) {
            return null;
        }
        return OffsetDateTime.parse(value.asText());
    }

    private static String trim(String message) {
        if (message == null) {
            return "UNKNOWN_ERROR";
        }
        return message.length() <= 1000 ? message : message.substring(0, 1000);
    }

    public record EventEnvelope(String sourceSystem, String eventCode, String eventType, String payload) {
    }

    public record ConsumeResult(boolean duplicated, String message) {
    }

    public record FailedEventView(
            long id,
            String sourceSystem,
            String eventCode,
            String eventType,
            int retryCount,
            String lastError
    ) {
        static FailedEventView from(WmsInboxMapper.Row row) {
            return new FailedEventView(
                    row.id(),
                    row.sourceSystem(),
                    row.eventCode(),
                    row.eventType(),
                    row.retryCount(),
                    row.lastError()
            );
        }
    }
}
