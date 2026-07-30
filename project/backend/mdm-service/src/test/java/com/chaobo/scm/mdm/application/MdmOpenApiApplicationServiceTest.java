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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * MdmOpenApiApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class MdmOpenApiApplicationServiceTest {

    /**
     * 校验业务约束 {@code validatesEnabledMasterDataAndReportsFailures}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void validatesEnabledMasterDataAndReportsFailures() {
        MasterDataRecordApplicationServiceTest.MemoryRecordMapper recordMapper = new MasterDataRecordApplicationServiceTest.MemoryRecordMapper();
        recordMapper.records.put("MDR200001", new MasterDataRecordMapper.RecordRow(null, "MDR200001", "SKU", "SKU-001", "测试商品", "{}", MasterDataRecordAggregate.ENABLED, 1, null, 3));
        MdmOpenApiApplicationService service = new MdmOpenApiApplicationService(recordMapper, new MemoryOpenApiMapper(), null, null);
        MdmOpenApiApplicationService.ValidateResponse response = service.validate(new MdmOpenApiApplicationService.ValidateRequest("PURCHASE_ORDER", List.of(new MdmOpenApiApplicationService.ValidateItem("line-1", "SKU", "SKU-001", 1, null), new MdmOpenApiApplicationService.ValidateItem("line-2", "SKU", "SKU-404", null, null))));
        assertThat(response.valid()).isFalse();
        assertThat(response.items()).extracting(MdmOpenApiApplicationService.ValidateItemResult::failureCode).contains(null, "NOT_FOUND");
    }

    /**
     * 处理当前类型职责中的操作 {@code internalEventRaisesQualityIssueAndIsIdempotent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void internalEventRaisesQualityIssueAndIsIdempotent() {
        MasterDataRecordApplicationServiceTest.MemoryMdmMapper mdmMapper = new MasterDataRecordApplicationServiceTest.MemoryMdmMapper();
        MdmImportQualityApplicationServiceTest.MemoryImportQualityMapper qualityMapper = new MdmImportQualityApplicationServiceTest.MemoryImportQualityMapper();
        MdmImportQualityApplicationService qualityService = new MdmImportQualityApplicationService(qualityMapper, mdmMapper);
        MemoryOpenApiMapper openApiMapper = new MemoryOpenApiMapper();
        MdmOpenApiApplicationService service = new MdmOpenApiApplicationService(new MasterDataRecordApplicationServiceTest.MemoryRecordMapper(), openApiMapper, null, qualityService);
        MdmOpenApiApplicationService.EventEnvelope event = new MdmOpenApiApplicationService.EventEnvelope("evt-1", "SupplierProfileChangeSubmitted", "SUPPLIER", "SUP-001", "idem-1", "{\"supplierId\":1}", null, null, "资料变更待治理", "SUPPLIER", "SUP-001");
        MdmOpenApiApplicationService.ConsumeResult first = service.consumeEvent(event);
        MdmOpenApiApplicationService.ConsumeResult duplicate = service.consumeEvent(event);
        assertThat(first.consumeStatus()).isEqualTo("SUCCESS");
        assertThat(duplicate.idempotentHit()).isTrue();
        assertThat(qualityMapper.issues).hasSize(1);
        assertThat(openApiMapper.inbox.get("evt-1").status()).isEqualTo(2);
    }

    @Test
    void applicationScopeAndFieldAllowlistAreEnforced() throws Exception {
        MasterDataRecordApplicationServiceTest.MemoryRecordMapper recordMapper =
                new MasterDataRecordApplicationServiceTest.MemoryRecordMapper();
        recordMapper.records.put("MDR200001", new MasterDataRecordMapper.RecordRow(null, "MDR200001",
                "SKU", "CN-SKU-001", "测试商品", "{\"name\":\"A\",\"cost\":99}",
                MasterDataRecordAggregate.ENABLED, 1, null, 3));
        MemoryOpenApiMapper mapper = new MemoryOpenApiMapper();
        mapper.clients.put("oms", new MdmOpenApiMapper.OpenApiClientRow(
                "oms", "secret", "SKU", "CN-", "SKU:*", true));
        MdmOpenApiApplicationService service = new MdmOpenApiApplicationService(recordMapper, mapper, null, null);
        long timestamp = Instant.now().getEpochSecond();

        String emptyHash = java.util.HexFormat.of().formatHex(
                java.security.MessageDigest.getInstance("SHA-256").digest(new byte[0]));
        var access = service.authenticate("oms", timestamp,
                signature("secret", "oms\n" + timestamp + "\nlegacy\n" + emptyHash));
        var unrestricted = service.snapshot(access, "SKU", "CN-SKU-001", false);
        mapper.clients.put("oms", new MdmOpenApiMapper.OpenApiClientRow(
                "oms", "secret", "SKU", "CN-", "SKU:name", true));
        var restrictedAccess = service.authenticate("oms", timestamp,
                signature("secret", "oms\n" + timestamp + "\nlegacy\n" + emptyHash));
        var snapshot = service.snapshot(restrictedAccess, "SKU", "CN-SKU-001", false);

        assertThat(unrestricted.dataPayload()).contains("cost");
        assertThat(snapshot.dataPayload()).isEqualTo("{\"name\":\"A\"}");
        assertThat(mapper.snapshots).containsKey("SKU:CN-SKU-001");
    }

    @Test
    void batchQueryDoesNotExposeDisabledMasterData() {
        MasterDataRecordApplicationServiceTest.MemoryRecordMapper recordMapper =
                new MasterDataRecordApplicationServiceTest.MemoryRecordMapper();
        recordMapper.records.put("MDR200002", new MasterDataRecordMapper.RecordRow(null, "MDR200002",
                "SKU", "SKU-DISABLED", "停用商品", "{}", MasterDataRecordAggregate.DISABLED,
                1, null, 2));
        MdmOpenApiApplicationService service = new MdmOpenApiApplicationService(
                recordMapper, new MemoryOpenApiMapper(), null, null);

        assertThatThrownBy(() -> service.query(new MdmOpenApiApplicationService.QueryRequest(
                List.of(new MdmOpenApiApplicationService.QueryItem("SKU", "SKU-DISABLED")))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not enabled");
    }

    /**
     * MemoryOpenApiMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class MemoryOpenApiMapper implements MdmOpenApiMapper {

        /**
         * inbox（类型：{@code Map<String,MdmPublicationMapper.EventInboxRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, MdmPublicationMapper.EventInboxRow> inbox = new LinkedHashMap<>();

        /**
         * outbox（类型：{@code List<MdmMapper.OutboxRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<MdmMapper.OutboxRow> outbox = new ArrayList<>();
        final Map<String, MdmOpenApiMapper.OpenApiClientRow> clients = new LinkedHashMap<>();
        final Map<String, MdmOpenApiMapper.OpenApiSnapshotRow> snapshots = new LinkedHashMap<>();

        @Override
        public MdmOpenApiMapper.OpenApiClientRow findClient(String appCode) {
            return clients.get(appCode);
        }

        @Override
        public MdmOpenApiMapper.OpenApiSnapshotRow findSnapshot(String typeCode, String dataCode) {
            return snapshots.get(typeCode + ':' + dataCode);
        }

        @Override
        public void upsertSnapshot(MdmOpenApiMapper.OpenApiSnapshotRow row) {
            snapshots.put(row.typeCode() + ':' + row.dataCode(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code claimEvent}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code MdmPublicationMapper.EventInboxRow}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        @Override
        public int claimEvent(MdmPublicationMapper.EventInboxRow row) {
            if (inbox.containsKey(row.eventId())) {
                return 0;
            }
            inbox.put(row.eventId(), row);
            return 1;
        }

        /**
         * 执行命令 {@code updateEvent}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code MdmPublicationMapper.EventInboxRow}
         */
        @Override
        public void updateEvent(MdmPublicationMapper.EventInboxRow row) {
            inbox.put(row.eventId(), row);
        }

        /**
         * 查询并返回 {@code listInboxEvents}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<MdmPublicationMapper.EventInboxRow>}
         */
        @Override
        public List<MdmPublicationMapper.EventInboxRow> listInboxEvents() {
            return new ArrayList<>(inbox.values());
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
    }

    private String signature(String secret, String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return java.util.HexFormat.of().formatHex(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }
}
