package com.chaobo.scm.oms.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * OMS 可靠事件 Outbox 投递 Mapper。
 */
@Mapper
public interface OmsOutboxMapper {

    @Select("""
            select id,event_code eventCode,event_type eventType,
                   business_no businessNo,payload,retry_count retryCount
            from oms_outbox_event
            where (event_status in (1,3)
                   or (event_status=4 and last_attempt_at < date_sub(now(),interval 5 minute)))
              and retry_count < 10
            order by id limit #{limit}
            """)
    List<OutboxMessage> pending(@Param("limit") int limit);

    @Update("""
            update oms_outbox_event set event_status=4,last_attempt_at=now()
            where id=#{id}
              and (event_status in (1,3)
                   or (event_status=4 and last_attempt_at < date_sub(now(),interval 5 minute)))
            """)
    int claim(@Param("id") long id);

    @Update("""
            update oms_outbox_event
            set event_status=2,last_error=null,published_at=now(),last_attempt_at=now()
            where id=#{id} and event_status=4
            """)
    int markPublished(@Param("id") long id);

    @Update("""
            update oms_outbox_event
            set event_status=3,retry_count=retry_count+1,last_error=#{error},
                last_attempt_at=now()
            where id=#{id} and event_status=4
            """)
    int markFailed(@Param("id") long id, @Param("error") String error);

    record OutboxMessage(long id, String eventCode, String eventType,
                         String businessNo, String payload, int retryCount) {
    }
}
