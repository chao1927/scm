package com.chaobo.scm.wms.domain.outbound;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

public class OutboundOrderAggregate {
    private final long id;
    private final String no;
    private final String sourceType;
    private final String sourceNo;
    private final long warehouseId;
    private int status;
    private int version;

    public OutboundOrderAggregate(
            long id,
            String no,
            String sourceType,
            String sourceNo,
            long warehouseId,
            int status,
            int version
    ) {
        if (sourceType == null || sourceType.isBlank() || sourceNo == null || sourceNo.isBlank() || warehouseId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "出库来源和仓库不能为空");
        }
        this.id = id;
        this.no = no;
        this.sourceType = sourceType;
        this.sourceNo = sourceNo;
        this.warehouseId = warehouseId;
        this.status = status;
        this.version = version;
    }

    public void allocate() {
        if (status != 1) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "当前出库单不可分配");
        }
        status = 2;
        version++;
    }

    public void cancel(String reason) {
        if (status >= 3 && status != 9) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "已拣货或已交接出库单不可取消");
        }
        if (status == 9) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "出库单已取消");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "取消原因不能为空");
        }
        status = 9;
        version++;
    }

    public long id() {
        return id;
    }

    public String no() {
        return no;
    }

    public String sourceType() {
        return sourceType;
    }

    public String sourceNo() {
        return sourceNo;
    }

    public long warehouseId() {
        return warehouseId;
    }

    public int status() {
        return status;
    }

    public int version() {
        return version;
    }
}
