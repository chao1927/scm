package com.chaobo.scm.purchase.domain.order;

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
 * PurchaseOrderAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class PurchaseOrderAggregateTest {

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids = new TestIdentifierGenerator();

    /**
     * 执行命令 {@code submitApproveAndPublishChangesStatus}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void submitApproveAndPublishChangesStatus() {
        var order = order(BigDecimal.ZERO);
        order.pullEvents();
        order.submit(ids);
        order.approve(true, null, ids);
        order.publish("EVENT", ids);
        assertThat(order.status()).isEqualTo(PurchaseOrderStatus.PENDING_SUPPLIER_CONFIRM);
        assertThat(order.releasedAt()).isNotNull();
        assertThat(order.pullEvents()).extracting("eventType").contains("PurchaseOrderSubmitted", "PurchaseOrderApproved", "PurchaseOrderPublished");
    }

    /**
     * 执行命令 {@code cancelRejectsReceivedOrder}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void cancelRejectsReceivedOrder() {
        var order = order(new BigDecimal("1"));
        assertThatThrownBy(() -> order.cancel("不采购了", ids)).isInstanceOf(BusinessException.class).hasMessageContaining("已有入库执行");
    }

    /**
     * 执行命令 {@code applyLineQtyChangeIncrementsBusinessVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void applyLineQtyChangeIncrementsBusinessVersion() {
        var order = order(BigDecimal.ZERO);
        var lineId = order.lines().get(0).lineId();
        order.applyLineQtyChanges(Map.of(lineId, new BigDecimal("20")), ids);
        assertThat(order.versionNo()).isEqualTo(2);
        assertThat(order.lines().get(0).orderQty()).isEqualByComparingTo("20");
    }

    /**
     * 处理当前类型职责中的操作 {@code supplierConfirmationMovesReleasedOrderToConfirmed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void supplierConfirmationMovesReleasedOrderToConfirmed() {
        var order = releasedOrder();
        order.recordSupplierConfirmation("按期送达", ids);
        assertThat(order.status()).isEqualTo(PurchaseOrderStatus.SUPPLIER_CONFIRMED);
        assertThat(order.pullEvents()).extracting("eventType").contains("SupplierOrderConfirmationRecorded");
    }

    /**
     * 处理当前类型职责中的操作 {@code supplierRejectionRequiresReasonAndUsesDedicatedState}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void supplierRejectionRequiresReasonAndUsesDedicatedState() {
        var order = releasedOrder();
        assertThatThrownBy(() -> order.recordSupplierRejection("", ids)).isInstanceOf(BusinessException.class).hasMessageContaining("拒绝原因不能为空");
        order.recordSupplierRejection("产能不足", ids);
        assertThat(order.status()).isEqualTo(PurchaseOrderStatus.SUPPLIER_REJECTED);
    }

    /**
     * 处理当前类型职责中的操作 {@code supplierDifferenceCanOnlyBeRecordedWhileWaitingForConfirmation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void supplierDifferenceCanOnlyBeRecordedWhileWaitingForConfirmation() {
        var order = releasedOrder();
        order.recordSupplierDifference("交期需要顺延", ids);
        assertThat(order.status()).isEqualTo(PurchaseOrderStatus.SUPPLIER_DIFF);
        assertThatThrownBy(() -> order.recordSupplierConfirmation(null, ids)).isInstanceOf(BusinessException.class).hasMessageContaining("不允许执行");
    }

    /**
     * 处理当前类型职责中的操作 {@code acceptedSupplierDifferenceReturnsOrderToConfirmed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void acceptedSupplierDifferenceReturnsOrderToConfirmed() {
        var order = releasedOrder();
        order.recordSupplierDifference("数量短缺", ids);
        order.pullEvents();
        order.acceptSupplierDifference("接受部分供货", ids);
        assertThat(order.status()).isEqualTo(PurchaseOrderStatus.SUPPLIER_CONFIRMED);
        assertThat(order.pullEvents()).extracting("eventType").contains("SupplierOrderDifferenceAccepted");
    }

    /**
     * 处理当前类型职责中的操作 {@code renegotiationReturnsDifferenceOrderToPendingConfirmation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void renegotiationReturnsDifferenceOrderToPendingConfirmation() {
        var order = releasedOrder();
        order.recordSupplierDifference("交期延后", ids);
        order.restartSupplierNegotiation("请在三天内重新确认交期", ids);
        assertThat(order.status()).isEqualTo(PurchaseOrderStatus.PENDING_SUPPLIER_CONFIRM);
    }

    /**
     * 执行命令 {@code closeRemainingRequiresAnExecutionStateAndReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void closeRemainingRequiresAnExecutionStateAndReason() {
        var draft = order(BigDecimal.ZERO);
        assertThatThrownBy(() -> draft.closeRemaining("停止采购", ids)).isInstanceOf(BusinessException.class).hasMessageContaining("不允许执行");
        var released = releasedOrder();
        released.recordSupplierConfirmation(null, ids);
        released.closeRemaining("需求取消", ids);
        assertThat(released.status()).isEqualTo(PurchaseOrderStatus.CLOSED);
        assertThat(released.pullEvents()).extracting("eventType").contains("PurchaseOrderClosed");
    }

    /**
     * 执行命令 {@code releasedOrder}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 执行命令的结果，类型为 {@code PurchaseOrderAggregate}
     */
    private PurchaseOrderAggregate releasedOrder() {
        var order = order(BigDecimal.ZERO);
        order.pullEvents();
        order.submit(ids);
        order.approve(true, null, ids);
        order.publish("EVENT", ids);
        order.pullEvents();
        return order;
    }

    /**
     * 处理当前类型职责中的操作 {@code order}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param receivedQty 数量值，类型为 {@code BigDecimal}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PurchaseOrderAggregate}
     */
    private PurchaseOrderAggregate order(BigDecimal receivedQty) {
        return PurchaseOrderAggregate.create(1, 3001, "SUP001", "测试供应商", 2001, "WH001", "CNY", List.of(new PurchaseOrderLine(ids.nextId(), "SKU-01", "测试SKU", new BigDecimal("10"), new BigDecimal("12"), new BigDecimal("0.13"), null, LocalDate.now().plusDays(7), receivedQty)), ids);
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
        private final AtomicLong sequence = new AtomicLong(5000);

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
