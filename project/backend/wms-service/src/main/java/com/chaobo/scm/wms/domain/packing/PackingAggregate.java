package com.chaobo.scm.wms.domain.packing;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

public class PackingAggregate {
    private final long id;
    private final String packingNo;
    private final long outboundId;
    private final String containerNo;
    private int status;
    private int version;

    public PackingAggregate(long id, String packingNo, long outboundId, String containerNo, int status, int version) {
        if (packingNo == null || packingNo.isBlank() || outboundId <= 0 || containerNo == null || containerNo.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "复核包装来源不能为空");
        }
        this.id = id;
        this.packingNo = packingNo;
        this.outboundId = outboundId;
        this.containerNo = containerNo;
        this.status = status;
        this.version = version;
    }

    public void verify() {
        if (status != 1) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "复核包装单当前不可确认");
        }
        status = 2;
        version++;
    }

    public long id() {
        return id;
    }

    public String packingNo() {
        return packingNo;
    }

    public long outboundId() {
        return outboundId;
    }

    public String containerNo() {
        return containerNo;
    }

    public int status() {
        return status;
    }

    public int version() {
        return version;
    }
}
