package com.chaobo.scm.supplier.domain.profile;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.domain.shared.DomainEvent;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ProfileChangeAggregate {
    private final long changeId;
    private final String changeNo;
    private final long supplierId;
    private final int profileVersion;
    private final String reason;
    private final List<ProfileFieldChange> changes;
    private final List<DomainEvent> events = new ArrayList<>();
    private ProfileChangeStatus status;
    private String withdrawReason;
    private int version;

    private ProfileChangeAggregate(long changeId, String changeNo, long supplierId, int profileVersion,
                                   String reason, List<ProfileFieldChange> changes,
                                   ProfileChangeStatus status, String withdrawReason, int version) {
        this.changeId = changeId; this.changeNo = changeNo; this.supplierId = supplierId;
        this.profileVersion = profileVersion; this.reason = reason; this.changes = List.copyOf(changes);
        this.status = status; this.withdrawReason = withdrawReason; this.version = version;
    }

    public static ProfileChangeAggregate submit(long supplierId, int profileVersion, String reason,
                                                List<ProfileFieldChange> changes, long operatorId,
                                                IdentifierGenerator generator) {
        if (supplierId <= 0 || profileVersion < 0) throw rule("供应商和档案版本不合法");
        if (reason == null || reason.isBlank()) throw rule("资料变更原因不能为空");
        if (changes == null || changes.isEmpty()) throw rule("至少提交一个变更字段");
        if (changes.stream().map(ProfileFieldChange::fieldCode).distinct().count() != changes.size()) {
            throw rule("同一字段不能重复提交");
        }
        long id = generator.nextId();
        var aggregate = new ProfileChangeAggregate(id, generator.nextBusinessNo("SPC"), supplierId,
                profileVersion, reason.trim(), changes, ProfileChangeStatus.PENDING, null, 0);
        aggregate.raise(generator, "SupplierProfileChangeSubmitted", "供应商资料变更已提交", operatorId,
                Map.of("supplierId", supplierId, "profileVersion", profileVersion,
                        "changedFields", changes.stream().map(ProfileFieldChange::fieldCode).toList()));
        return aggregate;
    }

    public static ProfileChangeAggregate rehydrate(long id, String no, long supplierId, int profileVersion,
                                                   String reason, List<ProfileFieldChange> changes,
                                                   ProfileChangeStatus status, String withdrawReason, int version) {
        return new ProfileChangeAggregate(id, no, supplierId, profileVersion, reason, changes,
                status, withdrawReason, version);
    }

    public void withdraw(String reason, long operatorId, IdentifierGenerator generator) {
        if (status != ProfileChangeStatus.PENDING) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "只有待审批资料变更可以撤回");
        }
        if (reason == null || reason.isBlank()) throw rule("撤回原因不能为空");
        status = ProfileChangeStatus.WITHDRAWN;
        withdrawReason = reason.trim();
        version++;
        raise(generator, "SupplierProfileChangeWithdrawn", "供应商资料变更已撤回", operatorId,
                Map.of("supplierId", supplierId, "withdrawReason", withdrawReason));
    }

    private void raise(IdentifierGenerator generator, String type, String name, long operatorId,
                       Map<String, Object> payload) {
        long eventId = generator.nextId();
        events.add(new DomainEvent(eventId, "SUP-" + eventId, type, name, "SUPPLIER_PROFILE_CHANGE",
                changeId, changeNo, version, operatorId, OffsetDateTime.now(), payload));
    }

    private static BusinessException rule(String message) {
        return new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, message);
    }

    public List<DomainEvent> pullEvents() { var copy = List.copyOf(events); events.clear(); return copy; }
    public long changeId() { return changeId; } public String changeNo() { return changeNo; }
    public long supplierId() { return supplierId; } public int profileVersion() { return profileVersion; }
    public String reason() { return reason; } public List<ProfileFieldChange> changes() { return changes; }
    public ProfileChangeStatus status() { return status; } public String withdrawReason() { return withdrawReason; }
    public int version() { return version; }
}
