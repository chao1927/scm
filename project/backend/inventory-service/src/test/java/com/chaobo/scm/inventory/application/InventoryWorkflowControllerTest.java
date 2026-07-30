package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.inventory.domain.InventoryAccountAggregate;
import com.chaobo.scm.inventory.interfaces.web.InventoryAdjustmentController;
import com.chaobo.scm.inventory.interfaces.web.StockFreezeController;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 冻结与调整 HTTP 契约测试。
 *
 * <p>验证公开路由、必填幂等头、认证上下文到应用命令的转换，以及创建接口的 201 语义。
 */
class InventoryWorkflowControllerTest {

    private final MemoryInventoryWorkflowRepository repository =
            new MemoryInventoryWorkflowRepository();
    private final StockFreezeApplicationService freezes =
            new StockFreezeApplicationService(repository);
    private final InventoryAdjustmentApplicationService adjustments =
            new InventoryAdjustmentApplicationService(repository);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        repository.putAccount(new InventoryAccountAggregate(
                10L, 88L, 99L, "SKU-1", null,
                new BigDecimal("10"), new BigDecimal("10"),
                BigDecimal.ZERO, BigDecimal.ZERO, 0));
        mockMvc = MockMvcBuilders.standaloneSetup(
                new StockFreezeController(freezes),
                new InventoryAdjustmentController(adjustments)).build();
    }

    @Test
    void createsFreezeThroughIndependentAggregateEndpoint() throws Exception {
        mockMvc.perform(post("/api/inventory/v1/freezes")
                        .principal(authentication())
                        .header("X-Idempotency-Key", "freeze-create-1")
                        .header("X-Request-Id", "REQ-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId":88,
                                  "warehouseId":99,
                                  "sku":"SKU-1",
                                  "freezeQty":3,
                                  "freezeReason":"QUALITY",
                                  "sourceNo":"QC-1",
                                  "autoSubmit":true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value(2));

        org.assertj.core.api.Assertions.assertThat(repository.freezes).hasSize(1);
    }

    @Test
    void executesApprovedAdjustmentThroughVersionedEndpoint() throws Exception {
        InventoryAdjustmentApplicationService.AdjustmentResult created = adjustments.create(
                new InventoryAdjustmentApplicationService.CreateAdjustmentCommand(
                        88L, 99L, "SKU-1", null, new BigDecimal("-2"),
                        "STOCK_LOSS", "盘亏复核", "WMS", "ST-1", true,
                        100L, "prepare-create", "REQ-P1"));
        InventoryAdjustmentApplicationService.AdjustmentResult approved = adjustments.approve(
                new InventoryAdjustmentApplicationService.ApproveAdjustmentCommand(
                        created.adjustmentNo(), "APPROVE", "APR-1", 200L,
                        created.version(), "prepare-approve", "REQ-P2"));

        mockMvc.perform(post("/api/inventory/v1/adjustments/"
                        + created.adjustmentNo() + "/execute")
                        .principal(authentication())
                        .header("X-Idempotency-Key", "adjust-execute-1")
                        .header("X-Request-Id", "REQ-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "version":%d,
                                  "executeRemark":"盘亏复核"
                                }
                                """.formatted(approved.version())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.adjustmentNo")
                        .value(created.adjustmentNo()))
                .andExpect(jsonPath("$.data.status").value(4));

        org.assertj.core.api.Assertions.assertThat(repository.ledgers)
                .contains("10:ADJUST:-2");
    }

    @Test
    void rejectsCreateWithoutPersistentIdempotencyHeader() throws Exception {
        mockMvc.perform(post("/api/inventory/v1/freezes")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId":88,
                                  "warehouseId":99,
                                  "sku":"SKU-1",
                                  "freezeQty":3,
                                  "freezeReason":"QUALITY",
                                  "sourceNo":"QC-1",
                                  "autoSubmit":true
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private static UsernamePasswordAuthenticationToken authentication() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("inventory-user", "n/a", Set.of());
        authentication.setDetails(new ScmAccessContext(
                100L,
                "inventory-user",
                "INVENTORY",
                Set.of("*"),
                Map.of("OWNER", Set.of("88"), "WAREHOUSE", Set.of("99"))));
        return authentication;
    }
}
