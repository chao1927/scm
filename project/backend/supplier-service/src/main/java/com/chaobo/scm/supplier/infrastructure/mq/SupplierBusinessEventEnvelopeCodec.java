package com.chaobo.scm.supplier.infrastructure.mq;

import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 供应商协同 RocketMQ V1 业务事件信封解码器。
 *
 * <p>运输层只接受标准信封，身份字段由信封提供，业务数据必须位于 {@code data}。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class SupplierBusinessEventEnvelopeCodec {

    static final int VERSION = 1;
    private final ObjectMapper json;

    public SupplierBusinessEventEnvelopeCodec(ObjectMapper json) {
        this.json = json;
    }

    /**
     * 解码 V1 信封；未知版本、缺失身份或非对象 data 均失败并交由 RocketMQ 重投。
     *
     * @param bytes 消息体
     * @return 已校验信封
     */
    public Envelope decode(byte[] bytes) {
        try {
            JsonNode root = json.readTree(bytes);
            if (root == null || !root.isObject()) {
                throw new IllegalArgumentException("供应商业务事件信封必须为 JSON 对象");
            }
            JsonNode version = root.get("schemaVersion");
            if (version == null) {
                version = root.get("eventVersion");
            }
            if (!supported(version)) {
                throw new IllegalArgumentException("不支持的供应商业务事件信封版本");
            }
            String eventCode = text(root, "eventCode");
            if (eventCode == null) {
                eventCode = text(root, "eventId");
            }
            String source = required(text(root, "sourceSystem"), "sourceSystem");
            String type = required(text(root, "eventType"), "eventType");
            eventCode = required(eventCode, "eventCode");
            JsonNode data = root.get("data");
            if (data == null) {
                data = root.get("payload");
            }
            if (data == null || !data.isObject()) {
                throw new IllegalArgumentException("供应商业务事件 data 必须为 JSON 对象");
            }
            return new Envelope(source, eventCode, type, data);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("供应商业务事件信封无法解析", exception);
        }
    }

    private boolean supported(JsonNode version) {
        if (version == null) {
            return false;
        }
        if (version.isIntegralNumber()) {
            return version.intValue() == VERSION;
        }
        return "1".equals(version.asText()) || "1.0".equals(version.asText());
    }

    private String text(JsonNode root, String name) {
        JsonNode value = root.get(name);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("供应商业务事件信封字段不能为空: " + name);
        }
        return value;
    }

    /**
     * 通过校验的 V1 信封。
     */
    public record Envelope(String sourceSystem, String eventCode, String eventType, JsonNode data) {
    }
}
