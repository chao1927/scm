package com.chaobo.scm.wms.infrastructure.persistence.event;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface WmsInboxMapper {
    record Row(
            long id,
            String sourceSystem,
            String eventCode,
            String eventType,
            String payload,
            int status,
            int retryCount,
            String lastError
    ) {
    }

    @Select("""
            select inbox_id id, source_system sourceSystem, event_code eventCode,
                   event_type eventType, cast(payload_json as char) payload,
                   status, retry_count retryCount, last_error lastError
            from wms_inbox_event
            where source_system=#{sourceSystem} and event_code=#{eventCode}
            """)
    Row find(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode);

    @Insert("""
            insert into wms_inbox_event(
                source_system, event_code, event_type, payload_json, status,
                retry_count, created_at, updated_at
            )
            values(
                #{sourceSystem}, #{eventCode}, #{eventType}, cast(#{payload} as json),
                1, 0, now(3), now(3)
            )
            """)
    void insert(
            @Param("sourceSystem") String sourceSystem,
            @Param("eventCode") String eventCode,
            @Param("eventType") String eventType,
            @Param("payload") String payload
    );

    @Update("""
            update wms_inbox_event
            set status=2, last_error=null, updated_at=now(3)
            where inbox_id=#{id}
            """)
    int markSucceeded(@Param("id") long id);

    @Update("""
            update wms_inbox_event
            set status=3, retry_count=retry_count+1, last_error=#{message}, updated_at=now(3)
            where inbox_id=#{id}
            """)
    int markFailed(@Param("id") long id, @Param("message") String message);

    @Select("""
            select inbox_id id, source_system sourceSystem, event_code eventCode,
                   event_type eventType, cast(payload_json as char) payload,
                   status, retry_count retryCount, last_error lastError
            from wms_inbox_event
            where status=3
            order by updated_at desc
            limit #{limit}
            """)
    List<Row> failed(@Param("limit") int limit);
}
