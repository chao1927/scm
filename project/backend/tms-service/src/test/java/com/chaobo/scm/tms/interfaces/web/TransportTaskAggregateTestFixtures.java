package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.domain.TransportTaskAggregate;
import java.math.BigDecimal;
import java.util.List;

/**
 * TransportTaskAggregateTestFixtures。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
final class TransportTaskAggregateTestFixtures {

    /**
     * 创建 TransportTaskAggregateTestFixtures。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     */
    private TransportTaskAggregateTestFixtures() {
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
