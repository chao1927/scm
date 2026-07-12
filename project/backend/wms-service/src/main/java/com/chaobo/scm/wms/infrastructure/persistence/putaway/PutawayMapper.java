package com.chaobo.scm.wms.infrastructure.persistence.putaway;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface PutawayMapper {
    record Row(
            long id,
            String no,
            long inspectionId,
            BigDecimal required,
            BigDecimal putaway,
            int status,
            int version
    ) {
    }

    @Select("""
            select task_id id, task_no no, inspection_id inspectionId, required_qty required,
                   putaway_qty putaway, task_status status, version
            from wms_putaway_task
            where task_no=#{no}
            """)
    Row find(String no);

    @Insert("""
            insert into wms_putaway_task(
                task_id, task_no, inspection_id, required_qty, putaway_qty,
                task_status, version, created_by, updated_by, created_at, updated_at
            )
            values(
                #{id}, #{no}, #{inspection}, #{required}, #{putaway},
                #{status}, #{version}, #{operator}, #{operator}, now(3), now(3)
            )
            """)
    void insert(
            @Param("id") long id,
            @Param("no") String no,
            @Param("inspection") long inspection,
            @Param("required") BigDecimal required,
            @Param("putaway") BigDecimal putaway,
            @Param("status") int status,
            @Param("version") int version,
            @Param("operator") long operator
    );

    @Update("""
            update wms_putaway_task
            set putaway_qty=#{putaway}, task_status=#{status}, version=#{version},
                updated_by=#{operator}, updated_at=now(3)
            where task_id=#{id} and version=#{expected}
            """)
    int update(
            @Param("id") long id,
            @Param("putaway") BigDecimal putaway,
            @Param("status") int status,
            @Param("version") int version,
            @Param("expected") int expected,
            @Param("operator") long operator
    );
}
