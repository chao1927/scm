package com.chaobo.scm.iam.application;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

final class TestTokenCache implements TokenCachePort {

    final Map<Long, OnlineSession> sessions = new LinkedHashMap<>();
    boolean unavailable;

    @Override
    public void store(OnlineSession session) {
        requireAvailable();
        sessions.put(session.sessionId(), session);
    }

    @Override
    public Optional<OnlineSession> findByAccessJti(String accessJti) {
        requireAvailable();
        return sessions.values().stream().filter(v -> v.accessJti().equals(accessJti)).findFirst();
    }

    @Override
    public Optional<OnlineSession> findByRefreshJti(String refreshJti) {
        requireAvailable();
        return sessions.values().stream()
                .filter(v -> v.refreshJti().equals(refreshJti) || refreshJti.startsWith("RT-" + v.sessionId()))
                .findFirst();
    }

    @Override
    public RotationResult rotate(String presentedRefreshJti, OnlineSession replacement) {
        requireAvailable();
        OnlineSession current = sessions.get(replacement.sessionId());
        if (current == null || !current.active()) {
            return RotationResult.NOT_FOUND;
        }
        if (!current.refreshJti().equals(presentedRefreshJti)) {
            sessions.put(current.sessionId(), new OnlineSession(current.sessionId(), current.userId(),
                    current.accessJti(), current.refreshJti(), current.generation(), current.accessExpiresAtEpochSecond(),
                    current.refreshExpiresAtEpochSecond(), false));
            return RotationResult.REPLAY_DETECTED;
        }
        sessions.put(replacement.sessionId(), replacement);
        return RotationResult.ROTATED;
    }

    @Override
    public void revoke(long sessionId) {
        requireAvailable();
        OnlineSession current = sessions.get(sessionId);
        if (current != null) {
            sessions.put(sessionId, new OnlineSession(current.sessionId(), current.userId(), current.accessJti(),
                    current.refreshJti(), current.generation(), current.accessExpiresAtEpochSecond(),
                    current.refreshExpiresAtEpochSecond(), false));
        }
    }

    private void requireAvailable() {
        if (unavailable) {
            throw new IllegalStateException("redis unavailable");
        }
    }
}
