package com.chaobo.scm.purchase.application.comparison;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BidComparisonQueryApplicationService {
    private final BidComparisonReadModelPort readModel;

    public BidComparisonQueryApplicationService(BidComparisonReadModelPort readModel) {
        this.readModel = readModel;
    }

    @Transactional(readOnly = true)
    public PageResult<BidComparisonView> page(
            Long purchaseOrgId,
            Long scope,
            Integer status,
            String rfqNo,
            int pageNo,
            int pageSize) {
        if (pageNo < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        return readModel.page(scope == null ? purchaseOrgId : scope, status, rfqNo, pageNo, pageSize);
    }

    @Transactional(readOnly = true)
    public BidComparisonView detail(String compareNo, Long scope) {
        var view = readModel.detail(compareNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "比价结果不存在"));
        if (scope != null && scope != view.purchaseOrgId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "比价结果不存在");
        }
        return view;
    }
}
