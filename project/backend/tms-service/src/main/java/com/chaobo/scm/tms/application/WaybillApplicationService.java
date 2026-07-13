package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.domain.TmsEvent;
import com.chaobo.scm.tms.domain.TransportTaskAggregate;
import com.chaobo.scm.tms.domain.WaybillAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import com.chaobo.scm.tms.infrastructure.persistence.WaybillMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class WaybillApplicationService {
    private final WaybillMapper mapper;
    private final TransportTaskApplicationService transportTaskService;
    private final AtomicLong sequence = new AtomicLong(800000);

    public WaybillApplicationService(WaybillMapper mapper, TransportTaskApplicationService transportTaskService) {
        this.mapper = mapper;
        this.transportTaskService = transportTaskService;
    }

    @Transactional
    public WaybillMapper.WaybillRow createFromTask(String taskNo, CreateCommand command) {
        WaybillMapper.WaybillRow existing = mapper.findActiveWaybillByTask(taskNo);
        if (existing != null) {
            return existing;
        }
        TransportTaskMapper.TaskRow task = transportTaskService.get(taskNo);
        if (task == null) {
            throw new IllegalArgumentException("transport task not found");
        }
        if (task.status() != TransportTaskAggregate.ACCEPTED) {
            throw new IllegalStateException("transport task must be accepted before waybill creation");
        }
        String waybillNo = "WB" + sequence.incrementAndGet();
        WaybillAggregate aggregate = WaybillAggregate.create(waybillNo, taskNo, command.carrierCode(),
                command.carrierName(), command.carrierWaybillNo(), command.logisticsProductCode(),
                command.receiptPayload());
        WaybillMapper.WaybillRow row = toRow(aggregate);
        mapper.insertWaybill(row);
        saveEvents(aggregate.pullEvents());
        log("CREATE_WAYBILL", waybillNo, command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public WaybillMapper.WaybillRow voidWaybill(String waybillNo, VoidCommand command) {
        WaybillAggregate aggregate = load(waybillNo);
        aggregate.voidWaybill(command.reason(), command.approvalNo(), command.expectedVersion());
        WaybillMapper.WaybillRow row = toRow(aggregate);
        mapper.updateWaybill(row);
        saveEvents(aggregate.pullEvents());
        log("VOID_WAYBILL", waybillNo, command.operatorId(), command.idempotencyKey());
        return mapper.findWaybill(waybillNo);
    }

    public WaybillMapper.WaybillRow get(String waybillNo) {
        return mapper.findWaybill(waybillNo);
    }

    public List<WaybillMapper.WaybillRow> list() {
        return mapper.listWaybills();
    }

    public List<TransportTaskMapper.OutboxRow> listOutbox() {
        return mapper.listOutbox();
    }

    public List<TransportTaskMapper.OperationLogRow> listOperationLogs() {
        return mapper.listOperationLogs();
    }

    private WaybillAggregate load(String waybillNo) {
        WaybillMapper.WaybillRow row = mapper.findWaybill(waybillNo);
        if (row == null) {
            throw new IllegalArgumentException("waybill not found");
        }
        return WaybillAggregate.restore(row.waybillNo(), row.taskNo(), row.carrierCode(), row.carrierName(),
                row.carrierWaybillNo(), row.logisticsProductCode(), row.receiptPayload(), row.status(),
                row.voidReason(), row.approvalNo(), row.version());
    }

    private WaybillMapper.WaybillRow toRow(WaybillAggregate aggregate) {
        return new WaybillMapper.WaybillRow(null, aggregate.waybillNo(), aggregate.taskNo(),
                aggregate.carrierCode(), aggregate.carrierName(), aggregate.carrierWaybillNo(),
                aggregate.logisticsProductCode(), aggregate.receiptPayload(), aggregate.status(),
                aggregate.voidReason(), aggregate.approvalNo(), aggregate.version());
    }

    private void saveEvents(List<TmsEvent> events) {
        for (TmsEvent event : events) {
            mapper.insertOutbox(new TransportTaskMapper.OutboxRow(event.eventType(), event.businessNo(),
                    event.payload(), 1, event.occurredAt()));
        }
    }

    private void log(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
        mapper.insertOperationLog(new TransportTaskMapper.OperationLogRow(operationType, businessNo, operatorId,
                idempotencyKey, LocalDateTime.now()));
    }

    public record CreateCommand(String carrierCode, String carrierName, String carrierWaybillNo,
                                String logisticsProductCode, String receiptPayload, Long operatorId,
                                String idempotencyKey) {}

    public record VoidCommand(String reason, String approvalNo, long expectedVersion, Long operatorId,
                              String idempotencyKey) {}
}
