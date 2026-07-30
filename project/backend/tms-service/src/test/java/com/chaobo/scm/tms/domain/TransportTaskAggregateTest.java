package com.chaobo.scm.tms.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TransportTaskAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class TransportTaskAggregateTest {

    /**
     * 执行命令 {@code createAndAcceptTransportTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void createAndAcceptTransportTask() {
        TransportTaskAggregate task = createTask();
        task.accept("SF", "顺丰", "SF-EXPRESS", 1);
        assertThat(task.status()).isEqualTo(TransportTaskAggregate.ACCEPTED);
        assertThat(task.version()).isEqualTo(2);
        assertThat(task.pullEvents()).extracting(TmsEvent::eventType).containsExactly("TransportTaskCreated", "TransportTaskAccepted");
    }

    /**
     * 处理当前类型职责中的操作 {@code transferTaskEmitsTransferFactsWhenStartedAndDelivered}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void transferTaskEmitsTransferFactsWhenStartedAndDelivered() {
        var task = TransportTaskAggregate.create("TMS2", "INVENTORY", "TRF-1", null, "TRANSFER", 1L, 2L, address(), address(), packages(), "TRANSFER", "OWNER");
        task.pullEvents();
        task.accept("SELF", "自营", "TRANSFER", 1);
        task.pullEvents();
        task.start(2);
        task.deliver(3);
        assertThat(task.status()).isEqualTo(TransportTaskAggregate.DELIVERED);
        assertThat(task.pullEvents()).extracting(TmsEvent::eventType).containsExactly("TransferInTransit", "TransferDelivered");
    }

    /**
     * 执行命令 {@code rejectDuplicateAcceptByVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectDuplicateAcceptByVersion() {
        TransportTaskAggregate task = createTask();
        assertThatThrownBy(() -> task.accept("SF", "顺丰", "SF-EXPRESS", 9)).isInstanceOf(IllegalStateException.class).hasMessageContaining("version conflict");
    }

    /**
     * 执行命令 {@code rejectUnsupportedScenario}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectUnsupportedScenario() {
        assertThatThrownBy(() -> TransportTaskAggregate.create("TMS1", "OMS", "SO1", null, "UNKNOWN", 1L, 2L, address(), address(), packages(), "SF-EXPRESS", "SHIPPER")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("unsupported transport scenario");
    }

    /**
     * 执行命令 {@code createTask}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 执行命令的结果，类型为 {@code TransportTaskAggregate}
     */
    private static TransportTaskAggregate createTask() {
        return TransportTaskAggregate.create("TMS1", "OMS", "SO1", null, "SALES_OUTBOUND", 1L, 2L, address(), address(), packages(), "SF-EXPRESS", "SHIPPER");
    }

    /**
     * 处理当前类型职责中的操作 {@code address}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TransportTaskAggregate.Address}
     */
    static TransportTaskAggregate.Address address() {
        return new TransportTaskAggregate.Address("浙江省", "杭州市", "西湖区", "文一西路1号", "张三", "13800000000");
    }

    /**
     * 处理当前类型职责中的操作 {@code packages}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<TransportTaskAggregate.PackageItem>}
     */
    static List<TransportTaskAggregate.PackageItem> packages() {
        return List.of(new TransportTaskAggregate.PackageItem("PKG1", BigDecimal.ONE, new BigDecimal("1.20"), new BigDecimal("0.03")));
    }
}
