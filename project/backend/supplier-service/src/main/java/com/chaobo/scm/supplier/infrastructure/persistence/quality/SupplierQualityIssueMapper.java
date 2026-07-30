package com.chaobo.scm.supplier.infrastructure.persistence.quality;

import org.apache.ibatis.annotations.*;
import java.time.*;
import java.util.*;

/**
 * SupplierQualityIssueMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierQualityIssueMapper {

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long id, String no, long supplierId, String sourceType, String sourceNo, String issueType, int severity, String description, int status, OffsetDateTime deadline, String plan, String verification, int version) {
    }

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("SELECT quality_issue_id id,issue_no no,supplier_id supplierId,source_type sourceType,source_no sourceNo,issue_type issueType,severity,issue_description description,issue_status status,rectification_deadline deadline,rectification_plan plan,verification_comment verification,version FROM sup_quality_issue WHERE quality_issue_id=#{id} AND deleted=0")
    Row find(long id);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code Row}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_quality_issue(quality_issue_id,issue_no,supplier_id,source_type,source_no,issue_type,severity,issue_description,issue_status,rectification_deadline,rectification_plan,verification_comment,created_by,updated_by,version,deleted) VALUES(#{r.id},#{r.no},#{r.supplierId},#{r.sourceType},#{r.sourceNo},#{r.issueType},#{r.severity},#{r.description},#{r.status},#{r.deadline},#{r.plan},#{r.verification},#{operator},#{operator},#{r.version},0)")
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
    @Update("UPDATE sup_quality_issue SET issue_status=#{r.status},rectification_deadline=#{r.deadline},rectification_plan=#{r.plan},verification_comment=#{r.verification},updated_by=#{operator},version=#{r.version} WHERE quality_issue_id=#{r.id} AND version=#{expected} AND deleted=0")
    int update(@Param("r") Row r, @Param("expected") int expected, @Param("operator") long operator);

    /**
     * 查询并返回 {@code count}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param severity 业务处理参数或成员，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code long}
     */
    @Select("<script>SELECT COUNT(*) FROM sup_quality_issue WHERE deleted=0 <if test='supplierId!=null'>AND supplier_id=#{supplierId}</if><if test='status!=null'>AND issue_status=#{status}</if><if test='severity!=null'>AND severity=#{severity}</if></script>")
    long count(@Param("supplierId") Long supplierId, @Param("status") Integer status, @Param("severity") Integer severity);

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param severity 业务处理参数或成员，类型为 {@code Integer}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Row>}
     */
    @Select("<script>SELECT quality_issue_id id,issue_no no,supplier_id supplierId,source_type sourceType,source_no sourceNo,issue_type issueType,severity,issue_description description,issue_status status,rectification_deadline deadline,rectification_plan plan,verification_comment verification,version FROM sup_quality_issue WHERE deleted=0 <if test='supplierId!=null'>AND supplier_id=#{supplierId}</if><if test='status!=null'>AND issue_status=#{status}</if><if test='severity!=null'>AND severity=#{severity}</if> ORDER BY updated_at DESC LIMIT #{offset},#{size}</script>")
    List<Row> page(@Param("supplierId") Long supplierId, @Param("status") Integer status, @Param("severity") Integer severity, @Param("offset") int offset, @Param("size") int size);

    /**
     * 处理当前类型职责中的操作 {@code overdueIds}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Long>}
     */
    @Select("SELECT quality_issue_id FROM sup_quality_issue WHERE issue_status=2 AND rectification_deadline<NOW(3) AND deleted=0")
    List<Long> overdueIds();
}
