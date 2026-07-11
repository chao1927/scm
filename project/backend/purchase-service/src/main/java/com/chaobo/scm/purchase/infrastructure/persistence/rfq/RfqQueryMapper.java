package com.chaobo.scm.purchase.infrastructure.persistence.rfq;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface RfqQueryMapper {

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
    long count(
            @Param("purchaseOrgId") Long purchaseOrgId,
            @Param("status") Integer status,
            @Param("categoryCode") String categoryCode,
            @Param("supplierId") Long supplierId,
            @Param("deadlineFrom") OffsetDateTime deadlineFrom,
            @Param("deadlineTo") OffsetDateTime deadlineTo);

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
    List<RfqMapper.HeaderRow> page(
            @Param("purchaseOrgId") Long purchaseOrgId,
            @Param("status") Integer status,
            @Param("categoryCode") String categoryCode,
            @Param("supplierId") Long supplierId,
            @Param("deadlineFrom") OffsetDateTime deadlineFrom,
            @Param("deadlineTo") OffsetDateTime deadlineTo,
            @Param("offset") int offset,
            @Param("limit") int limit);
}
