package com.chaobo.scm.common.integration;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * TmsCollaborationApi。
 *
 * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。定义跨进程或跨层协作端口，隔离调用方与具体技术实现。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface TmsCollaborationApi {

    /**
     * 执行命令 {@code createInboundTransport}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code InboundTransportCommand}
     * @return 执行命令的结果，类型为 {@code TransportResult}
     */
    TransportResult createInboundTransport(InboundTransportCommand command);

    /**
     * 执行命令 {@code createSupplierReturnTransport}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code ReturnTransportCommand}
     * @return 执行命令的结果，类型为 {@code TransportResult}
     */
    TransportResult createSupplierReturnTransport(ReturnTransportCommand command);

    /**
     * 执行命令 {@code cancelTransport}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CancelTransportCommand}
     */
    void cancelTransport(CancelTransportCommand command);

    /**
     * InboundTransportCommand。
     *
     * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record InboundTransportCommand(String idempotencyKey, long asnId, String asnNo, long supplierId, long warehouseId, OffsetDateTime shippedAt, String carrierCode, String trackingNo) implements Serializable {
    }

    /**
     * ReturnTransportCommand。
     *
     * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ReturnTransportCommand(String idempotencyKey, long returnId, String returnNo, long supplierId, long warehouseId, String outboundNo) implements Serializable {
    }

    /**
     * CancelTransportCommand。
     *
     * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record CancelTransportCommand(String idempotencyKey, String businessType, long businessId, String reason) implements Serializable {
    }

    /**
     * TransportResult。
     *
     * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record TransportResult(boolean accepted, String shipmentId, String waybillNo, String carrierCode, String reason) implements Serializable {
    }
}
