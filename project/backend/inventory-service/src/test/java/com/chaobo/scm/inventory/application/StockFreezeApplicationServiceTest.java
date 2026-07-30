package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.inventory.domain.InventoryAccountAggregate;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 冻结工作流应用服务测试。
 */
class StockFreezeApplicationServiceTest {

    private final MemoryInventoryWorkflowRepository repository =
            new MemoryInventoryWorkflowRepository();
    private final StockFreezeApplicationService service =
            new StockFreezeApplicationService(repository);

    @BeforeEach
    void prepareAccount() {
        repository.putAccount(new InventoryAccountAggregate(
                10L, 88L, 99L, "SKU-1", null,
                new BigDecimal("10"), new BigDecimal("10"),
                BigDecimal.ZERO, BigDecimal.ZERO, 0));
    }

    @Test
    void approvalAndUnfreezeKeepAccountQuantitiesConservedAndWriteLedger() {
        StockFreezeApplicationService.FreezeResult created = service.create(
                new StockFreezeApplicationService.CreateFreezeCommand(
                        88L, 99L, "SKU-1", null, new BigDecimal("4"),
                        "QUALITY", "WMS", "QC-1", true,
                        100L, "idem-create", "REQ-1"));

        StockFreezeApplicationService.FreezeResult frozen = service.approve(
                new StockFreezeApplicationService.ApproveFreezeCommand(
                        created.freezeNo(), "APPROVE", "APR-1", 200L,
                        created.version(), "idem-approve", "REQ-2"));
        StockFreezeApplicationService.FreezeResult unfrozen = service.unfreeze(
                new StockFreezeApplicationService.UnfreezeCommand(
                        created.freezeNo(), BigDecimal.ONE, "质检转良", 300L,
                        frozen.version(), "idem-unfreeze", "REQ-3"));

        InventoryAccountAggregate account = repository.accounts.get(10L);
        assertThat(account.onHandQty()).isEqualByComparingTo("10");
        assertThat(account.availableQty()).isEqualByComparingTo("7");
        assertThat(account.frozenQty()).isEqualByComparingTo("3");
        assertThat(repository.ledgers).containsExactly(
                "10:FREEZE:-4",
                "10:UNFREEZE:1");
        assertThat(repository.events).contains(
                "StockFreezeCreated:" + created.freezeNo(),
                "StockFrozen:" + created.freezeNo(),
                "StockUnfrozen:" + created.freezeNo());
        assertThat(repository.audits).hasSize(3);
        assertThat(unfrozen.remainingFrozenQty()).isEqualByComparingTo("3");
    }

    @Test
    void repeatedApprovalReturnsOriginalResultWithoutSecondLedger() {
        StockFreezeApplicationService.FreezeResult created = service.create(
                new StockFreezeApplicationService.CreateFreezeCommand(
                        88L, 99L, "SKU-1", null, new BigDecimal("2"),
                        "MANUAL", "INVENTORY", "CASE-1", true,
                        100L, "idem-create", "REQ-1"));
        StockFreezeApplicationService.ApproveFreezeCommand command =
                new StockFreezeApplicationService.ApproveFreezeCommand(
                        created.freezeNo(), "APPROVE", "APR-1", 200L,
                        created.version(), "idem-approve", "REQ-2");

        service.approve(command);
        StockFreezeApplicationService.FreezeResult duplicate = service.approve(command);

        assertThat(duplicate.idempotentHit()).isTrue();
        assertThat(repository.ledgers).hasSize(1);
    }

    @Test
    void accountOptimisticLockFailureStopsApproval() {
        StockFreezeApplicationService.FreezeResult created = service.create(
                new StockFreezeApplicationService.CreateFreezeCommand(
                        88L, 99L, "SKU-1", null, BigDecimal.ONE,
                        "MANUAL", "INVENTORY", "CASE-1", true,
                        100L, "idem-create", "REQ-1"));
        repository.forceAccountVersionConflict = true;

        assertThatThrownBy(() -> service.approve(
                new StockFreezeApplicationService.ApproveFreezeCommand(
                        created.freezeNo(), "APPROVE", "APR-1", 200L,
                        created.version(), "idem-approve", "REQ-2")))
                .isInstanceOf(BusinessException.class);
        assertThat(repository.ledgers).isEmpty();
    }
}
