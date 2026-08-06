package com.chaobo.scm.inventory.application.export;

import com.chaobo.scm.common.security.ScmAccessContext;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 库存异步导出的幂等和失败恢复测试。
 */
class InventoryExportApplicationServiceTest {

    @Test
    void sameLogicalScopeKeepsIdempotencyFingerprintStable() {
        MemoryStore store = new MemoryStore();
        InventoryExportApplicationService service = new InventoryExportApplicationService(
                store,
                new MemoryStorage(),
                new ObjectMapper());
        Map<String, Object> firstQuery = new LinkedHashMap<>();
        firstQuery.put("sku", "SKU-1");
        firstQuery.put("batchNo", "B-1");
        Map<String, Object> secondQuery = new LinkedHashMap<>();
        secondQuery.put("batchNo", "B-1");
        secondQuery.put("sku", "SKU-1");

        InventoryExportTask first = service.create(
                new InventoryExportApplicationService.CreateCommand(
                        "STOCK_AGE", null, null, firstQuery),
                "IDEM-1",
                access(linkedSet("11", "10"), linkedSet("21", "20")));

        assertThatCode(() -> service.create(
                new InventoryExportApplicationService.CreateCommand(
                        "STOCK_AGE", null, null, secondQuery),
                "IDEM-1",
                access(linkedSet("10", "11"), linkedSet("20", "21"))))
                .doesNotThrowAnyException();
        assertThat(store.tasks).containsExactly(first);
    }

    @Test
    void failedExportCanBeRecoveredByItsCreator() {
        MemoryStore store = new MemoryStore();
        InventoryExportApplicationService service = new InventoryExportApplicationService(
                store,
                new MemoryStorage(),
                new ObjectMapper());
        LocalDateTime now = LocalDateTime.now();
        store.tasks.add(new InventoryExportTask(
                1L, "EXP-FAILED", "EXPIRY", "{}", "[\"10\"]", "[\"20\"]",
                1001L, 4, 2, now, null, null, null, null, "storage unavailable",
                3, now.minusMinutes(2), now));

        service.retry("EXP-FAILED", 3, access(Set.of("10"), Set.of("20")));

        assertThat(store.tasks.get(0).status()).isEqualTo(1);
        assertThat(store.tasks.get(0).version()).isEqualTo(4);
    }

    private static ScmAccessContext access(
            Set<String> owners,
            Set<String> warehouses) {
        return new ScmAccessContext(
                1001L,
                "inventory-operator",
                "INVENTORY",
                Set.of("inventory:stock:export"),
                Map.of("OWNER", owners, "WAREHOUSE", warehouses));
    }

    private static Set<String> linkedSet(String... values) {
        return new LinkedHashSet<>(List.of(values));
    }

    private static final class MemoryStore implements InventoryExportStorePort {

        private final List<InventoryExportTask> tasks = new ArrayList<>();
        private final Map<String, String> fingerprints = new LinkedHashMap<>();

        @Override
        public InventoryExportTask create(CreateTask task) {
            String key = task.createdBy() + ":" + task.idempotencyKey();
            InventoryExportTask existing = tasks.stream()
                    .filter(value -> value.createdBy() == task.createdBy())
                    .filter(value -> value.taskNo().equals(fingerprints.get(key)))
                    .findFirst()
                    .orElse(null);
            if (existing != null) {
                return existing;
            }
            InventoryExportTask created = new InventoryExportTask(
                    tasks.size() + 1L,
                    task.taskNo(),
                    task.exportType(),
                    task.queryJson(),
                    task.ownerScopeJson(),
                    task.warehouseScopeJson(),
                    task.createdBy(),
                    1,
                    0,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    0,
                    LocalDateTime.now(),
                    LocalDateTime.now());
            tasks.add(created);
            fingerprints.put(key, created.taskNo());
            return created;
        }

        @Override
        public InventoryExportTask find(String taskNo) {
            return tasks.stream().filter(task -> task.taskNo().equals(taskNo))
                    .findFirst().orElse(null);
        }

        @Override
        public List<InventoryExportTask> list(
                long createdBy,
                Integer status,
                int offset,
                int limit) {
            return List.copyOf(tasks);
        }

        @Override
        public List<InventoryExportTask> claimable(
                int maxRetries,
                LocalDateTime staleBefore,
                int limit) {
            return List.of();
        }

        @Override
        public boolean claim(long id, int version) {
            return false;
        }

        @Override
        public boolean complete(
                long id,
                int version,
                InventoryExportObjectStoragePort.StoredObject object,
                String fileName) {
            return false;
        }

        @Override
        public boolean fail(
                long id,
                int version,
                String reason,
                LocalDateTime retryAt) {
            return false;
        }

        @Override
        public boolean retry(String taskNo, long createdBy, int version) {
            for (int index = 0; index < tasks.size(); index++) {
                InventoryExportTask task = tasks.get(index);
                if (task.taskNo().equals(taskNo)
                        && task.createdBy() == createdBy
                        && task.version() == version
                        && task.status() == 4) {
                    tasks.set(index, new InventoryExportTask(
                            task.id(), task.taskNo(), task.exportType(), task.queryJson(),
                            task.ownerScopeJson(), task.warehouseScopeJson(), task.createdBy(),
                            1, task.retryCount(), null, task.objectKey(), task.fileName(),
                            task.contentType(), task.fileSize(), task.lastError(),
                            task.version() + 1, task.createdAt(), LocalDateTime.now()));
                    return true;
                }
            }
            return false;
        }
    }

    private static final class MemoryStorage
            implements InventoryExportObjectStoragePort {

        @Override
        public StoredObject store(
                String objectKey,
                byte[] content,
                String contentType) {
            return new StoredObject(objectKey, contentType, content.length);
        }

        @Override
        public StoredContent load(String objectKey) {
            return new StoredContent(new byte[0], "text/csv");
        }
    }
}
