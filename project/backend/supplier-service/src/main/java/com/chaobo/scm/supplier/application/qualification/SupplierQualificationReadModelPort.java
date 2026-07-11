package com.chaobo.scm.supplier.application.qualification;import com.chaobo.scm.common.api.PageResult;import java.util.Optional;
public interface SupplierQualificationReadModelPort { Optional<SupplierQualificationView> detail(long id); PageResult<SupplierQualificationView> page(Long supplierId,Integer status,int pageNo,int pageSize); }
