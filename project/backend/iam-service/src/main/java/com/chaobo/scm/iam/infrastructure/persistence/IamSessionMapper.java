package com.chaobo.scm.iam.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/** Persistence boundary for security-sensitive session transitions. */
@Mapper
public interface IamSessionMapper {

    @Insert("""
            insert into iam_session(session_id,user_id,access_token,refresh_token,access_jti,refresh_jti,
            refresh_generation,access_expires_at,refresh_expires_at,session_status,version,created_at,updated_at)
            values(#{sessionId},#{userId},#{accessToken},#{refreshToken},#{accessJti},#{refreshJti},
            #{generation},from_unixtime(#{accessExpiresAt}),from_unixtime(#{refreshExpiresAt}),1,0,now(3),now(3))
            """)
    void insert(SessionWrite row);

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

    @Update("""
            update iam_session set session_status=2,revoked_at=now(3),revocation_reason=#{reason},
            version=version+1,updated_at=now(3) where session_id=#{sessionId} and session_status=1
            """)
    int revoke(@Param("sessionId") long sessionId, @Param("reason") String reason);

    record SessionWrite(long sessionId, long userId, String accessToken, String refreshToken,
                        String accessJti, String refreshJti, long generation,
                        long accessExpiresAt, long refreshExpiresAt) {
    }
}
