package com.chaobo.scm.inventory.application;

import com.chaobo.scm.inventory.domain.InventoryAccountAggregate;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 调整工作流应用服务测试。
 */
class InventoryAdjustmentApplicationServiceTest {

    private final MemoryInventoryWorkflowRepository repository =
            new MemoryInventoryWorkflowRepository();
    private final InventoryAdjustmentApplicationService service =
            new InventoryAdjustmentApplicationService(repository);

    @BeforeEach
    void prepareAccount() {
        repository.putAccount(new InventoryAccountAggregate(
                10L, 88L, 99L, "SKU-1", null,
                new BigDecimal("10"), new BigDecimal("10"),
                BigDecimal.ZERO, BigDecimal.ZERO, 0));
    }

    @Test
    void approvedAdjustmentExecutesOnceAndWritesImmutableEvidence() {
        InventoryAdjustmentApplicationService.AdjustmentResult created = service.create(
                new InventoryAdjustmentApplicationService.CreateAdjustmentCommand(
                        88L, 99L, "SKU-1", null, new BigDecimal("-2"),
                        "STOCK_LOSS", "盘亏复核", "WMS", "ST-1", true,
                        100L, "adj-create", "REQ-1"));
        InventoryAdjustmentApplicationService.AdjustmentResult approved = service.approve(
                new InventoryAdjustmentApplicationService.ApproveAdjustmentCommand(
                        created.adjustmentNo(), "APPROVE", "APR-1", 200L,
                        created.version(), "adj-approve", "REQ-2"));
        InventoryAdjustmentApplicationService.ExecuteAdjustmentCommand execute =
                new InventoryAdjustmentApplicationService.ExecuteAdjustmentCommand(
                        created.adjustmentNo(), "盘点差异确认", 300L,
                        approved.version(), "adj-execute", "REQ-3");

        service.execute(execute);
        InventoryAdjustmentApplicationService.AdjustmentResult duplicate =
                service.execute(execute);

        InventoryAccountAggregate account = repository.accounts.get(10L);
        assertThat(account.onHandQty()).isEqualByComparingTo("8");
        assertThat(account.availableQty()).isEqualByComparingTo("8");
        assertThat(repository.ledgers).containsExactly("10:ADJUST:-2");
        assertThat(repository.events).contains(
                "StockAdjustmentCreated:" + created.adjustmentNo(),
                "StockAdjustmentApproved:" + created.adjustmentNo(),
                "StockAdjusted:" + created.adjustmentNo());
        assertThat(duplicate.idempotentHit()).isTrue();
    }
}
