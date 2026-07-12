package com.chaobo.scm.mdm.application;

import com.chaobo.scm.mdm.domain.CodeRuleAggregate;
import com.chaobo.scm.mdm.domain.FieldTemplateAggregate;
import com.chaobo.scm.mdm.domain.MasterDataTypeAggregate;
import com.chaobo.scm.mdm.domain.MdmEvent;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MdmApplicationService {
    private final MdmMapper mapper;

    public MdmApplicationService(MdmMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional
    public MdmMapper.TypeRow createType(CreateType command) {
        if (mapper.findType(command.typeCode()) != null) {
            throw new IllegalStateException("type already exists");
        }
        MasterDataTypeAggregate aggregate = MasterDataTypeAggregate.create(command.typeCode(), command.typeName(), command.domainCode());
        MdmMapper.TypeRow row = toRow(aggregate);
        mapper.insertType(row);
        saveEvents(aggregate.pullEvents());
        log("CREATE_TYPE", command.typeCode(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public MdmMapper.TypeRow enableType(String typeCode, OperatorCommand command) {
        MasterDataTypeAggregate aggregate = loadType(typeCode);
        aggregate.enable();
        MdmMapper.TypeRow row = toRow(aggregate);
        mapper.updateType(row);
        saveEvents(aggregate.pullEvents());
        log("ENABLE_TYPE", typeCode, command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public MdmMapper.TypeRow disableType(String typeCode, ReasonCommand command) {
        MasterDataTypeAggregate aggregate = loadType(typeCode);
        aggregate.disable(command.reason());
        MdmMapper.TypeRow row = toRow(aggregate);
        mapper.updateType(row);
        saveEvents(aggregate.pullEvents());
        log("DISABLE_TYPE", typeCode, command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public MdmMapper.TemplateRow createTemplate(CreateTemplate command) {
        if (mapper.findType(command.typeCode()) == null) {
            throw new IllegalStateException("type does not exist");
        }
        if (mapper.findTemplate(command.templateCode()) != null) {
            throw new IllegalStateException("template already exists");
        }
        FieldTemplateAggregate aggregate = FieldTemplateAggregate.create(command.templateCode(), command.typeCode(), command.fields());
        MdmMapper.TemplateRow row = toRow(aggregate);
        mapper.insertTemplate(row);
        saveEvents(aggregate.pullEvents());
        log("CREATE_TEMPLATE", command.templateCode(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public MdmMapper.TemplateRow publishTemplate(String templateCode, OperatorCommand command) {
        FieldTemplateAggregate aggregate = loadTemplate(templateCode);
        aggregate.publish();
        MdmMapper.TemplateRow row = toRow(aggregate);
        mapper.updateTemplate(row);
        saveEvents(aggregate.pullEvents());
        log("PUBLISH_TEMPLATE", templateCode, command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public MdmMapper.TemplateRow disableTemplate(String templateCode, ReasonCommand command) {
        FieldTemplateAggregate aggregate = loadTemplate(templateCode);
        aggregate.disable(command.reason());
        MdmMapper.TemplateRow row = toRow(aggregate);
        mapper.updateTemplate(row);
        saveEvents(aggregate.pullEvents());
        log("DISABLE_TEMPLATE", templateCode, command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public MdmMapper.CodeRuleRow createCodeRule(CreateCodeRule command) {
        if (mapper.findType(command.typeCode()) == null) {
            throw new IllegalStateException("type does not exist");
        }
        if (mapper.findCodeRule(command.ruleCode()) != null) {
            throw new IllegalStateException("code rule already exists");
        }
        CodeRuleAggregate aggregate = CodeRuleAggregate.create(command.ruleCode(), command.typeCode(), command.prefix(), command.serialLength());
        MdmMapper.CodeRuleRow row = toRow(aggregate);
        mapper.insertCodeRule(row);
        saveEvents(aggregate.pullEvents());
        log("CREATE_CODE_RULE", command.ruleCode(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public MdmMapper.CodeRuleRow enableCodeRule(String ruleCode, OperatorCommand command) {
        CodeRuleAggregate aggregate = loadCodeRule(ruleCode);
        aggregate.enable();
        MdmMapper.CodeRuleRow row = toRow(aggregate);
        mapper.updateCodeRule(row);
        saveEvents(aggregate.pullEvents());
        log("ENABLE_CODE_RULE", ruleCode, command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public MdmMapper.CodeRuleRow disableCodeRule(String ruleCode, ReasonCommand command) {
        CodeRuleAggregate aggregate = loadCodeRule(ruleCode);
        aggregate.disable(command.reason());
        MdmMapper.CodeRuleRow row = toRow(aggregate);
        mapper.updateCodeRule(row);
        saveEvents(aggregate.pullEvents());
        log("DISABLE_CODE_RULE", ruleCode, command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public GeneratedCode generateCode(String ruleCode, OperatorCommand command) {
        CodeRuleAggregate aggregate = loadCodeRule(ruleCode);
        String code = aggregate.generateCode();
        mapper.updateCodeRule(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("GENERATE_CODE", ruleCode, command.operatorId(), command.idempotencyKey());
        return new GeneratedCode(ruleCode, code);
    }

    public List<MdmMapper.TypeRow> listTypes() { return mapper.listTypes(); }
    public List<MdmMapper.TemplateRow> listTemplates() { return mapper.listTemplates(); }
    public List<MdmMapper.CodeRuleRow> listCodeRules() { return mapper.listCodeRules(); }
    public List<MdmMapper.OutboxRow> listOutbox() { return mapper.listOutbox(); }
    public List<MdmMapper.OperationLogRow> listOperationLogs() { return mapper.listOperationLogs(); }

    private MasterDataTypeAggregate loadType(String typeCode) {
        MdmMapper.TypeRow row = mapper.findType(typeCode);
        if (row == null) throw new IllegalArgumentException("type not found");
        return MasterDataTypeAggregate.restore(row.typeCode(), row.typeName(), row.domainCode(), row.status(), row.version());
    }

    private FieldTemplateAggregate loadTemplate(String templateCode) {
        MdmMapper.TemplateRow row = mapper.findTemplate(templateCode);
        if (row == null) throw new IllegalArgumentException("template not found");
        return FieldTemplateAggregate.restore(row.templateCode(), row.typeCode(), parseFields(row.fieldPayload()), row.status(), row.version());
    }

    private CodeRuleAggregate loadCodeRule(String ruleCode) {
        MdmMapper.CodeRuleRow row = mapper.findCodeRule(ruleCode);
        if (row == null) throw new IllegalArgumentException("code rule not found");
        return CodeRuleAggregate.restore(row.ruleCode(), row.typeCode(), row.prefix(), row.serialLength(), row.status(), row.currentSerial(), row.version());
    }

    private void saveEvents(List<MdmEvent> events) {
        for (MdmEvent event : events) {
            mapper.insertOutbox(new MdmMapper.OutboxRow(event.eventType(), event.businessNo(), event.payload(), 1, event.occurredAt()));
        }
    }

    private void log(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
        mapper.insertOperationLog(new MdmMapper.OperationLogRow(operationType, businessNo, operatorId, idempotencyKey, LocalDateTime.now()));
    }

    private MdmMapper.TypeRow toRow(MasterDataTypeAggregate aggregate) {
        return new MdmMapper.TypeRow(null, aggregate.typeCode(), aggregate.typeName(), aggregate.domainCode(), aggregate.status(), aggregate.version());
    }

    private MdmMapper.TemplateRow toRow(FieldTemplateAggregate aggregate) {
        return new MdmMapper.TemplateRow(null, aggregate.templateCode(), aggregate.typeCode(), formatFields(aggregate.fields()), aggregate.status(), aggregate.version());
    }

    private MdmMapper.CodeRuleRow toRow(CodeRuleAggregate aggregate) {
        return new MdmMapper.CodeRuleRow(null, aggregate.ruleCode(), aggregate.typeCode(), aggregate.prefix(), aggregate.serialLength(), aggregate.status(), aggregate.currentSerial(), aggregate.version());
    }

    public static String formatFields(List<FieldTemplateAggregate.FieldDefinition> fields) {
        return fields.stream()
                .map(field -> String.join(":", field.fieldCode(), field.fieldName(), field.fieldType(),
                        Boolean.toString(field.required()), Boolean.toString(field.uniqueFlag()), Boolean.toString(field.keyField())))
                .collect(Collectors.joining(";"));
    }

    public static List<FieldTemplateAggregate.FieldDefinition> parseFields(String payload) {
        if (payload == null || payload.isBlank()) {
            return List.of();
        }
        return List.of(payload.split(";")).stream().map(item -> {
            String[] parts = item.split(":");
            if (parts.length != 6) {
                throw new IllegalArgumentException("invalid field payload");
            }
            return new FieldTemplateAggregate.FieldDefinition(parts[0], parts[1], parts[2], Boolean.parseBoolean(parts[3]), Boolean.parseBoolean(parts[4]), Boolean.parseBoolean(parts[5]));
        }).toList();
    }

    public record OperatorCommand(Long operatorId, String idempotencyKey) {}
    public record ReasonCommand(Long operatorId, String idempotencyKey, String reason) {}
    public record CreateType(String typeCode, String typeName, String domainCode, Long operatorId, String idempotencyKey) {}
    public record CreateTemplate(String templateCode, String typeCode, List<FieldTemplateAggregate.FieldDefinition> fields, Long operatorId, String idempotencyKey) {}
    public record CreateCodeRule(String ruleCode, String typeCode, String prefix, int serialLength, Long operatorId, String idempotencyKey) {}
    public record GeneratedCode(String ruleCode, String generatedCode) {}
}
