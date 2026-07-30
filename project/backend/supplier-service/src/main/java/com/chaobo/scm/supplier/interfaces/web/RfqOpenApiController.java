package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.*;
import com.chaobo.scm.supplier.application.rfq.*;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.security.core.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

/**
 * RfqOpenApiController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/openapi/supplier/v1/rfqs")
public class RfqOpenApiController {

    /**
     * service（类型：{@code RfqEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final RfqEventConsumerApplicationService service;

    /**
     * 创建 RfqOpenApiController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code RfqEventConsumerApplicationService}
     */
    public RfqOpenApiController(RfqEventConsumerApplicationService service) {
        this.service = service;
    }

    /**
     * 处理当前类型职责中的操作 {@code receive}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code Request}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param auth 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping
    public ApiResponse<Void> receive(@Valid @RequestBody Request body, HttpServletRequest request, Authentication auth) {
        if (!PURCHASE.equals(request.getHeader(HEADER_X_SOURCE_SYSTEM))) {
            throw new com.chaobo.scm.common.error.BusinessException(com.chaobo.scm.common.error.ErrorCode.FORBIDDEN, "来源系统必须是PURCHASE");
        }
        for (long supplierId : body.supplierIds()) {
            service.consume(new RfqEvent(request.getHeader("X-Event-Code"), body.eventType(), "PURCHASE", body.rfqId(), body.rfqNo(), supplierId, body.quoteDeadline(), body.payload()));
        }
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
    public record Request(@NotBlank String eventType, @Positive long rfqId, @NotBlank String rfqNo, @NotEmpty List<@Positive Long> supplierIds, OffsetDateTime quoteDeadline, Map<String, Object> payload) {
    }

    /**
     * 业务常量 {@code PURCHASE}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String PURCHASE = "PURCHASE";

    /**
     * 业务常量 {@code HEADER_X_SOURCE_SYSTEM}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String HEADER_X_SOURCE_SYSTEM = "X-Source-System";
}
