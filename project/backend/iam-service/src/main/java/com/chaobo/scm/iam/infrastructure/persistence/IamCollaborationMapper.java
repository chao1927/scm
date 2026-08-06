package com.chaobo.scm.iam.infrastructure.persistence;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** IAM 用户级供应商数据范围与 Dubbo 幂等收据。 */
@Mapper
public interface IamCollaborationMapper {

    record Receipt(String idempotencyKey, String commandType, String requestFingerprint,
                   String referenceNo) {
    }

    @Select("select idempotency_key idempotencyKey,command_type commandType,request_fingerprint requestFingerprint,reference_no referenceNo from iam_dubbo_receipt where idempotency_key=#{key}")
    Receipt findReceipt(String key);

    @Insert("insert into iam_dubbo_receipt(idempotency_key,command_type,request_fingerprint,reference_no) values(#{idempotencyKey},#{commandType},#{requestFingerprint},#{referenceNo})")
    void insertReceipt(Receipt receipt);

    @Delete("delete from iam_user_supplier_scope where user_id=#{userId}")
    void deleteSupplierScopes(long userId);

    @Insert("insert into iam_user_supplier_scope(user_id,supplier_id) values(#{userId},#{supplierId})")
    void insertSupplierScope(long userId, long supplierId);

    @Select("select supplier_id from iam_user_supplier_scope where user_id=#{userId} order by supplier_id")
    List<Long> supplierScopes(long userId);
}
