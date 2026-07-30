package com.chaobo.scm.purchase.infrastructure.mq;

import com.chaobo.scm.purchase.application.integration.PurchaseExternalEvent;
import com.chaobo.scm.purchase.application.outbox.OutboxMessage;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 采购 RocketMQ 标准事件信封编解码器。
 *
 * <p>信封身份字段由消息生产者负责，业务数据只能位于 {@code data}。消费时以信封
 * 身份覆盖 data 中可能存在的同名字段，避免伪造来源、事件编码或事件类型。
 */
@Component
public class PurchaseEventEnvelopeCodec {

    /** 当前唯一受支持的事件信封版本。 */
    static final int SCHEMA_VERSION = 1;

    private final ObjectMapper json;

    public PurchaseEventEnvelopeCodec(ObjectMapper json) {
        this.json = json;
    }

    /**
     * 将采购 Outbox 消息编码为标准事件信封。
     *
     * @param message 已持久化的 Outbox 消息
     * @return 可直接发送到 RocketMQ 的 UTF-8 JSON 字节
     */
    public byte[] encode(OutboxMessage message) {
        try {
            JsonNode data = json.readTree(message.payloadJson());
            if (data == null || data.isNull()) {
                throw new IllegalArgumentException("采购 Outbox 事件 data 不能为空");
            }
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("schemaVersion", SCHEMA_VERSION);
            envelope.put("sourceSystem", "PURCHASE");
            envelope.put("eventCode", required(message.eventCode(), "eventCode"));
            envelope.put("eventType", required(message.eventType(), "eventType"));
            envelope.put("data", data);
            return json.writeValueAsBytes(envelope);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("采购 Outbox 事件载荷不是合法 JSON", exception);
        }
    }

    /**
     * 解析外部标准信封；任何未知版本或缺失字段都直接失败，由 RocketMQ 重投。
     *
     * @param bytes RocketMQ 消息体
     * @return 统一的采购外部事件
     */
    public PurchaseExternalEvent decode(byte[] bytes) {
        try {
            JsonNode root = json.readTree(bytes);
            if (root == null || !root.isObject()) {
                throw new IllegalArgumentException("采购外部事件信封必须是 JSON 对象");
            }
            JsonNode version = required(root, "schemaVersion");
            if (!version.isIntegralNumber() || version.intValue() != SCHEMA_VERSION) {
                throw new IllegalArgumentException(
                        "不支持的采购事件信封版本: " + version.toString());
            }
            String sourceSystem = requiredText(root, "sourceSystem");
            String eventCode = requiredText(root, "eventCode");
            String eventType = requiredText(root, "eventType");
            JsonNode data = required(root, "data");
            if (!data.isObject()) {
                throw new IllegalArgumentException("采购外部事件 data 必须是 JSON 对象");
            }
            PurchaseExternalEvent payload = json.readValue(
                    data.toString(), PurchaseExternalEvent.class);
            return payload.withEnvelopeIdentity(sourceSystem, eventCode, eventType);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("采购外部事件信封无法解析", exception);
        }
    }

    private static JsonNode required(JsonNode root, String name) {
        JsonNode value = root.get(name);
        if (value == null || value.isNull()) {
            throw new IllegalArgumentException("采购外部事件信封缺少字段: " + name);
        }
        return value;
    }

    private static String requiredText(JsonNode root, String name) {
        JsonNode value = required(root, name);
        if (!value.isTextual()) {
            throw new IllegalArgumentException("采购外部事件信封字段必须是字符串: " + name);
        }
        return required(value.asText(), name);
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("采购外部事件信封字段不能为空: " + name);
        }
        return value.trim();
    }
}
