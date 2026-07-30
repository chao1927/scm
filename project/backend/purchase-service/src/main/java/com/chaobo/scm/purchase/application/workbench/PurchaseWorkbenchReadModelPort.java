package com.chaobo.scm.purchase.application.workbench;

import com.chaobo.scm.common.api.PageResult;

import java.util.List;

/**
 * 采购工作台读模型端口。
 *
 * <p>端口只暴露采购事实的查询能力，不提供任何聚合保存或状态变更方法。
 */
public interface PurchaseWorkbenchReadModelPort {

    /**
     * 汇总工作台指标。
     *
     * @param criteria 已授权且已校验的查询条件
     * @return 可追溯指标
     */
    List<PurchaseWorkbenchMetricView> summarize(PurchaseWorkbenchReadCriteria criteria);

    /**
     * 分页查询待办。
     *
     * @param criteria 已授权且已校验的查询条件
     * @return 含总数的分页结果
     */
    PageResult<PurchaseTodoView> pageTodos(PurchaseTodoReadCriteria criteria);
}
