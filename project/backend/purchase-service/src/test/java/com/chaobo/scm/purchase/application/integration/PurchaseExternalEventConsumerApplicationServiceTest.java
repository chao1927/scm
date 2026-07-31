package com.chaobo.scm.purchase.application.integration;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.purchase.infrastructure.persistence.integration.PurchaseExternalFactMapper;
import com.chaobo.scm.purchase.domain.rfq.RfqAggregate;
import com.chaobo.scm.purchase.domain.rfq.RfqInvitation;
import com.chaobo.scm.purchase.domain.rfq.RfqLine;
import com.chaobo.scm.purchase.domain.rfq.RfqRepository;
import com.chaobo.scm.purchase.domain.rfq.RfqStatus;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PurchaseExternalEventConsumerApplicationServiceTest。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class PurchaseExternalEventConsumerApplicationServiceTest {

    /**
     * inbox（类型：{@code FakeInbox}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final FakeInbox inbox = new FakeInbox();

    /**
     * facts（类型：{@code FakeFacts}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final FakeFacts facts = new FakeFacts();

    /**
     * service（类型：{@code PurchaseExternalEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final FakeRfqRepository rfqs = new FakeRfqRepository();

    private final PurchaseExternalEventConsumerApplicationService service = new PurchaseExternalEventConsumerApplicationService(inbox, new InboundEventPayloadStore(inbox, new ObjectMapper()), facts.proxy(), null, null, rfqs, new ObjectMapper());

    /**
     * 处理当前类型职责中的操作 {@code supplierQuoteEventWritesQuoteFactAndMarksInboxSucceeded}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void supplierQuoteEventWritesQuoteFactAndMarksInboxSucceeded() {
        rfqs.aggregate = quotingRfq();
        service.consume(new PurchaseExternalEvent("SUPPLIER", "EVT-1", "SupplierQuoteSubmitted", null, null, "RFQ001", "Q001", null, null, 3001L, null, null, "SKU-01", new BigDecimal("10"), null, null, null, null, new BigDecimal("99.00"), "CNY", null, null, null, null, null, null, 1, null, Map.of("score", 90)));
        assertThat(facts.quoteNo).isEqualTo("Q001");
        assertThat(facts.rfqNo).isEqualTo("RFQ001");
        assertThat(facts.supplierId).isEqualTo(3001L);
        assertThat(inbox.status).isEqualTo(2);
        assertThat(inbox.payloadJson).contains("SupplierQuoteSubmitted");
    }

    /**
     * 处理当前类型职责中的操作 {@code duplicateSucceededEventIsIgnored}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void duplicateSucceededEventIsIgnored() {
        inbox.claimResult = InboundEventLogPort.ClaimResult.ALREADY_SUCCEEDED;
        service.consume(new PurchaseExternalEvent("SUPPLIER", "EVT-1", "SupplierQuoteSubmitted", null, null, "RFQ001", "Q001", null, null, 3001L, null, null, "SKU-01", BigDecimal.ONE, null, null, null, null, BigDecimal.TEN, "CNY", null, null, null, null, null, null, 1, null, Map.of()));
        assertThat(facts.quoteNo).isNull();
    }

    @Test
    void quoteChangeAfterBiddingClosedIsRejectedWithoutUpdatingFact() {
        rfqs.aggregate = closedRfq();

        assertThatThrownBy(() -> service.consume(new PurchaseExternalEvent("SUPPLIER", "EVT-CLOSED-1", "SupplierQuoteChanged", null, null, "RFQ001", "Q001", null, null, 3001L, null, null, "SKU-01", BigDecimal.ONE, null, null, null, null, BigDecimal.TEN, "CNY", null, null, null, null, null, null, 2, null, Map.of())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("截标");
        assertThat(facts.quoteNo).isNull();
        assertThat(inbox.status).isEqualTo(3);
    }

    @Test
    void adoptedQuoteFactCanBeRecordedAfterBiddingClosed() {
        rfqs.aggregate = closedRfq();

        service.consume(new PurchaseExternalEvent("SUPPLIER", "EVT-ADOPTED-1", "SupplierQuoteAdopted", null, null, "RFQ001", "Q001", null, null, 3001L, null, null, "SKU-01", BigDecimal.ONE, null, null, null, null, BigDecimal.TEN, "CNY", null, null, null, null, null, null, 3, null, Map.of()));

        assertThat(facts.quoteNo).isEqualTo("Q001");
        assertThat(inbox.status).isEqualTo(2);
    }

    private RfqAggregate closedRfq() {
        return new RfqAggregate(1, "RFQ001", 2, 2001, "CATE-01", "PR001", OffsetDateTime.now().plusDays(1), RfqStatus.BIDDING_CLOSED, OffsetDateTime.now().minusDays(1), "到期截标", 2,
                List.of(new RfqLine(11, "SKU-01", BigDecimal.TEN, "PCS", LocalDate.now().plusDays(7), null)),
                List.of(new RfqInvitation(21, 3001, 4)));
    }

    private RfqAggregate quotingRfq() {
        return new RfqAggregate(1, "RFQ001", 2, 2001, "CATE-01", "PR001", OffsetDateTime.now().plusDays(1), RfqStatus.QUOTING, OffsetDateTime.now().minusDays(1), null, 1,
                List.of(new RfqLine(11, "SKU-01", BigDecimal.TEN, "PCS", LocalDate.now().plusDays(7), null)),
                List.of(new RfqInvitation(21, 3001, 2)));
    }

    private static final class FakeRfqRepository implements RfqRepository {
        private RfqAggregate aggregate;

        @Override
        public Optional<RfqAggregate> findById(long id) {
            return Optional.ofNullable(aggregate);
        }

        @Override
        public Optional<RfqAggregate> findByNo(String rfqNo) {
            return Optional.ofNullable(aggregate);
        }

        @Override
        public void save(RfqAggregate aggregate, long operatorId) {
            this.aggregate = aggregate;
        }
    }

    /**
     * FakeInbox。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static final class FakeInbox implements InboundEventLogPort {

        /**
         * claimResult（类型：{@code ClaimResult}）。
         *
         * <p>保存当前对象所需的处理结果；其具体生命周期由所属对象统一管理。
         */
        private ClaimResult claimResult = ClaimResult.CLAIMED;

        /**
         * status（类型：{@code int}）。
         *
         * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
         */
        private int status;

        /**
         * payloadJson（类型：{@code String}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private String payloadJson;

        /**
         * 处理当前类型职责中的操作 {@code claim}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
         * @param eventCode 可追踪业务编码，类型为 {@code String}
         * @param eventType 业务处理参数或成员，类型为 {@code String}
         * @param consumerName 业务处理参数或成员，类型为 {@code String}
         * @param idempotentKey 业务或技术标识，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code ClaimResult}
         */
        @Override
        public ClaimResult claim(String sourceSystem, String eventCode, String eventType, String consumerName, String idempotentKey) {
            return claimResult;
        }

        /**
         * 执行命令 {@code savePayload}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
         * @param eventCode 可追踪业务编码，类型为 {@code String}
         * @param consumerName 业务处理参数或成员，类型为 {@code String}
         * @param payloadJson 业务处理参数或成员，类型为 {@code String}
         */
        @Override
        public void savePayload(String sourceSystem, String eventCode, String consumerName, String payloadJson) {
            this.payloadJson = payloadJson;
        }

        /**
         * 处理当前类型职责中的操作 {@code markSucceeded}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
         * @param eventCode 可追踪业务编码，类型为 {@code String}
         * @param consumerName 业务处理参数或成员，类型为 {@code String}
         * @param ignored 业务处理参数或成员，类型为 {@code boolean}
         */
        @Override
        public void markSucceeded(String sourceSystem, String eventCode, String consumerName, boolean ignored) {
            this.status = ignored ? 4 : 2;
        }

        /**
         * 处理当前类型职责中的操作 {@code recordFailure}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
         * @param eventCode 可追踪业务编码，类型为 {@code String}
         * @param eventType 业务处理参数或成员，类型为 {@code String}
         * @param consumerName 业务处理参数或成员，类型为 {@code String}
         * @param idempotentKey 业务或技术标识，类型为 {@code String}
         * @param reason 业务处理参数或成员，类型为 {@code String}
         */
        @Override
        public void recordFailure(String sourceSystem, String eventCode, String eventType, String consumerName, String idempotentKey, String reason) {
            this.status = 3;
        }

        /**
         * 查询并返回 {@code findForReplay}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param consumeLogId 业务或技术标识，类型为 {@code long}
         * @return 查询并返回的结果，类型为 {@code Optional<ReplayEvent>}
         */
        @Override
        public Optional<ReplayEvent> findForReplay(long consumeLogId) {
            return Optional.empty();
        }

        /**
         * 处理当前类型职责中的操作 {@code markReplayRequested}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param consumeLogId 业务或技术标识，类型为 {@code long}
         * @param operatorId 业务或技术标识，类型为 {@code long}
         * @param reason 业务处理参数或成员，类型为 {@code String}
         */
        @Override
        public void markReplayRequested(long consumeLogId, long operatorId, String reason) {
        }
    }

    /**
     * FakeFacts。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static final class FakeFacts {

        /**
         * quoteNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private String quoteNo;

        /**
         * rfqNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private String rfqNo;

        /**
         * supplierId（类型：{@code long}）。
         *
         * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
         */
        private long supplierId;

        /**
         * 处理当前类型职责中的操作 {@code proxy}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code PurchaseExternalFactMapper}
         */
        PurchaseExternalFactMapper proxy() {
            return (PurchaseExternalFactMapper) Proxy.newProxyInstance(PurchaseExternalFactMapper.class.getClassLoader(), new Class<?>[] { PurchaseExternalFactMapper.class }, (target, method, args) -> {
                if (UPSERT_QUOTE.equals(method.getName())) {
                    quoteNo = (String) args[1];
                    rfqNo = (String) args[2];
                    supplierId = (Long) args[3];
                    return null;
                }
                if (method.getReturnType().equals(Void.TYPE)) {
                    return null;
                }
                throw new UnsupportedOperationException(method.getName());
            });
        }

        /**
         * 业务常量 {@code UPSERT_QUOTE}。
         *
         * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
         */
        private static final String UPSERT_QUOTE = "upsertQuote";
    }
}
