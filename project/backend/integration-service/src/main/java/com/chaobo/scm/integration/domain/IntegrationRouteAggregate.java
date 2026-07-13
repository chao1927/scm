package com.chaobo.scm.integration.domain;

public class IntegrationRouteAggregate {
    public static final int ENABLED = 1;
    public static final int DISABLED = 2;

    private final String routeNo;
    private final String messageType;
    private final String sourceSystem;
    private final String targetSystem;
    private final String channelType;
    private int status;
    private long version;

    private IntegrationRouteAggregate(String routeNo, String messageType, String sourceSystem, String targetSystem,
                                      String channelType, int status, long version) {
        if (blank(routeNo) || blank(messageType) || blank(sourceSystem) || blank(targetSystem)
                || blank(channelType)) {
            throw new IllegalArgumentException("integration route references are required");
        }
        this.routeNo = routeNo;
        this.messageType = messageType;
        this.sourceSystem = sourceSystem;
        this.targetSystem = targetSystem;
        this.channelType = channelType;
        this.status = status;
        this.version = version;
    }

    public static IntegrationRouteAggregate create(String routeNo, String messageType, String sourceSystem,
                                                   String targetSystem, String channelType) {
        return new IntegrationRouteAggregate(routeNo, messageType, sourceSystem, targetSystem, channelType,
                ENABLED, 1);
    }

    public static IntegrationRouteAggregate restore(String routeNo, String messageType, String sourceSystem,
                                                    String targetSystem, String channelType, int status,
                                                    long version) {
        return new IntegrationRouteAggregate(routeNo, messageType, sourceSystem, targetSystem, channelType,
                status, version);
    }

    public void disable(long expectedVersion) {
        ensureVersion(expectedVersion);
        status = DISABLED;
        version++;
    }

    public String routeNo() { return routeNo; }
    public String messageType() { return messageType; }
    public String sourceSystem() { return sourceSystem; }
    public String targetSystem() { return targetSystem; }
    public String channelType() { return channelType; }
    public int status() { return status; }
    public long version() { return version; }

    private void ensureVersion(long expectedVersion) {
        if (version != expectedVersion) {
            throw new IllegalStateException("integration route version conflict");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
