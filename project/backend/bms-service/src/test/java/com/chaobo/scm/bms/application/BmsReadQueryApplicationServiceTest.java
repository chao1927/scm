package com.chaobo.scm.bms.application;

import com.chaobo.scm.bms.infrastructure.persistence.BmsReadQueryMapper;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.security.ScmAccessContext;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * BMS 页面查询结算对象范围测试。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class BmsReadQueryApplicationServiceTest {

    @Test
    void filtersMoneyRowsByBillingObjectWithoutChangingDecimalValues() {
        BmsReadQueryApplicationService service =
            new BmsReadQueryApplicationService(new QueryMapper());

        var result = service.charges(null, "2026-07", 1, 20, access("BO-A"));

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.records().get(0).totalAmount()).isEqualByComparingTo("10.23");
        assertThatThrownBy(() ->
            service.charges("BO-B", "2026-07", 1, 20, access("BO-A")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("BILLING_OBJECT/BO-B");
    }

    private static ScmAccessContext access(String objectCode) {
        return new ScmAccessContext(1001, "finance", "BMS",
            Set.of("bms:query:read"),
            Map.of("BILLING_OBJECT", Set.of(objectCode)));
    }

    static final class QueryMapper implements BmsReadQueryMapper {

        @Override
        public List<ChargeView> listCharges(String objectCode, String billingPeriod) {
            return List.of(charge("BO-A", "10.23"), charge("BO-B", "99.99"));
        }

        @Override
        public List<RuleView> listRules(String objectCode) {
            return List.of();
        }

        @Override
        public List<ReconciliationView> listReconciliations(String billingPeriod) {
            return List.of();
        }

        @Override
        public List<BillView> listBills(String billingPeriod) {
            return List.of();
        }

        @Override
        public List<InvoiceView> listInvoices(String billingPeriod) {
            return List.of();
        }

        @Override
        public List<FinanceView> listFinanceHandovers() {
            return List.of();
        }

        @Override
        public List<RefundView> listRefunds() {
            return List.of();
        }

        @Override
        public List<SettlementView> listSettlementSummaries(String billingPeriod) {
            return List.of(
                settlement("BO-A", "100.10", "80.08", "10.01"),
                settlement("BO-B", "200.20", "100.10", "20.02"));
        }

        private ChargeView charge(String objectCode, String total) {
            return new ChargeView("CH-" + objectCode, "SRC", objectCode,
                objectCode, "PAYABLE", "CNY", "FREIGHT", "RULE",
                BigDecimal.ONE, new BigDecimal(total), new BigDecimal(total),
                BigDecimal.ZERO, new BigDecimal(total), "2026-07", 1,
                LocalDateTime.parse("2026-07-30T10:00:00"));
        }

        private SettlementView settlement(String objectCode, String bill,
                                          String invoice, String refund) {
            return new SettlementView(objectCode, objectCode, "PAYABLE", "CNY",
                "2026-07", new BigDecimal(bill), new BigDecimal(invoice),
                new BigDecimal(refund));
        }
    }
}
