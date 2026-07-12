package com.chaobo.scm.purchase.infrastructure.persistence.integration;

import com.chaobo.scm.purchase.application.operations.PurchaseOperationsViews;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PurchaseOperationsMapper {
    @Select("""
            select inbox_id id, source_system sourceSystem, event_code eventCode, event_type eventType,
                   consumer_name consumerName, retry_count retryCount, last_error reason, updated_at updatedAt
            from purchase_inbox_event
            where status = 3
            order by updated_at desc
            limit #{limit}
            """)
    List<PurchaseOperationsViews.FailedEvent> failedInboundEvents(int limit);
}
