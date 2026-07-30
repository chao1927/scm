package com.chaobo.scm.purchase.application.workbench;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 采购工作台汇总响应。
 *
 * <p>前五个计数保持产品接口的稳定契约，{@code metrics} 提供交期、价格、订单执行和异常的
 * 完整可追溯指标。
 *
 * @param pendingApprovalCount 待审批总数
 * @param pendingRfqCount 待发布询价数
 * @param pendingPublishCount 待发布采购订单数
 * @param supplierDiffCount 供应商差异数
 * @param inboundExceptionCount 到货异常数
 * @param metrics 全部可追溯指标
 * @param generatedAt 读模型生成时间
 */
public record PurchaseWorkbenchSummaryView(
        long pendingApprovalCount,
        long pendingRfqCount,
        long pendingPublishCount,
        long supplierDiffCount,
        long inboundExceptionCount,
        List<PurchaseWorkbenchMetricView> metrics,
        OffsetDateTime generatedAt
) {

    public PurchaseWorkbenchSummaryView {
        metrics = List.copyOf(metrics);
    }
}
