package com.chaobo.scm.iam.domain;

public class IamSsoClientAggregate {
    public static final int ENABLED = 1;
    public static final int DISABLED = 2;

    private final String ssoCode;
    private final String appCode;
    private String redirectUrl;
    private String secretHash;
    private int status;
    private long version;

    private IamSsoClientAggregate(String ssoCode, String appCode, String redirectUrl, String secretHash,
                                  int status, long version) {
        if (blank(ssoCode) || blank(appCode) || blank(redirectUrl) || blank(secretHash)) {
            throw new IllegalArgumentException("sso client references are required");
        }
        this.ssoCode = ssoCode;
        this.appCode = appCode;
        this.redirectUrl = redirectUrl;
        this.secretHash = secretHash;
        this.status = status;
        this.version = version;
    }

    public static IamSsoClientAggregate configure(String ssoCode, String appCode, String redirectUrl,
                                                  String secretHash) {
        return new IamSsoClientAggregate(ssoCode, appCode, redirectUrl, secretHash, ENABLED, 1);
    }

    public static IamSsoClientAggregate restore(String ssoCode, String appCode, String redirectUrl,
                                                String secretHash, int status, long version) {
        return new IamSsoClientAggregate(ssoCode, appCode, redirectUrl, secretHash, status, version);
    }

    public void resetSecret(String secretHash, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (blank(secretHash)) {
            throw new IllegalArgumentException("secret hash is required");
        }
        this.secretHash = secretHash;
        version++;
    }

    public void disable(long expectedVersion) {
        ensureVersion(expectedVersion);
        status = DISABLED;
        version++;
    }

    public String ssoCode() { return ssoCode; }
    public String appCode() { return appCode; }
    public String redirectUrl() { return redirectUrl; }
    public String secretHash() { return secretHash; }
    public int status() { return status; }
    public long version() { return version; }

    private void ensureVersion(long expectedVersion) {
        if (version != expectedVersion) {
            throw new IllegalStateException("sso client version conflict");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
