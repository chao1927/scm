package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.*;
import com.chaobo.scm.supplier.application.item.SupplierItemHistoryQueryApplicationService;
import jakarta.servlet.http.*;
import jakarta.validation.constraints.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * SupplierItemHistoryController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/supplier/v1")
public class SupplierItemHistoryController {

    /**
     * service（类型：{@code SupplierItemHistoryQueryApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierItemHistoryQueryApplicationService service;

    /**
     * 创建 SupplierItemHistoryController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code SupplierItemHistoryQueryApplicationService}
     */
    public SupplierItemHistoryController(SupplierItemHistoryQueryApplicationService service) {
        this.service = service;
    }

    /**
     * 处理当前类型职责中的操作 {@code conditions}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param itemId 业务或技术标识，类型为 {@code long}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<SupplierItemHistoryQueryApplicationService.Condition>>}
     */
    @GetMapping("/items/{itemId}/condition-history")
    public ApiResponse<List<SupplierItemHistoryQueryApplicationService.Condition>> conditions(@PathVariable long itemId, @AuthenticationPrincipal Jwt jwt, HttpServletRequest r) {
        return ApiResponse.success(service.conditions(itemId, scope(jwt)), r.getHeader("X-Request-Id"), r.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code prices}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<SupplierItemHistoryQueryApplicationService.Price>>}
     */
    @GetMapping("/suppliers/{supplierId}/skus/{skuCode}/price-snapshots")
    public ApiResponse<List<SupplierItemHistoryQueryApplicationService.Price>> prices(@PathVariable long supplierId, @PathVariable @NotBlank String skuCode, @AuthenticationPrincipal Jwt jwt, HttpServletRequest r) {
        return ApiResponse.success(service.prices(supplierId, skuCode, scope(jwt)), r.getHeader("X-Request-Id"), r.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code scope}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    private Long scope(Jwt jwt) {
        Number n = jwt.hasClaim("supplier_id") ? jwt.getClaim("supplier_id") : null;
        return n == null ? null : n.longValue();
    }
}
