package com.chaobo.scm.wms.domain.picking;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.math.BigDecimal;

public class PickTaskAggregate {
    private final long id;
    private final String no;
    private final long waveId;
    private final long outboundId;
    private final String sku;
    private final BigDecimal required;
    private BigDecimal picked;
    private int status;
    private int version;

    public PickTaskAggregate(
            long id,
            String no,
            long waveId,
            long outboundId,
            String sku,
            BigDecimal required,
            BigDecimal picked,
            int status,
            int version
    ) {
        if (required == null || required.signum() <= 0 || sku == null || sku.isBlank()
                || no == null || no.isBlank() || waveId <= 0 || outboundId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "拣货任务数据不合法");
        }
        this.id = id;
        this.no = no;
        this.waveId = waveId;
        this.outboundId = outboundId;
        this.sku = sku;
        this.required = required;
        this.picked = picked == null ? BigDecimal.ZERO : picked;
        this.status = status;
        this.version = version;
    }

    public void pick(BigDecimal qty) {
        if (status == 3) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "拣货任务已完成");
        }
        if (qty == null || qty.signum() <= 0 || picked.add(qty).compareTo(required) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "拣货数量超过应拣数量");
        }
        picked = picked.add(qty);
        status = picked.compareTo(required) == 0 ? 3 : 2;
        version++;
    }

    public long id() {
        return id;
    }

    public String no() {
        return no;
    }

    public long waveId() {
        return waveId;
    }

    public long outboundId() {
        return outboundId;
    }

    public String sku() {
        return sku;
    }

    public BigDecimal required() {
        return required;
    }

    public BigDecimal picked() {
        return picked;
    }

    public int status() {
        return status;
    }

    public int version() {
        return version;
    }
}
