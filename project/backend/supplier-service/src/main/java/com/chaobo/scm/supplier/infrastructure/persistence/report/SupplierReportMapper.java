package com.chaobo.scm.supplier.infrastructure.persistence.report;

import com.chaobo.scm.supplier.application.report.SupplierReportViews;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SupplierReportMapper {
    @Select("""
            <script>
            SELECT
              COALESCE(#{supplierId}, 0) supplierId,
              (SELECT COUNT(*) FROM sup_order WHERE deleted = 0
                <if test="supplierId != null">AND supplier_id = #{supplierId}</if>) purchaseOrders,
              (SELECT COUNT(*) FROM sup_order WHERE deleted = 0 AND confirm_status = 2
                <if test="supplierId != null">AND supplier_id = #{supplierId}</if>) confirmedOrders,
              (SELECT COUNT(*) FROM sup_order WHERE deleted = 0 AND confirm_status = 1
                <if test="supplierId != null">AND supplier_id = #{supplierId}</if>) pendingOrders,
              (SELECT COUNT(*) FROM sup_asn WHERE deleted = 0
                <if test="supplierId != null">AND supplier_id = #{supplierId}</if>) asnCount,
              (SELECT COUNT(*) FROM sup_asn WHERE deleted = 0 AND asn_status IN (4, 5, 6, 8)
                <if test="supplierId != null">AND supplier_id = #{supplierId}</if>) shippedAsns,
              (SELECT COUNT(*) FROM sup_asn WHERE deleted = 0 AND asn_status IN (6, 8)
                <if test="supplierId != null">AND supplier_id = #{supplierId}</if>) receivedAsns,
              (SELECT COALESCE(SUM(l.planned_qty), 0) FROM sup_asn_line l JOIN sup_asn a ON a.asn_id = l.asn_id
                WHERE l.deleted = 0 AND a.deleted = 0
                <if test="supplierId != null">AND a.supplier_id = #{supplierId}</if>) plannedQty,
              (SELECT COALESCE(SUM(l.received_qty), 0) FROM sup_asn_line l JOIN sup_asn a ON a.asn_id = l.asn_id
                WHERE l.deleted = 0 AND a.deleted = 0
                <if test="supplierId != null">AND a.supplier_id = #{supplierId}</if>) receivedQty,
              (SELECT CASE WHEN COALESCE(SUM(l.planned_qty), 0) = 0 THEN 0
                ELSE ROUND(COALESCE(SUM(l.received_qty), 0) / SUM(l.planned_qty), 4) END
               FROM sup_asn_line l JOIN sup_asn a ON a.asn_id = l.asn_id
               WHERE l.deleted = 0 AND a.deleted = 0
               <if test="supplierId != null">AND a.supplier_id = #{supplierId}</if>) receiveRate
            </script>
            """)
    SupplierReportViews.Fulfillment fulfillment(@Param("supplierId") Long supplierId);

    @Select("""
            <script>
            SELECT
              COALESCE(#{supplierId}, 0) supplierId,
              (SELECT COUNT(*) FROM sup_quality_issue WHERE deleted = 0 AND issue_status != 4
                <if test="supplierId != null">AND supplier_id = #{supplierId}</if>) openQualityIssues,
              (SELECT COUNT(*) FROM sup_quality_issue WHERE deleted = 0 AND issue_status = 5
                <if test="supplierId != null">AND supplier_id = #{supplierId}</if>) overdueQualityIssues,
              (SELECT COUNT(*) FROM sup_supplier_return WHERE deleted = 0 AND return_status NOT IN (10, 11)
                <if test="supplierId != null">AND supplier_id = #{supplierId}</if>) openReturns,
              (SELECT COUNT(*) FROM sup_asn_transport_fact f JOIN sup_asn a ON a.asn_id = f.asn_id
                WHERE f.transport_status = 4 AND a.deleted = 0
                <if test="supplierId != null">AND a.supplier_id = #{supplierId}</if>) transportExceptions,
              (SELECT COUNT(*) FROM sup_warning WHERE status IN (1, 2)
                <if test="supplierId != null">AND supplier_id = #{supplierId}</if>) openWarnings,
              (SELECT COUNT(*) FROM sup_event_consume_log WHERE consume_status = 3) failedInboundEvents,
              (SELECT COUNT(*) FROM sup_domain_event WHERE event_status = 4) failedOutboundEvents
            </script>
            """)
    SupplierReportViews.ExceptionOverview exceptions(@Param("supplierId") Long supplierId);
}
