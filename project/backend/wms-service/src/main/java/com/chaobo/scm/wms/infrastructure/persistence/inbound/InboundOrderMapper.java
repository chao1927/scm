package com.chaobo.scm.wms.infrastructure.persistence.inbound;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;

@Mapper
public interface InboundOrderMapper {
    record Row(long id, String inboundNo, String sourceType, String sourceNo, long warehouseId, int status,
               OffsetDateTime expectedArrivalAt, String cancelReason, int version) {
    }

    @Select("select * from wms_inbound where inbound_id = #{id} and deleted = 0")
    Row findById(long id);

    @Select("""
            select * from wms_inbound where source_type = #{sourceType} and source_order_no = #{sourceNo}
            and warehouse_id = #{warehouseId} and deleted = 0
            """)
    Row findBySource(@Param("sourceType") String sourceType, @Param("sourceNo") String sourceNo,
                     @Param("warehouseId") long warehouseId);

    @Insert("""
            insert into wms_inbound(inbound_id, inbound_order_no, source_type, source_order_no, warehouse_id,
              inbound_status, expected_arrival_at, cancel_reason, version, deleted, created_by, updated_by, created_at, updated_at)
            values(#{id}, #{inboundNo}, #{sourceType}, #{sourceNo}, #{warehouseId}, #{status}, #{expectedArrivalAt},
              #{cancelReason}, #{version}, 0, #{operatorId}, #{operatorId}, now(3), now(3))
            """)
    void insert(@Param("id") long id, @Param("inboundNo") String inboundNo, @Param("sourceType") String sourceType,
                @Param("sourceNo") String sourceNo, @Param("warehouseId") long warehouseId, @Param("status") int status,
                @Param("expectedArrivalAt") OffsetDateTime expectedArrivalAt, @Param("cancelReason") String cancelReason,
                @Param("version") int version, @Param("operatorId") long operatorId);

    @Update("""
            update wms_inbound set inbound_status = #{status}, cancel_reason = #{cancelReason}, version = #{version},
              updated_by = #{operatorId}, updated_at = now(3) where inbound_id = #{id} and deleted = 0
            """)
    void update(@Param("id") long id, @Param("status") int status, @Param("cancelReason") String cancelReason,
                @Param("version") int version, @Param("operatorId") long operatorId);
}
