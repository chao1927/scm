package com.chaobo.scm.purchase.application.workbench;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 工作台汇总读模型条件。
 *
 * @param scope 已授权数据范围
 * @param createdFrom 创建时间下界
 * @param createdTo 创建时间上界
 * @param businessDate 计算交期、价格有效性的业务日期
 */
public record PurchaseWorkbenchReadCriteria(
        PurchaseWorkbenchScope scope,
        OffsetDateTime createdFrom,
        OffsetDateTime createdTo,
        LocalDate businessDate
) {
}
