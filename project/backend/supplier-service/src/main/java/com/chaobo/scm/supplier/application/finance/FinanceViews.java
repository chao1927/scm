package com.chaobo.scm.supplier.application.finance;
import java.math.BigDecimal;
public final class FinanceViews{private FinanceViews(){}public record Reconciliation(long id,String statementNo,long supplierId,String currency,BigDecimal statementAmount,BigDecimal confirmedAmount,int status,String differenceReason,int sourceVersion,int version){}public record Invoice(long id,String invoiceNo,long supplierId,Long reconciliationId,int invoiceType,BigDecimal amountExcludingTax,BigDecimal taxAmount,BigDecimal taxRate,String attachmentUrl,int status,String validationMessage,int version){} }
