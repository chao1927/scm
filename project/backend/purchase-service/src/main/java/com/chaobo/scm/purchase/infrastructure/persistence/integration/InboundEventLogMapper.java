package com.chaobo.scm.purchase.infrastructure.persistence.integration;

import com.chaobo.scm.purchase.application.integration.InboundEventLogPort;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InboundEventLogMapper {
    @Insert("""
            insert ignore into purchase_inbox_event(
              source_system, event_code, event_type, consumer_name, idempotent_key,
              payload_json, status, retry_count, created_at, updated_at
            ) values (
              #{sourceSystem}, #{eventCode}, #{eventType}, #{consumerName}, #{idempotentKey},
              '{}', 1, 0, now(3), now(3)
            )
            """)
    int insertProcessing(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode,
                         @Param("eventType") String eventType, @Param("consumerName") String consumerName,
                         @Param("idempotentKey") String idempotentKey);

    @Select("""
            select inbox_id id, source_system sourceSystem, event_code eventCode, event_type eventType,
                   consumer_name consumerName, payload_json payloadJson, status
            from purchase_inbox_event
            where source_system = #{sourceSystem} and event_code = #{eventCode}
              and consumer_name = #{consumerName}
            """)
    InboundEventLogPort.ReplayEvent find(@Param("sourceSystem") String sourceSystem,
                                         @Param("eventCode") String eventCode,
                                         @Param("consumerName") String consumerName);

    @Select("""
            select inbox_id id, source_system sourceSystem, event_code eventCode, event_type eventType,
                   consumer_name consumerName, payload_json payloadJson, status
            from purchase_inbox_event where inbox_id = #{id}
            """)
    InboundEventLogPort.ReplayEvent findById(long id);

    @Update("""
            update purchase_inbox_event
            set status = 1, retry_count = retry_count + 1, last_error = null, updated_at = now(3)
            where source_system = #{sourceSystem} and event_code = #{eventCode}
              and consumer_name = #{consumerName} and status = 3
            """)
    int retryFailed(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode,
                    @Param("consumerName") String consumerName);

    @Update("""
            update purchase_inbox_event
            set payload_json = #{payloadJson}, updated_at = now(3)
            where source_system = #{sourceSystem} and event_code = #{eventCode}
              and consumer_name = #{consumerName}
            """)
    int savePayload(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode,
                    @Param("consumerName") String consumerName, @Param("payloadJson") String payloadJson);

    @Update("""
            update purchase_inbox_event
            set status = #{status}, updated_at = now(3), last_error = null
            where source_system = #{sourceSystem} and event_code = #{eventCode}
              and consumer_name = #{consumerName}
            """)
    int markSucceeded(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode,
                      @Param("consumerName") String consumerName, @Param("status") int status);

    @Update("""
            update purchase_inbox_event
            set status = 3, retry_count = retry_count + 1, event_type = #{eventType},
                idempotent_key = #{idempotentKey}, last_error = #{reason}, updated_at = now(3)
            where source_system = #{sourceSystem} and event_code = #{eventCode}
              and consumer_name = #{consumerName}
            """)
    int recordFailure(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode,
                      @Param("eventType") String eventType, @Param("consumerName") String consumerName,
                      @Param("idempotentKey") String idempotentKey, @Param("reason") String reason);

    @Update("""
            update purchase_inbox_event
            set status = 3, last_error = concat('MANUAL_REPLAY:', #{operatorId}, ':', #{reason}),
                updated_at = now(3)
            where inbox_id = #{id} and status = 3
            """)
    int markReplayRequested(@Param("id") long id, @Param("operatorId") long operatorId,
                            @Param("reason") String reason);
}
