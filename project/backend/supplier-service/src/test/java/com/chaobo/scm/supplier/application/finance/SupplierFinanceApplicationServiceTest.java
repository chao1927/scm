package com.chaobo.scm.supplier.application.finance;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.supplier.application.shared.AuditLogRepository;
import com.chaobo.scm.supplier.application.shared.CommandContext;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import com.chaobo.scm.supplier.infrastructure.persistence.finance.SupplierFinanceMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 验证供应商对账协同中的金额差异规则和持久化状态。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class SupplierFinanceApplicationServiceTest {

    /**
     * 验证确认金额与账单金额不一致时必须说明差异原因，否则不得写入对账结果。
     */
    @Test
    void shouldRejectReconciliationDifferenceWithoutReason() {
        var mapper = new FinanceMapperStub();
        var audit = new AuditStub();
        var service = service(mapper, audit);

        assertThatThrownBy(() -> service.confirm(20L, 0, new BigDecimal("99.00"), "", context("IDEM-1")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("差异原因");
        assertThat(mapper.responded).isFalse();
        assertThat(audit.operationType).isNull();
    }

    /**
     * 验证已说明原因的对账差异进入差异待处理状态并记录审计。
     */
    @Test
    void shouldRecordReconciliationDifferenceWithReason() {
        var mapper = new FinanceMapperStub();
        var audit = new AuditStub();
        var service = service(mapper, audit);

        service.confirm(20L, 0, new BigDecimal("99.00"), "质检扣款 1 元", context("IDEM-2"));

        assertThat(mapper.responded).isTrue();
        assertThat(mapper.responseStatus).isEqualTo(3);
        assertThat(mapper.responseReason).isEqualTo("质检扣款 1 元");
        assertThat(audit.operationType).isEqualTo("REPORT_RECONCILIATION_DIFFERENCE");
    }

    /** 创建只依赖内存契约实现的对账应用服务。 */
    private static SupplierFinanceApplicationService service(FinanceMapperStub mapper, AuditStub audit) {
        IdentifierGenerator ids = new IdentifierGenerator() {
            @Override
            public long nextId() {
                return 1L;
            }

            @Override
            public String nextBusinessNo(String prefix) {
                return prefix + "1";
            }
        };
        return new SupplierFinanceApplicationService(mapper, ids, audit);
    }

    /** 创建具备对账确认权限和供应商数据范围的命令上下文。 */
    private static CommandContext context(String idempotencyKey) {
        return new CommandContext(1L, "财务协同员", 1L, 101L, "REQ-1", "TRACE-1",
                idempotencyKey, Set.of("supplier:reconciliation:confirm"));
    }

    /** 实现对账差异用例所需的最小持久化契约。 */
    private static final class FinanceMapperStub implements SupplierFinanceMapper {
        private boolean responded;
        private int responseStatus;
        private String responseReason;

        @Override
        public void upsertStatement(long id, String no, long supplierId, String currency, BigDecimal amount,
                                    int sourceVersion) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FinanceViews.Reconciliation reconciliation(long id) {
            return new FinanceViews.Reconciliation(20L, "REC-20", 101L, "CNY", new BigDecimal("100.00"),
                    null, 1, null, 1, 0);
        }

        @Override
        public List<FinanceViews.Reconciliation> reconciliations(Long supplierId, Integer status, int offset,
                                                                  int size) {
            return List.of();
        }

        @Override
        public long reconciliationCount(Long supplierId, Integer status) {
            return 0;
        }

        @Override
        public int respond(long id, int version, BigDecimal amount, int status, String reason) {
            responded = true;
            responseStatus = status;
            responseReason = reason;
            return 1;
        }

        @Override
        public int changeStatus(long id, int version, int status) {
            return 0;
        }

        @Override
        public void insertInvoice(long id, String no, long supplierId, Long reconciliationId, int type,
                                  BigDecimal net, BigDecimal tax, BigDecimal rate, String url) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FinanceViews.Invoice invoice(long id) {
            return null;
        }

        @Override
        public List<FinanceViews.Invoice> invoices(Long supplierId, Integer status, int offset, int size) {
            return List.of();
        }

        @Override
        public long invoiceCount(Long supplierId, Integer status) {
            return 0;
        }

        @Override
        public int validateInvoice(long id, int status, String message) {
            return 0;
        }
    }

    /** 记录用例产生的审计操作类型。 */
    private static final class AuditStub implements AuditLogRepository {
        private String operationType;

        @Override
        public void save(CommandContext context, String operationType, String targetType, long targetId,
                         String targetNo, String beforeSnapshot, String afterSnapshot) {
            this.operationType = operationType;
        }
    }
}
