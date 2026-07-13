package com.chaobo.scm.mdm.application;

import com.chaobo.scm.mdm.domain.MdmEvent;
import com.chaobo.scm.mdm.domain.PublicationAggregate;
import com.chaobo.scm.mdm.domain.PublicationSubscriptionAggregate;
import com.chaobo.scm.mdm.infrastructure.persistence.MasterDataRecordMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmPublicationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MdmPublicationApplicationService {
    private final MdmPublicationMapper mapper;
    private final MasterDataRecordApplicationService recordService;
    private final AtomicLong subscriptionSequence = new AtomicLong(300000);
    private final AtomicLong publicationSequence = new AtomicLong(400000);

    public MdmPublicationApplicationService(MdmPublicationMapper mapper,
                                            MasterDataRecordApplicationService recordService) {
        this.mapper = mapper;
        this.recordService = recordService;
    }

    @Transactional
    public MdmPublicationMapper.SubscriptionRow createSubscription(CreateSubscriptionCommand command) {
        MdmPublicationMapper.SubscriptionRow existing = mapper.findActiveSubscription(command.typeCode(),
                command.targetSystem(), command.eventTopic());
        if (existing != null) {
            return existing;
        }
        PublicationSubscriptionAggregate aggregate = PublicationSubscriptionAggregate.create(
                "SUB" + subscriptionSequence.incrementAndGet(), command.typeCode(), command.targetSystem(),
                command.eventTopic(), command.filterRule());
        MdmPublicationMapper.SubscriptionRow row = toRow(aggregate);
        mapper.insertSubscription(row);
        saveEvents(aggregate.pullEvents());
        log("CREATE_PUBLICATION_SUBSCRIPTION", row.subscriptionNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public MdmPublicationMapper.SubscriptionRow disableSubscription(String subscriptionNo, DisableSubscriptionCommand command) {
        PublicationSubscriptionAggregate aggregate = loadSubscription(subscriptionNo);
        aggregate.disable(command.reason(), command.expectedVersion());
        mapper.updateSubscription(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("DISABLE_PUBLICATION_SUBSCRIPTION", subscriptionNo, command.operatorId(), command.idempotencyKey());
        return mapper.findSubscription(subscriptionNo);
    }

    @Transactional
    public List<MdmPublicationMapper.PublicationRow> publish(PublishCommand command) {
        MasterDataRecordMapper.VersionRow version = recordService.getVersion(command.versionNo());
        if (version == null) {
            throw new IllegalArgumentException("master data version not found");
        }
        List<MdmPublicationMapper.SubscriptionRow> subscriptions = mapper.listActiveSubscriptions(version.typeCode());
        List<MdmPublicationMapper.PublicationRow> rows = new ArrayList<>();
        for (MdmPublicationMapper.SubscriptionRow subscription : subscriptions) {
            PublicationAggregate aggregate = PublicationAggregate.create("PUB" + publicationSequence.incrementAndGet(),
                    version.versionNo(), version.typeCode(), version.dataCode(), subscription.targetSystem(),
                    subscription.eventTopic());
            MdmPublicationMapper.PublicationRow row = toRow(aggregate);
            mapper.insertPublication(row);
            saveEvents(aggregate.pullEvents());
            rows.add(row);
        }
        log("PUBLISH_MASTER_DATA_VERSION", command.versionNo(), command.operatorId(), command.idempotencyKey());
        return rows;
    }

    @Transactional
    public MdmPublicationMapper.PublicationRow retry(String publicationNo, RetryCommand command) {
        PublicationAggregate aggregate = loadPublication(publicationNo);
        aggregate.retry(command.reason());
        mapper.updatePublication(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("RETRY_MASTER_DATA_PUBLICATION", publicationNo, command.operatorId(), command.idempotencyKey());
        return mapper.findPublication(publicationNo);
    }

    @Transactional
    public void consumeReceipt(ReceiptEvent event) {
        int claimed = mapper.claimEvent(new MdmPublicationMapper.EventInboxRow(event.eventId(), event.eventType(),
                event.publicationNo(), event.payload(), 1, null));
        if (claimed == 0) {
            return;
        }
        try {
            PublicationAggregate aggregate = loadPublication(event.publicationNo());
            if ("SUCCESS".equals(event.receiptStatus())) {
                aggregate.confirm();
            } else {
                aggregate.fail(event.failureReason());
            }
            mapper.updatePublication(toRow(aggregate));
            saveEvents(aggregate.pullEvents());
            mapper.updateEvent(new MdmPublicationMapper.EventInboxRow(event.eventId(), event.eventType(),
                    event.publicationNo(), event.payload(), 2, null));
        } catch (RuntimeException exception) {
            mapper.updateEvent(new MdmPublicationMapper.EventInboxRow(event.eventId(), event.eventType(),
                    event.publicationNo(), event.payload(), 3, exception.getMessage()));
            throw exception;
        }
    }

    public List<MdmPublicationMapper.SubscriptionRow> listSubscriptions() {
        return mapper.listSubscriptions();
    }

    public List<MdmPublicationMapper.PublicationRow> listPublications() {
        return mapper.listPublications();
    }

    public List<MdmMapper.OutboxRow> listOutbox() {
        return mapper.listOutbox();
    }

    private PublicationSubscriptionAggregate loadSubscription(String subscriptionNo) {
        MdmPublicationMapper.SubscriptionRow row = mapper.findSubscription(subscriptionNo);
        if (row == null) {
            throw new IllegalArgumentException("publication subscription not found");
        }
        return PublicationSubscriptionAggregate.restore(row.subscriptionNo(), row.typeCode(), row.targetSystem(),
                row.eventTopic(), row.filterRule(), row.status(), row.version());
    }

    private PublicationAggregate loadPublication(String publicationNo) {
        MdmPublicationMapper.PublicationRow row = mapper.findPublication(publicationNo);
        if (row == null) {
            throw new IllegalArgumentException("publication not found");
        }
        return PublicationAggregate.restore(row.publicationNo(), row.versionNo(), row.typeCode(), row.dataCode(),
                row.targetSystem(), row.eventTopic(), row.status(), row.retryCount(), row.failureReason(),
                row.version());
    }

    private MdmPublicationMapper.SubscriptionRow toRow(PublicationSubscriptionAggregate aggregate) {
        return new MdmPublicationMapper.SubscriptionRow(null, aggregate.subscriptionNo(), aggregate.typeCode(),
                aggregate.targetSystem(), aggregate.eventTopic(), aggregate.filterRule(), aggregate.status(),
                aggregate.version());
    }

    private MdmPublicationMapper.PublicationRow toRow(PublicationAggregate aggregate) {
        return new MdmPublicationMapper.PublicationRow(null, aggregate.publicationNo(), aggregate.versionNo(),
                aggregate.typeCode(), aggregate.dataCode(), aggregate.targetSystem(), aggregate.eventTopic(),
                aggregate.status(), aggregate.retryCount(), aggregate.failureReason(), aggregate.version());
    }

    private void saveEvents(List<MdmEvent> events) {
        for (MdmEvent event : events) {
            mapper.insertOutbox(new MdmMapper.OutboxRow(event.eventType(), event.businessNo(), event.payload(), 1,
                    event.occurredAt()));
        }
    }

    private void log(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
        mapper.insertOperationLog(new MdmMapper.OperationLogRow(operationType, businessNo, operatorId,
                idempotencyKey, LocalDateTime.now()));
    }

    public record CreateSubscriptionCommand(String typeCode, String targetSystem, String eventTopic,
                                            String filterRule, Long operatorId, String idempotencyKey) {}

    public record DisableSubscriptionCommand(String reason, long expectedVersion, Long operatorId,
                                             String idempotencyKey) {}

    public record PublishCommand(String versionNo, Long operatorId, String idempotencyKey) {}
    public record RetryCommand(String reason, Long operatorId, String idempotencyKey) {}
    public record ReceiptEvent(String eventId, String eventType, String publicationNo, String receiptStatus,
                               String failureReason, String payload) {}
}
