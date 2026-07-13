package com.chaobo.scm.mdm.application;

import com.chaobo.scm.mdm.domain.MasterDataRecordAggregate;
import com.chaobo.scm.mdm.infrastructure.persistence.MasterDataRecordMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmOpenApiMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmPublicationMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MdmOpenApiApplicationServiceTest {
    @Test
    void validatesEnabledMasterDataAndReportsFailures() {
        MasterDataRecordApplicationServiceTest.MemoryRecordMapper recordMapper =
                new MasterDataRecordApplicationServiceTest.MemoryRecordMapper();
        recordMapper.records.put("MDR200001", new MasterDataRecordMapper.RecordRow(null, "MDR200001", "SKU",
                "SKU-001", "测试商品", "{}", MasterDataRecordAggregate.ENABLED, 1, null, 3));
        MdmOpenApiApplicationService service = new MdmOpenApiApplicationService(recordMapper,
                new MemoryOpenApiMapper(), null, null);

        MdmOpenApiApplicationService.ValidateResponse response = service.validate(
                new MdmOpenApiApplicationService.ValidateRequest("PURCHASE_ORDER", List.of(
                        new MdmOpenApiApplicationService.ValidateItem("line-1", "SKU", "SKU-001", 1, null),
                        new MdmOpenApiApplicationService.ValidateItem("line-2", "SKU", "SKU-404", null, null)
                )));

        assertThat(response.valid()).isFalse();
        assertThat(response.items()).extracting(MdmOpenApiApplicationService.ValidateItemResult::failureCode)
                .contains(null, "NOT_FOUND");
    }

    @Test
    void internalEventRaisesQualityIssueAndIsIdempotent() {
        MasterDataRecordApplicationServiceTest.MemoryMdmMapper mdmMapper =
                new MasterDataRecordApplicationServiceTest.MemoryMdmMapper();
        MdmImportQualityApplicationServiceTest.MemoryImportQualityMapper qualityMapper =
                new MdmImportQualityApplicationServiceTest.MemoryImportQualityMapper();
        MdmImportQualityApplicationService qualityService = new MdmImportQualityApplicationService(qualityMapper, mdmMapper);
        MemoryOpenApiMapper openApiMapper = new MemoryOpenApiMapper();
        MdmOpenApiApplicationService service = new MdmOpenApiApplicationService(
                new MasterDataRecordApplicationServiceTest.MemoryRecordMapper(), openApiMapper, null, qualityService);
        MdmOpenApiApplicationService.EventEnvelope event = new MdmOpenApiApplicationService.EventEnvelope(
                "evt-1", "SupplierProfileChangeSubmitted", "SUPPLIER", "SUP-001", "idem-1",
                "{\"supplierId\":1}", null, null, "资料变更待治理", "SUPPLIER", "SUP-001");

        MdmOpenApiApplicationService.ConsumeResult first = service.consumeEvent(event);
        MdmOpenApiApplicationService.ConsumeResult duplicate = service.consumeEvent(event);

        assertThat(first.consumeStatus()).isEqualTo("SUCCESS");
        assertThat(duplicate.idempotentHit()).isTrue();
        assertThat(qualityMapper.issues).hasSize(1);
        assertThat(openApiMapper.inbox.get("evt-1").status()).isEqualTo(2);
    }

    static class MemoryOpenApiMapper implements MdmOpenApiMapper {
        final Map<String, MdmPublicationMapper.EventInboxRow> inbox = new LinkedHashMap<>();
        final List<MdmMapper.OutboxRow> outbox = new ArrayList<>();

        @Override
        public int claimEvent(MdmPublicationMapper.EventInboxRow row) {
            if (inbox.containsKey(row.eventId())) {
                return 0;
            }
            inbox.put(row.eventId(), row);
            return 1;
        }

        @Override
        public void updateEvent(MdmPublicationMapper.EventInboxRow row) { inbox.put(row.eventId(), row); }

        @Override
        public List<MdmPublicationMapper.EventInboxRow> listInboxEvents() {
            return new ArrayList<>(inbox.values());
        }

        @Override
        public void insertOutbox(MdmMapper.OutboxRow row) { outbox.add(row); }

        @Override
        public List<MdmMapper.OutboxRow> listOutbox() { return outbox; }
    }
}
