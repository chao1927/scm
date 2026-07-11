package com.chaobo.scm.supplier.application.integration;
import java.time.OffsetDateTime;
public record IntegrationCommand(long id,String code,String type,String aggregateType,long aggregateId,int aggregateVersion,String targetSystem,String payloadJson,int status,int retryCount,OffsetDateTime nextRetryAt,String remoteReference,String failReason){}
