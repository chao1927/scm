package com.chaobo.scm.iam.application;

import java.util.Optional;

/**
 * Online session boundary. Redis failures must propagate and fail authentication closed.
 *
 * @author chaobo
 */
public interface TokenCachePort {

    /**
     * Stores the latest online-session token family.
     *
     * @param session session snapshot
     */
    void store(OnlineSession session);

    /**
     * Finds an active session by access-token identifier.
     *
     * @param accessJti access-token identifier
     * @return matching online session
     */
    Optional<OnlineSession> findByAccessJti(String accessJti);

    /**
     * Finds an active session by refresh-token identifier.
     *
     * @param refreshJti refresh-token identifier
     * @return matching online session
     */
    Optional<OnlineSession> findByRefreshJti(String refreshJti);

    /**
     * Atomically rotates the refresh-token family.
     *
     * @param presentedRefreshJti currently presented token identifier
     * @param replacement replacement session
     * @return rotation result
     */
    RotationResult rotate(String presentedRefreshJti, OnlineSession replacement);

    /**
     * Removes a revoked session from the online cache.
     *
     * @param sessionId session to revoke
     */
    void revoke(long sessionId);

    record OnlineSession(long sessionId, long userId, String accessJti, String refreshJti,
                         long generation, long accessExpiresAtEpochSecond,
                         long refreshExpiresAtEpochSecond, boolean active) {
    }

    enum RotationResult {
        /** Rotation succeeded. */
        ROTATED,
        /** An already-rotated token was presented. */
        REPLAY_DETECTED,
        /** Token family was not found. */
        NOT_FOUND
    }
}
