package com.chaobo.scm.purchase.infrastructure.mq;

import com.chaobo.scm.purchase.application.integration.PurchaseExternalEvent;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageId;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 采购 RocketMQ 消费入口测试。
 */
class RocketMqPurchaseExternalEventConsumerTest {

    private static final byte[] VALID_EVENT = """
            {
              "schemaVersion": 1,
              "sourceSystem": "WMS",
              "eventCode": "EVT-MQ-001",
              "eventType": "WmsReceiptCompleted",
              "data": {"inboundNo": "IN-001"}
            }
            """.getBytes(StandardCharsets.UTF_8);

    @Test
    void shouldDelegateValidMessageToInboxApplicationService() {
        var consumed = new ArrayList<PurchaseExternalEvent>();
        var consumer = RocketMqPurchaseExternalEventConsumer.forTest(
                new PurchaseEventEnvelopeCodec(new ObjectMapper()),
                consumed::add
        );

        var result = consumer.consume(message(VALID_EVENT));

        assertEquals(ConsumeResult.SUCCESS, result);
        assertEquals(1, consumed.size());
        assertEquals("EVT-MQ-001", consumed.get(0).eventCode());
    }

    @Test
    void shouldReturnFailureSoBrokerRetriesWhenApplicationFails() {
        var consumer = RocketMqPurchaseExternalEventConsumer.forTest(
                new PurchaseEventEnvelopeCodec(new ObjectMapper()),
                event -> {
                    throw new IllegalStateException("database unavailable");
                }
        );

        assertEquals(ConsumeResult.FAILURE, consumer.consume(message(VALID_EVENT)));
    }

    @Test
    void shouldReturnFailureForUnknownSchemaVersion() {
        var consumed = new ArrayList<PurchaseExternalEvent>();
        var consumer = RocketMqPurchaseExternalEventConsumer.forTest(
                new PurchaseEventEnvelopeCodec(new ObjectMapper()),
                consumed::add
        );
        var unsupported = new String(VALID_EVENT, StandardCharsets.UTF_8)
                .replace("\"schemaVersion\": 1", "\"schemaVersion\": 99")
                .getBytes(StandardCharsets.UTF_8);

        assertEquals(ConsumeResult.FAILURE, consumer.consume(message(unsupported)));
        assertEquals(0, consumed.size());
    }

    private static MessageView message(byte[] body) {
        return (MessageView) Proxy.newProxyInstance(
                MessageView.class.getClassLoader(),
                new Class<?>[]{MessageView.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getBody" -> ByteBuffer.wrap(body);
                    case "getMessageId" -> messageId();
                    case "getTopic" -> "wms-domain-event";
                    case "getProperties" -> Map.of();
                    case "getTag", "getMessageGroup", "getDeliveryTimestamp" -> Optional.empty();
                    case "getKeys" -> List.of();
                    case "getBornHost" -> "127.0.0.1";
                    case "getBornTimestamp" -> 0L;
                    case "getDeliveryAttempt" -> 1;
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private static MessageId messageId() {
        return (MessageId) Proxy.newProxyInstance(
                MessageId.class.getClassLoader(),
                new Class<?>[]{MessageId.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getVersion" -> "01";
                    case "toString" -> "MSG-001";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }
}
