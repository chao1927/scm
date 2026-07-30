package com.chaobo.scm.supplier.infrastructure.persistence.qualification;

import org.apache.ibatis.annotations.*;
import java.util.*;

/**
 * QualificationRequirementMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface QualificationRequirementMapper {

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long id, String supplierType, Long categoryId, String qualificationType, boolean mandatory, int status, int version) {
    }

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("SELECT requirement_id id,supplier_type supplierType,category_id categoryId,qualification_type qualificationType,mandatory,status,version FROM sup_qualification_requirement WHERE requirement_id=#{id}")
    Row find(long id);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code Row}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_qualification_requirement(requirement_id,supplier_type,category_id,qualification_type,mandatory,status,created_by,updated_by,version) VALUES(#{r.id},#{r.supplierType},#{r.categoryId},#{r.qualificationType},#{r.mandatory},#{r.status},#{operator},#{operator},0)")
    void insert(@Param("r") Row row, @Param("operator") long operator);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code Row}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_qualification_requirement SET supplier_type=#{r.supplierType},category_id=#{r.categoryId},qualification_type=#{r.qualificationType},mandatory=#{r.mandatory},status=#{r.status},updated_by=#{operator},version=version+1 WHERE requirement_id=#{r.id} AND version=#{version}")
    int update(@Param("r") Row row, @Param("version") int version, @Param("operator") long operator);

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<Row>}
     */
    @Select("SELECT requirement_id id,supplier_type supplierType,category_id categoryId,qualification_type qualificationType,mandatory,status,version FROM sup_qualification_requirement ORDER BY updated_at DESC")
    List<Row> list();
}
