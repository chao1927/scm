package com.chaobo.scm.purchase.application.workbench;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 工作台待办分页读模型条件。
 *
 * @param scope 已授权数据范围
 * @param createdFrom 创建时间下界
 * @param createdTo 创建时间上界
 * @param businessDate 业务日期
 * @param todoType 已校验的待办类型
 * @param pageNo 页码
 * @param pageSize 每页条数
 * @param sortField 已校验的排序字段
 * @param sortOrder 已校验的排序方向
 */
public record PurchaseTodoReadCriteria(
        PurchaseWorkbenchScope scope,
        OffsetDateTime createdFrom,
        OffsetDateTime createdTo,
        LocalDate businessDate,
        String todoType,
        int pageNo,
        int pageSize,
        String sortField,
        String sortOrder
) {

    /**
     * 计算数据库分页偏移量。
     *
     * @return 非负偏移量
     */
    public int offset() {
        return (pageNo - 1) * pageSize;
    }
}
