package com.chaobo.scm.wms.infrastructure.persistence.stock;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface StockLedgerMapper {
    @Insert("""
            insert into wms_stock_ledger(
                ledger_id, warehouse_id, location_code, sku_code, batch_no,
                transaction_type, quantity, source_type, source_no, occurred_at, created_at
            )
            values(
                #{id}, #{warehouse}, #{location}, #{sku}, #{batch},
                #{type}, #{qty}, #{sourceType}, #{sourceNo}, now(3), now(3)
            )
            """)
    void insert(
            @Param("id") long id,
            @Param("warehouse") long warehouse,
            @Param("location") String location,
            @Param("sku") String sku,
            @Param("batch") String batch,
            @Param("type") String type,
            @Param("qty") BigDecimal qty,
            @Param("sourceType") String sourceType,
            @Param("sourceNo") String sourceNo
    );
}
