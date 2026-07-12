package com.chaobo.scm.wms.application.outbound;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.domain.outbound.OutboundOrderAggregate;
import com.chaobo.scm.wms.infrastructure.persistence.outbound.OutboundMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class OutboundApplicationService {
    private final OutboundMapper mapper;
    private final WmsEventPublisher events;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    public OutboundApplicationService(OutboundMapper mapper, WmsEventPublisher events) {
        this.mapper = mapper;
        this.events = events;
    }

    @Transactional
    public Result create(String sourceType, String sourceNo, long warehouseId, long operator) {
        var existed = mapper.source(sourceType, sourceNo, warehouseId);
        if (existed != null) {
            return view(toAggregate(existed), true);
        }

        long id = ids.incrementAndGet();
        var outboundNo = "WOB" + id;
        mapper.insert(id, outboundNo, sourceType, sourceNo, warehouseId, operator);
        events.publish("WmsOutboundOrderCreated", "OUTBOUND", outboundNo, 0, payload(outboundNo));
        return new Result(id, outboundNo, 1, 0, false);
    }

    @Transactional
    public Result allocate(String sourceType, String sourceNo, long warehouseId, int version, long operator) {
        var outbound = toAggregate(required(sourceType, sourceNo, warehouseId));
        if (outbound.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "出库单版本冲突");
        }

        outbound.allocate();
        save(outbound, version, operator);
        events.publish("WmsOutboundAllocated", "OUTBOUND", outbound.no(), outbound.version(), payload(outbound.no()));
        return view(outbound, false);
    }

    @Transactional
    public Result cancel(String sourceType, String sourceNo, long warehouseId, int version, String reason, long operator) {
        var outbound = toAggregate(required(sourceType, sourceNo, warehouseId));
        if (outbound.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "出库单版本冲突");
        }

        outbound.cancel(reason);
        save(outbound, version, operator);
        events.publish("WmsOutboundCancelled", "OUTBOUND", outbound.no(), outbound.version(), payload(outbound.no()));
        return view(outbound, false);
    }

    private OutboundMapper.Row required(String sourceType, String sourceNo, long warehouseId) {
        var row = mapper.source(sourceType, sourceNo, warehouseId);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "出库单不存在");
        }
        return row;
    }

    private OutboundOrderAggregate toAggregate(OutboundMapper.Row row) {
        return new OutboundOrderAggregate(
                row.id(),
                row.no(),
                row.sourceType(),
                row.sourceNo(),
                row.warehouseId(),
                row.status(),
                row.version()
        );
    }

    private void save(OutboundOrderAggregate outbound, int oldVersion, long operator) {
        if (mapper.update(outbound.id(), outbound.status(), outbound.version(), oldVersion, operator) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "出库单版本冲突");
        }
    }

    private static Result view(OutboundOrderAggregate outbound, boolean duplicated) {
        return new Result(outbound.id(), outbound.no(), outbound.status(), outbound.version(), duplicated);
    }

    private static String payload(String outboundNo) {
        return """
                {"outboundNo":"%s"}
                """.formatted(outboundNo).trim();
    }

    public record Result(long id, String no, int status, int version, boolean duplicated) {
    }
}
