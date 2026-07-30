package com.chaobo.scm.supplier.application.qualification;

/**
 * SupplierQualificationPolicyPort。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。定义跨进程或跨层协作端口，隔离调用方与具体技术实现。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface SupplierQualificationPolicyPort {

    /**
     * 处理当前类型职责中的操作 {@code hasValidQualification}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    boolean hasValidQualification(long supplierId);

    /**
     * 处理当前类型职责中的操作 {@code assertEligible}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param categoryId 业务或技术标识，类型为 {@code Long}
     */
    void assertEligible(long supplierId, Long categoryId);
}
