package com.chaobo.scm.oms.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FulfillmentAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class FulfillmentAggregateTest {

    /**
     * 处理当前类型职责中的操作 {@code allocatesWarehouseAndRequestsReservation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void allocatesWarehouseAndRequestsReservation() {
        FulfillmentAggregate aggregate = FulfillmentAggregate.create("FUL-1", "SO-1", "TMALL", 88L, 100L, "WH-1", "STANDARD", List.of(new FulfillmentAggregate.Line("SKU-1", new BigDecimal("2"))));
        aggregate.requestReservation("RES-REF-1");
        aggregate.recordReservationSuccess("INV-RES-1", new BigDecimal("2"));
        aggregate.markOutboundIssued("OUT-1");
        assertThat(aggregate.status()).isEqualTo(FulfillmentAggregate.OUTBOUND_ISSUED);
        assertThat(aggregate.reservationNo()).isEqualTo("INV-RES-1");
        assertThat(aggregate.outboundNo()).isEqualTo("OUT-1");
        assertThat(aggregate.pullEvents()).extracting(OmsEvent::eventType).contains("FulfillmentOrderCreated", "StockReservationRequested", "FulfillmentInventoryReserved", "OutboundInstructionIssued");
    }

    /**
     * 执行命令 {@code rejectsWarehouseChangeAfterReservation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectsWarehouseChangeAfterReservation() {
        FulfillmentAggregate aggregate = FulfillmentAggregate.create("FUL-1", "SO-1", "TMALL", 88L, 100L, "WH-1", "STANDARD", List.of(new FulfillmentAggregate.Line("SKU-1", new BigDecimal("2"))));
        aggregate.requestReservation("RES-REF-1");
        assertThatThrownBy(() -> aggregate.changeWarehouse(101L, "WH-2", "库存不足换仓")).isInstanceOf(IllegalStateException.class).hasMessageContaining("pending reservation");
    }

    /**
     * 处理当前类型职责中的操作 {@code splitKeepsQuantityConserved}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void splitKeepsQuantityConserved() {
        FulfillmentAggregate aggregate = FulfillmentAggregate.create("FUL-1", "SO-1", "TMALL", 88L, 100L, "WH-1", "STANDARD", List.of(new FulfillmentAggregate.Line("SKU-1", new BigDecimal("5"))));
        FulfillmentAggregate child = aggregate.split("FUL-2", List.of(new FulfillmentAggregate.Line("SKU-1", new BigDecimal("2"))), "拆分到备用仓");
        assertThat(aggregate.lines().get(0).quantity()).isEqualByComparingTo("3");
        assertThat(child.lines().get(0).quantity()).isEqualByComparingTo("2");
        assertThat(child.status()).isEqualTo(FulfillmentAggregate.PENDING_RESERVATION);
    }

    /**
     * 处理当前类型职责中的操作 {@code cannotIssueOutboundBeforeReservation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void cannotIssueOutboundBeforeReservation() {
        FulfillmentAggregate aggregate = FulfillmentAggregate.create("FUL-1", "SO-1", "TMALL", 88L, 100L, "WH-1", "STANDARD", List.of(new FulfillmentAggregate.Line("SKU-1", new BigDecimal("2"))));
        assertThatThrownBy(() -> aggregate.markOutboundIssued("OUT-1")).isInstanceOf(IllegalStateException.class).hasMessageContaining("reservation");
    }
}
