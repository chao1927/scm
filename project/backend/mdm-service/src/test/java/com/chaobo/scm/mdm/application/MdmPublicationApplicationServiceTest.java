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

/**
 * MdmPublicationApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class MdmPublicationApplicationServiceTest {

    /**
     * 执行命令 {@code publishToActiveSubscriptionsAndConfirmReceiptIdempotently}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void publishToActiveSubscriptionsAndConfirmReceiptIdempotently() {
        MasterDataRecordApplicationServiceTest.MemoryRecordMapper recordMapper = new MasterDataRecordApplicationServiceTest.MemoryRecordMapper();
        recordMapper.versions.put("MDV200001V1", MasterDataRecordApplicationServiceTest.version("MDV200001V1", "MDR200001", "SKU", "SKU-001"));
        MasterDataRecordApplicationService recordService = new MasterDataRecordApplicationService(recordMapper, new MasterDataRecordApplicationServiceTest.MemoryMdmMapper());
        MemoryPublicationMapper mapper = new MemoryPublicationMapper();
        MdmPublicationApplicationService service = new MdmPublicationApplicationService(mapper, recordService);
        service.createSubscription(new MdmPublicationApplicationService.CreateSubscriptionCommand("SKU", "WMS", "mdm.sku.changed", null, 1001L, "idem-1"));
        List<MdmPublicationMapper.PublicationRow> publications = service.publish(new MdmPublicationApplicationService.PublishCommand("MDV200001V1", 1001L, "idem-2"));
        service.consumeReceipt(new MdmPublicationApplicationService.ReceiptEvent("evt-1", "MdmPublicationReceiptReceived", publications.get(0).publicationNo(), "SUCCESS", null, "{}"));
        service.consumeReceipt(new MdmPublicationApplicationService.ReceiptEvent("evt-1", "MdmPublicationReceiptReceived", publications.get(0).publicationNo(), "SUCCESS", null, "{}"));
        MdmPublicationMapper.PublicationRow confirmed = mapper.findPublication(publications.get(0).publicationNo());
        assertThat(confirmed.status()).isEqualTo(PublicationAggregate.CONFIRMED);
        assertThat(mapper.inbox).hasSize(1);
        assertThat(mapper.outbox).extracting(MdmMapper.OutboxRow::eventType).contains("PublicationSubscriptionCreated", "MasterDataPublished", "MasterDataPublishConfirmed");
    }

    /**
     * 处理当前类型职责中的操作 {@code failedReceiptCanBeRetried}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void failedReceiptCanBeRetried() {
        MasterDataRecordApplicationServiceTest.MemoryRecordMapper recordMapper = new MasterDataRecordApplicationServiceTest.MemoryRecordMapper();
        recordMapper.versions.put("MDV200001V1", MasterDataRecordApplicationServiceTest.version("MDV200001V1", "MDR200001", "SKU", "SKU-001"));
        MasterDataRecordApplicationService recordService = new MasterDataRecordApplicationService(recordMapper, new MasterDataRecordApplicationServiceTest.MemoryMdmMapper());
        MemoryPublicationMapper mapper = new MemoryPublicationMapper();
        MdmPublicationApplicationService service = new MdmPublicationApplicationService(mapper, recordService);
        service.createSubscription(new MdmPublicationApplicationService.CreateSubscriptionCommand("SKU", "OMS", "mdm.sku.changed", null, 1001L, "idem-1"));
        MdmPublicationMapper.PublicationRow publication = service.publish(new MdmPublicationApplicationService.PublishCommand("MDV200001V1", 1001L, "idem-2")).get(0);
        service.consumeReceipt(new MdmPublicationApplicationService.ReceiptEvent("evt-2", "MdmPublicationReceiptReceived", publication.publicationNo(), "FAILED", "字段校验失败", "{}"));
        MdmPublicationMapper.PublicationRow retried = service.retry(publication.publicationNo(), new MdmPublicationApplicationService.RetryCommand("修正后重试", 1002L, "idem-3"));
        assertThat(retried.status()).isEqualTo(PublicationAggregate.PENDING);
        assertThat(retried.retryCount()).isEqualTo(1);
        assertThat(mapper.outbox).extracting(MdmMapper.OutboxRow::eventType).contains("MasterDataRepublished");
    }

    /**
     * 执行命令 {@code retryRejectsPendingPublication}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void retryRejectsPendingPublication() {
        MasterDataRecordApplicationServiceTest.MemoryRecordMapper recordMapper = new MasterDataRecordApplicationServiceTest.MemoryRecordMapper();
        recordMapper.versions.put("MDV200001V1", MasterDataRecordApplicationServiceTest.version("MDV200001V1", "MDR200001", "SKU", "SKU-001"));
        MasterDataRecordApplicationService recordService = new MasterDataRecordApplicationService(recordMapper, new MasterDataRecordApplicationServiceTest.MemoryMdmMapper());
        MemoryPublicationMapper mapper = new MemoryPublicationMapper();
        MdmPublicationApplicationService service = new MdmPublicationApplicationService(mapper, recordService);
        service.createSubscription(new MdmPublicationApplicationService.CreateSubscriptionCommand("SKU", "OMS", "mdm.sku.changed", null, 1001L, "idem-1"));
        MdmPublicationMapper.PublicationRow publication = service.publish(new MdmPublicationApplicationService.PublishCommand("MDV200001V1", 1001L, "idem-2")).get(0);
        assertThatThrownBy(() -> service.retry(publication.publicationNo(), new MdmPublicationApplicationService.RetryCommand("重试", 1002L, "idem-3"))).isInstanceOf(IllegalStateException.class).hasMessageContaining("not failed");
    }

    /**
     * MemoryPublicationMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class MemoryPublicationMapper implements MdmPublicationMapper {

        /**
         * subscriptions（类型：{@code Map<String,SubscriptionRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, SubscriptionRow> subscriptions = new LinkedHashMap<>();

        /**
         * publications（类型：{@code Map<String,PublicationRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, PublicationRow> publications = new LinkedHashMap<>();

        /**
         * inbox（类型：{@code Map<String,EventInboxRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, EventInboxRow> inbox = new LinkedHashMap<>();

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
         * 查询并返回 {@code findSubscription}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param subscriptionNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code SubscriptionRow}
         */
        @Override
        public SubscriptionRow findSubscription(String subscriptionNo) {
            return subscriptions.get(subscriptionNo);
        }

        /**
         * 查询并返回 {@code findActiveSubscription}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param typeCode 可追踪业务编码，类型为 {@code String}
         * @param targetSystem 业务处理参数或成员，类型为 {@code String}
         * @param eventTopic 业务处理参数或成员，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code SubscriptionRow}
         */
        @Override
        public SubscriptionRow findActiveSubscription(String typeCode, String targetSystem, String eventTopic) {
            return subscriptions.values().stream().filter(row -> row.typeCode().equals(typeCode) && row.targetSystem().equals(targetSystem) && row.eventTopic().equals(eventTopic) && row.status() == 1).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code listActiveSubscriptions}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param typeCode 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code List<SubscriptionRow>}
         */
        @Override
        public List<SubscriptionRow> listActiveSubscriptions(String typeCode) {
            return subscriptions.values().stream().filter(row -> row.typeCode().equals(typeCode) && row.status() == 1).toList();
        }

        /**
         * 查询并返回 {@code listSubscriptions}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<SubscriptionRow>}
         */
        @Override
        public List<SubscriptionRow> listSubscriptions() {
            return new ArrayList<>(subscriptions.values());
        }

        /**
         * 处理当前类型职责中的操作 {@code insertSubscription}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code SubscriptionRow}
         */
        @Override
        public void insertSubscription(SubscriptionRow row) {
            subscriptions.put(row.subscriptionNo(), row);
        }

        /**
         * 执行命令 {@code updateSubscription}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code SubscriptionRow}
         */
        @Override
        public void updateSubscription(SubscriptionRow row) {
            subscriptions.put(row.subscriptionNo(), row);
        }

        /**
         * 查询并返回 {@code findPublication}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param publicationNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code PublicationRow}
         */
        @Override
        public PublicationRow findPublication(String publicationNo) {
            return publications.get(publicationNo);
        }

        /**
         * 查询并返回 {@code listPublications}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<PublicationRow>}
         */
        @Override
        public List<PublicationRow> listPublications() {
            return new ArrayList<>(publications.values());
        }

        /**
         * 处理当前类型职责中的操作 {@code insertPublication}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code PublicationRow}
         */
        @Override
        public void insertPublication(PublicationRow row) {
            publications.put(row.publicationNo(), row);
        }

        /**
         * 执行命令 {@code updatePublication}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code PublicationRow}
         */
        @Override
        public void updatePublication(PublicationRow row) {
            publications.put(row.publicationNo(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code claimEvent}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code EventInboxRow}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        @Override
        public int claimEvent(EventInboxRow row) {
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
         * @param row 业务处理参数或成员，类型为 {@code EventInboxRow}
         */
        @Override
        public void updateEvent(EventInboxRow row) {
            inbox.put(row.eventId(), row);
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
}
