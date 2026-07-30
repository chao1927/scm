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

/**
 * TransportTaskApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class TransportTaskApplicationServiceTest {

    /**
     * 执行命令 {@code createAcceptAndQueryTransportTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void createAcceptAndQueryTransportTask() {
        MemoryTransportTaskMapper mapper = new MemoryTransportTaskMapper();
        TransportTaskApplicationService service = new TransportTaskApplicationService(mapper);
        TransportTaskMapper.TaskRow created = service.createFromSource(createCommand("idem-1"));
        TransportTaskMapper.TaskRow repeated = service.createFromSource(createCommand("idem-repeat"));
        TransportTaskMapper.TaskRow accepted = service.accept(created.taskNo(), new TransportTaskApplicationService.AcceptCommand("SF", "顺丰", "SF-EXPRESS", created.version(), 1001L, "idem-2"));
        TransportTaskMapper.TaskRow started = service.start(created.taskNo(), new TransportTaskApplicationService.ChangeCommand(accepted.version(), 1001L, "idem-3"));
        TransportTaskMapper.TaskRow delivered = service.deliver(created.taskNo(), new TransportTaskApplicationService.ChangeCommand(started.version(), 1001L, "idem-4"));
        assertThat(repeated.taskNo()).isEqualTo(created.taskNo());
        assertThat(accepted.status()).isEqualTo(TransportTaskAggregate.ACCEPTED);
        assertThat(delivered.status()).isEqualTo(TransportTaskAggregate.DELIVERED);
        assertThat(service.list(new TransportTaskApplicationService.Query("OMS", "SALES_OUTBOUND", TransportTaskAggregate.DELIVERED, 2L, "SF", 1, 20))).hasSize(1);
        assertThat(service.listOutbox()).extracting(TransportTaskMapper.OutboxRow::eventType).containsExactly("TransportTaskCreated", "TransportTaskAccepted", "TransportStarted", "TransportDelivered");
        assertThat(service.listOperationLogs()).extracting(TransportTaskMapper.OperationLogRow::operationType).contains("CREATE_TRANSPORT_TASK", "ACCEPT_TRANSPORT_TASK");
    }

    /**
     * 执行命令 {@code rejectInvalidPageSize}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectInvalidPageSize() {
        TransportTaskApplicationService service = new TransportTaskApplicationService(new MemoryTransportTaskMapper());
        assertThatThrownBy(() -> service.list(new TransportTaskApplicationService.Query(null, null, null, null, null, 1, 101))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("page size");
    }

    /**
     * 执行命令 {@code createCommand}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code TransportTaskApplicationService.CreateCommand}
     */
    public static TransportTaskApplicationService.CreateCommand createCommand(String idempotencyKey) {
        return new TransportTaskApplicationService.CreateCommand("OMS", "SO1", null, "SALES_OUTBOUND", 1L, 2L, TransportTaskAggregateTestFixtures.address(), TransportTaskAggregateTestFixtures.address(), TransportTaskAggregateTestFixtures.packages(), "SF-EXPRESS", "SHIPPER", 1001L, idempotencyKey);
    }

    /**
     * MemoryTransportTaskMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public static class MemoryTransportTaskMapper implements TransportTaskMapper {

        /**
         * tasks（类型：{@code Map<String,TaskRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, TaskRow> tasks = new LinkedHashMap<>();

        /**
         * outbox（类型：{@code List<OutboxRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<OutboxRow> outbox = new ArrayList<>();

        /**
         * logs（类型：{@code List<OperationLogRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<OperationLogRow> logs = new ArrayList<>();

        /**
         * 查询并返回 {@code findTask}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param taskNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code TaskRow}
         */
        @Override
        public TaskRow findTask(String taskNo) {
            return tasks.get(taskNo);
        }

        /**
         * 查询并返回 {@code findActiveBySource}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
         * @param sourceOrderNo 可追踪业务编码，类型为 {@code String}
         * @param scenario 业务处理参数或成员，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code TaskRow}
         */
        @Override
        public TaskRow findActiveBySource(String sourceSystem, String sourceOrderNo, String scenario) {
            return tasks.values().stream().filter(row -> row.sourceSystem().equals(sourceSystem)).filter(row -> row.sourceOrderNo().equals(sourceOrderNo)).filter(row -> row.scenario().equals(scenario)).filter(row -> row.status() != TransportTaskAggregate.CANCELLED).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code listTasks}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
         * @param scenario 业务处理参数或成员，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code Integer}
         * @param warehouseId 业务或技术标识，类型为 {@code Long}
         * @param carrierCode 可追踪业务编码，类型为 {@code String}
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @param offset 业务处理参数或成员，类型为 {@code int}
         * @return 查询并返回的结果，类型为 {@code List<TaskRow>}
         */
        @Override
        public List<TaskRow> listTasks(String sourceSystem, String scenario, Integer status, Long warehouseId, String carrierCode, int limit, int offset) {
            return tasks.values().stream().filter(row -> sourceSystem == null || row.sourceSystem().equals(sourceSystem)).filter(row -> scenario == null || row.scenario().equals(scenario)).filter(row -> status == null || row.status() == status).filter(row -> warehouseId == null || row.warehouseId().equals(warehouseId)).filter(row -> carrierCode == null || carrierCode.equals(row.carrierCode())).skip(offset).limit(limit).toList();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertTask}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code TaskRow}
         */
        @Override
        public void insertTask(TaskRow row) {
            tasks.put(row.taskNo(), row);
        }

        /**
         * 执行命令 {@code updateTask}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code TaskRow}
         */
        @Override
        public void updateTask(TaskRow row) {
            tasks.put(row.taskNo(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOutbox}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code OutboxRow}
         */
        @Override
        public void insertOutbox(OutboxRow row) {
            outbox.add(row);
        }

        /**
         * 查询并返回 {@code listOutbox}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<OutboxRow>}
         */
        @Override
        public List<OutboxRow> listOutbox() {
            return outbox;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOperationLog}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code OperationLogRow}
         */
        @Override
        public void insertOperationLog(OperationLogRow row) {
            logs.add(new OperationLogRow(row.operationType(), row.businessNo(), row.operatorId(), row.idempotencyKey(), LocalDateTime.now()));
        }

        /**
         * 查询并返回 {@code listOperationLogs}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<OperationLogRow>}
         */
        @Override
        public List<OperationLogRow> listOperationLogs() {
            return logs;
        }
    }
}
