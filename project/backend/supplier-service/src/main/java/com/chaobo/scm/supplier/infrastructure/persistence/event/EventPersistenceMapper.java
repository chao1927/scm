package com.chaobo.scm.supplier.infrastructure.persistence.event;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EventPersistenceMapper {
    record OutboxRow(long eventId, String eventCode, String eventType, String aggregateType,
                     long aggregateId, String payloadJson, int retryCount) {}
    @Insert("""
            INSERT INTO sup_domain_event(event_id, event_code, event_name, event_type, aggregate_type,
                aggregate_id, aggregate_no, source_system, payload_json, event_status, retry_count,
                occurred_at)
            VALUES(#{eventId}, #{eventCode}, #{eventName}, #{eventType}, #{aggregateType},
                #{aggregateId}, #{aggregateNo}, 'SUPPLIER', CAST(#{payloadJson} AS JSON), 1, 0,
                #{occurredAt})
            """)
    void insertEvent(@Param("eventId") long eventId, @Param("eventCode") String eventCode,
                     @Param("eventName") String eventName, @Param("eventType") String eventType,
                     @Param("aggregateType") String aggregateType, @Param("aggregateId") long aggregateId,
                     @Param("aggregateNo") String aggregateNo, @Param("payloadJson") String payloadJson,
                     @Param("occurredAt") java.time.OffsetDateTime occurredAt);

    @Insert("""
            INSERT INTO sup_operation_audit_log(operation_log_id, operator_id, operator_name,
                operation_type, target_type, target_id, target_no, before_snapshot, after_snapshot,
                result, request_id, operation_at)
            VALUES(#{id}, #{operatorId}, #{operatorName}, #{operationType}, #{targetType}, #{targetId},
                #{targetNo}, #{beforeSnapshot}, #{afterSnapshot}, 1, #{requestId}, NOW())
            """)
    void insertAudit(@Param("id") long id, @Param("operatorId") long operatorId,
                     @Param("operatorName") String operatorName, @Param("operationType") String operationType,
                     @Param("targetType") String targetType,
                     @Param("targetId") long targetId, @Param("targetNo") String targetNo,
                     @Param("beforeSnapshot") String beforeSnapshot,
                     @Param("afterSnapshot") String afterSnapshot, @Param("requestId") String requestId);

    @org.apache.ibatis.annotations.Select("SELECT event_id,event_code,event_type,aggregate_type,aggregate_id,payload_json,retry_count FROM sup_domain_event WHERE ((event_status IN (1,4) AND (event_status=1 OR updated_at &lt;= DATE_SUB(NOW(),INTERVAL 30 SECOND))) OR (event_status=2 AND updated_at &lt;= DATE_SUB(NOW(),INTERVAL 5 MINUTE))) AND retry_count &lt; #{maxRetries} ORDER BY created_at LIMIT #{batchSize} FOR UPDATE SKIP LOCKED")
    java.util.List<OutboxRow> findDispatchable(@Param("batchSize") int batchSize, @Param("maxRetries") int maxRetries);

    @org.apache.ibatis.annotations.Update("UPDATE sup_domain_event SET event_status=2,updated_at=NOW() WHERE event_id=#{id} AND event_status IN (1,2,4)")
    int markPublishing(long id);

    @org.apache.ibatis.annotations.Update("UPDATE sup_domain_event SET event_status=3,published_at=NOW(),fail_reason=NULL,updated_at=NOW() WHERE event_id=#{id} AND event_status=2")
    int markPublished(long id);

    @org.apache.ibatis.annotations.Update("UPDATE sup_domain_event SET event_status=4,retry_count=retry_count+1,fail_reason=#{reason},updated_at=NOW() WHERE event_id=#{id} AND event_status=2")
    int markFailed(@Param("id") long id,@Param("reason") String reason);
}
