package com.chaobo.scm.purchase.infrastructure.persistence.inbound;

import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface InboundTrackingMapper {
    record Row(long id, String inboundNo, String orderNo, String asnNo, long supplierId, long purchaseOrgId,
               String warehouseCode, String skuCode, BigDecimal notifiedQty, BigDecimal receivedQty,
               BigDecimal qualifiedQty, BigDecimal unqualifiedQty, BigDecimal putawayQty, int status,
               String exceptionReason, int version) {
    }

    @Select("select * from purchase_inbound_tracking where inbound_no = #{inboundNo} and deleted = 0")
    Row findByNo(String inboundNo);

    @Select("select * from purchase_inbound_tracking where asn_no = #{asnNo} and deleted = 0")
    Row findByAsnNo(String asnNo);

    @Insert("""
            insert into purchase_inbound_tracking(
              id, inbound_no, order_no, asn_no, supplier_id, purchase_org_id, warehouse_code, sku_code,
              notified_qty, received_qty, qualified_qty, unqualified_qty, putaway_qty, status, exception_reason,
              version, deleted, created_by, updated_by, created_at, updated_at
            ) values (
              #{id}, #{inboundNo}, #{orderNo}, #{asnNo}, #{supplierId}, #{purchaseOrgId}, #{warehouseCode}, #{skuCode},
              #{notifiedQty}, #{receivedQty}, #{qualifiedQty}, #{unqualifiedQty}, #{putawayQty}, #{status},
              #{exceptionReason}, #{version}, 0, #{operatorId}, #{operatorId}, now(3), now(3)
            )
            """)
    void insert(Row row, @Param("operatorId") long operatorId);

    @Update("""
            update purchase_inbound_tracking
            set received_qty = #{receivedQty}, qualified_qty = #{qualifiedQty}, unqualified_qty = #{unqualifiedQty},
                putaway_qty = #{putawayQty}, status = #{status}, exception_reason = #{exceptionReason},
                version = #{version}, updated_by = #{operatorId}, updated_at = now(3)
            where id = #{id}
            """)
    void update(Row row, @Param("operatorId") long operatorId);

    @Select("""
            <script>
            select count(1) from purchase_inbound_tracking where deleted = 0
            <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
            <if test="orderNo != null and orderNo != ''">and order_no = #{orderNo}</if>
            <if test="asnNo != null and asnNo != ''">and asn_no = #{asnNo}</if>
            <if test="warehouseCode != null and warehouseCode != ''">and warehouse_code = #{warehouseCode}</if>
            <if test="status != null">and status = #{status}</if>
            </script>
            """)
    long count(@Param("purchaseOrgId") Long purchaseOrgId, @Param("orderNo") String orderNo,
               @Param("asnNo") String asnNo, @Param("warehouseCode") String warehouseCode,
               @Param("status") Integer status);

    @Select("""
            <script>
            select * from purchase_inbound_tracking where deleted = 0
            <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
            <if test="orderNo != null and orderNo != ''">and order_no = #{orderNo}</if>
            <if test="asnNo != null and asnNo != ''">and asn_no = #{asnNo}</if>
            <if test="warehouseCode != null and warehouseCode != ''">and warehouse_code = #{warehouseCode}</if>
            <if test="status != null">and status = #{status}</if>
            order by updated_at desc
            limit #{offset}, #{limit}
            </script>
            """)
    List<Row> page(@Param("purchaseOrgId") Long purchaseOrgId, @Param("orderNo") String orderNo,
                   @Param("asnNo") String asnNo, @Param("warehouseCode") String warehouseCode,
                   @Param("status") Integer status, @Param("offset") int offset, @Param("limit") int limit);
}
