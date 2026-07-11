package com.chaobo.scm.common.integration;import java.io.Serializable;import java.util.Set;
public interface IamCollaborationApi{void updateSupplierDataScope(UpdateSupplierScopeCommand command);record UpdateSupplierScopeCommand(String idempotencyKey,long userId,Set<Long> supplierIds,String reason)implements Serializable{}}
