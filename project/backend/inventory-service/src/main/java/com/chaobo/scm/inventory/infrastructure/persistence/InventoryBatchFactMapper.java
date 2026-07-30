package com.chaobo.scm.inventory.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 库存批次事实 MyBatis 映射。
 *
 * @author SCM Team
 */
@Mapper
public interface InventoryBatchFactMapper {

    /**
     * 按库存账户覆盖最新的 WMS 效期事实。
     */
    @Insert("""
            insert into inv_inventory_batch_fact(
                stock_id,expiry_date,expiry_source_event,expiry_fact_at,created_at,updated_at
            ) values(
                #{stockId},#{expiryDate},#{sourceEvent},#{factAt},now(3),now(3)
            )
            on duplicate key update
                expiry_date=values(expiry_date),
                expiry_source_event=values(expiry_source_event),
                expiry_fact_at=values(expiry_fact_at),
                updated_at=now(3)
            """)
    void upsertExpiry(
            @Param("stockId") long stockId,
            @Param("expiryDate") LocalDate expiryDate,
            @Param("sourceEvent") String sourceEvent,
            @Param("factAt") LocalDateTime factAt);
}
