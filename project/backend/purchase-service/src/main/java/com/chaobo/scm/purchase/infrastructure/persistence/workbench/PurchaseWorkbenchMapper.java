package com.chaobo.scm.purchase.infrastructure.persistence.workbench;

import com.chaobo.scm.purchase.application.workbench.PurchaseTodoReadCriteria;
import com.chaobo.scm.purchase.application.workbench.PurchaseTodoView;
import com.chaobo.scm.purchase.application.workbench.PurchaseWorkbenchMetricView;
import com.chaobo.scm.purchase.application.workbench.PurchaseWorkbenchReadCriteria;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 采购工作台 MyBatis 查询映射。
 *
 * <p>只声明采购事实查询，不包含 insert、update 或 delete，防止查询用例修改聚合。
 */
@Mapper
public interface PurchaseWorkbenchMapper {

    /**
     * 汇总可追溯工作台指标。
     *
     * @param criteria 已授权汇总条件
     * @return 指标行
     */
    List<PurchaseWorkbenchMetricView> summarize(
            @Param("criteria") PurchaseWorkbenchReadCriteria criteria);

    /**
     * 查询符合条件的待办总数。
     *
     * @param criteria 已授权分页条件
     * @return 总数
     */
    long countTodos(@Param("criteria") PurchaseTodoReadCriteria criteria);

    /**
     * 查询一页待办。
     *
     * @param criteria 已授权分页条件
     * @return 当前页记录
     */
    List<PurchaseTodoView> pageTodos(
            @Param("criteria") PurchaseTodoReadCriteria criteria);
}
