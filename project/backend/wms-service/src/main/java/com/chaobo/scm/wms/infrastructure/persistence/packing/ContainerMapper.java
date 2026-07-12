package com.chaobo.scm.wms.infrastructure.persistence.packing;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ContainerMapper {
    record Row(long id, String containerNo, long outboundId, long pickTaskId, int status, int version) {
    }

    @Select("""
            select container_id id, container_no containerNo, outbound_id outboundId,
                   pick_task_id pickTaskId, container_status status, version
            from wms_container
            where container_no=#{containerNo}
            """)
    Row find(@Param("containerNo") String containerNo);

    @Insert("""
            insert into wms_container(
                container_id, container_no, outbound_id, pick_task_id, container_status,
                version, created_at, updated_at
            )
            values(#{id}, #{containerNo}, #{outboundId}, #{pickTaskId}, #{status}, #{version}, now(3), now(3))
            """)
    void insert(
            @Param("id") long id,
            @Param("containerNo") String containerNo,
            @Param("outboundId") long outboundId,
            @Param("pickTaskId") long pickTaskId,
            @Param("status") int status,
            @Param("version") int version
    );

    @Update("""
            update wms_container
            set container_status=#{status}, version=#{version}, updated_at=now(3)
            where container_id=#{id} and version=#{oldVersion}
            """)
    int update(@Param("id") long id, @Param("status") int status, @Param("version") int version, @Param("oldVersion") int oldVersion);
}
