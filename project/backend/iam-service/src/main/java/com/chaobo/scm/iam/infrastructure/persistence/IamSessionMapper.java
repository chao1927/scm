package com.chaobo.scm.iam.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Persistence boundary for security-sensitive session transitions.
 *
 * @author chaobo
 */
@Mapper
public interface IamSessionMapper {

    /**
     * Persists a newly issued session and both token identifiers.
     *
     * @param row session write model
     */
    @Insert("""
            insert into iam_session(session_id,user_id,access_token,refresh_token,access_jti,refresh_jti,
            refresh_generation,access_expires_at,refresh_expires_at,session_status,version,created_at,updated_at)
            values(#{sessionId},#{userId},#{accessToken},#{refreshToken},#{accessJti},#{refreshJti},
            #{generation},from_unixtime(#{accessExpiresAt}),from_unixtime(#{refreshExpiresAt}),1,0,now(3),now(3))
            """)
    void insert(SessionWrite row);

    /**
     * 按会话标识查询已签发的会话，用于 MFA 登录完成幂等。
     *
     * @param sessionId 会话标识
     * @return 会话快照，不存在时返回 {@code null}
     */
    @Select("select session_id sessionId,user_id userId,access_token accessToken,"
        + "refresh_token refreshToken from iam_session where session_id=#{sessionId}")
    SessionSnapshot find(@Param("sessionId") long sessionId);

    /**
     * 查询会话治理列表，不返回 Token 和 JTI 秘密材料。
     *
     * @param limit 数量上限
     * @return 会话治理读模型
     */
    @Select("select session_id sessionId,user_id userId,refresh_generation generation,"
        + "session_status status,access_expires_at accessExpiresAt,"
        + "refresh_expires_at refreshExpiresAt,revoked_at revokedAt,"
        + "revocation_reason revocationReason from iam_session "
        + "order by updated_at desc limit #{limit}")
    List<SessionGovernanceRow> list(@Param("limit") int limit);

    /**
     * Atomically rotates an active session when the expected refresh JTI still matches.
     *
     * @param sessionId session identifier
     * @param expectedRefreshJti current refresh-token identifier
     * @param accessToken replacement access token
     * @param refreshToken replacement refresh token
     * @param accessJti replacement access-token identifier
     * @param refreshJti replacement refresh-token identifier
     * @param generation next token-family generation
     * @param accessExpiresAt access-token expiration epoch second
     * @param refreshExpiresAt refresh-token expiration epoch second
     * @return affected row count
     */
    @Update("""
            update iam_session set access_token=#{accessToken},refresh_token=#{refreshToken},
            access_jti=#{accessJti},refresh_jti=#{refreshJti},refresh_generation=#{generation},
            access_expires_at=from_unixtime(#{accessExpiresAt}),refresh_expires_at=from_unixtime(#{refreshExpiresAt}),
            version=version+1,updated_at=now(3)
            where session_id=#{sessionId} and refresh_jti=#{expectedRefreshJti} and session_status=1
            """)
    int rotate(@Param("sessionId") long sessionId,
               @Param("expectedRefreshJti") String expectedRefreshJti,
               @Param("accessToken") String accessToken,
               @Param("refreshToken") String refreshToken,
               @Param("accessJti") String accessJti,
               @Param("refreshJti") String refreshJti,
               @Param("generation") long generation,
               @Param("accessExpiresAt") long accessExpiresAt,
               @Param("refreshExpiresAt") long refreshExpiresAt);

    /**
     * Revokes an active session without overwriting an earlier revocation.
     *
     * @param sessionId session identifier
     * @param reason auditable revocation reason
     * @return affected row count
     */
    @Update("""
            update iam_session set session_status=2,revoked_at=now(3),revocation_reason=#{reason},
            version=version+1,updated_at=now(3) where session_id=#{sessionId} and session_status=1
            """)
    int revoke(@Param("sessionId") long sessionId, @Param("reason") String reason);

    record SessionWrite(long sessionId, long userId, String accessToken, String refreshToken,
                        String accessJti, String refreshJti, long generation,
                        long accessExpiresAt, long refreshExpiresAt) {
    }

    /** 会话幂等返回快照。 */
    record SessionSnapshot(long sessionId, long userId, String accessToken, String refreshToken) {
    }

    /** 脱敏的会话治理读模型。 */
    record SessionGovernanceRow(long sessionId, long userId, long generation, int status,
                                LocalDateTime accessExpiresAt, LocalDateTime refreshExpiresAt,
                                LocalDateTime revokedAt, String revocationReason) {
    }
}
