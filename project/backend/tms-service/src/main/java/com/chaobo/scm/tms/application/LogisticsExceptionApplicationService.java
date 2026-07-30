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

/**
 * LogisticsExceptionApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class LogisticsExceptionApplicationService {

    /**
     * mapper（类型：{@code LogisticsSettlementMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final LogisticsSettlementMapper mapper;

    /**
     * waybillService（类型：{@code WaybillApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final WaybillApplicationService waybillService;

    /**
     * sequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong sequence = new AtomicLong(120000);

    /**
     * 创建 LogisticsExceptionApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code LogisticsSettlementMapper}
     * @param waybillService 应用或外部协作依赖，类型为 {@code WaybillApplicationService}
     */
    public LogisticsExceptionApplicationService(LogisticsSettlementMapper mapper, WaybillApplicationService waybillService) {
        this.mapper = mapper;
        this.waybillService = waybillService;
    }

    /**
     * 处理当前类型职责中的操作 {@code register}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code RegisterCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code LogisticsSettlementMapper.ExceptionRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public LogisticsSettlementMapper.ExceptionRow register(RegisterCommand command) {
        if (waybillService.get(command.waybillNo()) == null) {
            throw new IllegalArgumentException("waybill not found");
        }
        LogisticsExceptionAggregate aggregate = LogisticsExceptionAggregate.register("EXC" + sequence.incrementAndGet(), command.waybillNo(), command.exceptionType(), command.level(), command.description(), command.responsibleParty());
        LogisticsSettlementMapper.ExceptionRow row = toRow(aggregate);
        mapper.insertException(row);
        saveEvents(aggregate.pullEvents());
        log("REGISTER_LOGISTICS_EXCEPTION", row.exceptionNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 执行命令 {@code close}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param exceptionNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code CloseCommand}
     * @return 执行命令的结果，类型为 {@code LogisticsSettlementMapper.ExceptionRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public LogisticsSettlementMapper.ExceptionRow close(String exceptionNo, CloseCommand command) {
        LogisticsExceptionAggregate aggregate = load(exceptionNo);
        aggregate.close(command.closeResult(), command.responsibleParty(), command.expectedVersion());
        LogisticsSettlementMapper.ExceptionRow row = toRow(aggregate);
        mapper.updateException(row);
        saveEvents(aggregate.pullEvents());
        log("CLOSE_LOGISTICS_EXCEPTION", exceptionNo, command.operatorId(), command.idempotencyKey());
        return mapper.findException(exceptionNo);
    }

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<LogisticsSettlementMapper.ExceptionRow>}
     */
    public List<LogisticsSettlementMapper.ExceptionRow> list() {
        return mapper.listExceptions();
    }

    /**
     * 查询并返回 {@code load}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param exceptionNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code LogisticsExceptionAggregate}
     */
    private LogisticsExceptionAggregate load(String exceptionNo) {
        LogisticsSettlementMapper.ExceptionRow row = mapper.findException(exceptionNo);
        if (row == null) {
            throw new IllegalArgumentException("logistics exception not found");
        }
        return LogisticsExceptionAggregate.restore(row.exceptionNo(), row.waybillNo(), row.exceptionType(), row.level(), row.description(), row.responsibleParty(), row.status(), row.closeResult(), row.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code LogisticsExceptionAggregate}
     * @return 转换数据模型的结果，类型为 {@code LogisticsSettlementMapper.ExceptionRow}
     */
    private LogisticsSettlementMapper.ExceptionRow toRow(LogisticsExceptionAggregate aggregate) {
        return new LogisticsSettlementMapper.ExceptionRow(null, aggregate.exceptionNo(), aggregate.waybillNo(), aggregate.exceptionType(), aggregate.level(), aggregate.description(), aggregate.responsibleParty(), aggregate.status(), aggregate.closeResult(), aggregate.version());
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
     * RegisterCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record RegisterCommand(String waybillNo, String exceptionType, String level, String description, String responsibleParty, Long operatorId, String idempotencyKey) {
    }

    /**
     * CloseCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CloseCommand(String closeResult, String responsibleParty, long expectedVersion, Long operatorId, String idempotencyKey) {
    }
}
