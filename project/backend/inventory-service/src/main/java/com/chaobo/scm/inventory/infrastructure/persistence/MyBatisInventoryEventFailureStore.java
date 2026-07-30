package com.chaobo.scm.inventory.infrastructure.persistence;

import com.chaobo.scm.inventory.application.InventoryEventFailureStore;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

/**
 * MyBatis 库存事件失败治理存储。
 *
 * @author SCM Team
 */
@Repository
public class MyBatisInventoryEventFailureStore implements InventoryEventFailureStore {

    private final InventoryEventFailureMapper mapper;

    public MyBatisInventoryEventFailureStore(InventoryEventFailureMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public FailurePage failures(Direction direction, int offset, int limit) {
        long total = direction == Direction.INBOUND
                ? mapper.countInboundFailures()
                : mapper.countOutboundFailures();
        List<InventoryEventFailureMapper.FailureRow> rows = direction == Direction.INBOUND
                ? mapper.inboundFailures(offset, limit)
                : mapper.outboundFailures(offset, limit);
        return new FailurePage(
                total,
                rows.stream().map(row -> toFailure(direction, row)).toList());
    }

    @Override
    public FailureEvent findFailure(Direction direction, String eventCode) {
        InventoryEventFailureMapper.FailureRow row = direction == Direction.INBOUND
                ? mapper.findInboundFailure(eventCode)
                : mapper.findOutboundFailure(eventCode);
        return row == null ? null : toFailure(direction, row);
    }

    @Override
    public ReplayRegistration registerReplay(
            String idempotencyKey,
            Direction direction,
            String eventCode,
            String reason,
            long operatorId) {
        InventoryEventFailureMapper.ReplayInsert insert =
                new InventoryEventFailureMapper.ReplayInsert(
                        idempotencyKey,
                        direction.name(),
                        eventCode,
                        reason,
                        operatorId);
        try {
            mapper.insertReplay(insert);
            return new ReplayRegistration(insert.getId(), true, 1);
        } catch (DuplicateKeyException ignored) {
            InventoryEventFailureMapper.ReplayRow existing =
                    mapper.findReplay(idempotencyKey);
            return new ReplayRegistration(
                    existing.replayId(),
                    false,
                    existing.replayStatus());
        }
    }

    @Override
    public void markReplaySucceeded(long replayId) {
        mapper.markReplaySucceeded(replayId);
    }

    @Override
    public void markReplayFailed(long replayId, String reason) {
        mapper.markReplayFailed(replayId, reason);
    }

    private static FailureEvent toFailure(
            Direction direction,
            InventoryEventFailureMapper.FailureRow row) {
        return new FailureEvent(
                direction,
                row.eventCode(),
                row.eventType(),
                row.eventVersion(),
                row.aggregateType(),
                row.aggregateId(),
                row.status(),
                row.retryCount(),
                row.lastError(),
                row.rawJson());
    }
}
