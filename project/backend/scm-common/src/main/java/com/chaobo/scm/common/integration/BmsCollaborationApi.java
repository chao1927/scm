package com.chaobo.scm.common.integration;
import java.io.Serializable;import java.math.BigDecimal;
public interface BmsCollaborationApi{SettlementResult createSupplierReturnSettlement(ReturnSettlementCommand command);record ReturnSettlementCommand(String idempotencyKey,long returnId,String returnNo,long supplierId,BigDecimal offsetAmount,BigDecimal claimAmount,String reason)implements Serializable{}record SettlementResult(boolean accepted,String settlementRef,String reason)implements Serializable{}}
