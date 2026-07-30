package com.chaobo.scm.bms.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * BMS Outbox RocketMQ 投递映射。
 *
 * @author SCM Team
 */
@Mapper
public interface BmsOutboxMapper {

    /**
     * 查询达到投递时间且未超过重试上限的事件。
     *
     * @param limit 最大数量
     * @param maxRetries 最大重试次数
     * @return 待投递事件
     */
    @Select("select event_no eventNo,event_type eventType,aggregate_no aggregateNo,"
        + "business_no businessNo,payload from bms_domain_event "
        + "where status in (1,3) and retry_count<#{maxRetries} "
        + "and (next_retry_at is null or next_retry_at<=now(3)) "
        + "order by created_at,id limit #{limit}")
    List<OutboxEvent> pending(@Param("limit") int limit,
                              @Param("maxRetries") int maxRetries);

    /**
     * 标记事件已成功投递 RocketMQ。
     *
     * @param eventNo 事件编号
     * @return 影响行数
     */
    @Update("update bms_domain_event set status=2,published_at=now(3),"
        + "last_error=null,next_retry_at=null,updated_at=now(3) "
        + "where event_no=#{eventNo} and status in (1,3)")
    int markPublished(@Param("eventNo") String eventNo);

    /**
     * 标记事件投递失败并安排退避重试。
     *
     * @param eventNo 事件编号
     * @param reason 失败原因
     * @return 影响行数
     */
    @Update("update bms_domain_event set status=3,retry_count=retry_count+1,"
        + "last_error=#{reason},next_retry_at=date_add(now(3),"
        + "interval least(300,pow(2,least(retry_count,8))) second),"
        + "updated_at=now(3) where event_no=#{eventNo} and status in (1,3)")
    int markFailed(@Param("eventNo") String eventNo,
                   @Param("reason") String reason);

    record OutboxEvent(String eventNo, String eventType, String aggregateNo,
                       String businessNo, String payload) {
    }
}
