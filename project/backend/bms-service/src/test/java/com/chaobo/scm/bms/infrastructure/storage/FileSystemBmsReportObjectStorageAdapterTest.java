package com.chaobo.scm.bms.infrastructure.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * BMS 报表文件系统对象存储测试。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class FileSystemBmsReportObjectStorageAdapterTest {

    @TempDir
    Path root;

    @Test
    void persistsAndReadsReportFromConfiguredRoot() throws Exception {
        FileSystemBmsReportObjectStorageAdapter adapter =
            new FileSystemBmsReportObjectStorageAdapter(root.toString());

        String reference = adapter.put("exports/BO-A/report.csv",
            "10.23".getBytes(StandardCharsets.UTF_8), "text/csv");

        assertThat(reference).isEqualTo("bms-object://exports/BO-A/report.csv");
        assertThat(adapter.open(reference).inputStream().readAllBytes())
            .isEqualTo("10.23".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void rejectsEscapingObjectKey() {
        FileSystemBmsReportObjectStorageAdapter adapter =
            new FileSystemBmsReportObjectStorageAdapter(root.toString());

        assertThatThrownBy(() -> adapter.put("../outside.csv", new byte[0], "text/csv"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
