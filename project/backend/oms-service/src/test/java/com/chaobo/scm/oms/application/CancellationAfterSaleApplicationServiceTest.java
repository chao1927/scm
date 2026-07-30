package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.domain.FulfillmentAggregate;
import com.chaobo.scm.oms.domain.SalesOrderAggregate;
import com.chaobo.scm.oms.infrastructure.persistence.CancellationMapper;
import com.chaobo.scm.oms.infrastructure.persistence.FulfillmentMapper;
import com.chaobo.scm.oms.infrastructure.persistence.OmsMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * CancellationAfterSaleApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class CancellationAfterSaleApplicationServiceTest {

    /**
     * 执行命令 {@code cancellationWithoutOutboundCompletesAfterLocalStockCheck}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void cancellationWithoutOutboundCompletesAfterLocalStockCheck() {
        FulfillmentApplicationServiceTest.MemoryFulfillmentMapper fulfillmentMapper = new FulfillmentApplicationServiceTest.MemoryFulfillmentMapper();
        fulfillmentMapper.fulfillments.put("FUL-1", new FulfillmentMapper.FulfillmentRow("FUL-1", "SO-1", "TMALL", 88L, 100L, "WH-1", "STANDARD", "SKU-1:2:0:0", FulfillmentAggregate.PENDING_RESERVATION, null, null, null, null, null, 1));
        MemoryCancellationMapper cancellationMapper = new MemoryCancellationMapper();
        CancellationApplicationService service = new CancellationApplicationService(cancellationMapper, new FulfillmentApplicationService(fulfillmentMapper, new FulfillmentApplicationServiceTest.MemoryOmsMapper()));
        CancellationMapper.CancelRow created = service.create(new CancellationApplicationService.CreateCommand("FUL-1", "客户取消", 1L, "c-1"));
        service.approve(created.cancellationNo(), new CancellationApplicationService.ApproveCommand("同意", 1L, "c-2"));
        CancellationMapper.CancelRow completed = service.process(created.cancellationNo(), new CancellationApplicationService.ProcessCommand(1L, "c-3"));
        assertThat(completed.status()).isEqualTo(4);
        assertThat(cancellationMapper.outbox).extracting(CancellationMapper.OutboxRow::eventType).contains("SalesOrderCanceled");
    }

    /**
     * 执行命令 {@code approvedAfterSaleRequestsBmsRefundAndConsumesCompletion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void approvedAfterSaleRequestsBmsRefundAndConsumesCompletion() {
        MemoryCancellationMapper cancellationMapper = new MemoryCancellationMapper();
        FulfillmentApplicationServiceTest.MemoryOmsMapper omsMapper = new FulfillmentApplicationServiceTest.MemoryOmsMapper();
        omsMapper.orders.put("SO-1", new OmsMapper.SalesOrderRow(1L, "SO-1", "TMALL", "C-1", 88L, "上海市", "SKU-1:2:10.00", new BigDecimal("20.00"), SalesOrderAggregate.APPROVED, "通过", 2));
        AfterSaleApplicationService service = new AfterSaleApplicationService(cancellationMapper, omsMapper);
        CancellationMapper.AfterSaleRow created = service.create(new AfterSaleApplicationService.CreateCommand("SO-1", "FUL-1", new BigDecimal("20.00"), "仅退款", 1L, "a-1"));
        service.approve(created.afterSaleNo(), new AfterSaleApplicationService.ApproveCommand("同意", 1L, "a-2"));
        service.requestRefund(created.afterSaleNo(), new AfterSaleApplicationService.RefundCommand(1L, "a-3"));
        service.consumeEvent(new AfterSaleApplicationService.RefundEvent("refund-1", "RefundCompleted", created.afterSaleNo(), created.afterSaleNo(), new BigDecimal("20.00"), "{}"));
        CancellationMapper.AfterSaleRow completed = service.complete(created.afterSaleNo(), new AfterSaleApplicationService.CompleteCommand(1L, "a-4"));
        assertThat(completed.status()).isEqualTo(5);
        assertThat(cancellationMapper.commands).extracting(CancellationMapper.IntegrationCommandRow::targetSystem).containsExactly("BMS");
    }

    /**
     * MemoryCancellationMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class MemoryCancellationMapper implements CancellationMapper {

        /**
         * cancels（类型：{@code Map<String,CancelRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, CancelRow> cancels = new LinkedHashMap<>();

        /**
         * afterSales（类型：{@code Map<String,AfterSaleRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, AfterSaleRow> afterSales = new LinkedHashMap<>();

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
         * 查询并返回 {@code findCancel}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param cancellationNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code CancelRow}
         */
        @Override
        public CancelRow findCancel(String cancellationNo) {
            return cancels.get(cancellationNo);
        }

        /**
         * 查询并返回 {@code findCancelByFulfillment}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code CancelRow}
         */
        @Override
        public CancelRow findCancelByFulfillment(String fulfillmentNo) {
            return cancels.values().stream().filter(row -> row.fulfillmentNo().equals(fulfillmentNo)).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code findCancelByOutbound}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param outboundNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code CancelRow}
         */
        @Override
        public CancelRow findCancelByOutbound(String outboundNo) {
            return null;
        }

        /**
         * 查询并返回 {@code findCancelByReservation}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param reservationRefNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code CancelRow}
         */
        @Override
        public CancelRow findCancelByReservation(String reservationRefNo) {
            return null;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertCancel}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code CancelRow}
         */
        @Override
        public void insertCancel(CancelRow row) {
            cancels.put(row.cancellationNo(), row);
        }

        /**
         * 执行命令 {@code updateCancel}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code CancelRow}
         */
        @Override
        public void updateCancel(CancelRow row) {
            cancels.put(row.cancellationNo(), row);
        }

        /**
         * 查询并返回 {@code findAfterSale}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param afterSaleNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code AfterSaleRow}
         */
        @Override
        public AfterSaleRow findAfterSale(String afterSaleNo) {
            return afterSales.get(afterSaleNo);
        }

        /**
         * 查询并返回 {@code findAfterSaleByOrder}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param salesOrderNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code AfterSaleRow}
         */
        @Override
        public AfterSaleRow findAfterSaleByOrder(String salesOrderNo) {
            return afterSales.values().stream().filter(row -> row.salesOrderNo().equals(salesOrderNo)).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertAfterSale}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code AfterSaleRow}
         */
        @Override
        public void insertAfterSale(AfterSaleRow row) {
            afterSales.put(row.afterSaleNo(), row);
        }

        /**
         * 执行命令 {@code updateAfterSale}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code AfterSaleRow}
         */
        @Override
        public void updateAfterSale(AfterSaleRow row) {
            afterSales.put(row.afterSaleNo(), row);
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
