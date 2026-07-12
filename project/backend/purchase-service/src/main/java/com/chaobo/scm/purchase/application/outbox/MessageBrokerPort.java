package com.chaobo.scm.purchase.application.outbox;

public interface MessageBrokerPort {
    void publish(OutboxMessage message);
}
