package com.chaobo.scm.supplier.application.quality;
public record QualitySourceEvent(String sourceSystem,String eventCode,String eventType,long supplierId,String sourceNo,String issueType,int severity,String description){}
