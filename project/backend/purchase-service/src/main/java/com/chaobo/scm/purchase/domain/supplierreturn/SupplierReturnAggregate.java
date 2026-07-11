package com.chaobo.scm.purchase.domain.supplierreturn;

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

public class SupplierReturnAggregate {
    private final long id;
    private final String returnNo;
    private final String sourceOrderNo;
    private final long supplierId;
    private final long purchaseOrgId;
    private final String warehouseCode;
    private SupplierReturnStatus status;
    private String rejectReason;
    private int version;
    private final List<SupplierReturnLine> lines;
    private final List<DomainEvent> events = new ArrayList<>();

    public SupplierReturnAggregate(long id, String returnNo, String sourceOrderNo, long supplierId, long purchaseOrgId,
                                   String warehouseCode, SupplierReturnStatus status, String rejectReason, int version,
                                   List<SupplierReturnLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "退供申请必须至少包含一行");
        }
        this.id = id;
        this.returnNo = returnNo;
        this.sourceOrderNo = sourceOrderNo;
        this.supplierId = supplierId;
        this.purchaseOrgId = purchaseOrgId;
        this.warehouseCode = warehouseCode;
        this.status = status;
        this.rejectReason = rejectReason;
        this.version = version;
        this.lines = new ArrayList<>(lines);
    }

    public static SupplierReturnAggregate create(String sourceOrderNo, long supplierId, long purchaseOrgId,
                                                 String warehouseCode, List<SupplierReturnLine> lines,
                                                 IdentifierGenerator ids) {
        var aggregate = new SupplierReturnAggregate(ids.nextId(), ids.nextCode("SRET"), sourceOrderNo, supplierId,
                purchaseOrgId, warehouseCode, SupplierReturnStatus.CREATED, null, 0, lines);
        aggregate.raise("SupplierReturnCreated", Map.of());
        return aggregate;
    }

    public void submit(IdentifierGenerator ids) {
        ensureStatus(SupplierReturnStatus.CREATED, SupplierReturnStatus.REJECTED);
        version++;
        status = SupplierReturnStatus.SUBMITTED;
        raise("SupplierReturnSubmitted", Map.of());
    }

    public void approve(boolean approved, String reason, IdentifierGenerator ids) {
        ensureStatus(SupplierReturnStatus.SUBMITTED);
        version++;
        if (approved) {
            status = SupplierReturnStatus.APPROVED;
            raise("SupplierReturnApproved", Map.of());
        } else {
            status = SupplierReturnStatus.REJECTED;
            rejectReason = reason;
            raise("SupplierReturnRejected", Map.of("reason", Objects.requireNonNullElse(reason, "")));
        }
    }

    public void notifyExecution(String notifyMode, IdentifierGenerator ids) {
        ensureStatus(SupplierReturnStatus.APPROVED);
        version++;
        status = SupplierReturnStatus.EXECUTION_NOTIFIED;
        raise("SupplierReturnExecutionNotified", Map.of("notifyMode", Objects.requireNonNullElse(notifyMode, "EVENT")));
    }

    public List<DomainEvent> pullEvents() {
        var pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    private void ensureStatus(SupplierReturnStatus... allowed) {
        for (SupplierReturnStatus candidate : allowed) {
            if (status == candidate) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.STATE_CONFLICT, "当前退供状态不允许执行该操作");
    }

    private void raise(String eventType, Map<String, Object> extra) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("returnId", id);
        payload.put("returnNo", returnNo);
        payload.put("sourceOrderNo", sourceOrderNo);
        payload.put("supplierId", supplierId);
        payload.put("purchaseOrgId", purchaseOrgId);
        payload.put("warehouseCode", Objects.requireNonNullElse(warehouseCode, ""));
        payload.put("status", status.code());
        payload.put("version", version);
        payload.putAll(extra);
        events.add(new DomainEvent(0, "PUR-" + eventType + "-" + id + "-" + version, eventType,
                "SUPPLIER_RETURN", Long.toString(id), version, OffsetDateTime.now(), payload));
    }

    public long id() { return id; }
    public String returnNo() { return returnNo; }
    public String sourceOrderNo() { return sourceOrderNo; }
    public long supplierId() { return supplierId; }
    public long purchaseOrgId() { return purchaseOrgId; }
    public String warehouseCode() { return warehouseCode; }
    public SupplierReturnStatus status() { return status; }
    public String rejectReason() { return rejectReason; }
    public int version() { return version; }
    public List<SupplierReturnLine> lines() { return List.copyOf(lines); }
}
