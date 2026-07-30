package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.common.security.ScmAccessContexts;
import com.chaobo.scm.tms.application.TmsAttachmentDownloadApplicationService;
import com.chaobo.scm.tms.application.storage.TmsObjectStoragePort;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * TMS 面单与签收证明下载接口。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/tms/v1")
@PreAuthorize("hasAnyAuthority('*', 'tms:*', 'tms:attachment:download')")
public class TmsAttachmentController {

    private final TmsAttachmentDownloadApplicationService service;

    /**
     * 创建附件下载接口。
     *
     * @param service 附件下载应用服务
     */
    public TmsAttachmentController(TmsAttachmentDownloadApplicationService service) {
        this.service = service;
    }

    /**
     * 下载面单文件。
     *
     * @param labelNo 面单编号
     * @param authentication 已认证访问主体
     * @return 文件响应
     * @throws IOException 文件读取失败
     */
    @GetMapping("/shipping-labels/{labelNo}/attachment")
    public ResponseEntity<InputStreamResource> downloadLabel(
        @PathVariable String labelNo, Authentication authentication) throws IOException {
        return response(service.downloadLabel(labelNo, ScmAccessContexts.require(authentication)));
    }

    /**
     * 下载签收证明文件。
     *
     * @param receiptNo 签收编号
     * @param authentication 已认证访问主体
     * @return 文件响应
     * @throws IOException 文件读取失败
     */
    @GetMapping("/delivery-receipts/{receiptNo}/attachment")
    public ResponseEntity<InputStreamResource> downloadReceipt(
        @PathVariable String receiptNo, Authentication authentication) throws IOException {
        return response(service.downloadReceipt(receiptNo, ScmAccessContexts.require(authentication)));
    }

    private ResponseEntity<InputStreamResource> response(
        TmsObjectStoragePort.StoredObject object) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(object.fileName(), StandardCharsets.UTF_8).build());
        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(object.contentLength())
            .contentType(MediaType.parseMediaType(object.contentType()))
            .body(new InputStreamResource(object.inputStream()));
    }
}
