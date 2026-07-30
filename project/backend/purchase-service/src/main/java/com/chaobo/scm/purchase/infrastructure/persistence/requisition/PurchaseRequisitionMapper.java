package com.chaobo.scm.purchase.infrastructure.persistence.requisition;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * PurchaseRequisitionMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface PurchaseRequisitionMapper {

    /**
     * HeaderRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record HeaderRow(long id, String requisitionNo, long applicantId, long purchaseOrgId, long demandDepartmentId, int status, String reason, int version, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    }

    /**
     * LineRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record LineRow(long lineId, long requisitionId, String skuCode, BigDecimal requestedQty, BigDecimal approvedQty, BigDecimal convertedQty, String purchaseUnit, LocalDate requiredDate, String remark) {
    }

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code HeaderRow}
     */
    @Select("select * from purchase_requisition where id = #{id} and deleted = 0")
    HeaderRow findById(long id);

    /**
     * 查询并返回 {@code findByNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code HeaderRow}
     */
    @Select("select * from purchase_requisition where requisition_no = #{no} and deleted = 0")
    HeaderRow findByNo(String no);

    /**
     * 查询并返回 {@code findLines}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code List<LineRow>}
     */
    @Select("select * from purchase_requisition_line where requisition_id = #{id} and deleted = 0 order by line_id")
    List<LineRow> findLines(long id);

    /**
     * 处理当前类型职责中的操作 {@code insertHeader}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param requisitionNo 可追踪业务编码，类型为 {@code String}
     * @param applicantId 业务或技术标识，类型为 {@code long}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param demandDepartmentId 业务或技术标识，类型为 {@code long}
     * @param status 生命周期状态，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Insert("""
        insert into purchase_requisition(
          id, requisition_no, applicant_id, purchase_org_id, demand_department_id,
          status, reason, version, deleted, created_by, updated_by, created_at, updated_at
        ) values (
          #{id}, #{requisitionNo}, #{applicantId}, #{purchaseOrgId}, #{demandDepartmentId},
          #{status}, #{reason}, #{version}, 0, #{operatorId}, #{operatorId}, now(3), now(3)
        )
        """)
    void insertHeader(@Param("id") long id, @Param("requisitionNo") String requisitionNo, @Param("applicantId") long applicantId, @Param("purchaseOrgId") long purchaseOrgId, @Param("demandDepartmentId") long demandDepartmentId, @Param("status") int status, @Param("reason") String reason, @Param("version") int version, @Param("operatorId") long operatorId);

    /**
     * 执行命令 {@code updateHeader}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param status 生命周期状态，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Update("""
        update purchase_requisition
        set status = #{status},
            reason = #{reason},
            version = #{version},
            updated_by = #{operatorId},
            updated_at = now(3)
        where id = #{id}
        """)
    void updateHeader(@Param("id") long id, @Param("status") int status, @Param("reason") String reason, @Param("version") int version, @Param("operatorId") long operatorId);

    /**
     * 执行命令 {@code deleteLines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     */
    @Delete("delete from purchase_requisition_line where requisition_id = #{id}")
    void deleteLines(long id);

    /**
     * 处理当前类型职责中的操作 {@code insertLine}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param line 业务处理参数或成员，类型为 {@code LineRow}
     */
    @Insert("""
        insert into purchase_requisition_line(
          line_id, requisition_id, sku_code, requested_qty, approved_qty, converted_qty,
          purchase_unit, required_date, remark, deleted, created_at, updated_at
        ) values (
          #{lineId}, #{requisitionId}, #{skuCode}, #{requestedQty}, #{approvedQty}, #{convertedQty},
          #{purchaseUnit}, #{requiredDate}, #{remark}, 0, now(3), now(3)
        )
        """)
    void insertLine(LineRow line);
}
