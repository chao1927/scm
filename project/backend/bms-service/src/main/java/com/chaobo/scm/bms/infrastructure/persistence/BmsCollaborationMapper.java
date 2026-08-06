package com.chaobo.scm.bms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/** 供应商退供结算同步命令的持久化映射。 */
@Mapper
public interface BmsCollaborationMapper {

    record Settlement(String settlementRef, String idempotencyKey, String requestFingerprint,
                      long returnId, String returnNo, long supplierId, BigDecimal offsetAmount,
                      BigDecimal claimAmount, String reason, int status, int version) {
    }

    @Select("select settlement_ref settlementRef,idempotency_key idempotencyKey,request_fingerprint requestFingerprint,return_id returnId,return_no returnNo,supplier_id supplierId,offset_amount offsetAmount,claim_amount claimAmount,settlement_reason reason,settlement_status status,version from bms_supplier_return_settlement where idempotency_key=#{key}")
    Settlement findByIdempotency(String key);

    @Select("select settlement_ref settlementRef,idempotency_key idempotencyKey,request_fingerprint requestFingerprint,return_id returnId,return_no returnNo,supplier_id supplierId,offset_amount offsetAmount,claim_amount claimAmount,settlement_reason reason,settlement_status status,version from bms_supplier_return_settlement where return_id=#{returnId}")
    Settlement findByReturnId(long returnId);

    @Insert("insert into bms_supplier_return_settlement(settlement_ref,idempotency_key,request_fingerprint,return_id,return_no,supplier_id,offset_amount,claim_amount,settlement_reason) values(#{settlementRef},#{idempotencyKey},#{requestFingerprint},#{returnId},#{returnNo},#{supplierId},#{offsetAmount},#{claimAmount},#{reason})")
    void insert(Settlement settlement);
}
