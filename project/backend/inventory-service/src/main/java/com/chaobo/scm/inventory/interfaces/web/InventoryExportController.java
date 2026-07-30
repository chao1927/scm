package com.chaobo.scm.inventory.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.security.ScmAccessContexts;
import com.chaobo.scm.inventory.application.export.InventoryExportApplicationService;
import com.chaobo.scm.inventory.application.export.InventoryExportTask;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存异步导出 HTTP 入口。
 *
 * @author SCM Team
 */
@RestController
@RequestMapping("/api/inventory/v1/exports")
public class InventoryExportController {

    private final InventoryExportApplicationService service;

    public InventoryExportController(InventoryExportApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<InventoryExportTask> create(
            @Valid @RequestBody CreateRequest body,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.create(
                new InventoryExportApplicationService.CreateCommand(
                        body.exportType(),
                        body.ownerId(),
                        body.warehouseId(),
                        body.query()),
                idempotencyKey,
                ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping
    public ApiResponse<List<InventoryExportTask>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.list(
                status,
                pageNo,
                pageSize,
                ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/{taskNo}")
    public ApiResponse<InventoryExportTask> detail(
            @PathVariable String taskNo,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.detail(taskNo, ScmAccessContexts.require(authentication)), request);
    }

    @PostMapping("/{taskNo}/retry")
    public ApiResponse<Void> retry(
            @PathVariable String taskNo,
            @Valid @RequestBody RetryRequest body,
            Authentication authentication,
            HttpServletRequest request) {
        service.retry(
                taskNo,
                body.version(),
                ScmAccessContexts.require(authentication));
        return ok(null, request);
    }

    @GetMapping("/{taskNo}/file")
    public ResponseEntity<byte[]> download(
            @PathVariable String taskNo,
            Authentication authentication) {
        InventoryExportApplicationService.ExportFile file =
                service.download(taskNo, ScmAccessContexts.require(authentication));
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(file.fileName(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.bytes());
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(
                data,
                request.getHeader("X-Request-Id"),
                request.getHeader("X-Trace-Id"));
    }

    public record CreateRequest(
            @NotBlank String exportType,
            Long ownerId,
            Long warehouseId,
            Map<String, Object> query) {
    }

    public record RetryRequest(@PositiveOrZero int version) {
    }
}
