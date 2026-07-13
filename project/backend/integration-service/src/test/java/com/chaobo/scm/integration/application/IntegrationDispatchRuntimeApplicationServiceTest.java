package com.chaobo.scm.integration.application;

import com.chaobo.scm.integration.domain.IntegrationMessageAggregate;
import com.chaobo.scm.integration.infrastructure.persistence.IntegrationMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationDispatchRuntimeApplicationServiceTest {
    @Test
    void dispatchesDueMessagesAndRecordsAttempts() {
        IntegrationApplicationServiceTest.MemoryIntegrationMapper mapper =
                new IntegrationApplicationServiceTest.MemoryIntegrationMapper();
        IntegrationApplicationService integrationService = new IntegrationApplicationService(mapper);
        IntegrationDispatchRuntimeApplicationService runtimeService =
                new IntegrationDispatchRuntimeApplicationService(mapper, integrationService,
                        request -> IntegrationTransportPort.DeliveryResult.success(7));
        integrationService.createRoute(new IntegrationApplicationService.CreateRouteCommand(
                "WmsPutawayCompleted", "WMS", "INVENTORY", "LOCAL_ACK", 1001L, "route-1"));
        IntegrationMapper.MessageRow message = integrationService.acceptEvent(
                new IntegrationApplicationService.AcceptMessageCommand("WmsPutawayCompleted", "WMS",
                        "PUT-1", "event-1", "{}", 1001L)).getFirst();

        IntegrationDispatchRuntimeApplicationService.DispatchRunResult result =
                runtimeService.dispatchDueMessages(
                        new IntegrationDispatchRuntimeApplicationService.DispatchRunCommand(20, 1001L, "run-1"));

        assertThat(result.scannedCount()).isEqualTo(1);
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(mapper.findMessage(message.messageNo()).status())
                .isEqualTo(IntegrationMessageAggregate.DISPATCHED);
        assertThat(runtimeService.listDeliveryAttempts(message.messageNo())).hasSize(1);
        assertThat(runtimeService.dispatchSummary().dispatchedCount()).isEqualTo(1);
    }

    @Test
    void repeatedDispatchFailuresMoveMessageToDeadLetter() {
        IntegrationApplicationServiceTest.MemoryIntegrationMapper mapper =
                new IntegrationApplicationServiceTest.MemoryIntegrationMapper();
        IntegrationApplicationService integrationService = new IntegrationApplicationService(mapper);
        IntegrationDispatchRuntimeApplicationService runtimeService =
                new IntegrationDispatchRuntimeApplicationService(mapper, integrationService,
                        request -> IntegrationTransportPort.DeliveryResult.failed("target timeout", 11));
        integrationService.createRoute(new IntegrationApplicationService.CreateRouteCommand(
                "TransportSigned", "TMS", "BMS", "LOCAL_FAIL", 1001L, "route-1"));
        IntegrationMapper.MessageRow message = integrationService.acceptEvent(
                new IntegrationApplicationService.AcceptMessageCommand("TransportSigned", "TMS",
                        "WB-1", "event-1", "{}", 1001L)).getFirst();

        runtimeService.dispatchDueMessages(new IntegrationDispatchRuntimeApplicationService.DispatchRunCommand(
                20, 1001L, "run-1"));
        runtimeService.dispatchDueMessages(new IntegrationDispatchRuntimeApplicationService.DispatchRunCommand(
                20, 1001L, "run-2"));
        IntegrationDispatchRuntimeApplicationService.DispatchRunResult third =
                runtimeService.dispatchDueMessages(new IntegrationDispatchRuntimeApplicationService.DispatchRunCommand(
                        20, 1001L, "run-3"));

        assertThat(third.deadLetterCount()).isEqualTo(1);
        assertThat(mapper.findMessage(message.messageNo()).status())
                .isEqualTo(IntegrationMessageAggregate.DEAD_LETTER);
        assertThat(runtimeService.listDeliveryAttempts(message.messageNo()))
                .extracting(IntegrationMapper.DeliveryAttemptRow::success)
                .containsExactly(false, false, false);
        assertThat(integrationService.listDeadLetters()).hasSize(1);
    }

    @Test
    void missingRouteIsRecordedAsDeliveryFailure() {
        IntegrationApplicationServiceTest.MemoryIntegrationMapper mapper =
                new IntegrationApplicationServiceTest.MemoryIntegrationMapper();
        IntegrationApplicationService integrationService = new IntegrationApplicationService(mapper);
        IntegrationDispatchRuntimeApplicationService runtimeService =
                new IntegrationDispatchRuntimeApplicationService(mapper, integrationService,
                        request -> IntegrationTransportPort.DeliveryResult.success(1));
        mapper.insertMessage(new IntegrationMapper.MessageRow(null, "IM1", "UnknownEvent", "OMS",
                "WMS", "SO-1", "event-1", "{}", IntegrationMessageAggregate.PENDING, 0, null, 1));

        IntegrationDispatchRuntimeApplicationService.DispatchRunResult result =
                runtimeService.dispatchDueMessages(new IntegrationDispatchRuntimeApplicationService.DispatchRunCommand(
                        20, 1001L, "run-1"));

        assertThat(result.failedCount()).isEqualTo(1);
        List<IntegrationMapper.DeliveryAttemptRow> attempts = runtimeService.listDeliveryAttempts("IM1");
        assertThat(attempts.getFirst().channelType()).isEqualTo("UNROUTED");
        assertThat(attempts.getFirst().failureReason()).isEqualTo("enabled route not found");
    }
}
