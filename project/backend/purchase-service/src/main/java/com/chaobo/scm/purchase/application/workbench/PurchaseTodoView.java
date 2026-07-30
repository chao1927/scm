package com.chaobo.scm.purchase.application.workbench;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 采购工作台待办读模型。
 *
 * @param todoId 跨待办类型稳定唯一标识
 * @param businessType 业务类型
 * @param businessNo 业务单号
 * @param title 待办标题
 * @param statusName 当前事实状态
 * @param priority 优先级
 * @param targetRoute 下钻路由
 * @param purchaseOrgId 采购组织
 * @param ownerId 事实负责人
 * @param dueDate 到期日期
 * @param sourceTable 事实来源表
 * @param sourceId 事实主键
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record PurchaseTodoView(
        String todoId,
        String businessType,
        String businessNo,
        String title,
        String statusName,
        String priority,
        String targetRoute,
        Long purchaseOrgId,
        Long ownerId,
        LocalDate dueDate,
        String sourceTable,
        String sourceId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
