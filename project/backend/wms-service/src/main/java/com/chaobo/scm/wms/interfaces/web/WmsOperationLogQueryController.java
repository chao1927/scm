package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.operations.WmsOperationLogQueryApplicationService;
import com.chaobo.scm.wms.infrastructure.persistence.WmsOperationLogQueryMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/** 不依赖消息投递能力的 WMS 操作日志查询接口。 */
@RestController
@RequestMapping("/api/wms/v1/operations/operation-logs")
@PreAuthorize("hasAnyAuthority('*', 'wms:*', 'wms:operation-log:read')")
public class WmsOperationLogQueryController {

    private final WmsOperationLogQueryApplicationService service;

    public WmsOperationLogQueryController(WmsOperationLogQueryApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<WmsOperationLogQueryMapper.OperationLogView>> operationLogs(
            @RequestParam(defaultValue = "100") int limit, HttpServletRequest request) {
        return ApiResponse.success(service.list(limit), request.getHeader("X-Request-Id"),
                request.getHeader("X-Trace-Id"));
    }
}
