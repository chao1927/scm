package com.chaobo.scm.purchase.application.comparison;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * BidComparisonView。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record BidComparisonView(long id, String compareNo, String rfqNo, long purchaseOrgId, String currency, int status, String statusName, Long awardedCandidateId, String decisionReason, Long decidedBy, OffsetDateTime decidedAt, int version, List<Candidate> candidates) {

    /**
     * 处理当前类型职责中的操作 {@code Candidate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param candidateId 业务或技术标识，类型为 {@code long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param supplierName 业务处理参数或成员，类型为 {@code String}
     * @param quoteNo 可追踪业务编码，类型为 {@code String}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param quoteQty 数量值，类型为 {@code BigDecimal}
     * @param unitPrice 金额或计费值，类型为 {@code BigDecimal}
     * @param taxRate 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param deliveryDays 业务处理参数或成员，类型为 {@code int}
     * @param supplierScore 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param transportScore 应用或外部协作依赖，类型为 {@code BigDecimal}
     * @param estimatedFreightCost 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param totalCost 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param compositeScore 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param awarded 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code record}
     */
    public record Candidate(long candidateId, long supplierId, String supplierName, String quoteNo, String skuCode, BigDecimal quoteQty, BigDecimal unitPrice, BigDecimal taxRate, int deliveryDays, BigDecimal supplierScore, BigDecimal transportScore, BigDecimal estimatedFreightCost, BigDecimal totalCost, BigDecimal compositeScore, boolean awarded) {
    }
}
