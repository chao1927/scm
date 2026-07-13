package com.chaobo.scm.mdm.application;

import com.chaobo.scm.mdm.domain.PublicationAggregate;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmPublicationMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MdmPublicationApplicationServiceTest {
    @Test
    void publishToActiveSubscriptionsAndConfirmReceiptIdempotently() {
        MasterDataRecordApplicationServiceTest.MemoryRecordMapper recordMapper =
                new MasterDataRecordApplicationServiceTest.MemoryRecordMapper();
        recordMapper.versions.put("MDV200001V1",
                MasterDataRecordApplicationServiceTest.version("MDV200001V1", "MDR200001", "SKU", "SKU-001"));
        MasterDataRecordApplicationService recordService = new MasterDataRecordApplicationService(
                recordMapper, new MasterDataRecordApplicationServiceTest.MemoryMdmMapper());
        MemoryPublicationMapper mapper = new MemoryPublicationMapper();
        MdmPublicationApplicationService service = new MdmPublicationApplicationService(mapper, recordService);

        service.createSubscription(new MdmPublicationApplicationService.CreateSubscriptionCommand(
                "SKU", "WMS", "mdm.sku.changed", null, 1001L, "idem-1"));
        List<MdmPublicationMapper.PublicationRow> publications = service.publish(
                new MdmPublicationApplicationService.PublishCommand("MDV200001V1", 1001L, "idem-2"));
        service.consumeReceipt(new MdmPublicationApplicationService.ReceiptEvent(
                "evt-1", "MdmPublicationReceiptReceived", publications.getFirst().publicationNo(), "SUCCESS", null, "{}"));
        service.consumeReceipt(new MdmPublicationApplicationService.ReceiptEvent(
                "evt-1", "MdmPublicationReceiptReceived", publications.getFirst().publicationNo(), "SUCCESS", null, "{}"));

        MdmPublicationMapper.PublicationRow confirmed = mapper.findPublication(publications.getFirst().publicationNo());
        assertThat(confirmed.status()).isEqualTo(PublicationAggregate.CONFIRMED);
        assertThat(mapper.inbox).hasSize(1);
        assertThat(mapper.outbox).extracting(MdmMapper.OutboxRow::eventType)
                .contains("PublicationSubscriptionCreated", "MasterDataPublished", "MasterDataPublishConfirmed");
    }

    @Test
    void failedReceiptCanBeRetried() {
        MasterDataRecordApplicationServiceTest.MemoryRecordMapper recordMapper =
                new MasterDataRecordApplicationServiceTest.MemoryRecordMapper();
        recordMapper.versions.put("MDV200001V1",
                MasterDataRecordApplicationServiceTest.version("MDV200001V1", "MDR200001", "SKU", "SKU-001"));
        MasterDataRecordApplicationService recordService = new MasterDataRecordApplicationService(
                recordMapper, new MasterDataRecordApplicationServiceTest.MemoryMdmMapper());
        MemoryPublicationMapper mapper = new MemoryPublicationMapper();
        MdmPublicationApplicationService service = new MdmPublicationApplicationService(mapper, recordService);

        service.createSubscription(new MdmPublicationApplicationService.CreateSubscriptionCommand(
                "SKU", "OMS", "mdm.sku.changed", null, 1001L, "idem-1"));
        MdmPublicationMapper.PublicationRow publication = service.publish(
                new MdmPublicationApplicationService.PublishCommand("MDV200001V1", 1001L, "idem-2")).getFirst();
        service.consumeReceipt(new MdmPublicationApplicationService.ReceiptEvent(
                "evt-2", "MdmPublicationReceiptReceived", publication.publicationNo(), "FAILED", "字段校验失败", "{}"));
        MdmPublicationMapper.PublicationRow retried = service.retry(publication.publicationNo(),
                new MdmPublicationApplicationService.RetryCommand("修正后重试", 1002L, "idem-3"));

        assertThat(retried.status()).isEqualTo(PublicationAggregate.PENDING);
        assertThat(retried.retryCount()).isEqualTo(1);
        assertThat(mapper.outbox).extracting(MdmMapper.OutboxRow::eventType)
                .contains("MasterDataRepublished");
    }

    @Test
    void retryRejectsPendingPublication() {
        MasterDataRecordApplicationServiceTest.MemoryRecordMapper recordMapper =
                new MasterDataRecordApplicationServiceTest.MemoryRecordMapper();
        recordMapper.versions.put("MDV200001V1",
                MasterDataRecordApplicationServiceTest.version("MDV200001V1", "MDR200001", "SKU", "SKU-001"));
        MasterDataRecordApplicationService recordService = new MasterDataRecordApplicationService(
                recordMapper, new MasterDataRecordApplicationServiceTest.MemoryMdmMapper());
        MemoryPublicationMapper mapper = new MemoryPublicationMapper();
        MdmPublicationApplicationService service = new MdmPublicationApplicationService(mapper, recordService);

        service.createSubscription(new MdmPublicationApplicationService.CreateSubscriptionCommand(
                "SKU", "OMS", "mdm.sku.changed", null, 1001L, "idem-1"));
        MdmPublicationMapper.PublicationRow publication = service.publish(
                new MdmPublicationApplicationService.PublishCommand("MDV200001V1", 1001L, "idem-2")).getFirst();

        assertThatThrownBy(() -> service.retry(publication.publicationNo(),
                new MdmPublicationApplicationService.RetryCommand("重试", 1002L, "idem-3")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not failed");
    }

    static class MemoryPublicationMapper implements MdmPublicationMapper {
        final Map<String, SubscriptionRow> subscriptions = new LinkedHashMap<>();
        final Map<String, PublicationRow> publications = new LinkedHashMap<>();
        final Map<String, EventInboxRow> inbox = new LinkedHashMap<>();
        final List<MdmMapper.OutboxRow> outbox = new ArrayList<>();
        final List<MdmMapper.OperationLogRow> logs = new ArrayList<>();

        @Override
        public SubscriptionRow findSubscription(String subscriptionNo) { return subscriptions.get(subscriptionNo); }

        @Override
        public SubscriptionRow findActiveSubscription(String typeCode, String targetSystem, String eventTopic) {
            return subscriptions.values().stream()
                    .filter(row -> row.typeCode().equals(typeCode) && row.targetSystem().equals(targetSystem)
                            && row.eventTopic().equals(eventTopic) && row.status() == 1)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<SubscriptionRow> listActiveSubscriptions(String typeCode) {
            return subscriptions.values().stream()
                    .filter(row -> row.typeCode().equals(typeCode) && row.status() == 1)
                    .toList();
        }

        @Override
        public List<SubscriptionRow> listSubscriptions() { return new ArrayList<>(subscriptions.values()); }

        @Override
        public void insertSubscription(SubscriptionRow row) { subscriptions.put(row.subscriptionNo(), row); }

        @Override
        public void updateSubscription(SubscriptionRow row) { subscriptions.put(row.subscriptionNo(), row); }

        @Override
        public PublicationRow findPublication(String publicationNo) { return publications.get(publicationNo); }

        @Override
        public List<PublicationRow> listPublications() { return new ArrayList<>(publications.values()); }

        @Override
        public void insertPublication(PublicationRow row) { publications.put(row.publicationNo(), row); }

        @Override
        public void updatePublication(PublicationRow row) { publications.put(row.publicationNo(), row); }

        @Override
        public int claimEvent(EventInboxRow row) {
            if (inbox.containsKey(row.eventId())) {
                return 0;
            }
            inbox.put(row.eventId(), row);
            return 1;
        }

        @Override
        public void updateEvent(EventInboxRow row) { inbox.put(row.eventId(), row); }

        @Override
        public void insertOutbox(MdmMapper.OutboxRow row) { outbox.add(row); }

        @Override
        public List<MdmMapper.OutboxRow> listOutbox() { return outbox; }

        @Override
        public void insertOperationLog(MdmMapper.OperationLogRow row) { logs.add(row); }

        @Override
        public List<MdmMapper.OperationLogRow> listOperationLogs() { return logs; }
    }
}
