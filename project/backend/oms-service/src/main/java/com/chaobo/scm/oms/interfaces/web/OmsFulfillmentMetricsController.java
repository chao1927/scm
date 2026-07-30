package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.security.ScmAccessContexts;
import com.chaobo.scm.oms.application.OmsFulfillmentMetricsApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/** OMS 履约指标查询、异步导出和文件下载接口。 */
@RestController
@RequestMapping("/api/oms/v1")
public class OmsFulfillmentMetricsController {

    private final OmsFulfillmentMetricsApplicationService service;

    public OmsFulfillmentMetricsController(
            OmsFulfillmentMetricsApplicationService service) {
        this.service = service;
    }

    @GetMapping("/metrics/fulfillment")
    public ApiResponse<OmsFulfillmentMetricsApplicationService.MetricsView> metrics(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime periodStart,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime periodEnd,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.metrics(periodStart, periodEnd,
                ScmAccessContexts.require(authentication)), request);
    }

    @PostMapping("/metric-exports")
    public ApiResponse<OmsFulfillmentMetricsApplicationService.ExportTaskView> createExport(
            @Valid @RequestBody ExportCreateRequest body,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.createExport(body.periodStart(), body.periodEnd(),
                idempotencyKey, ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/metric-exports")
    public ApiResponse<List<OmsFulfillmentMetricsApplicationService.ExportTaskView>> exports(
            Authentication authentication, HttpServletRequest request) {
        return ok(service.exports(ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/metric-exports/{exportNo}")
    public ApiResponse<OmsFulfillmentMetricsApplicationService.ExportTaskView> export(
            @PathVariable String exportNo,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.export(exportNo, ScmAccessContexts.require(authentication)), request);
    }

    @PostMapping("/metric-exports/{exportNo}/retry")
    public ApiResponse<OmsFulfillmentMetricsApplicationService.ExportTaskView> retry(
            @PathVariable String exportNo,
            @Valid @RequestBody ExportRetryRequest body,
            Authentication authentication,
            HttpServletRequest request) {
        return ok(service.retry(exportNo, body.version(),
                ScmAccessContexts.require(authentication)), request);
    }

    @GetMapping("/metric-exports/{exportNo}/file")
    public ResponseEntity<byte[]> download(
            @PathVariable String exportNo, Authentication authentication) {
        var file = service.download(exportNo, ScmAccessContexts.require(authentication));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(file.fileName(), StandardCharsets.UTF_8)
                                .build().toString())
                .header(HttpHeaders.CONTENT_TYPE, file.contentType())
                .body(file.bytes());
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"),
                request.getHeader("X-Trace-Id"));
    }

    public record ExportCreateRequest(@NotNull LocalDateTime periodStart,
                                      @NotNull LocalDateTime periodEnd) {
    }

    public record ExportRetryRequest(@Positive long version) {
    }
}
