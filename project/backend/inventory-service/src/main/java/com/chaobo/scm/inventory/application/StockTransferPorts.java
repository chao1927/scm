package com.chaobo.scm.inventory.application;

import java.math.BigDecimal;

/**
 * StockTransferPorts。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class StockTransferPorts {

    /**
     * 创建 StockTransferPorts。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     */
    private StockTransferPorts() {
    }

    /**
     * StockReservation。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。以稳定接口声明调用方所需能力，具体实现可在不影响调用方的前提下替换。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public interface StockReservation {

        /**
         * 执行命令 {@code reserve}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param ownerId 业务或技术标识，类型为 {@code long}
         * @param warehouseId 业务或技术标识，类型为 {@code long}
         * @param sku 业务处理参数或成员，类型为 {@code String}
         * @param batchNo 可追踪业务编码，类型为 {@code String}
         * @param qty 数量值，类型为 {@code BigDecimal}
         * @param transferNo 可追踪业务编码，类型为 {@code String}
         * @return 执行命令的结果，类型为 {@code String}
         */
        String reserve(long ownerId, long warehouseId, String sku, String batchNo, BigDecimal qty, String transferNo);

        /**
         * 执行命令 {@code releaseForTransfer}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param transferNo 可追踪业务编码，类型为 {@code String}
         */
        void releaseForTransfer(String transferNo);

        /**
         * 处理当前类型职责中的操作 {@code outboundForTransfer}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param transferNo 可追踪业务编码，类型为 {@code String}
         * @param qty 数量值，类型为 {@code BigDecimal}
         */
        void outboundForTransfer(String transferNo, BigDecimal qty);

        /**
         * 处理当前类型职责中的操作 {@code inboundForTransfer}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param ownerId 业务或技术标识，类型为 {@code long}
         * @param warehouseId 业务或技术标识，类型为 {@code long}
         * @param sku 业务处理参数或成员，类型为 {@code String}
         * @param batchNo 可追踪业务编码，类型为 {@code String}
         * @param qty 数量值，类型为 {@code BigDecimal}
         * @param transferNo 可追踪业务编码，类型为 {@code String}
         */
        void inboundForTransfer(long ownerId, long warehouseId, String sku, String batchNo, BigDecimal qty, String transferNo);
    }

    /**
     * EventPublisher。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。以稳定接口声明调用方所需能力，具体实现可在不影响调用方的前提下替换。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public interface EventPublisher {

        /**
         * 执行命令 {@code publish}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param eventType 业务处理参数或成员，类型为 {@code String}
         * @param transferNo 可追踪业务编码，类型为 {@code String}
         * @param payload 业务处理参数或成员，类型为 {@code String}
         */
        void publish(String eventType, String transferNo, String payload);
    }
}
