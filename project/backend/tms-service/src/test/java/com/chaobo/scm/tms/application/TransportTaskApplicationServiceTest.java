package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.domain.TransportTaskAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TransportTaskApplicationServiceTest {
    @Test
    void createAcceptAndQueryTransportTask() {
        MemoryTransportTaskMapper mapper = new MemoryTransportTaskMapper();
        TransportTaskApplicationService service = new TransportTaskApplicationService(mapper);

        TransportTaskMapper.TaskRow created = service.createFromSource(createCommand("idem-1"));
        TransportTaskMapper.TaskRow repeated = service.createFromSource(createCommand("idem-repeat"));
        TransportTaskMapper.TaskRow accepted = service.accept(created.taskNo(), new TransportTaskApplicationService.AcceptCommand(
                "SF", "顺丰", "SF-EXPRESS", created.version(), 1001L, "idem-2"));

        assertThat(repeated.taskNo()).isEqualTo(created.taskNo());
        assertThat(accepted.status()).isEqualTo(TransportTaskAggregate.ACCEPTED);
        assertThat(service.list(new TransportTaskApplicationService.Query("OMS", "SALES_OUTBOUND",
                TransportTaskAggregate.ACCEPTED, 2L, "SF", 1, 20))).hasSize(1);
        assertThat(service.listOutbox()).extracting(TransportTaskMapper.OutboxRow::eventType)
                .containsExactly("TransportTaskCreated", "TransportTaskAccepted");
        assertThat(service.listOperationLogs()).extracting(TransportTaskMapper.OperationLogRow::operationType)
                .contains("CREATE_TRANSPORT_TASK", "ACCEPT_TRANSPORT_TASK");
    }

    @Test
    void rejectInvalidPageSize() {
        TransportTaskApplicationService service = new TransportTaskApplicationService(new MemoryTransportTaskMapper());

        assertThatThrownBy(() -> service.list(new TransportTaskApplicationService.Query(null, null, null,
                null, null, 1, 101)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page size");
    }

    public static TransportTaskApplicationService.CreateCommand createCommand(String idempotencyKey) {
        return new TransportTaskApplicationService.CreateCommand("OMS", "SO1", null, "SALES_OUTBOUND",
                1L, 2L, TransportTaskAggregateTestFixtures.address(),
                TransportTaskAggregateTestFixtures.address(), TransportTaskAggregateTestFixtures.packages(),
                "SF-EXPRESS", "SHIPPER", 1001L, idempotencyKey);
    }

    public static class MemoryTransportTaskMapper implements TransportTaskMapper {
        final Map<String, TaskRow> tasks = new LinkedHashMap<>();
        final List<OutboxRow> outbox = new ArrayList<>();
        final List<OperationLogRow> logs = new ArrayList<>();

        @Override
        public TaskRow findTask(String taskNo) { return tasks.get(taskNo); }

        @Override
        public TaskRow findActiveBySource(String sourceSystem, String sourceOrderNo, String scenario) {
            return tasks.values().stream()
                    .filter(row -> row.sourceSystem().equals(sourceSystem))
                    .filter(row -> row.sourceOrderNo().equals(sourceOrderNo))
                    .filter(row -> row.scenario().equals(scenario))
                    .filter(row -> row.status() != TransportTaskAggregate.CANCELLED)
                    .findFirst().orElse(null);
        }

        @Override
        public List<TaskRow> listTasks(String sourceSystem, String scenario, Integer status, Long warehouseId,
                                       String carrierCode, int limit, int offset) {
            return tasks.values().stream()
                    .filter(row -> sourceSystem == null || row.sourceSystem().equals(sourceSystem))
                    .filter(row -> scenario == null || row.scenario().equals(scenario))
                    .filter(row -> status == null || row.status() == status)
                    .filter(row -> warehouseId == null || row.warehouseId().equals(warehouseId))
                    .filter(row -> carrierCode == null || carrierCode.equals(row.carrierCode()))
                    .skip(offset)
                    .limit(limit)
                    .toList();
        }

        @Override
        public void insertTask(TaskRow row) { tasks.put(row.taskNo(), row); }

        @Override
        public void updateTask(TaskRow row) { tasks.put(row.taskNo(), row); }

        @Override
        public void insertOutbox(OutboxRow row) { outbox.add(row); }

        @Override
        public List<OutboxRow> listOutbox() { return outbox; }

        @Override
        public void insertOperationLog(OperationLogRow row) {
            logs.add(new OperationLogRow(row.operationType(), row.businessNo(), row.operatorId(),
                    row.idempotencyKey(), LocalDateTime.now()));
        }

        @Override
        public List<OperationLogRow> listOperationLogs() { return logs; }
    }
}
