package com.chaobo.scm.integration.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface IntegrationMapper {
    @Select("select route_no routeNo,message_type messageType,source_system sourceSystem,target_system targetSystem,channel_type channelType,route_status status,version from int_route where route_no=#{routeNo}")
    RouteRow findRoute(@Param("routeNo") String routeNo);

    @Select("select route_no routeNo,message_type messageType,source_system sourceSystem,target_system targetSystem,channel_type channelType,route_status status,version from int_route where message_type=#{messageType} and source_system=#{sourceSystem} and route_status=1 order by id")
    List<RouteRow> findEnabledRoutes(@Param("messageType") String messageType, @Param("sourceSystem") String sourceSystem);

    @Select("select route_no routeNo,message_type messageType,source_system sourceSystem,target_system targetSystem,channel_type channelType,route_status status,version from int_route where message_type=#{messageType} and source_system=#{sourceSystem} and target_system=#{targetSystem} and route_status=1 order by id limit 1")
    RouteRow findRouteForMessage(@Param("messageType") String messageType,
                                 @Param("sourceSystem") String sourceSystem,
                                 @Param("targetSystem") String targetSystem);

    @Select("select route_no routeNo,message_type messageType,source_system sourceSystem,target_system targetSystem,channel_type channelType,route_status status,version from int_route order by id desc")
    List<RouteRow> listRoutes();

    @Select("select endpoint_no endpointNo,target_system targetSystem,channel_type channelType,endpoint_url endpointUrl,timeout_millis timeoutMillis,failure_threshold failureThreshold,consecutive_failures consecutiveFailures,endpoint_status status,version from int_endpoint where target_system=#{targetSystem} and channel_type=#{channelType} and endpoint_status=1 order by id desc limit 1")
    EndpointRow findEnabledEndpoint(@Param("targetSystem") String targetSystem,
                                    @Param("channelType") String channelType);

    @Select("select endpoint_no endpointNo,target_system targetSystem,channel_type channelType,endpoint_url endpointUrl,timeout_millis timeoutMillis,failure_threshold failureThreshold,consecutive_failures consecutiveFailures,endpoint_status status,version from int_endpoint where endpoint_no=#{endpointNo}")
    EndpointRow findEndpoint(@Param("endpointNo") String endpointNo);

    @Select("select endpoint_no endpointNo,target_system targetSystem,channel_type channelType,endpoint_url endpointUrl,timeout_millis timeoutMillis,failure_threshold failureThreshold,consecutive_failures consecutiveFailures,endpoint_status status,version from int_endpoint order by id desc")
    List<EndpointRow> listEndpoints();

    @Insert("insert into int_endpoint(endpoint_no,target_system,channel_type,endpoint_url,timeout_millis,failure_threshold,consecutive_failures,endpoint_status,version,created_at,updated_at) values(#{endpointNo},#{targetSystem},#{channelType},#{endpointUrl},#{timeoutMillis},#{failureThreshold},#{consecutiveFailures},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertEndpoint(EndpointRow row);

    @Update("update int_endpoint set endpoint_url=#{endpointUrl},timeout_millis=#{timeoutMillis},failure_threshold=#{failureThreshold},consecutive_failures=#{consecutiveFailures},endpoint_status=#{status},version=#{version},updated_at=now(3) where endpoint_no=#{endpointNo}")
    void updateEndpoint(EndpointRow row);

    @Insert("insert into int_route(route_no,message_type,source_system,target_system,channel_type,route_status,version,created_at,updated_at) values(#{routeNo},#{messageType},#{sourceSystem},#{targetSystem},#{channelType},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertRoute(RouteRow row);

    @Update("update int_route set route_status=#{status},version=#{version},updated_at=now(3) where route_no=#{routeNo}")
    void updateRoute(RouteRow row);

    @Select("select message_no messageNo,message_type messageType,source_system sourceSystem,target_system targetSystem,business_no businessNo,idempotency_key idempotencyKey,payload,message_status status,retry_count retryCount,failure_reason failureReason,version from int_message where message_no=#{messageNo}")
    MessageRow findMessage(@Param("messageNo") String messageNo);

    @Select("select message_no messageNo,message_type messageType,source_system sourceSystem,target_system targetSystem,business_no businessNo,idempotency_key idempotencyKey,payload,message_status status,retry_count retryCount,failure_reason failureReason,version from int_message where source_system=#{sourceSystem} and idempotency_key=#{idempotencyKey} order by id limit 1")
    MessageRow findMessageByIdempotency(@Param("sourceSystem") String sourceSystem,
                                         @Param("idempotencyKey") String idempotencyKey);

    @Select("select message_no messageNo,message_type messageType,source_system sourceSystem,target_system targetSystem,business_no businessNo,idempotency_key idempotencyKey,payload,message_status status,retry_count retryCount,failure_reason failureReason,version from int_message where (#{status} is null or message_status=#{status}) order by id desc")
    List<MessageRow> listMessages(@Param("status") Integer status);

    @Select("select message_no messageNo,message_type messageType,source_system sourceSystem,target_system targetSystem,business_no businessNo,idempotency_key idempotencyKey,payload,message_status status,retry_count retryCount,failure_reason failureReason,version from int_message where message_status in (1,3) order by id limit #{limit}")
    List<MessageRow> listDispatchableMessages(@Param("limit") int limit);

    @Insert("insert into int_message(message_no,message_type,source_system,target_system,business_no,idempotency_key,payload,message_status,retry_count,failure_reason,version,created_at,updated_at) values(#{messageNo},#{messageType},#{sourceSystem},#{targetSystem},#{businessNo},#{idempotencyKey},#{payload},#{status},#{retryCount},#{failureReason},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertMessage(MessageRow row);

    @Update("update int_message set message_status=#{status},retry_count=#{retryCount},failure_reason=#{failureReason},version=#{version},updated_at=now(3) where message_no=#{messageNo}")
    void updateMessage(MessageRow row);

    @Insert("insert into int_dead_letter(dead_letter_no,message_no,message_type,source_system,target_system,business_no,payload,failure_reason,replayed,created_at,updated_at) values(#{deadLetterNo},#{messageNo},#{messageType},#{sourceSystem},#{targetSystem},#{businessNo},#{payload},#{failureReason},#{replayed},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertDeadLetter(DeadLetterRow row);

    @Select("select dead_letter_no deadLetterNo,message_no messageNo,message_type messageType,source_system sourceSystem,target_system targetSystem,business_no businessNo,payload,failure_reason failureReason,replayed from int_dead_letter where dead_letter_no=#{deadLetterNo}")
    DeadLetterRow findDeadLetter(@Param("deadLetterNo") String deadLetterNo);

    @Select("select dead_letter_no deadLetterNo,message_no messageNo,message_type messageType,source_system sourceSystem,target_system targetSystem,business_no businessNo,payload,failure_reason failureReason,replayed from int_dead_letter order by id desc")
    List<DeadLetterRow> listDeadLetters();

    @Update("update int_dead_letter set replayed=#{replayed},updated_at=now(3) where dead_letter_no=#{deadLetterNo}")
    void updateDeadLetter(DeadLetterRow row);

    @Insert("insert into int_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now(3))")
    void insertOperationLog(OperationLogRow row);

    @Select("select operation_type operationType,business_no businessNo,operator_id operatorId,idempotency_key idempotencyKey,created_at createdAt from int_operation_log order by id desc")
    List<OperationLogRow> listOperationLogs();

    @Insert("insert into int_delivery_attempt(attempt_no,message_no,message_type,source_system,target_system,channel_type,success,failure_reason,duration_millis,created_at) values(#{attemptNo},#{messageNo},#{messageType},#{sourceSystem},#{targetSystem},#{channelType},#{success},#{failureReason},#{durationMillis},#{createdAt})")
    void insertDeliveryAttempt(DeliveryAttemptRow row);

    @Select("select attempt_no attemptNo,message_no messageNo,message_type messageType,source_system sourceSystem,target_system targetSystem,channel_type channelType,success,failure_reason failureReason,duration_millis durationMillis,created_at createdAt from int_delivery_attempt where (#{messageNo} is null or message_no=#{messageNo}) order by id desc")
    List<DeliveryAttemptRow> listDeliveryAttempts(@Param("messageNo") String messageNo);

    @Select("select coalesce(sum(case when message_status=1 then 1 else 0 end),0) pendingCount,coalesce(sum(case when message_status=2 then 1 else 0 end),0) dispatchedCount,coalesce(sum(case when message_status=3 then 1 else 0 end),0) failedCount,coalesce(sum(case when message_status=4 then 1 else 0 end),0) deadLetterCount,coalesce(sum(case when message_status=5 then 1 else 0 end),0) replayedCount from int_message")
    DispatchSummaryRow dispatchSummary();

    record RouteRow(Long id, String routeNo, String messageType, String sourceSystem, String targetSystem,
                    String channelType, int status, long version) {}

    record EndpointRow(Long id, String endpointNo, String targetSystem, String channelType, String endpointUrl,
                       int timeoutMillis, int failureThreshold, int consecutiveFailures, int status, long version) {}

    record MessageRow(Long id, String messageNo, String messageType, String sourceSystem, String targetSystem,
                      String businessNo, String idempotencyKey, String payload, int status, int retryCount,
                      String failureReason, long version) {}

    record DeadLetterRow(Long id, String deadLetterNo, String messageNo, String messageType, String sourceSystem,
                         String targetSystem, String businessNo, String payload, String failureReason,
                         boolean replayed) {}

    record OperationLogRow(String operationType, String businessNo, Long operatorId, String idempotencyKey,
                           LocalDateTime createdAt) {}

    record DeliveryAttemptRow(String attemptNo, String messageNo, String messageType, String sourceSystem,
                              String targetSystem, String channelType, boolean success, String failureReason,
                              long durationMillis, LocalDateTime createdAt) {}

    record DispatchSummaryRow(long pendingCount, long dispatchedCount, long failedCount, long deadLetterCount,
                              long replayedCount) {}
}
