package com.chaobo.scm.wms.infrastructure.persistence.operation;

import org.apache.ibatis.annotations.*;

@Mapper
public interface ShipmentHandoverMapper {
    record Row(long id, String no, long outboundId, int status, int version) {}

    @Select("select handover_id id,handover_no no,outbound_id outboundId,handover_status status,version from wms_shipment_handover where handover_no=#{no}")
    Row find(@Param("no") String no);

    @Insert("insert into wms_shipment_handover(handover_id,handover_no,outbound_id,handover_status,version,created_at,updated_at) values(#{id},#{no},#{outboundId},#{status},#{version},now(3),now(3))")
    void insert(@Param("id") long id, @Param("no") String no, @Param("outboundId") long outboundId, @Param("status") int status, @Param("version") int version);

    @Update("update wms_shipment_handover set handover_status=#{status},version=#{version},updated_at=now(3) where handover_id=#{id} and version=#{oldVersion}")
    int update(@Param("id") long id, @Param("status") int status, @Param("version") int version, @Param("oldVersion") int oldVersion);
}
