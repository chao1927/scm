package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.domain.SalesOrderAggregate;
import com.chaobo.scm.oms.infrastructure.persistence.FulfillmentMapper;
import com.chaobo.scm.oms.infrastructure.persistence.OmsMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * FulfillmentApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class FulfillmentApplicationServiceTest {

    /**
     * 执行命令 {@code approvedOrderCanReserveAndDispatchOutbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void approvedOrderCanReserveAndDispatchOutbound() {
        MemoryFulfillmentMapper mapper = new MemoryFulfillmentMapper();
        MemoryOmsMapper omsMapper = new MemoryOmsMapper();
        omsMapper.orders.put("SO-1", new OmsMapper.SalesOrderRow(1L, "SO-1", "TMALL", "C-1", 88L, "上海市", "SKU-1:2:10.00", new BigDecimal("20.00"), SalesOrderAggregate.APPROVED, "通过", 2));
        FulfillmentApplicationService service = new FulfillmentApplicationService(mapper, omsMapper);
        FulfillmentMapper.FulfillmentRow fulfillment = service.allocate(new FulfillmentApplicationService.AllocateCommand("SO-1", 100L, "WH-1", "STANDARD", 1L, "a-1"));
        service.reserve(fulfillment.fulfillmentNo(), new FulfillmentApplicationService.ReserveCommand(1L, "r-1"));
        String reservationRefNo = mapper.reservations.values().iterator().next().reservationRefNo();
        service.consumeEvent(new FulfillmentApplicationService.ExternalEvent("evt-1", "StockReserved", "RES-1", fulfillment.fulfillmentNo(), reservationRefNo, "INV-1", new BigDecimal("2"), null, null, null, "{}"));
        service.createOutbound(fulfillment.fulfillmentNo(), new FulfillmentApplicationService.CreateOutboundCommand(1L, "o-1"));
        String outboundNo = mapper.outbounds.values().iterator().next().outboundNo();
        service.dispatchOutbound(outboundNo, new FulfillmentApplicationService.OutboundCommand(1L, "d-1"));
        service.consumeEvent(new FulfillmentApplicationService.ExternalEvent("evt-2", "WmsOutboundAccepted", outboundNo, fulfillment.fulfillmentNo(), null, null, null, outboundNo, "WMS-1", null, "{}"));
        service.consumeEvent(new FulfillmentApplicationService.ExternalEvent("evt-3", "WmsOutboundShipped", outboundNo, fulfillment.fulfillmentNo(), null, null, null, outboundNo, "WMS-1", null, "{}"));
        service.consumeEvent(new FulfillmentApplicationService.ExternalEvent("evt-3", "WmsOutboundShipped", outboundNo, fulfillment.fulfillmentNo(), null, null, null, outboundNo, "WMS-1", null, "{}"));
        assertThat(mapper.fulfillments.get(fulfillment.fulfillmentNo()).status()).isEqualTo(com.chaobo.scm.oms.domain.FulfillmentAggregate.SHIPPED);
        assertThat(mapper.outbounds.get(outboundNo).status()).isEqualTo(com.chaobo.scm.oms.domain.OutboundAggregate.SHIPPED);
        assertThat(mapper.commands).extracting(FulfillmentMapper.IntegrationCommandRow::commandType).containsExactly("ReserveInventory", "CreateOutboundOrder");
        assertThat(mapper.inbox).hasSize(3);
    }

    /**
     * MemoryOmsMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class MemoryOmsMapper implements OmsMapper {

        /**
         * orders（类型：{@code Map<String,SalesOrderRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, SalesOrderRow> orders = new LinkedHashMap<>();

        /**
         * 查询并返回 {@code findOrder}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param orderNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code SalesOrderRow}
         */
        @Override
        public SalesOrderRow findOrder(String orderNo) {
            return orders.get(orderNo);
        }

        /**
         * 查询并返回 {@code findByChannelOrder}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param channelCode 可追踪业务编码，类型为 {@code String}
         * @param channelOrderNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code SalesOrderRow}
         */
        @Override
        public SalesOrderRow findByChannelOrder(String channelCode, String channelOrderNo) {
            return null;
        }

        /**
         * 查询并返回 {@code listOrders}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<SalesOrderRow>}
         */
        @Override
        public List<SalesOrderRow> listOrders() {
            return new ArrayList<>(orders.values());
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOrder}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code SalesOrderRow}
         */
        @Override
        public void insertOrder(SalesOrderRow row) {
            orders.put(row.orderNo(), row);
        }

        /**
         * 执行命令 {@code updateOrder}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code SalesOrderRow}
         */
        @Override
        public void updateOrder(SalesOrderRow row) {
            orders.put(row.orderNo(), row);
        }

        /**
         * 查询并返回 {@code listChannelOrders}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<ChannelOrderRow>}
         */
        @Override
        public List<ChannelOrderRow> listChannelOrders() {
            return List.of();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertChannelOrder}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code ChannelOrderRow}
         */
        @Override
        public void insertChannelOrder(ChannelOrderRow row) {
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOutbox}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code OutboxRow}
         */
        @Override
        public void insertOutbox(OutboxRow row) {
        }

        /**
         * 查询并返回 {@code listOutbox}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<OutboxRow>}
         */
        @Override
        public List<OutboxRow> listOutbox() {
            return List.of();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOperationLog}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code OperationLogRow}
         */
        @Override
        public void insertOperationLog(OperationLogRow row) {
        }

        /**
         * 查询并返回 {@code listOperationLogs}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<OperationLogRow>}
         */
        @Override
        public List<OperationLogRow> listOperationLogs() {
            return List.of();
        }
    }

    /**
     * MemoryFulfillmentMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class MemoryFulfillmentMapper implements FulfillmentMapper {

        /**
         * fulfillments（类型：{@code Map<String,FulfillmentRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, FulfillmentRow> fulfillments = new LinkedHashMap<>();

        /**
         * reservations（类型：{@code Map<String,ReservationRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, ReservationRow> reservations = new LinkedHashMap<>();

        /**
         * outbounds（类型：{@code Map<String,OutboundRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, OutboundRow> outbounds = new LinkedHashMap<>();

        /**
         * commands（类型：{@code List<IntegrationCommandRow>}）。
         *
         * <p>保存当前对象所需的用例输入命令；其具体生命周期由所属对象统一管理。
         */
        final List<IntegrationCommandRow> commands = new ArrayList<>();

        /**
         * inbox（类型：{@code Map<String,EventInboxRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, EventInboxRow> inbox = new LinkedHashMap<>();

        /**
         * outbox（类型：{@code List<OutboxRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<OutboxRow> outbox = new ArrayList<>();

        /**
         * 查询并返回 {@code findFulfillment}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code FulfillmentRow}
         */
        @Override
        public FulfillmentRow findFulfillment(String fulfillmentNo) {
            return fulfillments.get(fulfillmentNo);
        }

        /**
         * 查询并返回 {@code findBySalesOrder}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param salesOrderNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code FulfillmentRow}
         */
        @Override
        public FulfillmentRow findBySalesOrder(String salesOrderNo) {
            return fulfillments.values().stream().filter(row -> row.salesOrderNo().equals(salesOrderNo)).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code listFulfillments}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<FulfillmentRow>}
         */
        @Override
        public List<FulfillmentRow> listFulfillments() {
            return new ArrayList<>(fulfillments.values());
        }

        /**
         * 处理当前类型职责中的操作 {@code insertFulfillment}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code FulfillmentRow}
         */
        @Override
        public void insertFulfillment(FulfillmentRow row) {
            fulfillments.put(row.fulfillmentNo(), row);
        }

        /**
         * 执行命令 {@code updateFulfillment}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code FulfillmentRow}
         */
        @Override
        public void updateFulfillment(FulfillmentRow row) {
            fulfillments.put(row.fulfillmentNo(), row);
        }

        /**
         * 查询并返回 {@code findReservation}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param reservationRefNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code ReservationRow}
         */
        @Override
        public ReservationRow findReservation(String reservationRefNo) {
            return reservations.get(reservationRefNo);
        }

        /**
         * 查询并返回 {@code findReservationByFulfillment}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code ReservationRow}
         */
        @Override
        public ReservationRow findReservationByFulfillment(String fulfillmentNo) {
            return reservations.values().stream().filter(row -> row.fulfillmentNo().equals(fulfillmentNo)).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertReservation}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code ReservationRow}
         */
        @Override
        public void insertReservation(ReservationRow row) {
            reservations.put(row.reservationRefNo(), row);
        }

        /**
         * 执行命令 {@code updateReservation}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code ReservationRow}
         */
        @Override
        public void updateReservation(ReservationRow row) {
            reservations.put(row.reservationRefNo(), row);
        }

        /**
         * 查询并返回 {@code findOutbound}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param outboundNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code OutboundRow}
         */
        @Override
        public OutboundRow findOutbound(String outboundNo) {
            return outbounds.get(outboundNo);
        }

        /**
         * 查询并返回 {@code findOutboundByFulfillment}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code OutboundRow}
         */
        @Override
        public OutboundRow findOutboundByFulfillment(String fulfillmentNo) {
            return outbounds.values().stream().filter(row -> row.fulfillmentNo().equals(fulfillmentNo)).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code listOutbounds}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<OutboundRow>}
         */
        @Override
        public List<OutboundRow> listOutbounds() {
            return new ArrayList<>(outbounds.values());
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOutbound}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code OutboundRow}
         */
        @Override
        public void insertOutbound(OutboundRow row) {
            outbounds.put(row.outboundNo(), row);
        }

        /**
         * 执行命令 {@code updateOutbound}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code OutboundRow}
         */
        @Override
        public void updateOutbound(OutboundRow row) {
            outbounds.put(row.outboundNo(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertIntegrationCommand}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code IntegrationCommandRow}
         */
        @Override
        public void insertIntegrationCommand(IntegrationCommandRow row) {
            commands.add(row);
        }

        /**
         * 处理当前类型职责中的操作 {@code claimEvent}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code EventInboxRow}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        @Override
        public int claimEvent(EventInboxRow row) {
            return inbox.putIfAbsent(row.eventId(), row) == null ? 1 : 0;
        }

        /**
         * 执行命令 {@code updateEvent}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code EventInboxRow}
         */
        @Override
        public void updateEvent(EventInboxRow row) {
            inbox.put(row.eventId(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOutbox}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code OutboxRow}
         */
        @Override
        public void insertOutbox(OutboxRow row) {
            outbox.add(row);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOperationLog}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code OperationLogRow}
         */
        @Override
        public void insertOperationLog(OperationLogRow row) {
        }
    }
}
