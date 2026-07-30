package com.chaobo.scm.inventory.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.security.ScmAccessContexts;
import com.chaobo.scm.inventory.application.InventoryOperationReadModelPort;
import com.chaobo.scm.inventory.application.InventoryOperationsQueryApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存运营列表与指标查询入口。
 *
 * <p>控制器只做协议转换；功能权限和货主/仓库数据范围由应用服务统一收敛。
 *
 * @author SCM Team
 */
@RestController
@RequestMapping("/api/inventory/v1")
public class InventoryOperationsQueryController {

    private final InventoryOperationsQueryApplicationService service;

    public InventoryOperationsQueryController(
            InventoryOperationsQueryApplicationService service) {
        this.service = service;
    }

    @GetMapping("/reservations")
    public ApiResponse<PageResult<InventoryOperationReadModelPort.ReservationView>> reservations(
            CommonQuery query,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.reservations(query.operationQuery(), ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/freezes")
    public ApiResponse<PageResult<InventoryOperationReadModelPort.FreezeView>> freezes(
            CommonQuery query,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.freezes(query.operationQuery(), ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/adjustments")
    public ApiResponse<PageResult<InventoryOperationReadModelPort.AdjustmentView>> adjustments(
            CommonQuery query,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.adjustments(query.operationQuery(), ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/event-logs")
    public ApiResponse<PageResult<InventoryOperationReadModelPort.EventLogView>> eventLogs(
            CommonQuery query,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.eventLogs(query.operationQuery(), ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/operation-logs")
    public ApiResponse<PageResult<InventoryOperationReadModelPort.OperationLogView>> operationLogs(
            CommonQuery query,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.operationLogs(query.operationQuery(), ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/metrics")
    public ApiResponse<PageResult<InventoryOperationReadModelPort.MetricView>> metrics(
            @RequestParam InventoryOperationReadModelPort.MetricType metricType,
            CommonQuery query,
            @RequestParam(defaultValue = "90") int slowMovingDays,
            @RequestParam(defaultValue = "30") int expiryWarningDays,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.metrics(
                metricType,
                new InventoryOperationsQueryApplicationService.MetricQuery(
                        query.ownerId(), query.warehouseId(), query.sku(), query.batchNo(),
                        slowMovingDays, expiryWarningDays, query.pageNo(), query.pageSize()),
                ScmAccessContexts.require(authentication)), request);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(
                data,
                request.getHeader("X-Request-Id"),
                request.getHeader("X-Trace-Id"));
    }

    /**
     * 五类运营页面共享的白名单查询条件。
     */
    public record CommonQuery(
            Long ownerId,
            Long warehouseId,
            String sku,
            String batchNo,
            Integer status,
            int pageNo,
            int pageSize,
            String sortBy,
            String sortDirection) {

        private InventoryOperationsQueryApplicationService.OperationQuery operationQuery() {
            return new InventoryOperationsQueryApplicationService.OperationQuery(
                    ownerId, warehouseId, sku, batchNo, status,
                    pageNo, pageSize, sortBy, sortDirection);
        }
    }
}
