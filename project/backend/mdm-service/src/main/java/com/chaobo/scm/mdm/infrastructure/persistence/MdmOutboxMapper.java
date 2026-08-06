package com.chaobo.scm.mdm.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 主数据 Outbox 可靠投递映射。
 *
 * @author SCM Team
 */
@Mapper
public interface MdmOutboxMapper {

    /**
     * 查询已到重试时间且没有超过重试上限的事件。
     *
     * @param limit 批次上限
     * @param maxRetries 最大重试次数
     * @return 待投递事件
     */
    @Select("select o.id eventId,o.event_type eventType,o.business_no businessNo,o.payload,"
        + "p.event_topic destinationTopic from mdm_outbox_event o "
        + "left join mdm_publication_log p on p.publication_no=o.business_no "
        + "where o.event_status in (1,3) and o.retry_count<#{maxRetries} "
        + "and (o.next_retry_at is null or o.next_retry_at<=now()) "
        + "order by o.occurred_at,o.id limit #{limit}")
    List<OutboxEvent> pending(@Param("limit") int limit,
                              @Param("maxRetries") int maxRetries);

    /**
     * 仅在 RocketMQ 确认后标记投递成功。
     *
     * @param eventId 事件标识
     * @return 影响行数
     */
    @Update("update mdm_outbox_event set event_status=2,published_at=now(),"
        + "last_error=null,next_retry_at=null,updated_at=now() "
        + "where id=#{eventId} and event_status in (1,3)")
    int markPublished(@Param("eventId") long eventId);

    /**
     * 记录投递失败并安排有界指数退避。
     *
     * @param eventId 事件标识
     * @param reason 失败原因
     * @return 影响行数
     */
    @Update("update mdm_outbox_event set event_status=3,retry_count=retry_count+1,"
        + "last_error=#{reason},next_retry_at=date_add(now(),"
        + "interval least(300,pow(2,least(retry_count,8))) second),updated_at=now() "
        + "where id=#{eventId} and event_status in (1,3)")
    int markFailed(@Param("eventId") long eventId, @Param("reason") String reason);

    /** 主数据 Outbox 持久化快照。 */
    record OutboxEvent(long eventId, String eventType, String businessNo,
                       String payload, String destinationTopic) {
    }
}
