package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.TransportTaskApplicationService;
import com.chaobo.scm.tms.application.TransportTaskApplicationServiceTest;
import com.chaobo.scm.tms.domain.TransportTaskAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransportTaskControllerTest {
    @Test
    void openApiCreateAndBackOfficeAcceptWorkThroughControllers() {
        TransportTaskApplicationServiceTest.MemoryTransportTaskMapper mapper =
                new TransportTaskApplicationServiceTest.MemoryTransportTaskMapper();
        TransportTaskApplicationService service = new TransportTaskApplicationService(mapper);
        TransportTaskOpenApiController openApiController = new TransportTaskOpenApiController(service);
        TransportTaskController controller = new TransportTaskController(service);

        TransportTaskMapper.TaskRow created = openApiController.create(new TransportTaskOpenApiController.CreateTransportTaskRequest(
                "OMS", "SO1", null, "SALES_OUTBOUND", 1L, 2L,
                TransportTaskAggregateTestFixtures.address(), TransportTaskAggregateTestFixtures.address(),
                TransportTaskAggregateTestFixtures.packages(), "SF-EXPRESS", "SHIPPER", 1001L, "idem-1"));
        TransportTaskMapper.TaskRow accepted = controller.accept(created.taskNo(),
                new TransportTaskController.AcceptRequest("SF", "顺丰", "SF-EXPRESS", created.version(),
                        1001L, "idem-2"));

        assertThat(accepted.status()).isEqualTo(TransportTaskAggregate.ACCEPTED);
        assertThat(controller.list("OMS", "SALES_OUTBOUND", TransportTaskAggregate.ACCEPTED, 2L,
                "SF", 1, 20)).hasSize(1);
    }
}
