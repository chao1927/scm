package com.chaobo.scm.purchase.application.inbound;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InboundTrackingQueryApplicationService {
    private final InboundTrackingReadModelPort readModel;

    public InboundTrackingQueryApplicationService(InboundTrackingReadModelPort readModel) {
        this.readModel = readModel;
    }

    @Transactional(readOnly = true)
    public PageResult<InboundTrackingView> page(Long purchaseOrgId, Long scope, String orderNo, String asnNo,
                                                String warehouseCode, Integer status, int pageNo, int pageSize) {
        if (pageNo < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        return readModel.page(scope == null ? purchaseOrgId : scope, orderNo, asnNo, warehouseCode, status, pageNo, pageSize);
    }

    @Transactional(readOnly = true)
    public InboundTrackingView detail(String inboundNo, Long scope) {
        var view = readModel.detail(inboundNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "到货跟踪不存在"));
        if (scope != null && scope != view.purchaseOrgId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "到货跟踪不存在");
        }
        return view;
    }
}
