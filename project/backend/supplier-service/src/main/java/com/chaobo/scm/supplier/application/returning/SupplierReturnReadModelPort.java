package com.chaobo.scm.supplier.application.returning;
import com.chaobo.scm.common.api.PageResult;import java.util.Optional;
public interface SupplierReturnReadModelPort {Optional<SupplierReturnView> detail(long id);PageResult<SupplierReturnView> page(Long supplierId,Integer status,int page,int size);}
