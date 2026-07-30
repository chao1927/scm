package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.order.*;
import com.chaobo.scm.supplier.application.shared.CommandResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

/**
 * PurchaseOrderOpenApiController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/openapi/supplier/v1/purchase-orders")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "scm.legacy-http-event-ingress.enabled", havingValue = "true")
public class PurchaseOrderOpenApiController {

    /**
     * consumer（类型：{@code PurchaseOrderEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseOrderEventConsumerApplicationService consumer;

    /**
     * 创建 PurchaseOrderOpenApiController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param consumer 业务处理参数或成员，类型为 {@code PurchaseOrderEventConsumerApplicationService}
     */
    public PurchaseOrderOpenApiController(PurchaseOrderEventConsumerApplicationService consumer) {
        this.consumer = consumer;
    }

    /**
     * 处理当前类型职责中的操作 {@code receive}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code ReceiveRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping
    public ApiResponse<CommandResult> receive(@Valid @RequestBody ReceiveRequest body, HttpServletRequest request) {
        source(request);
        return ok(consumer.consume(event(request, "PurchaseOrderReleased", body.purchaseOrderId(), body.purchaseOrderNo(), body.supplierId(), body.confirmDeadline(), body.lines(), body.sourceVersion(), null)), request);
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code EventRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/events")
    public ApiResponse<CommandResult> consume(@Valid @RequestBody EventRequest body, HttpServletRequest request) {
        source(request);
        return ok(consumer.consume(event(request, body.eventType(), body.purchaseOrderId(), body.purchaseOrderNo(), body.supplierId(), body.confirmDeadline(), body.lines(), body.sourceVersion(), body.reason())), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code event}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param deadline 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param lines 业务处理参数或成员，类型为 {@code List<SourceLine>}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PurchaseOrderEvent}
     */
    private PurchaseOrderEvent event(HttpServletRequest request, String type, long id, String no, long supplierId, OffsetDateTime deadline, List<SourceLine> lines, int version, String reason) {
        return new PurchaseOrderEvent(request.getHeader("X-Event-Code"), type, id, no, supplierId, deadline, lines == null ? List.of() : lines.stream().map(line -> new PurchaseOrderEvent.Line(line.skuCode(), line.orderQty(), line.requestedDeliveryDate())).toList(), version, reason);
    }

    /**
     * 处理当前类型职责中的操作 {@code source}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     */
    private void source(HttpServletRequest request) {
        if (!PURCHASE.equals(request.getHeader(HEADER_X_SOURCE_SYSTEM))) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "来源系统必须是PURCHASE");
        }
        if (request.getHeader(HEADER_X_EVENT_CODE) == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "缺少X-Event-Code");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code ok}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param result 处理结果，类型为 {@code CommandResult}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    private ApiResponse<CommandResult> ok(CommandResult result, HttpServletRequest request) {
        return ApiResponse.success(result, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * ReceiveRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ReceiveRequest(@Positive long purchaseOrderId, @NotBlank String purchaseOrderNo, @Positive long supplierId, OffsetDateTime confirmDeadline, @NotEmpty List<@Valid SourceLine> lines, @Positive int sourceVersion) {
    }

    /**
     * EventRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record EventRequest(@NotBlank String eventType, @Positive long purchaseOrderId, String purchaseOrderNo, long supplierId, OffsetDateTime confirmDeadline, List<@Valid SourceLine> lines, @Positive int sourceVersion, String reason) {
    }

    /**
     * SourceLine。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record SourceLine(@NotBlank String skuCode, @NotNull @Positive BigDecimal orderQty, LocalDate requestedDeliveryDate) {
    }

    /**
     * 业务常量 {@code HEADER_X_EVENT_CODE}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String HEADER_X_EVENT_CODE = "X-Event-Code";

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
