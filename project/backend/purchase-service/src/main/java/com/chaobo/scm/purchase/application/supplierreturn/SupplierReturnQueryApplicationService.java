package com.chaobo.scm.purchase.application.supplierreturn;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierReturnQueryApplicationService {
    private final SupplierReturnReadModelPort readModel;

    public SupplierReturnQueryApplicationService(SupplierReturnReadModelPort readModel) {
        this.readModel = readModel;
    }

    @Transactional(readOnly = true)
    public PageResult<SupplierReturnView> page(Long purchaseOrgId, Long scope, Long supplierId, String warehouseCode,
                                               Integer status, int pageNo, int pageSize) {
        if (pageNo < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        return readModel.page(scope == null ? purchaseOrgId : scope, supplierId, warehouseCode, status, pageNo, pageSize);
    }

    @Transactional(readOnly = true)
    public SupplierReturnView detail(String returnNo, Long scope) {
        var view = readModel.detail(returnNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "退供申请不存在"));
        if (scope != null && scope != view.purchaseOrgId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "退供申请不存在");
        }
        return view;
    }
}
