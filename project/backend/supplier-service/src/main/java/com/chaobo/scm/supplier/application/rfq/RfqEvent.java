package com.chaobo.scm.supplier.application.rfq;import java.time.OffsetDateTime;import java.util.Map;
public record RfqEvent(String eventCode,String eventType,String sourceSystem,long rfqId,String rfqNo,long supplierId,OffsetDateTime quoteDeadline,Map<String,Object> payload){}
