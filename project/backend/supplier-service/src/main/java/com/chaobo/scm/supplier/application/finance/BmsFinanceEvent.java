package com.chaobo.scm.supplier.application.finance;import java.math.BigDecimal;
public record BmsFinanceEvent(String eventCode,String eventType,String statementNo,long supplierId,String currency,BigDecimal amount,int sourceVersion,Long invoiceId,boolean passed,String message){}
