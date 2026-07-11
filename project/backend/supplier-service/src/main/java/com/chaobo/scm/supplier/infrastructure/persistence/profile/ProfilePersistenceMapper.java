package com.chaobo.scm.supplier.infrastructure.persistence.profile;

import org.apache.ibatis.annotations.*;
import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface ProfilePersistenceMapper {
    record ProfileRow(long supplierId, String supplierCode, String supplierName, int lifecycleStatus,
                      int riskLevel, String profileJson, int version, OffsetDateTime updatedAt) {}
    record ChangeRow(long changeId, String changeNo, long supplierId, int profileVersion, String changeReason,
                     String changedFieldsJson, int changeStatus, String withdrawReason, int version,
                     OffsetDateTime createdAt) {}

    @Select("SELECT supplier_id, supplier_code, supplier_name, lifecycle_status, risk_level, profile_json, version, updated_at FROM sup_supplier_profile_snapshot WHERE supplier_id=#{id}")
    ProfileRow findProfile(long id);

    @Select("SELECT COUNT(*) FROM sup_supplier_profile_change WHERE supplier_id=#{supplierId} AND change_status=1 AND deleted=0")
    boolean existsPending(long supplierId);

    @Select("SELECT change_id, change_no, supplier_id, profile_version, change_reason, changed_fields_json, change_status, withdraw_reason, version, created_at FROM sup_supplier_profile_change WHERE change_id=#{id} AND deleted=0")
    ChangeRow findChange(long id);

    @Insert("INSERT INTO sup_supplier_profile_change(change_id,change_no,supplier_id,profile_version,change_reason,changed_fields_json,change_status,created_by,updated_by,version,deleted) VALUES(#{row.changeId},#{row.changeNo},#{row.supplierId},#{row.profileVersion},#{row.changeReason},CAST(#{row.changedFieldsJson} AS JSON),#{row.changeStatus},#{operatorId},#{operatorId},#{row.version},0)")
    void insert(@Param("row") ChangeRow row, @Param("operatorId") long operatorId);

    @Update("UPDATE sup_supplier_profile_change SET change_status=#{row.changeStatus},withdraw_reason=#{row.withdrawReason},updated_by=#{operatorId},version=#{row.version} WHERE change_id=#{row.changeId} AND version=#{expectedVersion} AND deleted=0")
    int update(@Param("row") ChangeRow row, @Param("expectedVersion") int expectedVersion, @Param("operatorId") long operatorId);

    @Select("<script>SELECT COUNT(*) FROM sup_supplier_profile_change WHERE supplier_id=#{supplierId} AND deleted=0 <if test='status != null'>AND change_status=#{status}</if></script>")
    long countChanges(@Param("supplierId") long supplierId, @Param("status") Integer status);

    @Select("<script>SELECT change_id, change_no, supplier_id, profile_version, change_reason, changed_fields_json, change_status, withdraw_reason, version, created_at FROM sup_supplier_profile_change WHERE supplier_id=#{supplierId} AND deleted=0 <if test='status != null'>AND change_status=#{status}</if> ORDER BY created_at DESC LIMIT #{offset},#{size}</script>")
    List<ChangeRow> pageChanges(@Param("supplierId") long supplierId, @Param("status") Integer status,
                                @Param("offset") int offset, @Param("size") int size);
}
