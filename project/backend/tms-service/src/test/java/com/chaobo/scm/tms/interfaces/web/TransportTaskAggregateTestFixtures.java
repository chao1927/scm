package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.domain.TransportTaskAggregate;

import java.math.BigDecimal;
import java.util.List;

final class TransportTaskAggregateTestFixtures {
    private TransportTaskAggregateTestFixtures() {
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
