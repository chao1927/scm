package com.chaobo.scm.oms.infrastructure.mq;

import com.chaobo.scm.oms.application.OmsExternalEvent;
import com.chaobo.scm.oms.application.OmsMessageBroker;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OMS 标准 V1 事件信封编解码器。
 */
@Component
public class OmsEventEnvelopeCodec {

    static final int SCHEMA_VERSION = 1;

    private final ObjectMapper json;

    public OmsEventEnvelopeCodec(ObjectMapper json) {
        this.json = json;
    }

    public byte[] encode(OmsMessageBroker.OutboundMessage message) {
        try {
            Object payload = parsePayload(message.payload());
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("businessNo", required(message.businessNo(), "businessNo"));
            data.put("payload", payload);
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("schemaVersion", SCHEMA_VERSION);
            envelope.put("sourceSystem", "OMS");
            envelope.put("eventCode", required(message.eventCode(), "eventCode"));
            envelope.put("eventType", required(message.eventType(), "eventType"));
            envelope.put("data", data);
            return json.writeValueAsBytes(envelope);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("OMS Outbox 事件无法编码", exception);
        }
    }

    public OmsExternalEvent decode(byte[] bytes) {
        try {
            JsonNode root = json.readTree(bytes);
            if (root == null || !root.isObject()) {
                throw new IllegalArgumentException("OMS 事件信封必须是 JSON 对象");
            }
            JsonNode version = required(root, "schemaVersion");
            if (!version.isIntegralNumber() || version.intValue() != SCHEMA_VERSION) {
                throw new IllegalArgumentException(
                        "不支持的 OMS 事件信封版本: " + version);
            }
            String source = text(root, "sourceSystem", true);
            String eventCode = text(root, "eventCode", true);
            String eventType = text(root, "eventType", true);
            JsonNode data = required(root, "data");
            if (!data.isObject()) {
                throw new IllegalArgumentException("OMS 事件 data 必须是 JSON 对象");
            }
            return new OmsExternalEvent(
                    source, eventCode, eventType, text(data, "businessNo", true),
                    text(data, "fulfillmentNo", false),
                    text(data, "reservationRefNo", false),
                    text(data, "reservationNo", false),
                    decimal(data, "quantity"), text(data, "outboundNo", false),
                    text(data, "wmsOrderNo", false),
                    text(data, "afterSaleNo", false),
                    text(data, "reason", false), decimal(data, "receivedQty"),
                    decimal(data, "acceptedQty"), decimal(data, "amount"),
                    bool(data, "unmatched"), data.toString()
            );
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("OMS 事件信封无法解析", exception);
        }
    }

    private Object parsePayload(String payload) throws JacksonException {
        if (payload == null || payload.isBlank()) {
            return "";
        }
        try {
            return json.readTree(payload);
        } catch (JacksonException ignored) {
            return payload;
        }
    }

    private static JsonNode required(JsonNode parent, String name) {
        JsonNode value = parent.get(name);
        if (value == null || value.isNull()) {
            throw new IllegalArgumentException("OMS 事件信封缺少字段: " + name);
        }
        return value;
    }

    private static String text(JsonNode parent, String name, boolean required) {
        JsonNode value = parent.get(name);
        if (value == null || value.isNull()) {
            if (required) {
                throw new IllegalArgumentException("OMS 事件信封缺少字段: " + name);
            }
            return null;
        }
        if (!value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException("OMS 事件字段格式错误: " + name);
        }
        return value.asText().trim();
    }

    private static BigDecimal decimal(JsonNode parent, String name) {
        JsonNode value = parent.get(name);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isNumber() && !value.isTextual()) {
            throw new IllegalArgumentException("OMS 事件数字字段格式错误: " + name);
        }
        try {
            return new BigDecimal(value.asText());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("OMS 事件数字字段格式错误: " + name,
                    exception);
        }
    }

    private static boolean bool(JsonNode parent, String name) {
        JsonNode value = parent.get(name);
        return value != null && !value.isNull() && value.asBoolean();
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OMS 事件字段不能为空: " + name);
        }
        return value.trim();
    }
}
