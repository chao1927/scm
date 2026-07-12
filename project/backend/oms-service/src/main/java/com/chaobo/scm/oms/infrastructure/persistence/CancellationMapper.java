package com.chaobo.scm.oms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper
public interface CancellationMapper {
    @Select("select cancellation_no cancellationNo,sales_order_no salesOrderNo,fulfillment_no fulfillmentNo,outbound_no outboundNo,reservation_ref_no reservationRefNo,reason,cancel_status status,wms_cancelled wmsCancelled,stock_released stockReleased,version from oms_cancel_request where cancellation_no=#{cancellationNo}")
    CancelRow findCancel(@Param("cancellationNo") String cancellationNo);

    @Select("select cancellation_no cancellationNo,sales_order_no salesOrderNo,fulfillment_no fulfillmentNo,outbound_no outboundNo,reservation_ref_no reservationRefNo,reason,cancel_status status,wms_cancelled wmsCancelled,stock_released stockReleased,version from oms_cancel_request where fulfillment_no=#{fulfillmentNo} order by id desc limit 1")
    CancelRow findCancelByFulfillment(@Param("fulfillmentNo") String fulfillmentNo);

    @Select("select cancellation_no cancellationNo,sales_order_no salesOrderNo,fulfillment_no fulfillmentNo,outbound_no outboundNo,reservation_ref_no reservationRefNo,reason,cancel_status status,wms_cancelled wmsCancelled,stock_released stockReleased,version from oms_cancel_request where outbound_no=#{outboundNo} order by id desc limit 1")
    CancelRow findCancelByOutbound(@Param("outboundNo") String outboundNo);

    @Select("select cancellation_no cancellationNo,sales_order_no salesOrderNo,fulfillment_no fulfillmentNo,outbound_no outboundNo,reservation_ref_no reservationRefNo,reason,cancel_status status,wms_cancelled wmsCancelled,stock_released stockReleased,version from oms_cancel_request where reservation_ref_no=#{reservationRefNo} order by id desc limit 1")
    CancelRow findCancelByReservation(@Param("reservationRefNo") String reservationRefNo);

    @Insert("insert into oms_cancel_request(cancellation_no,sales_order_no,fulfillment_no,outbound_no,reservation_ref_no,reason,cancel_status,wms_cancelled,stock_released,version,created_at,updated_at) values(#{cancellationNo},#{salesOrderNo},#{fulfillmentNo},#{outboundNo},#{reservationRefNo},#{reason},#{status},#{wmsCancelled},#{stockReleased},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertCancel(CancelRow row);

    @Update("update oms_cancel_request set cancel_status=#{status},wms_cancelled=#{wmsCancelled},stock_released=#{stockReleased},version=#{version},updated_at=now() where cancellation_no=#{cancellationNo}")
    void updateCancel(CancelRow row);

    @Select("select after_sale_no afterSaleNo,sales_order_no salesOrderNo,fulfillment_no fulfillmentNo,refund_amount refundAmount,refunded_amount refundedAmount,reason,after_sale_status status,version from oms_after_sale where after_sale_no=#{afterSaleNo}")
    AfterSaleRow findAfterSale(@Param("afterSaleNo") String afterSaleNo);

    @Select("select after_sale_no afterSaleNo,sales_order_no salesOrderNo,fulfillment_no fulfillmentNo,refund_amount refundAmount,refunded_amount refundedAmount,reason,after_sale_status status,version from oms_after_sale where sales_order_no=#{salesOrderNo} order by id desc limit 1")
    AfterSaleRow findAfterSaleByOrder(@Param("salesOrderNo") String salesOrderNo);

    @Insert("insert into oms_after_sale(after_sale_no,sales_order_no,fulfillment_no,refund_amount,refunded_amount,reason,after_sale_status,version,created_at,updated_at) values(#{afterSaleNo},#{salesOrderNo},#{fulfillmentNo},#{refundAmount},#{refundedAmount},#{reason},#{status},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertAfterSale(AfterSaleRow row);

    @Update("update oms_after_sale set refunded_amount=#{refundedAmount},after_sale_status=#{status},version=#{version},updated_at=now() where after_sale_no=#{afterSaleNo}")
    void updateAfterSale(AfterSaleRow row);

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

    record CancelRow(String cancellationNo, String salesOrderNo, String fulfillmentNo, String outboundNo,
                     String reservationRefNo, String reason, int status, boolean wmsCancelled,
                     boolean stockReleased, long version) {}

    record AfterSaleRow(String afterSaleNo, String salesOrderNo, String fulfillmentNo, BigDecimal refundAmount,
                        BigDecimal refundedAmount, String reason, int status, long version) {}

    record IntegrationCommandRow(String commandType, String targetSystem, String businessNo,
                                 String idempotencyKey, String payload) {}

    record EventInboxRow(String eventId, String eventType, String businessNo, String payload,
                         int status, String failReason) {}

    record OutboxRow(String eventType, String businessNo, String payload, LocalDateTime occurredAt) {}

    record OperationLogRow(String operationType, String businessNo, Long operatorId, String idempotencyKey) {}
}
