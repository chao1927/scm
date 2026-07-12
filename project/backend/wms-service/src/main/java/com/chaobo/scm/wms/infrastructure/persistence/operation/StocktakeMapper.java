package com.chaobo.scm.wms.infrastructure.persistence.operation;

import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;

@Mapper
public interface StocktakeMapper {
    record Row(long id, String no, long warehouseId, String sku, BigDecimal differenceQty, int status, int version) {}

    @Select("select stocktake_id id,stocktake_no no,warehouse_id warehouseId,sku_code sku,difference_qty differenceQty,stocktake_status status,version from wms_stocktake where stocktake_no=#{no}")
    Row find(@Param("no") String no);

    @Insert("insert into wms_stocktake(stocktake_id,stocktake_no,warehouse_id,sku_code,difference_qty,stocktake_status,version,created_at,updated_at) values(#{id},#{no},#{warehouseId},#{sku},#{differenceQty},#{status},#{version},now(3),now(3))")
    void insert(@Param("id") long id, @Param("no") String no, @Param("warehouseId") long warehouseId, @Param("sku") String sku, @Param("differenceQty") BigDecimal differenceQty, @Param("status") int status, @Param("version") int version);

    @Update("update wms_stocktake set stocktake_status=#{status},version=#{version},updated_at=now(3) where stocktake_id=#{id} and version=#{oldVersion}")
    int update(@Param("id") long id, @Param("status") int status, @Param("version") int version, @Param("oldVersion") int oldVersion);
}
