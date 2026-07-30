package com.chaobo.scm.supplier.infrastructure.persistence.profile;

import org.apache.ibatis.annotations.*;

/**
 * SupplierAdmissionMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierAdmissionMapper {

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long id, String no, String code, String name, String taxNo, String type, String contact, String mobile, String settlement, int status, String reject, int version) {
    }

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("SELECT admission_id id,admission_no no,candidate_supplier_code code,candidate_supplier_name name,tax_no taxNo,supplier_type type,contact_name contact,contact_mobile mobile,settlement_json settlement,admission_status status,reject_reason reject,version FROM sup_supplier_admission WHERE admission_id=#{id} AND deleted=0")
    Row find(long id);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code Row}
     * @param op 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_supplier_admission(admission_id,admission_no,candidate_supplier_code,candidate_supplier_name,tax_no,supplier_type,admission_status,contact_name,contact_mobile,settlement_json,reject_reason,created_by,updated_by,version,deleted) VALUES(#{r.id},#{r.no},#{r.code},#{r.name},#{r.taxNo},#{r.type},#{r.status},#{r.contact},#{r.mobile},CAST(#{r.settlement} AS JSON),#{r.reject},#{op},#{op},#{r.version},0)")
    void insert(@Param("r") Row r, @Param("op") long op);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code Row}
     * @param expected 业务处理参数或成员，类型为 {@code int}
     * @param op 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_supplier_admission SET admission_status=#{r.status},reject_reason=#{r.reject},updated_by=#{op},version=#{r.version} WHERE admission_id=#{r.id} AND version=#{expected} AND deleted=0")
    int update(@Param("r") Row r, @Param("expected") int expected, @Param("op") long op);
}
