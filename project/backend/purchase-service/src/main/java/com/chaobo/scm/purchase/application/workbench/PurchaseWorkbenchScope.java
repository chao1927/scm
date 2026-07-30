package com.chaobo.scm.purchase.application.workbench;

import java.util.Set;

/**
 * 采购工作台已授权数据范围。
 *
 * <p>该对象只能由应用服务根据可信令牌构造，基础设施查询不得直接信任 HTTP 参数。
 *
 * @param purchaseOrgIds 允许查询的采购组织集合
 * @param unrestrictedOrganizations 是否拥有全部采购组织范围
 * @param purchaseGroupId 采购组标识；通过采购组成员投影限制事实负责人
 * @param ownerId 本人范围对应的事实负责人
 */
public record PurchaseWorkbenchScope(
        Set<Long> purchaseOrgIds,
        boolean unrestrictedOrganizations,
        Long purchaseGroupId,
        Long ownerId
) {

    public PurchaseWorkbenchScope {
        purchaseOrgIds = Set.copyOf(purchaseOrgIds);
    }

    /**
     * 判断当前令牌是否没有任何可见采购组织。
     *
     * @return 无组织范围时返回 true
     */
    public boolean isEmpty() {
        return !unrestrictedOrganizations && purchaseOrgIds.isEmpty();
    }
}
