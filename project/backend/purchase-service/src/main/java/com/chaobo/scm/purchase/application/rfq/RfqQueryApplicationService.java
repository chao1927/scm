package com.chaobo.scm.purchase.application.rfq;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class RfqQueryApplicationService {
    private final RfqReadModelPort readModel;

    public RfqQueryApplicationService(RfqReadModelPort readModel) {
        this.readModel = readModel;
    }

    @Transactional(readOnly = true)
    public PageResult<RfqView> page(
            Long purchaseOrgId,
            Long scope,
            Integer status,
            String categoryCode,
            Long supplierId,
            OffsetDateTime deadlineFrom,
            OffsetDateTime deadlineTo,
            int pageNo,
            int pageSize) {
        if (pageNo < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        if (deadlineFrom != null && deadlineTo != null && deadlineFrom.plusDays(366).isBefore(deadlineTo)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "查询跨度不能超过一年");
        }
        return readModel.page(
                scope == null ? purchaseOrgId : scope,
                status,
                categoryCode,
                supplierId,
                deadlineFrom,
                deadlineTo,
                pageNo,
                pageSize);
    }

    @Transactional(readOnly = true)
    public RfqView detailByNo(String rfqNo, Long scope) {
        var view = readModel.detailByNo(rfqNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "询价单不存在"));
        if (scope != null && scope != view.purchaseOrgId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "询价单不存在");
        }
        return view;
    }
}
