package com.chaobo.scm.purchase.infrastructure.persistence.supplierconfirm;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface SupplierConfirmMapper {
    record Row(long confirmId, String eventCode, String orderNo, long supplierId, String confirmStatus,
               String reason, int sourceVersion, int processedStatus, String processComment,
               long purchaseOrgId, int version, OffsetDateTime occurredAt, OffsetDateTime processedAt,
               String payloadJson) {
    }

    @Select("""
            <script>
            select count(*) from purchase_supplier_confirm_fact f
            join purchase_order o on o.order_no = f.order_no and o.deleted = 0
            where 1 = 1
              <if test='purchaseOrgId != null'>and o.purchase_org_id = #{purchaseOrgId}</if>
              <if test='orderNo != null and orderNo != ""'>and f.order_no = #{orderNo}</if>
              <if test='supplierId != null'>and f.supplier_id = #{supplierId}</if>
              <if test='processedStatus != null'>and f.processed_status = #{processedStatus}</if>
            </script>
            """)
    long count(@Param("purchaseOrgId") Long purchaseOrgId, @Param("orderNo") String orderNo,
               @Param("supplierId") Long supplierId, @Param("processedStatus") Integer processedStatus);

    @Select("""
            <script>
            select f.fact_id confirmId, f.event_code eventCode, f.order_no orderNo, f.supplier_id supplierId,
                   f.confirm_status confirmStatus, f.reason, f.source_version sourceVersion,
                   f.processed_status processedStatus, f.process_comment processComment,
                   o.purchase_org_id purchaseOrgId, f.process_version version, f.occurred_at occurredAt,
                   f.processed_at processedAt, cast(f.payload_json as char) payloadJson
            from purchase_supplier_confirm_fact f
            join purchase_order o on o.order_no = f.order_no and o.deleted = 0
            where 1 = 1
              <if test='purchaseOrgId != null'>and o.purchase_org_id = #{purchaseOrgId}</if>
              <if test='orderNo != null and orderNo != ""'>and f.order_no = #{orderNo}</if>
              <if test='supplierId != null'>and f.supplier_id = #{supplierId}</if>
              <if test='processedStatus != null'>and f.processed_status = #{processedStatus}</if>
            order by f.occurred_at desc, f.fact_id desc
            limit #{offset}, #{pageSize}
            </script>
            """)
    List<Row> page(@Param("purchaseOrgId") Long purchaseOrgId, @Param("orderNo") String orderNo,
                   @Param("supplierId") Long supplierId, @Param("processedStatus") Integer processedStatus,
                   @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("""
            select f.fact_id confirmId, f.event_code eventCode, f.order_no orderNo, f.supplier_id supplierId,
                   f.confirm_status confirmStatus, f.reason, f.source_version sourceVersion,
                   f.processed_status processedStatus, f.process_comment processComment,
                   o.purchase_org_id purchaseOrgId, f.process_version version, f.occurred_at occurredAt,
                   f.processed_at processedAt, cast(f.payload_json as char) payloadJson
            from purchase_supplier_confirm_fact f
            join purchase_order o on o.order_no = f.order_no and o.deleted = 0
            where f.fact_id = #{confirmId}
            """)
    Row findById(long confirmId);

    @Update("""
            update purchase_supplier_confirm_fact
            set processed_status = #{processedStatus}, process_comment = #{comment}, processed_by = #{operatorId},
                processed_at = now(3), process_version = process_version + 1, updated_at = now(3)
            where fact_id = #{confirmId} and processed_status = 1 and process_version = #{version}
            """)
    int complete(@Param("confirmId") long confirmId, @Param("version") int version,
                 @Param("processedStatus") int processedStatus, @Param("comment") String comment,
                 @Param("operatorId") long operatorId);
}
