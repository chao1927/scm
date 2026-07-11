package com.chaobo.scm.purchase.infrastructure.persistence.price;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PurchasePriceQueryMapper {

    @Select("""
            <script>
            select count(1)
            from purchase_price
            where deleted = 0
            <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
            <if test="supplierId != null">and supplier_id = #{supplierId}</if>
            <if test="skuCode != null and skuCode != ''">and sku_code = #{skuCode}</if>
            <if test="currency != null and currency != ''">and currency = #{currency}</if>
            <if test="status != null">and status = #{status}</if>
            </script>
            """)
    long count(
            @Param("purchaseOrgId") Long purchaseOrgId,
            @Param("supplierId") Long supplierId,
            @Param("skuCode") String skuCode,
            @Param("currency") String currency,
            @Param("status") Integer status);

    @Select("""
            <script>
            select *
            from purchase_price
            where deleted = 0
            <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
            <if test="supplierId != null">and supplier_id = #{supplierId}</if>
            <if test="skuCode != null and skuCode != ''">and sku_code = #{skuCode}</if>
            <if test="currency != null and currency != ''">and currency = #{currency}</if>
            <if test="status != null">and status = #{status}</if>
            order by updated_at desc
            limit #{offset}, #{limit}
            </script>
            """)
    List<PurchasePriceMapper.PriceRow> page(
            @Param("purchaseOrgId") Long purchaseOrgId,
            @Param("supplierId") Long supplierId,
            @Param("skuCode") String skuCode,
            @Param("currency") String currency,
            @Param("status") Integer status,
            @Param("offset") int offset,
            @Param("limit") int limit);
}
