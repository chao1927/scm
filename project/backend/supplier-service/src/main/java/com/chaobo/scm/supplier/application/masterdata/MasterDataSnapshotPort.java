package com.chaobo.scm.supplier.application.masterdata;

import java.util.Optional;

public interface MasterDataSnapshotPort {
    Optional<SupplierSnapshot> findSupplier(long supplierId);
    Optional<SkuSnapshot> findSku(String skuCode);
    void saveSupplier(SupplierSnapshot snapshot);
    void saveSku(SkuSnapshot snapshot);

    record SupplierSnapshot(long supplierId, String supplierCode, String supplierName,
                            int lifecycleStatus, int riskLevel, String snapshotJson,
                            long sourceVersion) {
        public boolean enabled() { return lifecycleStatus == 3; }
    }

    record SkuSnapshot(String skuCode, String skuName, int skuStatus, String baseUnit,
                       Long categoryId, String snapshotJson, long sourceVersion) {
        public boolean enabled() { return skuStatus == 1; }
    }
}
