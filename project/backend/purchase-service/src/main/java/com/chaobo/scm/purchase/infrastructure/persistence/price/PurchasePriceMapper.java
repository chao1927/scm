package com.chaobo.scm.purchase.infrastructure.persistence.price;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PurchasePriceMapper {

    record PriceRow(
            long id,
            String priceNo,
            long supplierId,
            String skuCode,
            long purchaseOrgId,
            int priceType,
            String currency,
            BigDecimal unitPrice,
            BigDecimal taxRate,
            BigDecimal taxIncludedPrice,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            String sourceType,
            String sourceNo,
            int status,
            int version) {
    }

    @Select("select * from purchase_price where price_no = #{priceNo} and deleted = 0")
    PriceRow findByNo(String priceNo);

    @Select("""
            select *
            from purchase_price
            where deleted = 0
              and status = 1
              and supplier_id = #{supplierId}
              and sku_code = #{skuCode}
              and purchase_org_id = #{purchaseOrgId}
              and currency = #{currency}
              and not (coalesce(effective_to, '9999-12-31') < #{effectiveFrom}
                       or coalesce(#{effectiveTo}, '9999-12-31') < effective_from)
            """)
    List<PriceRow> findActiveOverlaps(
            @Param("supplierId") long supplierId,
            @Param("skuCode") String skuCode,
            @Param("purchaseOrgId") long purchaseOrgId,
            @Param("currency") String currency,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo);

    @Insert("""
            insert into purchase_price(
              id, price_no, supplier_id, sku_code, purchase_org_id, price_type, currency,
              unit_price, tax_rate, tax_included_price, effective_from, effective_to,
              source_type, source_no, status, version, deleted, created_by, updated_by, created_at, updated_at
            ) values (
              #{id}, #{priceNo}, #{supplierId}, #{skuCode}, #{purchaseOrgId}, #{priceType}, #{currency},
              #{unitPrice}, #{taxRate}, #{taxIncludedPrice}, #{effectiveFrom}, #{effectiveTo},
              #{sourceType}, #{sourceNo}, #{status}, #{version}, 0, #{operatorId}, #{operatorId}, now(3), now(3)
            )
            """)
    void insert(PriceRow row, @Param("operatorId") long operatorId);

    @Update("""
            update purchase_price
            set status = #{status},
                version = #{version},
                updated_by = #{operatorId},
                updated_at = now(3)
            where id = #{id}
            """)
    void updateStatus(@Param("id") long id, @Param("status") int status, @Param("version") int version,
                      @Param("operatorId") long operatorId);
}
