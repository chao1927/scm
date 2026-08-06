package com.chaobo.scm.iam.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * IAM Outbox RocketMQ 投递映射。
 *
 * @author SCM Team
 */
@Mapper
public interface IamOutboxMapper {

    /**
     * 查询到达投递时间且未超过重试上限的事件。
     *
     * @param limit 批次上限
     * @param maxRetries 最大重试次数
     * @return 待投递事件
     */
    @Select("select event_id eventId,event_type eventType,business_no businessNo,payload "
        + "from iam_outbox_event where event_status in (1,3) and retry_count<#{maxRetries} "
        + "and (next_retry_at is null or next_retry_at<=now(3)) "
        + "order by occurred_at,event_id limit #{limit}")
    List<OutboxEvent> pending(@Param("limit") int limit,
                              @Param("maxRetries") int maxRetries);

    /**
     * 标记 RocketMQ 已确认的事件。
     *
     * @param eventId 事件标识
     * @return 影响行数
     */
    @Update("update iam_outbox_event set event_status=2,published_at=now(3),"
        + "last_error=null,next_retry_at=null,updated_at=now(3) "
        + "where event_id=#{eventId} and event_status in (1,3)")
    int markPublished(@Param("eventId") long eventId);

    /**
     * 记录投递失败并安排指数退避。
     *
     * @param eventId 事件标识
     * @param reason 失败原因
     * @return 影响行数
     */
    @Update("update iam_outbox_event set event_status=3,retry_count=retry_count+1,"
        + "last_error=#{reason},next_retry_at=date_add(now(3),"
        + "interval least(300,pow(2,least(retry_count,8))) second),updated_at=now(3) "
        + "where event_id=#{eventId} and event_status in (1,3)")
    int markFailed(@Param("eventId") long eventId, @Param("reason") String reason);

    /** IAM Outbox 持久化快照。 */
    record OutboxEvent(long eventId, String eventType, String businessNo, String payload) {
    }
}
