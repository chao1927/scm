package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.wms.application.query.WmsExceptionQueryApplicationService;
import com.chaobo.scm.wms.application.query.WmsExceptionReadModelPort;
import com.chaobo.scm.wms.infrastructure.security.WmsAccessControl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * WMS 退货、盘点和异常查询接口。
 */
@RestController
@RequestMapping("/api/wms/v1")
@PreAuthorize("isAuthenticated()")
public class WmsExceptionQueryController {
    private final WmsExceptionQueryApplicationService service;
    public WmsExceptionQueryController(WmsExceptionQueryApplicationService service) {
        this.service = service;
    }

    @GetMapping("/return-receipts")
    public ApiResponse<PageResult<WmsExceptionReadModelPort.ReturnSummary>> returns(
            @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize,
            Authentication auth, HttpServletRequest request) {
        return ok(service.returns(query(keyword, status, pageNo, pageSize),
            WmsAccessControl.verifiedContext(auth)), request);
    }
    @GetMapping("/return-receipts/{no}")
    public ApiResponse<WmsExceptionReadModelPort.ReturnSummary> returnDetail(
            @PathVariable String no, Authentication auth, HttpServletRequest request) {
        return ok(service.returnDetail(no, WmsAccessControl.verifiedContext(auth)), request);
    }
    @GetMapping("/stocktakes")
    public ApiResponse<PageResult<WmsExceptionReadModelPort.StocktakeSummary>> stocktakes(
            @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize,
            Authentication auth, HttpServletRequest request) {
        return ok(service.stocktakes(query(keyword, status, pageNo, pageSize),
            WmsAccessControl.verifiedContext(auth)), request);
    }
    @GetMapping("/stocktakes/{no}")
    public ApiResponse<WmsExceptionReadModelPort.StocktakeSummary> stocktakeDetail(
            @PathVariable String no, Authentication auth, HttpServletRequest request) {
        return ok(service.stocktakeDetail(no, WmsAccessControl.verifiedContext(auth)), request);
    }
    @GetMapping("/warehouse-exceptions")
    public ApiResponse<PageResult<WmsExceptionReadModelPort.ExceptionSummary>> exceptions(
            @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize,
            Authentication auth, HttpServletRequest request) {
        return ok(service.exceptions(query(keyword, status, pageNo, pageSize),
            WmsAccessControl.verifiedContext(auth)), request);
    }
    @GetMapping("/warehouse-exceptions/{no}")
    public ApiResponse<WmsExceptionReadModelPort.ExceptionSummary> exceptionDetail(
            @PathVariable String no, Authentication auth, HttpServletRequest request) {
        return ok(service.exceptionDetail(no, WmsAccessControl.verifiedContext(auth)), request);
    }
    private static WmsExceptionQueryApplicationService.PageQuery query(
            String keyword, Integer status, int pageNo, int pageSize) {
        return new WmsExceptionQueryApplicationService.PageQuery(keyword, status, pageNo, pageSize);
    }
    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"),
            request.getHeader("X-Trace-Id"));
    }
}
