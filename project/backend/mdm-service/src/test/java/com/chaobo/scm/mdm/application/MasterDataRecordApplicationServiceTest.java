package com.chaobo.scm.mdm.application;

import com.chaobo.scm.mdm.domain.MasterDataRecordAggregate;
import com.chaobo.scm.mdm.infrastructure.persistence.MasterDataRecordMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MasterDataRecordApplicationServiceTest {
    @Test
    void createSubmitApproveAndListVersions() {
        MemoryMdmMapper mdmMapper = new MemoryMdmMapper();
        mdmMapper.types.put("SKU", new MdmMapper.TypeRow(null, "SKU", "商品SKU", "PRODUCT", 2, 2));
        MemoryRecordMapper recordMapper = new MemoryRecordMapper();
        MasterDataRecordApplicationService service = new MasterDataRecordApplicationService(recordMapper, mdmMapper);

        MasterDataRecordMapper.RecordRow created = service.create(new MasterDataRecordApplicationService.CreateRecordCommand(
                "SKU", "SKU-001", "测试商品", "{\"name\":\"测试商品\"}", 1001L, "idem-1"));
        MasterDataRecordMapper.RecordRow submitted = service.submitReview(created.recordNo(),
                new MasterDataRecordApplicationService.StateCommand("提交", created.version(), 1001L, "idem-2"));
        MasterDataRecordMapper.RecordRow approved = service.approve(created.recordNo(),
                new MasterDataRecordApplicationService.StateCommand("通过", submitted.version(), 1002L, "idem-3"));

        assertThat(approved.status()).isEqualTo(MasterDataRecordAggregate.ENABLED);
        assertThat(approved.currentVersionNo()).isEqualTo(1);
        assertThat(service.listVersions(created.recordNo())).hasSize(1);
        assertThat(recordMapper.outbox).extracting(MdmMapper.OutboxRow::eventType)
                .contains("MasterDataDraftCreated", "MasterDataSubmitted", "MasterDataEnabled", "MasterDataVersionGenerated");
        assertThat(recordMapper.logs).extracting(MdmMapper.OperationLogRow::operationType)
                .contains("CREATE_MASTER_DATA_RECORD", "SUBMIT_MASTER_DATA_RECORD", "APPROVE_MASTER_DATA_RECORD");
    }

    @Test
    void createRejectsUnknownTypeAndDuplicateCode() {
        MemoryMdmMapper mdmMapper = new MemoryMdmMapper();
        MemoryRecordMapper recordMapper = new MemoryRecordMapper();
        MasterDataRecordApplicationService service = new MasterDataRecordApplicationService(recordMapper, mdmMapper);

        assertThatThrownBy(() -> service.create(new MasterDataRecordApplicationService.CreateRecordCommand(
                "SKU", "SKU-001", "测试商品", "{}", 1001L, "idem-1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("type does not exist");

        mdmMapper.types.put("SKU", new MdmMapper.TypeRow(null, "SKU", "商品SKU", "PRODUCT", 2, 2));
        service.create(new MasterDataRecordApplicationService.CreateRecordCommand(
                "SKU", "SKU-001", "测试商品", "{}", 1001L, "idem-2"));

        assertThatThrownBy(() -> service.create(new MasterDataRecordApplicationService.CreateRecordCommand(
                "SKU", "SKU-001", "重复商品", "{}", 1001L, "idem-3")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");
    }

    static class MemoryMdmMapper implements MdmMapper {
        final Map<String, TypeRow> types = new LinkedHashMap<>();

        @Override
        public TypeRow findType(String typeCode) { return types.get(typeCode); }

        @Override
        public List<TypeRow> listTypes() { return new ArrayList<>(types.values()); }

        @Override
        public void insertType(TypeRow row) { types.put(row.typeCode(), row); }

        @Override
        public void updateType(TypeRow row) { types.put(row.typeCode(), row); }

        @Override
        public TemplateRow findTemplate(String templateCode) { return null; }

        @Override
        public List<TemplateRow> listTemplates() { return List.of(); }

        @Override
        public void insertTemplate(TemplateRow row) {}

        @Override
        public void updateTemplate(TemplateRow row) {}

        @Override
        public CodeRuleRow findCodeRule(String ruleCode) { return null; }

        @Override
        public List<CodeRuleRow> listCodeRules() { return List.of(); }

        @Override
        public void insertCodeRule(CodeRuleRow row) {}

        @Override
        public void updateCodeRule(CodeRuleRow row) {}

        @Override
        public void insertOutbox(OutboxRow row) {}

        @Override
        public List<OutboxRow> listOutbox() { return List.of(); }

        @Override
        public void insertOperationLog(OperationLogRow row) {}

        @Override
        public List<OperationLogRow> listOperationLogs() { return List.of(); }
    }

    static class MemoryRecordMapper implements MasterDataRecordMapper {
        final Map<String, RecordRow> records = new LinkedHashMap<>();
        final Map<String, VersionRow> versions = new LinkedHashMap<>();
        final List<MdmMapper.OutboxRow> outbox = new ArrayList<>();
        final List<MdmMapper.OperationLogRow> logs = new ArrayList<>();

        @Override
        public RecordRow findRecord(String recordNo) { return records.get(recordNo); }

        @Override
        public RecordRow findRecordByCode(String typeCode, String dataCode) {
            return records.values().stream()
                    .filter(row -> row.typeCode().equals(typeCode) && row.dataCode().equals(dataCode))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<RecordRow> listRecords(String typeCode, Integer status, int limit, int offset) {
            return records.values().stream()
                    .filter(row -> typeCode == null || row.typeCode().equals(typeCode))
                    .filter(row -> status == null || row.status() == status)
                    .skip(offset)
                    .limit(limit)
                    .toList();
        }

        @Override
        public void insertRecord(RecordRow row) { records.put(row.recordNo(), row); }

        @Override
        public void updateRecord(RecordRow row) { records.put(row.recordNo(), row); }

        @Override
        public void insertVersion(VersionRow row) { versions.put(row.versionNo(), row); }

        @Override
        public VersionRow findVersion(String versionNo) { return versions.get(versionNo); }

        @Override
        public List<VersionRow> listVersions(String recordNo) {
            return versions.values().stream().filter(row -> row.recordNo().equals(recordNo)).toList();
        }

        @Override
        public void insertOutbox(MdmMapper.OutboxRow row) { outbox.add(row); }

        @Override
        public List<MdmMapper.OutboxRow> listOutbox() { return outbox; }

        @Override
        public void insertOperationLog(MdmMapper.OperationLogRow row) { logs.add(row); }

        @Override
        public List<MdmMapper.OperationLogRow> listOperationLogs() { return logs; }
    }

    static MasterDataRecordMapper.VersionRow version(String versionNo, String recordNo, String typeCode,
                                                     String dataCode) {
        return new MasterDataRecordMapper.VersionRow(null, versionNo, recordNo, typeCode, dataCode, 1,
                "{\"name\":\"测试商品\"}", "通过", LocalDateTime.now());
    }
}
