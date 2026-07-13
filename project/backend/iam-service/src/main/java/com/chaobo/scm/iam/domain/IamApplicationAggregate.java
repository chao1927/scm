package com.chaobo.scm.iam.domain;

public class IamApplicationAggregate {
    public static final int ENABLED = 1;
    public static final int DISABLED = 2;

    private final String appCode;
    private String appName;
    private String homeUrl;
    private int status;
    private long version;

    private IamApplicationAggregate(String appCode, String appName, String homeUrl, int status, long version) {
        if (blank(appCode) || blank(appName)) {
            throw new IllegalArgumentException("application code and name are required");
        }
        this.appCode = appCode;
        this.appName = appName;
        this.homeUrl = homeUrl;
        this.status = status;
        this.version = version;
    }

    public static IamApplicationAggregate create(String appCode, String appName, String homeUrl) {
        return new IamApplicationAggregate(appCode, appName, homeUrl, ENABLED, 1);
    }

    public static IamApplicationAggregate restore(String appCode, String appName, String homeUrl, int status,
                                                  long version) {
        return new IamApplicationAggregate(appCode, appName, homeUrl, status, version);
    }

    public void change(String appName, String homeUrl, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (blank(appName)) {
            throw new IllegalArgumentException("application name is required");
        }
        this.appName = appName;
        this.homeUrl = homeUrl;
        version++;
    }

    public void enable(long expectedVersion) {
        ensureVersion(expectedVersion);
        status = ENABLED;
        version++;
    }

    public void disable(long expectedVersion) {
        ensureVersion(expectedVersion);
        status = DISABLED;
        version++;
    }

    public String appCode() { return appCode; }
    public String appName() { return appName; }
    public String homeUrl() { return homeUrl; }
    public int status() { return status; }
    public long version() { return version; }

    private void ensureVersion(long expectedVersion) {
        if (version != expectedVersion) {
            throw new IllegalStateException("application version conflict");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
