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

/**
 * WaybillApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class WaybillApplicationService {

    /**
     * mapper（类型：{@code WaybillMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final WaybillMapper mapper;

    /**
     * transportTaskService（类型：{@code TransportTaskApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final TransportTaskApplicationService transportTaskService;

    /**
     * sequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong sequence = new AtomicLong(800000);

    /**
     * 创建 WaybillApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code WaybillMapper}
     * @param transportTaskService 应用或外部协作依赖，类型为 {@code TransportTaskApplicationService}
     */
    public WaybillApplicationService(WaybillMapper mapper, TransportTaskApplicationService transportTaskService) {
        this.mapper = mapper;
        this.transportTaskService = transportTaskService;
    }

    /**
     * 执行命令 {@code createFromTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code CreateCommand}
     * @return 执行命令的结果，类型为 {@code WaybillMapper.WaybillRow}
     */
    @Transactional(rollbackFor = Exception.class)
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
        WaybillAggregate aggregate = WaybillAggregate.create(waybillNo, taskNo, command.carrierCode(), command.carrierName(), command.carrierWaybillNo(), command.logisticsProductCode(), command.receiptPayload());
        WaybillMapper.WaybillRow row = toRow(aggregate);
        mapper.insertWaybill(row);
        saveEvents(aggregate.pullEvents());
        log("CREATE_WAYBILL", waybillNo, command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 处理当前类型职责中的操作 {@code voidWaybill}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code VoidCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code WaybillMapper.WaybillRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public WaybillMapper.WaybillRow voidWaybill(String waybillNo, VoidCommand command) {
        WaybillAggregate aggregate = load(waybillNo);
        aggregate.voidWaybill(command.reason(), command.approvalNo(), command.expectedVersion());
        WaybillMapper.WaybillRow row = toRow(aggregate);
        mapper.updateWaybill(row);
        saveEvents(aggregate.pullEvents());
        log("VOID_WAYBILL", waybillNo, command.operatorId(), command.idempotencyKey());
        return mapper.findWaybill(waybillNo);
    }

    /**
     * 查询并返回 {@code get}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code WaybillMapper.WaybillRow}
     */
    public WaybillMapper.WaybillRow get(String waybillNo) {
        return mapper.findWaybill(waybillNo);
    }

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<WaybillMapper.WaybillRow>}
     */
    public List<WaybillMapper.WaybillRow> list() {
        return mapper.listWaybills();
    }

    /**
     * 查询并返回 {@code listOutbox}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<TransportTaskMapper.OutboxRow>}
     */
    public List<TransportTaskMapper.OutboxRow> listOutbox() {
        return mapper.listOutbox();
    }

    /**
     * 查询并返回 {@code listOperationLogs}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<TransportTaskMapper.OperationLogRow>}
     */
    public List<TransportTaskMapper.OperationLogRow> listOperationLogs() {
        return mapper.listOperationLogs();
    }

    /**
     * 查询并返回 {@code load}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code WaybillAggregate}
     */
    private WaybillAggregate load(String waybillNo) {
        WaybillMapper.WaybillRow row = mapper.findWaybill(waybillNo);
        if (row == null) {
            throw new IllegalArgumentException("waybill not found");
        }
        return WaybillAggregate.restore(row.waybillNo(), row.taskNo(), row.carrierCode(), row.carrierName(), row.carrierWaybillNo(), row.logisticsProductCode(), row.receiptPayload(), row.status(), row.voidReason(), row.approvalNo(), row.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code WaybillAggregate}
     * @return 转换数据模型的结果，类型为 {@code WaybillMapper.WaybillRow}
     */
    private WaybillMapper.WaybillRow toRow(WaybillAggregate aggregate) {
        return new WaybillMapper.WaybillRow(null, aggregate.waybillNo(), aggregate.taskNo(), aggregate.carrierCode(), aggregate.carrierName(), aggregate.carrierWaybillNo(), aggregate.logisticsProductCode(), aggregate.receiptPayload(), aggregate.status(), aggregate.voidReason(), aggregate.approvalNo(), aggregate.version());
    }

    /**
     * 执行命令 {@code saveEvents}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param events 业务处理参数或成员，类型为 {@code List<TmsEvent>}
     */
    private void saveEvents(List<TmsEvent> events) {
        for (TmsEvent event : events) {
            mapper.insertOutbox(new TransportTaskMapper.OutboxRow(event.eventType(), event.businessNo(), event.payload(), 1, event.occurredAt()));
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code log}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param operationType 业务处理参数或成员，类型为 {@code String}
     * @param businessNo 可追踪业务编码，类型为 {@code String}
     * @param operatorId 业务或技术标识，类型为 {@code Long}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     */
    private void log(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
        mapper.insertOperationLog(new TransportTaskMapper.OperationLogRow(operationType, businessNo, operatorId, idempotencyKey, LocalDateTime.now()));
    }

    /**
     * CreateCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateCommand(String carrierCode, String carrierName, String carrierWaybillNo, String logisticsProductCode, String receiptPayload, Long operatorId, String idempotencyKey) {
    }

    /**
     * VoidCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record VoidCommand(String reason, String approvalNo, long expectedVersion, Long operatorId, String idempotencyKey) {
    }
}
