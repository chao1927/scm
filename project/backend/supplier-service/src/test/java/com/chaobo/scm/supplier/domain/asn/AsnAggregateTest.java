package com.chaobo.scm.supplier.domain.asn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AsnAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class AsnAggregateTest {

    /**
     * generator（类型：{@code TestIdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final TestIdentifierGenerator generator = new TestIdentifierGenerator();

    /**
     * 处理当前类型职责中的操作 {@code shouldCreateDraftAndRaiseEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldCreateDraftAndRaiseEvent() {
        AsnAggregate aggregate = createDraft();
        assertThat(aggregate.status()).isEqualTo(AsnStatus.DRAFT);
        assertThat(aggregate.lines()).hasSize(1);
        assertThat(aggregate.pullEvents()).extracting(event -> event.eventType()).containsExactly("SupplierAsnCreated");
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldSubmitThenShip}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldSubmitThenShip() {
        AsnAggregate aggregate = createDraft();
        aggregate.pullEvents();
        aggregate.submit(1001L, generator);
        aggregate.confirmShipment(new ShipmentInfo(OffsetDateTime.now(), "顺丰供应链", "SF10001"), 1001L, generator);
        assertThat(aggregate.status()).isEqualTo(AsnStatus.SHIPPED);
        assertThat(aggregate.version()).isEqualTo(2);
        assertThat(aggregate.pullEvents()).extracting(event -> event.eventType()).containsExactly("SupplierAsnSubmitted", "SupplierAsnShipped");
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldRejectShippingDraft}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldRejectShippingDraft() {
        AsnAggregate aggregate = createDraft();
        assertThatThrownBy(() -> aggregate.confirmShipment(new ShipmentInfo(OffsetDateTime.now(), "顺丰供应链", "SF10001"), 1001L, generator)).isInstanceOfSatisfying(BusinessException.class, exception -> assertThat(exception.code()).isEqualTo(ErrorCode.STATE_CONFLICT));
    }

    /**
     * 验证已提交 ASN 不能重复提交，防止重复生成仓储预约与下游协同命令。
     */
    @Test
    void shouldRejectDuplicateAsnSubmission() {
        AsnAggregate aggregate = createDraft();
        aggregate.submit(1001L, generator);

        assertThatThrownBy(() -> aggregate.submit(1001L, generator))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.code()).isEqualTo(ErrorCode.STATE_CONFLICT));
    }

    /**
     * 执行命令 {@code createDraft}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 执行命令的结果，类型为 {@code AsnAggregate}
     */
    private AsnAggregate createDraft() {
        return AsnAggregate.create(2001L, 3001L, 4001L, OffsetDateTime.now().plusDays(1), List.of(new AsnAggregate.NewLine("SKU-001", new BigDecimal("10"), "BATCH-01", null, null)), 1001L, generator);
    }

    /**
     * TestIdentifierGenerator。
     *
     * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static final class TestIdentifierGenerator implements IdentifierGenerator {

        /**
         * sequence（类型：{@code AtomicLong}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final AtomicLong sequence = new AtomicLong(1);

        /**
         * 处理当前类型职责中的操作 {@code nextId}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        @Override
        public long nextId() {
            return sequence.getAndIncrement();
        }

        /**
         * 处理当前类型职责中的操作 {@code nextBusinessNo}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param prefix 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        @Override
        public String nextBusinessNo(String prefix) {
            return prefix + sequence.getAndIncrement();
        }
    }
}
