package com.chaobo.scm.supplier.application.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PriceAgreementView(long agreementId,String agreementRef,long contractId,long quoteId,long supplierId,String currency,LocalDate effectiveFrom,LocalDate effectiveTo,int status,int sourceContractVersion,List<Line> lines){
    public record Line(String skuCode,BigDecimal unitPrice,BigDecimal taxRate,BigDecimal moq,int deliveryDays){}
}
