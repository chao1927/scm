package com.chaobo.scm.wms.infrastructure.persistence.operation;

import org.apache.ibatis.annotations.*;

@Mapper
public interface WarehouseExceptionMapper {
    record Row(long id, String no, String reason, int status, int version) {}

    @Select("select exception_id id,exception_no no,reason,exception_status status,version from wms_warehouse_exception where exception_no=#{no}")
    Row find(@Param("no") String no);

    @Insert("insert into wms_warehouse_exception(exception_id,exception_no,reason,exception_status,version,created_at,updated_at) values(#{id},#{no},#{reason},#{status},#{version},now(3),now(3))")
    void insert(@Param("id") long id, @Param("no") String no, @Param("reason") String reason, @Param("status") int status, @Param("version") int version);

    @Update("update wms_warehouse_exception set exception_status=#{status},version=#{version},updated_at=now(3) where exception_id=#{id} and version=#{oldVersion}")
    int update(@Param("id") long id, @Param("status") int status, @Param("version") int version, @Param("oldVersion") int oldVersion);
}
