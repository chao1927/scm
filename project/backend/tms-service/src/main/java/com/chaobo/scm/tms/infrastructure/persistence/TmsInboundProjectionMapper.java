package com.chaobo.scm.tms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/** 保存 TMS 消费外部事件后形成的主数据、审批和运输事实投影。 */
@Mapper
public interface TmsInboundProjectionMapper {

    @Insert("""
        insert into tms_inbound_business_projection(
            projection_type,object_key,source_system,event_id,event_type,
            projection_status,payload,created_at,updated_at)
        values(#{projectionType},#{objectKey},#{sourceSystem},#{eventId},#{eventType},
            #{status},#{payload},now(3),now(3))
        on duplicate key update source_system=values(source_system),event_id=values(event_id),
            event_type=values(event_type),projection_status=values(projection_status),
            payload=values(payload),updated_at=now(3)
        """)
    void upsert(ProjectionRow row);

    record ProjectionRow(String projectionType, String objectKey, String sourceSystem,
                         String eventId, String eventType, String status, String payload) {
    }
}
