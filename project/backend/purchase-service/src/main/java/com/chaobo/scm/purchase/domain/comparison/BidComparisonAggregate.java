package com.chaobo.scm.purchase.domain.comparison;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.domain.shared.DomainEvent;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * BidComparisonAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class BidComparisonAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * compareNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String compareNo;

    /**
     * rfqNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String rfqNo;

    /**
     * purchaseOrgId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long purchaseOrgId;

    /**
     * currency（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String currency;

    /**
     * status（类型：{@code BidComparisonStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private BidComparisonStatus status;

    /**
     * awardedCandidateId（类型：{@code Long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private Long awardedCandidateId;

    /**
     * decisionReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String decisionReason;

    /**
     * decidedBy（类型：{@code Long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private Long decidedBy;

    /**
     * decidedAt（类型：{@code OffsetDateTime}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private OffsetDateTime decidedAt;

    /**
     * version（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private int version;

    /**
     * candidates（类型：{@code List<BidCandidate>}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final List<BidCandidate> candidates;

    /**
     * events（类型：{@code List<DomainEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * 创建 BidComparisonAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param compareNo 可追踪业务编码，类型为 {@code String}
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code BidComparisonStatus}
     * @param awardedCandidateId 业务或技术标识，类型为 {@code Long}
     * @param decisionReason 业务处理参数或成员，类型为 {@code String}
     * @param decidedBy 业务或技术标识，类型为 {@code Long}
     * @param decidedAt 业务或技术标识，类型为 {@code OffsetDateTime}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param candidates 业务或技术标识，类型为 {@code List<BidCandidate>}
     */
    public BidComparisonAggregate(long id, String compareNo, String rfqNo, long purchaseOrgId, String currency, BidComparisonStatus status, Long awardedCandidateId, String decisionReason, Long decidedBy, OffsetDateTime decidedAt, int version, List<BidCandidate> candidates) {
        if (rfqNo == null || rfqNo.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "RFQ单号不能为空");
        }
        if (purchaseOrgId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "采购组织不能为空");
        }
        if (candidates == null || candidates.size() < BUSINESS_VALUE_2) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "生成比价至少需要两个候选报价");
        }
        this.id = id;
        this.compareNo = compareNo;
        this.rfqNo = rfqNo;
        this.purchaseOrgId = purchaseOrgId;
        this.currency = currency;
        this.status = status;
        this.awardedCandidateId = awardedCandidateId;
        this.decisionReason = decisionReason;
        this.decidedBy = decidedBy;
        this.decidedAt = decidedAt;
        this.version = version;
        this.candidates = new ArrayList<>(candidates);
        assertSameCurrency(currency);
    }

    /**
     * 处理当前类型职责中的操作 {@code generate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param candidates 业务或技术标识，类型为 {@code List<BidCandidate>}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BidComparisonAggregate}
     */
    public static BidComparisonAggregate generate(String rfqNo, long purchaseOrgId, String currency, List<BidCandidate> candidates, IdentifierGenerator ids) {
        var aggregate = new BidComparisonAggregate(ids.nextId(), ids.nextCode("CMP"), rfqNo, purchaseOrgId, currency, BidComparisonStatus.GENERATED, null, null, null, null, 0, candidates);
        aggregate.raise("CompareResultGenerated", Map.of("recommendedCandidateId", aggregate.recommended().candidateId()));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code recommended}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BidCandidate}
     */
    public BidCandidate recommended() {
        return candidates.stream().max(Comparator.comparing(BidCandidate::compositeScore)).orElseThrow();
    }

    /**
     * 处理当前类型职责中的操作 {@code award}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param candidateId 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BidCandidate}
     */
    public BidCandidate award(long candidateId, String reason, long operatorId, IdentifierGenerator ids) {
        ensureStatus(BidComparisonStatus.GENERATED);
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "定标理由不能为空");
        }
        var winner = candidate(candidateId);
        for (BidCandidate candidate : candidates) {
            candidate.clearAward();
        }
        winner.award();
        touch();
        this.status = BidComparisonStatus.AWARDED;
        this.awardedCandidateId = candidateId;
        this.decisionReason = reason;
        this.decidedBy = operatorId;
        this.decidedAt = OffsetDateTime.now();
        raise("CompareResultAwarded", Map.of("awardedCandidateId", candidateId, "supplierId", winner.supplierId(), "quoteNo", winner.quoteNo(), "skuCode", winner.skuCode()));
        return winner;
    }

    /**
     * 处理当前类型职责中的操作 {@code pullEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<DomainEvent>}
     */
    public List<DomainEvent> pullEvents() {
        var pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    /**
     * 处理当前类型职责中的操作 {@code candidate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param candidateId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BidCandidate}
     */
    private BidCandidate candidate(long candidateId) {
        return candidates.stream().filter(candidate -> candidate.candidateId() == candidateId).findFirst().orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "中标候选报价不在比价池内"));
    }

    /**
     * 校验业务约束 {@code ensureStatus}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param expected 业务处理参数或成员，类型为 {@code BidComparisonStatus}
     */
    private void ensureStatus(BidComparisonStatus expected) {
        if (status != expected) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "当前比价状态不允许执行该操作");
        }
    }

    /**
     * 转换数据模型 {@code touch}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     */
    private void touch() {
        version++;
    }

    /**
     * 处理当前类型职责中的操作 {@code raise}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param extra 业务处理参数或成员，类型为 {@code Map<String,Object>}
     */
    private void raise(String eventType, Map<String, Object> extra) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("compareId", id);
        payload.put("compareNo", compareNo);
        payload.put("rfqNo", rfqNo);
        payload.put("purchaseOrgId", purchaseOrgId);
        payload.put("currency", Objects.requireNonNullElse(currency, ""));
        payload.put("status", status.code());
        payload.put("version", version);
        payload.putAll(extra);
        events.add(new DomainEvent(0, "PUR-" + eventType + "-" + id + "-" + version, eventType, "BID_COMPARISON", Long.toString(id), version, OffsetDateTime.now(), payload));
    }

    /**
     * 处理当前类型职责中的操作 {@code assertSameCurrency}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     */
    private void assertSameCurrency(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "比价币种不能为空");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code id}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long id() {
        return id;
    }

    /**
     * 处理当前类型职责中的操作 {@code compareNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String compareNo() {
        return compareNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code rfqNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String rfqNo() {
        return rfqNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code purchaseOrgId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long purchaseOrgId() {
        return purchaseOrgId;
    }

    /**
     * 处理当前类型职责中的操作 {@code currency}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String currency() {
        return currency;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BidComparisonStatus}
     */
    public BidComparisonStatus status() {
        return status;
    }

    /**
     * 处理当前类型职责中的操作 {@code awardedCandidateId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    public Long awardedCandidateId() {
        return awardedCandidateId;
    }

    /**
     * 处理当前类型职责中的操作 {@code decisionReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String decisionReason() {
        return decisionReason;
    }

    /**
     * 处理当前类型职责中的操作 {@code decidedBy}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    public Long decidedBy() {
        return decidedBy;
    }

    /**
     * 处理当前类型职责中的操作 {@code decidedAt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OffsetDateTime}
     */
    public OffsetDateTime decidedAt() {
        return decidedAt;
    }

    /**
     * 处理当前类型职责中的操作 {@code version}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int version() {
        return version;
    }

    /**
     * 处理当前类型职责中的操作 {@code candidates}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<BidCandidate>}
     */
    public List<BidCandidate> candidates() {
        return List.copyOf(candidates);
    }

    /**
     * 业务常量 {@code BUSINESS_VALUE_2}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int BUSINESS_VALUE_2 = 2;
}
