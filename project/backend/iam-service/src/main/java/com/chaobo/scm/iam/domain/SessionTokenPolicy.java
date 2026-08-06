package com.chaobo.scm.iam.domain;

/**
 * Protects refresh-token family invariants independently from Redis and SQL details.
 *
 * @author chaobo
 */
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
        /** The current refresh token may be rotated. */
        ALLOWED,
        /** A stale token reveals refresh-token replay. */
        REPLAY,
        /** The session family has already been revoked. */
        REVOKED
    }
}
