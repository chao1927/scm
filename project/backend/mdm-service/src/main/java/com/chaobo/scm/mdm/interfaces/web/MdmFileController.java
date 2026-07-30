package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.file.MdmFileStoragePort;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmImportQualityMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 主数据导入上传、错误文件和导出文件下载入口。
 */
@RestController
@RequestMapping("/api/mdm/v1/files")
@PreAuthorize("hasAnyAuthority('*', 'mdm:*', 'master-data:importexport:page')")
public class MdmFileController {

    private final MdmFileStoragePort storage;
    private final MdmImportQualityMapper mapper;
    private final int maxUploadBytes;

    public MdmFileController(MdmFileStoragePort storage, MdmImportQualityMapper mapper,
                             @org.springframework.beans.factory.annotation.Value("${scm.mdm.file-task.max-upload-bytes:10485760}") int maxUploadBytes) {
        this.storage = storage;
        this.mapper = mapper;
        this.maxUploadBytes = maxUploadBytes;
    }

    @PostMapping("/imports")
    @PreAuthorize("hasAnyAuthority('*', 'mdm:*', 'master-data:importexport:import')")
    public UploadedFile uploadImport(@RequestParam String fileName,
                                     @RequestBody byte[] content) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("导入文件不能为空");
        }
        if (content.length > maxUploadBytes) {
            throw new IllegalArgumentException("导入文件超过大小上限");
        }
        String digest = sha256(content);
        String safeName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String objectKey = "imports/source/" + digest + '/' + safeName;
        MdmFileStoragePort.StoredObject stored = storage.store(objectKey, content, contentType(fileName));
        return new UploadedFile(stored.objectKey(), fileName, digest, stored.contentType(), stored.size());
    }

    @GetMapping("/imports/{importTaskNo}/errors")
    public ResponseEntity<byte[]> downloadErrors(@PathVariable String importTaskNo) {
        MdmImportQualityMapper.ImportTaskRow task = mapper.findImportTask(importTaskNo);
        if (task == null || task.errorFileUrl() == null || task.errorFileUrl().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        return download(task.errorFileUrl(), importTaskNo + "-errors.csv");
    }

    @GetMapping("/exports/{exportTaskNo}")
    public ResponseEntity<byte[]> downloadExport(@PathVariable String exportTaskNo) {
        MdmImportQualityMapper.ExportTaskRow task = mapper.findExportTask(exportTaskNo);
        if (task == null || task.fileUrl() == null || task.fileUrl().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        return download(task.fileUrl(), exportTaskNo + ".csv");
    }

    private ResponseEntity<byte[]> download(String objectKey, String fileName) {
        MdmFileStoragePort.StoredContent content = storage.load(objectKey);
        MediaType mediaType = content.contentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(content.contentType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName.replace("\"", "") + "\"")
                .body(content.bytes());
    }

    private String sha256(byte[] content) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 不可用", exception);
        }
    }

    private String contentType(String fileName) {
        String lowerName = fileName.toLowerCase(java.util.Locale.ROOT);
        if (lowerName.endsWith(".csv")) {
            return "text/csv;charset=" + StandardCharsets.UTF_8.name();
        }
        if (lowerName.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        throw new IllegalArgumentException("仅支持 CSV 或 XLSX 文件");
    }

    public record UploadedFile(String objectKey, String fileName, String fileHash,
                               String contentType, long size) {
    }
}
