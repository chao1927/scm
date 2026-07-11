package com.chaobo.scm.supplier.application.qualification;
public interface SupplierQualificationPolicyPort { boolean hasValidQualification(long supplierId); void assertEligible(long supplierId,Long categoryId); }
