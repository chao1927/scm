package com.chaobo.scm.oms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FulfillmentMapper {
    @Select("select fulfillment_no fulfillmentNo,sales_order_no salesOrderNo,channel_code channelCode,customer_id customerId,warehouse_id warehouseId,warehouse_code warehouseCode,logistics_product_code logisticsProductCode,line_payload linePayload,fulfillment_status status,reservation_ref_no reservationRefNo,reservation_no reservationNo,outbound_order_no outboundNo,failure_reason failureReason,split_reason splitReason,version from oms_fulfillment where fulfillment_no=#{fulfillmentNo}")
    FulfillmentRow findFulfillment(@Param("fulfillmentNo") String fulfillmentNo);

    @Select("select fulfillment_no fulfillmentNo,sales_order_no salesOrderNo,channel_code channelCode,customer_id customerId,warehouse_id warehouseId,warehouse_code warehouseCode,logistics_product_code logisticsProductCode,line_payload linePayload,fulfillment_status status,reservation_ref_no reservationRefNo,reservation_no reservationNo,outbound_order_no outboundNo,failure_reason failureReason,split_reason splitReason,version from oms_fulfillment where sales_order_no=#{salesOrderNo} order by id limit 1")
    FulfillmentRow findBySalesOrder(@Param("salesOrderNo") String salesOrderNo);

    @Select("select fulfillment_no fulfillmentNo,sales_order_no salesOrderNo,channel_code channelCode,customer_id customerId,warehouse_id warehouseId,warehouse_code warehouseCode,logistics_product_code logisticsProductCode,line_payload linePayload,fulfillment_status status,reservation_ref_no reservationRefNo,reservation_no reservationNo,outbound_order_no outboundNo,failure_reason failureReason,split_reason splitReason,version from oms_fulfillment order by id desc")
    List<FulfillmentRow> listFulfillments();

    @Insert("insert into oms_fulfillment(fulfillment_no,sales_order_no,channel_code,customer_id,warehouse_id,warehouse_code,logistics_product_code,line_payload,fulfillment_status,reservation_ref_no,reservation_no,outbound_order_no,failure_reason,split_reason,version,created_at,updated_at) values(#{fulfillmentNo},#{salesOrderNo},#{channelCode},#{customerId},#{warehouseId},#{warehouseCode},#{logisticsProductCode},#{linePayload},#{status},#{reservationRefNo},#{reservationNo},#{outboundNo},#{failureReason},#{splitReason},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertFulfillment(FulfillmentRow row);

    @Update("update oms_fulfillment set warehouse_id=#{warehouseId},warehouse_code=#{warehouseCode},line_payload=#{linePayload},fulfillment_status=#{status},reservation_ref_no=#{reservationRefNo},reservation_no=#{reservationNo},outbound_order_no=#{outboundNo},failure_reason=#{failureReason},split_reason=#{splitReason},version=#{version},updated_at=now() where fulfillment_no=#{fulfillmentNo}")
    void updateFulfillment(FulfillmentRow row);

    @Select("select reservation_ref_no reservationRefNo,fulfillment_no fulfillmentNo,reservation_no reservationNo,reserve_qty reserveQty,reserved_qty reservedQty,reservation_status status,fail_reason failReason,version from oms_stock_reservation where reservation_ref_no=#{reservationRefNo}")
    ReservationRow findReservation(@Param("reservationRefNo") String reservationRefNo);

    @Select("select reservation_ref_no reservationRefNo,fulfillment_no fulfillmentNo,reservation_no reservationNo,reserve_qty reserveQty,reserved_qty reservedQty,reservation_status status,fail_reason failReason,version from oms_stock_reservation where fulfillment_no=#{fulfillmentNo} order by id desc limit 1")
    ReservationRow findReservationByFulfillment(@Param("fulfillmentNo") String fulfillmentNo);

    @Insert("insert into oms_stock_reservation(reservation_ref_no,fulfillment_no,reservation_no,reserve_qty,reserved_qty,reservation_status,fail_reason,version,created_at,updated_at) values(#{reservationRefNo},#{fulfillmentNo},#{reservationNo},#{reserveQty},#{reservedQty},#{status},#{failReason},#{version},now(),now())")
    void insertReservation(ReservationRow row);

    @Update("update oms_stock_reservation set reservation_no=#{reservationNo},reserved_qty=#{reservedQty},reservation_status=#{status},fail_reason=#{failReason},version=#{version},updated_at=now() where reservation_ref_no=#{reservationRefNo}")
    void updateReservation(ReservationRow row);

    @Select("select outbound_order_no outboundNo,fulfillment_no fulfillmentNo,sales_order_no salesOrderNo,warehouse_id warehouseId,warehouse_code warehouseCode,wms_order_no wmsOrderNo,outbound_status status,cancel_reason cancelReason,retry_count retryCount,version from oms_outbound where outbound_order_no=#{outboundNo}")
    OutboundRow findOutbound(@Param("outboundNo") String outboundNo);

    @Select("select outbound_order_no outboundNo,fulfillment_no fulfillmentNo,sales_order_no salesOrderNo,warehouse_id warehouseId,warehouse_code warehouseCode,wms_order_no wmsOrderNo,outbound_status status,cancel_reason cancelReason,retry_count retryCount,version from oms_outbound where fulfillment_no=#{fulfillmentNo}")
    OutboundRow findOutboundByFulfillment(@Param("fulfillmentNo") String fulfillmentNo);

    @Select("select outbound_order_no outboundNo,fulfillment_no fulfillmentNo,sales_order_no salesOrderNo,warehouse_id warehouseId,warehouse_code warehouseCode,wms_order_no wmsOrderNo,outbound_status status,cancel_reason cancelReason,retry_count retryCount,version from oms_outbound order by id desc")
    List<OutboundRow> listOutbounds();

    @Insert("insert into oms_outbound(outbound_order_no,fulfillment_no,sales_order_no,warehouse_id,warehouse_code,wms_order_no,outbound_status,cancel_reason,retry_count,version,created_at,updated_at) values(#{outboundNo},#{fulfillmentNo},#{salesOrderNo},#{warehouseId},#{warehouseCode},#{wmsOrderNo},#{status},#{cancelReason},#{retryCount},#{version},now(),now())")
    void insertOutbound(OutboundRow row);

    @Update("update oms_outbound set wms_order_no=#{wmsOrderNo},outbound_status=#{status},cancel_reason=#{cancelReason},retry_count=#{retryCount},version=#{version},updated_at=now() where outbound_order_no=#{outboundNo}")
    void updateOutbound(OutboundRow row);

    @Insert("insert into oms_integration_command(command_type,target_system,business_no,idempotency_key,payload,command_status,retry_count,created_at,updated_at) values(#{commandType},#{targetSystem},#{businessNo},#{idempotencyKey},#{payload},1,0,now(),now())")
    void insertIntegrationCommand(IntegrationCommandRow row);

    @Insert("insert ignore into oms_event_inbox(event_id,event_type,business_no,payload,event_status,created_at) values(#{eventId},#{eventType},#{businessNo},#{payload},1,now())")
    int claimEvent(EventInboxRow row);

    @Update("update oms_event_inbox set event_status=#{status},fail_reason=#{failReason},processed_at=now() where event_id=#{eventId}")
    void updateEvent(EventInboxRow row);

    @Insert("insert into oms_outbox_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(OutboxRow row);

    @Insert("insert into oms_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(OperationLogRow row);

    record FulfillmentRow(String fulfillmentNo, String salesOrderNo, String channelCode, Long customerId,
                          Long warehouseId, String warehouseCode, String logisticsProductCode, String linePayload,
                          int status, String reservationRefNo, String reservationNo, String outboundNo,
                          String failureReason, String splitReason, long version) {}

    record ReservationRow(String reservationRefNo, String fulfillmentNo, String reservationNo,
                          BigDecimal reserveQty, BigDecimal reservedQty, int status, String failReason,
                          long version) {}

    record OutboundRow(String outboundNo, String fulfillmentNo, String salesOrderNo, Long warehouseId,
                       String warehouseCode, String wmsOrderNo, int status, String cancelReason,
                       int retryCount, long version) {}

    record IntegrationCommandRow(String commandType, String targetSystem, String businessNo,
                                 String idempotencyKey, String payload) {}

    record EventInboxRow(String eventId, String eventType, String businessNo, String payload,
                         int status, String failReason) {}

    record OutboxRow(String eventType, String businessNo, String payload, LocalDateTime occurredAt) {}

    record OperationLogRow(String operationType, String businessNo, Long operatorId, String idempotencyKey) {}
}
