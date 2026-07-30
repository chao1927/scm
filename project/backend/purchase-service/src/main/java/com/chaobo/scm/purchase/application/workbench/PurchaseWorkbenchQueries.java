package com.chaobo.scm.purchase.application.workbench;

import java.time.OffsetDateTime;

/**
 * 采购工作台查询协议。
 *
 * <p>集中定义接口层传入应用层的查询条件，查询对象不包含任何状态变更意图。
 */
public final class PurchaseWorkbenchQueries {

    private PurchaseWorkbenchQueries() {
    }

    /**
     * 工作台汇总查询。
     *
     * @param purchaseOrgId 采购组织；为空时按令牌中的全部可见组织查询
     * @param purchaseGroupId 采购组；仅采购组范围使用
     * @param scopeMode 数据范围模式：ORGANIZATION、PURCHASE_GROUP 或 SELF
     * @param createdFrom 事实创建时间下界
     * @param createdTo 事实创建时间上界
     */
    public record SummaryQuery(
            Long purchaseOrgId,
            Long purchaseGroupId,
            String scopeMode,
            OffsetDateTime createdFrom,
            OffsetDateTime createdTo
    ) {
    }

    /**
     * 工作台待办分页查询。
     *
     * @param purchaseOrgId 采购组织
     * @param purchaseGroupId 采购组
     * @param scopeMode 数据范围模式
     * @param createdFrom 事实创建时间下界
     * @param createdTo 事实创建时间上界
     * @param todoType 待办类型；为空时查询全部类型
     * @param pageNo 页码，从 1 开始
     * @param pageSize 每页条数，仅允许 10、20、50
     * @param sortField 排序字段白名单
     * @param sortOrder asc 或 desc
     */
    public record TodoPageQuery(
            Long purchaseOrgId,
            Long purchaseGroupId,
            String scopeMode,
            OffsetDateTime createdFrom,
            OffsetDateTime createdTo,
            String todoType,
            int pageNo,
            int pageSize,
            String sortField,
            String sortOrder
    ) {
    }
}
