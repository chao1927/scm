package com.chaobo.scm.wms.infrastructure.persistence.event;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface WmsEventMapper {
    record Row(
            long id,
            String code,
            String type,
            String aggregateType,
            String aggregateId,
            int version,
            String payload,
            int status,
            int retryCount
    ) {
    }

    @Insert("""
            insert into wms_operation_event(
                event_id, event_code, event_type, aggregate_type, aggregate_id,
                aggregate_version, payload_json, status, retry_count, created_at, updated_at
            )
            values(
                #{id}, #{code}, #{type}, #{aggregateType}, #{aggregateId},
                #{version}, cast(#{payload} as json), 1, 0, now(3), now(3)
            )
            """)
    void insert(
            @Param("id") long id,
            @Param("code") String code,
            @Param("type") String type,
            @Param("aggregateType") String aggregateType,
            @Param("aggregateId") String aggregateId,
            @Param("version") int version,
            @Param("payload") String payload
    );

    @Select("""
            select event_id id, event_code code, event_type type, aggregate_type aggregateType,
                   aggregate_id aggregateId, aggregate_version version,
                   cast(payload_json as char) payload, status, retry_count retryCount
            from wms_operation_event
            where status in (1, 3)
            order by created_at
            limit #{limit}
            """)
    List<Row> pending(@Param("limit") int limit);

    @Update("""
            update wms_operation_event
            set status=2, updated_at=now(3)
            where event_id=#{id}
            """)
    int markPublished(@Param("id") long id);

    @Update("""
            update wms_operation_event
            set status=3, retry_count=retry_count+1, updated_at=now(3)
            where event_id=#{id}
            """)
    int markFailed(@Param("id") long id);

    @Select("""
            select event_id id, event_code code, event_type type, aggregate_type aggregateType,
                   aggregate_id aggregateId, aggregate_version version,
                   cast(payload_json as char) payload, status, retry_count retryCount
            from wms_operation_event
            where status=3
            order by updated_at desc
            limit #{limit}
            """)
    List<Row> failed(@Param("limit") int limit);

    @Update("""
            update wms_operation_event
            set status=1, updated_at=now(3)
            where event_id=#{id} and status=3
            """)
    int retry(@Param("id") long id);
}
