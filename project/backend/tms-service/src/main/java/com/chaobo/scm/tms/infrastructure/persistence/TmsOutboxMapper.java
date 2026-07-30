package com.chaobo.scm.tms.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * TMS Outbox 可靠投递映射。
 *
 * @author SCM Team
 */
@Mapper
public interface TmsOutboxMapper {

    /**
     * 查询重试窗口内的待投递事件。
     *
     * @param limit 批次数量
     * @param maxRetries 最大重试次数
     * @return 待投递事件
     */
    @Select("""
        select id,coalesce(event_code,concat('TMS-',id)) eventCode,
               event_type eventType,business_no businessNo,payload
          from tms_domain_event
         where event_status in (1,3)
           and retry_count < #{maxRetries}
           and (next_retry_at is null or next_retry_at <= now(3))
         order by occurred_at,id
         limit #{limit}
        """)
    List<OutboxEvent> pending(@Param("limit") int limit,
                              @Param("maxRetries") int maxRetries);

    /**
     * 标记消息代理已经确认接收。
     *
     * @param id Outbox 主键
     * @return 更新行数
     */
    @Update("""
        update tms_domain_event
           set event_status=2,published_at=now(3),last_error=null,
               next_retry_at=null,updated_at=now(3)
         where id=#{id} and event_status in (1,3)
        """)
    int markPublished(@Param("id") long id);

    /**
     * 记录投递失败并设置退避时间。
     *
     * @param id Outbox 主键
     * @param reason 失败原因
     * @return 更新行数
     */
    @Update("""
        update tms_domain_event
           set event_status=3,retry_count=retry_count+1,last_error=#{reason},
               next_retry_at=date_add(
                   now(3),interval least(300,pow(2,least(retry_count,8))) second),
               updated_at=now(3)
         where id=#{id} and event_status in (1,3)
        """)
    int markFailed(@Param("id") long id, @Param("reason") String reason);

    record OutboxEvent(long id, String eventCode, String eventType,
                       String businessNo, String payload) {
    }
}
