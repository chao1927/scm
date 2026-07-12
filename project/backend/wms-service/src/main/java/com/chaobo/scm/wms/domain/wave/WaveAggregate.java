package com.chaobo.scm.wms.domain.wave;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

public class WaveAggregate {
    private final long id;
    private final String no;
    private final long warehouseId;
    private int status;
    private int version;

    public WaveAggregate(long id, String no, long warehouseId, int status, int version) {
        if (warehouseId <= 0 || no == null || no.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "波次号和仓库不能为空");
        }
        this.id = id;
        this.no = no;
        this.warehouseId = warehouseId;
        this.status = status;
        this.version = version;
    }

    public void release() {
        if (status != 1) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "波次当前不可释放");
        }
        status = 2;
        version++;
    }

    public long id() {
        return id;
    }

    public String no() {
        return no;
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
