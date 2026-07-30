package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.security.ScmAccessContexts;
import com.chaobo.scm.oms.application.OmsOperationsQueryApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * OMS 运营读模型查询接口。
 *
 * <p>列表和详情统一调用只读应用服务，身份信息仅从认证上下文获取，不接受客户端
 * 伪造组织、货主或仓库范围。
 */
@RestController
@RequestMapping("/api/oms/v1")
public class OmsOperationsQueryController {

    private final OmsOperationsQueryApplicationService service;

    public OmsOperationsQueryController(OmsOperationsQueryApplicationService service) {
        this.service = service;
    }

    @GetMapping("/audit-results")
    public ApiResponse<PageResult<OmsOperationsQueryApplicationService.AuditView>> audits(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.audits(query(keyword, status, pageNo, pageSize),
                ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/audit-results/{id}")
    public ApiResponse<OmsOperationsQueryApplicationService.AuditView> audit(
            @PathVariable String id, Authentication authentication,
            HttpServletRequest request) {
        return ok(service.audit(id, ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/reservations")
    public ApiResponse<PageResult<OmsOperationsQueryApplicationService.ReservationView>>
    reservations(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.reservations(query(keyword, status, pageNo, pageSize),
                ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/reservations/{no}")
    public ApiResponse<OmsOperationsQueryApplicationService.ReservationView> reservation(
            @PathVariable String no, Authentication authentication,
            HttpServletRequest request) {
        return ok(service.reservation(no, ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/cancel-requests")
    public ApiResponse<PageResult<OmsOperationsQueryApplicationService.CancellationView>>
    cancellations(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.cancellations(query(keyword, status, pageNo, pageSize),
                ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/cancel-requests/{no}")
    public ApiResponse<OmsOperationsQueryApplicationService.CancellationView> cancellation(
            @PathVariable String no, Authentication authentication,
            HttpServletRequest request) {
        return ok(service.cancellation(no, ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/after-sales")
    public ApiResponse<PageResult<OmsOperationsQueryApplicationService.AfterSaleView>>
    afterSales(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.afterSales(query(keyword, status, pageNo, pageSize),
                ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/after-sales/{no}")
    public ApiResponse<OmsOperationsQueryApplicationService.AfterSaleView> afterSale(
            @PathVariable String no, Authentication authentication,
            HttpServletRequest request) {
        return ok(service.afterSale(no, ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/exceptions")
    public ApiResponse<PageResult<OmsOperationsQueryApplicationService.ExceptionView>>
    exceptions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.exceptions(query(keyword, status, pageNo, pageSize),
                ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/exceptions/{no}")
    public ApiResponse<OmsOperationsQueryApplicationService.ExceptionView> exception(
            @PathVariable String no, Authentication authentication,
            HttpServletRequest request) {
        return ok(service.exception(no, ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/operation-logs")
    public ApiResponse<PageResult<OmsOperationsQueryApplicationService.OperationLogView>>
    operationLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.operationLogs(query(keyword, status, pageNo, pageSize),
                ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/operation-logs/{id}")
    public ApiResponse<OmsOperationsQueryApplicationService.OperationLogView> operationLog(
            @PathVariable long id, Authentication authentication,
            HttpServletRequest request) {
        return ok(service.operationLog(id, ScmAccessContexts.require(authentication)), request);
    }

    private static OmsOperationsQueryApplicationService.PageQuery query(
            String keyword, Integer status, int pageNo, int pageSize) {
        return new OmsOperationsQueryApplicationService.PageQuery(
                keyword, status, pageNo, pageSize);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"),
                request.getHeader("X-Trace-Id"));
    }
}
