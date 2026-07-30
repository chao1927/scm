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

/**
 * PurchaseSecurityWebTest。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class PurchaseSecurityWebTest {

    /**
     * context（类型：{@code AnnotationConfigWebApplicationContext}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private AnnotationConfigWebApplicationContext context;

    /**
     * mockMvc（类型：{@code MockMvc}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private MockMvc mockMvc;

    /**
     * 处理当前类型职责中的操作 {@code setUp}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context, "scm.security.hmac-secret=01234567890123456789012345678901", "scm.security.permission-namespace=purchase");
        context.register(WebConfiguration.class, ScmSecurityConfiguration.class, ProbeController.class);
        context.refresh();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(new PurchaseIdempotencyKeyFilter()).apply(springSecurity()).build();
    }

    /**
     * 处理当前类型职责中的操作 {@code tearDown}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @AfterEach
    void tearDown() {
        context.close();
    }

    /**
     * WebConfiguration。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。集中声明框架装配和运行配置，保持业务代码不感知基础设施初始化细节。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    static class WebConfiguration {
    }

    /**
     * 执行命令 {@code rejectsAnonymousAndWrongNamespaceButAllowsPurchasePermission}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectsAnonymousAndWrongNamespaceButAllowsPurchasePermission() throws Exception {
        mockMvc.perform(get("/security-probe")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/security-probe").with(jwt().authorities(new SimpleGrantedAuthority("wms:receipt:read")))).andExpect(status().isForbidden());
        mockMvc.perform(get("/security-probe").with(jwt().authorities(new SimpleGrantedAuthority("purchase:po:read")))).andExpect(status().isOk());
    }

    /**
     * 处理当前类型职责中的操作 {@code protectedWriteRequiresIdempotencyHeader}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void protectedWriteRequiresIdempotencyHeader() throws Exception {
        var authorized = jwt().authorities(new SimpleGrantedAuthority("purchase:po:write"));
        mockMvc.perform(post("/security-probe").with(authorized)).andExpect(status().isBadRequest());
        mockMvc.perform(post("/security-probe").header("X-Idempotency-Key", "SEC-TEST-1").with(jwt().authorities(new SimpleGrantedAuthority("purchase:po:write")))).andExpect(status().isOk());
    }

    /**
     * ProbeController。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    @RestController
    static class ProbeController {

        /**
         * 处理当前类型职责中的操作 {@code probe}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        @GetMapping("/security-probe")
        String probe() {
            return "ok";
        }

        /**
         * 处理当前类型职责中的操作 {@code writeProbe}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        @org.springframework.web.bind.annotation.PostMapping("/security-probe")
        String writeProbe() {
            return "ok";
        }
    }
}
