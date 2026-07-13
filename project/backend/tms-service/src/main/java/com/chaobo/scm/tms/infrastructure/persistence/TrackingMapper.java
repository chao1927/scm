package com.chaobo.scm.tms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TrackingMapper {
    @Select("select track_no trackNo,waybill_no waybillNo,node_code nodeCode,description,location,track_at trackAt,source_type sourceType,raw_event_id rawEventId,manual_reason manualReason from tms_tracking_node where waybill_no=#{waybillNo} and node_code=#{nodeCode} and track_at=#{trackAt} limit 1")
    TrackRow findTrackDuplicate(@Param("waybillNo") String waybillNo, @Param("nodeCode") String nodeCode,
                                @Param("trackAt") LocalDateTime trackAt);

    @Select("select track_no trackNo,waybill_no waybillNo,node_code nodeCode,description,location,track_at trackAt,source_type sourceType,raw_event_id rawEventId,manual_reason manualReason from tms_tracking_node where waybill_no=#{waybillNo} order by track_at asc,id asc")
    List<TrackRow> listTracks(@Param("waybillNo") String waybillNo);

    @Insert("insert into tms_tracking_node(track_no,waybill_no,node_code,description,location,track_at,source_type,raw_event_id,manual_reason,created_at) values(#{trackNo},#{waybillNo},#{nodeCode},#{description},#{location},#{trackAt},#{sourceType},#{rawEventId},#{manualReason},now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertTrack(TrackRow row);

    @Select("select receipt_no receiptNo,waybill_no waybillNo,receipt_result result,signed_by signedBy,signed_at signedAt,reject_reason rejectReason,proof_url proofUrl from tms_delivery_receipt where waybill_no=#{waybillNo} limit 1")
    ReceiptRow findReceiptByWaybill(@Param("waybillNo") String waybillNo);

    @Select("select receipt_no receiptNo,waybill_no waybillNo,receipt_result result,signed_by signedBy,signed_at signedAt,reject_reason rejectReason,proof_url proofUrl from tms_delivery_receipt where receipt_no=#{receiptNo}")
    ReceiptRow findReceipt(@Param("receiptNo") String receiptNo);

    @Insert("insert into tms_delivery_receipt(receipt_no,waybill_no,receipt_result,signed_by,signed_at,reject_reason,proof_url,created_at) values(#{receiptNo},#{waybillNo},#{result},#{signedBy},#{signedAt},#{rejectReason},#{proofUrl},now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertReceipt(ReceiptRow row);

    @Insert("insert ignore into tms_event_inbox(event_id,event_type,business_no,payload,event_status,error_message,created_at,updated_at) values(#{eventId},#{eventType},#{businessNo},#{payload},#{status},#{errorMessage},now(),now())")
    int claimEvent(EventInboxRow row);

    @Update("update tms_event_inbox set event_status=#{status},error_message=#{errorMessage},updated_at=now() where event_id=#{eventId}")
    void updateEvent(EventInboxRow row);

    @Insert("insert into tms_domain_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(TransportTaskMapper.OutboxRow row);

    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from tms_domain_event order by id desc")
    List<TransportTaskMapper.OutboxRow> listOutbox();

    @Insert("insert into tms_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(TransportTaskMapper.OperationLogRow row);

    @Select("select operation_type operationType,business_no businessNo,operator_id operatorId,idempotency_key idempotencyKey,created_at createdAt from tms_operation_log order by id desc")
    List<TransportTaskMapper.OperationLogRow> listOperationLogs();

    record TrackRow(Long id, String trackNo, String waybillNo, String nodeCode, String description, String location,
                    LocalDateTime trackAt, String sourceType, String rawEventId, String manualReason) {}

    record ReceiptRow(Long id, String receiptNo, String waybillNo, int result, String signedBy,
                      LocalDateTime signedAt, String rejectReason, String proofUrl) {}

    record EventInboxRow(String eventId, String eventType, String businessNo, String payload, int status,
                         String errorMessage) {}
}
