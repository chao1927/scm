package com.chaobo.scm.purchase.domain.rfq;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RfqAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class RfqAggregateTest {

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids = new TestIdentifierGenerator();

    /**
     * 执行命令 {@code createRejectsDuplicateSuppliers}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void createRejectsDuplicateSuppliers() {
        assertThatThrownBy(() -> RfqAggregate.create(2, 2001, "CATE-01", "PR001", OffsetDateTime.now().plusDays(3), List.of(line("SKU-01")), List.of(invitation(3001), invitation(3001)), ids)).isInstanceOf(BusinessException.class).hasMessageContaining("邀请供应商不能重复");
    }

    /**
     * 执行命令 {@code publishRaisesEventForEachSupplierAndLocksStatus}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void publishRaisesEventForEachSupplierAndLocksStatus() {
        var aggregate = rfq();
        aggregate.pullEvents();
        aggregate.publish(ids);
        assertThat(aggregate.status()).isEqualTo(RfqStatus.QUOTING);
        assertThat(aggregate.publishedAt()).isNotNull();
        assertThat(aggregate.pullEvents()).extracting("eventType").containsExactly("RfqPublished", "RfqPublished");
    }

    /**
     * 执行命令 {@code closeBiddingClosesSupplierTodos}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void closeBiddingClosesSupplierTodos() {
        var aggregate = rfq();
        aggregate.publish(ids);
        aggregate.pullEvents();
        aggregate.closeBidding("到达报价截止时间", ids);
        assertThat(aggregate.status()).isEqualTo(RfqStatus.BIDDING_CLOSED);
        assertThat(aggregate.invitations()).allMatch(invitation -> invitation.quoteStatus() == 4);
        assertThat(aggregate.pullEvents()).extracting("eventType").containsExactly("RfqBiddingClosed");
    }

    /**
     * 处理当前类型职责中的操作 {@code cannotPublishTwice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void cannotPublishTwice() {
        var aggregate = rfq();
        aggregate.publish(ids);
        assertThatThrownBy(() -> aggregate.publish(ids)).isInstanceOf(BusinessException.class).hasMessageContaining("不允许");
    }

    /**
     * 处理当前类型职责中的操作 {@code rfq}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code RfqAggregate}
     */
    private RfqAggregate rfq() {
        return RfqAggregate.create(2, 2001, "CATE-01", "PR001", OffsetDateTime.now().plusDays(3), List.of(line("SKU-01")), List.of(invitation(3001), invitation(3002)), ids);
    }

    /**
     * 处理当前类型职责中的操作 {@code line}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code RfqLine}
     */
    private RfqLine line(String skuCode) {
        return new RfqLine(ids.nextId(), skuCode, new BigDecimal("10"), "PCS", LocalDate.now().plusDays(7), "常规质检");
    }

    /**
     * 处理当前类型职责中的操作 {@code invitation}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code RfqInvitation}
     */
    private RfqInvitation invitation(long supplierId) {
        return new RfqInvitation(ids.nextId(), supplierId, 1);
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
        private final AtomicLong sequence = new AtomicLong(2000);

        /**
         * 处理当前类型职责中的操作 {@code nextId}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        @Override
        public long nextId() {
            return sequence.incrementAndGet();
        }

        /**
         * 处理当前类型职责中的操作 {@code nextCode}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param prefix 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        @Override
        public String nextCode(String prefix) {
            return prefix + sequence.incrementAndGet();
        }
    }
}
