package com.chaobo.scm.inventory.infrastructure.persistence;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface InventoryEventMapper {
    record EventRow(long id, String eventCode, String eventType, String aggregateType, String aggregateId, String payload, int status, int retryCount) {}
    record InboxRow(long id, String sourceSystem, String eventCode, String eventType, String payload, int status, String lastError) {}

    @Insert("insert into inv_outbox_event(event_id,event_code,event_type,aggregate_type,aggregate_id,payload_json,status,retry_count,created_at,updated_at) values(#{id},#{code},#{type},#{aggregateType},#{aggregateId},cast(#{payload} as json),1,0,now(3),now(3))")
    void insertOutbox(@Param("id") long id, @Param("code") String code, @Param("type") String type, @Param("aggregateType") String aggregateType, @Param("aggregateId") String aggregateId, @Param("payload") String payload);

    @Select("select event_id id,event_code eventCode,event_type eventType,aggregate_type aggregateType,aggregate_id aggregateId,cast(payload_json as char) payload,status,retry_count retryCount from inv_outbox_event where status in (1,3) order by created_at limit #{limit}")
    List<EventRow> pending(@Param("limit") int limit);

    @Update("update inv_outbox_event set status=2,updated_at=now(3) where event_id=#{id}")
    int markPublished(@Param("id") long id);

    @Update("update inv_outbox_event set status=3,retry_count=retry_count+1,updated_at=now(3) where event_id=#{id}")
    int markFailed(@Param("id") long id);

    @Insert("insert into inv_inbox_event(source_system,event_code,event_type,payload_json,status,retry_count,created_at,updated_at) values(#{sourceSystem},#{eventCode},#{eventType},cast(#{payload} as json),1,0,now(3),now(3))")
    void insertInbox(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode, @Param("eventType") String eventType, @Param("payload") String payload);

    @Select("select inbox_id id,source_system sourceSystem,event_code eventCode,event_type eventType,cast(payload_json as char) payload,status,last_error lastError from inv_inbox_event where source_system=#{sourceSystem} and event_code=#{eventCode}")
    InboxRow findInbox(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode);

    @Update("update inv_inbox_event set status=2,last_error=null,updated_at=now(3) where inbox_id=#{id}")
    int markInboxSucceeded(@Param("id") long id);

    @Update("update inv_inbox_event set status=3,retry_count=retry_count+1,last_error=#{error},updated_at=now(3) where inbox_id=#{id}")
    int markInboxFailed(@Param("id") long id, @Param("error") String error);
}
