package com.chaobo.scm.purchase.infrastructure.persistence.order;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PurchaseOrderQueryMapper {
    @Select("""
            <script>
            select count(1) from purchase_order where deleted = 0
            <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
            <if test="supplierId != null">and supplier_id = #{supplierId}</if>
            <if test="status != null">and status = #{status}</if>
            </script>
            """)
    long count(@Param("purchaseOrgId") Long purchaseOrgId, @Param("supplierId") Long supplierId,
               @Param("status") Integer status);

    @Select("""
            <script>
            select * from purchase_order where deleted = 0
            <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
            <if test="supplierId != null">and supplier_id = #{supplierId}</if>
            <if test="status != null">and status = #{status}</if>
            order by updated_at desc
            limit #{offset}, #{limit}
            </script>
            """)
    List<PurchaseOrderMapper.HeaderRow> page(@Param("purchaseOrgId") Long purchaseOrgId,
                                             @Param("supplierId") Long supplierId,
                                             @Param("status") Integer status,
                                             @Param("offset") int offset,
                                             @Param("limit") int limit);
}
