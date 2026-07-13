package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmOpenApiApplicationService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MdmOpenApiControllerTest {
    @Test
    void delegatesOpenApiQueryAndInternalEvents() {
        StubOpenApiService service = new StubOpenApiService();
        MdmMasterDataOpenApiController openApiController = new MdmMasterDataOpenApiController(service);
        MdmInternalEventController eventController = new MdmInternalEventController(service);
        MdmOpenApiApplicationService.QueryRequest query =
                new MdmOpenApiApplicationService.QueryRequest(List.of(new MdmOpenApiApplicationService.QueryItem("SKU", "SKU-001")));
        MdmOpenApiApplicationService.EventEnvelope event = new MdmOpenApiApplicationService.EventEnvelope(
                "evt-1", "PermissionDataScopeChanged", "IAM", "scope-1", "idem-1", "{}", null, null,
                null, null, null);

        assertThat(openApiController.query(query).items()).hasSize(1);
        assertThat(eventController.consume(event).consumeStatus()).isEqualTo("SUCCESS");
        assertThat(service.lastQueryRequest).isEqualTo(query);
        assertThat(service.lastEvent).isEqualTo(event);
    }

    static class StubOpenApiService extends MdmOpenApiApplicationService {
        MdmOpenApiApplicationService.QueryRequest lastQueryRequest;
        MdmOpenApiApplicationService.EventEnvelope lastEvent;

        StubOpenApiService() {
            super(null, null, null, null);
        }

        @Override
        public MdmOpenApiApplicationService.QueryResponse query(MdmOpenApiApplicationService.QueryRequest request) {
            lastQueryRequest = request;
            return new MdmOpenApiApplicationService.QueryResponse(List.of(new MdmOpenApiApplicationService.Snapshot(
                    "MDR200001", "SKU", "SKU-001", "测试商品", "{}", 3, 1, 3)));
        }

        @Override
        public MdmOpenApiApplicationService.ConsumeResult consumeEvent(MdmOpenApiApplicationService.EventEnvelope event) {
            lastEvent = event;
            return new MdmOpenApiApplicationService.ConsumeResult(event.eventId(), "SUCCESS", false, "consumed");
        }
    }
}
