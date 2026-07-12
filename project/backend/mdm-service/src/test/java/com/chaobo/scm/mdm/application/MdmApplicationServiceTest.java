package com.chaobo.scm.mdm.application;

import com.chaobo.scm.mdm.domain.FieldTemplateAggregate;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MdmApplicationServiceTest {
    @Test
    void typeTemplateAndCodeRuleFormAWorkingSlice() {
        MemoryMdmMapper mapper = new MemoryMdmMapper();
        MdmApplicationService service = new MdmApplicationService(mapper);

        service.createType(new MdmApplicationService.CreateType("SKU", "商品SKU", "PRODUCT", 1001L, "idem-1"));
        service.enableType("SKU", new MdmApplicationService.OperatorCommand(1001L, "idem-2"));

        service.createTemplate(new MdmApplicationService.CreateTemplate(
                "TPL-SKU",
                "SKU",
                List.of(
                        new FieldTemplateAggregate.FieldDefinition("skuCode", "SKU编码", "STRING", true, true, true),
                        new FieldTemplateAggregate.FieldDefinition("skuName", "SKU名称", "STRING", true, false, false)
                ),
                1001L,
                "idem-3"
        ));
        service.publishTemplate("TPL-SKU", new MdmApplicationService.OperatorCommand(1001L, "idem-4"));

        service.createCodeRule(new MdmApplicationService.CreateCodeRule("RULE-SKU", "SKU", "SKU", 4, 1001L, "idem-5"));
        service.enableCodeRule("RULE-SKU", new MdmApplicationService.OperatorCommand(1001L, "idem-6"));
        MdmApplicationService.GeneratedCode generated = service.generateCode("RULE-SKU", new MdmApplicationService.OperatorCommand(1001L, "idem-7"));

        assertThat(service.listTypes()).hasSize(1);
        assertThat(service.listTemplates().getFirst().status()).isEqualTo(FieldTemplateAggregate.PUBLISHED);
        assertThat(generated.generatedCode()).startsWith("SKU").endsWith("0001");
        assertThat(service.listOutbox()).extracting(MdmMapper.OutboxRow::eventType)
                .contains("MasterDataTypeCreated", "MasterDataTypeEnabled", "FieldTemplatePublished", "CodeRuleEnabled", "MasterDataCodeGenerated");
        assertThat(service.listOperationLogs()).extracting(MdmMapper.OperationLogRow::operationType)
                .contains("CREATE_TYPE", "PUBLISH_TEMPLATE", "GENERATE_CODE");
    }

    static class MemoryMdmMapper implements MdmMapper {
        final Map<String, TypeRow> types = new LinkedHashMap<>();
        final Map<String, TemplateRow> templates = new LinkedHashMap<>();
        final Map<String, CodeRuleRow> codeRules = new LinkedHashMap<>();
        final List<OutboxRow> outbox = new ArrayList<>();
        final List<OperationLogRow> logs = new ArrayList<>();

        @Override
        public TypeRow findType(String typeCode) { return types.get(typeCode); }

        @Override
        public List<TypeRow> listTypes() { return new ArrayList<>(types.values()); }

        @Override
        public void insertType(TypeRow row) { types.put(row.typeCode(), row); }

        @Override
        public void updateType(TypeRow row) { types.put(row.typeCode(), row); }

        @Override
        public TemplateRow findTemplate(String templateCode) { return templates.get(templateCode); }

        @Override
        public List<TemplateRow> listTemplates() { return new ArrayList<>(templates.values()); }

        @Override
        public void insertTemplate(TemplateRow row) { templates.put(row.templateCode(), row); }

        @Override
        public void updateTemplate(TemplateRow row) { templates.put(row.templateCode(), row); }

        @Override
        public CodeRuleRow findCodeRule(String ruleCode) { return codeRules.get(ruleCode); }

        @Override
        public List<CodeRuleRow> listCodeRules() { return new ArrayList<>(codeRules.values()); }

        @Override
        public void insertCodeRule(CodeRuleRow row) { codeRules.put(row.ruleCode(), row); }

        @Override
        public void updateCodeRule(CodeRuleRow row) { codeRules.put(row.ruleCode(), row); }

        @Override
        public void insertOutbox(OutboxRow row) { outbox.add(row); }

        @Override
        public List<OutboxRow> listOutbox() { return outbox; }

        @Override
        public void insertOperationLog(OperationLogRow row) { logs.add(row); }

        @Override
        public List<OperationLogRow> listOperationLogs() { return logs; }
    }
}
