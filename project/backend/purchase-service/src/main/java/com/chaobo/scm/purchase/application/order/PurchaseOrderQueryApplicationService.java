package com.chaobo.scm.purchase.application.order;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseOrderQueryApplicationService {
    private final PurchaseOrderReadModelPort readModel;

    public PurchaseOrderQueryApplicationService(PurchaseOrderReadModelPort readModel) {
        this.readModel = readModel;
    }

    @Transactional(readOnly = true)
    public PageResult<PurchaseOrderView> page(Long purchaseOrgId, Long scope, Long supplierId, Integer status,
                                              int pageNo, int pageSize) {
        if (pageNo < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        return readModel.page(scope == null ? purchaseOrgId : scope, supplierId, status, pageNo, pageSize);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderView detail(String orderNo, Long scope) {
        var view = readModel.detail(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "采购订单不存在"));
        if (scope != null && scope != view.purchaseOrgId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "采购订单不存在");
        }
        return view;
    }
}
