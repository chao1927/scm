package com.chaobo.scm.oms.infrastructure.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** OMS 指标导出持久文件适配器测试。 */
class LocalOmsMetricExportObjectStorageAdapterTest {

    @TempDir
    Path root;

    @Test
    void storesAndLoadsRealFileAndRejectsPathTraversal() {
        var storage = new LocalOmsMetricExportObjectStorageAdapter(root.toString());
        byte[] content = "orderNo,status\r\nSO-1,COMPLETED\r\n"
                .getBytes(StandardCharsets.UTF_8);

        var stored = storage.store("exports/task-1.csv", content, "text/csv");
        var loaded = storage.load(stored.objectKey());

        assertThat(loaded.bytes()).isEqualTo(content);
        assertThat(stored.size()).isEqualTo(content.length);
        assertThatThrownBy(() -> storage.store("../escape.csv", content, "text/csv"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
