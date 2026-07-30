package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.wms.application.query.WmsInboundQueryApplicationService;
import com.chaobo.scm.wms.application.query.WmsInboundReadModelPort;
import com.chaobo.scm.wms.infrastructure.security.WmsAccessControl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * WMS 入库工作台查询接口。
 *
 * <p>接口层只转换 HTTP 分页参数并提取过滤器写入的已验证访问上下文。功能权限、排序白名单、
 * 仓库和货主数据范围由查询应用服务统一校验，避免不同页面各自实现安全规则。
 */
@RestController
@RequestMapping("/api/wms/v1")
@PreAuthorize("isAuthenticated()")
public class WmsInboundQueryController {

    private final WmsInboundQueryApplicationService service;

    /**
     * 创建查询控制器。
     *
     * @param service WMS 入库工作台查询应用服务
     */
    public WmsInboundQueryController(WmsInboundQueryApplicationService service) {
        this.service = service;
    }

    @GetMapping("/inbound-orders")
    public ApiResponse<PageResult<WmsInboundReadModelPort.InboundSummary>> pageInbounds(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.pageInbounds(query(keyword, status, pageNo, pageSize,
            sortBy, sortDirection), WmsAccessControl.verifiedContext(authentication)), request);
    }

    @GetMapping("/inbound-orders/{inboundOrderNo}")
    public ApiResponse<WmsInboundReadModelPort.InboundDetail> inboundDetail(
            @PathVariable String inboundOrderNo,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.inboundDetail(inboundOrderNo,
            WmsAccessControl.verifiedContext(authentication)), request);
    }

    @GetMapping("/receipts")
    public ApiResponse<PageResult<WmsInboundReadModelPort.ReceiptSummary>> pageReceipts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.pageReceipts(query(keyword, status, pageNo, pageSize,
            sortBy, sortDirection), WmsAccessControl.verifiedContext(authentication)), request);
    }

    @GetMapping("/receipts/{receiptNo}")
    public ApiResponse<WmsInboundReadModelPort.ReceiptSummary> receiptDetail(
            @PathVariable String receiptNo,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.receiptDetail(receiptNo,
            WmsAccessControl.verifiedContext(authentication)), request);
    }

    @GetMapping("/inspections")
    public ApiResponse<PageResult<WmsInboundReadModelPort.InspectionSummary>> pageInspections(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.pageInspections(query(keyword, status, pageNo, pageSize,
            sortBy, sortDirection), WmsAccessControl.verifiedContext(authentication)), request);
    }

    @GetMapping("/inspections/{inspectionNo}")
    public ApiResponse<WmsInboundReadModelPort.InspectionSummary> inspectionDetail(
            @PathVariable String inspectionNo,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.inspectionDetail(inspectionNo,
            WmsAccessControl.verifiedContext(authentication)), request);
    }

    @GetMapping("/putaway-tasks")
    public ApiResponse<PageResult<WmsInboundReadModelPort.PutawaySummary>> pagePutawayTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.pagePutawayTasks(query(keyword, status, pageNo, pageSize,
            sortBy, sortDirection), WmsAccessControl.verifiedContext(authentication)), request);
    }

    @GetMapping("/putaway-tasks/{taskNo}")
    public ApiResponse<WmsInboundReadModelPort.PutawaySummary> putawayDetail(
            @PathVariable String taskNo,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.putawayDetail(taskNo,
            WmsAccessControl.verifiedContext(authentication)), request);
    }

    @GetMapping("/stocks")
    public ApiResponse<PageResult<WmsInboundReadModelPort.StockSummary>> pageStocks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.pageStocks(query(keyword, null, pageNo, pageSize,
            sortBy, sortDirection), WmsAccessControl.verifiedContext(authentication)), request);
    }

    @GetMapping("/stocks/{stockKey}")
    public ApiResponse<WmsInboundReadModelPort.StockSummary> stockDetail(
            @PathVariable String stockKey,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.stockDetail(stockKey,
            WmsAccessControl.verifiedContext(authentication)), request);
    }

    private static WmsInboundQueryApplicationService.PageQuery query(
            String keyword, Integer status, int pageNo, int pageSize,
            String sortBy, String sortDirection) {
        return new WmsInboundQueryApplicationService.PageQuery(
            keyword, status, pageNo, pageSize, sortBy, sortDirection);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"),
            request.getHeader("X-Trace-Id"));
    }
}
