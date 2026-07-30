package com.chaobo.scm.mdm.infrastructure.file;

import com.chaobo.scm.mdm.application.file.MdmFileStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 可直接运行的本地文件存储适配器。
 *
 * <p>对象键必须位于配置根目录内，写入使用临时文件替换，防止下载到半文件。
 */
@Component
@ConditionalOnProperty(prefix = "scm.mdm.file-storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalMdmFileStorageAdapter implements MdmFileStoragePort {

    private final Path root;

    public LocalMdmFileStorageAdapter(
            @Value("${scm.mdm.file-storage.local.root:./data/mdm-files}") String root) {
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
            throw new UncheckedIOException("保存主数据文件失败", exception);
        }
    }

    @Override
    public StoredContent load(String objectKey) {
        Path source = resolve(objectKey);
        try {
            return new StoredContent(Files.readAllBytes(source), Files.probeContentType(source));
        } catch (IOException exception) {
            throw new UncheckedIOException("读取主数据文件失败", exception);
        }
    }

    private Path resolve(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("对象键不能为空");
        }
        String normalizedKey = objectKey.startsWith("mdm://") ? objectKey.substring("mdm://".length()) : objectKey;
        Path target = root.resolve(normalizedKey).normalize();
        if (!target.startsWith(root) || target.equals(root)) {
            throw new IllegalArgumentException("对象键不能逃逸存储根目录");
        }
        return target;
    }
}
