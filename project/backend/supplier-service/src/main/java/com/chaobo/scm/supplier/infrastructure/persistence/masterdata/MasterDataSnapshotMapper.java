package com.chaobo.scm.supplier.infrastructure.persistence.masterdata;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * MasterDataSnapshotMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface MasterDataSnapshotMapper {

    /**
     * SupplierRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record SupplierRow(long supplierId, String supplierCode, String supplierName, int lifecycleStatus, int riskLevel, String snapshotJson, long sourceVersion) {
    }

    /**
     * SkuRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record SkuRow(String skuCode, String skuName, int skuStatus, String baseUnit, Long categoryId, String snapshotJson, long sourceVersion) {
    }

    /**
     * 查询并返回 {@code findSupplier}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code SupplierRow}
     */
    @Select("SELECT supplier_id,supplier_code,supplier_name,lifecycle_status,risk_level,profile_json,source_version FROM sup_supplier_profile_snapshot WHERE supplier_id=#{supplierId}")
    SupplierRow findSupplier(long supplierId);

    /**
     * 处理当前类型职责中的操作 {@code upsertSupplier}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code SupplierRow}
     */
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

    /**
     * 查询并返回 {@code findSku}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code SkuRow}
     */
    @Select("SELECT sku_code,sku_name,sku_status,base_unit,category_id,snapshot_json,source_version FROM sup_sku_availability_snapshot WHERE sku_code=#{skuCode}")
    SkuRow findSku(String skuCode);

    /**
     * 处理当前类型职责中的操作 {@code upsertSku}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code SkuRow}
     */
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
