package com.chaobo.scm.mdm.application;

import com.chaobo.scm.mdm.domain.MasterDataRecordAggregate;
import com.chaobo.scm.mdm.infrastructure.persistence.MasterDataRecordMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmOpenApiMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmPublicationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MdmOpenApiApplicationService {
    private final MasterDataRecordMapper recordMapper;
    private final MdmOpenApiMapper mapper;
    private final MdmPublicationApplicationService publicationService;
    private final MdmImportQualityApplicationService qualityService;

    public MdmOpenApiApplicationService(MasterDataRecordMapper recordMapper, MdmOpenApiMapper mapper,
                                        MdmPublicationApplicationService publicationService,
                                        MdmImportQualityApplicationService qualityService) {
        this.recordMapper = recordMapper;
        this.mapper = mapper;
        this.publicationService = publicationService;
        this.qualityService = qualityService;
    }

    public QueryResponse query(QueryRequest request) {
        List<Snapshot> snapshots = request.items().stream()
                .map(item -> snapshot(item.typeCode(), item.dataCode(), true))
                .toList();
        return new QueryResponse(snapshots);
    }

    public ValidateResponse validate(ValidateRequest request) {
        List<ValidateItemResult> results = request.items().stream().map(this::validateOne).toList();
        boolean valid = results.stream().allMatch(ValidateItemResult::valid);
        return new ValidateResponse(valid, results);
    }

    public Snapshot snapshot(String typeCode, String dataCode, boolean includeDisabled) {
        MasterDataRecordMapper.RecordRow row = recordMapper.findRecordByCode(typeCode, dataCode);
        if (row == null) {
            throw new IllegalArgumentException("master data snapshot not found");
        }
        if (!includeDisabled && row.status() != MasterDataRecordAggregate.ENABLED) {
            throw new IllegalStateException("master data is not enabled");
        }
        return new Snapshot(row.recordNo(), row.typeCode(), row.dataCode(), row.dataName(), row.dataPayload(),
                row.status(), row.currentVersionNo(), row.version());
    }

    @Transactional
    public ConsumeResult consumeEvent(EventEnvelope event) {
        if ("PublishReceiptReturned".equals(event.eventType())) {
            publicationService.consumeReceipt(new MdmPublicationApplicationService.ReceiptEvent(
                    event.eventId(), event.eventType(), event.publicationNo(), event.receiptStatus(),
                    event.failureReason(), event.payload()));
            return new ConsumeResult(event.eventId(), "SUCCESS", false, "receipt consumed");
        }
        int claimed = mapper.claimEvent(new MdmPublicationMapper.EventInboxRow(event.eventId(), event.eventType(),
                event.businessKey(), event.payload(), 1, null));
        if (claimed == 0) {
            return new ConsumeResult(event.eventId(), "DUPLICATE", true, "idempotent hit");
        }
        try {
            if ("SupplierProfileChangeSubmitted".equals(event.eventType())
                    || "CarrierServiceConfirmed".equals(event.eventType())) {
                qualityService.raiseQualityIssue(new MdmImportQualityApplicationService.RaiseQualityIssueCommand(
                        event.typeCode(), event.dataCode(), event.eventType(), event.failureReason() == null
                        ? event.payload() : event.failureReason(), null, event.idempotencyKey()));
            } else if (!supportedIgnored(event.eventType())) {
                mapper.updateEvent(new MdmPublicationMapper.EventInboxRow(event.eventId(), event.eventType(),
                        event.businessKey(), event.payload(), 4, "unsupported event type"));
                return new ConsumeResult(event.eventId(), "IGNORED", false, "unsupported event type");
            }
            mapper.insertOutbox(new MdmMapper.OutboxRow("MdmExternalEventConsumed", event.businessKey(),
                    event.eventType(), 1, LocalDateTime.now()));
            mapper.updateEvent(new MdmPublicationMapper.EventInboxRow(event.eventId(), event.eventType(),
                    event.businessKey(), event.payload(), 2, null));
            return new ConsumeResult(event.eventId(), "SUCCESS", false, "consumed");
        } catch (RuntimeException exception) {
            mapper.updateEvent(new MdmPublicationMapper.EventInboxRow(event.eventId(), event.eventType(),
                    event.businessKey(), event.payload(), 3, exception.getMessage()));
            throw exception;
        }
    }

    public List<MdmPublicationMapper.EventInboxRow> listInboxEvents() {
        return mapper.listInboxEvents();
    }

    private ValidateItemResult validateOne(ValidateItem item) {
        MasterDataRecordMapper.RecordRow row = recordMapper.findRecordByCode(item.typeCode(), item.dataCode());
        if (row == null) {
            return ValidateItemResult.failed(item, "NOT_FOUND", "master data does not exist", null);
        }
        if (item.expectedVersionNo() != null && row.currentVersionNo() != item.expectedVersionNo()) {
            return ValidateItemResult.failed(item, "VERSION_MISMATCH", "master data version mismatch",
                    toSnapshot(row));
        }
        int requiredStatus = item.requiredStatus() == null ? MasterDataRecordAggregate.ENABLED
                : item.requiredStatus();
        if (row.status() != requiredStatus) {
            return ValidateItemResult.failed(item, "STATUS_MISMATCH", "master data status mismatch",
                    toSnapshot(row));
        }
        return new ValidateItemResult(item.businessKey(), item.typeCode(), item.dataCode(), true, null, null,
                toSnapshot(row));
    }

    private Snapshot toSnapshot(MasterDataRecordMapper.RecordRow row) {
        return new Snapshot(row.recordNo(), row.typeCode(), row.dataCode(), row.dataName(), row.dataPayload(),
                row.status(), row.currentVersionNo(), row.version());
    }

    private static boolean supportedIgnored(String eventType) {
        return "ApprovalApproved".equals(eventType)
                || "ApprovalRejected".equals(eventType)
                || "PermissionDataScopeChanged".equals(eventType)
                || "WarehouseExternalCodeBound".equals(eventType)
                || "BillingMasterDataReferenced".equals(eventType);
    }

    public record QueryRequest(List<QueryItem> items) {
        public QueryRequest {
            items = items == null ? List.of() : List.copyOf(items);
        }
    }

    public record QueryItem(String typeCode, String dataCode) {}
    public record QueryResponse(List<Snapshot> items) {}

    public record ValidateRequest(String validateScene, List<ValidateItem> items) {
        public ValidateRequest {
            items = items == null ? List.of() : List.copyOf(items);
        }
    }

    public record ValidateItem(String businessKey, String typeCode, String dataCode, Integer expectedVersionNo,
                               Integer requiredStatus) {}

    public record ValidateResponse(boolean valid, List<ValidateItemResult> items) {}

    public record ValidateItemResult(String businessKey, String typeCode, String dataCode, boolean valid,
                                     String failureCode, String failureReason, Snapshot snapshot) {
        static ValidateItemResult failed(ValidateItem item, String failureCode, String failureReason,
                                         Snapshot snapshot) {
            return new ValidateItemResult(item.businessKey(), item.typeCode(), item.dataCode(), false,
                    failureCode, failureReason, snapshot);
        }
    }

    public record Snapshot(String recordNo, String typeCode, String dataCode, String dataName, String dataPayload,
                           int status, int currentVersionNo, long version) {}

    public record EventEnvelope(String eventId, String eventType, String sourceSystem, String businessKey,
                                String idempotencyKey, String payload, String publicationNo, String receiptStatus,
                                String failureReason, String typeCode, String dataCode) {}

    public record ConsumeResult(String consumeId, String consumeStatus, boolean idempotentHit, String message) {}
}
