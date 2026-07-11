package com.chaobo.scm.purchase.infrastructure.persistence.order;

import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface PurchaseOrderMapper {
    record HeaderRow(long id, String orderNo, int purchaseType, long supplierId, String supplierCode,
                     String supplierName, long purchaseOrgId, String warehouseCode, String currency,
                     BigDecimal totalAmount, BigDecimal taxAmount, BigDecimal taxIncludedAmount, int status,
                     int versionNo, int version, OffsetDateTime releasedAt, String cancelReason) {
    }

    record LineRow(long lineId, long orderId, String skuCode, String skuName, BigDecimal orderQty,
                   BigDecimal unitPrice, BigDecimal taxRate, BigDecimal taxIncludedPrice,
                   LocalDate requiredDeliveryDate, BigDecimal receivedQty) {
    }

    @Select("select * from purchase_order where order_no = #{orderNo} and deleted = 0")
    HeaderRow findByNo(String orderNo);

    @Select("select * from purchase_order_line where order_id = #{orderId} and deleted = 0 order by line_id")
    List<LineRow> findLines(long orderId);

    @Insert("""
            insert into purchase_order(
              id, order_no, purchase_type, supplier_id, supplier_code, supplier_name, purchase_org_id, warehouse_code,
              currency, total_amount, tax_amount, tax_included_amount, status, version_no, version, released_at,
              cancel_reason, deleted, created_by, updated_by, created_at, updated_at
            ) values (
              #{id}, #{orderNo}, #{purchaseType}, #{supplierId}, #{supplierCode}, #{supplierName}, #{purchaseOrgId},
              #{warehouseCode}, #{currency}, #{totalAmount}, #{taxAmount}, #{taxIncludedAmount}, #{status},
              #{versionNo}, #{version}, #{releasedAt}, #{cancelReason}, 0, #{operatorId}, #{operatorId}, now(3), now(3)
            )
            """)
    void insertHeader(@Param("id") long id, @Param("orderNo") String orderNo, @Param("purchaseType") int purchaseType,
                      @Param("supplierId") long supplierId, @Param("supplierCode") String supplierCode,
                      @Param("supplierName") String supplierName, @Param("purchaseOrgId") long purchaseOrgId,
                      @Param("warehouseCode") String warehouseCode, @Param("currency") String currency,
                      @Param("totalAmount") BigDecimal totalAmount, @Param("taxAmount") BigDecimal taxAmount,
                      @Param("taxIncludedAmount") BigDecimal taxIncludedAmount, @Param("status") int status,
                      @Param("versionNo") int versionNo, @Param("version") int version,
                      @Param("releasedAt") OffsetDateTime releasedAt, @Param("cancelReason") String cancelReason,
                      @Param("operatorId") long operatorId);

    @Update("""
            update purchase_order
            set total_amount = #{totalAmount}, tax_amount = #{taxAmount}, tax_included_amount = #{taxIncludedAmount},
                status = #{status}, version_no = #{versionNo}, version = #{version}, released_at = #{releasedAt},
                cancel_reason = #{cancelReason}, updated_by = #{operatorId}, updated_at = now(3)
            where id = #{id}
            """)
    void updateHeader(@Param("id") long id, @Param("totalAmount") BigDecimal totalAmount,
                      @Param("taxAmount") BigDecimal taxAmount, @Param("taxIncludedAmount") BigDecimal taxIncludedAmount,
                      @Param("status") int status, @Param("versionNo") int versionNo, @Param("version") int version,
                      @Param("releasedAt") OffsetDateTime releasedAt, @Param("cancelReason") String cancelReason,
                      @Param("operatorId") long operatorId);

    @Delete("delete from purchase_order_line where order_id = #{orderId}")
    void deleteLines(long orderId);

    @Insert("""
            insert into purchase_order_line(
              line_id, order_id, sku_code, sku_name, order_qty, unit_price, tax_rate, tax_included_price,
              required_delivery_date, received_qty, deleted, created_at, updated_at
            ) values (
              #{lineId}, #{orderId}, #{skuCode}, #{skuName}, #{orderQty}, #{unitPrice}, #{taxRate}, #{taxIncludedPrice},
              #{requiredDeliveryDate}, #{receivedQty}, 0, now(3), now(3)
            )
            """)
    void insertLine(LineRow row);
}
