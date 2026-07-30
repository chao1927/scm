package com.chaobo.scm.supplier.application.account;

import java.time.OffsetDateTime;

/**
 * SupplierUserBindingView。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record SupplierUserBindingView(long bindingId, long supplierId, long userId, String bindingRole, boolean primary, int status, OffsetDateTime boundAt, int version) {
}
