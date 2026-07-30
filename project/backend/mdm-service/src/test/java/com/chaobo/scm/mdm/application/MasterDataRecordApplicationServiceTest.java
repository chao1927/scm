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

/**
 * MasterDataRecordApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class MasterDataRecordApplicationServiceTest {

    /**
     * 执行命令 {@code createSubmitApproveAndListVersions}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void createSubmitApproveAndListVersions() {
        MemoryMdmMapper mdmMapper = new MemoryMdmMapper();
        mdmMapper.types.put("SKU", new MdmMapper.TypeRow(null, "SKU", "商品SKU", "PRODUCT", 2, 2));
        MemoryRecordMapper recordMapper = new MemoryRecordMapper();
        MasterDataRecordApplicationService service = new MasterDataRecordApplicationService(recordMapper, mdmMapper);
        MasterDataRecordMapper.RecordRow created = service.create(new MasterDataRecordApplicationService.CreateRecordCommand("SKU", "SKU-001", "测试商品", "{\"name\":\"测试商品\"}", 1001L, "idem-1"));
        MasterDataRecordMapper.RecordRow submitted = service.submitReview(created.recordNo(), new MasterDataRecordApplicationService.StateCommand("提交", created.version(), 1001L, "idem-2"));
        MasterDataRecordMapper.RecordRow approved = service.approve(created.recordNo(), new MasterDataRecordApplicationService.StateCommand("通过", submitted.version(), 1002L, "idem-3"));
        assertThat(approved.status()).isEqualTo(MasterDataRecordAggregate.ENABLED);
        assertThat(approved.currentVersionNo()).isEqualTo(1);
        assertThat(service.listVersions(created.recordNo())).hasSize(1);
        assertThat(recordMapper.outbox).extracting(MdmMapper.OutboxRow::eventType).contains("MasterDataDraftCreated", "MasterDataSubmitted", "MasterDataEnabled", "MasterDataVersionGenerated");
        assertThat(recordMapper.logs).extracting(MdmMapper.OperationLogRow::operationType).contains("CREATE_MASTER_DATA_RECORD", "SUBMIT_MASTER_DATA_RECORD", "APPROVE_MASTER_DATA_RECORD");
    }

    /**
     * 执行命令 {@code createRejectsUnknownTypeAndDuplicateCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void createRejectsUnknownTypeAndDuplicateCode() {
        MemoryMdmMapper mdmMapper = new MemoryMdmMapper();
        MemoryRecordMapper recordMapper = new MemoryRecordMapper();
        MasterDataRecordApplicationService service = new MasterDataRecordApplicationService(recordMapper, mdmMapper);
        assertThatThrownBy(() -> service.create(new MasterDataRecordApplicationService.CreateRecordCommand("SKU", "SKU-001", "测试商品", "{}", 1001L, "idem-1"))).isInstanceOf(IllegalStateException.class).hasMessageContaining("type does not exist");
        mdmMapper.types.put("SKU", new MdmMapper.TypeRow(null, "SKU", "商品SKU", "PRODUCT", 2, 2));
        service.create(new MasterDataRecordApplicationService.CreateRecordCommand("SKU", "SKU-001", "测试商品", "{}", 1001L, "idem-2"));
        assertThatThrownBy(() -> service.create(new MasterDataRecordApplicationService.CreateRecordCommand("SKU", "SKU-001", "重复商品", "{}", 1001L, "idem-3"))).isInstanceOf(IllegalStateException.class).hasMessageContaining("already exists");
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
            return null;
        }

        /**
         * 查询并返回 {@code listTemplates}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<TemplateRow>}
         */
        @Override
        public List<TemplateRow> listTemplates() {
            return List.of();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertTemplate}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code TemplateRow}
         */
        @Override
        public void insertTemplate(TemplateRow row) {
        }

        /**
         * 执行命令 {@code updateTemplate}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code TemplateRow}
         */
        @Override
        public void updateTemplate(TemplateRow row) {
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
            return null;
        }

        /**
         * 查询并返回 {@code listCodeRules}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<CodeRuleRow>}
         */
        @Override
        public List<CodeRuleRow> listCodeRules() {
            return List.of();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertCodeRule}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code CodeRuleRow}
         */
        @Override
        public void insertCodeRule(CodeRuleRow row) {
        }

        /**
         * 执行命令 {@code updateCodeRule}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code CodeRuleRow}
         */
        @Override
        public void updateCodeRule(CodeRuleRow row) {
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOutbox}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code OutboxRow}
         */
        @Override
        public void insertOutbox(OutboxRow row) {
        }

        /**
         * 查询并返回 {@code listOutbox}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<OutboxRow>}
         */
        @Override
        public List<OutboxRow> listOutbox() {
            return List.of();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOperationLog}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code OperationLogRow}
         */
        @Override
        public void insertOperationLog(OperationLogRow row) {
        }

        /**
         * 查询并返回 {@code listOperationLogs}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<OperationLogRow>}
         */
        @Override
        public List<OperationLogRow> listOperationLogs() {
            return List.of();
        }
    }

    /**
     * MemoryRecordMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class MemoryRecordMapper implements MasterDataRecordMapper {

        /**
         * records（类型：{@code Map<String,RecordRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, RecordRow> records = new LinkedHashMap<>();

        /**
         * versions（类型：{@code Map<String,VersionRow>}）。
         *
         * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
         */
        final Map<String, VersionRow> versions = new LinkedHashMap<>();

        /**
         * outbox（类型：{@code List<MdmMapper.OutboxRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<MdmMapper.OutboxRow> outbox = new ArrayList<>();

        /**
         * logs（类型：{@code List<MdmMapper.OperationLogRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<MdmMapper.OperationLogRow> logs = new ArrayList<>();

        /**
         * 查询并返回 {@code findRecord}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param recordNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code RecordRow}
         */
        @Override
        public RecordRow findRecord(String recordNo) {
            return records.get(recordNo);
        }

        /**
         * 查询并返回 {@code findRecordByCode}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param typeCode 可追踪业务编码，类型为 {@code String}
         * @param dataCode 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code RecordRow}
         */
        @Override
        public RecordRow findRecordByCode(String typeCode, String dataCode) {
            return records.values().stream().filter(row -> row.typeCode().equals(typeCode) && row.dataCode().equals(dataCode)).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code listRecords}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param typeCode 可追踪业务编码，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code Integer}
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @param offset 业务处理参数或成员，类型为 {@code int}
         * @return 查询并返回的结果，类型为 {@code List<RecordRow>}
         */
        @Override
        public List<RecordRow> listRecords(String typeCode, Integer status, int limit, int offset) {
            return records.values().stream().filter(row -> typeCode == null || row.typeCode().equals(typeCode)).filter(row -> status == null || row.status() == status).skip(offset).limit(limit).toList();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertRecord}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code RecordRow}
         */
        @Override
        public void insertRecord(RecordRow row) {
            records.put(row.recordNo(), row);
        }

        /**
         * 执行命令 {@code updateRecord}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code RecordRow}
         */
        @Override
        public void updateRecord(RecordRow row) {
            records.put(row.recordNo(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertVersion}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code VersionRow}
         */
        @Override
        public void insertVersion(VersionRow row) {
            versions.put(row.versionNo(), row);
        }

        /**
         * 查询并返回 {@code findVersion}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param versionNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code VersionRow}
         */
        @Override
        public VersionRow findVersion(String versionNo) {
            return versions.get(versionNo);
        }

        /**
         * 查询并返回 {@code listVersions}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param recordNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code List<VersionRow>}
         */
        @Override
        public List<VersionRow> listVersions(String recordNo) {
            return versions.values().stream().filter(row -> row.recordNo().equals(recordNo)).toList();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOutbox}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code MdmMapper.OutboxRow}
         */
        @Override
        public void insertOutbox(MdmMapper.OutboxRow row) {
            outbox.add(row);
        }

        /**
         * 查询并返回 {@code listOutbox}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<MdmMapper.OutboxRow>}
         */
        @Override
        public List<MdmMapper.OutboxRow> listOutbox() {
            return outbox;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOperationLog}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code MdmMapper.OperationLogRow}
         */
        @Override
        public void insertOperationLog(MdmMapper.OperationLogRow row) {
            logs.add(row);
        }

        /**
         * 查询并返回 {@code listOperationLogs}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<MdmMapper.OperationLogRow>}
         */
        @Override
        public List<MdmMapper.OperationLogRow> listOperationLogs() {
            return logs;
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code version}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param versionNo 可追踪业务编码，类型为 {@code String}
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param dataCode 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MasterDataRecordMapper.VersionRow}
     */
    static MasterDataRecordMapper.VersionRow version(String versionNo, String recordNo, String typeCode, String dataCode) {
        return new MasterDataRecordMapper.VersionRow(null, versionNo, recordNo, typeCode, dataCode, 1, "{\"name\":\"测试商品\"}", "通过", LocalDateTime.now());
    }
}
