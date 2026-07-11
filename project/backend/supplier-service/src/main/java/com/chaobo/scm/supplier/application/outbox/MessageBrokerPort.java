package com.chaobo.scm.supplier.application.outbox;public interface MessageBrokerPort{void publish(OutboxMessage message) throws Exception;}
