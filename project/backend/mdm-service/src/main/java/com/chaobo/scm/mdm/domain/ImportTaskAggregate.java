package com.chaobo.scm.mdm.domain;

import java.util.ArrayList;
import java.util.List;

public class ImportTaskAggregate {
    public static final int PENDING = 1;
    public static final int VALIDATED = 2;
    public static final int EXECUTED = 3;
    public static final int COMPLETED = 4;
    public static final int PARTIAL_FAILED = 5;
    public static final int FAILED = 6;
    public static final int CANCELLED = 7;

    private final String importTaskNo;
    private final String typeCode;
    private final String fileName;
    private final String fileUrl;
    private final String fileHash;
    private final String importMode;
    private final boolean validateOnly;
    private final String duplicatePolicy;
    private int status;
    private int totalCount;
    private int successCount;
    private int failedCount;
    private String errorFileUrl;
    private String reason;
    private long version;
    private final List<MdmEvent> events = new ArrayList<>();

    private ImportTaskAggregate(String importTaskNo, String typeCode, String fileName, String fileUrl,
                                String fileHash, String importMode, boolean validateOnly, String duplicatePolicy,
                                int status, int totalCount, int successCount, int failedCount, String errorFileUrl,
                                String reason, long version) {
        if (blank(importTaskNo) || blank(typeCode) || blank(fileName) || blank(fileUrl)
                || blank(fileHash) || blank(importMode)) {
            throw new IllegalArgumentException("import task references are required");
        }
        this.importTaskNo = importTaskNo;
        this.typeCode = typeCode;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileHash = fileHash;
        this.importMode = importMode;
        this.validateOnly = validateOnly;
        this.duplicatePolicy = blank(duplicatePolicy) ? "REJECT" : duplicatePolicy;
        this.status = status;
        this.totalCount = totalCount;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.errorFileUrl = errorFileUrl;
        this.reason = reason;
        this.version = version;
    }

    public static ImportTaskAggregate create(String importTaskNo, String typeCode, String fileName, String fileUrl,
                                             String fileHash, String importMode, boolean validateOnly,
                                             String duplicatePolicy) {
        ImportTaskAggregate aggregate = new ImportTaskAggregate(importTaskNo, typeCode, fileName, fileUrl, fileHash,
                importMode, validateOnly, duplicatePolicy, PENDING, 0, 0, 0, null, null, 1);
        aggregate.events.add(MdmEvent.of("ImportTaskCreated", importTaskNo, typeCode + "|" + fileName));
        return aggregate;
    }

    public static ImportTaskAggregate restore(String importTaskNo, String typeCode, String fileName, String fileUrl,
                                              String fileHash, String importMode, boolean validateOnly,
                                              String duplicatePolicy, int status, int totalCount, int successCount,
                                              int failedCount, String errorFileUrl, String reason, long version) {
        return new ImportTaskAggregate(importTaskNo, typeCode, fileName, fileUrl, fileHash, importMode, validateOnly,
                duplicatePolicy, status, totalCount, successCount, failedCount, errorFileUrl, reason, version);
    }

    public void validateFile(int totalCount, int failedCount, String errorFileUrl, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != PENDING) {
            throw new IllegalStateException("import task cannot be validated");
        }
        if (totalCount < 0 || failedCount < 0 || failedCount > totalCount) {
            throw new IllegalArgumentException("invalid import counts");
        }
        this.totalCount = totalCount;
        this.failedCount = failedCount;
        this.successCount = totalCount - failedCount;
        this.errorFileUrl = errorFileUrl;
        status = VALIDATED;
        version++;
        events.add(MdmEvent.of("ImportFileValidated", importTaskNo, totalCount + "|" + failedCount));
    }

    public void execute(long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != VALIDATED) {
            throw new IllegalStateException("import task is not validated");
        }
        status = EXECUTED;
        version++;
        events.add(MdmEvent.of("ImportTaskExecuted", importTaskNo, importMode));
    }

    public void complete(long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != EXECUTED) {
            throw new IllegalStateException("import task is not executed");
        }
        if (totalCount == 0 || successCount == 0) {
            status = FAILED;
        } else if (failedCount > 0) {
            status = PARTIAL_FAILED;
        } else {
            status = COMPLETED;
        }
        version++;
        events.add(MdmEvent.of("ImportTaskCompleted", importTaskNo, successCount + "|" + failedCount));
    }

    public void cancel(String reason, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != PENDING && status != VALIDATED) {
            throw new IllegalStateException("import task cannot be cancelled");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("cancel reason is required");
        }
        this.reason = reason;
        status = CANCELLED;
        version++;
        events.add(MdmEvent.of("ImportTaskCancelled", importTaskNo, reason));
    }

    public List<MdmEvent> pullEvents() {
        List<MdmEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String importTaskNo() { return importTaskNo; }
    public String typeCode() { return typeCode; }
    public String fileName() { return fileName; }
    public String fileUrl() { return fileUrl; }
    public String fileHash() { return fileHash; }
    public String importMode() { return importMode; }
    public boolean validateOnly() { return validateOnly; }
    public String duplicatePolicy() { return duplicatePolicy; }
    public int status() { return status; }
    public int totalCount() { return totalCount; }
    public int successCount() { return successCount; }
    public int failedCount() { return failedCount; }
    public String errorFileUrl() { return errorFileUrl; }
    public String reason() { return reason; }
    public long version() { return version; }

    private void ensureVersion(long expectedVersion) {
        if (version != expectedVersion) {
            throw new IllegalStateException("import task version conflict");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
