package com.chaobo.scm.wms.infrastructure.persistence.picking;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface PickTaskMapper {
    record Row(
            long id,
            String no,
            long waveId,
            long outboundId,
            String sku,
            BigDecimal required,
            BigDecimal picked,
            int status,
            int version
    ) {
    }

    @Select("""
            select task_id id, task_no no, wave_id waveId, outbound_id outboundId, sku_code sku,
                   required_qty required, picked_qty picked, task_status status, version
            from wms_pick_task
            where task_no=#{no}
            """)
    Row find(@Param("no") String no);

    @Insert("""
            insert into wms_pick_task(
                task_id, task_no, wave_id, outbound_id, sku_code, required_qty,
                picked_qty, task_status, version, created_at, updated_at
            )
            values(
                #{id}, #{no}, #{waveId}, #{outboundId}, #{sku}, #{required},
                #{picked}, #{status}, #{version}, now(3), now(3)
            )
            """)
    void insert(
            @Param("id") long id,
            @Param("no") String no,
            @Param("waveId") long waveId,
            @Param("outboundId") long outboundId,
            @Param("sku") String sku,
            @Param("required") BigDecimal required,
            @Param("picked") BigDecimal picked,
            @Param("status") int status,
            @Param("version") int version
    );

    @Update("""
            update wms_pick_task
            set picked_qty=#{picked}, task_status=#{status}, version=#{version}, updated_at=now(3)
            where task_id=#{id} and version=#{oldVersion}
            """)
    int update(
            @Param("id") long id,
            @Param("picked") BigDecimal picked,
            @Param("status") int status,
            @Param("version") int version,
            @Param("oldVersion") int oldVersion
    );
}
