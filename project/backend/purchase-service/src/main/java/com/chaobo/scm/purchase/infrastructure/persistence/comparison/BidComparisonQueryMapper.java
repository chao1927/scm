package com.chaobo.scm.purchase.infrastructure.persistence.comparison;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BidComparisonQueryMapper {

    @Select("""
            <script>
            select count(1)
            from purchase_bid_comparison
            where deleted = 0
            <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
            <if test="status != null">and status = #{status}</if>
            <if test="rfqNo != null and rfqNo != ''">and rfq_no = #{rfqNo}</if>
            </script>
            """)
    long count(@Param("purchaseOrgId") Long purchaseOrgId, @Param("status") Integer status, @Param("rfqNo") String rfqNo);

    @Select("""
            <script>
            select *
            from purchase_bid_comparison
            where deleted = 0
            <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
            <if test="status != null">and status = #{status}</if>
            <if test="rfqNo != null and rfqNo != ''">and rfq_no = #{rfqNo}</if>
            order by updated_at desc
            limit #{offset}, #{limit}
            </script>
            """)
    List<BidComparisonMapper.HeaderRow> page(
            @Param("purchaseOrgId") Long purchaseOrgId,
            @Param("status") Integer status,
            @Param("rfqNo") String rfqNo,
            @Param("offset") int offset,
            @Param("limit") int limit);
}
