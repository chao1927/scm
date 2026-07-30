package com.chaobo.scm.inventory.application;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * WMS 批次效期事实投影端口。
 *
 * <p>效期只能由明确携带效期的 WMS 上架事实更新；库存账面数量不能推导或伪造效期。
 *
 * @author SCM Team
 */
@FunctionalInterface
public interface InventoryBatchFactPort {

    /**
     * 幂等记录一次批次效期事实。
     *
     * @param stockId 库存账户标识
     * @param expiryDate 失效日期
     * @param sourceEvent 来源事件编码
     * @param factAt 事实发生时间
     */
    void recordExpiry(long stockId, LocalDate expiryDate, String sourceEvent, LocalDateTime factAt);
}
