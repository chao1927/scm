package com.chaobo.scm.purchase.infrastructure.persistence.supplierreturn;

import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface SupplierReturnMapper {
    record HeaderRow(long id, String returnNo, String sourceOrderNo, long supplierId, long purchaseOrgId,
                     String warehouseCode, int status, String rejectReason, int version) {
    }

    record LineRow(long lineId, long returnId, String skuCode, BigDecimal returnQty, BigDecimal returnableQty,
                   String reason) {
    }

    @Select("select * from purchase_supplier_return where return_no = #{returnNo} and deleted = 0")
    HeaderRow findByNo(String returnNo);

    @Select("select * from purchase_supplier_return_line where return_id = #{returnId} and deleted = 0 order by line_id")
    List<LineRow> findLines(long returnId);

    @Insert("""
            insert into purchase_supplier_return(
              id, return_no, source_order_no, supplier_id, purchase_org_id, warehouse_code, status, reject_reason,
              version, deleted, created_by, updated_by, created_at, updated_at
            ) values (
              #{id}, #{returnNo}, #{sourceOrderNo}, #{supplierId}, #{purchaseOrgId}, #{warehouseCode}, #{status},
              #{rejectReason}, #{version}, 0, #{operatorId}, #{operatorId}, now(3), now(3)
            )
            """)
    void insertHeader(@Param("id") long id, @Param("returnNo") String returnNo,
                      @Param("sourceOrderNo") String sourceOrderNo, @Param("supplierId") long supplierId,
                      @Param("purchaseOrgId") long purchaseOrgId, @Param("warehouseCode") String warehouseCode,
                      @Param("status") int status, @Param("rejectReason") String rejectReason,
                      @Param("version") int version, @Param("operatorId") long operatorId);

    @Update("""
            update purchase_supplier_return
            set status = #{status}, reject_reason = #{rejectReason}, version = #{version},
                updated_by = #{operatorId}, updated_at = now(3)
            where id = #{id}
            """)
    void updateHeader(@Param("id") long id, @Param("status") int status, @Param("rejectReason") String rejectReason,
                      @Param("version") int version, @Param("operatorId") long operatorId);

    @Delete("delete from purchase_supplier_return_line where return_id = #{returnId}")
    void deleteLines(long returnId);

    @Insert("""
            insert into purchase_supplier_return_line(
              line_id, return_id, sku_code, return_qty, returnable_qty, reason, deleted, created_at, updated_at
            ) values (
              #{lineId}, #{returnId}, #{skuCode}, #{returnQty}, #{returnableQty}, #{reason}, 0, now(3), now(3)
            )
            """)
    void insertLine(LineRow row);

    @Select("""
            <script>
            select count(1) from purchase_supplier_return where deleted = 0
            <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
            <if test="supplierId != null">and supplier_id = #{supplierId}</if>
            <if test="warehouseCode != null and warehouseCode != ''">and warehouse_code = #{warehouseCode}</if>
            <if test="status != null">and status = #{status}</if>
            </script>
            """)
    long count(@Param("purchaseOrgId") Long purchaseOrgId, @Param("supplierId") Long supplierId,
               @Param("warehouseCode") String warehouseCode, @Param("status") Integer status);

    @Select("""
            <script>
            select * from purchase_supplier_return where deleted = 0
            <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
            <if test="supplierId != null">and supplier_id = #{supplierId}</if>
            <if test="warehouseCode != null and warehouseCode != ''">and warehouse_code = #{warehouseCode}</if>
            <if test="status != null">and status = #{status}</if>
            order by updated_at desc
            limit #{offset}, #{limit}
            </script>
            """)
    List<HeaderRow> page(@Param("purchaseOrgId") Long purchaseOrgId, @Param("supplierId") Long supplierId,
                         @Param("warehouseCode") String warehouseCode, @Param("status") Integer status,
                         @Param("offset") int offset, @Param("limit") int limit);
}
