package com.chaobo.scm.supplier.application.asn.event;import java.time.OffsetDateTime;
public record TmsAsnEvent(String eventCode,String eventType,long asnId,Long shipmentId,String waybillNo,String carrierCode,String node,OffsetDateTime occurredAt,String exceptionCode,String exceptionReason,long sourceVersion){}
