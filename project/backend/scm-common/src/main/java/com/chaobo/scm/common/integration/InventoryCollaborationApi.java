package com.chaobo.scm.common.integration;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * InventoryCollaborationApi。
 *
 * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。定义跨进程或跨层协作端口，隔离调用方与具体技术实现。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface InventoryCollaborationApi {

    /**
     * 处理当前类型职责中的操作 {@code lockSupplierReturn}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code ReturnLockCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code LockResult}
     */
    LockResult lockSupplierReturn(ReturnLockCommand command);

    /**
     * 执行命令 {@code releaseSupplierReturn}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code ReturnReleaseCommand}
     */
    void releaseSupplierReturn(ReturnReleaseCommand command);

    /**
     * ReturnLockCommand。
     *
     * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ReturnLockCommand(String idempotencyKey, long returnId, String returnNo, long supplierId, long warehouseId, List<Line> lines) implements Serializable {
    }

    /**
     * ReturnReleaseCommand。
     *
     * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ReturnReleaseCommand(String idempotencyKey, long returnId, String lockNo, String reason) implements Serializable {
    }

    /**
     * Line。
     *
     * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Line(long lineId, String skuCode, String batchNo, String inventoryStatus, BigDecimal quantity) implements Serializable {
    }

    /**
     * LockedLine。
     *
     * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record LockedLine(long lineId, BigDecimal lockedQuantity) implements Serializable {
    }

    /**
     * LockResult。
     *
     * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record LockResult(boolean accepted, String lockNo, List<LockedLine> lines, String reason) implements Serializable {
    }
}
