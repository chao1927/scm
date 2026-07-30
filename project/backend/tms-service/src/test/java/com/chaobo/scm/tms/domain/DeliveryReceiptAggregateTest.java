package com.chaobo.scm.tms.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DeliveryReceiptAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class DeliveryReceiptAggregateTest {

    /**
     * 处理当前类型职责中的操作 {@code signedReceiptPublishesSignedEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void signedReceiptPublishesSignedEvent() {
        DeliveryReceiptAggregate receipt = DeliveryReceiptAggregate.record("RCP1", "WB1", DeliveryReceiptAggregate.SIGNED, "李四", LocalDateTime.parse("2026-07-12T12:00:00"), null, "oss://proof/RCP1.jpg");
        assertThat(receipt.pullEvents()).extracting(TmsEvent::eventType).containsExactly("TransportSigned");
    }

    /**
     * 执行命令 {@code rejectedReceiptRequiresReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectedReceiptRequiresReason() {
        assertThatThrownBy(() -> DeliveryReceiptAggregate.record("RCP1", "WB1", DeliveryReceiptAggregate.REJECTED, null, LocalDateTime.parse("2026-07-12T12:00:00"), null, null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("reject reason");
    }
}
