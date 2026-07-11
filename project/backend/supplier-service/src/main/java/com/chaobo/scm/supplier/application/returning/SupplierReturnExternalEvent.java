package com.chaobo.scm.supplier.application.returning;
import java.math.BigDecimal;import java.util.Map;
public record SupplierReturnExternalEvent(String sourceSystem,String eventCode,String eventType,long returnId,boolean success,String referenceNo,String shipmentId,String waybillNo,String carrierCode,Map<Long,BigDecimal> lineQuantities,BigDecimal offsetAmount,BigDecimal claimAmount,String reason){}
