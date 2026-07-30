package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.security.ScmSecurityConfiguration;
import com.chaobo.scm.supplier.application.asn.*;
import com.chaobo.scm.supplier.application.integration.InboundEventReplayApplicationService;
import com.chaobo.scm.supplier.application.operations.OperationViews;
import com.chaobo.scm.supplier.application.operations.SupplierOperationsApplicationService;
import com.chaobo.scm.supplier.application.order.PoConfirmApplicationService;
import com.chaobo.scm.supplier.application.order.PoConfirmView;
import com.chaobo.scm.supplier.application.profile.SupplierAdmissionApplicationService;
import com.chaobo.scm.supplier.application.quality.SupplierQualityIssueApplicationService;
import com.chaobo.scm.supplier.application.quality.SupplierQualityIssueView;
import com.chaobo.scm.supplier.infrastructure.security.CommandContextFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import jakarta.servlet.Filter;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 验证供应商协同核心入口的认证、授权和供应商数据范围透传契约。
 *
 * <p>测试使用真实 Spring Security 过滤器链、方法级授权和生产 Controller，仅将应用服务替换为 Mock，
 * 因而可以在不启动数据库、Redis、Nacos 的情况下稳定验证 HTTP 401/403 语义。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class SupplierSecurityAndScopeWebTest {

    private static final long TOKEN_SUPPLIER_ID = 9001L;
    private static final long REQUESTED_SUPPLIER_ID = 9002L;

    private AnnotationConfigWebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                "scm.security.hmac-secret=01234567890123456789012345678901",
                "scm.security.permission-namespace=supplier");
        context.register(WebConfiguration.class, MockApplicationServices.class, ScmSecurityConfiguration.class,
                CommandContextFactory.class, GlobalExceptionHandler.class, SupplierAdmissionController.class,
                PoConfirmController.class, AsnController.class, SupplierQualityIssueController.class,
                SupplierOperationsController.class);
        context.refresh();
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class))
                .build();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    /**
     * 准入、PO 确认、ASN、质量整改和导出任务入口都必须拒绝匿名访问。
     */
    @Test
    void coreSupplierEndpointsRejectAnonymousRequests() throws Exception {
        mockMvc.perform(post("/api/supplier/v1/admissions")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/supplier/v1/po-confirms")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/supplier/v1/asns")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/supplier/v1/quality-issues/1/request-rectification"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/supplier/v1/operations/exports")).andExpect(status().isUnauthorized());
    }

    /**
     * 错误权限不能越过方法授权或应用服务权限边界。
     */
    @Test
    void coreSupplierEndpointsRejectWrongPermissions() throws Exception {
        mockMvc.perform(post("/api/supplier/v1/admissions")
                        .header("X-Idempotency-Key", "WEB-ADM-1")
                        .contentType("application/json")
                        .content("""
                                {"supplierCode":"SUP-1","supplierName":"候选供应商","taxNo":"91310000",
                                 "supplierType":"MANUFACTURER","contactName":"张三",
                                 "contactMobile":"13800000000","settlementJson":"{}"}
                                """)
                        .with(token("supplier:po_confirm:read")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/supplier/v1/po-confirms")
                        .with(token("supplier:asn:read")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/supplier/v1/asns")
                        .with(token("supplier:po_confirm:read")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/supplier/v1/quality-issues/1/request-rectification")
                        .header("X-Idempotency-Key", "WEB-QI-1")
                        .contentType("application/json")
                        .content("{\"version\":0,\"deadline\":\"2099-01-01T00:00:00+08:00\"}")
                        .with(token("supplier:asn:read")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/supplier/v1/operations/exports")
                        .with(token("supplier:asn:read")))
                .andExpect(status().isForbidden());
    }

    /**
     * 外部供应商令牌中的 supplier_id 必须覆盖查询参数，防止横向读取其他供应商数据。
     */
    @Test
    void supplierClaimIsPassedAsMandatoryDataScope() throws Exception {
        mockMvc.perform(get("/api/supplier/v1/po-confirms")
                        .param("supplierId", Long.toString(REQUESTED_SUPPLIER_ID))
                        .with(token("supplier:po_confirm:read")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/supplier/v1/asns")
                        .param("supplierId", Long.toString(REQUESTED_SUPPLIER_ID))
                        .with(token("supplier:asn:read")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/supplier/v1/quality-issues")
                        .param("supplierId", Long.toString(REQUESTED_SUPPLIER_ID))
                        .with(token("supplier:quality:read")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/supplier/v1/operations/exports")
                        .param("supplierId", Long.toString(REQUESTED_SUPPLIER_ID))
                        .with(token("supplier:export:read")))
                .andExpect(status().isOk());

        assertThat(context.getBean(RecordingPoConfirmService.class).requestedSupplierId)
                .isEqualTo(REQUESTED_SUPPLIER_ID);
        assertThat(context.getBean(RecordingPoConfirmService.class).supplierScopeId)
                .isEqualTo(TOKEN_SUPPLIER_ID);
        assertThat(context.getBean(RecordingAsnQueryService.class).requestedSupplierId)
                .isEqualTo(REQUESTED_SUPPLIER_ID);
        assertThat(context.getBean(RecordingAsnQueryService.class).supplierScopeId)
                .isEqualTo(TOKEN_SUPPLIER_ID);
        assertThat(context.getBean(RecordingQualityService.class).requestedSupplierId)
                .isEqualTo(REQUESTED_SUPPLIER_ID);
        assertThat(context.getBean(RecordingQualityService.class).supplierScopeId)
                .isEqualTo(TOKEN_SUPPLIER_ID);
        assertThat(context.getBean(RecordingOperationsService.class).requestedSupplierId)
                .isEqualTo(REQUESTED_SUPPLIER_ID);
        assertThat(context.getBean(RecordingOperationsService.class).supplierScopeId)
                .isEqualTo(TOKEN_SUPPLIER_ID);
    }

    private RequestPostProcessor token(String permission) {
        return request -> {
            Jwt jwt = Jwt.withTokenValue("supplier-test-token")
                    .header("alg", "none")
                    .subject("1001")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(300))
                    .claim("name", "供应商用户")
                    .claim("supplier_id", TOKEN_SUPPLIER_ID)
                    .build();
            var authentication = new JwtAuthenticationToken(jwt,
                    List.of(new SimpleGrantedAuthority(permission)), jwt.getSubject());
            var securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            request.getSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
            return request;
        };
    }

    /**
     * 仅装配 MVC 与方法授权，避免测试上下文扫描生产基础设施。
     */
    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @EnableMethodSecurity
    static class WebConfiguration {
    }

    /**
     * 为生产 Controller 提供隔离的应用服务替身。
     */
    @Configuration(proxyBeanMethods = false)
    static class MockApplicationServices {

        @Bean
        SupplierAdmissionApplicationService admissionService() {
            return new SupplierAdmissionApplicationService(null, null, null, null, null, null);
        }

        @Bean
        PoConfirmApplicationService poConfirmService() {
            return new RecordingPoConfirmService();
        }

        @Bean
        AsnCommandApplicationService asnCommandService() {
            return new AsnCommandApplicationService(null, null, null, null, null, null);
        }

        @Bean
        AsnQueryApplicationService asnQueryService() {
            return new RecordingAsnQueryService();
        }

        @Bean
        SupplierQualityIssueApplicationService qualityService() {
            return new RecordingQualityService();
        }

        @Bean
        SupplierOperationsApplicationService operationsService() {
            return new RecordingOperationsService();
        }

        @Bean
        InboundEventReplayApplicationService inboundReplayService() {
            return new InboundEventReplayApplicationService(null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null);
        }
    }

    /**
     * 记录 PO 查询参数，验证 Controller 没有丢失令牌数据范围。
     */
    static final class RecordingPoConfirmService extends PoConfirmApplicationService {
        private Long requestedSupplierId;
        private Long supplierScopeId;

        RecordingPoConfirmService() {
            super(null, null, null, null, null, null, null, null);
        }

        @Override
        public PageResult<PoConfirmView> page(Long supplierId, Long scope, Integer status, String keyword,
                                              int pageNo, int pageSize) {
            requestedSupplierId = supplierId;
            supplierScopeId = scope;
            return new PageResult<>(pageNo, pageSize, 0, List.of());
        }
    }

    /**
     * 记录 ASN 查询参数，验证 Controller 没有丢失令牌数据范围。
     */
    static final class RecordingAsnQueryService extends AsnQueryApplicationService {
        private Long requestedSupplierId;
        private Long supplierScopeId;

        RecordingAsnQueryService() {
            super(null, null);
        }

        @Override
        public PageResult<AsnSummaryView> page(Long requestedSupplierId, Long supplierScopeId,
                                               Integer status, String keyword, int pageNo, int pageSize) {
            this.requestedSupplierId = requestedSupplierId;
            this.supplierScopeId = supplierScopeId;
            return new PageResult<>(pageNo, pageSize, 0, List.of());
        }
    }

    /**
     * 使用真实应用权限检查，同时记录质量问题查询的数据范围。
     */
    static final class RecordingQualityService extends SupplierQualityIssueApplicationService {
        private Long requestedSupplierId;
        private Long supplierScopeId;

        RecordingQualityService() {
            super(null, null, null, null, null, null);
        }

        @Override
        public PageResult<SupplierQualityIssueView> page(Long supplierId, Long scope, Integer status,
                                                         Integer severity, int page, int size) {
            requestedSupplierId = supplierId;
            supplierScopeId = scope;
            return new PageResult<>(page, size, 0, List.of());
        }
    }

    /**
     * 记录导出查询参数，验证供应商范围优先于客户端筛选参数。
     */
    static final class RecordingOperationsService extends SupplierOperationsApplicationService {
        private Long requestedSupplierId;
        private Long supplierScopeId;

        RecordingOperationsService() {
            super(null, null, null, null);
        }

        @Override
        public List<OperationViews.ExportTask> exportTasks(Long supplierId, Long scope, Integer status,
                                                           int page, int size) {
            requestedSupplierId = supplierId;
            supplierScopeId = scope;
            return List.of();
        }
    }
}
