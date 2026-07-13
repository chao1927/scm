package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.domain.LogisticsFeeSourceAggregate;
import com.chaobo.scm.tms.domain.TmsEvent;
import com.chaobo.scm.tms.infrastructure.persistence.LogisticsSettlementMapper;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import com.chaobo.scm.tms.infrastructure.persistence.WaybillMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LogisticsFeeSourceApplicationService {
    private final LogisticsSettlementMapper mapper;
    private final WaybillApplicationService waybillService;
    private final AtomicLong sequence = new AtomicLong(130000);

    public LogisticsFeeSourceApplicationService(LogisticsSettlementMapper mapper,
                                                WaybillApplicationService waybillService) {
        this.mapper = mapper;
        this.waybillService = waybillService;
    }

    @Transactional
    public LogisticsSettlementMapper.FeeSourceRow generate(String waybillNo, GenerateCommand command) {
        LogisticsSettlementMapper.FeeSourceRow existing = mapper.findFeeSourceByWaybillAndItem(waybillNo,
                command.feeItemCode());
        if (existing != null) {
            return existing;
        }
        WaybillMapper.WaybillRow waybill = waybillService.get(waybillNo);
        if (waybill == null) {
            throw new IllegalArgumentException("waybill not found");
        }
        LogisticsFeeSourceAggregate aggregate = LogisticsFeeSourceAggregate.generate("FEE" + sequence.incrementAndGet(),
                waybillNo, waybill.carrierCode(), waybill.logisticsProductCode(), command.feeItemCode(),
                command.amount(), command.currency(), command.billingPeriod(), command.responsibleParty());
        LogisticsSettlementMapper.FeeSourceRow row = toRow(aggregate);
        mapper.insertFeeSource(row);
        saveEvents(aggregate.pullEvents());
        log("GENERATE_LOGISTICS_FEE_SOURCE", row.feeSourceNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public LogisticsSettlementMapper.FeeSourceRow pushBms(String feeSourceNo, PushCommand command) {
        LogisticsFeeSourceAggregate aggregate = load(feeSourceNo);
        aggregate.pushToBms(command.bmsReceiveNo());
        LogisticsSettlementMapper.FeeSourceRow row = toRow(aggregate);
        mapper.updateFeeSource(row);
        saveEvents(aggregate.pullEvents());
        log("PUSH_LOGISTICS_FEE_SOURCE", feeSourceNo, command.operatorId(), command.idempotencyKey());
        return mapper.findFeeSource(feeSourceNo);
    }

    public List<LogisticsSettlementMapper.FeeSourceRow> list() {
        return mapper.listFeeSources();
    }

    private LogisticsFeeSourceAggregate load(String feeSourceNo) {
        LogisticsSettlementMapper.FeeSourceRow row = mapper.findFeeSource(feeSourceNo);
        if (row == null) {
            throw new IllegalArgumentException("logistics fee source not found");
        }
        return LogisticsFeeSourceAggregate.restore(row.feeSourceNo(), row.waybillNo(), row.carrierCode(),
                row.logisticsProductCode(), row.feeItemCode(), row.amount(), row.currency(), row.billingPeriod(),
                row.responsibleParty(), row.pushStatus(), row.bmsReceiveNo(), row.failureReason(), row.version());
    }

    private LogisticsSettlementMapper.FeeSourceRow toRow(LogisticsFeeSourceAggregate aggregate) {
        return new LogisticsSettlementMapper.FeeSourceRow(null, aggregate.feeSourceNo(), aggregate.waybillNo(),
                aggregate.carrierCode(), aggregate.logisticsProductCode(), aggregate.feeItemCode(),
                aggregate.amount(), aggregate.currency(), aggregate.billingPeriod(), aggregate.responsibleParty(),
                aggregate.pushStatus(), aggregate.bmsReceiveNo(), aggregate.failureReason(), aggregate.version());
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

    public record GenerateCommand(String feeItemCode, BigDecimal amount, String currency, String billingPeriod,
                                  String responsibleParty, Long operatorId, String idempotencyKey) {}

    public record PushCommand(String bmsReceiveNo, Long operatorId, String idempotencyKey) {}
}
