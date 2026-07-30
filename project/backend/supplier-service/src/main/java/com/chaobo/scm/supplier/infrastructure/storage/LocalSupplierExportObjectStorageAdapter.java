package com.chaobo.scm.supplier.infrastructure.storage;

import com.chaobo.scm.supplier.application.operations.export.SupplierExportObjectStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 默认可运行的本地文件对象存储适配器。
 *
 * <p>文件先写入同目录临时文件再原子替换，避免下载到半文件；规范化对象键并验证根目录，阻止路径穿越。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
@ConditionalOnProperty(prefix = "scm.supplier.export.storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalSupplierExportObjectStorageAdapter implements SupplierExportObjectStoragePort {

    private static final String CSV_CONTENT_TYPE = "text/csv;charset=UTF-8";
    private final Path root;

    public LocalSupplierExportObjectStorageAdapter(
            @Value("${scm.supplier.export.storage.local.root:./data/supplier-exports}") String root) {
        this.root = Path.of(root).toAbsolutePath().normalize();
    }

    @Override
    public StoredObject store(String objectKey, byte[] content, String contentType) {
        Path target = resolve(objectKey);
        try {
            Files.createDirectories(target.getParent());
            Path temporary = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".part");
            Files.write(temporary, content);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new StoredObject(objectKey, contentType, content.length);
        } catch (IOException exception) {
            throw new UncheckedIOException("保存供应商导出文件失败", exception);
        }
    }

    @Override
    public StoredContent load(String objectKey) {
        try {
            return new StoredContent(Files.readAllBytes(resolve(objectKey)), CSV_CONTENT_TYPE);
        } catch (IOException exception) {
            throw new UncheckedIOException("读取供应商导出文件失败", exception);
        }
    }

    private Path resolve(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("对象键不能为空");
        }
        Path target = root.resolve(objectKey).normalize();
        if (!target.startsWith(root) || target.equals(root)) {
            throw new IllegalArgumentException("对象键不能逃逸存储根目录");
        }
        return target;
    }
}
