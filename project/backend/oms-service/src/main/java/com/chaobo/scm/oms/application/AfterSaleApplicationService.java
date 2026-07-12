package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.domain.AfterSaleAggregate;
import com.chaobo.scm.oms.domain.OmsEvent;
import com.chaobo.scm.oms.infrastructure.persistence.CancellationMapper;
import com.chaobo.scm.oms.infrastructure.persistence.OmsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AfterSaleApplicationService {
    private final CancellationMapper mapper;
    private final OmsMapper omsMapper;
    private final AtomicLong sequence = new AtomicLong(600000);

    public AfterSaleApplicationService(CancellationMapper mapper, OmsMapper omsMapper) {
        this.mapper = mapper;
        this.omsMapper = omsMapper;
    }

    @Transactional
    public CancellationMapper.AfterSaleRow create(CreateCommand command) {
        OmsMapper.SalesOrderRow order = omsMapper.findOrder(command.salesOrderNo());
        if (order == null) {
            throw new IllegalArgumentException("sales order not found");
        }
        if (command.refundAmount() == null || command.refundAmount().compareTo(order.totalAmount()) > 0) {
            throw new IllegalArgumentException("refund amount exceeds order amount");
        }
        CancellationMapper.AfterSaleRow existing = mapper.findAfterSaleByOrder(command.salesOrderNo());
        if (existing != null) {
            return existing;
        }
        String afterSaleNo = "AS" + sequence.incrementAndGet();
        AfterSaleAggregate aggregate = AfterSaleAggregate.create(afterSaleNo, command.salesOrderNo(),
                command.fulfillmentNo(), command.refundAmount(), command.reason());
        mapper.insertAfterSale(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("CREATE_AFTER_SALE", afterSaleNo, command.operatorId(), command.idempotencyKey());
        return mapper.findAfterSale(afterSaleNo);
    }

    @Transactional
    public CancellationMapper.AfterSaleRow approve(String afterSaleNo, ApproveCommand command) {
        AfterSaleAggregate aggregate = load(afterSaleNo);
        aggregate.approve(command.remark());
        mapper.updateAfterSale(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("APPROVE_AFTER_SALE", afterSaleNo, command.operatorId(), command.idempotencyKey());
        return mapper.findAfterSale(afterSaleNo);
    }

    @Transactional
    public CancellationMapper.AfterSaleRow requestRefund(String afterSaleNo, RefundCommand command) {
        AfterSaleAggregate aggregate = load(afterSaleNo);
        aggregate.requestRefund();
        mapper.updateAfterSale(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        mapper.insertIntegrationCommand(new CancellationMapper.IntegrationCommandRow("RequestRefund", "BMS",
                afterSaleNo, afterSaleNo + ":REFUND:" + aggregate.version(), aggregate.refundAmount().toPlainString()));
        log("REQUEST_REFUND", afterSaleNo, command.operatorId(), command.idempotencyKey());
        return mapper.findAfterSale(afterSaleNo);
    }

    @Transactional
    public void consumeEvent(RefundEvent event) {
        int claimed = mapper.claimEvent(new CancellationMapper.EventInboxRow(event.eventId(), event.eventType(),
                event.businessNo(), event.payload(), 1, null));
        if (claimed == 0) {
            return;
        }
        try {
            AfterSaleAggregate aggregate = load(event.afterSaleNo());
            aggregate.markRefunded(event.refundedAmount());
            mapper.updateAfterSale(toRow(aggregate));
            saveEvents(aggregate.pullEvents());
            mapper.updateEvent(new CancellationMapper.EventInboxRow(event.eventId(), event.eventType(),
                    event.businessNo(), event.payload(), 2, null));
        } catch (RuntimeException exception) {
            mapper.updateEvent(new CancellationMapper.EventInboxRow(event.eventId(), event.eventType(),
                    event.businessNo(), event.payload(), 3, exception.getMessage()));
            throw exception;
        }
    }

    @Transactional
    public CancellationMapper.AfterSaleRow complete(String afterSaleNo, CompleteCommand command) {
        AfterSaleAggregate aggregate = load(afterSaleNo);
        aggregate.complete();
        mapper.updateAfterSale(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("COMPLETE_AFTER_SALE", afterSaleNo, command.operatorId(), command.idempotencyKey());
        return mapper.findAfterSale(afterSaleNo);
    }

    public CancellationMapper.AfterSaleRow get(String afterSaleNo) { return mapper.findAfterSale(afterSaleNo); }

    private AfterSaleAggregate load(String afterSaleNo) {
        CancellationMapper.AfterSaleRow row = mapper.findAfterSale(afterSaleNo);
        if (row == null) {
            throw new IllegalArgumentException("after-sale not found");
        }
        return AfterSaleAggregate.restore(row.afterSaleNo(), row.salesOrderNo(), row.fulfillmentNo(),
                row.refundAmount(), row.reason(), row.status(), row.refundedAmount(), row.version());
    }

    private CancellationMapper.AfterSaleRow toRow(AfterSaleAggregate aggregate) {
        return new CancellationMapper.AfterSaleRow(aggregate.afterSaleNo(), aggregate.salesOrderNo(),
                aggregate.fulfillmentNo(), aggregate.refundAmount(), aggregate.refundedAmount(), aggregate.reason(),
                aggregate.status(), aggregate.version());
    }

    private void saveEvents(List<OmsEvent> events) {
        for (OmsEvent event : events) {
            mapper.insertOutbox(new CancellationMapper.OutboxRow(event.eventType(), event.businessNo(),
                    event.payload(), event.occurredAt()));
        }
    }

    private void log(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
        mapper.insertOperationLog(new CancellationMapper.OperationLogRow(operationType, businessNo, operatorId,
                idempotencyKey));
    }

    public record CreateCommand(String salesOrderNo, String fulfillmentNo, BigDecimal refundAmount,
                                String reason, Long operatorId, String idempotencyKey) {}
    public record ApproveCommand(String remark, Long operatorId, String idempotencyKey) {}
    public record RefundCommand(Long operatorId, String idempotencyKey) {}
    public record CompleteCommand(Long operatorId, String idempotencyKey) {}
    public record RefundEvent(String eventId, String eventType, String businessNo, String afterSaleNo,
                              BigDecimal refundedAmount, String payload) {}
}
