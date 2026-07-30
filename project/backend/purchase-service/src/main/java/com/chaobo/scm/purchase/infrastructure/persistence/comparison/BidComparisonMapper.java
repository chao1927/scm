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

/**
 * BidComparisonMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface BidComparisonMapper {

    /**
     * HeaderRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record HeaderRow(long id, String compareNo, String rfqNo, long purchaseOrgId, String currency, int status, Long awardedCandidateId, String decisionReason, Long decidedBy, OffsetDateTime decidedAt, int version, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    }

    /**
     * CandidateRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record CandidateRow(long candidateId, long comparisonId, long supplierId, String supplierName, String quoteNo, String skuCode, BigDecimal quoteQty, BigDecimal unitPrice, BigDecimal taxRate, int deliveryDays, BigDecimal supplierScore, BigDecimal transportScore, BigDecimal estimatedFreightCost, BigDecimal totalCost, BigDecimal compositeScore, boolean awarded) {
    }

    /**
     * 查询并返回 {@code findByNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param compareNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code HeaderRow}
     */
    @Select("select * from purchase_bid_comparison where compare_no = #{compareNo} and deleted = 0")
    HeaderRow findByNo(String compareNo);

    /**
     * 查询并返回 {@code findCandidates}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param comparisonId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code List<CandidateRow>}
     */
    @Select("select * from purchase_bid_candidate where comparison_id = #{comparisonId} and deleted = 0 order by composite_score desc")
    List<CandidateRow> findCandidates(long comparisonId);

    /**
     * 处理当前类型职责中的操作 {@code insertHeader}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param compareNo 可追踪业务编码，类型为 {@code String}
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param awardedCandidateId 业务或技术标识，类型为 {@code Long}
     * @param decisionReason 业务处理参数或成员，类型为 {@code String}
     * @param decidedBy 业务或技术标识，类型为 {@code Long}
     * @param decidedAt 业务或技术标识，类型为 {@code OffsetDateTime}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Insert("""
        insert into purchase_bid_comparison(
          id, compare_no, rfq_no, purchase_org_id, currency, status, awarded_candidate_id,
          decision_reason, decided_by, decided_at, version, deleted, created_by, updated_by, created_at, updated_at
        ) values (
          #{id}, #{compareNo}, #{rfqNo}, #{purchaseOrgId}, #{currency}, #{status}, #{awardedCandidateId},
          #{decisionReason}, #{decidedBy}, #{decidedAt}, #{version}, 0, #{operatorId}, #{operatorId}, now(3), now(3)
        )
        """)
    void insertHeader(@Param("id") long id, @Param("compareNo") String compareNo, @Param("rfqNo") String rfqNo, @Param("purchaseOrgId") long purchaseOrgId, @Param("currency") String currency, @Param("status") int status, @Param("awardedCandidateId") Long awardedCandidateId, @Param("decisionReason") String decisionReason, @Param("decidedBy") Long decidedBy, @Param("decidedAt") OffsetDateTime decidedAt, @Param("version") int version, @Param("operatorId") long operatorId);

    /**
     * 执行命令 {@code updateHeader}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param status 生命周期状态，类型为 {@code int}
     * @param awardedCandidateId 业务或技术标识，类型为 {@code Long}
     * @param decisionReason 业务处理参数或成员，类型为 {@code String}
     * @param decidedBy 业务或技术标识，类型为 {@code Long}
     * @param decidedAt 业务或技术标识，类型为 {@code OffsetDateTime}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
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
    void updateHeader(@Param("id") long id, @Param("status") int status, @Param("awardedCandidateId") Long awardedCandidateId, @Param("decisionReason") String decisionReason, @Param("decidedBy") Long decidedBy, @Param("decidedAt") OffsetDateTime decidedAt, @Param("version") int version, @Param("operatorId") long operatorId);

    /**
     * 执行命令 {@code deleteCandidates}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param comparisonId 业务或技术标识，类型为 {@code long}
     */
    @Delete("delete from purchase_bid_candidate where comparison_id = #{comparisonId}")
    void deleteCandidates(long comparisonId);

    /**
     * 处理当前类型职责中的操作 {@code insertCandidate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code CandidateRow}
     */
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
