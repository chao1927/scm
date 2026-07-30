package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * 库存事件标准信封 JSON 编解码器。
 *
 * <p>使用 Jackson 完整处理嵌套对象、数组、转义字符和数值类型，替代无法正确解析复杂 JSON 的字符串拆分。
 *
 * @author SCM Team
 */
@Component
public class InventoryEventEnvelopeCodec {

    private final ObjectMapper objectMapper;

    public InventoryEventEnvelopeCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 解析标准事件信封。
     *
     * @param json 完整信封 JSON
     * @return 版本化事件信封
     */
    @SuppressWarnings("unchecked")
    public InventoryEventEnvelope decode(String json) {
        try {
            Map<String, Object> values = objectMapper.readValue(json, Map.class);
            Object rawPayload = values.get("payload");
            Map<String, Object> payload = rawPayload instanceof Map<?, ?> map
                    ? (Map<String, Object>) map
                    : Map.of();
            return new InventoryEventEnvelope(
                    text(values, "eventId"),
                    text(values, "eventType"),
                    text(values, "eventVersion"),
                    text(values, "sourceContext"),
                    text(values, "sourceSystem"),
                    text(values, "aggregateType"),
                    text(values, "aggregateId"),
                    number(values, "aggregateVersion"),
                    text(values, "businessKey"),
                    text(values, "idempotencyKey"),
                    text(values, "occurredAt"),
                    optionalText(values, "traceId"),
                    payload);
        } catch (JacksonException | ClassCastException exception) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "库存事件信封不是合法 JSON");
        }
    }

    /**
     * 序列化标准事件信封。
     *
     * @param event 事件信封
     * @return 完整信封 JSON
     */
    public String encode(InventoryEventEnvelope event) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("eventId", event.eventId());
        values.put("eventType", event.eventType());
        values.put("eventVersion", event.eventVersion());
        values.put("sourceContext", event.sourceContext());
        values.put("sourceSystem", event.sourceSystem());
        values.put("aggregateType", event.aggregateType());
        values.put("aggregateId", event.aggregateId());
        values.put("aggregateVersion", event.aggregateVersion());
        values.put("businessKey", event.businessKey());
        values.put("idempotencyKey", event.idempotencyKey());
        values.put("occurredAt", event.occurredAt());
        values.put("traceId", event.traceId());
        values.put("payload", event.payload());
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JacksonException exception) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "库存事件信封无法序列化");
        }
    }

    /**
     * 解析 Outbox 中保存的业务载荷。
     *
     * @param payloadJson 业务载荷 JSON
     * @return 可保留嵌套结构的载荷映射
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> decodePayload(String payloadJson) {
        try {
            return objectMapper.readValue(payloadJson, Map.class);
        } catch (JacksonException | ClassCastException exception) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "库存 Outbox 业务载荷不是合法 JSON");
        }
    }

    /**
     * 序列化事件业务载荷。
     *
     * @param payload 业务载荷
     * @return JSON 文本
     */
    public String encodePayload(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
        } catch (JacksonException exception) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "库存事件业务载荷无法序列化");
        }
    }

    private static String text(Map<String, Object> values, String key) {
        String value = optionalText(values, key);
        if (value == null || value.isBlank()) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "库存事件信封缺少字段: " + key);
        }
        return value;
    }

    private static String optionalText(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? null : value.toString();
    }

    private static long number(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return value == null ? 0L : Long.parseLong(value.toString());
        } catch (NumberFormatException exception) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "库存事件信封字段类型错误: " + key);
        }
    }
}
