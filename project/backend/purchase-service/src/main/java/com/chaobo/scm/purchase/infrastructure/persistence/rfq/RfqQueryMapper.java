package com.chaobo.scm.purchase.infrastructure.persistence.rfq;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * RfqQueryMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface RfqQueryMapper {

    /**
     * 查询并返回 {@code count}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param categoryCode 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param deadlineFrom 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param deadlineTo 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @return 查询并返回的结果，类型为 {@code long}
     */
    @Select("""
        <script>
        select count(distinct r.id)
        from purchase_rfq r
        <if test="supplierId != null">
          join purchase_rfq_invitation i on i.rfq_id = r.id and i.deleted = 0
        </if>
        where r.deleted = 0
        <if test="purchaseOrgId != null">and r.purchase_org_id = #{purchaseOrgId}</if>
        <if test="status != null">and r.status = #{status}</if>
        <if test="categoryCode != null and categoryCode != ''">and r.category_code = #{categoryCode}</if>
        <if test="supplierId != null">and i.supplier_id = #{supplierId}</if>
        <if test="deadlineFrom != null">and r.quote_deadline &gt;= #{deadlineFrom}</if>
        <if test="deadlineTo != null">and r.quote_deadline &lt;= #{deadlineTo}</if>
        </script>
        """)
    long count(@Param("purchaseOrgId") Long purchaseOrgId, @Param("status") Integer status, @Param("categoryCode") String categoryCode, @Param("supplierId") Long supplierId, @Param("deadlineFrom") OffsetDateTime deadlineFrom, @Param("deadlineTo") OffsetDateTime deadlineTo);

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param categoryCode 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param deadlineFrom 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param deadlineTo 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<RfqMapper.HeaderRow>}
     */
    @Select("""
        <script>
        select distinct r.*
        from purchase_rfq r
        <if test="supplierId != null">
          join purchase_rfq_invitation i on i.rfq_id = r.id and i.deleted = 0
        </if>
        where r.deleted = 0
        <if test="purchaseOrgId != null">and r.purchase_org_id = #{purchaseOrgId}</if>
        <if test="status != null">and r.status = #{status}</if>
        <if test="categoryCode != null and categoryCode != ''">and r.category_code = #{categoryCode}</if>
        <if test="supplierId != null">and i.supplier_id = #{supplierId}</if>
        <if test="deadlineFrom != null">and r.quote_deadline &gt;= #{deadlineFrom}</if>
        <if test="deadlineTo != null">and r.quote_deadline &lt;= #{deadlineTo}</if>
        order by r.updated_at desc
        limit #{offset}, #{limit}
        </script>
        """)
    List<RfqMapper.HeaderRow> page(@Param("purchaseOrgId") Long purchaseOrgId, @Param("status") Integer status, @Param("categoryCode") String categoryCode, @Param("supplierId") Long supplierId, @Param("deadlineFrom") OffsetDateTime deadlineFrom, @Param("deadlineTo") OffsetDateTime deadlineTo, @Param("offset") int offset, @Param("limit") int limit);
}
