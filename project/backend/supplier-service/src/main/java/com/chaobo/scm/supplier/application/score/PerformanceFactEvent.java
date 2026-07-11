package com.chaobo.scm.supplier.application.score;import java.math.BigDecimal;import java.time.OffsetDateTime;import java.util.Map;
public record PerformanceFactEvent(String sourceSystem,String eventCode,String eventType,long supplierId,String sourceNo,BigDecimal metricValue,OffsetDateTime occurredAt,Map<String,Object> payload){}
