package com.chaobo.scm.supplier.infrastructure.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 验证默认本地对象存储适配器确实落盘，并阻止路径逃逸。
 */
class LocalSupplierExportObjectStorageAdapterTest {

    @TempDir
    Path root;

    @Test
    void storesAndLoadsRealFileUsingObjectKey() {
        var storage = new LocalSupplierExportObjectStorageAdapter(root.toString());

        var metadata = storage.store("exports/42/result.csv", "中文".getBytes(java.nio.charset.StandardCharsets.UTF_8),
                "text/csv;charset=UTF-8");
        var content = storage.load(metadata.objectKey());

        assertThat(root.resolve("exports/42/result.csv")).exists();
        assertThat(metadata.size()).isEqualTo(6);
        assertThat(content.bytes()).isEqualTo("中文".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertThat(content.contentType()).isEqualTo("text/csv;charset=UTF-8");
    }

    @Test
    void rejectsObjectKeyThatEscapesConfiguredRoot() {
        var storage = new LocalSupplierExportObjectStorageAdapter(root.toString());

        assertThatThrownBy(() -> storage.store("../outside.csv", new byte[] {1}, "text/csv"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("对象键");
    }
}
