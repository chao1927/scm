package com.chaobo.scm.wms.domain.putaway;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.math.BigDecimal;

public class PutawayTaskAggregate {
    private final long id;
    private final String taskNo;
    private final long inspectionId;
    private final BigDecimal requiredQty;
    private BigDecimal putawayQty = BigDecimal.ZERO;
    private boolean completed;
    private int version;

    public PutawayTaskAggregate(long id, String taskNo, long inspectionId, BigDecimal requiredQty) {
        if (inspectionId <= 0 || requiredQty == null || requiredQty.signum() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "上架来源和数量不合法");
        }
        this.id = id;
        this.taskNo = taskNo;
        this.inspectionId = inspectionId;
        this.requiredQty = requiredQty;
    }

    public static PutawayTaskAggregate rehydrate(
            long id,
            String no,
            long inspectionId,
            BigDecimal required,
            BigDecimal putaway,
            boolean completed,
            int version
    ) {
        var task = new PutawayTaskAggregate(id, no, inspectionId, required);
        task.putawayQty = putaway;
        task.completed = completed;
        task.version = version;
        return task;
    }

    public void putaway(BigDecimal qty, String locationCode) {
        if (completed) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "上架任务已完成");
        }
        if (qty == null || qty.signum() <= 0 || locationCode == null || locationCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "上架数量和库位不能为空");
        }
        if (putawayQty.add(qty).compareTo(requiredQty) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "上架数量不能超过合格数量");
        }
        putawayQty = putawayQty.add(qty);
        completed = putawayQty.compareTo(requiredQty) == 0;
        version++;
    }

    public long id() {
        return id;
    }

    public String taskNo() {
        return taskNo;
    }

    public long inspectionId() {
        return inspectionId;
    }

    public BigDecimal requiredQty() {
        return requiredQty;
    }

    public BigDecimal putawayQty() {
        return putawayQty;
    }

    public boolean completed() {
        return completed;
    }

    public int version() {
        return version;
    }
}
