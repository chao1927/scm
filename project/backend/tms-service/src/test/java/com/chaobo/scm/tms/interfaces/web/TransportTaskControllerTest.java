package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.TransportTaskApplicationService;
import com.chaobo.scm.tms.application.TransportTaskApplicationServiceTest;
import com.chaobo.scm.tms.domain.TransportTaskAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import com.chaobo.scm.common.security.ScmAccessContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * TransportTaskControllerTest。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class TransportTaskControllerTest {

    /**
     * 处理当前类型职责中的操作 {@code openApiCreateAndBackOfficeAcceptWorkThroughControllers}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void openApiCreateAndBackOfficeAcceptWorkThroughControllers() {
        TransportTaskApplicationServiceTest.MemoryTransportTaskMapper mapper = new TransportTaskApplicationServiceTest.MemoryTransportTaskMapper();
        TransportTaskApplicationService service = new TransportTaskApplicationService(mapper);
        TransportTaskOpenApiController openApiController = new TransportTaskOpenApiController(service);
        TransportTaskController controller = new TransportTaskController(service);
        var oms = UsernamePasswordAuthenticationToken.authenticated("oms-service", "n/a", java.util.List.of());
        oms.setDetails(new ScmAccessContext(1, "oms-service", "OMS", java.util.Set.of("tms:task:create"), java.util.Map.of()));
        TransportTaskMapper.TaskRow created = openApiController.create(new TransportTaskOpenApiController.CreateTransportTaskRequest("OMS", "SO1", null, "SALES_OUTBOUND", 1L, 2L, TransportTaskAggregateTestFixtures.address(), TransportTaskAggregateTestFixtures.address(), TransportTaskAggregateTestFixtures.packages(), "SF-EXPRESS", "SHIPPER", 1001L, "idem-1"), oms);
        TransportTaskMapper.TaskRow accepted = controller.accept(created.taskNo(), new TransportTaskController.AcceptRequest("SF", "顺丰", "SF-EXPRESS", created.version(), 1001L, "idem-2"));
        assertThat(accepted.status()).isEqualTo(TransportTaskAggregate.ACCEPTED);
        assertThat(controller.list("OMS", "SALES_OUTBOUND", TransportTaskAggregate.ACCEPTED, 2L, "SF", 1, 20)).hasSize(1);
    }
}
