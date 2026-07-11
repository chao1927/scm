package com.chaobo.scm.purchase.domain.requisition;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.domain.shared.DomainEvent;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PurchaseRequisitionAggregate {
    private final long id;
    private final String requisitionNo;
    private final long applicantId;
    private final long purchaseOrgId;
    private final long demandDepartmentId;
    private PurchaseRequisitionStatus status;
    private String reason;
    private int version;
    private final List<PurchaseRequisitionLine> lines;
    private final List<DomainEvent> events = new ArrayList<>();

    public PurchaseRequisitionAggregate(
            long id,
            String requisitionNo,
            long applicantId,
            long purchaseOrgId,
            long demandDepartmentId,
            PurchaseRequisitionStatus status,
            String reason,
            int version,
            List<PurchaseRequisitionLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "请购单必须至少包含一行商品");
        }
        this.id = id;
        this.requisitionNo = requisitionNo;
        this.applicantId = applicantId;
        this.purchaseOrgId = purchaseOrgId;
        this.demandDepartmentId = demandDepartmentId;
        this.status = status;
        this.reason = reason;
        this.version = version;
        this.lines = new ArrayList<>(lines);
        assertNoDuplicateSkuAndDate(this.lines);
    }

    public static PurchaseRequisitionAggregate create(
            long applicantId,
            long purchaseOrgId,
            long demandDepartmentId,
            String reason,
            List<PurchaseRequisitionLine> lines,
            IdentifierGenerator ids) {
        var aggregate = new PurchaseRequisitionAggregate(
                ids.nextId(),
                ids.nextCode("PR"),
                applicantId,
                purchaseOrgId,
                demandDepartmentId,
                PurchaseRequisitionStatus.DRAFT,
                reason,
                0,
                lines);
        aggregate.raise("PurchaseRequisitionCreated");
        return aggregate;
    }

    public void changeDraft(String reason, List<PurchaseRequisitionLine> newLines, IdentifierGenerator ids) {
        ensureStatus(PurchaseRequisitionStatus.DRAFT, PurchaseRequisitionStatus.REJECTED);
        assertNoDuplicateSkuAndDate(newLines);
        this.reason = reason;
        this.lines.clear();
        this.lines.addAll(newLines);
        touch();
        raise("PurchaseRequisitionChanged");
    }

    public void submit(IdentifierGenerator ids) {
        ensureStatus(PurchaseRequisitionStatus.DRAFT, PurchaseRequisitionStatus.REJECTED);
        touch();
        this.status = PurchaseRequisitionStatus.SUBMITTED;
        raise("PurchaseRequisitionSubmitted");
    }

    public void approve(Map<Long, BigDecimal> approvedQuantities, IdentifierGenerator ids) {
        ensureStatus(PurchaseRequisitionStatus.SUBMITTED);
        for (PurchaseRequisitionLine line : lines) {
            line.approve(approvedQuantities.getOrDefault(line.lineId(), line.requestedQty()));
        }
        if (lines.stream().allMatch(line -> line.approvedQty().signum() == 0)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "批准数量不能全部为0");
        }
        touch();
        this.status = PurchaseRequisitionStatus.APPROVED;
        raise("PurchaseRequisitionApproved");
    }

    public void reject(String rejectReason, IdentifierGenerator ids) {
        ensureStatus(PurchaseRequisitionStatus.SUBMITTED);
        if (rejectReason == null || rejectReason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "驳回原因不能为空");
        }
        this.reason = rejectReason;
        touch();
        this.status = PurchaseRequisitionStatus.REJECTED;
        raise("PurchaseRequisitionRejected");
    }

    public void convert(Map<Long, BigDecimal> quantities, String targetType, String targetNo, IdentifierGenerator ids) {
        ensureStatus(PurchaseRequisitionStatus.APPROVED, PurchaseRequisitionStatus.PARTIALLY_CONVERTED);
        if (quantities == null || quantities.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "转采购行不能为空");
        }
        for (Map.Entry<Long, BigDecimal> entry : quantities.entrySet()) {
            line(entry.getKey()).convert(entry.getValue());
        }
        touch();
        this.status = allConverted() ? PurchaseRequisitionStatus.CONVERTED : PurchaseRequisitionStatus.PARTIALLY_CONVERTED;
        raise("PurchaseRequisitionConverted", Map.of(
                "targetType", Objects.requireNonNullElse(targetType, ""),
                "targetNo", Objects.requireNonNullElse(targetNo, "")));
    }

    public List<DomainEvent> pullEvents() {
        var pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    private void ensureStatus(PurchaseRequisitionStatus... allowed) {
        for (PurchaseRequisitionStatus candidate : allowed) {
            if (status == candidate) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.STATE_CONFLICT, "当前请购状态不允许执行该操作");
    }

    private PurchaseRequisitionLine line(long lineId) {
        return lines.stream()
                .filter(line -> line.lineId() == lineId)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "请购行不存在"));
    }

    private boolean allConverted() {
        return lines.stream().allMatch(line -> line.remainingApprovedQty().signum() == 0);
    }

    private void touch() {
        this.version++;
    }

    private void raise(String eventType) {
        raise(eventType, Map.of());
    }

    private void raise(String eventType, Map<String, Object> extra) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("requisitionId", id);
        payload.put("requisitionNo", requisitionNo);
        payload.put("purchaseOrgId", purchaseOrgId);
        payload.put("demandDepartmentId", demandDepartmentId);
        payload.put("status", status.code());
        payload.put("version", version);
        payload.putAll(extra);
        events.add(new DomainEvent(
                0,
                "PUR-" + eventType + "-" + id + "-" + version,
                eventType,
                "PURCHASE_REQUISITION",
                Long.toString(id),
                version,
                OffsetDateTime.now(),
                payload));
    }

    private static void assertNoDuplicateSkuAndDate(List<PurchaseRequisitionLine> lines) {
        var keys = new java.util.HashSet<String>();
        for (PurchaseRequisitionLine line : lines) {
            var key = line.skuCode() + "|" + line.requiredDate();
            if (!keys.add(key)) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "同一SKU和需求日期不能重复");
            }
        }
    }

    public long id() {
        return id;
    }

    public String requisitionNo() {
        return requisitionNo;
    }

    public long applicantId() {
        return applicantId;
    }

    public long purchaseOrgId() {
        return purchaseOrgId;
    }

    public long demandDepartmentId() {
        return demandDepartmentId;
    }

    public PurchaseRequisitionStatus status() {
        return status;
    }

    public String reason() {
        return reason;
    }

    public int version() {
        return version;
    }

    public List<PurchaseRequisitionLine> lines() {
        return List.copyOf(lines);
    }
}
