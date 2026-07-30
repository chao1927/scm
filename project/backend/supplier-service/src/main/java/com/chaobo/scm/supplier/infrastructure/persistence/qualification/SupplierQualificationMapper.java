package com.chaobo.scm.supplier.infrastructure.persistence.qualification;

import org.apache.ibatis.annotations.*;
import java.time.*;
import java.util.*;

/**
 * SupplierQualificationMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierQualificationMapper {

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long id, long supplierId, String type, String number, LocalDate from, LocalDate to, String attachment, int status, String remark, int version) {
    }

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("SELECT qualification_id id,supplier_id supplierId,qualification_type type,qualification_no number,valid_from `from`,valid_to `to`,attachment_url attachment,qualification_status status,review_remark remark,version FROM sup_supplier_qualification WHERE qualification_id=#{id} AND deleted=0")
    Row find(long id);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code Row}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_supplier_qualification(qualification_id,supplier_id,qualification_type,qualification_no,valid_from,valid_to,attachment_url,qualification_status,review_remark,created_by,updated_by,version,deleted) VALUES(#{r.id},#{r.supplierId},#{r.type},#{r.number},#{r.from},#{r.to},#{r.attachment},#{r.status},#{r.remark},#{operator},#{operator},#{r.version},0)")
    void insert(@Param("r") Row r, @Param("operator") long operator);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code Row}
     * @param expected 业务处理参数或成员，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_supplier_qualification SET qualification_status=#{r.status},review_remark=#{r.remark},updated_by=#{operator},version=#{r.version} WHERE qualification_id=#{r.id} AND version=#{expected} AND deleted=0")
    int update(@Param("r") Row r, @Param("expected") int expected, @Param("operator") long operator);

    /**
     * 处理当前类型职责中的操作 {@code hasValid}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    @Select("SELECT COUNT(*) FROM sup_supplier_qualification WHERE supplier_id=#{supplierId} AND qualification_status=2 AND valid_to>=CURDATE() AND deleted=0")
    boolean hasValid(long supplierId);

    /**
     * 查询并返回 {@code requiredTypes}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param categoryId 业务或技术标识，类型为 {@code Long}
     * @return 查询并返回的结果，类型为 {@code List<String>}
     */
    @Select("<script>SELECT qualification_type FROM sup_qualification_requirement WHERE status=1 AND mandatory=1 AND (category_id IS NULL OR category_id=#{categoryId}) ORDER BY category_id IS NOT NULL DESC</script>")
    List<String> requiredTypes(@Param("categoryId") Long categoryId);

    /**
     * 处理当前类型职责中的操作 {@code validRequiredCount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param types 业务处理参数或成员，类型为 {@code List<String>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Select("<script>SELECT COUNT(DISTINCT qualification_type) FROM sup_supplier_qualification WHERE supplier_id=#{supplierId} AND qualification_status=2 AND valid_to>=CURDATE() AND deleted=0 AND qualification_type IN <foreach collection='types' item='type' open='(' separator=',' close=')'>#{type}</foreach></script>")
    long validRequiredCount(@Param("supplierId") long supplierId, @Param("types") List<String> types);

    /**
     * 查询并返回 {@code count}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code long}
     */
    @Select("<script>SELECT COUNT(*) FROM sup_supplier_qualification WHERE deleted=0 <if test='supplierId != null'>AND supplier_id=#{supplierId}</if><if test='status != null'>AND qualification_status=#{status}</if></script>")
    long count(@Param("supplierId") Long supplierId, @Param("status") Integer status);

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Row>}
     */
    @Select("<script>SELECT qualification_id id,supplier_id supplierId,qualification_type type,qualification_no number,valid_from `from`,valid_to `to`,attachment_url attachment,qualification_status status,review_remark remark,version FROM sup_supplier_qualification WHERE deleted=0 <if test='supplierId != null'>AND supplier_id=#{supplierId}</if><if test='status != null'>AND qualification_status=#{status}</if> ORDER BY valid_to ASC LIMIT #{offset},#{size}</script>")
    List<Row> page(@Param("supplierId") Long supplierId, @Param("status") Integer status, @Param("offset") int offset, @Param("size") int size);

    /**
     * 处理当前类型职责中的操作 {@code expiredIds}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Long>}
     */
    @Select("SELECT qualification_id FROM sup_supplier_qualification WHERE qualification_status=2 AND valid_to<CURDATE() AND deleted=0")
    List<Long> expiredIds();
}
