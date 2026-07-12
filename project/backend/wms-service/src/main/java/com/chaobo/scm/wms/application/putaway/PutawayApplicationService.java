package com.chaobo.scm.wms.application.putaway;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.domain.putaway.PutawayTaskAggregate;
import com.chaobo.scm.wms.infrastructure.persistence.putaway.PutawayMapper;
import com.chaobo.scm.wms.infrastructure.persistence.stock.StockLedgerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PutawayApplicationService {
    private final PutawayMapper mapper;
    private final StockLedgerMapper ledger;
    private final WmsEventPublisher events;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    public PutawayApplicationService(PutawayMapper mapper, StockLedgerMapper ledger, WmsEventPublisher events) {
        this.mapper = mapper;
        this.ledger = ledger;
        this.events = events;
    }

    @Transactional
    public Result create(String no, long inspection, BigDecimal qty, long operator) {
        var existed = mapper.find(no);
        if (existed != null) {
            return view(map(existed), true);
        }

        var task = new PutawayTaskAggregate(ids.incrementAndGet(), no, inspection, qty);
        mapper.insert(
                task.id(),
                task.taskNo(),
                task.inspectionId(),
                task.requiredQty(),
                task.putawayQty(),
                1,
                task.version(),
                operator
        );
        return view(task, false);
    }

    @Transactional
    public Result scan(
            String no,
            int version,
            long warehouse,
            String location,
            String sku,
            String batch,
            BigDecimal qty,
            long operator
    ) {
        var task = load(no);
        if (task.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "上架任务版本冲突");
        }

        task.putaway(qty, location);
        int updated = mapper.update(
                task.id(),
                task.putawayQty(),
                task.completed() ? 2 : 1,
                task.version(),
                version,
                operator
        );
        if (updated != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "上架任务版本冲突");
        }

        ledger.insert(ids.incrementAndGet(), warehouse, location, sku, batch, "PUTAWAY_IN", qty, "PUTAWAY_TASK", task.taskNo());
        if (task.completed()) {
            events.publish("WmsPutawayCompleted", "PUTAWAY_TASK", task.taskNo(), task.version(), payload(task));
        }
        return view(task, false);
    }

    private PutawayTaskAggregate load(String no) {
        var row = mapper.find(no);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "上架任务不存在");
        }
        return map(row);
    }

    private PutawayTaskAggregate map(PutawayMapper.Row row) {
        return PutawayTaskAggregate.rehydrate(
                row.id(),
                row.no(),
                row.inspectionId(),
                row.required(),
                row.putaway(),
                row.status() == 2,
                row.version()
        );
    }

    private static Result view(PutawayTaskAggregate task, boolean duplicated) {
        return new Result(task.id(), task.taskNo(), task.putawayQty(), task.completed(), task.version(), duplicated);
    }

    private static String payload(PutawayTaskAggregate task) {
        return """
                {"taskNo":"%s","putawayQty":%s}
                """.formatted(task.taskNo(), task.putawayQty()).trim();
    }

    public record Result(long id, String no, BigDecimal qty, boolean completed, int version, boolean duplicated) {
    }
}
