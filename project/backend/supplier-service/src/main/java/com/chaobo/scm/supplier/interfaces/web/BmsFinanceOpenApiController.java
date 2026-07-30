package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.finance.*;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

/**
 * BmsFinanceOpenApiController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/internal/supplier/v1/bms/events")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "scm.legacy-http-event-ingress.enabled", havingValue = "true")
public class BmsFinanceOpenApiController {

    /**
     * service（类型：{@code BmsFinanceEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final BmsFinanceEventConsumerApplicationService service;

    /**
     * 创建 BmsFinanceOpenApiController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code BmsFinanceEventConsumerApplicationService}
     */
    public BmsFinanceOpenApiController(BmsFinanceEventConsumerApplicationService service) {
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
        if (!BMS.equals(r.getHeader(HEADER_X_SOURCE_SYSTEM))) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "来源系统必须为BMS");
        }
        String code = r.getHeader("X-Event-Code");
        if (code == null || code.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "缺少事件编码");
        }
        service.consume(new BmsFinanceEvent(code, b.eventType(), b.statementNo(), b.supplierId(), b.currency(), b.amount(), b.sourceVersion(), b.invoiceId(), b.passed(), b.message()));
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
    public record Request(@NotBlank String eventType, String statementNo, long supplierId, String currency, BigDecimal amount, int sourceVersion, Long invoiceId, boolean passed, String message) {
    }

    /**
     * 业务常量 {@code BMS}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String BMS = "BMS";

    /**
     * 业务常量 {@code HEADER_X_SOURCE_SYSTEM}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String HEADER_X_SOURCE_SYSTEM = "X-Source-System";
}
