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

public class BidComparisonAggregate {
    private final long id;
    private final String compareNo;
    private final String rfqNo;
    private final long purchaseOrgId;
    private final String currency;
    private BidComparisonStatus status;
    private Long awardedCandidateId;
    private String decisionReason;
    private Long decidedBy;
    private OffsetDateTime decidedAt;
    private int version;
    private final List<BidCandidate> candidates;
    private final List<DomainEvent> events = new ArrayList<>();

    public BidComparisonAggregate(
            long id,
            String compareNo,
            String rfqNo,
            long purchaseOrgId,
            String currency,
            BidComparisonStatus status,
            Long awardedCandidateId,
            String decisionReason,
            Long decidedBy,
            OffsetDateTime decidedAt,
            int version,
            List<BidCandidate> candidates) {
        if (rfqNo == null || rfqNo.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "RFQ单号不能为空");
        }
        if (purchaseOrgId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "采购组织不能为空");
        }
        if (candidates == null || candidates.size() < 2) {
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

    public static BidComparisonAggregate generate(
            String rfqNo,
            long purchaseOrgId,
            String currency,
            List<BidCandidate> candidates,
            IdentifierGenerator ids) {
        var aggregate = new BidComparisonAggregate(
                ids.nextId(),
                ids.nextCode("CMP"),
                rfqNo,
                purchaseOrgId,
                currency,
                BidComparisonStatus.GENERATED,
                null,
                null,
                null,
                null,
                0,
                candidates);
        aggregate.raise("CompareResultGenerated", Map.of("recommendedCandidateId", aggregate.recommended().candidateId()));
        return aggregate;
    }

    public BidCandidate recommended() {
        return candidates.stream()
                .max(Comparator.comparing(BidCandidate::compositeScore))
                .orElseThrow();
    }

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
        raise("CompareResultAwarded", Map.of(
                "awardedCandidateId", candidateId,
                "supplierId", winner.supplierId(),
                "quoteNo", winner.quoteNo(),
                "skuCode", winner.skuCode()));
        return winner;
    }

    public List<DomainEvent> pullEvents() {
        var pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    private BidCandidate candidate(long candidateId) {
        return candidates.stream()
                .filter(candidate -> candidate.candidateId() == candidateId)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "中标候选报价不在比价池内"));
    }

    private void ensureStatus(BidComparisonStatus expected) {
        if (status != expected) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "当前比价状态不允许执行该操作");
        }
    }

    private void touch() {
        version++;
    }

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
        events.add(new DomainEvent(
                0,
                "PUR-" + eventType + "-" + id + "-" + version,
                eventType,
                "BID_COMPARISON",
                Long.toString(id),
                version,
                OffsetDateTime.now(),
                payload));
    }

    private void assertSameCurrency(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "比价币种不能为空");
        }
    }

    public long id() {
        return id;
    }

    public String compareNo() {
        return compareNo;
    }

    public String rfqNo() {
        return rfqNo;
    }

    public long purchaseOrgId() {
        return purchaseOrgId;
    }

    public String currency() {
        return currency;
    }

    public BidComparisonStatus status() {
        return status;
    }

    public Long awardedCandidateId() {
        return awardedCandidateId;
    }

    public String decisionReason() {
        return decisionReason;
    }

    public Long decidedBy() {
        return decidedBy;
    }

    public OffsetDateTime decidedAt() {
        return decidedAt;
    }

    public int version() {
        return version;
    }

    public List<BidCandidate> candidates() {
        return List.copyOf(candidates);
    }
}
