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

/**
 * MdmApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class MdmApplicationService {

    /**
     * mapper（类型：{@code MdmMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final MdmMapper mapper;

    /**
     * 创建 MdmApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code MdmMapper}
     */
    public MdmApplicationService(MdmMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 执行命令 {@code createType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CreateType}
     * @return 执行命令的结果，类型为 {@code MdmMapper.TypeRow}
     */
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 执行命令 {@code enableType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code OperatorCommand}
     * @return 执行命令的结果，类型为 {@code MdmMapper.TypeRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmMapper.TypeRow enableType(String typeCode, OperatorCommand command) {
        MasterDataTypeAggregate aggregate = loadType(typeCode);
        aggregate.enable();
        MdmMapper.TypeRow row = toRow(aggregate);
        mapper.updateType(row);
        saveEvents(aggregate.pullEvents());
        log("ENABLE_TYPE", typeCode, command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 执行命令 {@code disableType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ReasonCommand}
     * @return 执行命令的结果，类型为 {@code MdmMapper.TypeRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmMapper.TypeRow disableType(String typeCode, ReasonCommand command) {
        MasterDataTypeAggregate aggregate = loadType(typeCode);
        aggregate.disable(command.reason());
        MdmMapper.TypeRow row = toRow(aggregate);
        mapper.updateType(row);
        saveEvents(aggregate.pullEvents());
        log("DISABLE_TYPE", typeCode, command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 执行命令 {@code createTemplate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CreateTemplate}
     * @return 执行命令的结果，类型为 {@code MdmMapper.TemplateRow}
     */
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 执行命令 {@code publishTemplate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param templateCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code OperatorCommand}
     * @return 执行命令的结果，类型为 {@code MdmMapper.TemplateRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmMapper.TemplateRow publishTemplate(String templateCode, OperatorCommand command) {
        FieldTemplateAggregate aggregate = loadTemplate(templateCode);
        aggregate.publish();
        MdmMapper.TemplateRow row = toRow(aggregate);
        mapper.updateTemplate(row);
        saveEvents(aggregate.pullEvents());
        log("PUBLISH_TEMPLATE", templateCode, command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 执行命令 {@code disableTemplate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param templateCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ReasonCommand}
     * @return 执行命令的结果，类型为 {@code MdmMapper.TemplateRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmMapper.TemplateRow disableTemplate(String templateCode, ReasonCommand command) {
        FieldTemplateAggregate aggregate = loadTemplate(templateCode);
        aggregate.disable(command.reason());
        MdmMapper.TemplateRow row = toRow(aggregate);
        mapper.updateTemplate(row);
        saveEvents(aggregate.pullEvents());
        log("DISABLE_TEMPLATE", templateCode, command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 执行命令 {@code createCodeRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CreateCodeRule}
     * @return 执行命令的结果，类型为 {@code MdmMapper.CodeRuleRow}
     */
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 执行命令 {@code enableCodeRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ruleCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code OperatorCommand}
     * @return 执行命令的结果，类型为 {@code MdmMapper.CodeRuleRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmMapper.CodeRuleRow enableCodeRule(String ruleCode, OperatorCommand command) {
        CodeRuleAggregate aggregate = loadCodeRule(ruleCode);
        aggregate.enable();
        MdmMapper.CodeRuleRow row = toRow(aggregate);
        mapper.updateCodeRule(row);
        saveEvents(aggregate.pullEvents());
        log("ENABLE_CODE_RULE", ruleCode, command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 执行命令 {@code disableCodeRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ruleCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ReasonCommand}
     * @return 执行命令的结果，类型为 {@code MdmMapper.CodeRuleRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmMapper.CodeRuleRow disableCodeRule(String ruleCode, ReasonCommand command) {
        CodeRuleAggregate aggregate = loadCodeRule(ruleCode);
        aggregate.disable(command.reason());
        MdmMapper.CodeRuleRow row = toRow(aggregate);
        mapper.updateCodeRule(row);
        saveEvents(aggregate.pullEvents());
        log("DISABLE_CODE_RULE", ruleCode, command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 处理当前类型职责中的操作 {@code generateCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ruleCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code OperatorCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code GeneratedCode}
     */
    @Transactional(rollbackFor = Exception.class)
    public GeneratedCode generateCode(String ruleCode, OperatorCommand command) {
        CodeRuleAggregate aggregate = loadCodeRule(ruleCode);
        String code = aggregate.generateCode();
        mapper.updateCodeRule(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("GENERATE_CODE", ruleCode, command.operatorId(), command.idempotencyKey());
        return new GeneratedCode(ruleCode, code);
    }

    /**
     * 查询并返回 {@code listTypes}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<MdmMapper.TypeRow>}
     */
    public List<MdmMapper.TypeRow> listTypes() {
        return mapper.listTypes();
    }

    /**
     * 查询并返回 {@code listTemplates}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<MdmMapper.TemplateRow>}
     */
    public List<MdmMapper.TemplateRow> listTemplates() {
        return mapper.listTemplates();
    }

    /**
     * 查询并返回 {@code listCodeRules}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<MdmMapper.CodeRuleRow>}
     */
    public List<MdmMapper.CodeRuleRow> listCodeRules() {
        return mapper.listCodeRules();
    }

    /**
     * 查询并返回 {@code listOutbox}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<MdmMapper.OutboxRow>}
     */
    public List<MdmMapper.OutboxRow> listOutbox() {
        return mapper.listOutbox();
    }

    /**
     * 查询并返回 {@code listOperationLogs}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<MdmMapper.OperationLogRow>}
     */
    public List<MdmMapper.OperationLogRow> listOperationLogs() {
        return mapper.listOperationLogs();
    }

    /**
     * 查询并返回 {@code loadType}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code MasterDataTypeAggregate}
     */
    private MasterDataTypeAggregate loadType(String typeCode) {
        MdmMapper.TypeRow row = mapper.findType(typeCode);
        if (row == null) {
            throw new IllegalArgumentException("type not found");
        }
        return MasterDataTypeAggregate.restore(row.typeCode(), row.typeName(), row.domainCode(), row.status(), row.version());
    }

    /**
     * 查询并返回 {@code loadTemplate}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param templateCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code FieldTemplateAggregate}
     */
    private FieldTemplateAggregate loadTemplate(String templateCode) {
        MdmMapper.TemplateRow row = mapper.findTemplate(templateCode);
        if (row == null) {
            throw new IllegalArgumentException("template not found");
        }
        return FieldTemplateAggregate.restore(row.templateCode(), row.typeCode(), parseFields(row.fieldPayload()), row.status(), row.version());
    }

    /**
     * 查询并返回 {@code loadCodeRule}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param ruleCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code CodeRuleAggregate}
     */
    private CodeRuleAggregate loadCodeRule(String ruleCode) {
        MdmMapper.CodeRuleRow row = mapper.findCodeRule(ruleCode);
        if (row == null) {
            throw new IllegalArgumentException("code rule not found");
        }
        return CodeRuleAggregate.restore(row.ruleCode(), row.typeCode(), row.prefix(), row.serialLength(), row.status(), row.currentSerial(), row.version());
    }

    /**
     * 执行命令 {@code saveEvents}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param events 业务处理参数或成员，类型为 {@code List<MdmEvent>}
     */
    private void saveEvents(List<MdmEvent> events) {
        for (MdmEvent event : events) {
            mapper.insertOutbox(new MdmMapper.OutboxRow(event.eventType(), event.businessNo(), event.payload(), 1, event.occurredAt()));
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code log}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param operationType 业务处理参数或成员，类型为 {@code String}
     * @param businessNo 可追踪业务编码，类型为 {@code String}
     * @param operatorId 业务或技术标识，类型为 {@code Long}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     */
    private void log(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
        mapper.insertOperationLog(new MdmMapper.OperationLogRow(operationType, businessNo, operatorId, idempotencyKey, LocalDateTime.now()));
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code MasterDataTypeAggregate}
     * @return 转换数据模型的结果，类型为 {@code MdmMapper.TypeRow}
     */
    private MdmMapper.TypeRow toRow(MasterDataTypeAggregate aggregate) {
        return new MdmMapper.TypeRow(null, aggregate.typeCode(), aggregate.typeName(), aggregate.domainCode(), aggregate.status(), aggregate.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code FieldTemplateAggregate}
     * @return 转换数据模型的结果，类型为 {@code MdmMapper.TemplateRow}
     */
    private MdmMapper.TemplateRow toRow(FieldTemplateAggregate aggregate) {
        return new MdmMapper.TemplateRow(null, aggregate.templateCode(), aggregate.typeCode(), formatFields(aggregate.fields()), aggregate.status(), aggregate.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code CodeRuleAggregate}
     * @return 转换数据模型的结果，类型为 {@code MdmMapper.CodeRuleRow}
     */
    private MdmMapper.CodeRuleRow toRow(CodeRuleAggregate aggregate) {
        return new MdmMapper.CodeRuleRow(null, aggregate.ruleCode(), aggregate.typeCode(), aggregate.prefix(), aggregate.serialLength(), aggregate.status(), aggregate.currentSerial(), aggregate.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code formatFields}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param fields 业务处理参数或成员，类型为 {@code List<FieldTemplateAggregate.FieldDefinition>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public static String formatFields(List<FieldTemplateAggregate.FieldDefinition> fields) {
        return fields.stream().map(field -> String.join(":", field.fieldCode(), field.fieldName(), field.fieldType(), Boolean.toString(field.required()), Boolean.toString(field.uniqueFlag()), Boolean.toString(field.keyField()))).collect(Collectors.joining(";"));
    }

    /**
     * 处理当前类型职责中的操作 {@code parseFields}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param payload 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<FieldTemplateAggregate.FieldDefinition>}
     */
    public static List<FieldTemplateAggregate.FieldDefinition> parseFields(String payload) {
        if (payload == null || payload.isBlank()) {
            return List.of();
        }
        return List.of(payload.split(";")).stream().map(item -> {
            String[] parts = item.split(":");
            if (parts.length != PARSE_FIELDS_VALUE_6) {
                throw new IllegalArgumentException("invalid field payload");
            }
            return new FieldTemplateAggregate.FieldDefinition(parts[0], parts[1], parts[2], Boolean.parseBoolean(parts[3]), Boolean.parseBoolean(parts[4]), Boolean.parseBoolean(parts[5]));
        }).toList();
    }

    /**
     * OperatorCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record OperatorCommand(Long operatorId, String idempotencyKey) {
    }

    /**
     * ReasonCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ReasonCommand(Long operatorId, String idempotencyKey, String reason) {
    }

    /**
     * CreateType。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateType(String typeCode, String typeName, String domainCode, Long operatorId, String idempotencyKey) {
    }

    /**
     * CreateTemplate。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateTemplate(String templateCode, String typeCode, List<FieldTemplateAggregate.FieldDefinition> fields, Long operatorId, String idempotencyKey) {
    }

    /**
     * CreateCodeRule。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateCodeRule(String ruleCode, String typeCode, String prefix, int serialLength, Long operatorId, String idempotencyKey) {
    }

    /**
     * GeneratedCode。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record GeneratedCode(String ruleCode, String generatedCode) {
    }

    /**
     * 业务常量 {@code PARSE_FIELDS_VALUE_6}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PARSE_FIELDS_VALUE_6 = 6;
}
