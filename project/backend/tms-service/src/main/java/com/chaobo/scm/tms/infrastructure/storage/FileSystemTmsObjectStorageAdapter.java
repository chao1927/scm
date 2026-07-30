package com.chaobo.scm.tms.infrastructure.storage;

import com.chaobo.scm.tms.application.storage.TmsObjectStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 基于本地文件系统的 TMS 对象存储适配器。
 *
 * <p>部署时通过 {@code scm.tms.object-storage.root} 把根目录挂载到持久卷。业务表中的
 * {@code oss://bucket/key}、{@code tms-object://bucket/key} 或相对对象键都会被解析到该根目录，
 * 且规范化后的路径不得越过根目录。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class FileSystemTmsObjectStorageAdapter implements TmsObjectStoragePort {

    private static final String OSS_SCHEME = "oss";
    private static final String TMS_OBJECT_SCHEME = "tms-object";
    private static final String PATH_SEPARATOR = "/";

    private final Path root;

    /**
     * 创建文件系统对象存储适配器。
     *
     * @param rootDirectory 对象存储挂载根目录
     */
    public FileSystemTmsObjectStorageAdapter(
        @Value("${scm.tms.object-storage.root:${java.io.tmpdir}/scm-tms-objects}")
        String rootDirectory) {
        this.root = Path.of(rootDirectory).toAbsolutePath().normalize();
    }

    @Override
    public StoredObject open(String objectReference) throws IOException {
        Path objectPath = resolve(objectReference);
        if (!Files.isRegularFile(objectPath)) {
            throw new IOException("TMS object not found: " + objectReference);
        }
        String contentType = Files.probeContentType(objectPath);
        return new StoredObject(
            Files.newInputStream(objectPath),
            Files.size(objectPath),
            contentType == null ? "application/octet-stream" : contentType,
            objectPath.getFileName().toString());
    }

    private Path resolve(String objectReference) {
        if (objectReference == null || objectReference.isBlank()) {
            throw new IllegalArgumentException("object reference must not be blank");
        }
        String key = objectReference.trim();
        URI uri = URI.create(key);
        if (uri.getScheme() != null) {
            if (!OSS_SCHEME.equalsIgnoreCase(uri.getScheme())
                && !TMS_OBJECT_SCHEME.equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException("unsupported TMS object scheme");
            }
            key = (uri.getHost() == null ? "" : uri.getHost())
                + (uri.getPath() == null ? "" : uri.getPath());
        }
        while (key.startsWith(PATH_SEPARATOR)) {
            key = key.substring(1);
        }
        Path resolved = root.resolve(key).normalize();
        if (key.isBlank() || !resolved.startsWith(root)) {
            throw new IllegalArgumentException("TMS object reference escapes storage root");
        }
        return resolved;
    }
}
