package com.chaobo.scm.iam.application;

import java.util.Optional;

/** Online session boundary. Redis failures must propagate and fail authentication closed. */
public interface TokenCachePort {

    void store(OnlineSession session);

    Optional<OnlineSession> findByAccessJti(String accessJti);

    Optional<OnlineSession> findByRefreshJti(String refreshJti);

    RotationResult rotate(String presentedRefreshJti, OnlineSession replacement);

    void revoke(long sessionId);

    record OnlineSession(long sessionId, long userId, String accessJti, String refreshJti,
                         long generation, long accessExpiresAtEpochSecond,
                         long refreshExpiresAtEpochSecond, boolean active) {
    }

    enum RotationResult {
        ROTATED,
        REPLAY_DETECTED,
        NOT_FOUND
    }
}
