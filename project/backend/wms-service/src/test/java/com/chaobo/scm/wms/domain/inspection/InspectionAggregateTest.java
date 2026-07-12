package com.chaobo.scm.wms.domain.inspection;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InspectionAggregateTest {
    @Test
    void inspectionResultMustBalance() {
        var inspection = new InspectionAggregate(1, "QC001", 1, BigDecimal.TEN);
        assertThatThrownBy(() -> inspection.submit(BigDecimal.ONE, BigDecimal.ONE))
                .isInstanceOf(BusinessException.class);
        inspection.submit(new BigDecimal("8"), new BigDecimal("2"));
        assertThat(inspection.completed()).isTrue();
    }
}
