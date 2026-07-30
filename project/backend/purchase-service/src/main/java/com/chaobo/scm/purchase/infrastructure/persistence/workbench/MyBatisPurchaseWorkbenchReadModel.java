package com.chaobo.scm.purchase.infrastructure.persistence.workbench;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.workbench.PurchaseTodoReadCriteria;
import com.chaobo.scm.purchase.application.workbench.PurchaseTodoView;
import com.chaobo.scm.purchase.application.workbench.PurchaseWorkbenchMetricView;
import com.chaobo.scm.purchase.application.workbench.PurchaseWorkbenchReadCriteria;
import com.chaobo.scm.purchase.application.workbench.PurchaseWorkbenchReadModelPort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 基于现有采购事实表的工作台读模型实现。
 *
 * <p>分页总数和记录使用同一组 SQL 事实定义与过滤条件，避免页面总数和明细口径漂移。
 */
@Repository
public class MyBatisPurchaseWorkbenchReadModel implements PurchaseWorkbenchReadModelPort {

    private final PurchaseWorkbenchMapper mapper;

    /**
     * 创建读模型适配器。
     *
     * @param mapper 工作台查询映射
     */
    public MyBatisPurchaseWorkbenchReadModel(PurchaseWorkbenchMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<PurchaseWorkbenchMetricView> summarize(
            PurchaseWorkbenchReadCriteria criteria
    ) {
        return mapper.summarize(criteria);
    }

    @Override
    public PageResult<PurchaseTodoView> pageTodos(PurchaseTodoReadCriteria criteria) {
        long total = mapper.countTodos(criteria);
        List<PurchaseTodoView> records = total == 0L
                ? List.of()
                : mapper.pageTodos(criteria);
        return new PageResult<>(
                criteria.pageNo(), criteria.pageSize(), total, records);
    }
}
