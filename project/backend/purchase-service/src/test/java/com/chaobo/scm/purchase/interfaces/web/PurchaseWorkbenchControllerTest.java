package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.purchase.application.workbench.PurchaseTodoView;
import com.chaobo.scm.purchase.application.workbench.PurchaseTodoReadCriteria;
import com.chaobo.scm.purchase.application.workbench.PurchaseWorkbenchMetricView;
import com.chaobo.scm.purchase.application.workbench.PurchaseWorkbenchQueryApplicationService;
import com.chaobo.scm.purchase.application.workbench.PurchaseWorkbenchReadCriteria;
import com.chaobo.scm.purchase.application.workbench.PurchaseWorkbenchReadModelPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 采购工作台 HTTP 契约测试。
 */
class PurchaseWorkbenchControllerTest {

    private PurchaseWorkbenchQueryApplicationService queryService;
    private CapturingReadModel readModel;
    private MockMvc mockMvc;
    private TestingAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        readModel = new CapturingReadModel();
        queryService = new PurchaseWorkbenchQueryApplicationService(readModel);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PurchaseWorkbenchController(queryService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        authentication = new TestingAuthenticationToken("buyer", null);
        authentication.setAuthenticated(true);
        authentication.setDetails(new ScmAccessContext(
                42L,
                "buyer",
                "SCM",
                Set.of("purchase:workbench:read"),
                Map.of("PURCHASE_ORG", Set.of("1001"))
        ));
    }

    @Test
    void exposesTraceableSummaryContract() throws Exception {
        readModel.metrics = List.of(
                new PurchaseWorkbenchMetricView(
                        "TODO", "REQUISITION_APPROVAL", "待审批请购",
                        2L, "purchase_requisition",
                        "/purchase/requisition-approvals"),
                new PurchaseWorkbenchMetricView(
                        "PRICE", "EXPIRING_PRICE", "三十日内到期价格",
                        6L, "purchase_price", "/purchase/prices")
        );

        mockMvc.perform(get("/api/purchase/v1/workbench/summary")
                        .param("purchaseOrgId", "1001")
                        .param("scopeMode", "ORGANIZATION")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pendingApprovalCount").value(2))
                .andExpect(jsonPath("$.data.metrics[1].factSource")
                        .value("purchase_price"));
    }

    @Test
    void bindsPaginationSortAndScopeToTodoQuery() throws Exception {
        mockMvc.perform(get("/api/purchase/v1/workbench/todos")
                        .param("purchaseOrgId", "1001")
                        .param("scopeMode", "SELF")
                        .param("todoType", "DELIVERY_OVERDUE")
                        .param("pageNo", "2")
                        .param("pageSize", "20")
                        .param("sortField", "dueDate")
                        .param("sortOrder", "asc")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageNo").value(2))
                .andExpect(jsonPath("$.data.total").value(0));

        assertThat(readModel.todoCriteria.scope().ownerId()).isEqualTo(42L);
        assertThat(readModel.todoCriteria.sortField()).isEqualTo("dueDate");
        assertThat(readModel.todoCriteria.todoType())
                .isEqualTo("DELIVERY_OVERDUE");
    }

    private static final class CapturingReadModel
            implements PurchaseWorkbenchReadModelPort {

        private List<PurchaseWorkbenchMetricView> metrics = List.of();
        private PurchaseTodoReadCriteria todoCriteria;

        @Override
        public List<PurchaseWorkbenchMetricView> summarize(
                PurchaseWorkbenchReadCriteria criteria
        ) {
            return metrics;
        }

        @Override
        public PageResult<PurchaseTodoView> pageTodos(
                PurchaseTodoReadCriteria criteria
        ) {
            todoCriteria = criteria;
            return new PageResult<>(
                    criteria.pageNo(), criteria.pageSize(), 0L, List.of());
        }
    }
}
