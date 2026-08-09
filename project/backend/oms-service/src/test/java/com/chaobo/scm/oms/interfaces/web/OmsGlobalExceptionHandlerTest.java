package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OMS 接口异常到 HTTP 协议的映射测试。
 *
 * <p>业务校验失败属于调用方可修正的请求问题，不能退化为 HTTP 500，否则工作台会把参数错误误判为服务故障。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class OmsGlobalExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new RequestValidationProbeController())
        .setControllerAdvice(new OmsGlobalExceptionHandler())
        .build();

    /**
     * 验证分页等输入校验异常返回 HTTP 400 并保留可展示的业务消息。
     */
    @Test
    void shouldMapValidationFailureToBadRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "request-1");
        request.addHeader("X-Trace-Id", "trace-1");

        var response = new OmsGlobalExceptionHandler().handleBusiness(
            new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法"),
            request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().message()).isEqualTo("分页参数不合法");
        assertThat(response.getBody().requestId()).isEqualTo("request-1");
        assertThat(response.getBody().traceId()).isEqualTo("trace-1");
    }

    /**
     * 验证非数字分页参数在真实 MVC 异常解析链路中返回 HTTP 400。
     *
     * @throws Exception MockMvc 请求执行异常
     */
    @Test
    void shouldMapRequestParameterTypeMismatchToBadRequest() throws Exception {
        mockMvc.perform(get("/validation-probe").param("pageSize", "abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    /**
     * 验证缺少必填请求参数时返回 HTTP 400。
     *
     * @throws Exception MockMvc 请求执行异常
     */
    @Test
    void shouldMapMissingRequestParameterToBadRequest() throws Exception {
        mockMvc.perform(get("/validation-probe"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    /**
     * 验证不可解析的 JSON 请求体返回 HTTP 400。
     *
     * @throws Exception MockMvc 请求执行异常
     */
    @Test
    void shouldMapUnreadableJsonToBadRequest() throws Exception {
        mockMvc.perform(post("/validation-probe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    /**
     * 验证不支持的 HTTP 方法保持为 405，而不是被系统异常兜底改成 500。
     *
     * @throws Exception MockMvc 请求执行异常
     */
    @Test
    void shouldPreserveMethodNotAllowedStatus() throws Exception {
        mockMvc.perform(post("/validation-probe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .put("/validation-probe"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    /**
     * 验证不支持的请求媒体类型保持为 415。
     *
     * @throws Exception MockMvc 请求执行异常
     */
    @Test
    void shouldPreserveUnsupportedMediaTypeStatus() throws Exception {
        mockMvc.perform(post("/validation-probe")
                .contentType(MediaType.TEXT_PLAIN)
                .content("not-json"))
            .andExpect(status().isUnsupportedMediaType())
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    /**
     * 仅用于验证 Spring MVC 请求解析异常是否经过 OMS 全局异常处理器。
     */
    @RestController
    private static class RequestValidationProbeController {

        /**
         * 接收必须为整数的分页参数。
         *
         * @param pageSize 分页大小
         */
        @GetMapping("/validation-probe")
        void query(@RequestParam int pageSize) {
            // MockMvc 仅验证请求参数解析，无需执行业务逻辑。
        }

        /**
         * 接收必须为合法 JSON 的请求体。
         *
         * @param body JSON 请求体
         */
        @PostMapping("/validation-probe")
        void command(@RequestBody Map<String, Object> body) {
            // MockMvc 仅验证请求体解析，无需执行业务逻辑。
        }
    }
}
