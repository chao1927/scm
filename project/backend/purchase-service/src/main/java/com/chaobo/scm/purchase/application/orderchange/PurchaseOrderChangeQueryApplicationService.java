package com.chaobo.scm.purchase.application.orderchange;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseOrderChangeQueryApplicationService {
    private final PurchaseOrderChangeReadModelPort readModel;

    public PurchaseOrderChangeQueryApplicationService(PurchaseOrderChangeReadModelPort readModel) {
        this.readModel = readModel;
    }

    @Transactional(readOnly = true)
    public PageResult<PurchaseOrderChangeView> page(String orderNo, Integer status, int pageNo, int pageSize) {
        if (pageNo < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        return readModel.page(orderNo, status, pageNo, pageSize);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderChangeView detail(String changeNo) {
        return readModel.detail(changeNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "采购订单变更单不存在"));
    }
}
