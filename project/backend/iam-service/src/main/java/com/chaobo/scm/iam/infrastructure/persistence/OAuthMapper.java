package com.chaobo.scm.iam.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.Instant;

/** OAuth client, authorization-code, audit and outbox persistence boundary. */
@Mapper
public interface OAuthMapper {

    @Select("""
            select client_id clientId,app_code appCode,client_type clientType,secret_hash secretHash,
                   redirect_uris redirectUris,grant_types grantTypes,allowed_scopes allowedScopes,
                   access_ttl_seconds accessTtlSeconds,id_token_ttl_seconds idTokenTtlSeconds,
                   client_status status,version
            from iam_oauth_client where client_id=#{clientId} and client_status=1
            """)
    OAuthClientRow findEnabledClient(String clientId);

    @Select("select user_status from iam_user where user_id=#{userId} and user_status=1")
    Integer findEnabledUser(long userId);

    @Insert("""
            insert into iam_oauth_authorization_code(code_hash,client_id,user_id,redirect_uri,scopes,
              code_challenge,nonce,request_id,issued_at,expires_at,consumed_at,created_at)
            values(#{codeHash},#{clientId},#{userId},#{redirectUri},#{scopes},#{codeChallenge},
              #{nonce},#{requestId},#{issuedAt},#{expiresAt},#{consumedAt},now(3))
            """)
    void insertAuthorizationCode(AuthorizationCodeRow row);

    @Select("""
            select code_hash codeHash,client_id clientId,user_id userId,redirect_uri redirectUri,
                   scopes,code_challenge codeChallenge,nonce,request_id requestId,issued_at issuedAt,
                   expires_at expiresAt,consumed_at consumedAt
            from iam_oauth_authorization_code where code_hash=#{codeHash}
            """)
    AuthorizationCodeRow findAuthorizationCode(String codeHash);

    @Update("""
            update iam_oauth_authorization_code set consumed_at=#{consumedAt}
            where code_hash=#{codeHash} and client_id=#{clientId} and redirect_uri=#{redirectUri}
              and consumed_at is null and expires_at>#{consumedAt}
            """)
    int consumeAuthorizationCode(@Param("codeHash") String codeHash,
                                 @Param("clientId") String clientId,
                                 @Param("redirectUri") String redirectUri,
                                 @Param("consumedAt") Instant consumedAt);

    @Insert("""
            insert into iam_oauth_audit(audit_id,action,client_id,user_id,request_id,detail,occurred_at)
            values(#{auditId},#{action},#{clientId},#{userId},#{requestId},#{detail},#{occurredAt})
            """)
    void insertAudit(AuditRow row);

    @Insert("""
            insert into iam_outbox_event(event_id,event_type,business_no,payload,event_status,occurred_at,created_at)
            values(#{eventId},#{eventType},#{businessNo},#{payload},1,#{occurredAt},now(3))
            """)
    void insertOutbox(OutboxRow row);

    @Insert("""
            insert into iam_oauth_grant(grant_id,client_id,user_id,scopes,grant_status,created_at)
            values(#{grantId},#{clientId},#{userId},#{scopes},1,#{createdAt})
            """)
    void insertGrant(GrantRow row);

    @Insert("""
            insert into iam_oauth_refresh_token(token_hash,grant_id,generation,expires_at,consumed_at,created_at)
            values(#{tokenHash},#{grantId},#{generation},#{expiresAt},#{consumedAt},#{createdAt})
            """)
    void insertRefreshToken(RefreshTokenRow row);

    @Select("""
            select r.token_hash tokenHash,r.grant_id grantId,r.generation,r.expires_at expiresAt,
                   r.consumed_at consumedAt,g.client_id clientId,g.user_id userId,g.scopes,
                   g.grant_status grantStatus,g.revoked_at revokedAt
            from iam_oauth_refresh_token r join iam_oauth_grant g on g.grant_id=r.grant_id
            where r.token_hash=#{tokenHash}
            """)
    RefreshGrantRow findRefreshGrant(String tokenHash);

    @Update("""
            update iam_oauth_refresh_token set consumed_at=#{consumedAt}
            where token_hash=#{tokenHash} and consumed_at is null and expires_at>#{consumedAt}
            """)
    int consumeRefreshToken(@Param("tokenHash") String tokenHash,
                            @Param("consumedAt") Instant consumedAt);

    @Update("""
            update iam_oauth_grant set grant_status=2,revoked_at=#{revokedAt}
            where grant_id=#{grantId} and grant_status=1
            """)
    int revokeGrant(@Param("grantId") String grantId, @Param("revokedAt") Instant revokedAt);

    record OAuthClientRow(String clientId, String appCode, String clientType, String secretHash,
                          String redirectUris, String grantTypes, String allowedScopes,
                          int accessTtlSeconds, int idTokenTtlSeconds, int status, int version) { }

    record AuthorizationCodeRow(String codeHash, String clientId, long userId, String redirectUri,
                                String scopes, String codeChallenge, String nonce, String requestId,
                                Instant issuedAt, Instant expiresAt, Instant consumedAt) {
        public AuthorizationCodeRow consumed(Instant at) {
            return new AuthorizationCodeRow(codeHash, clientId, userId, redirectUri, scopes,
                    codeChallenge, nonce, requestId, issuedAt, expiresAt, at);
        }
    }

    record AuditRow(long auditId, String action, String clientId, Long userId, String requestId,
                    String detail, Instant occurredAt) { }

    record OutboxRow(long eventId, String eventType, String businessNo, String payload,
                     Instant occurredAt) { }

    record GrantRow(String grantId, String clientId, long userId, String scopes, Instant createdAt) { }

    record RefreshTokenRow(String tokenHash, String grantId, int generation, Instant expiresAt,
                           Instant consumedAt, Instant createdAt) { }

    record RefreshGrantRow(String tokenHash, String grantId, int generation, Instant expiresAt,
                           Instant consumedAt, String clientId, long userId, String scopes,
                           int grantStatus, Instant revokedAt) { }
}
