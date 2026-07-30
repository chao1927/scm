package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.supplier.application.report.SupplierReportApplicationService;
import com.chaobo.scm.supplier.application.report.SupplierReportViews;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * SupplierReportController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/supplier/v1/reports")
public class SupplierReportController {

    /**
     * service（类型：{@code SupplierReportApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierReportApplicationService service;

    /**
     * 创建 SupplierReportController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code SupplierReportApplicationService}
     */
    public SupplierReportController(SupplierReportApplicationService service) {
        this.service = service;
    }

    /**
     * 处理当前类型职责中的操作 {@code fulfillment}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<SupplierReportViews.Fulfillment>}
     */
    @GetMapping("/fulfillment")
    public ApiResponse<SupplierReportViews.Fulfillment> fulfillment(@RequestParam(required = false) Long supplierId, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return ok(service.fulfillment(supplierId, scope(jwt)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code exceptions}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<SupplierReportViews.ExceptionOverview>}
     */
    @GetMapping("/exceptions")
    public ApiResponse<SupplierReportViews.ExceptionOverview> exceptions(@RequestParam(required = false) Long supplierId, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return ok(service.exceptions(supplierId, scope(jwt)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code ok}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code T}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<T>}
     */
    private <T> ApiResponse<T> ok(T value, HttpServletRequest request) {
        return ApiResponse.success(value, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code scope}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    private Long scope(Jwt jwt) {
        if (jwt == null || !jwt.hasClaim(SUPPLIER_ID)) {
            return null;
        }
        Number value = jwt.getClaim("supplier_id");
        return value == null ? null : value.longValue();
    }

    /**
     * 业务常量 {@code SUPPLIER_ID}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String SUPPLIER_ID = "supplier_id";
}
