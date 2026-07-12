package com.chaobo.scm.wms.infrastructure.persistence.packing;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PackingMapper {
    record Row(long id, String packingNo, long outboundId, String containerNo, int status, int version) {
    }

    @Select("""
            select packing_id id, packing_no packingNo, outbound_id outboundId,
                   container_no containerNo, packing_status status, version
            from wms_packing
            where packing_no=#{packingNo}
            """)
    Row find(@Param("packingNo") String packingNo);

    @Insert("""
            insert into wms_packing(
                packing_id, packing_no, outbound_id, container_no, packing_status,
                version, created_at, updated_at
            )
            values(#{id}, #{packingNo}, #{outboundId}, #{containerNo}, #{status}, #{version}, now(3), now(3))
            """)
    void insert(
            @Param("id") long id,
            @Param("packingNo") String packingNo,
            @Param("outboundId") long outboundId,
            @Param("containerNo") String containerNo,
            @Param("status") int status,
            @Param("version") int version
    );

    @Update("""
            update wms_packing
            set packing_status=#{status}, version=#{version}, updated_at=now(3)
            where packing_id=#{id} and version=#{oldVersion}
            """)
    int update(@Param("id") long id, @Param("status") int status, @Param("version") int version, @Param("oldVersion") int oldVersion);
}
