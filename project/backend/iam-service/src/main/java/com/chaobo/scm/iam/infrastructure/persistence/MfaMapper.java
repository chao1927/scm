package com.chaobo.scm.iam.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.Instant;
import java.util.List;

/**
 * MFA challenge persistence and atomic recovery-code consumption.
 *
 * @author chaobo
 */
@SuppressWarnings("PMD.AbstractMethodOrInterfaceMethodMustUseJavadocRule")
@Mapper
public interface MfaMapper {

    @Select("select * from iam_mfa_challenge where idempotency_key=#{key}")
    ChallengeRow findByIdempotencyKey(String key);

    @Select("select * from iam_mfa_challenge where challenge_no=#{challengeNo}")
    ChallengeRow findByChallengeNo(String challengeNo);

    @Insert("""
            insert into iam_mfa_challenge(challenge_id,challenge_no,user_id,app_code,factor_type,
            session_id,purpose,device_digest,secret_ciphertext,challenge_status,failed_attempts,max_attempts,expires_at,idempotency_key,
            version,created_at,updated_at) values(#{id},#{challengeNo},#{userId},#{appCode},'TOTP',
            #{sessionId},#{purpose},#{deviceDigest},#{secretCiphertext},'PENDING',0,#{maxAttempts},#{expiresAt},#{idempotencyKey},0,now(3),now(3))
            """)
    void insertChallenge(ChallengeRow row);

    @Update("""
            update iam_mfa_challenge set challenge_status=#{status},failed_attempts=#{failedAttempts},
            verified_at=#{verifiedAt},version=#{version},updated_at=now(3)
            where challenge_id=#{id} and version=#{oldVersion}
            """)
    int updateChallenge(@Param("id") long id, @Param("status") String status,
                        @Param("failedAttempts") int failedAttempts,
                        @Param("verifiedAt") Instant verifiedAt, @Param("version") int version,
                        @Param("oldVersion") int oldVersion);

    @Update("""
            update iam_mfa_recovery_code set consumed_at=now(3),consumed_challenge_no=#{challengeNo}
            where user_id=#{userId} and code_hash=#{codeHash} and consumed_at is null
            """)
    int consumeRecoveryCode(@Param("userId") long userId, @Param("codeHash") String codeHash,
                            @Param("challengeNo") String challengeNo);

    @Select("select * from iam_mfa_configuration where user_id=#{userId} and config_status=1")
    ConfigurationRow findActiveConfiguration(long userId);

    @Select("select * from iam_mfa_configuration where user_id=#{userId}")
    ConfigurationRow findConfiguration(long userId);

    /** @param limit 数量上限 @return MFA 配置治理读模型 */
    @Select("select config_id configId,user_id userId,config_status status,version "
        + "from iam_mfa_configuration order by updated_at desc limit #{limit}")
    List<ConfigurationGovernanceRow> listConfigurations(@Param("limit") int limit);

    /** @param limit 数量上限 @return MFA 挑战治理读模型 */
    @Select("select challenge_no challengeNo,user_id userId,app_code appCode,session_id sessionId,"
        + "purpose,challenge_status status,failed_attempts failedAttempts,max_attempts maxAttempts,"
        + "expires_at expiresAt,verified_at verifiedAt,version from iam_mfa_challenge "
        + "order by updated_at desc limit #{limit}")
    List<ChallengeGovernanceRow> listChallenges(@Param("limit") int limit);

    @Insert("""
            insert into iam_mfa_configuration(config_id,user_id,secret_ciphertext,config_status,version,created_at,updated_at)
            values(#{configId},#{userId},#{secretCiphertext},#{status},0,now(3),now(3))
            on duplicate key update secret_ciphertext=values(secret_ciphertext),config_status=values(config_status),
              version=version+1,updated_at=now(3)
            """)
    void upsertConfiguration(ConfigurationRow row);

    @Update("update iam_mfa_configuration set config_status=#{status},version=version+1,updated_at=now(3) where user_id=#{userId}")
    int updateConfigurationStatus(@Param("userId") long userId, @Param("status") int status);

    @Update("update iam_mfa_recovery_code set consumed_at=coalesce(consumed_at,now(3)),consumed_challenge_no=#{reason} where user_id=#{userId}")
    int invalidateRecoveryCodes(@Param("userId") long userId, @Param("reason") String reason);

    @Insert("insert into iam_mfa_recovery_code(recovery_code_id,user_id,code_hash,created_at) values(#{id},#{userId},#{codeHash},now(3))")
    void insertRecoveryCode(@Param("id") long id, @Param("userId") long userId, @Param("codeHash") String codeHash);

    @Select("select session_id from iam_session where user_id=#{userId} and session_status=1")
    java.util.List<Long> findActiveSessionIds(long userId);

    @Update("update iam_session set session_status=2,revoked_at=now(3),revocation_reason=#{reason},version=version+1,updated_at=now(3) where user_id=#{userId} and session_status=1")
    int revokeSessions(@Param("userId") long userId, @Param("reason") String reason);

    @Insert("""
            insert into iam_mfa_audit(audit_id,user_id,action,challenge_no,operator_id,reason,occurred_at)
            values(#{auditId},#{userId},#{action},#{challengeNo},#{operatorId},#{reason},#{occurredAt})
            """)
    void insertAudit(AuditRow row);

    @Insert("""
            insert into iam_outbox_event(event_id,event_type,business_no,payload,event_status,occurred_at,created_at)
            values(#{eventId},#{eventType},#{businessNo},#{payload},1,#{occurredAt},now(3))
            """)
    void insertOutbox(OutboxRow row);

    record ChallengeRow(long challengeId, String challengeNo, long userId, String appCode,
                        long sessionId, String purpose, String deviceDigest,
                        String factorType, String secretCiphertext, String challengeStatus,
                        int failedAttempts, int maxAttempts, Instant expiresAt, Instant verifiedAt,
                        String idempotencyKey, int version) {
        public long id() { return challengeId; }
    }

    record ConfigurationRow(long configId, long userId, String secretCiphertext, int status, int version) { }
    record ConfigurationGovernanceRow(long configId, long userId, int status, int version) { }
    record ChallengeGovernanceRow(String challengeNo, long userId, String appCode, long sessionId,
                                  String purpose, String status, int failedAttempts, int maxAttempts,
                                  Instant expiresAt, Instant verifiedAt, int version) { }
    record AuditRow(long auditId, long userId, String action, String challengeNo, Long operatorId,
                    String reason, Instant occurredAt) { }
    record OutboxRow(long eventId, String eventType, String businessNo, String payload, Instant occurredAt) { }
}
