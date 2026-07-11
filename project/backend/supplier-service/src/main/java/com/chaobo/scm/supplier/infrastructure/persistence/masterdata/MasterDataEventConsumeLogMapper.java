package com.chaobo.scm.supplier.infrastructure.persistence.masterdata;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MasterDataEventConsumeLogMapper {
    record ConsumeRow(int status) {}

    @Insert("""
            INSERT IGNORE INTO sup_event_consume_log(source_system,event_code,event_type,consumer_name,idempotent_key,consume_status)
            VALUES(#{sourceSystem},#{eventCode},#{eventType},#{consumerName},#{idempotentKey},1)
            """)
    int insertProcessing(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode,
                         @Param("eventType") String eventType, @Param("consumerName") String consumerName,
                         @Param("idempotentKey") String idempotentKey);

    @Select("SELECT consume_status FROM sup_event_consume_log WHERE source_system=#{sourceSystem} AND event_code=#{eventCode} AND consumer_name=#{consumerName}")
    ConsumeRow find(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode,
                    @Param("consumerName") String consumerName);

    @Update("UPDATE sup_event_consume_log SET consume_status=1,retry_count=retry_count+1,fail_reason=NULL WHERE source_system=#{sourceSystem} AND event_code=#{eventCode} AND consumer_name=#{consumerName} AND consume_status=3")
    int retryFailed(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode,
                    @Param("consumerName") String consumerName);

    @Update("UPDATE sup_event_consume_log SET consume_status=#{status},consumed_at=NOW(3),fail_reason=NULL WHERE source_system=#{sourceSystem} AND event_code=#{eventCode} AND consumer_name=#{consumerName} AND consume_status=1")
    int markSucceeded(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode,
                      @Param("consumerName") String consumerName, @Param("status") int status);

    @Insert("""
            INSERT INTO sup_event_consume_log(source_system,event_code,event_type,consumer_name,idempotent_key,consume_status,fail_reason)
            VALUES(#{sourceSystem},#{eventCode},#{eventType},#{consumerName},#{idempotentKey},3,#{reason})
            ON DUPLICATE KEY UPDATE consume_status=3,retry_count=retry_count+1,fail_reason=VALUES(fail_reason)
            """)
    void recordFailure(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode,
                       @Param("eventType") String eventType, @Param("consumerName") String consumerName,
                       @Param("idempotentKey") String idempotentKey, @Param("reason") String reason);

    @Update("UPDATE sup_event_consume_log SET payload_json=CAST(#{payload} AS JSON) WHERE source_system=#{sourceSystem} AND event_code=#{eventCode} AND consumer_name=#{consumerName} AND payload_json IS NULL")
    void savePayload(@Param("sourceSystem")String sourceSystem,@Param("eventCode")String eventCode,@Param("consumerName")String consumerName,@Param("payload")String payload);

    @Select("SELECT consume_log_id id,source_system sourceSystem,event_code eventCode,event_type eventType,consumer_name consumerName,payload_json payloadJson,consume_status status FROM sup_event_consume_log WHERE consume_log_id=#{id}")
    com.chaobo.scm.supplier.application.masterdata.MasterDataEventConsumeLogPort.ReplayEvent findForReplay(long id);

    @Update("UPDATE sup_event_consume_log SET replay_count=replay_count+1,last_replayed_by=#{operator},last_replayed_at=NOW(3),fail_reason=CONCAT('MANUAL_REPLAY:',#{reason}) WHERE consume_log_id=#{id} AND consume_status=3 AND payload_json IS NOT NULL")
    int markReplayRequested(@Param("id")long id,@Param("operator")long operator,@Param("reason")String reason);
}
