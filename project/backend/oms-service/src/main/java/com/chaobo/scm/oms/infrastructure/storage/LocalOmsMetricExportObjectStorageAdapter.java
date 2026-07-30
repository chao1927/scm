package com.chaobo.scm.oms.infrastructure.storage;

import com.chaobo.scm.oms.application.OmsMetricExportObjectStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** 使用可挂载持久卷的本地文件系统存储 OMS 指标导出。 */
@Component
@ConditionalOnProperty(prefix = "scm.oms.metric-export.storage", name = "type",
        havingValue = "local", matchIfMissing = true)
public class LocalOmsMetricExportObjectStorageAdapter
        implements OmsMetricExportObjectStoragePort {

    private static final String CSV_CONTENT_TYPE = "text/csv;charset=UTF-8";
    private final Path root;

    public LocalOmsMetricExportObjectStorageAdapter(
            @Value("${scm.oms.metric-export.storage.local.root:./data/oms-metric-exports}")
            String root) {
        this.root = Path.of(root).toAbsolutePath().normalize();
    }

    @Override
    public StoredObject store(String objectKey, byte[] content, String contentType) {
        Path target = resolve(objectKey);
        try {
            Files.createDirectories(target.getParent());
            Path temporary = Files.createTempFile(
                    target.getParent(), target.getFileName().toString(), ".part");
            Files.write(temporary, content);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new StoredObject(objectKey, contentType, content.length);
        } catch (IOException exception) {
            throw new UncheckedIOException("保存 OMS 指标导出文件失败", exception);
        }
    }

    @Override
    public StoredContent load(String objectKey) {
        try {
            return new StoredContent(Files.readAllBytes(resolve(objectKey)), CSV_CONTENT_TYPE);
        } catch (IOException exception) {
            throw new UncheckedIOException("读取 OMS 指标导出文件失败", exception);
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
