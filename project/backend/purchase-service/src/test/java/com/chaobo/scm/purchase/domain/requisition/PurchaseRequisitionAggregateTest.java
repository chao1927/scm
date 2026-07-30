package com.chaobo.scm.purchase.domain.requisition;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PurchaseRequisitionAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class PurchaseRequisitionAggregateTest {

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids = new TestIdentifierGenerator();

    /**
     * 执行命令 {@code createRejectsDuplicateSkuAndDate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void createRejectsDuplicateSkuAndDate() {
        var requiredDate = LocalDate.now().plusDays(3);
        assertThatThrownBy(() -> PurchaseRequisitionAggregate.create(1001, 2001, 3001, "补货", List.of(line("SKU-01", requiredDate, "10"), line("SKU-01", requiredDate, "5")), ids)).isInstanceOf(BusinessException.class).hasMessageContaining("不能重复");
    }

    /**
     * 执行命令 {@code submitApproveAndConvertChangesStatusAndRaisesEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void submitApproveAndConvertChangesStatusAndRaisesEvents() {
        var aggregate = PurchaseRequisitionAggregate.create(1001, 2001, 3001, "安全库存补货", List.of(line("SKU-01", LocalDate.now().plusDays(3), "10")), ids);
        aggregate.pullEvents();
        aggregate.submit(ids);
        assertThat(aggregate.status()).isEqualTo(PurchaseRequisitionStatus.SUBMITTED);
        var lineId = aggregate.lines().get(0).lineId();
        aggregate.approve(Map.of(lineId, new BigDecimal("8")), ids);
        assertThat(aggregate.status()).isEqualTo(PurchaseRequisitionStatus.APPROVED);
        aggregate.convert(Map.of(lineId, new BigDecimal("3")), "RFQ", "RFQ20260711001", ids);
        assertThat(aggregate.status()).isEqualTo(PurchaseRequisitionStatus.PARTIALLY_CONVERTED);
        aggregate.convert(Map.of(lineId, new BigDecimal("5")), "RFQ", "RFQ20260711001", ids);
        assertThat(aggregate.status()).isEqualTo(PurchaseRequisitionStatus.CONVERTED);
        assertThat(aggregate.pullEvents()).extracting("eventType").contains("PurchaseRequisitionSubmitted", "PurchaseRequisitionApproved", "PurchaseRequisitionConverted");
    }

    /**
     * 处理当前类型职责中的操作 {@code convertCannotExceedApprovedQuantity}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void convertCannotExceedApprovedQuantity() {
        var aggregate = PurchaseRequisitionAggregate.create(1001, 2001, 3001, "安全库存补货", List.of(line("SKU-01", LocalDate.now().plusDays(3), "10")), ids);
        aggregate.submit(ids);
        var lineId = aggregate.lines().get(0).lineId();
        aggregate.approve(Map.of(lineId, new BigDecimal("8")), ids);
        assertThatThrownBy(() -> aggregate.convert(Map.of(lineId, new BigDecimal("9")), "PO", "PO20260711001", ids)).isInstanceOf(BusinessException.class).hasMessageContaining("不能超过");
    }

    /**
     * 处理当前类型职责中的操作 {@code line}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param requiredDate 业务时间，类型为 {@code LocalDate}
     * @param quantity 数量值，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PurchaseRequisitionLine}
     */
    private PurchaseRequisitionLine line(String skuCode, LocalDate requiredDate, String quantity) {
        return new PurchaseRequisitionLine(ids.nextId(), skuCode, new BigDecimal(quantity), BigDecimal.ZERO, BigDecimal.ZERO, "PCS", requiredDate, null);
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
        private final AtomicLong sequence = new AtomicLong(1000);

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
