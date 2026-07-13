package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.domain.TmsEvent;
import com.chaobo.scm.tms.domain.TransportTaskAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class TransportTaskApplicationService {
    private final TransportTaskMapper mapper;
    private final AtomicLong sequence = new AtomicLong(700000);

    public TransportTaskApplicationService(TransportTaskMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional
    public TransportTaskMapper.TaskRow createFromSource(CreateCommand command) {
        TransportTaskMapper.TaskRow existing = mapper.findActiveBySource(command.sourceSystem(),
                command.sourceOrderNo(), command.scenario());
        if (existing != null) {
            return existing;
        }
        String taskNo = "TMS" + sequence.incrementAndGet();
        TransportTaskAggregate aggregate = TransportTaskAggregate.create(taskNo, command.sourceSystem(),
                command.sourceOrderNo(), command.sourceLineNo(), command.scenario(), command.shipperId(),
                command.warehouseId(), command.originAddress(), command.destinationAddress(), command.packages(),
                command.logisticsProductCode(), command.feeResponsibility());
        TransportTaskMapper.TaskRow row = toRow(aggregate);
        mapper.insertTask(row);
        saveEvents(aggregate.pullEvents());
        log("CREATE_TRANSPORT_TASK", taskNo, command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public TransportTaskMapper.TaskRow accept(String taskNo, AcceptCommand command) {
        TransportTaskAggregate aggregate = load(taskNo);
        aggregate.accept(command.carrierCode(), command.carrierName(), command.logisticsProductCode(),
                command.expectedVersion());
        TransportTaskMapper.TaskRow row = toRow(aggregate);
        mapper.updateTask(row);
        saveEvents(aggregate.pullEvents());
        log("ACCEPT_TRANSPORT_TASK", taskNo, command.operatorId(), command.idempotencyKey());
        return mapper.findTask(taskNo);
    }

    public TransportTaskMapper.TaskRow get(String taskNo) {
        return mapper.findTask(taskNo);
    }

    public List<TransportTaskMapper.TaskRow> list(Query query) {
        int pageNo = query.pageNo() == null || query.pageNo() < 1 ? 1 : query.pageNo();
        int pageSize = query.pageSize() == null ? 20 : query.pageSize();
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("page size must be between 1 and 100");
        }
        return mapper.listTasks(emptyToNull(query.sourceSystem()), emptyToNull(query.scenario()), query.status(),
                query.warehouseId(), emptyToNull(query.carrierCode()), pageSize, (pageNo - 1) * pageSize);
    }

    public List<TransportTaskMapper.OutboxRow> listOutbox() {
        return mapper.listOutbox();
    }

    public List<TransportTaskMapper.OperationLogRow> listOperationLogs() {
        return mapper.listOperationLogs();
    }

    private TransportTaskAggregate load(String taskNo) {
        TransportTaskMapper.TaskRow row = mapper.findTask(taskNo);
        if (row == null) {
            throw new IllegalArgumentException("transport task not found");
        }
        return fromRow(row);
    }

    private TransportTaskAggregate fromRow(TransportTaskMapper.TaskRow row) {
        return TransportTaskAggregate.restore(row.taskNo(), row.sourceSystem(), row.sourceOrderNo(),
                row.sourceLineNo(), row.scenario(), row.shipperId(), row.warehouseId(),
                parseAddress(row.originAddress()), parseAddress(row.destinationAddress()),
                parsePackages(row.packagePayload()), row.status(), row.carrierCode(), row.carrierName(),
                row.logisticsProductCode(), row.feeResponsibility(), row.version());
    }

    private TransportTaskMapper.TaskRow toRow(TransportTaskAggregate aggregate) {
        return new TransportTaskMapper.TaskRow(null, aggregate.taskNo(), aggregate.sourceSystem(),
                aggregate.sourceOrderNo(), aggregate.sourceLineNo(), aggregate.scenario(), aggregate.shipperId(),
                aggregate.warehouseId(), formatAddress(aggregate.originAddress()),
                formatAddress(aggregate.destinationAddress()), formatPackages(aggregate.packages()),
                aggregate.status(), aggregate.carrierCode(), aggregate.carrierName(),
                aggregate.logisticsProductCode(), aggregate.feeResponsibility(), aggregate.version());
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

    public static String formatAddress(TransportTaskAggregate.Address address) {
        return String.join("|", address.province(), address.city(), blankAsEmpty(address.district()),
                address.detail(), address.contactName(), address.contactPhone());
    }

    public static TransportTaskAggregate.Address parseAddress(String payload) {
        String[] parts = payload.split("\\|", -1);
        if (parts.length != 6) {
            throw new IllegalArgumentException("invalid address payload");
        }
        return new TransportTaskAggregate.Address(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
    }

    public static String formatPackages(List<TransportTaskAggregate.PackageItem> packages) {
        return packages.stream()
                .map(item -> String.join(":", item.packageNo(), item.quantity().toPlainString(),
                        decimalToString(item.weightKg()), decimalToString(item.volumeCbm())))
                .collect(Collectors.joining(";"));
    }

    public static List<TransportTaskAggregate.PackageItem> parsePackages(String payload) {
        if (payload == null || payload.isBlank()) {
            return List.of();
        }
        return List.of(payload.split(";")).stream().map(item -> {
            String[] parts = item.split(":", -1);
            if (parts.length != 4) {
                throw new IllegalArgumentException("invalid package payload");
            }
            return new TransportTaskAggregate.PackageItem(parts[0], new BigDecimal(parts[1]),
                    stringToDecimal(parts[2]), stringToDecimal(parts[3]));
        }).toList();
    }

    private static String decimalToString(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    private static BigDecimal stringToDecimal(String value) {
        return value == null || value.isBlank() ? null : new BigDecimal(value);
    }

    private static String blankAsEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    public record CreateCommand(String sourceSystem, String sourceOrderNo, String sourceLineNo, String scenario,
                                Long shipperId, Long warehouseId, TransportTaskAggregate.Address originAddress,
                                TransportTaskAggregate.Address destinationAddress,
                                List<TransportTaskAggregate.PackageItem> packages,
                                String logisticsProductCode, String feeResponsibility, Long operatorId,
                                String idempotencyKey) {}

    public record AcceptCommand(String carrierCode, String carrierName, String logisticsProductCode,
                                long expectedVersion, Long operatorId, String idempotencyKey) {}

    public record Query(String sourceSystem, String scenario, Integer status, Long warehouseId, String carrierCode,
                        Integer pageNo, Integer pageSize) {}
}
