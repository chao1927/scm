package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.purchase.application.workbench.PurchaseTodoView;
import com.chaobo.scm.purchase.application.workbench.PurchaseWorkbenchQueries;
import com.chaobo.scm.purchase.application.workbench.PurchaseWorkbenchQueryApplicationService;
import com.chaobo.scm.purchase.application.workbench.PurchaseWorkbenchSummaryView;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

/**
 * 采购工作台 HTTP 接口。
 *
 * <p>负责查询参数绑定、可信认证上下文提取和统一响应封装，不承载指标口径或数据范围规则。
 */
@RestController
@RequestMapping("/api/purchase/v1/workbench")
public class PurchaseWorkbenchController {

    private final PurchaseWorkbenchQueryApplicationService queryService;

    /**
     * 创建采购工作台控制器。
     *
     * @param queryService 工作台查询应用服务
     */
    public PurchaseWorkbenchController(
            PurchaseWorkbenchQueryApplicationService queryService
    ) {
        this.queryService = queryService;
    }

    /**
     * 查询待办、交期、价格、订单执行和异常汇总。
     *
     * @param purchaseOrgId 采购组织
     * @param purchaseGroupId 采购组
     * @param scopeMode 数据范围模式
     * @param createdFrom 创建时间下界
     * @param createdTo 创建时间上界
     * @param request HTTP 请求
     * @param authentication 认证信息
     * @return 工作台汇总
     */
    @GetMapping("/summary")
    public ApiResponse<PurchaseWorkbenchSummaryView> summary(
            @RequestParam(required = false) Long purchaseOrgId,
            @RequestParam(required = false) Long purchaseGroupId,
            @RequestParam(defaultValue = "ORGANIZATION") String scopeMode,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime createdFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime createdTo,
            HttpServletRequest request,
            Authentication authentication
    ) {
        var query = new PurchaseWorkbenchQueries.SummaryQuery(
                purchaseOrgId, purchaseGroupId, scopeMode, createdFrom, createdTo);
        return ok(queryService.summary(query, access(authentication)), request);
    }

    /**
     * 分页查询当前用户可见的采购待办。
     *
     * @param purchaseOrgId 采购组织
     * @param purchaseGroupId 采购组
     * @param scopeMode 数据范围模式
     * @param createdFrom 创建时间下界
     * @param createdTo 创建时间上界
     * @param todoType 待办类型
     * @param pageNo 页码
     * @param pageSize 每页条数
     * @param sortField 排序字段
     * @param sortOrder 排序方向
     * @param request HTTP 请求
     * @param authentication 认证信息
     * @return 待办分页
     */
    @GetMapping("/todos")
    public ApiResponse<PageResult<PurchaseTodoView>> todos(
            @RequestParam(required = false) Long purchaseOrgId,
            @RequestParam(required = false) Long purchaseGroupId,
            @RequestParam(defaultValue = "ORGANIZATION") String scopeMode,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime createdFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime createdTo,
            @RequestParam(required = false) String todoType,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "updatedAt") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder,
            HttpServletRequest request,
            Authentication authentication
    ) {
        var query = new PurchaseWorkbenchQueries.TodoPageQuery(
                purchaseOrgId,
                purchaseGroupId,
                scopeMode,
                createdFrom,
                createdTo,
                todoType,
                pageNo,
                pageSize,
                sortField,
                sortOrder
        );
        return ok(queryService.todos(query, access(authentication)), request);
    }

    private static ScmAccessContext access(Authentication authentication) {
        if (authentication == null
                || !(authentication.getDetails() instanceof ScmAccessContext access)) {
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED, "当前请求没有有效访问令牌");
        }
        return access;
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(
                data,
                request.getHeader("X-Request-Id"),
                request.getHeader("X-Trace-Id")
        );
    }
}
