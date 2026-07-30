package com.chaobo.scm.common.integration;

import java.io.Serializable;

/**
 * MasterDataCollaborationApi。
 *
 * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。定义跨进程或跨层协作端口，隔离调用方与具体技术实现。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface MasterDataCollaborationApi {

    /**
     * 执行命令 {@code createSupplier}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CreateSupplierCommand}
     * @return 执行命令的结果，类型为 {@code SupplierResult}
     */
    SupplierResult createSupplier(CreateSupplierCommand command);

    /**
     * 处理当前类型职责中的操作 {@code changeSupplierStatus}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code ChangeSupplierStatusCommand}
     */
    void changeSupplierStatus(ChangeSupplierStatusCommand command);

    /**
     * CreateSupplierCommand。
     *
     * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record CreateSupplierCommand(String idempotencyKey, long admissionId, String admissionNo, String supplierCode, String supplierName, String taxNo, String supplierType, String contactName, String contactMobile, String settlementJson) implements Serializable {
    }

    /**
     * ChangeSupplierStatusCommand。
     *
     * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ChangeSupplierStatusCommand(String idempotencyKey, long supplierId, int targetStatus, String reason) implements Serializable {
    }

    /**
     * SupplierResult。
     *
     * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record SupplierResult(boolean accepted, long supplierId, String supplierCode, String reason) implements Serializable {
    }
}
