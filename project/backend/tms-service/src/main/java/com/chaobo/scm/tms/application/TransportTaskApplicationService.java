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

/**
 * TransportTaskApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class TransportTaskApplicationService {

    /**
     * mapper（类型：{@code TransportTaskMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final TransportTaskMapper mapper;

    /**
     * sequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong sequence = new AtomicLong(700000);

    /**
     * 创建 TransportTaskApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code TransportTaskMapper}
     */
    public TransportTaskApplicationService(TransportTaskMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 执行命令 {@code createFromSource}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CreateCommand}
     * @return 执行命令的结果，类型为 {@code TransportTaskMapper.TaskRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public TransportTaskMapper.TaskRow createFromSource(CreateCommand command) {
        TransportTaskMapper.TaskRow existing = mapper.findActiveBySource(command.sourceSystem(), command.sourceOrderNo(), command.scenario());
        if (existing != null) {
            return existing;
        }
        String taskNo = "TMS" + sequence.incrementAndGet();
        TransportTaskAggregate aggregate = TransportTaskAggregate.create(taskNo, command.sourceSystem(), command.sourceOrderNo(), command.sourceLineNo(), command.scenario(), command.shipperId(), command.warehouseId(), command.originAddress(), command.destinationAddress(), command.packages(), command.logisticsProductCode(), command.feeResponsibility());
        TransportTaskMapper.TaskRow row = toRow(aggregate);
        mapper.insertTask(row);
        saveEvents(aggregate.pullEvents());
        log("CREATE_TRANSPORT_TASK", taskNo, command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 处理当前类型职责中的操作 {@code accept}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code AcceptCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TransportTaskMapper.TaskRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public TransportTaskMapper.TaskRow accept(String taskNo, AcceptCommand command) {
        TransportTaskAggregate aggregate = load(taskNo);
        aggregate.accept(command.carrierCode(), command.carrierName(), command.logisticsProductCode(), command.expectedVersion());
        TransportTaskMapper.TaskRow row = toRow(aggregate);
        mapper.updateTask(row);
        saveEvents(aggregate.pullEvents());
        log("ACCEPT_TRANSPORT_TASK", taskNo, command.operatorId(), command.idempotencyKey());
        return mapper.findTask(taskNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code start}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ChangeCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TransportTaskMapper.TaskRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public TransportTaskMapper.TaskRow start(String taskNo, ChangeCommand command) {
        TransportTaskAggregate aggregate = load(taskNo);
        aggregate.start(command.expectedVersion());
        mapper.updateTask(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("START_TRANSPORT_TASK", taskNo, command.operatorId(), command.idempotencyKey());
        return mapper.findTask(taskNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code deliver}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ChangeCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TransportTaskMapper.TaskRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public TransportTaskMapper.TaskRow deliver(String taskNo, ChangeCommand command) {
        TransportTaskAggregate aggregate = load(taskNo);
        aggregate.deliver(command.expectedVersion());
        mapper.updateTask(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("DELIVER_TRANSPORT_TASK", taskNo, command.operatorId(), command.idempotencyKey());
        return mapper.findTask(taskNo);
    }

    /**
     * 查询并返回 {@code get}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code TransportTaskMapper.TaskRow}
     */
    public TransportTaskMapper.TaskRow get(String taskNo) {
        return mapper.findTask(taskNo);
    }

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param query 业务处理参数或成员，类型为 {@code Query}
     * @return 查询并返回的结果，类型为 {@code List<TransportTaskMapper.TaskRow>}
     */
    public List<TransportTaskMapper.TaskRow> list(Query query) {
        int pageNo = query.pageNo() == null || query.pageNo() < 1 ? 1 : query.pageNo();
        int pageSize = query.pageSize() == null ? 20 : query.pageSize();
        if (pageSize < 1 || pageSize > LIST_VALUE_100) {
            throw new IllegalArgumentException("page size must be between 1 and 100");
        }
        return mapper.listTasks(emptyToNull(query.sourceSystem()), emptyToNull(query.scenario()), query.status(), query.warehouseId(), emptyToNull(query.carrierCode()), pageSize, (pageNo - 1) * pageSize);
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
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code TransportTaskAggregate}
     */
    private TransportTaskAggregate load(String taskNo) {
        TransportTaskMapper.TaskRow row = mapper.findTask(taskNo);
        if (row == null) {
            throw new IllegalArgumentException("transport task not found");
        }
        return fromRow(row);
    }

    /**
     * 转换数据模型 {@code fromRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code TransportTaskMapper.TaskRow}
     * @return 转换数据模型的结果，类型为 {@code TransportTaskAggregate}
     */
    private TransportTaskAggregate fromRow(TransportTaskMapper.TaskRow row) {
        return TransportTaskAggregate.restore(row.taskNo(), row.sourceSystem(), row.sourceOrderNo(), row.sourceLineNo(), row.scenario(), row.shipperId(), row.warehouseId(), parseAddress(row.originAddress()), parseAddress(row.destinationAddress()), parsePackages(row.packagePayload()), row.status(), row.carrierCode(), row.carrierName(), row.logisticsProductCode(), row.feeResponsibility(), row.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code TransportTaskAggregate}
     * @return 转换数据模型的结果，类型为 {@code TransportTaskMapper.TaskRow}
     */
    private TransportTaskMapper.TaskRow toRow(TransportTaskAggregate aggregate) {
        return new TransportTaskMapper.TaskRow(null, aggregate.taskNo(), aggregate.sourceSystem(), aggregate.sourceOrderNo(), aggregate.sourceLineNo(), aggregate.scenario(), aggregate.shipperId(), aggregate.warehouseId(), formatAddress(aggregate.originAddress()), formatAddress(aggregate.destinationAddress()), formatPackages(aggregate.packages()), aggregate.status(), aggregate.carrierCode(), aggregate.carrierName(), aggregate.logisticsProductCode(), aggregate.feeResponsibility(), aggregate.version());
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
     * 处理当前类型职责中的操作 {@code formatAddress}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param address 业务处理参数或成员，类型为 {@code TransportTaskAggregate.Address}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public static String formatAddress(TransportTaskAggregate.Address address) {
        return String.join("|", address.province(), address.city(), blankAsEmpty(address.district()), address.detail(), address.contactName(), address.contactPhone());
    }

    /**
     * 处理当前类型职责中的操作 {@code parseAddress}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param payload 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TransportTaskAggregate.Address}
     */
    public static TransportTaskAggregate.Address parseAddress(String payload) {
        String[] parts = payload.split("\\|", -1);
        if (parts.length != PARSE_ADDRESS_VALUE_6) {
            throw new IllegalArgumentException("invalid address payload");
        }
        return new TransportTaskAggregate.Address(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
    }

    /**
     * 处理当前类型职责中的操作 {@code formatPackages}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param packages 业务处理参数或成员，类型为 {@code List<TransportTaskAggregate.PackageItem>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public static String formatPackages(List<TransportTaskAggregate.PackageItem> packages) {
        return packages.stream().map(item -> String.join(":", item.packageNo(), item.quantity().toPlainString(), decimalToString(item.weightKg()), decimalToString(item.volumeCbm()))).collect(Collectors.joining(";"));
    }

    /**
     * 处理当前类型职责中的操作 {@code parsePackages}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param payload 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<TransportTaskAggregate.PackageItem>}
     */
    public static List<TransportTaskAggregate.PackageItem> parsePackages(String payload) {
        if (payload == null || payload.isBlank()) {
            return List.of();
        }
        return List.of(payload.split(";")).stream().map(item -> {
            String[] parts = item.split(":", -1);
            if (parts.length != PARSE_PACKAGES_VALUE_4) {
                throw new IllegalArgumentException("invalid package payload");
            }
            return new TransportTaskAggregate.PackageItem(parts[0], new BigDecimal(parts[1]), stringToDecimal(parts[2]), stringToDecimal(parts[3]));
        }).toList();
    }

    /**
     * 处理当前类型职责中的操作 {@code decimalToString}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code BigDecimal}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String decimalToString(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    /**
     * 处理当前类型职责中的操作 {@code stringToDecimal}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal stringToDecimal(String value) {
        return value == null || value.isBlank() ? null : new BigDecimal(value);
    }

    /**
     * 处理当前类型职责中的操作 {@code blankAsEmpty}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String blankAsEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * 处理当前类型职责中的操作 {@code emptyToNull}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    /**
     * CreateCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateCommand(String sourceSystem, String sourceOrderNo, String sourceLineNo, String scenario, Long shipperId, Long warehouseId, TransportTaskAggregate.Address originAddress, TransportTaskAggregate.Address destinationAddress, List<TransportTaskAggregate.PackageItem> packages, String logisticsProductCode, String feeResponsibility, Long operatorId, String idempotencyKey) {
    }

    /**
     * AcceptCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record AcceptCommand(String carrierCode, String carrierName, String logisticsProductCode, long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * ChangeCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ChangeCommand(long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * Query。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Query(String sourceSystem, String scenario, Integer status, Long warehouseId, String carrierCode, Integer pageNo, Integer pageSize) {
    }

    /**
     * 业务常量 {@code PARSE_PACKAGES_VALUE_4}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PARSE_PACKAGES_VALUE_4 = 4;

    /**
     * 业务常量 {@code PARSE_ADDRESS_VALUE_6}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PARSE_ADDRESS_VALUE_6 = 6;

    /**
     * 业务常量 {@code LIST_VALUE_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int LIST_VALUE_100 = 100;
}
