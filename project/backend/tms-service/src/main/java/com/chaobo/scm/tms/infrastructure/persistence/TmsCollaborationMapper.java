package com.chaobo.scm.tms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/** TMS Dubbo 运输请求的持久化边界。 */
@Mapper
public interface TmsCollaborationMapper {

    /** 同步运输请求持久化行。 */
    record Request(String requestId, String idempotencyKey, String commandType,
                   String requestFingerprint, String businessType, long businessId,
                   String businessNo, long shipperId, long warehouseId, String carrierCode,
                   String trackingNo, String requestPayload, int status, String cancelReason,
                   int version) {
    }

    /** 取消等非创建命令的幂等收据。 */
    record Receipt(String idempotencyKey, String commandType, String requestFingerprint,
                   String referenceNo) {
    }

    @Select("select request_id requestId,idempotency_key idempotencyKey,command_type commandType,request_fingerprint requestFingerprint,business_type businessType,business_id businessId,business_no businessNo,shipper_id shipperId,warehouse_id warehouseId,carrier_code carrierCode,tracking_no trackingNo,request_payload requestPayload,request_status status,cancel_reason cancelReason,version from tms_dubbo_transport_request where idempotency_key=#{key}")
    Request findByIdempotency(String key);

    @Select("select request_id requestId,idempotency_key idempotencyKey,command_type commandType,request_fingerprint requestFingerprint,business_type businessType,business_id businessId,business_no businessNo,shipper_id shipperId,warehouse_id warehouseId,carrier_code carrierCode,tracking_no trackingNo,request_payload requestPayload,request_status status,cancel_reason cancelReason,version from tms_dubbo_transport_request where business_type=#{type} and business_id=#{id}")
    Request findByBusiness(@Param("type") String type, @Param("id") long id);

    @Insert("insert into tms_dubbo_transport_request(request_id,idempotency_key,command_type,request_fingerprint,business_type,business_id,business_no,shipper_id,warehouse_id,carrier_code,tracking_no,request_payload) values(#{requestId},#{idempotencyKey},#{commandType},#{requestFingerprint},#{businessType},#{businessId},#{businessNo},#{shipperId},#{warehouseId},#{carrierCode},#{trackingNo},#{requestPayload})")
    void insert(Request request);

    @Update("update tms_dubbo_transport_request set request_status=3,cancel_reason=#{reason},version=version+1 where request_id=#{requestId} and request_status in (1,2)")
    int cancel(@Param("requestId") String requestId, @Param("reason") String reason);

    @Select("select idempotency_key idempotencyKey,command_type commandType,request_fingerprint requestFingerprint,reference_no referenceNo from tms_dubbo_command_receipt where idempotency_key=#{key}")
    Receipt findReceipt(String key);

    @Insert("insert into tms_dubbo_command_receipt(idempotency_key,command_type,request_fingerprint,reference_no) values(#{idempotencyKey},#{commandType},#{requestFingerprint},#{referenceNo})")
    void insertReceipt(Receipt receipt);
}
