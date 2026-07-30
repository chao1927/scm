package com.chaobo.scm.oms.infrastructure.mq;

import com.chaobo.scm.oms.application.OmsExternalEvent;
import com.chaobo.scm.oms.application.OmsExternalEventHandler;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RocketMQ 消费确认语义测试。
 */
class RocketMqOmsExternalEventConsumerTest {

    @Test
    void returnsSuccessOnlyAfterApplicationHandlerSucceeds() {
        AtomicReference<OmsExternalEvent> handled = new AtomicReference<>();
        var consumer = new RocketMqOmsExternalEventConsumer(
                new OmsEventEnvelopeCodec(new ObjectMapper()), handled::set);

        ConsumeResult result = consumer.consume(message(validEnvelope(1)));

        assertThat(result).isEqualTo(ConsumeResult.SUCCESS);
        assertThat(handled.get().eventCode()).isEqualTo("WMS-E1");
    }

    @Test
    void returnsFailureForUnknownVersionOrApplicationFailure() {
        OmsExternalEventHandler failed = event -> {
            throw new IllegalStateException("inbox failed");
        };
        var consumer = new RocketMqOmsExternalEventConsumer(
                new OmsEventEnvelopeCodec(new ObjectMapper()), failed);

        assertThat(consumer.consume(message(validEnvelope(2))))
                .isEqualTo(ConsumeResult.FAILURE);
        assertThat(consumer.consume(message(validEnvelope(1))))
                .isEqualTo(ConsumeResult.FAILURE);
    }

    private static String validEnvelope(int version) {
        return """
                {"schemaVersion":%d,"sourceSystem":"WMS",
                 "eventCode":"WMS-E1","eventType":"WmsOutboundShipped",
                 "data":{"businessNo":"OUT-1","fulfillmentNo":"FUL-1",
                         "outboundNo":"OUT-1","wmsOrderNo":"WMS-1"}}
                """.formatted(version);
    }

    private static MessageView message(String body) {
        return (MessageView) Proxy.newProxyInstance(
                MessageView.class.getClassLoader(), new Class<?>[]{MessageView.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getBody" -> ByteBuffer.wrap(
                            body.getBytes(StandardCharsets.UTF_8));
                    case "getMessageId" -> null;
                    case "getDeliveryAttempt", "hashCode" -> 0;
                    case "getBornTimestamp" -> 0L;
                    case "toString" -> "test-message";
                    case "equals" -> proxy == arguments[0];
                    default -> null;
                });
    }
}
