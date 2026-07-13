package com.chaobo.scm.mdm.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MdmOpenApiMapper {
    @Insert("insert ignore into mdm_event_inbox(event_id,event_type,business_no,payload,event_status,error_message,created_at,updated_at) values(#{eventId},#{eventType},#{businessNo},#{payload},#{status},#{errorMessage},now(),now())")
    int claimEvent(MdmPublicationMapper.EventInboxRow row);

    @Update("update mdm_event_inbox set event_status=#{status},error_message=#{errorMessage},updated_at=now() where event_id=#{eventId}")
    void updateEvent(MdmPublicationMapper.EventInboxRow row);

    @Select("select event_id eventId,event_type eventType,business_no businessNo,payload,event_status status,error_message errorMessage from mdm_event_inbox order by updated_at desc")
    List<MdmPublicationMapper.EventInboxRow> listInboxEvents();

    @Insert("insert into mdm_outbox_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(MdmMapper.OutboxRow row);

    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from mdm_outbox_event order by id desc")
    List<MdmMapper.OutboxRow> listOutbox();
}
