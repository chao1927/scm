package com.chaobo.scm.inventory.infrastructure.persistence;

import com.chaobo.scm.inventory.application.InventoryBatchFactPort;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;

/**
 * 批次效期事实持久化适配器。
 *
 * @author SCM Team
 */
@Repository
public class MyBatisInventoryBatchFactAdapter implements InventoryBatchFactPort {

    private final InventoryBatchFactMapper mapper;

    public MyBatisInventoryBatchFactAdapter(InventoryBatchFactMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void recordExpiry(
            long stockId,
            LocalDate expiryDate,
            String sourceEvent,
            LocalDateTime factAt) {
        mapper.upsertExpiry(stockId, expiryDate, sourceEvent, factAt);
    }
}
