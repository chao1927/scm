package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.score.*;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * PerformanceFactOpenApiController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/internal/supplier/v1/performance/events")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "scm.legacy-http-event-ingress.enabled", havingValue = "true")
public class PerformanceFactOpenApiController {

    /**
     * service（类型：{@code PerformanceFactEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final PerformanceFactEventConsumerApplicationService service;

    /**
     * 创建 PerformanceFactOpenApiController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code PerformanceFactEventConsumerApplicationService}
     */
    public PerformanceFactOpenApiController(PerformanceFactEventConsumerApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param b 业务处理参数或成员，类型为 {@code Request}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping
    public ApiResponse<Void> consume(@Valid @RequestBody Request b, HttpServletRequest r) {
        String source = r.getHeader("X-Source-System"), code = r.getHeader("X-Event-Code");
        if (source == null || code == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "缺少事件请求头");
        }
        service.consume(new PerformanceFactEvent(source, code, b.eventType(), b.supplierId(), b.sourceNo(), b.metricValue(), b.occurredAt(), b.payload()));
        return ApiResponse.success(null, r.getHeader("X-Request-Id"), r.getHeader("X-Trace-Id"));
    }

    /**
     * Request。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Request(@NotBlank String eventType, @Positive long supplierId, String sourceNo, @NotNull BigDecimal metricValue, @NotNull OffsetDateTime occurredAt, Map<String, Object> payload) {
    }
}
