package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.domain.SalesOrderAggregate;
import com.chaobo.scm.oms.infrastructure.persistence.OmsMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * OmsApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class OmsApplicationServiceTest {

    /**
     * 处理当前类型职责中的操作 {@code receiveChannelOrderIsIdempotentAndCanReview}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void receiveChannelOrderIsIdempotentAndCanReview() {
        MemoryOmsMapper mapper = new MemoryOmsMapper();
        OmsApplicationService service = new OmsApplicationService(mapper);
        OmsApplicationService.ReceiveChannelOrder command = new OmsApplicationService.ReceiveChannelOrder("TMALL", "C1001", 88L, "上海市浦东新区", List.of(new SalesOrderAggregate.OrderLine("SKU1", 2, new BigDecimal("10.00"))), "{\"channelOrderNo\":\"C1001\"}", 1001L, "idem-1");
        OmsMapper.SalesOrderRow first = service.receiveChannelOrder(command);
        OmsMapper.SalesOrderRow second = service.receiveChannelOrder(command);
        OmsMapper.SalesOrderRow reviewed = service.reviewSalesOrder(first.orderNo(), new OmsApplicationService.ReviewCommand(true, "通过", 1002L, "idem-2"));
        assertThat(second.orderNo()).isEqualTo(first.orderNo());
        assertThat(reviewed.status()).isEqualTo(SalesOrderAggregate.APPROVED);
        assertThat(service.listChannelOrders()).hasSize(1);
        assertThat(service.listOutbox()).extracting(OmsMapper.OutboxRow::eventType).contains("ChannelOrderReceived", "SalesOrderCreated", "SalesOrderReviewed");
        assertThat(service.listOperationLogs()).extracting(OmsMapper.OperationLogRow::operationType).contains("RECEIVE_CHANNEL_ORDER", "APPROVE_SALES_ORDER");
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
         * channelIndex（类型：{@code Map<String,SalesOrderRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, SalesOrderRow> channelIndex = new LinkedHashMap<>();

        /**
         * channelOrders（类型：{@code List<ChannelOrderRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<ChannelOrderRow> channelOrders = new ArrayList<>();

        /**
         * outbox（类型：{@code List<OutboxRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<OutboxRow> outbox = new ArrayList<>();

        /**
         * logs（类型：{@code List<OperationLogRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<OperationLogRow> logs = new ArrayList<>();

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
            return channelIndex.get(channelCode + ":" + channelOrderNo);
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
            channelIndex.put(row.channelCode() + ":" + row.channelOrderNo(), row);
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
            channelIndex.put(row.channelCode() + ":" + row.channelOrderNo(), row);
        }

        /**
         * 查询并返回 {@code listChannelOrders}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<ChannelOrderRow>}
         */
        @Override
        public List<ChannelOrderRow> listChannelOrders() {
            return channelOrders;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertChannelOrder}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code ChannelOrderRow}
         */
        @Override
        public void insertChannelOrder(ChannelOrderRow row) {
            channelOrders.add(row);
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
         * 查询并返回 {@code listOutbox}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<OutboxRow>}
         */
        @Override
        public List<OutboxRow> listOutbox() {
            return outbox;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOperationLog}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code OperationLogRow}
         */
        @Override
        public void insertOperationLog(OperationLogRow row) {
            logs.add(row);
        }

        /**
         * 查询并返回 {@code listOperationLogs}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<OperationLogRow>}
         */
        @Override
        public List<OperationLogRow> listOperationLogs() {
            return logs;
        }
    }
}
