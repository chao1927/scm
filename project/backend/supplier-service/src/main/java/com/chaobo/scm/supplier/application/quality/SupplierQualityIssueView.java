package com.chaobo.scm.supplier.application.quality;import java.time.OffsetDateTime;
public record SupplierQualityIssueView(long qualityIssueId,String issueNo,long supplierId,String sourceType,String sourceNo,String issueType,int severity,String description,int status,String statusName,OffsetDateTime rectificationDeadline,String rectificationPlan,String verificationComment,int version){}
