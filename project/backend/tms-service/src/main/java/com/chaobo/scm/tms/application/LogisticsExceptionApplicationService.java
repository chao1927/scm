package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.domain.LogisticsExceptionAggregate;
import com.chaobo.scm.tms.domain.TmsEvent;
import com.chaobo.scm.tms.infrastructure.persistence.LogisticsSettlementMapper;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LogisticsExceptionApplicationService {
    private final LogisticsSettlementMapper mapper;
    private final WaybillApplicationService waybillService;
    private final AtomicLong sequence = new AtomicLong(120000);

    public LogisticsExceptionApplicationService(LogisticsSettlementMapper mapper,
                                                WaybillApplicationService waybillService) {
        this.mapper = mapper;
        this.waybillService = waybillService;
    }

    @Transactional
    public LogisticsSettlementMapper.ExceptionRow register(RegisterCommand command) {
        if (waybillService.get(command.waybillNo()) == null) {
            throw new IllegalArgumentException("waybill not found");
        }
        LogisticsExceptionAggregate aggregate = LogisticsExceptionAggregate.register("EXC" + sequence.incrementAndGet(),
                command.waybillNo(), command.exceptionType(), command.level(), command.description(),
                command.responsibleParty());
        LogisticsSettlementMapper.ExceptionRow row = toRow(aggregate);
        mapper.insertException(row);
        saveEvents(aggregate.pullEvents());
        log("REGISTER_LOGISTICS_EXCEPTION", row.exceptionNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public LogisticsSettlementMapper.ExceptionRow close(String exceptionNo, CloseCommand command) {
        LogisticsExceptionAggregate aggregate = load(exceptionNo);
        aggregate.close(command.closeResult(), command.responsibleParty(), command.expectedVersion());
        LogisticsSettlementMapper.ExceptionRow row = toRow(aggregate);
        mapper.updateException(row);
        saveEvents(aggregate.pullEvents());
        log("CLOSE_LOGISTICS_EXCEPTION", exceptionNo, command.operatorId(), command.idempotencyKey());
        return mapper.findException(exceptionNo);
    }

    public List<LogisticsSettlementMapper.ExceptionRow> list() {
        return mapper.listExceptions();
    }

    private LogisticsExceptionAggregate load(String exceptionNo) {
        LogisticsSettlementMapper.ExceptionRow row = mapper.findException(exceptionNo);
        if (row == null) {
            throw new IllegalArgumentException("logistics exception not found");
        }
        return LogisticsExceptionAggregate.restore(row.exceptionNo(), row.waybillNo(), row.exceptionType(),
                row.level(), row.description(), row.responsibleParty(), row.status(), row.closeResult(),
                row.version());
    }

    private LogisticsSettlementMapper.ExceptionRow toRow(LogisticsExceptionAggregate aggregate) {
        return new LogisticsSettlementMapper.ExceptionRow(null, aggregate.exceptionNo(), aggregate.waybillNo(),
                aggregate.exceptionType(), aggregate.level(), aggregate.description(), aggregate.responsibleParty(),
                aggregate.status(), aggregate.closeResult(), aggregate.version());
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

    public record RegisterCommand(String waybillNo, String exceptionType, String level, String description,
                                  String responsibleParty, Long operatorId, String idempotencyKey) {}

    public record CloseCommand(String closeResult, String responsibleParty, long expectedVersion, Long operatorId,
                               String idempotencyKey) {}
}
