package com.chaobo.scm.common.integration;

import java.io.Serializable;
import java.util.Set;

/**
 * IamCollaborationApi。
 *
 * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。定义跨进程或跨层协作端口，隔离调用方与具体技术实现。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface IamCollaborationApi {

    /**
     * 执行命令 {@code updateSupplierDataScope}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code UpdateSupplierScopeCommand}
     */
    void updateSupplierDataScope(UpdateSupplierScopeCommand command);

    /**
     * UpdateSupplierScopeCommand。
     *
     * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record UpdateSupplierScopeCommand(String idempotencyKey, long userId, Set<Long> supplierIds, String reason) implements Serializable {
    }
}
