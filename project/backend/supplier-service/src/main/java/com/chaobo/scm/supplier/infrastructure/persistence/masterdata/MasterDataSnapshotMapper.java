package com.chaobo.scm.supplier.infrastructure.persistence.masterdata;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MasterDataSnapshotMapper {
    record SupplierRow(long supplierId, String supplierCode, String supplierName, int lifecycleStatus,
                       int riskLevel, String snapshotJson, long sourceVersion) {}
    record SkuRow(String skuCode, String skuName, int skuStatus, String baseUnit,
                  Long categoryId, String snapshotJson, long sourceVersion) {}

    @Select("SELECT supplier_id,supplier_code,supplier_name,lifecycle_status,risk_level,profile_json,source_version FROM sup_supplier_profile_snapshot WHERE supplier_id=#{supplierId}")
    SupplierRow findSupplier(long supplierId);

    @Insert("""
            INSERT INTO sup_supplier_profile_snapshot(supplier_id,supplier_code,supplier_name,lifecycle_status,risk_level,profile_json,source_version,version)
            VALUES(#{row.supplierId},#{row.supplierCode},#{row.supplierName},#{row.lifecycleStatus},#{row.riskLevel},CAST(#{row.snapshotJson} AS JSON),#{row.sourceVersion},0)
            ON DUPLICATE KEY UPDATE supplier_code=IF(VALUES(source_version)>source_version,VALUES(supplier_code),supplier_code),
              supplier_name=IF(VALUES(source_version)>source_version,VALUES(supplier_name),supplier_name),
              lifecycle_status=IF(VALUES(source_version)>source_version,VALUES(lifecycle_status),lifecycle_status),
              risk_level=IF(VALUES(source_version)>source_version,VALUES(risk_level),risk_level),
              profile_json=IF(VALUES(source_version)>source_version,VALUES(profile_json),profile_json),
              version=IF(VALUES(source_version)>source_version,version+1,version),
              source_version=GREATEST(source_version,VALUES(source_version))
            """)
    void upsertSupplier(@Param("row") SupplierRow row);

    @Select("SELECT sku_code,sku_name,sku_status,base_unit,category_id,snapshot_json,source_version FROM sup_sku_availability_snapshot WHERE sku_code=#{skuCode}")
    SkuRow findSku(String skuCode);

    @Insert("""
            INSERT INTO sup_sku_availability_snapshot(sku_code,sku_name,sku_status,base_unit,category_id,snapshot_json,source_version)
            VALUES(#{row.skuCode},#{row.skuName},#{row.skuStatus},#{row.baseUnit},#{row.categoryId},CAST(#{row.snapshotJson} AS JSON),#{row.sourceVersion})
            ON DUPLICATE KEY UPDATE sku_name=IF(VALUES(source_version)>source_version,VALUES(sku_name),sku_name),
              sku_status=IF(VALUES(source_version)>source_version,VALUES(sku_status),sku_status),
              base_unit=IF(VALUES(source_version)>source_version,VALUES(base_unit),base_unit),
              category_id=IF(VALUES(source_version)>source_version,VALUES(category_id),category_id),
              snapshot_json=IF(VALUES(source_version)>source_version,VALUES(snapshot_json),snapshot_json),
              source_version=GREATEST(source_version,VALUES(source_version))
            """)
    void upsertSku(@Param("row") SkuRow row);
}
