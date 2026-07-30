package com.chaobo.scm.bms.interfaces.web;

import com.chaobo.scm.bms.application.BmsReportExportApplicationService;
import com.chaobo.scm.bms.application.storage.BmsReportObjectStoragePort;
import com.chaobo.scm.bms.infrastructure.persistence.BmsReportExportMapper;
import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.common.security.ScmAccessContexts;
import com.chaobo.scm.common.api.PageResult;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * BMS 异步报表导出、失败恢复和下载接口。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/bms/v1/report-exports")
public class BmsReportExportController {

    private final BmsReportExportApplicationService service;

    /**
     * 创建报表导出接口。
     */
    public BmsReportExportController(BmsReportExportApplicationService service) {
        this.service = service;
    }

    /**
     * 创建异步导出任务。
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('*','bms:*','bms:report:export')")
    public BmsReportExportMapper.ExportTaskRow create(
        @RequestBody BmsReportExportApplicationService.CreateCommand command,
        Authentication authentication) {
        return service.enqueue(command, access(authentication));
    }

    /**
     * 查询导出任务及失败原因。
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('*','bms:*','bms:report:read')")
    public PageResult<BmsReportExportMapper.ExportTaskRow> list(
        @RequestParam(required = false) String objectCode,
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "20") int pageSize,
        Authentication authentication) {
        return service.list(objectCode, pageNo, pageSize, access(authentication));
    }

    /**
     * 人工恢复最终失败导出任务。
     */
    @PostMapping("/{exportNo}/retry")
    @PreAuthorize("hasAnyAuthority('*','bms:*','bms:report:retry')")
    public void retry(@PathVariable String exportNo, @RequestBody RetryRequest request,
                      Authentication authentication) {
        service.retry(exportNo, request.reason(), access(authentication));
    }

    /**
     * 下载成功生成的报表。
     */
    @GetMapping("/{exportNo}/download")
    @PreAuthorize("hasAnyAuthority('*','bms:*','bms:report:download')")
    public ResponseEntity<InputStreamResource> download(
        @PathVariable String exportNo, Authentication authentication) throws IOException {
        BmsReportObjectStoragePort.StoredObject object =
            service.download(exportNo, access(authentication));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(object.fileName(), StandardCharsets.UTF_8).build());
        return ResponseEntity.ok().headers(headers)
            .contentLength(object.contentLength())
            .contentType(MediaType.parseMediaType(object.contentType()))
            .body(new InputStreamResource(object.inputStream()));
    }

    private ScmAccessContext access(Authentication authentication) {
        return ScmAccessContexts.require(authentication);
    }

    /**
     * 最终失败导出任务的人工恢复请求。
     */
    public record RetryRequest(String reason) {
    }
}
