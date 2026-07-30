package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.domain.ReverseAfterSaleAggregate;
import com.chaobo.scm.oms.domain.SalesOrderAggregate;
import com.chaobo.scm.oms.infrastructure.persistence.OmsMapper;
import com.chaobo.scm.oms.infrastructure.persistence.ReverseAfterSaleMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReverseAfterSaleApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class ReverseAfterSaleApplicationServiceTest {

    /**
     * 处理当前类型职责中的操作 {@code orchestratesReturnInspectionAndRefundWithInboxIdempotency}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void orchestratesReturnInspectionAndRefundWithInboxIdempotency() {
        MemoryReverseMapper mapper = new MemoryReverseMapper();
        FulfillmentApplicationServiceTest.MemoryOmsMapper orders = new FulfillmentApplicationServiceTest.MemoryOmsMapper();
        orders.orders.put("SO-1", new OmsMapper.SalesOrderRow(1L, "SO-1", "TMALL", "C-1", 88L, "上海市", "SKU-1:2:10.00", new BigDecimal("20"), SalesOrderAggregate.APPROVED, "通过", 2));
        var service = new ReverseAfterSaleApplicationService(mapper, orders);
        var created = service.create(new ReverseAfterSaleApplicationService.Create(ReverseAfterSaleAggregate.Type.RETURN_REFUND, "SO-1", "FUL-1", 88, "SKU-1", new BigDecimal("2"), new BigDecimal("20"), 10, "质量问题"));
        var approved = service.approve(created.afterSaleNo(), created.version());
        service.consume(new ReverseAfterSaleApplicationService.Event("WMS-1", "ReturnInspected", created.afterSaleNo(), new BigDecimal("2"), new BigDecimal("2"), null, false, "{}"));
        service.consume(new ReverseAfterSaleApplicationService.Event("WMS-1", "ReturnInspected", created.afterSaleNo(), new BigDecimal("2"), new BigDecimal("2"), null, false, "{}"));
        var inspected = service.get(created.afterSaleNo());
        var requested = service.requestRefund(created.afterSaleNo(), inspected.version());
        service.consume(new ReverseAfterSaleApplicationService.Event("BMS-1", "RefundCompleted", created.afterSaleNo(), null, null, new BigDecimal("20"), false, "{}"));
        assertThat(approved.rmaNo()).isNotBlank();
        assertThat(mapper.commands).extracting(Command::target).containsExactly("TMS", "WMS", "BMS");
        assertThat(requested.status()).isEqualTo(ReverseAfterSaleAggregate.REFUND_REQUESTED);
        assertThat(service.get(created.afterSaleNo()).status()).isEqualTo(ReverseAfterSaleAggregate.COMPLETED);
        assertThat(mapper.inbox).hasSize(2);
    }

    /**
     * Command。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Command(String type, String target, String businessNo, String key, String payload) {
    }

    /**
     * MemoryReverseMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class MemoryReverseMapper implements ReverseAfterSaleMapper {

        /**
         * rows（类型：{@code Map<String,Row>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, Row> rows = new LinkedHashMap<>();

        /**
         * commands（类型：{@code List<Command>}）。
         *
         * <p>保存当前对象所需的用例输入命令；其具体生命周期由所属对象统一管理。
         */
        final List<Command> commands = new ArrayList<>();

        /**
         * inbox（类型：{@code Map<String,Integer>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, Integer> inbox = new LinkedHashMap<>();

        /**
         * 查询并返回 {@code find}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param no 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Row}
         */
        @Override
        public Row find(String no) {
            return rows.get(no);
        }

        /**
         * 查询并返回 {@code findActive}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param orderNo 可追踪业务编码，类型为 {@code String}
         * @param sku 业务处理参数或成员，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Row}
         */
        @Override
        public Row findActive(String orderNo, String sku) {
            return rows.values().stream().filter(r -> r.salesOrderNo().equals(orderNo) && r.sku().equals(sku) && r.status() != 8 && r.status() != 9).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code insert}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code Row}
         */
        @Override
        public void insert(Row row) {
            rows.put(row.afterSaleNo(), row);
        }

        /**
         * 执行命令 {@code update}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code Row}
         * @param oldVersion 乐观锁或契约版本，类型为 {@code long}
         * @return 执行命令的结果，类型为 {@code int}
         */
        @Override
        public int update(Row row, long oldVersion) {
            Row current = rows.get(row.afterSaleNo());
            if (current == null || current.version() != oldVersion) {
                return 0;
            }
            rows.put(row.afterSaleNo(), row);
            return 1;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertCommand}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param type 业务处理参数或成员，类型为 {@code String}
         * @param target 业务处理参数或成员，类型为 {@code String}
         * @param businessNo 可追踪业务编码，类型为 {@code String}
         * @param key 业务处理参数或成员，类型为 {@code String}
         * @param payload 业务处理参数或成员，类型为 {@code String}
         */
        @Override
        public void insertCommand(String type, String target, String businessNo, String key, String payload) {
            commands.add(new Command(type, target, businessNo, key, payload));
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOutbox}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param type 业务处理参数或成员，类型为 {@code String}
         * @param businessNo 可追踪业务编码，类型为 {@code String}
         * @param payload 业务处理参数或成员，类型为 {@code String}
         * @param occurredAt 业务时间，类型为 {@code LocalDateTime}
         */
        @Override
        public void insertOutbox(String type, String businessNo, String payload, LocalDateTime occurredAt) {
        }

        /**
         * 处理当前类型职责中的操作 {@code claimEvent}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param eventId 业务或技术标识，类型为 {@code String}
         * @param eventType 业务处理参数或成员，类型为 {@code String}
         * @param businessNo 可追踪业务编码，类型为 {@code String}
         * @param payload 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        @Override
        public int claimEvent(String eventId, String eventType, String businessNo, String payload) {
            return inbox.putIfAbsent(eventId, 1) == null ? 1 : 0;
        }

        /**
         * 处理当前类型职责中的操作 {@code finishEvent}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param eventId 业务或技术标识，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code int}
         * @param error 业务处理参数或成员，类型为 {@code String}
         */
        @Override
        public void finishEvent(String eventId, int status, String error) {
            inbox.put(eventId, status);
        }
    }
}
