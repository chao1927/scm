package com.chaobo.scm.purchase.application.supplierreturn;

import com.chaobo.scm.common.api.PageResult;
import java.util.Optional;

/**
 * SupplierReturnReadModelPort。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。定义跨进程或跨层协作端口，隔离调用方与具体技术实现。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface SupplierReturnReadModelPort {

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<SupplierReturnView>}
     */
    PageResult<SupplierReturnView> page(Long purchaseOrgId, Long supplierId, String warehouseCode, Integer status, int pageNo, int pageSize);

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param returnNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Optional<SupplierReturnView>}
     */
    Optional<SupplierReturnView> detail(String returnNo);
}
