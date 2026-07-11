package com.chaobo.scm.supplier.application.item;
import java.math.BigDecimal;import java.time.*;
public record SupplierItemView(long itemId,long supplierId,String skuCode,String supplierSkuCode,BigDecimal moq,BigDecimal mpq,int leadTimeDays,String purchaseUnit,LocalDate effectiveFrom,LocalDate effectiveTo,int status,String statusName,String pauseReason,int version,OffsetDateTime updatedAt){}
