package com.chaobo.scm.supplier.application.profile;

import com.chaobo.scm.supplier.domain.profile.ProfileFieldChange;
import java.time.OffsetDateTime;
import java.util.List;

public final class ProfileViews {
    private ProfileViews() {}
    public record Profile(long supplierId, String supplierCode, String supplierName, int lifecycleStatus,
                          int riskLevel, String profileJson, int version, OffsetDateTime updatedAt) {}
    public record Change(long changeId, String changeNo, long supplierId, int status, String statusName,
                         String reason, String withdrawReason, int profileVersion, int version,
                         OffsetDateTime createdAt, List<ProfileFieldChange> changedFields) {}
}
