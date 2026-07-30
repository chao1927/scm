package com.chaobo.scm.supplier.infrastructure.persistence.profile;

import org.apache.ibatis.annotations.*;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * ProfilePersistenceMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface ProfilePersistenceMapper {

    /**
     * ProfileRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ProfileRow(long supplierId, String supplierCode, String supplierName, int lifecycleStatus, int riskLevel, String profileJson, int version, OffsetDateTime updatedAt) {
    }

    /**
     * ChangeRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ChangeRow(long changeId, String changeNo, long supplierId, int profileVersion, String changeReason, String changedFieldsJson, int changeStatus, String withdrawReason, int version, OffsetDateTime createdAt) {
    }

    /**
     * 查询并返回 {@code findProfile}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code ProfileRow}
     */
    @Select("SELECT supplier_id, supplier_code, supplier_name, lifecycle_status, risk_level, profile_json, version, updated_at FROM sup_supplier_profile_snapshot WHERE supplier_id=#{id}")
    ProfileRow findProfile(long id);

    /**
     * 查询并返回 {@code existsPending}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    @Select("SELECT COUNT(*) FROM sup_supplier_profile_change WHERE supplier_id=#{supplierId} AND change_status=1 AND deleted=0")
    boolean existsPending(long supplierId);

    /**
     * 查询并返回 {@code findChange}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code ChangeRow}
     */
    @Select("SELECT change_id, change_no, supplier_id, profile_version, change_reason, changed_fields_json, change_status, withdraw_reason, version, created_at FROM sup_supplier_profile_change WHERE change_id=#{id} AND deleted=0")
    ChangeRow findChange(long id);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ChangeRow}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_supplier_profile_change(change_id,change_no,supplier_id,profile_version,change_reason,changed_fields_json,change_status,created_by,updated_by,version,deleted) VALUES(#{row.changeId},#{row.changeNo},#{row.supplierId},#{row.profileVersion},#{row.changeReason},CAST(#{row.changedFieldsJson} AS JSON),#{row.changeStatus},#{operatorId},#{operatorId},#{row.version},0)")
    void insert(@Param("row") ChangeRow row, @Param("operatorId") long operatorId);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ChangeRow}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_supplier_profile_change SET change_status=#{row.changeStatus},withdraw_reason=#{row.withdrawReason},updated_by=#{operatorId},version=#{row.version} WHERE change_id=#{row.changeId} AND version=#{expectedVersion} AND deleted=0")
    int update(@Param("row") ChangeRow row, @Param("expectedVersion") int expectedVersion, @Param("operatorId") long operatorId);

    /**
     * 查询并返回 {@code countChanges}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code long}
     */
    @Select("<script>SELECT COUNT(*) FROM sup_supplier_profile_change WHERE supplier_id=#{supplierId} AND deleted=0 <if test='status != null'>AND change_status=#{status}</if></script>")
    long countChanges(@Param("supplierId") long supplierId, @Param("status") Integer status);

    /**
     * 处理当前类型职责中的操作 {@code pageChanges}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<ChangeRow>}
     */
    @Select("<script>SELECT change_id, change_no, supplier_id, profile_version, change_reason, changed_fields_json, change_status, withdraw_reason, version, created_at FROM sup_supplier_profile_change WHERE supplier_id=#{supplierId} AND deleted=0 <if test='status != null'>AND change_status=#{status}</if> ORDER BY created_at DESC LIMIT #{offset},#{size}</script>")
    List<ChangeRow> pageChanges(@Param("supplierId") long supplierId, @Param("status") Integer status, @Param("offset") int offset, @Param("size") int size);
}
