package com.chaobo.scm.tms.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransportTaskAggregateTest {
    @Test
    void createAndAcceptTransportTask() {
        TransportTaskAggregate task = createTask();

        task.accept("SF", "顺丰", "SF-EXPRESS", 1);

        assertThat(task.status()).isEqualTo(TransportTaskAggregate.ACCEPTED);
        assertThat(task.version()).isEqualTo(2);
        assertThat(task.pullEvents()).extracting(TmsEvent::eventType)
                .containsExactly("TransportTaskCreated", "TransportTaskAccepted");
    }

    @Test
    void rejectDuplicateAcceptByVersion() {
        TransportTaskAggregate task = createTask();

        assertThatThrownBy(() -> task.accept("SF", "顺丰", "SF-EXPRESS", 9))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("version conflict");
    }

    @Test
    void rejectUnsupportedScenario() {
        assertThatThrownBy(() -> TransportTaskAggregate.create("TMS1", "OMS", "SO1", null,
                "UNKNOWN", 1L, 2L, address(), address(), packages(), "SF-EXPRESS", "SHIPPER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unsupported transport scenario");
    }

    private static TransportTaskAggregate createTask() {
        return TransportTaskAggregate.create("TMS1", "OMS", "SO1", null, "SALES_OUTBOUND",
                1L, 2L, address(), address(), packages(), "SF-EXPRESS", "SHIPPER");
    }

    static TransportTaskAggregate.Address address() {
        return new TransportTaskAggregate.Address("浙江省", "杭州市", "西湖区", "文一西路1号",
                "张三", "13800000000");
    }

    static List<TransportTaskAggregate.PackageItem> packages() {
        return List.of(new TransportTaskAggregate.PackageItem("PKG1", BigDecimal.ONE,
                new BigDecimal("1.20"), new BigDecimal("0.03")));
    }
}
