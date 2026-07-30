package com.chaobo.scm.inventory.infrastructure.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 库存事件失败查询与重放审计 MyBatis 映射。
 *
 * @author SCM Team
 */
@Mapper
public interface InventoryEventFailureMapper {

    /**
     * 统计入站失败事件。
     *
     * @return 失败数量
     */
    @Select("select count(*) from inv_inbox_event where status=3")
    long countInboundFailures();

    /**
     * 分页查询入站失败事件。
     *
     * @param offset 偏移量
     * @param limit 返回数量
     * @return 失败事件
     */
    @Select("""
            select event_code,event_type,event_version,aggregate_type,aggregate_id,
                   status,retry_count,last_error,cast(envelope_json as char) raw_json
              from inv_inbox_event
             where status=3
             order by updated_at desc,inbox_id desc
             limit #{limit} offset #{offset}
            """)
    List<FailureRow> inboundFailures(
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 查找入站失败事件。
     *
     * @param eventCode 事件编码
     * @return 失败事件
     */
    @Select("""
            select event_code,event_type,event_version,aggregate_type,aggregate_id,
                   status,retry_count,last_error,cast(envelope_json as char) raw_json
              from inv_inbox_event
             where event_code=#{eventCode} and status=3
             order by updated_at desc,inbox_id desc
             limit 1
            """)
    FailureRow findInboundFailure(@Param("eventCode") String eventCode);

    /**
     * 统计出站失败事件。
     *
     * @return 失败数量
     */
    @Select("select count(*) from inv_outbox_event where status=3")
    long countOutboundFailures();

    /**
     * 分页查询出站失败事件。
     *
     * @param offset 偏移量
     * @param limit 返回数量
     * @return 失败事件
     */
    @Select("""
            select event_code,event_type,event_version,aggregate_type,aggregate_id,
                   status,retry_count,last_error,cast(payload_json as char) raw_json
              from inv_outbox_event
             where status=3
             order by updated_at desc,event_id desc
             limit #{limit} offset #{offset}
            """)
    List<FailureRow> outboundFailures(
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 查找出站失败事件。
     *
     * @param eventCode 事件编码
     * @return 失败事件
     */
    @Select("""
            select event_code,event_type,event_version,aggregate_type,aggregate_id,
                   status,retry_count,last_error,cast(payload_json as char) raw_json
              from inv_outbox_event
             where event_code=#{eventCode} and status=3
             limit 1
            """)
    FailureRow findOutboundFailure(@Param("eventCode") String eventCode);

    /**
     * 插入重放审计。
     *
     * @param row 重放审计新增行
     */
    @Insert("""
            insert into inv_event_replay_log(
                idempotency_key,direction,event_code,replay_reason,operator_id,
                replay_status,created_at,updated_at
            ) values(
                #{idempotencyKey},#{direction},#{eventCode},#{reason},#{operatorId},
                1,now(3),now(3)
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertReplay(ReplayInsert row);

    /**
     * 按幂等键查询重放审计。
     *
     * @param idempotencyKey 幂等键
     * @return 重放审计
     */
    @Select("""
            select replay_id,replay_status
              from inv_event_replay_log
             where idempotency_key=#{idempotencyKey}
            """)
    ReplayRow findReplay(@Param("idempotencyKey") String idempotencyKey);

    /**
     * 标记重放成功。
     *
     * @param replayId 重放 ID
     * @return 受影响行数
     */
    @Update("""
            update inv_event_replay_log
               set replay_status=2,replay_error=null,updated_at=now(3)
             where replay_id=#{replayId} and replay_status=1
            """)
    int markReplaySucceeded(@Param("replayId") long replayId);

    /**
     * 标记重放失败。
     *
     * @param replayId 重放 ID
     * @param reason 失败原因
     * @return 受影响行数
     */
    @Update("""
            update inv_event_replay_log
               set replay_status=3,replay_error=#{reason},updated_at=now(3)
             where replay_id=#{replayId} and replay_status=1
            """)
    int markReplayFailed(
            @Param("replayId") long replayId,
            @Param("reason") String reason);

    /**
     * 失败事件查询行。
     */
    record FailureRow(
            String eventCode,
            String eventType,
            String eventVersion,
            String aggregateType,
            String aggregateId,
            int status,
            int retryCount,
            String lastError,
            String rawJson) {
    }

    /**
     * 重放审计查询行。
     */
    record ReplayRow(long replayId, int replayStatus) {
    }

    /**
     * 重放审计新增行。
     */
    final class ReplayInsert {

        private Long id;
        private final String idempotencyKey;
        private final String direction;
        private final String eventCode;
        private final String reason;
        private final long operatorId;

        public ReplayInsert(
                String idempotencyKey,
                String direction,
                String eventCode,
                String reason,
                long operatorId) {
            this.idempotencyKey = idempotencyKey;
            this.direction = direction;
            this.eventCode = eventCode;
            this.reason = reason;
            this.operatorId = operatorId;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getIdempotencyKey() {
            return idempotencyKey;
        }

        public String getDirection() {
            return direction;
        }

        public String getEventCode() {
            return eventCode;
        }

        public String getReason() {
            return reason;
        }

        public long getOperatorId() {
            return operatorId;
        }
    }
}
