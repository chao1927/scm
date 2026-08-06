package com.chaobo.scm.wms.infrastructure.persistence.integration;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** WMS Dubbo 协作命令的持久化收据和 ASN 预约映射。 */
@Mapper
public interface WmsCollaborationMapper {

    /** 幂等命令收据。 */
    record Receipt(String idempotencyKey, String commandType, String requestFingerprint,
                   String referenceNo) {
    }

    /** ASN 预约状态。 */
    record Appointment(long asnId, String appointmentNo, String asnNo, long supplierId,
                       long warehouseId, int status, int version) {
    }

    /** ASN 行与统一入库单的映射。 */
    record AppointmentLine(long asnId, String sourceLineNo, long inboundOrderId,
                           int inboundOrderVersion) {
    }

    @Select("select idempotency_key idempotencyKey,command_type commandType,request_fingerprint requestFingerprint,reference_no referenceNo from wms_dubbo_receipt where idempotency_key=#{key}")
    Receipt findReceipt(String key);

    @Insert("insert into wms_dubbo_receipt(idempotency_key,command_type,request_fingerprint,reference_no) values(#{idempotencyKey},#{commandType},#{requestFingerprint},#{referenceNo})")
    void insertReceipt(Receipt receipt);

    @Select("select asn_id asnId,appointment_no appointmentNo,asn_no asnNo,supplier_id supplierId,warehouse_id warehouseId,appointment_status status,version from wms_supplier_appointment where asn_id=#{asnId}")
    Appointment findAppointment(long asnId);

    @Insert("insert into wms_supplier_appointment(asn_id,appointment_no,asn_no,supplier_id,warehouse_id) values(#{asnId},#{appointmentNo},#{asnNo},#{supplierId},#{warehouseId})")
    void insertAppointment(Appointment appointment);

    @Insert("insert into wms_supplier_appointment_line(asn_id,source_line_no,inbound_order_id,inbound_order_version) values(#{asnId},#{sourceLineNo},#{inboundOrderId},#{inboundOrderVersion})")
    void insertAppointmentLine(AppointmentLine line);

    @Select("select asn_id asnId,source_line_no sourceLineNo,inbound_order_id inboundOrderId,inbound_order_version inboundOrderVersion from wms_supplier_appointment_line where asn_id=#{asnId} order by source_line_no")
    List<AppointmentLine> appointmentLines(long asnId);

    @Update("update wms_supplier_appointment set appointment_status=2,version=version+1 where asn_id=#{asnId} and appointment_status=1")
    int cancelAppointment(long asnId);
}
