package com.chaobo.scm.purchase.domain.rfq;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.domain.shared.DomainEvent;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RfqAggregate {
    private final long id;
    private final String rfqNo;
    private final int rfqType;
    private final long purchaseOrgId;
    private final String categoryCode;
    private final String sourceRequisitionNo;
    private OffsetDateTime quoteDeadline;
    private RfqStatus status;
    private OffsetDateTime publishedAt;
    private String closeReason;
    private int version;
    private final List<RfqLine> lines;
    private final List<RfqInvitation> invitations;
    private final List<DomainEvent> events = new ArrayList<>();

    public RfqAggregate(
            long id,
            String rfqNo,
            int rfqType,
            long purchaseOrgId,
            String categoryCode,
            String sourceRequisitionNo,
            OffsetDateTime quoteDeadline,
            RfqStatus status,
            OffsetDateTime publishedAt,
            String closeReason,
            int version,
            List<RfqLine> lines,
            List<RfqInvitation> invitations) {
        validateType(rfqType);
        validateDeadline(quoteDeadline);
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "询价单必须至少包含一行商品");
        }
        if (invitations == null || invitations.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "询价单必须邀请供应商");
        }
        this.id = id;
        this.rfqNo = rfqNo;
        this.rfqType = rfqType;
        this.purchaseOrgId = purchaseOrgId;
        this.categoryCode = categoryCode;
        this.sourceRequisitionNo = sourceRequisitionNo;
        this.quoteDeadline = quoteDeadline;
        this.status = status;
        this.publishedAt = publishedAt;
        this.closeReason = closeReason;
        this.version = version;
        this.lines = new ArrayList<>(lines);
        this.invitations = new ArrayList<>(invitations);
        assertNoDuplicateSku(this.lines);
        assertNoDuplicateSupplier(this.invitations);
    }

    public static RfqAggregate create(
            int rfqType,
            long purchaseOrgId,
            String categoryCode,
            String sourceRequisitionNo,
            OffsetDateTime quoteDeadline,
            List<RfqLine> lines,
            List<RfqInvitation> invitations,
            IdentifierGenerator ids) {
        var aggregate = new RfqAggregate(
                ids.nextId(),
                ids.nextCode("RFQ"),
                rfqType,
                purchaseOrgId,
                categoryCode,
                sourceRequisitionNo,
                quoteDeadline,
                RfqStatus.DRAFT,
                null,
                null,
                0,
                lines,
                invitations);
        aggregate.raise("RfqCreated", Map.of(), "");
        return aggregate;
    }

    public void publish(IdentifierGenerator ids) {
        ensureStatus(RfqStatus.DRAFT);
        if (!quoteDeadline.isAfter(OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "报价截止时间必须晚于当前时间");
        }
        touch();
        this.status = RfqStatus.QUOTING;
        this.publishedAt = OffsetDateTime.now();
        for (RfqInvitation invitation : invitations) {
            raise("RfqPublished", Map.of("supplierId", invitation.supplierId()), "-" + invitation.supplierId());
        }
    }

    public void closeBidding(String reason, IdentifierGenerator ids) {
        ensureStatus(RfqStatus.PUBLISHED, RfqStatus.QUOTING);
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "截标原因不能为空");
        }
        touch();
        this.status = RfqStatus.BIDDING_CLOSED;
        this.closeReason = reason;
        for (RfqInvitation invitation : invitations) {
            invitation.closeTodo();
        }
        raise("RfqBiddingClosed", Map.of("closeReason", reason), "");
    }

    public List<DomainEvent> pullEvents() {
        var pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    private void ensureStatus(RfqStatus... allowed) {
        for (RfqStatus candidate : allowed) {
            if (status == candidate) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.STATE_CONFLICT, "当前询价状态不允许执行该操作");
    }

    private void touch() {
        this.version++;
    }

    private void raise(String eventType, Map<String, Object> extra, String eventSuffix) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("rfqId", id);
        payload.put("rfqNo", rfqNo);
        payload.put("rfqType", rfqType);
        payload.put("purchaseOrgId", purchaseOrgId);
        payload.put("categoryCode", Objects.requireNonNullElse(categoryCode, ""));
        payload.put("sourceRequisitionNo", Objects.requireNonNullElse(sourceRequisitionNo, ""));
        payload.put("quoteDeadline", quoteDeadline.toString());
        payload.put("status", status.code());
        payload.put("version", version);
        payload.put("supplierIds", invitations.stream().map(RfqInvitation::supplierId).toList().toString());
        payload.putAll(extra);
        events.add(new DomainEvent(
                0,
                "PUR-" + eventType + "-" + id + "-" + version + eventSuffix,
                eventType,
                "RFQ",
                Long.toString(id),
                version,
                OffsetDateTime.now(),
                payload));
    }

    private static void validateType(int rfqType) {
        if (rfqType < 1 || rfqType > 3) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "询价类型不合法");
        }
    }

    private static void validateDeadline(OffsetDateTime quoteDeadline) {
        if (quoteDeadline == null || !quoteDeadline.isAfter(OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "报价截止时间必须晚于当前时间");
        }
    }

    private static void assertNoDuplicateSku(List<RfqLine> lines) {
        var keys = new java.util.HashSet<String>();
        for (RfqLine line : lines) {
            if (!keys.add(line.skuCode())) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "询价行SKU不能重复");
            }
        }
    }

    private static void assertNoDuplicateSupplier(List<RfqInvitation> invitations) {
        var supplierIds = new java.util.HashSet<Long>();
        for (RfqInvitation invitation : invitations) {
            if (!supplierIds.add(invitation.supplierId())) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "邀请供应商不能重复");
            }
        }
    }

    public long id() {
        return id;
    }

    public String rfqNo() {
        return rfqNo;
    }

    public int rfqType() {
        return rfqType;
    }

    public long purchaseOrgId() {
        return purchaseOrgId;
    }

    public String categoryCode() {
        return categoryCode;
    }

    public String sourceRequisitionNo() {
        return sourceRequisitionNo;
    }

    public OffsetDateTime quoteDeadline() {
        return quoteDeadline;
    }

    public RfqStatus status() {
        return status;
    }

    public OffsetDateTime publishedAt() {
        return publishedAt;
    }

    public String closeReason() {
        return closeReason;
    }

    public int version() {
        return version;
    }

    public List<RfqLine> lines() {
        return List.copyOf(lines);
    }

    public List<RfqInvitation> invitations() {
        return List.copyOf(invitations);
    }
}
