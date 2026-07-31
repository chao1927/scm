package com.chaobo.scm.inventory.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 版本化 Inbox 与事件顺序游标 MyBatis 映射。
 *
 * <p>该 Mapper 只负责持久化；是否忽略、失败或推进版本由应用服务根据事件可靠性规则决定。
 *
 * @author SCM Team
 */
@Mapper
public interface InventoryReliableEventMapper {

    /**
     * 查询指定消费者的 Inbox。
     *
     * @param sourceSystem 来源系统
     * @param eventCode 事件编码
     * @param consumerName 消费者名称
     * @return Inbox 数据行；不存在时返回 {@code null}
     */
    @Select("""
            select inbox_id id,source_system,event_code,event_type,event_version,
                   aggregate_type,aggregate_id,aggregate_version,consumer_name,
                   cast(envelope_json as char) envelope_json,status,retry_count,
                   last_error,ignored_reason
              from inv_inbox_event
             where source_system=#{sourceSystem}
               and event_code=#{eventCode}
               and consumer_name=#{consumerName}
            """)
    InboxRow findInbox(
            @Param("sourceSystem") String sourceSystem,
            @Param("eventCode") String eventCode,
            @Param("consumerName") String consumerName);

    /**
     * 插入包含完整原始信封的 Inbox。
     *
     * @param row Inbox 数据行
     */
    @Insert("""
            insert into inv_inbox_event(
                source_system,event_code,event_type,event_version,
                aggregate_type,aggregate_id,aggregate_version,consumer_name,
                payload_json,envelope_json,status,retry_count,
                created_at,updated_at
            ) values(
                #{sourceSystem},#{eventCode},#{eventType},#{eventVersion},
                #{aggregateType},#{aggregateId},#{aggregateVersion},#{consumerName},
                json_extract(cast(#{envelopeJson} as json),'$.payload'),
                cast(#{envelopeJson} as json),1,0,now(3),now(3)
            )
            """)
    void insertInbox(InboxInsert row);

    /**
     * 标记 Inbox 成功。
     *
     * @param id Inbox ID
     * @return 受影响行数
     */
    @Update("""
            update inv_inbox_event
               set status=2,last_error=null,ignored_reason=null,updated_at=now(3)
             where inbox_id=#{id}
            """)
    int markSucceeded(@Param("id") long id);

    /**
     * 标记过期事件已忽略。
     *
     * @param id Inbox ID
     * @param reason 忽略原因
     * @return 受影响行数
     */
    @Update("""
            update inv_inbox_event
               set status=4,last_error=null,ignored_reason=#{reason},updated_at=now(3)
             where inbox_id=#{id}
            """)
    int markIgnored(
            @Param("id") long id,
            @Param("reason") String reason);

    /**
     * 标记 Inbox 失败并增加重试次数。
     *
     * @param id Inbox ID
     * @param reason 失败原因
     * @return 受影响行数
     */
    @Update("""
            update inv_inbox_event
               set status=3,retry_count=retry_count+1,last_error=#{reason},
                   ignored_reason=null,updated_at=now(3)
             where inbox_id=#{id}
            """)
    int markFailed(
            @Param("id") long id,
            @Param("reason") String reason);

    /** 标记前置版本缺失，等待重放且不累计业务处理重试次数。 */
    @Update("""
            update inv_inbox_event
               set status=5,last_error=#{reason},ignored_reason=null,updated_at=now(3)
             where inbox_id=#{id}
            """)
    int markWaitingReplay(
            @Param("id") long id,
            @Param("reason") String reason);

    /**
     * 查询来源聚合消费顺序游标。
     *
     * @param sourceSystem 来源系统
     * @param aggregateType 聚合类型
     * @param aggregateId 聚合 ID
     * @param consumerName 消费者名称
     * @return 游标；不存在时返回 {@code null}
     */
    @Select("""
            select aggregate_version,event_code
              from inv_event_aggregate_cursor
             where source_system=#{sourceSystem}
               and aggregate_type=#{aggregateType}
               and aggregate_id=#{aggregateId}
               and consumer_name=#{consumerName}
            """)
    CursorRow findCursor(
            @Param("sourceSystem") String sourceSystem,
            @Param("aggregateType") String aggregateType,
            @Param("aggregateId") String aggregateId,
            @Param("consumerName") String consumerName);

    /**
     * 创建首个成功版本游标。
     *
     * @param row 游标数据行
     */
    @Insert("""
            insert into inv_event_aggregate_cursor(
                source_system,aggregate_type,aggregate_id,consumer_name,
                aggregate_version,event_code,created_at,updated_at
            ) values(
                #{sourceSystem},#{aggregateType},#{aggregateId},#{consumerName},
                #{aggregateVersion},#{eventCode},now(3),now(3)
            )
            """)
    void insertCursor(CursorInsert row);

    /**
     * 使用期望版本推进事件顺序游标。
     *
     * @param row 新游标数据
     * @param expectedVersion 更新前期望版本
     * @return 受影响行数
     */
    @Update("""
            update inv_event_aggregate_cursor
               set aggregate_version=#{row.aggregateVersion},
                   event_code=#{row.eventCode},
                   updated_at=now(3)
             where source_system=#{row.sourceSystem}
               and aggregate_type=#{row.aggregateType}
               and aggregate_id=#{row.aggregateId}
               and consumer_name=#{row.consumerName}
               and aggregate_version=#{expectedVersion}
            """)
    int updateCursor(
            @Param("row") CursorInsert row,
            @Param("expectedVersion") long expectedVersion);

    /**
     * Inbox 查询行。
     */
    record InboxRow(
            long id,
            String sourceSystem,
            String eventCode,
            String eventType,
            String eventVersion,
            String aggregateType,
            String aggregateId,
            long aggregateVersion,
            String consumerName,
            String envelopeJson,
            int status,
            int retryCount,
            String lastError,
            String ignoredReason) {
    }

    /**
     * Inbox 新增行。
     */
    record InboxInsert(
            String sourceSystem,
            String eventCode,
            String eventType,
            String eventVersion,
            String aggregateType,
            String aggregateId,
            long aggregateVersion,
            String consumerName,
            String envelopeJson) {
    }

    /**
     * 聚合游标查询行。
     */
    record CursorRow(long aggregateVersion, String eventCode) {
    }

    /**
     * 聚合游标新增或更新行。
     */
    record CursorInsert(
            String sourceSystem,
            String aggregateType,
            String aggregateId,
            String consumerName,
            long aggregateVersion,
            String eventCode) {
    }
}
