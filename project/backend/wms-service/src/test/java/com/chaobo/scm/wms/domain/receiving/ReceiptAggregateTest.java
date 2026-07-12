package com.chaobo.scm.wms.domain.receiving;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReceiptAggregateTest {
    @Test
    void receiptRequiresBalancedQuantityBeforeCompletion() {
        var receipt = new ReceiptAggregate(1, "REC001", 1, "SKU001", BigDecimal.TEN,
                BigDecimal.ZERO, BigDecimal.ZERO, ReceiptStatus.RECEIVING, 0);
        receipt.scan(new BigDecimal("8"), BigDecimal.ZERO, null);
        assertThatThrownBy(receipt::complete).isInstanceOf(BusinessException.class);
        receipt.scan(new BigDecimal("2"), BigDecimal.ZERO, null);
        receipt.complete();
        assertThat(receipt.status()).isEqualTo(ReceiptStatus.COMPLETED);
    }

    @Test
    void rejectedQuantityRequiresReason() {
        var receipt = new ReceiptAggregate(1, "REC001", 1, "SKU001", BigDecimal.ONE,
                BigDecimal.ZERO, BigDecimal.ZERO, ReceiptStatus.RECEIVING, 0);
        assertThatThrownBy(() -> receipt.scan(BigDecimal.ZERO, BigDecimal.ONE, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("拒收必须填写原因");
    }
}
