package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmPublicationApplicationService;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmPublicationMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MdmPublicationControllerTest {
    @Test
    void delegatesPublicationCommandsToApplicationService() {
        StubPublicationService service = new StubPublicationService();
        MdmPublicationController controller = new MdmPublicationController(service);
        MdmPublicationApplicationService.CreateSubscriptionCommand command =
                new MdmPublicationApplicationService.CreateSubscriptionCommand("SKU", "WMS", "mdm.sku.changed", null, 1001L, "idem-1");

        MdmPublicationMapper.SubscriptionRow created = controller.createSubscription(command);

        assertThat(created.subscriptionNo()).isEqualTo("SUB300001");
        assertThat(service.lastCreateSubscriptionCommand).isEqualTo(command);
    }

    @Test
    void openApiReceiptEndpointUsesInboxConsumer() {
        StubPublicationService service = new StubPublicationService();
        MdmPublicationReceiptOpenApiController controller = new MdmPublicationReceiptOpenApiController(service);
        MdmPublicationApplicationService.ReceiptEvent event =
                new MdmPublicationApplicationService.ReceiptEvent("evt-1", "MdmPublicationReceiptReceived",
                        "PUB400001", "SUCCESS", null, "{}");

        controller.receipt(event);

        assertThat(service.lastReceiptEvent).isEqualTo(event);
    }

    @Test
    void mapsPublishAndListEndpoints() {
        StubPublicationService service = new StubPublicationService();
        MdmPublicationController controller = new MdmPublicationController(service);
        MdmPublicationApplicationService.PublishCommand command =
                new MdmPublicationApplicationService.PublishCommand("MDV200001V1", 1001L, "idem-1");

        assertThat(controller.publish(command)).isEmpty();
        assertThat(controller.publications()).isEmpty();
        assertThat(service.lastPublishCommand).isEqualTo(command);
    }

    static class StubPublicationService extends MdmPublicationApplicationService {
        MdmPublicationApplicationService.CreateSubscriptionCommand lastCreateSubscriptionCommand;
        MdmPublicationApplicationService.PublishCommand lastPublishCommand;
        MdmPublicationApplicationService.ReceiptEvent lastReceiptEvent;

        StubPublicationService() {
            super(null, null);
        }

        @Override
        public MdmPublicationMapper.SubscriptionRow createSubscription(MdmPublicationApplicationService.CreateSubscriptionCommand command) {
            lastCreateSubscriptionCommand = command;
            return new MdmPublicationMapper.SubscriptionRow(null, "SUB300001", "SKU", "WMS",
                    "mdm.sku.changed", null, 1, 1);
        }

        @Override
        public List<MdmPublicationMapper.PublicationRow> publish(MdmPublicationApplicationService.PublishCommand command) {
            lastPublishCommand = command;
            return List.of();
        }

        @Override
        public List<MdmPublicationMapper.PublicationRow> listPublications() {
            return List.of();
        }

        @Override
        public void consumeReceipt(MdmPublicationApplicationService.ReceiptEvent event) {
            lastReceiptEvent = event;
        }
    }
}
