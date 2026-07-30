package com.chaobo.scm.supplier.application.contract;

import com.chaobo.scm.supplier.domain.contract.*;
import com.chaobo.scm.supplier.domain.quote.*;

/**
 * PriceAgreementProjectionPort。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。定义跨进程或跨层协作端口，隔离调用方与具体技术实现。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface PriceAgreementProjectionPort {

    /**
     * 处理当前类型职责中的操作 {@code activate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param contract 业务处理参数或成员，类型为 {@code SupplierContractAggregate}
     * @param quote 业务处理参数或成员，类型为 {@code SupplierQuoteAggregate}
     */
    void activate(SupplierContractAggregate contract, SupplierQuoteAggregate quote);

    /**
     * 处理当前类型职责中的操作 {@code renew}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param contract 业务处理参数或成员，类型为 {@code SupplierContractAggregate}
     */
    void renew(SupplierContractAggregate contract);

    /**
     * 处理当前类型职责中的操作 {@code terminate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param contract 业务处理参数或成员，类型为 {@code SupplierContractAggregate}
     */
    void terminate(SupplierContractAggregate contract);
}
