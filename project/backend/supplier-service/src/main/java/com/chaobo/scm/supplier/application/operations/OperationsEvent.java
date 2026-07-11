package com.chaobo.scm.supplier.application.operations;import java.time.OffsetDateTime;
public record OperationsEvent(String sourceSystem,String eventCode,String eventType,long supplierId,String businessType,long businessId,String businessNo,String message,OffsetDateTime dueAt,OffsetDateTime occurredAt){}
