package com.chaobo.scm.tms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TrackingMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface TrackingMapper {

    /**
     * 查询并返回 {@code findTrackDuplicate}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param nodeCode 可追踪业务编码，类型为 {@code String}
     * @param trackAt 业务时间，类型为 {@code LocalDateTime}
     * @return 查询并返回的结果，类型为 {@code TrackRow}
     */
    @Select("select track_no trackNo,waybill_no waybillNo,node_code nodeCode,description,location,track_at trackAt,source_type sourceType,raw_event_id rawEventId,manual_reason manualReason from tms_tracking_node where waybill_no=#{waybillNo} and node_code=#{nodeCode} and track_at=#{trackAt} limit 1")
    TrackRow findTrackDuplicate(@Param("waybillNo") String waybillNo, @Param("nodeCode") String nodeCode, @Param("trackAt") LocalDateTime trackAt);

    /**
     * 查询并返回 {@code listTracks}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code List<TrackRow>}
     */
    @Select("select track_no trackNo,waybill_no waybillNo,node_code nodeCode,description,location,track_at trackAt,source_type sourceType,raw_event_id rawEventId,manual_reason manualReason from tms_tracking_node where waybill_no=#{waybillNo} order by track_at asc,id asc")
    List<TrackRow> listTracks(@Param("waybillNo") String waybillNo);

    /**
     * 处理当前类型职责中的操作 {@code insertTrack}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code TrackRow}
     */
    @Insert("insert into tms_tracking_node(track_no,waybill_no,node_code,description,location,track_at,source_type,raw_event_id,manual_reason,created_at) values(#{trackNo},#{waybillNo},#{nodeCode},#{description},#{location},#{trackAt},#{sourceType},#{rawEventId},#{manualReason},now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertTrack(TrackRow row);

    /**
     * 查询并返回 {@code findReceiptByWaybill}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ReceiptRow}
     */
    @Select("select receipt_no receiptNo,waybill_no waybillNo,receipt_result result,signed_by signedBy,signed_at signedAt,reject_reason rejectReason,proof_url proofUrl from tms_delivery_receipt where waybill_no=#{waybillNo} limit 1")
    ReceiptRow findReceiptByWaybill(@Param("waybillNo") String waybillNo);

    /**
     * 查询并返回 {@code findReceipt}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param receiptNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ReceiptRow}
     */
    @Select("select receipt_no receiptNo,waybill_no waybillNo,receipt_result result,signed_by signedBy,signed_at signedAt,reject_reason rejectReason,proof_url proofUrl from tms_delivery_receipt where receipt_no=#{receiptNo}")
    ReceiptRow findReceipt(@Param("receiptNo") String receiptNo);

    /**
     * 处理当前类型职责中的操作 {@code insertReceipt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ReceiptRow}
     */
    @Insert("insert into tms_delivery_receipt(receipt_no,waybill_no,receipt_result,signed_by,signed_at,reject_reason,proof_url,created_at) values(#{receiptNo},#{waybillNo},#{result},#{signedBy},#{signedAt},#{rejectReason},#{proofUrl},now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertReceipt(ReceiptRow row);

    /**
     * 处理当前类型职责中的操作 {@code claimEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code EventInboxRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Insert("insert ignore into tms_event_inbox(event_id,event_type,business_no,payload,event_status,error_message,created_at,updated_at) values(#{eventId},#{eventType},#{businessNo},#{payload},#{status},#{errorMessage},now(),now())")
    int claimEvent(EventInboxRow row);

    /**
     * 执行命令 {@code updateEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code EventInboxRow}
     */
    @Update("update tms_event_inbox set event_status=#{status},error_message=#{errorMessage},updated_at=now() where event_id=#{eventId}")
    void updateEvent(EventInboxRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertOutbox}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code TransportTaskMapper.OutboxRow}
     */
    @Insert("insert into tms_domain_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(TransportTaskMapper.OutboxRow row);

    /**
     * 查询并返回 {@code listOutbox}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<TransportTaskMapper.OutboxRow>}
     */
    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from tms_domain_event order by id desc")
    List<TransportTaskMapper.OutboxRow> listOutbox();

    /**
     * 处理当前类型职责中的操作 {@code insertOperationLog}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code TransportTaskMapper.OperationLogRow}
     */
    @Insert("insert into tms_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(TransportTaskMapper.OperationLogRow row);

    /**
     * 查询并返回 {@code listOperationLogs}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<TransportTaskMapper.OperationLogRow>}
     */
    @Select("select operation_type operationType,business_no businessNo,operator_id operatorId,idempotency_key idempotencyKey,created_at createdAt from tms_operation_log order by id desc")
    List<TransportTaskMapper.OperationLogRow> listOperationLogs();

    /**
     * TrackRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record TrackRow(Long id, String trackNo, String waybillNo, String nodeCode, String description, String location, LocalDateTime trackAt, String sourceType, String rawEventId, String manualReason) {
    }

    /**
     * ReceiptRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ReceiptRow(Long id, String receiptNo, String waybillNo, int result, String signedBy, LocalDateTime signedAt, String rejectReason, String proofUrl) {
    }

    /**
     * EventInboxRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record EventInboxRow(String eventId, String eventType, String businessNo, String payload, int status, String errorMessage) {
    }
}
