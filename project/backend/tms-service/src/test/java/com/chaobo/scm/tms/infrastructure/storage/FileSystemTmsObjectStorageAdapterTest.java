package com.chaobo.scm.tms.infrastructure.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 文件系统对象存储适配器测试。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class FileSystemTmsObjectStorageAdapterTest {

    @TempDir
    Path root;

    @Test
    void resolvesObjectUriInsideConfiguredRoot() throws Exception {
        Path label = root.resolve("labels/LBL1.pdf");
        Files.createDirectories(label.getParent());
        Files.writeString(label, "label", StandardCharsets.UTF_8);
        FileSystemTmsObjectStorageAdapter adapter =
            new FileSystemTmsObjectStorageAdapter(root.toString());

        var object = adapter.open("oss://labels/LBL1.pdf");

        assertThat(object.inputStream().readAllBytes())
            .isEqualTo("label".getBytes(StandardCharsets.UTF_8));
        assertThat(object.fileName()).isEqualTo("LBL1.pdf");
    }

    @Test
    void rejectsPathTraversalAndUnsupportedRemoteScheme() {
        FileSystemTmsObjectStorageAdapter adapter =
            new FileSystemTmsObjectStorageAdapter(root.toString());

        assertThatThrownBy(() -> adapter.open("../secret"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> adapter.open("https://example.com/file"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
