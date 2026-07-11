package com.chaobo.scm.supplier.application.quality;import com.chaobo.scm.common.api.PageResult;import java.util.*;
public interface SupplierQualityIssueReadModelPort{Optional<SupplierQualityIssueView> detail(long id);PageResult<SupplierQualityIssueView> page(Long supplierId,Integer status,Integer severity,int pageNo,int pageSize);List<Long> overdueIds();}
