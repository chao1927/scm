package com.chaobo.scm.wms.infrastructure.persistence.wave;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WaveMapper {
    record Row(long id, String no, long warehouseId, int status, int version) {
    }

    @Select("""
            select wave_id id, wave_no no, warehouse_id warehouseId, wave_status status, version
            from wms_wave
            where wave_no=#{no}
            """)
    Row find(@Param("no") String no);

    @Insert("""
            insert into wms_wave(wave_id, wave_no, warehouse_id, wave_status, version, created_at, updated_at)
            values(#{id}, #{no}, #{warehouseId}, #{status}, #{version}, now(3), now(3))
            """)
    void insert(
            @Param("id") long id,
            @Param("no") String no,
            @Param("warehouseId") long warehouseId,
            @Param("status") int status,
            @Param("version") int version
    );

    @Update("""
            update wms_wave
            set wave_status=#{status}, version=#{version}, updated_at=now(3)
            where wave_id=#{id} and version=#{oldVersion}
            """)
    int update(@Param("id") long id, @Param("status") int status, @Param("version") int version, @Param("oldVersion") int oldVersion);
}
