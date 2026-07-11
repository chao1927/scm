package com.chaobo.scm.supplier.application.outbox;
public record OutboxMessage(long eventId,String eventCode,String eventType,String aggregateType,long aggregateId,String payloadJson,int retryCount){}
