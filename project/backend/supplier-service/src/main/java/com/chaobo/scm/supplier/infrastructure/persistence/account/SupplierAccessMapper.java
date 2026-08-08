package com.chaobo.scm.supplier.infrastructure.persistence.account;

import org.apache.ibatis.annotations.*;
import java.time.*;
import java.util.*;

/**
 * SupplierAccessMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierAccessMapper {

    /**
     * ContactRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ContactRow(long id, long supplierId, String name, String mobile, String email, String role, boolean primary, int status, int version) {
    }

    /**
     * BindingRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record BindingRow(long id, long supplierId, long userId, String role, boolean primary, int status, OffsetDateTime boundAt, int version) {
    }

    /**
     * 处理当前类型职责中的操作 {@code contact}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ContactRow}
     */
    @Select("SELECT contact_id id,supplier_id supplierId,contact_name name,mobile,email,contact_role role,is_primary `primary`,status,version FROM sup_supplier_contact WHERE contact_id=#{id} AND deleted=0")
    ContactRow contact(long id);

    /**
     * 处理当前类型职责中的操作 {@code contacts}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<ContactRow>}
     */
    @Select("SELECT contact_id id,supplier_id supplierId,contact_name name,mobile,email,contact_role role,is_primary `primary`,status,version FROM sup_supplier_contact WHERE supplier_id=#{supplierId} AND deleted=0 ORDER BY is_primary DESC,created_at")
    List<ContactRow> contacts(long supplierId);

    /**
     * 处理当前类型职责中的操作 {@code insertContact}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code ContactRow}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_supplier_contact(contact_id,supplier_id,contact_name,mobile,email,contact_role,is_primary,status,created_by,updated_by,version,deleted) VALUES(#{r.id},#{r.supplierId},#{r.name},#{r.mobile},#{r.email},#{r.role},#{r.primary},1,#{operator},#{operator},0,0)")
    void insertContact(@Param("r") ContactRow r, @Param("operator") long operator);

    /**
     * 执行命令 {@code updateContact}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code ContactRow}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_supplier_contact SET contact_name=#{r.name},mobile=#{r.mobile},email=#{r.email},contact_role=#{r.role},is_primary=#{r.primary},updated_by=#{operator},version=version+1 WHERE contact_id=#{r.id} AND version=#{version} AND deleted=0")
    int updateContact(@Param("r") ContactRow r, @Param("version") int version, @Param("operator") long operator);

    /**
     * 处理当前类型职责中的操作 {@code clearPrimaryContact}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     */
    @Update("UPDATE sup_supplier_contact SET is_primary=0 WHERE supplier_id=#{supplierId} AND deleted=0")
    void clearPrimaryContact(long supplierId);

    /**
     * 执行命令 {@code binding}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code BindingRow}
     */
    @Select("SELECT supplier_user_id id,supplier_id supplierId,user_id userId,binding_role role,is_primary `primary`,status,bound_at boundAt,version FROM sup_supplier_user_binding WHERE supplier_user_id=#{id}")
    BindingRow binding(long id);

    /**
     * 执行命令 {@code bindings}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code List<BindingRow>}
     */
    @Select("SELECT supplier_user_id id,supplier_id supplierId,user_id userId,binding_role role,is_primary `primary`,status,bound_at boundAt,version FROM sup_supplier_user_binding WHERE supplier_id=#{supplierId} ORDER BY status,bound_at")
    List<BindingRow> bindings(long supplierId);

    @Select("<script>SELECT supplier_user_id id,supplier_id supplierId,user_id userId,binding_role role,is_primary `primary`,status,bound_at boundAt,version FROM sup_supplier_user_binding <if test='supplierId!=null'>WHERE supplier_id=#{supplierId}</if> ORDER BY status,bound_at DESC</script>")
    List<BindingRow> allBindings(@Param("supplierId") Long supplierId);

    /**
     * 处理当前类型职责中的操作 {@code insertBinding}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code BindingRow}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_supplier_user_binding(supplier_user_id,supplier_id,user_id,binding_role,is_primary,status,bound_by,version) VALUES(#{r.id},#{r.supplierId},#{r.userId},#{r.role},#{r.primary},1,#{operator},0)")
    void insertBinding(@Param("r") BindingRow r, @Param("operator") long operator);

    /**
     * 执行命令 {@code disableBinding}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_supplier_user_binding SET status=2,unbound_at=NOW(3),version=version+1 WHERE supplier_user_id=#{id} AND version=#{version} AND status=1")
    int disableBinding(@Param("id") long id, @Param("version") int version);

    /**
     * 处理当前类型职责中的操作 {@code activeBinding}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param userId 业务或技术标识，类型为 {@code long}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    @Select("SELECT COUNT(*) FROM sup_supplier_user_binding WHERE supplier_id=#{supplierId} AND user_id=#{userId} AND status=1")
    boolean activeBinding(@Param("supplierId") long supplierId, @Param("userId") long userId);

    /**
     * 处理当前类型职责中的操作 {@code activeSupplierIds}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param userId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Set<Long>}
     */
    @Select("SELECT supplier_id FROM sup_supplier_user_binding WHERE user_id=#{userId} AND status=1 ORDER BY supplier_id")
    Set<Long> activeSupplierIds(long userId);
}
