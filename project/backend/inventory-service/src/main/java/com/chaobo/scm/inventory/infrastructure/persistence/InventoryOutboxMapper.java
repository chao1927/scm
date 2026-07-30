package com.chaobo.scm.inventory.infrastructure.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 库存 Outbox 可靠投递 MyBatis 映射。
 *
 * @author SCM Team
 */
@Mapper
public interface InventoryOutboxMapper {

    /**
     * 查询仍在重试窗口内的事件。
     *
     * @param limit 批次数量
     * @param maxRetries 最大重试次数
     * @return 待投递数据行
     */
    @Select("""
            select event_id id,event_code,event_type,event_version,
                   aggregate_type,aggregate_id,cast(payload_json as char) payload_json,
                   status,retry_count
              from inv_outbox_event
             where status in (1,3)
               and retry_count < #{maxRetries}
               and (next_retry_at is null or next_retry_at <= now(3))
             order by created_at,event_id
             limit #{limit}
            """)
    List<OutboxRow> pending(
            @Param("limit") int limit,
            @Param("maxRetries") int maxRetries);

    /**
     * 查询指定失败事件。
     *
     * @param eventCode 事件编码
     * @return 失败事件
     */
    @Select("""
            select event_id id,event_code,event_type,event_version,
                   aggregate_type,aggregate_id,cast(payload_json as char) payload_json,
                   status,retry_count
              from inv_outbox_event
             where event_code=#{eventCode} and status=3
             limit 1
            """)
    OutboxRow findFailed(@Param("eventCode") String eventCode);

    /**
     * 标记 RocketMQ 已确认接收。
     *
     * @param eventId 事件主键
     * @return 受影响行数
     */
    @Update("""
            update inv_outbox_event
               set status=2,last_error=null,next_retry_at=null,
                   published_at=now(3),updated_at=now(3)
             where event_id=#{eventId} and status in (1,3)
            """)
    int markPublished(@Param("eventId") long eventId);

    /**
     * 标记发送失败并采用上限退避等待下次调度。
     *
     * @param eventId 事件主键
     * @param reason 失败原因
     * @return 受影响行数
     */
    @Update("""
            update inv_outbox_event
               set status=3,retry_count=retry_count+1,last_error=#{reason},
                   next_retry_at=date_add(
                       now(3),
                       interval least(300,pow(2,least(retry_count,8))) second),
                   updated_at=now(3)
             where event_id=#{eventId} and status in (1,3)
            """)
    int markFailed(
            @Param("eventId") long eventId,
            @Param("reason") String reason);

    /**
     * Outbox 数据行。
     */
    record OutboxRow(
            long id,
            String eventCode,
            String eventType,
            String eventVersion,
            String aggregateType,
            String aggregateId,
            String payloadJson,
            int status,
            int retryCount) {
    }
}
