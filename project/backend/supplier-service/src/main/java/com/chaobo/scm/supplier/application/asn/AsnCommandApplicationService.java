package com.chaobo.scm.supplier.application.asn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.domain.asn.AsnAggregate;
import com.chaobo.scm.supplier.domain.asn.AsnRepository;
import com.chaobo.scm.supplier.domain.shared.DomainEvent;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import com.chaobo.scm.supplier.application.shared.AuditLogRepository;
import com.chaobo.scm.supplier.application.shared.CommandContext;
import com.chaobo.scm.supplier.application.shared.CommandResult;
import com.chaobo.scm.supplier.application.shared.OutboxRepository;
import com.chaobo.scm.supplier.application.shared.TransactionalCommandExecutor;
import com.chaobo.scm.supplier.application.integration.IntegrationCommandEnqueuer;
import com.chaobo.scm.common.integration.WmsCollaborationApi;
import com.chaobo.scm.common.integration.TmsCollaborationApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AsnCommandApplicationService {
    private final AsnRepository repository;
    private final OutboxRepository outboxRepository;
    private final AuditLogRepository auditLogRepository;
    private final TransactionalCommandExecutor commandExecutor;
    private final IdentifierGenerator identifierGenerator;
    private final IntegrationCommandEnqueuer integrations;

    public AsnCommandApplicationService(AsnRepository repository, OutboxRepository outboxRepository,
                                        AuditLogRepository auditLogRepository, TransactionalCommandExecutor commandExecutor,
                                        IdentifierGenerator identifierGenerator,
                                        IntegrationCommandEnqueuer integrations) {
        this.repository = repository;
        this.outboxRepository = outboxRepository;
        this.auditLogRepository = auditLogRepository;
        this.commandExecutor = commandExecutor;
        this.identifierGenerator = identifierGenerator;
        this.integrations = integrations;
    }

    @Transactional
    public CommandResult create(AsnCommands.Create command, CommandContext context) {
        context.requirePermission("supplier:asn:create");
        context.requireSupplierScope(command.supplierId());
        return commandExecutor.execute("supplier:asn", context, command, () -> {
            AsnAggregate aggregate = AsnAggregate.create(command.purchaseOrderId(), command.supplierId(),
                    command.warehouseId(), command.estimatedArrivalAt(), command.lines(),
                    context.operatorId(), identifierGenerator);
            return persist(aggregate, context, "CREATE_ASN", null);
        });
    }

    @Transactional
    public CommandResult submit(AsnCommands.Submit command, CommandContext context) {
        context.requirePermission("supplier:asn:submit");
        return change(command.asnId(), command.version(), command, context, "SUBMIT_ASN",
                aggregate -> aggregate.submit(context.operatorId(), identifierGenerator));
    }

    @Transactional
    public CommandResult cancel(AsnCommands.Cancel command, CommandContext context) {
        context.requirePermission("supplier:asn:cancel");
        return change(command.asnId(), command.version(), command, context, "CANCEL_ASN",
                aggregate -> aggregate.cancel(command.reason(), context.operatorId(), identifierGenerator));
    }

    @Transactional
    public CommandResult confirmShipment(AsnCommands.ConfirmShipment command, CommandContext context) {
        context.requirePermission("supplier:asn:ship");
        return change(command.asnId(), command.version(), command, context, "SHIP_ASN",
                aggregate -> aggregate.confirmShipment(command.shipmentInfo(), context.operatorId(), identifierGenerator));
    }

    private CommandResult change(long asnId, int expectedVersion, Object command, CommandContext context,
                                 String operation, java.util.function.Consumer<AsnAggregate> action) {
        return commandExecutor.execute("supplier:asn", context, command, () -> {
            AsnAggregate aggregate = repository.findById(asnId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "ASN 不存在"));
            context.requireSupplierScope(aggregate.supplierId());
            if (aggregate.version() != expectedVersion) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "ASN 已被其他操作更新，请刷新后重试");
            }
            String before = snapshot(aggregate);
            action.accept(aggregate);
            return persist(aggregate, context, operation, before);
        });
    }

    private CommandResult persist(AsnAggregate aggregate, CommandContext context,
                                  String operation, String beforeSnapshot) {
        repository.save(aggregate, context.operatorId());
        List<DomainEvent> events = aggregate.pullEvents();
        outboxRepository.saveAll(events);
        auditLogRepository.save(context, operation, "ASN", aggregate.asnId(), aggregate.asnNo(),
                beforeSnapshot, snapshot(aggregate));
        if ("SUBMIT_ASN".equals(operation)) {
            var lines = aggregate.lines().stream().map(line -> new WmsCollaborationApi.Line(
                    line.lineId(), line.skuCode(), line.batchNo(), line.plannedQuantity())).toList();
            integrations.enqueue("WMS_CREATE_APPOINTMENT", "ASN", aggregate.asnId(), aggregate.version(), "WMS",
                    new WmsCollaborationApi.InboundAppointmentCommand(
                            "ASN-APPOINT-" + aggregate.asnId() + "-" + aggregate.version(),
                            aggregate.asnId(), aggregate.asnNo(), aggregate.supplierId(), aggregate.warehouseId(),
                            aggregate.estimatedArrivalAt(), lines));
        } else if ("SHIP_ASN".equals(operation)) {
            var shipment = aggregate.shipmentInfo();
            integrations.enqueue("TMS_CREATE_INBOUND_TRANSPORT", "ASN", aggregate.asnId(), aggregate.version(), "TMS",
                    new TmsCollaborationApi.InboundTransportCommand(
                            "ASN-TRANSPORT-" + aggregate.asnId() + "-" + aggregate.version(),
                            aggregate.asnId(), aggregate.asnNo(), aggregate.supplierId(), aggregate.warehouseId(),
                            shipment.shippedAt(), shipment.carrierName(), shipment.trackingNo()));
        } else if ("CANCEL_ASN".equals(operation)) {
            integrations.enqueue("WMS_CANCEL_APPOINTMENT", "ASN", aggregate.asnId(), aggregate.version(), "WMS",
                    new WmsCollaborationApi.CancelAppointmentCommand(
                            "ASN-APPOINT-CANCEL-" + aggregate.asnId() + "-" + aggregate.version(),
                            aggregate.asnId(), aggregate.cancelReason()));
            if (aggregate.shipmentInfo() != null) {
                integrations.enqueue("TMS_CANCEL_TRANSPORT", "ASN", aggregate.asnId(), aggregate.version(), "TMS",
                        new TmsCollaborationApi.CancelTransportCommand(
                                "ASN-TRANSPORT-CANCEL-" + aggregate.asnId() + "-" + aggregate.version(),
                                "ASN", aggregate.asnId(), aggregate.cancelReason()));
            }
        }
        String eventCode = events.isEmpty() ? null : events.get(events.size() - 1).eventCode();
        return new CommandResult(aggregate.asnId(), aggregate.asnNo(), aggregate.status().code(),
                aggregate.status().label(), aggregate.version(), eventCode, false);
    }

    private String snapshot(AsnAggregate aggregate) {
        return "{\"asnNo\":\"%s\",\"status\":\"%s\",\"version\":%d}"
                .formatted(aggregate.asnNo(), aggregate.status().name(), aggregate.version());
    }

}
