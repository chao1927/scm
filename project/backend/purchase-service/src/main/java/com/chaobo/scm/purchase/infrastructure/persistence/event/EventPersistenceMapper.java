package com.chaobo.scm.purchase.infrastructure.persistence.event;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.chaobo.scm.purchase.application.outbox.OutboxMessage;

import java.util.List;

@Mapper
public interface EventPersistenceMapper {

    @Insert("""
            insert into purchase_outbox_event(
              event_code, event_type, aggregate_type, aggregate_id, aggregate_version,
              payload_json, status, retry_count, occurred_at, created_at, updated_at
            ) values (
              #{eventCode}, #{eventType}, #{aggregateType}, #{aggregateId}, #{aggregateVersion},
              #{payloadJson}, 1, 0, #{occurredAt}, now(3), now(3)
            )
            """)
    void insertOutbox(
            @Param("eventCode") String eventCode,
            @Param("eventType") String eventType,
            @Param("aggregateType") String aggregateType,
            @Param("aggregateId") String aggregateId,
            @Param("aggregateVersion") int aggregateVersion,
            @Param("payloadJson") String payloadJson,
            @Param("occurredAt") java.time.OffsetDateTime occurredAt);

    @Insert("""
            insert into purchase_operation_log(
              request_id, trace_id, operator_id, operator_name, operation,
              target_type, target_id, target_no, before_snapshot, after_snapshot, created_at
            ) values (
              #{requestId}, #{traceId}, #{operatorId}, #{operatorName}, #{operation},
              #{targetType}, #{targetId}, #{targetNo}, #{beforeSnapshot}, #{afterSnapshot}, now(3)
            )
            """)
    void insertAuditLog(
            @Param("requestId") String requestId,
            @Param("traceId") String traceId,
            @Param("operatorId") long operatorId,
            @Param("operatorName") String operatorName,
            @Param("operation") String operation,
            @Param("targetType") String targetType,
            @Param("targetId") long targetId,
            @Param("targetNo") String targetNo,
            @Param("beforeSnapshot") String beforeSnapshot,
            @Param("afterSnapshot") String afterSnapshot);

    @Select("""
            select event_id eventId, event_code eventCode, event_type eventType, aggregate_type aggregateType,
                   aggregate_id aggregateId, payload_json payloadJson, retry_count retryCount
            from purchase_outbox_event
            where status in (1, 4) and retry_count < #{maxRetries}
            order by created_at
            limit #{batchSize}
            """)
    List<OutboxMessage> claimOutbox(@Param("batchSize") int batchSize, @Param("maxRetries") int maxRetries);

    @Update("""
            <script>
            update purchase_outbox_event set status = 2, updated_at = now(3)
            where event_id in
            <foreach collection="eventIds" item="id" open="(" separator="," close=")">#{id}</foreach>
            and status in (1, 4)
            </script>
            """)
    int markOutboxPublishing(@Param("eventIds") List<Long> eventIds);

    @Update("""
            update purchase_outbox_event
            set status = 3, published_at = now(3), updated_at = now(3), last_error = null
            where event_id = #{eventId}
            """)
    int markOutboxPublished(long eventId);

    @Update("""
            update purchase_outbox_event
            set status = 4, retry_count = retry_count + 1, last_error = #{reason}, updated_at = now(3)
            where event_id = #{eventId}
            """)
    int markOutboxFailed(@Param("eventId") long eventId, @Param("reason") String reason);
}
