package com.chaobo.scm.mdm.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/** 供应商与 MDM 同步协作的主数据映射和幂等收据。 */
@Mapper
public interface MdmCollaborationMapper {

    record SupplierMapping(long supplierId, long admissionId, String supplierCode,
                           String recordNo, String requestFingerprint) {
    }

    record Receipt(String idempotencyKey, String commandType, String requestFingerprint,
                   String referenceNo) {
    }

    @Select("select supplier_id supplierId,admission_id admissionId,supplier_code supplierCode,record_no recordNo,request_fingerprint requestFingerprint from mdm_supplier_rpc_mapping where supplier_id=#{supplierId}")
    SupplierMapping findSupplier(long supplierId);

    @Insert("insert into mdm_supplier_rpc_mapping(supplier_id,admission_id,supplier_code,record_no,request_fingerprint) values(#{supplierId},#{admissionId},#{supplierCode},#{recordNo},#{requestFingerprint})")
    void insertSupplier(SupplierMapping mapping);

    @Select("select idempotency_key idempotencyKey,command_type commandType,request_fingerprint requestFingerprint,reference_no referenceNo from mdm_dubbo_receipt where idempotency_key=#{key}")
    Receipt findReceipt(String key);

    @Insert("insert into mdm_dubbo_receipt(idempotency_key,command_type,request_fingerprint,reference_no) values(#{idempotencyKey},#{commandType},#{requestFingerprint},#{referenceNo})")
    void insertReceipt(Receipt receipt);
}
