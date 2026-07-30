package com.chaobo.scm.inventory.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.security.ScmAccessContexts;
import com.chaobo.scm.inventory.application.StockTransferEventApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

/**
 * StockTransferEventController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/internal/inventory/v1/transfer-events")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'inventory:*', 'inventory:transfer-event:consume')")
public class StockTransferEventController {

    /**
     * service（类型：{@code StockTransferEventApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final StockTransferEventApplicationService service;

    /**
     * 创建 StockTransferEventController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code StockTransferEventApplicationService}
     */
    public StockTransferEventController(StockTransferEventApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code Event}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<StockTransferEventApplicationService.ConsumeResult>}
     */
    @PostMapping
    public ApiResponse<StockTransferEventApplicationService.ConsumeResult> consume(@Valid @RequestBody Event body, HttpServletRequest request, Authentication authentication) {
        ScmAccessContexts.require(authentication).requireApplication(body.sourceSystem());
        return ApiResponse.success(service.consume(new StockTransferEventApplicationService.EventEnvelope(body.sourceSystem(), body.eventCode(), body.eventType(), body.transferNo(), body.qty(), body.finalReceipt(), body.version())), request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * Event。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。表达已经发生的业务事实，载荷用于跨事务或跨上下文可靠传播。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Event(@NotBlank String sourceSystem, @NotBlank String eventCode, @NotBlank String eventType, @NotBlank String transferNo, BigDecimal qty, boolean finalReceipt, @PositiveOrZero int version) {
    }
}
