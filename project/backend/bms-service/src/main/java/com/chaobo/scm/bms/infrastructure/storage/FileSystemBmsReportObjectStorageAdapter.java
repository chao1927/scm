package com.chaobo.scm.bms.infrastructure.storage;

import com.chaobo.scm.bms.application.storage.BmsReportObjectStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 基于持久卷目录的 BMS 报表对象存储适配器。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class FileSystemBmsReportObjectStorageAdapter
    implements BmsReportObjectStoragePort {

    private static final String OBJECT_SCHEME = "bms-object";
    private static final String PATH_SEPARATOR = "/";
    private final Path root;

    /**
     * 创建文件系统对象存储适配器。
     *
     * @param rootDirectory 持久卷根目录
     */
    public FileSystemBmsReportObjectStorageAdapter(
        @Value("${scm.bms.report.object-storage-root:"
            + "${java.io.tmpdir}/scm-bms-reports}") String rootDirectory) {
        this.root = Path.of(rootDirectory).toAbsolutePath().normalize();
    }

    @Override
    public String put(String objectKey, byte[] content, String contentType)
        throws IOException {
        Path path = resolve(objectKey);
        Files.createDirectories(path.getParent());
        Files.write(path, content);
        return OBJECT_SCHEME + "://" + objectKey;
    }

    @Override
    public StoredObject open(String objectReference) throws IOException {
        Path path = resolve(objectReference);
        if (!Files.isRegularFile(path)) {
            throw new IOException("BMS report object not found: " + objectReference);
        }
        String contentType = Files.probeContentType(path);
        return new StoredObject(Files.newInputStream(path), Files.size(path),
            contentType == null ? "text/csv" : contentType,
            path.getFileName().toString());
    }

    private Path resolve(String reference) {
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("report object reference must not be blank");
        }
        String key = reference.trim();
        URI uri = URI.create(key);
        if (uri.getScheme() != null) {
            if (!OBJECT_SCHEME.equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException("unsupported BMS report object scheme");
            }
            key = (uri.getHost() == null ? "" : uri.getHost())
                + (uri.getPath() == null ? "" : uri.getPath());
        }
        while (key.startsWith(PATH_SEPARATOR)) {
            key = key.substring(1);
        }
        Path path = root.resolve(key).normalize();
        if (key.isBlank() || !path.startsWith(root)) {
            throw new IllegalArgumentException("report object reference escapes storage root");
        }
        return path;
    }
}
