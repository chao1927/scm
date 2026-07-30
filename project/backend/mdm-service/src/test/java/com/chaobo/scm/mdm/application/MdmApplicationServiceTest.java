package com.chaobo.scm.mdm.application;

import com.chaobo.scm.mdm.domain.FieldTemplateAggregate;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmMapper;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * MdmApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class MdmApplicationServiceTest {

    /**
     * 验证类型、模板和编码规则能够组成可运行的主数据切片。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldFormWorkingSliceForTypeTemplateAndCodeRule() {
        MemoryMdmMapper mapper = new MemoryMdmMapper();
        MdmApplicationService service = new MdmApplicationService(mapper);
        service.createType(new MdmApplicationService.CreateType("SKU", "商品SKU", "PRODUCT", 1001L, "idem-1"));
        service.enableType("SKU", new MdmApplicationService.OperatorCommand(1001L, "idem-2"));
        service.createTemplate(new MdmApplicationService.CreateTemplate("TPL-SKU", "SKU", List.of(new FieldTemplateAggregate.FieldDefinition("skuCode", "SKU编码", "STRING", true, true, true), new FieldTemplateAggregate.FieldDefinition("skuName", "SKU名称", "STRING", true, false, false)), 1001L, "idem-3"));
        service.publishTemplate("TPL-SKU", new MdmApplicationService.OperatorCommand(1001L, "idem-4"));
        service.createCodeRule(new MdmApplicationService.CreateCodeRule("RULE-SKU", "SKU", "SKU", 4, 1001L, "idem-5"));
        service.enableCodeRule("RULE-SKU", new MdmApplicationService.OperatorCommand(1001L, "idem-6"));
        MdmApplicationService.GeneratedCode generated = service.generateCode("RULE-SKU", new MdmApplicationService.OperatorCommand(1001L, "idem-7"));
        assertThat(service.listTypes()).hasSize(1);
        assertThat(service.listTemplates().get(0).status()).isEqualTo(FieldTemplateAggregate.PUBLISHED);
        assertThat(generated.generatedCode()).startsWith("SKU").endsWith("0001");
        assertThat(service.listOutbox()).extracting(MdmMapper.OutboxRow::eventType).contains("MasterDataTypeCreated", "MasterDataTypeEnabled", "FieldTemplatePublished", "CodeRuleEnabled", "MasterDataCodeGenerated");
        assertThat(service.listOperationLogs()).extracting(MdmMapper.OperationLogRow::operationType).contains("CREATE_TYPE", "PUBLISH_TEMPLATE", "GENERATE_CODE");
    }

    /**
     * MemoryMdmMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class MemoryMdmMapper implements MdmMapper {

        /**
         * types（类型：{@code Map<String,TypeRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, TypeRow> types = new LinkedHashMap<>();

        /**
         * templates（类型：{@code Map<String,TemplateRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, TemplateRow> templates = new LinkedHashMap<>();

        /**
         * codeRules（类型：{@code Map<String,CodeRuleRow>}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        final Map<String, CodeRuleRow> codeRules = new LinkedHashMap<>();

        /**
         * outbox（类型：{@code List<OutboxRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<OutboxRow> outbox = new ArrayList<>();

        /**
         * logs（类型：{@code List<OperationLogRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<OperationLogRow> logs = new ArrayList<>();

        /**
         * 查询并返回 {@code findType}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param typeCode 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code TypeRow}
         */
        @Override
        public TypeRow findType(String typeCode) {
            return types.get(typeCode);
        }

        /**
         * 查询并返回 {@code listTypes}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<TypeRow>}
         */
        @Override
        public List<TypeRow> listTypes() {
            return new ArrayList<>(types.values());
        }

        /**
         * 处理当前类型职责中的操作 {@code insertType}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code TypeRow}
         */
        @Override
        public void insertType(TypeRow row) {
            types.put(row.typeCode(), row);
        }

        /**
         * 执行命令 {@code updateType}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code TypeRow}
         */
        @Override
        public void updateType(TypeRow row) {
            types.put(row.typeCode(), row);
        }

        /**
         * 查询并返回 {@code findTemplate}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param templateCode 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code TemplateRow}
         */
        @Override
        public TemplateRow findTemplate(String templateCode) {
            return templates.get(templateCode);
        }

        /**
         * 查询并返回 {@code listTemplates}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<TemplateRow>}
         */
        @Override
        public List<TemplateRow> listTemplates() {
            return new ArrayList<>(templates.values());
        }

        /**
         * 处理当前类型职责中的操作 {@code insertTemplate}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code TemplateRow}
         */
        @Override
        public void insertTemplate(TemplateRow row) {
            templates.put(row.templateCode(), row);
        }

        /**
         * 执行命令 {@code updateTemplate}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code TemplateRow}
         */
        @Override
        public void updateTemplate(TemplateRow row) {
            templates.put(row.templateCode(), row);
        }

        /**
         * 查询并返回 {@code findCodeRule}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param ruleCode 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code CodeRuleRow}
         */
        @Override
        public CodeRuleRow findCodeRule(String ruleCode) {
            return codeRules.get(ruleCode);
        }

        /**
         * 查询并返回 {@code listCodeRules}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<CodeRuleRow>}
         */
        @Override
        public List<CodeRuleRow> listCodeRules() {
            return new ArrayList<>(codeRules.values());
        }

        /**
         * 处理当前类型职责中的操作 {@code insertCodeRule}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code CodeRuleRow}
         */
        @Override
        public void insertCodeRule(CodeRuleRow row) {
            codeRules.put(row.ruleCode(), row);
        }

        /**
         * 执行命令 {@code updateCodeRule}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code CodeRuleRow}
         */
        @Override
        public void updateCodeRule(CodeRuleRow row) {
            codeRules.put(row.ruleCode(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOutbox}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code OutboxRow}
         */
        @Override
        public void insertOutbox(OutboxRow row) {
            outbox.add(row);
        }

        /**
         * 查询并返回 {@code listOutbox}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<OutboxRow>}
         */
        @Override
        public List<OutboxRow> listOutbox() {
            return outbox;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOperationLog}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code OperationLogRow}
         */
        @Override
        public void insertOperationLog(OperationLogRow row) {
            logs.add(row);
        }

        /**
         * 查询并返回 {@code listOperationLogs}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<OperationLogRow>}
         */
        @Override
        public List<OperationLogRow> listOperationLogs() {
            return logs;
        }
    }
}
