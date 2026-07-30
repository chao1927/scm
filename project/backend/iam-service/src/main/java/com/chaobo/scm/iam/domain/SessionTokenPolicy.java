package com.chaobo.scm.iam.domain;

/** Protects refresh-token family invariants independently from Redis and SQL details. */
public final class SessionTokenPolicy {

    private SessionTokenPolicy() {
    }

    public static RefreshDecision decideRefresh(boolean active, String currentRefreshJti,
                                                String presentedRefreshJti) {
        if (!active) {
            return RefreshDecision.REVOKED;
        }
        if (!currentRefreshJti.equals(presentedRefreshJti)) {
            return RefreshDecision.REPLAY;
        }
        return RefreshDecision.ALLOWED;
    }

    public enum RefreshDecision {
        ALLOWED,
        REPLAY,
        REVOKED
    }
}
