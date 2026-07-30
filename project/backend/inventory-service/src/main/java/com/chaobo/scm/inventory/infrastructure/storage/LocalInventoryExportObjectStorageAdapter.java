package com.chaobo.scm.inventory.infrastructure.storage;

import com.chaobo.scm.inventory.application.export.InventoryExportObjectStoragePort;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 默认本地对象存储适配器，可由 OSS/S3 适配器无缝替换。
 *
 * <p>对象键经过规范化和根目录校验，写入采用临时文件原子替换。
 *
 * @author SCM Team
 */
@Component
@ConditionalOnProperty(
        prefix = "scm.inventory.export.storage",
        name = "type",
        havingValue = "local",
        matchIfMissing = true)
public class LocalInventoryExportObjectStorageAdapter
        implements InventoryExportObjectStoragePort {

    private static final String CSV_CONTENT_TYPE = "text/csv;charset=UTF-8";
    private final Path root;

    public LocalInventoryExportObjectStorageAdapter(
            @Value("${scm.inventory.export.storage.local.root:./data/inventory-exports}")
            String root) {
        this.root = Path.of(root).toAbsolutePath().normalize();
    }

    @Override
    public StoredObject store(
            String objectKey,
            byte[] content,
            String contentType) {
        Path target = resolve(objectKey);
        try {
            Files.createDirectories(target.getParent());
            Path temporary = Files.createTempFile(
                    target.getParent(),
                    target.getFileName().toString(),
                    ".part");
            Files.write(temporary, content);
            try {
                Files.move(
                        temporary,
                        target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new StoredObject(objectKey, contentType, content.length);
        } catch (IOException exception) {
            throw new UncheckedIOException("保存库存导出文件失败", exception);
        }
    }

    @Override
    public StoredContent load(String objectKey) {
        try {
            return new StoredContent(
                    Files.readAllBytes(resolve(objectKey)),
                    CSV_CONTENT_TYPE);
        } catch (IOException exception) {
            throw new UncheckedIOException("读取库存导出文件失败", exception);
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
