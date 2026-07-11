package com.chaobo.scm.purchase.infrastructure.persistence.comparison;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface BidComparisonMapper {

    record HeaderRow(
            long id,
            String compareNo,
            String rfqNo,
            long purchaseOrgId,
            String currency,
            int status,
            Long awardedCandidateId,
            String decisionReason,
            Long decidedBy,
            OffsetDateTime decidedAt,
            int version,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
    }

    record CandidateRow(
            long candidateId,
            long comparisonId,
            long supplierId,
            String supplierName,
            String quoteNo,
            String skuCode,
            BigDecimal quoteQty,
            BigDecimal unitPrice,
            BigDecimal taxRate,
            int deliveryDays,
            BigDecimal supplierScore,
            BigDecimal transportScore,
            BigDecimal estimatedFreightCost,
            BigDecimal totalCost,
            BigDecimal compositeScore,
            boolean awarded) {
    }

    @Select("select * from purchase_bid_comparison where compare_no = #{compareNo} and deleted = 0")
    HeaderRow findByNo(String compareNo);

    @Select("select * from purchase_bid_candidate where comparison_id = #{comparisonId} and deleted = 0 order by composite_score desc")
    List<CandidateRow> findCandidates(long comparisonId);

    @Insert("""
            insert into purchase_bid_comparison(
              id, compare_no, rfq_no, purchase_org_id, currency, status, awarded_candidate_id,
              decision_reason, decided_by, decided_at, version, deleted, created_by, updated_by, created_at, updated_at
            ) values (
              #{id}, #{compareNo}, #{rfqNo}, #{purchaseOrgId}, #{currency}, #{status}, #{awardedCandidateId},
              #{decisionReason}, #{decidedBy}, #{decidedAt}, #{version}, 0, #{operatorId}, #{operatorId}, now(3), now(3)
            )
            """)
    void insertHeader(
            @Param("id") long id,
            @Param("compareNo") String compareNo,
            @Param("rfqNo") String rfqNo,
            @Param("purchaseOrgId") long purchaseOrgId,
            @Param("currency") String currency,
            @Param("status") int status,
            @Param("awardedCandidateId") Long awardedCandidateId,
            @Param("decisionReason") String decisionReason,
            @Param("decidedBy") Long decidedBy,
            @Param("decidedAt") OffsetDateTime decidedAt,
            @Param("version") int version,
            @Param("operatorId") long operatorId);

    @Update("""
            update purchase_bid_comparison
            set status = #{status},
                awarded_candidate_id = #{awardedCandidateId},
                decision_reason = #{decisionReason},
                decided_by = #{decidedBy},
                decided_at = #{decidedAt},
                version = #{version},
                updated_by = #{operatorId},
                updated_at = now(3)
            where id = #{id}
            """)
    void updateHeader(
            @Param("id") long id,
            @Param("status") int status,
            @Param("awardedCandidateId") Long awardedCandidateId,
            @Param("decisionReason") String decisionReason,
            @Param("decidedBy") Long decidedBy,
            @Param("decidedAt") OffsetDateTime decidedAt,
            @Param("version") int version,
            @Param("operatorId") long operatorId);

    @Delete("delete from purchase_bid_candidate where comparison_id = #{comparisonId}")
    void deleteCandidates(long comparisonId);

    @Insert("""
            insert into purchase_bid_candidate(
              candidate_id, comparison_id, supplier_id, supplier_name, quote_no, sku_code,
              quote_qty, unit_price, tax_rate, delivery_days, supplier_score, transport_score,
              estimated_freight_cost, total_cost, composite_score, awarded, deleted, created_at, updated_at
            ) values (
              #{candidateId}, #{comparisonId}, #{supplierId}, #{supplierName}, #{quoteNo}, #{skuCode},
              #{quoteQty}, #{unitPrice}, #{taxRate}, #{deliveryDays}, #{supplierScore}, #{transportScore},
              #{estimatedFreightCost}, #{totalCost}, #{compositeScore}, #{awarded}, 0, now(3), now(3)
            )
            """)
    void insertCandidate(CandidateRow row);
}
