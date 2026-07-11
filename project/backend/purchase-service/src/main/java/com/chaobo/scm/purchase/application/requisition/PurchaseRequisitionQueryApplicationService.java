package com.chaobo.scm.purchase.application.requisition;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseRequisitionQueryApplicationService {
    private final PurchaseRequisitionReadModelPort readModel;

    public PurchaseRequisitionQueryApplicationService(PurchaseRequisitionReadModelPort readModel) {
        this.readModel = readModel;
    }

    @Transactional(readOnly = true)
    public PageResult<PurchaseRequisitionView> page(
            Long purchaseOrgId,
            Long scope,
            Integer status,
            String keyword,
            int pageNo,
            int pageSize) {
        if (pageNo < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        return readModel.page(scope == null ? purchaseOrgId : scope, status, keyword, pageNo, pageSize);
    }

    @Transactional(readOnly = true)
    public PurchaseRequisitionView detail(long id, Long scope) {
        var view = readModel.detail(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "请购单不存在"));
        if (scope != null && scope != view.purchaseOrgId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "请购单不存在");
        }
        return view;
    }
}
