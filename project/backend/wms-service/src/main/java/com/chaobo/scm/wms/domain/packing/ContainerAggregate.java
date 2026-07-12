package com.chaobo.scm.wms.domain.packing;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

public class ContainerAggregate {
    private final long id;
    private final String containerNo;
    private final long outboundId;
    private final long pickTaskId;
    private int status;
    private int version;

    public ContainerAggregate(long id, String containerNo, long outboundId, long pickTaskId, int status, int version) {
        if (containerNo == null || containerNo.isBlank() || outboundId <= 0 || pickTaskId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "容器号、出库单和拣货任务不能为空");
        }
        this.id = id;
        this.containerNo = containerNo;
        this.outboundId = outboundId;
        this.pickTaskId = pickTaskId;
        this.status = status;
        this.version = version;
    }

    public void seal() {
        if (status != 1) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "容器当前不可封箱");
        }
        status = 2;
        version++;
    }

    public long id() {
        return id;
    }

    public String containerNo() {
        return containerNo;
    }

    public long outboundId() {
        return outboundId;
    }

    public long pickTaskId() {
        return pickTaskId;
    }

    public int status() {
        return status;
    }

    public int version() {
        return version;
    }
}
