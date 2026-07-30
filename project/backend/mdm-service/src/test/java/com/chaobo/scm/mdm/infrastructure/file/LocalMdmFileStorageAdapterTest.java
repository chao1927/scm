package com.chaobo.scm.mdm.infrastructure.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalMdmFileStorageAdapterTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void storesAndLoadsInsideConfiguredRoot() {
        LocalMdmFileStorageAdapter storage = new LocalMdmFileStorageAdapter(temporaryDirectory.toString());
        storage.store("imports/source/test.csv", new byte[]{1, 2, 3}, "text/csv");

        assertThat(storage.load("imports/source/test.csv").bytes()).containsExactly(1, 2, 3);
    }

    @Test
    void rejectsPathTraversal() {
        LocalMdmFileStorageAdapter storage = new LocalMdmFileStorageAdapter(temporaryDirectory.toString());

        assertThatThrownBy(() -> storage.store("../outside.csv", new byte[]{1}, "text/csv"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
