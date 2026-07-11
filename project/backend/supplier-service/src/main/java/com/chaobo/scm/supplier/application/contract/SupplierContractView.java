package com.chaobo.scm.supplier.application.contract;import java.time.*;
public record SupplierContractView(long contractId,String contractNo,long supplierId,Long quoteId,String priceAgreementRef,String contractType,LocalDate effectiveFrom,LocalDate effectiveTo,int status,String statusName,String attachmentUrl,int version){}
