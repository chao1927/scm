package com.chaobo.scm.mdm.domain;

import java.util.ArrayList;
import java.util.List;

public class DataQualityIssueAggregate {
    public static final int OPEN = 1;
    public static final int ASSIGNED = 2;
    public static final int FIXED = 3;
    public static final int VERIFIED = 4;
    public static final int CLOSED = 5;

    private final String issueNo;
    private final String typeCode;
    private final String dataCode;
    private final String issueType;
    private final String issueDescription;
    private int status;
    private Long assigneeId;
    private String resolution;
    private long version;
    private final List<MdmEvent> events = new ArrayList<>();

    private DataQualityIssueAggregate(String issueNo, String typeCode, String dataCode, String issueType,
                                      String issueDescription, int status, Long assigneeId, String resolution,
                                      long version) {
        if (blank(issueNo) || blank(typeCode) || blank(dataCode) || blank(issueType) || blank(issueDescription)) {
            throw new IllegalArgumentException("quality issue references are required");
        }
        this.issueNo = issueNo;
        this.typeCode = typeCode;
        this.dataCode = dataCode;
        this.issueType = issueType;
        this.issueDescription = issueDescription;
        this.status = status;
        this.assigneeId = assigneeId;
        this.resolution = resolution;
        this.version = version;
    }

    public static DataQualityIssueAggregate raise(String issueNo, String typeCode, String dataCode, String issueType,
                                                  String issueDescription) {
        DataQualityIssueAggregate aggregate = new DataQualityIssueAggregate(issueNo, typeCode, dataCode, issueType,
                issueDescription, OPEN, null, null, 1);
        aggregate.events.add(MdmEvent.of("DataQualityIssueRaised", issueNo, typeCode + "|" + dataCode));
        return aggregate;
    }

    public static DataQualityIssueAggregate restore(String issueNo, String typeCode, String dataCode, String issueType,
                                                    String issueDescription, int status, Long assigneeId,
                                                    String resolution, long version) {
        return new DataQualityIssueAggregate(issueNo, typeCode, dataCode, issueType, issueDescription, status,
                assigneeId, resolution, version);
    }

    public void assign(Long assigneeId, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != OPEN) {
            throw new IllegalStateException("quality issue is not open");
        }
        if (assigneeId == null) {
            throw new IllegalArgumentException("assignee is required");
        }
        this.assigneeId = assigneeId;
        status = ASSIGNED;
        version++;
        events.add(MdmEvent.of("DataQualityIssueAssigned", issueNo, String.valueOf(assigneeId)));
    }

    public void markFixed(String resolution, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != ASSIGNED) {
            throw new IllegalStateException("quality issue is not assigned");
        }
        if (blank(resolution)) {
            throw new IllegalArgumentException("resolution is required");
        }
        this.resolution = resolution;
        status = FIXED;
        version++;
        events.add(MdmEvent.of("DataQualityIssueFixed", issueNo, resolution));
    }

    public void verify(long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != FIXED) {
            throw new IllegalStateException("quality issue is not fixed");
        }
        status = VERIFIED;
        version++;
        events.add(MdmEvent.of("DataQualityIssueVerified", issueNo, resolution));
    }

    public void close(long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != VERIFIED) {
            throw new IllegalStateException("quality issue is not verified");
        }
        status = CLOSED;
        version++;
        events.add(MdmEvent.of("DataQualityIssueClosed", issueNo, resolution));
    }

    public List<MdmEvent> pullEvents() {
        List<MdmEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String issueNo() { return issueNo; }
    public String typeCode() { return typeCode; }
    public String dataCode() { return dataCode; }
    public String issueType() { return issueType; }
    public String issueDescription() { return issueDescription; }
    public int status() { return status; }
    public Long assigneeId() { return assigneeId; }
    public String resolution() { return resolution; }
    public long version() { return version; }

    private void ensureVersion(long expectedVersion) {
        if (version != expectedVersion) {
            throw new IllegalStateException("quality issue version conflict");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
