package com.chaobo.scm.iam.application;

import com.chaobo.scm.iam.infrastructure.persistence.IamSessionMapper;

import java.util.LinkedHashMap;
import java.util.Map;

final class TestIamSessionMapper implements IamSessionMapper {

    final Map<Long, SessionWrite> sessions = new LinkedHashMap<>();

    @Override
    public void insert(SessionWrite row) {
        sessions.put(row.sessionId(), row);
    }

    @Override
    public SessionSnapshot find(long sessionId) {
        SessionWrite row = sessions.get(sessionId);
        return row == null ? null : new SessionSnapshot(row.sessionId(), row.userId(),
            row.accessToken(), row.refreshToken());
    }

    @Override
    public java.util.List<SessionGovernanceRow> list(int limit) {
        return java.util.List.of();
    }

    @Override
    public int rotate(long sessionId, String expectedRefreshJti, String accessToken, String refreshToken,
                      String accessJti, String refreshJti, long generation, long accessExpiresAt,
                      long refreshExpiresAt) {
        SessionWrite current = sessions.get(sessionId);
        if (current == null || !current.refreshJti().equals(expectedRefreshJti)) {
            return 0;
        }
        sessions.put(sessionId, new SessionWrite(sessionId, current.userId(), accessToken, refreshToken,
                accessJti, refreshJti, generation, accessExpiresAt, refreshExpiresAt));
        return 1;
    }

    @Override
    public int revoke(long sessionId, String reason) {
        return sessions.containsKey(sessionId) ? 1 : 0;
    }
}
