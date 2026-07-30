package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.*;
import com.chaobo.scm.supplier.application.quality.*;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

/**
 * QualitySourceEventController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/internal/supplier/v1/quality/events")
public class QualitySourceEventController {

    /**
     * service（类型：{@code QualitySourceEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final QualitySourceEventConsumerApplicationService service;

    /**
     * 创建 QualitySourceEventController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code QualitySourceEventConsumerApplicationService}
     */
    public QualitySourceEventController(QualitySourceEventConsumerApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code Request}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping
    public ApiResponse<Void> consume(@Valid @RequestBody Request body, HttpServletRequest request) {
        String source = request.getHeader("X-Source-System");
        service.consume(new QualitySourceEvent(source, request.getHeader("X-Event-Code"), body.eventType(), body.supplierId(), body.sourceNo(), body.issueType(), body.severity(), body.description()));
        return ApiResponse.success(null, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * Request。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Request(@NotBlank String eventType, @Positive long supplierId, @NotBlank String sourceNo, @NotBlank String issueType, @Min(1) @Max(4) int severity, @NotBlank String description) {
    }
}
