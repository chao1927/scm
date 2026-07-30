package com.chaobo.scm.inventory.infrastructure.transfer;

import com.chaobo.scm.inventory.application.InventoryApplicationService;
import com.chaobo.scm.inventory.application.InventoryEventPublisher;
import com.chaobo.scm.inventory.application.StockTransferPorts;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * InventoryStockTransferAdapters。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class InventoryStockTransferAdapters {

    /**
     * 创建 InventoryStockTransferAdapters。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     */
    private InventoryStockTransferAdapters() {
    }

    /**
     * ReservationAdapter。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    @Component
    public static class ReservationAdapter implements StockTransferPorts.StockReservation {

        /**
         * inventory（类型：{@code InventoryApplicationService}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final InventoryApplicationService inventory;

        /**
         * 创建 ReservationAdapter。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param inventory 业务处理参数或成员，类型为 {@code InventoryApplicationService}
         */
        public ReservationAdapter(InventoryApplicationService inventory) {
            this.inventory = inventory;
        }

        /**
         * 执行命令 {@code reserve}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param ownerId 业务或技术标识，类型为 {@code long}
         * @param warehouseId 业务或技术标识，类型为 {@code long}
         * @param sku 业务处理参数或成员，类型为 {@code String}
         * @param batchNo 可追踪业务编码，类型为 {@code String}
         * @param qty 数量值，类型为 {@code BigDecimal}
         * @param transferNo 可追踪业务编码，类型为 {@code String}
         * @return 执行命令的结果，类型为 {@code String}
         */
        @Override
        public String reserve(long ownerId, long warehouseId, String sku, String batchNo, BigDecimal qty, String transferNo) {
            return inventory.reserve(new InventoryApplicationService.ReservationCommand(ownerId, warehouseId, sku, batchNo, qty, "INVENTORY_TRANSFER", transferNo)).reservationNo();
        }

        /**
         * 执行命令 {@code releaseForTransfer}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param transferNo 可追踪业务编码，类型为 {@code String}
         */
        @Override
        public void releaseForTransfer(String transferNo) {
            inventory.releaseBySource("INVENTORY_TRANSFER", transferNo);
        }

        /**
         * 处理当前类型职责中的操作 {@code outboundForTransfer}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param transferNo 可追踪业务编码，类型为 {@code String}
         * @param qty 数量值，类型为 {@code BigDecimal}
         */
        @Override
        public void outboundForTransfer(String transferNo, BigDecimal qty) {
            inventory.outboundByReservationSource("INVENTORY_TRANSFER", transferNo, qty);
        }

        /**
         * 处理当前类型职责中的操作 {@code inboundForTransfer}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param ownerId 业务或技术标识，类型为 {@code long}
         * @param warehouseId 业务或技术标识，类型为 {@code long}
         * @param sku 业务处理参数或成员，类型为 {@code String}
         * @param batchNo 可追踪业务编码，类型为 {@code String}
         * @param qty 数量值，类型为 {@code BigDecimal}
         * @param transferNo 可追踪业务编码，类型为 {@code String}
         */
        @Override
        public void inboundForTransfer(long ownerId, long warehouseId, String sku, String batchNo, BigDecimal qty, String transferNo) {
            inventory.inbound(new InventoryApplicationService.AccountCommand(ownerId, warehouseId, sku, batchNo, qty, "INVENTORY_TRANSFER", transferNo));
        }
    }

    /**
     * EventAdapter。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    @Component
    public static class EventAdapter implements StockTransferPorts.EventPublisher {

        /**
         * events（类型：{@code InventoryEventPublisher}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final InventoryEventPublisher events;

        /**
         * 创建 EventAdapter。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param events 业务处理参数或成员，类型为 {@code InventoryEventPublisher}
         */
        public EventAdapter(InventoryEventPublisher events) {
            this.events = events;
        }

        /**
         * 执行命令 {@code publish}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param eventType 业务处理参数或成员，类型为 {@code String}
         * @param transferNo 可追踪业务编码，类型为 {@code String}
         * @param payload 业务处理参数或成员，类型为 {@code String}
         */
        @Override
        public void publish(String eventType, String transferNo, String payload) {
            events.publish(eventType, "STOCK_TRANSFER", transferNo, payload);
        }
    }
}
