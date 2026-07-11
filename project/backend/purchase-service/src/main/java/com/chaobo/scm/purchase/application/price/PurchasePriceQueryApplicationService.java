package com.chaobo.scm.purchase.application.price;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchasePriceQueryApplicationService {
    private final PurchasePriceReadModelPort readModel;

    public PurchasePriceQueryApplicationService(PurchasePriceReadModelPort readModel) {
        this.readModel = readModel;
    }

    @Transactional(readOnly = true)
    public PageResult<PurchasePriceView> page(
            Long purchaseOrgId,
            Long scope,
            Long supplierId,
            String skuCode,
            String currency,
            Integer status,
            int pageNo,
            int pageSize) {
        if (pageNo < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        return readModel.page(scope == null ? purchaseOrgId : scope, supplierId, skuCode, currency, status, pageNo, pageSize);
    }

    @Transactional(readOnly = true)
    public PurchasePriceView detail(String priceNo, Long scope) {
        var view = readModel.detail(priceNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "采购价格不存在"));
        if (scope != null && scope != view.purchaseOrgId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "采购价格不存在");
        }
        return view;
    }
}
