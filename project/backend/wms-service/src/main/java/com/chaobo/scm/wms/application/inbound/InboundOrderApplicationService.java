package com.chaobo.scm.wms.application.inbound;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.domain.inbound.InboundOrderAggregate;
import com.chaobo.scm.wms.domain.inbound.InboundOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InboundOrderApplicationService {
    private final InboundOrderRepository repository;
    private final AtomicLong sequence = new AtomicLong(System.currentTimeMillis());

    public InboundOrderApplicationService(InboundOrderRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public WmsCommandResult create(Create command, long operatorId) {
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "外部创建入库单必须提供幂等键");
        }
        var existed = repository.findBySource(command.sourceType(), command.sourceNo(), command.warehouseId());
        if (existed.isPresent()) {
            return result(existed.get(), true);
        }
        var id = sequence.incrementAndGet();
        var inboundNo = "WIB" + LocalDate.now().toString().replace("-", "") + id;
        var order = InboundOrderAggregate.create(id, inboundNo, command.sourceType(), command.sourceNo(),
                command.warehouseId(), command.expectedArrivalAt());
        repository.save(order, operatorId);
        return result(order, false);
    }

    @Transactional
    public WmsCommandResult cancel(long id, Cancel command, long warehouseScope, long operatorId) {
        var order = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "WMS入库单不存在"));
        if (warehouseScope > 0 && warehouseScope != order.warehouseId()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无仓库数据权限");
        }
        if (order.version() != command.version()) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "WMS入库单已被其他人修改");
        }
        order.cancel(command.reason());
        repository.save(order, operatorId);
        return result(order, false);
    }

    private static WmsCommandResult result(InboundOrderAggregate order, boolean duplicated) {
        return new WmsCommandResult(order.id(), order.inboundNo(), order.status().code(), order.status().label(),
                order.version(), duplicated);
    }

    public record Create(String sourceType, String sourceNo, long warehouseId, OffsetDateTime expectedArrivalAt,
                         String idempotencyKey) {
    }

    public record Cancel(int version, String reason) {
    }
}
