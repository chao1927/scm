package com.chaobo.scm.mdm.application.file;

import com.chaobo.scm.mdm.infrastructure.persistence.MasterDataRecordMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmImportQualityMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MdmFileTaskProcessorTest {

    @Test
    void maskedSnapshotHidesEveryNonBaseFieldWithoutKeywordGuessing() {
        String csv = processExport(true);

        assertThat(csv).contains(
                "\"SKU-001\",\"商品一\",\"2\",\"***\",\"***\",\"***\"");
        assertThat(csv).doesNotContain("buyer@example.com", "91310000TEST", "hidden");
    }

    @Test
    void unmaskedSnapshotCanExportAuthorizedFieldValues() {
        String csv = processExport(false);

        assertThat(csv).contains("buyer@example.com", "91310000TEST", "hidden");
    }

    private String processExport(boolean maskSensitiveFields) {
        AtomicReference<byte[]> exported = new AtomicReference<>();
        MdmImportQualityMapper taskMapper = proxy(MdmImportQualityMapper.class, (method, args) -> {
            if (method.getName().equals("claimExportTask")) {
                return 1;
            }
            return defaultValue(method.getReturnType());
        });
        MasterDataRecordMapper recordMapper = proxy(MasterDataRecordMapper.class, (method, args) -> {
            if (method.getName().equals("listRecordsForExport")) {
                return List.of(new MasterDataRecordMapper.RecordRow(
                        1L, "REC-001", "SKU", "SKU-001", "商品一",
                        "{\"email\":\"buyer@example.com\",\"taxNo\":\"91310000TEST\","
                                + "\"hidden\":\"hidden\",\"dataCode\":\"FORGED\"}",
                        2, 1, null, 1));
            }
            return defaultValue(method.getReturnType());
        });
        MdmFileStoragePort storage = new MdmFileStoragePort() {
            @Override
            public StoredObject store(String objectKey, byte[] content, String contentType) {
                exported.set(content);
                return new StoredObject(objectKey, contentType, content.length);
            }

            @Override
            public StoredContent load(String objectKey) {
                throw new UnsupportedOperationException();
            }
        };
        MdmFileTaskProcessor processor = new MdmFileTaskProcessor(
                taskMapper, recordMapper, storage, null, null, 60, 100);
        processor.processExport(new MdmImportQualityMapper.ExportTaskRow(
                1L, "EXP-001", "SKU", "{}",
                "[\"dataCode\",\"dataName\",\"status\",\"email\",\"taxNo\",\"dataPayload\"]",
                maskSensitiveFields, 1, null, 1));

        assertThat(exported.get()).isNotNull();
        return new String(exported.get(), StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (instance, method, args) -> invocation.invoke(method, args));
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        return 0;
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(java.lang.reflect.Method method, Object[] args) throws Throwable;
    }
}
