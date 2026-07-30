package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.wms.application.query.WmsOutboundQueryApplicationService;
import com.chaobo.scm.wms.application.query.WmsOutboundReadModelPort;
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
 * WMS 出库、波次、拣货、包装和交接统一查询接口。
 */
@RestController
@RequestMapping("/api/wms/v1")
@PreAuthorize("isAuthenticated()")
public class WmsOutboundQueryController {

    private final WmsOutboundQueryApplicationService service;

    public WmsOutboundQueryController(WmsOutboundQueryApplicationService service) {
        this.service = service;
    }

    @GetMapping("/outbound-orders")
    public ApiResponse<PageResult<WmsOutboundReadModelPort.OutboundSummary>> outbounds(
            @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication auth, HttpServletRequest request) {
        return ok(service.pageOutbounds(query(keyword, status, pageNo, pageSize, sortBy, sortDirection),
            WmsAccessControl.verifiedContext(auth)), request);
    }

    @GetMapping("/outbound-orders/{no}")
    public ApiResponse<WmsOutboundReadModelPort.OutboundSummary> outbound(
            @PathVariable String no, Authentication auth, HttpServletRequest request) {
        return ok(service.outboundDetail(no, WmsAccessControl.verifiedContext(auth)), request);
    }

    @GetMapping("/waves")
    public ApiResponse<PageResult<WmsOutboundReadModelPort.WaveSummary>> waves(
            @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication auth, HttpServletRequest request) {
        return ok(service.pageWaves(query(keyword, status, pageNo, pageSize, sortBy, sortDirection),
            WmsAccessControl.verifiedContext(auth)), request);
    }

    @GetMapping("/waves/{no}")
    public ApiResponse<WmsOutboundReadModelPort.WaveSummary> wave(
            @PathVariable String no, Authentication auth, HttpServletRequest request) {
        return ok(service.waveDetail(no, WmsAccessControl.verifiedContext(auth)), request);
    }

    @GetMapping("/picking-orders")
    public ApiResponse<PageResult<WmsOutboundReadModelPort.PickSummary>> picks(
            @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication auth, HttpServletRequest request) {
        return ok(service.pagePicks(query(keyword, status, pageNo, pageSize, sortBy, sortDirection),
            WmsAccessControl.verifiedContext(auth)), request);
    }

    @GetMapping("/picking-orders/{no}")
    public ApiResponse<WmsOutboundReadModelPort.PickSummary> pick(
            @PathVariable String no, Authentication auth, HttpServletRequest request) {
        return ok(service.pickDetail(no, WmsAccessControl.verifiedContext(auth)), request);
    }

    @GetMapping("/pack-orders")
    public ApiResponse<PageResult<WmsOutboundReadModelPort.PackingSummary>> packings(
            @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication auth, HttpServletRequest request) {
        return ok(service.pagePackings(query(keyword, status, pageNo, pageSize, sortBy, sortDirection),
            WmsAccessControl.verifiedContext(auth)), request);
    }

    @GetMapping("/pack-orders/{no}")
    public ApiResponse<WmsOutboundReadModelPort.PackingSummary> packing(
            @PathVariable String no, Authentication auth, HttpServletRequest request) {
        return ok(service.packingDetail(no, WmsAccessControl.verifiedContext(auth)), request);
    }

    @GetMapping("/shipments")
    public ApiResponse<PageResult<WmsOutboundReadModelPort.ShipmentSummary>> shipments(
            @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication auth, HttpServletRequest request) {
        return ok(service.pageShipments(query(keyword, status, pageNo, pageSize, sortBy, sortDirection),
            WmsAccessControl.verifiedContext(auth)), request);
    }

    @GetMapping("/shipments/{no}")
    public ApiResponse<WmsOutboundReadModelPort.ShipmentSummary> shipment(
            @PathVariable String no, Authentication auth, HttpServletRequest request) {
        return ok(service.shipmentDetail(no, WmsAccessControl.verifiedContext(auth)), request);
    }

    private static WmsOutboundQueryApplicationService.PageQuery query(
            String keyword, Integer status, int pageNo, int pageSize, String sortBy, String direction) {
        return new WmsOutboundQueryApplicationService.PageQuery(
            keyword, status, pageNo, pageSize, sortBy, direction);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"),
            request.getHeader("X-Trace-Id"));
    }
}
