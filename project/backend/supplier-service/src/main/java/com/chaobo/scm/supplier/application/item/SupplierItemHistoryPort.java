package com.chaobo.scm.supplier.application.item;

import com.chaobo.scm.supplier.domain.item.SupplierItemAggregate;

/**
 * SupplierItemHistoryPort。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。定义跨进程或跨层协作端口，隔离调用方与具体技术实现。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface SupplierItemHistoryPort {

    /**
     * 处理当前类型职责中的操作 {@code recordCondition}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param aggregate 业务处理参数或成员，类型为 {@code SupplierItemAggregate}
     * @param changeType 业务处理参数或成员，类型为 {@code String}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    void recordCondition(SupplierItemAggregate aggregate, String changeType, long operatorId);
}
