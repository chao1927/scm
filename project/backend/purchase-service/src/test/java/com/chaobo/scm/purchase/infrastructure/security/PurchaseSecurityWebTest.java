package com.chaobo.scm.purchase.infrastructure.security;

import com.chaobo.scm.common.security.ScmSecurityConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PurchaseSecurityWebTest {
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                "scm.security.hmac-secret=01234567890123456789012345678901",
                "scm.security.permission-namespace=purchase");
        context.register(WebConfiguration.class, ScmSecurityConfiguration.class, ProbeController.class);
        context.refresh();
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new PurchaseIdempotencyKeyFilter())
                .apply(springSecurity())
                .build();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    static class WebConfiguration {
    }

    @Test
    void rejectsAnonymousAndWrongNamespaceButAllowsPurchasePermission() throws Exception {
        mockMvc.perform(get("/security-probe"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/security-probe").with(jwt()
                        .authorities(new SimpleGrantedAuthority("wms:receipt:read"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/security-probe").with(jwt()
                        .authorities(new SimpleGrantedAuthority("purchase:po:read"))))
                .andExpect(status().isOk());
    }

    @Test
    void protectedWriteRequiresIdempotencyHeader() throws Exception {
        var authorized = jwt().authorities(new SimpleGrantedAuthority("purchase:po:write"));

        mockMvc.perform(post("/security-probe").with(authorized))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/security-probe")
                        .header("X-Idempotency-Key", "SEC-TEST-1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("purchase:po:write"))))
                .andExpect(status().isOk());
    }

    @RestController
    static class ProbeController {
        @GetMapping("/security-probe")
        String probe() {
            return "ok";
        }

        @org.springframework.web.bind.annotation.PostMapping("/security-probe")
        String writeProbe() {
            return "ok";
        }
    }
}
