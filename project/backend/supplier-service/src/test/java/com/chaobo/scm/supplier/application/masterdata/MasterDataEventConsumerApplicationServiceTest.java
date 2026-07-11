package com.chaobo.scm.supplier.application.masterdata;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MasterDataEventConsumerApplicationServiceTest {
    private final Snapshots snapshots = new Snapshots();
    private final Logs logs = new Logs();
    private final MasterDataEventConsumerApplicationService service =
            new MasterDataEventConsumerApplicationService(snapshots, logs, new ObjectMapper());

    @Test
    void shouldRefreshSupplierAndSkuSnapshotsFromMasterDataEvents() {
        var supplier = new MasterDataEvent("MDM-1", "SupplierEnabled", "MDM", 101, 5, OffsetDateTime.now(),
                Map.of("supplierId", 101L, "supplierCode", "SUP-101", "supplierName", "华东供应商", "riskLevel", 2, "sourceVersion", 5L));
        var sku = new MasterDataEvent("MDM-2", "SkuEnabled", "MDM", 201, 3, OffsetDateTime.now(),
                Map.of("skuCode", "SKU-201", "skuName", "蓝牙耳机", "baseUnit", "件", "sourceVersion", 3L));

        assertThat(service.consume(supplier).consumed()).isTrue();
        assertThat(service.consume(sku).consumed()).isTrue();
        assertThat(snapshots.findSupplier(101)).get().extracting(MasterDataSnapshotPort.SupplierSnapshot::enabled,
                MasterDataSnapshotPort.SupplierSnapshot::riskLevel).containsExactly(true, 2);
        assertThat(snapshots.findSku("SKU-201")).get().extracting(MasterDataSnapshotPort.SkuSnapshot::enabled,
                MasterDataSnapshotPort.SkuSnapshot::baseUnit).containsExactly(true, "件");
    }

    @Test
    void shouldIgnoreOlderSkuVersion() {
        service.consume(new MasterDataEvent("MDM-3", "SkuEnabled", "MDM", 202, 8, OffsetDateTime.now(),
                Map.of("skuCode", "SKU-202", "skuName", "新名称", "sourceVersion", 8L)));
        var result = service.consume(new MasterDataEvent("MDM-4", "SkuDisabled", "MDM", 202, 7, OffsetDateTime.now(),
                Map.of("skuCode", "SKU-202", "skuName", "旧名称", "sourceVersion", 7L)));

        assertThat(result.ignored()).isTrue();
        assertThat(snapshots.findSku("SKU-202")).get().extracting(MasterDataSnapshotPort.SkuSnapshot::enabled,
                MasterDataSnapshotPort.SkuSnapshot::skuName).containsExactly(true, "新名称");
    }

    private static final class Snapshots implements MasterDataSnapshotPort {
        private final Map<Long, SupplierSnapshot> suppliers = new HashMap<>();
        private final Map<String, SkuSnapshot> skus = new HashMap<>();
        @Override public Optional<SupplierSnapshot> findSupplier(long supplierId) { return Optional.ofNullable(suppliers.get(supplierId)); }
        @Override public Optional<SkuSnapshot> findSku(String skuCode) { return Optional.ofNullable(skus.get(skuCode)); }
        @Override public void saveSupplier(SupplierSnapshot snapshot) { suppliers.put(snapshot.supplierId(), snapshot); }
        @Override public void saveSku(SkuSnapshot snapshot) { skus.put(snapshot.skuCode(), snapshot); }
    }

    private static final class Logs implements MasterDataEventConsumeLogPort {
        private final Map<String, ClaimResult> claimed = new HashMap<>();
        @Override public ClaimResult claim(String source, String code, String type, String consumer, String key) {
            return claimed.putIfAbsent(code, ClaimResult.CLAIMED) == null ? ClaimResult.CLAIMED : ClaimResult.ALREADY_SUCCEEDED;
        }
        @Override public void markSucceeded(String source, String code, String consumer, boolean ignored) { }
        @Override public void recordFailure(String source, String code, String type, String consumer, String key, String reason) { }
    }
}
