package com.chaobo.scm.wms.infrastructure.persistence.outbound;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OutboundMapper {
    record Row(long id, String no, String sourceType, String sourceNo, long warehouseId, int status, int version) {
    }

    @Select("""
            select outbound_id id, outbound_no no, source_type sourceType, source_no sourceNo,
                   warehouse_id warehouseId, outbound_status status, version
            from wms_outbound
            where source_type=#{type} and source_no=#{sourceNo} and warehouse_id=#{warehouseId}
            """)
    Row source(
            @Param("type") String type,
            @Param("sourceNo") String sourceNo,
            @Param("warehouseId") long warehouseId
    );

    @Insert("""
            insert into wms_outbound(
                outbound_id, outbound_no, source_type, source_no, warehouse_id,
                outbound_status, version, created_by, updated_by, created_at, updated_at
            )
            values(
                #{id}, #{no}, #{type}, #{sourceNo}, #{warehouseId},
                1, 0, #{operator}, #{operator}, now(3), now(3)
            )
            """)
    void insert(
            @Param("id") long id,
            @Param("no") String no,
            @Param("type") String type,
            @Param("sourceNo") String sourceNo,
            @Param("warehouseId") long warehouseId,
            @Param("operator") long operator
    );

    @Update("""
            update wms_outbound
            set outbound_status=#{status}, version=#{version}, updated_by=#{operator}, updated_at=now(3)
            where outbound_id=#{id} and version=#{oldVersion}
            """)
    int update(
            @Param("id") long id,
            @Param("status") int status,
            @Param("version") int version,
            @Param("oldVersion") int oldVersion,
            @Param("operator") long operator
    );
}
