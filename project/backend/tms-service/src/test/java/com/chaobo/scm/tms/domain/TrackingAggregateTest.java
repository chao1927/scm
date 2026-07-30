package com.chaobo.scm.tms.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * TrackingAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class TrackingAggregateTest {

    /**
     * 处理当前类型职责中的操作 {@code appendArrivalPublishesTrackingAndArrivedEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void appendArrivalPublishesTrackingAndArrivedEvents() {
        TrackingAggregate tracking = TrackingAggregate.append("TRK1", "WB1", "ARRIVED", "到达目的地", "杭州", LocalDateTime.parse("2026-07-12T10:00:00"), "CARRIER:SF", "evt-1");
        assertThat(tracking.pullEvents()).extracting(TmsEvent::eventType).containsExactly("TrackingAppended", "TransportArrived");
    }

    /**
     * 处理当前类型职责中的操作 {@code supplementPublishesSupplementedEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void supplementPublishesSupplementedEvent() {
        TrackingAggregate tracking = TrackingAggregate.supplement("TRK1", "WB1", "IN_TRANSIT", "人工补录在途", "嘉兴", LocalDateTime.parse("2026-07-12T11:00:00"), "承运商漏推");
        assertThat(tracking.sourceType()).isEqualTo("MANUAL");
        assertThat(tracking.pullEvents()).extracting(TmsEvent::eventType).containsExactly("TrackingSupplemented");
    }
}
