package com.chaobo.scm.purchase.infrastructure.persistence.requisition;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PurchaseRequisitionQueryMapper {

    @Select("""
            <script>
            select count(1)
            from purchase_requisition
            where deleted = 0
            <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
            <if test="status != null">and status = #{status}</if>
            <if test="keyword != null and keyword != ''">
              and (requisition_no like concat('%', #{keyword}, '%') or reason like concat('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long count(
            @Param("purchaseOrgId") Long purchaseOrgId,
            @Param("status") Integer status,
            @Param("keyword") String keyword);

    @Select("""
            <script>
            select *
            from purchase_requisition
            where deleted = 0
            <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
            <if test="status != null">and status = #{status}</if>
            <if test="keyword != null and keyword != ''">
              and (requisition_no like concat('%', #{keyword}, '%') or reason like concat('%', #{keyword}, '%'))
            </if>
            order by updated_at desc
            limit #{offset}, #{limit}
            </script>
            """)
    List<PurchaseRequisitionMapper.HeaderRow> page(
            @Param("purchaseOrgId") Long purchaseOrgId,
            @Param("status") Integer status,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit);
}
