package com.chaobo.scm.integration.domain;

public class IntegrationMessageAggregate {
    public static final int PENDING = 1;
    public static final int DISPATCHED = 2;
    public static final int FAILED = 3;
    public static final int DEAD_LETTER = 4;
    public static final int REPLAYED = 5;

    private final String messageNo;
    private final String messageType;
    private final String sourceSystem;
    private final String targetSystem;
    private final String businessNo;
    private final String idempotencyKey;
    private final String payload;
    private int status;
    private int retryCount;
    private String failureReason;
    private long version;

    private IntegrationMessageAggregate(String messageNo, String messageType, String sourceSystem,
                                        String targetSystem, String businessNo, String idempotencyKey,
                                        String payload, int status, int retryCount, String failureReason,
                                        long version) {
        if (blank(messageNo) || blank(messageType) || blank(sourceSystem) || blank(targetSystem)
                || blank(businessNo) || blank(idempotencyKey) || blank(payload)) {
            throw new IllegalArgumentException("integration message references are required");
        }
        this.messageNo = messageNo;
        this.messageType = messageType;
        this.sourceSystem = sourceSystem;
        this.targetSystem = targetSystem;
        this.businessNo = businessNo;
        this.idempotencyKey = idempotencyKey;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.failureReason = failureReason;
        this.version = version;
    }

    public static IntegrationMessageAggregate create(String messageNo, String messageType, String sourceSystem,
                                                     String targetSystem, String businessNo, String idempotencyKey,
                                                     String payload) {
        return new IntegrationMessageAggregate(messageNo, messageType, sourceSystem, targetSystem, businessNo,
                idempotencyKey, payload, PENDING, 0, null, 1);
    }

    public static IntegrationMessageAggregate restore(String messageNo, String messageType, String sourceSystem,
                                                      String targetSystem, String businessNo, String idempotencyKey,
                                                      String payload, int status, int retryCount,
                                                      String failureReason, long version) {
        return new IntegrationMessageAggregate(messageNo, messageType, sourceSystem, targetSystem, businessNo,
                idempotencyKey, payload, status, retryCount, failureReason, version);
    }

    public void markDispatched(long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != PENDING && status != FAILED) {
            throw new IllegalStateException("integration message cannot dispatch");
        }
        status = DISPATCHED;
        failureReason = null;
        version++;
    }

    public boolean markFailed(String reason, int maxRetry, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (blank(reason)) {
            throw new IllegalArgumentException("failure reason is required");
        }
        retryCount++;
        failureReason = reason;
        status = retryCount >= maxRetry ? DEAD_LETTER : FAILED;
        version++;
        return status == DEAD_LETTER;
    }

    public void retry(long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != FAILED) {
            throw new IllegalStateException("only failed message can retry");
        }
        status = PENDING;
        version++;
    }

    public void markReplayed(long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != DEAD_LETTER) {
            throw new IllegalStateException("only dead letter can be replayed");
        }
        status = REPLAYED;
        version++;
    }

    public String messageNo() { return messageNo; }
    public String messageType() { return messageType; }
    public String sourceSystem() { return sourceSystem; }
    public String targetSystem() { return targetSystem; }
    public String businessNo() { return businessNo; }
    public String idempotencyKey() { return idempotencyKey; }
    public String payload() { return payload; }
    public int status() { return status; }
    public int retryCount() { return retryCount; }
    public String failureReason() { return failureReason; }
    public long version() { return version; }

    private void ensureVersion(long expectedVersion) {
        if (version != expectedVersion) {
            throw new IllegalStateException("integration message version conflict");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
