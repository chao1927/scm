package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.domain.ShippingLabelAggregate;
import com.chaobo.scm.tms.domain.TmsEvent;
import com.chaobo.scm.tms.domain.WaybillAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import com.chaobo.scm.tms.infrastructure.persistence.WaybillMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ShippingLabelApplicationService {
    private final WaybillMapper mapper;
    private final WaybillApplicationService waybillService;
    private final AtomicLong sequence = new AtomicLong(900000);

    public ShippingLabelApplicationService(WaybillMapper mapper, WaybillApplicationService waybillService) {
        this.mapper = mapper;
        this.waybillService = waybillService;
    }

    @Transactional
    public WaybillMapper.LabelRow generate(String waybillNo, GenerateCommand command) {
        WaybillMapper.LabelRow existing = mapper.findActiveLabel(waybillNo, command.packageNo());
        if (existing != null) {
            return existing;
        }
        WaybillMapper.WaybillRow waybill = waybillService.get(waybillNo);
        if (waybill == null) {
            throw new IllegalArgumentException("waybill not found");
        }
        if (waybill.status() != WaybillAggregate.CREATED) {
            throw new IllegalStateException("shipping label requires active waybill");
        }
        String labelNo = "LBL" + sequence.incrementAndGet();
        ShippingLabelAggregate aggregate = ShippingLabelAggregate.generate(labelNo, waybillNo, command.packageNo(),
                command.templateVersion(), command.labelUrl());
        WaybillMapper.LabelRow row = toRow(aggregate);
        mapper.insertLabel(row);
        saveEvents(aggregate.pullEvents());
        log("GENERATE_SHIPPING_LABEL", labelNo, command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public WaybillMapper.LabelRow print(String labelNo, PrintCommand command) {
        ShippingLabelAggregate aggregate = load(labelNo);
        aggregate.print(command.deviceNo());
        WaybillMapper.LabelRow row = toRow(aggregate);
        mapper.updateLabel(row);
        saveEvents(aggregate.pullEvents());
        log("PRINT_SHIPPING_LABEL", labelNo, command.operatorId(), command.idempotencyKey());
        return mapper.findLabel(labelNo);
    }

    public List<WaybillMapper.LabelRow> listByWaybill(String waybillNo) {
        return mapper.listLabelsByWaybill(waybillNo);
    }

    private ShippingLabelAggregate load(String labelNo) {
        WaybillMapper.LabelRow row = mapper.findLabel(labelNo);
        if (row == null) {
            throw new IllegalArgumentException("shipping label not found");
        }
        return ShippingLabelAggregate.restore(row.labelNo(), row.waybillNo(), row.packageNo(),
                row.templateVersion(), row.labelUrl(), row.status(), row.printCount(), row.lastPrintDevice(),
                row.voidReason(), row.version());
    }

    private WaybillMapper.LabelRow toRow(ShippingLabelAggregate aggregate) {
        return new WaybillMapper.LabelRow(null, aggregate.labelNo(), aggregate.waybillNo(), aggregate.packageNo(),
                aggregate.templateVersion(), aggregate.labelUrl(), aggregate.status(), aggregate.printCount(),
                aggregate.lastPrintDevice(), aggregate.voidReason(), aggregate.version());
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

    public record GenerateCommand(String packageNo, String templateVersion, String labelUrl, Long operatorId,
                                  String idempotencyKey) {}

    public record PrintCommand(String deviceNo, Long operatorId, String idempotencyKey) {}
}
