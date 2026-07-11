package com.chaobo.scm.supplier.infrastructure.persistence.masterdata;

import com.chaobo.scm.supplier.application.masterdata.MasterDataSnapshotPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisMasterDataSnapshotAdapter implements MasterDataSnapshotPort {
    private final MasterDataSnapshotMapper mapper;

    public MyBatisMasterDataSnapshotAdapter(MasterDataSnapshotMapper mapper) { this.mapper = mapper; }

    @Override public Optional<SupplierSnapshot> findSupplier(long supplierId) {
        var row = mapper.findSupplier(supplierId);
        return row == null ? Optional.empty() : Optional.of(new SupplierSnapshot(row.supplierId(), row.supplierCode(),
                row.supplierName(), row.lifecycleStatus(), row.riskLevel(), row.snapshotJson(), row.sourceVersion()));
    }
    @Override public Optional<SkuSnapshot> findSku(String skuCode) {
        var row = mapper.findSku(skuCode);
        return row == null ? Optional.empty() : Optional.of(new SkuSnapshot(row.skuCode(), row.skuName(), row.skuStatus(),
                row.baseUnit(), row.categoryId(), row.snapshotJson(), row.sourceVersion()));
    }
    @Override public void saveSupplier(SupplierSnapshot snapshot) {
        mapper.upsertSupplier(new MasterDataSnapshotMapper.SupplierRow(snapshot.supplierId(), snapshot.supplierCode(),
                snapshot.supplierName(), snapshot.lifecycleStatus(), snapshot.riskLevel(), snapshot.snapshotJson(), snapshot.sourceVersion()));
    }
    @Override public void saveSku(SkuSnapshot snapshot) {
        mapper.upsertSku(new MasterDataSnapshotMapper.SkuRow(snapshot.skuCode(), snapshot.skuName(), snapshot.skuStatus(),
                snapshot.baseUnit(), snapshot.categoryId(), snapshot.snapshotJson(), snapshot.sourceVersion()));
    }
}
