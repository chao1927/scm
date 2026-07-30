package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.infrastructure.persistence.FulfillmentMapper;
import com.chaobo.scm.oms.infrastructure.persistence.OmsMapper;
import org.springframework.stereotype.Service;

/**
 * OmsQueryApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class OmsQueryApplicationService {

    /**
     * omsMapper（类型：{@code OmsMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final OmsMapper omsMapper;

    /**
     * fulfillmentMapper（类型：{@code FulfillmentMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final FulfillmentMapper fulfillmentMapper;

    /**
     * 创建 OmsQueryApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param omsMapper 持久化访问依赖，类型为 {@code OmsMapper}
     * @param fulfillmentMapper 持久化访问依赖，类型为 {@code FulfillmentMapper}
     */
    public OmsQueryApplicationService(OmsMapper omsMapper, FulfillmentMapper fulfillmentMapper) {
        this.omsMapper = omsMapper;
        this.fulfillmentMapper = fulfillmentMapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code order}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ExternalOrderView}
     */
    public ExternalOrderView order(String orderNo) {
        OmsMapper.SalesOrderRow order = omsMapper.findOrder(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("sales order not found");
        }
        FulfillmentMapper.FulfillmentRow fulfillment = fulfillmentMapper.findBySalesOrder(orderNo);
        FulfillmentMapper.OutboundRow outbound = fulfillment == null || fulfillment.outboundNo() == null ? null : fulfillmentMapper.findOutbound(fulfillment.outboundNo());
        return toView(order, fulfillment, outbound);
    }

    /**
     * 处理当前类型职责中的操作 {@code fulfillment}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ExternalOrderView}
     */
    public ExternalOrderView fulfillment(String fulfillmentNo) {
        FulfillmentMapper.FulfillmentRow fulfillment = fulfillmentMapper.findFulfillment(fulfillmentNo);
        if (fulfillment == null) {
            throw new IllegalArgumentException("fulfillment not found");
        }
        OmsMapper.SalesOrderRow order = omsMapper.findOrder(fulfillment.salesOrderNo());
        if (order == null) {
            throw new IllegalArgumentException("sales order not found");
        }
        FulfillmentMapper.OutboundRow outbound = fulfillment.outboundNo() == null ? null : fulfillmentMapper.findOutbound(fulfillment.outboundNo());
        return toView(order, fulfillment, outbound);
    }

    /**
     * 转换数据模型 {@code toView}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param order 业务处理参数或成员，类型为 {@code OmsMapper.SalesOrderRow}
     * @param fulfillment 业务处理参数或成员，类型为 {@code FulfillmentMapper.FulfillmentRow}
     * @param outbound 业务处理参数或成员，类型为 {@code FulfillmentMapper.OutboundRow}
     * @return 转换数据模型的结果，类型为 {@code ExternalOrderView}
     */
    private ExternalOrderView toView(OmsMapper.SalesOrderRow order, FulfillmentMapper.FulfillmentRow fulfillment, FulfillmentMapper.OutboundRow outbound) {
        return new ExternalOrderView(order.orderNo(), order.channelCode(), order.status(), fulfillment == null ? null : fulfillment.fulfillmentNo(), fulfillment == null ? null : fulfillment.status(), fulfillment == null ? null : fulfillment.warehouseCode(), fulfillment == null ? null : fulfillment.reservationRefNo(), outbound == null ? null : outbound.outboundNo(), outbound == null ? null : outbound.status(), outbound == null ? null : outbound.wmsOrderNo());
    }

    /**
     * ExternalOrderView。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ExternalOrderView(String salesOrderNo, String channelCode, int salesOrderStatus, String fulfillmentNo, Integer fulfillmentStatus, String warehouseCode, String reservationRefNo, String outboundNo, Integer outboundStatus, String wmsOrderNo) {
    }
}
