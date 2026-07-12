package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.domain.OmsEvent;
import com.chaobo.scm.oms.domain.SalesOrderAggregate;
import com.chaobo.scm.oms.infrastructure.persistence.OmsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class OmsApplicationService {
    private final OmsMapper mapper;
    private final AtomicLong orderSequence = new AtomicLong(100000);

    public OmsApplicationService(OmsMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional
    public OmsMapper.SalesOrderRow receiveChannelOrder(ReceiveChannelOrder command) {
        OmsMapper.SalesOrderRow existing = mapper.findByChannelOrder(command.channelCode(), command.channelOrderNo());
        if (existing != null) {
            return existing;
        }
        String orderNo = "SO" + orderSequence.incrementAndGet();
        SalesOrderAggregate aggregate = SalesOrderAggregate.create(orderNo, command.channelCode(), command.channelOrderNo(),
                command.customerId(), command.receiverAddress(), command.lines());
        OmsMapper.SalesOrderRow row = toRow(aggregate);
        mapper.insertOrder(row);
        mapper.insertChannelOrder(new OmsMapper.ChannelOrderRow(command.channelCode(), command.channelOrderNo(), orderNo, command.rawPayload(), LocalDateTime.now()));
        saveEvents(aggregate.pullEvents());
        log("RECEIVE_CHANNEL_ORDER", orderNo, command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public OmsMapper.SalesOrderRow reviewSalesOrder(String orderNo, ReviewCommand command) {
        SalesOrderAggregate aggregate = loadOrder(orderNo);
        if (command.approved()) {
            aggregate.approve(command.remark());
            log("APPROVE_SALES_ORDER", orderNo, command.operatorId(), command.idempotencyKey());
        } else {
            aggregate.intercept(command.remark());
            log("INTERCEPT_SALES_ORDER", orderNo, command.operatorId(), command.idempotencyKey());
        }
        OmsMapper.SalesOrderRow row = toRow(aggregate);
        mapper.updateOrder(row);
        saveEvents(aggregate.pullEvents());
        return row;
    }

    public List<OmsMapper.ChannelOrderRow> listChannelOrders() { return mapper.listChannelOrders(); }
    public List<OmsMapper.SalesOrderRow> listOrders() { return mapper.listOrders(); }
    public OmsMapper.SalesOrderRow getOrder(String orderNo) { return mapper.findOrder(orderNo); }
    public List<OmsMapper.OutboxRow> listOutbox() { return mapper.listOutbox(); }
    public List<OmsMapper.OperationLogRow> listOperationLogs() { return mapper.listOperationLogs(); }

    private SalesOrderAggregate loadOrder(String orderNo) {
        OmsMapper.SalesOrderRow row = mapper.findOrder(orderNo);
        if (row == null) {
            throw new IllegalArgumentException("sales order not found");
        }
        return SalesOrderAggregate.restore(row.orderNo(), row.channelCode(), row.channelOrderNo(), row.customerId(),
                row.receiverAddress(), parseLines(row.linePayload()), row.totalAmount(), row.status(), row.reviewRemark(), row.version());
    }

    private OmsMapper.SalesOrderRow toRow(SalesOrderAggregate aggregate) {
        return new OmsMapper.SalesOrderRow(null, aggregate.orderNo(), aggregate.channelCode(), aggregate.channelOrderNo(),
                aggregate.customerId(), aggregate.receiverAddress(), formatLines(aggregate.lines()), aggregate.totalAmount(),
                aggregate.status(), aggregate.reviewRemark(), aggregate.version());
    }

    private void saveEvents(List<OmsEvent> events) {
        for (OmsEvent event : events) {
            mapper.insertOutbox(new OmsMapper.OutboxRow(event.eventType(), event.businessNo(), event.payload(), 1, event.occurredAt()));
        }
    }

    private void log(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
        mapper.insertOperationLog(new OmsMapper.OperationLogRow(operationType, businessNo, operatorId, idempotencyKey, LocalDateTime.now()));
    }

    public static String formatLines(List<SalesOrderAggregate.OrderLine> lines) {
        return lines.stream()
                .map(line -> String.join(":", line.skuCode(), Integer.toString(line.quantity()), line.unitPrice().toPlainString()))
                .collect(Collectors.joining(";"));
    }

    public static List<SalesOrderAggregate.OrderLine> parseLines(String payload) {
        if (payload == null || payload.isBlank()) {
            return List.of();
        }
        return List.of(payload.split(";")).stream().map(item -> {
            String[] parts = item.split(":");
            if (parts.length != 3) {
                throw new IllegalArgumentException("invalid line payload");
            }
            return new SalesOrderAggregate.OrderLine(parts[0], Integer.parseInt(parts[1]), new BigDecimal(parts[2]));
        }).toList();
    }

    public record ReceiveChannelOrder(String channelCode, String channelOrderNo, Long customerId, String receiverAddress,
                                      List<SalesOrderAggregate.OrderLine> lines, String rawPayload, Long operatorId,
                                      String idempotencyKey) {}
    public record ReviewCommand(boolean approved, String remark, Long operatorId, String idempotencyKey) {}
}
