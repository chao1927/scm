package com.chaobo.scm.supplier.infrastructure.persistence.workbench;

import com.chaobo.scm.supplier.application.workbench.SupplierWorkbenchView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface SupplierWorkbenchMapper {
    @Select("""
            <script>
            SELECT COUNT(*) FROM sup_quote_todo WHERE status = 1
            <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
            </script>
            """)
    long pendingQuotes(@Param("supplierId") Long supplierId);

    @Select("""
            <script>
            SELECT COUNT(*) FROM sup_order WHERE confirm_status = 1 AND deleted = 0
            <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
            </script>
            """)
    long pendingPurchaseOrderConfirms(@Param("supplierId") Long supplierId);

    @Select("""
            <script>
            SELECT COUNT(*) FROM sup_asn WHERE asn_status IN (1, 2, 3) AND deleted = 0
            <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
            </script>
            """)
    long pendingAsns(@Param("supplierId") Long supplierId);

    @Select("""
            <script>
            SELECT COUNT(*) FROM sup_reconciliation WHERE status IN (1, 3) AND deleted = 0
            <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
            </script>
            """)
    long pendingReconciliations(@Param("supplierId") Long supplierId);

    @Select("""
            <script>
            SELECT COUNT(*) FROM sup_quality_issue WHERE issue_status IN (2, 3) AND deleted = 0
            <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
            </script>
            """)
    long pendingRectifications(@Param("supplierId") Long supplierId);

    @Select("""
            <script>
            SELECT COUNT(*) FROM sup_warning WHERE status IN (1, 2)
            <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
            </script>
            """)
    long openWarnings(@Param("supplierId") Long supplierId);

    @Select("""
            SELECT
              (SELECT COUNT(*) FROM sup_event_consume_log WHERE consume_status = 3) +
              (SELECT COUNT(*) FROM sup_domain_event WHERE event_status = 4)
            """)
    long failedEvents();

    @Select("""
            <script>
            SELECT COUNT(*) FROM sup_supplier_return WHERE return_status NOT IN (10, 11) AND deleted = 0
            <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
            </script>
            """)
    long openReturns(@Param("supplierId") Long supplierId);

    @Select("""
            <script>
            SELECT COALESCE(AVG(total_score), 0) FROM sup_score_result WHERE status = 2
            <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
            <if test="since != null">AND updated_at &gt;= #{since}</if>
            </script>
            """)
    BigDecimal latestScore(@Param("supplierId") Long supplierId, @Param("since") OffsetDateTime since);

    @Select("""
            <script>
            SELECT work_type type, COUNT(*) count FROM sup_work_item WHERE status IN (1, 2)
            <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
            GROUP BY work_type ORDER BY count DESC
            </script>
            """)
    List<SupplierWorkbenchView.TodoGroup> todoGroups(@Param("supplierId") Long supplierId);

    @Select("""
            <script>
            SELECT CASE warning_level WHEN 3 THEN '严重' WHEN 2 THEN '预警' ELSE '提醒' END level, COUNT(*) count
            FROM sup_warning WHERE status IN (1, 2)
            <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
            GROUP BY warning_level ORDER BY warning_level DESC
            </script>
            """)
    List<SupplierWorkbenchView.WarningGroup> warningGroups(@Param("supplierId") Long supplierId);
}
