package com.chaobo.scm.iam.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.Instant;

/** MFA challenge persistence and atomic recovery-code consumption. */
@Mapper
public interface MfaMapper {

    @Select("select * from iam_mfa_challenge where idempotency_key=#{key}")
    ChallengeRow findByIdempotencyKey(String key);

    @Select("select * from iam_mfa_challenge where challenge_no=#{challengeNo}")
    ChallengeRow findByChallengeNo(String challengeNo);

    @Insert("""
            insert into iam_mfa_challenge(challenge_id,challenge_no,user_id,app_code,factor_type,
            secret_ciphertext,challenge_status,failed_attempts,max_attempts,expires_at,idempotency_key,
            version,created_at,updated_at) values(#{id},#{challengeNo},#{userId},#{appCode},'TOTP',
            #{secretCiphertext},'PENDING',0,#{maxAttempts},#{expiresAt},#{idempotencyKey},0,now(3),now(3))
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

    record ChallengeRow(long challengeId, String challengeNo, long userId, String appCode,
                        String factorType, String secretCiphertext, String challengeStatus,
                        int failedAttempts, int maxAttempts, Instant expiresAt, Instant verifiedAt,
                        String idempotencyKey, int version) {
        public long id() { return challengeId; }
    }
}
