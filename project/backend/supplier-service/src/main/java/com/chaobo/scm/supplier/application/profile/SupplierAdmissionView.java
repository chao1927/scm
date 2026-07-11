package com.chaobo.scm.supplier.application.profile;

public record SupplierAdmissionView(long id,String admissionNo,String supplierCode,String supplierName,String taxNo,String supplierType,String contactName,String contactMobile,String settlementJson,int status,String rejectReason,int version){}
