package com.chaobo.scm.supplier.application.qualification;import java.time.LocalDate;
public record SupplierQualificationView(long qualificationId,long supplierId,String qualificationType,String qualificationNo,LocalDate validFrom,LocalDate validTo,String attachmentUrl,int status,String statusName,String reviewRemark,int version){}
