package com.chaobo.scm.supplier.application.contract;
public record ContractApprovalEvent(String eventCode,String sourceSystem,String eventType,long contractId,int contractVersion,boolean approved,String comment){}
