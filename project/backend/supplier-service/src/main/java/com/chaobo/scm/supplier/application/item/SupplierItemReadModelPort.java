package com.chaobo.scm.supplier.application.item;
import com.chaobo.scm.common.api.PageResult;import java.util.Optional;
public interface SupplierItemReadModelPort { Optional<SupplierItemView> detail(long id); PageResult<SupplierItemView> page(Long supplierId,Integer status,String keyword,int pageNo,int pageSize); }
