package com.chaobo.scm.iam.interfaces.web;

import com.chaobo.scm.iam.application.IamAdminApplicationService;
import com.chaobo.scm.iam.infrastructure.persistence.IamAdminMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IamAdminControllerTest {
    @Test
    void delegatesAppAndEventCommands() {
        StubAdminService service = new StubAdminService();
        IamAdminController adminController = new IamAdminController(service);
        IamInternalEventController eventController = new IamInternalEventController(service);
        IamAdminApplicationService.CreateAppCommand createApp =
                new IamAdminApplicationService.CreateAppCommand("OMS", "订单系统", "/oms", 1001L, "idem-1");
        IamAdminApplicationService.EventEnvelope event =
                new IamAdminApplicationService.EventEnvelope("evt-1", "PermissionResourceScanned", "GATEWAY",
                        "API-1", "{}");

        IamAdminMapper.AppRow app = adminController.createApp(createApp);
        IamAdminApplicationService.ConsumeResult result = eventController.consume(event);

        assertThat(app.appCode()).isEqualTo("OMS");
        assertThat(result.consumeStatus()).isEqualTo("SUCCESS");
        assertThat(service.lastCreateAppCommand).isEqualTo(createApp);
        assertThat(service.lastEvent).isEqualTo(event);
    }

    static class StubAdminService extends IamAdminApplicationService {
        IamAdminApplicationService.CreateAppCommand lastCreateAppCommand;
        IamAdminApplicationService.EventEnvelope lastEvent;

        StubAdminService() {
            super(null);
        }

        @Override
        public IamAdminMapper.AppRow createApp(IamAdminApplicationService.CreateAppCommand command) {
            lastCreateAppCommand = command;
            return new IamAdminMapper.AppRow(null, command.appCode(), command.appName(), command.homeUrl(), 1, 1);
        }

        @Override
        public List<IamAdminMapper.AppRow> listApps() {
            return List.of();
        }

        @Override
        public IamAdminApplicationService.ConsumeResult consumeEvent(IamAdminApplicationService.EventEnvelope event) {
            lastEvent = event;
            return new IamAdminApplicationService.ConsumeResult(event.eventId(), "SUCCESS", false, "consumed");
        }
    }
}
