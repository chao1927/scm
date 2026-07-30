package com.chaobo.scm.mdm.application;

import com.chaobo.scm.mdm.domain.MasterDataRecordAggregate;
import com.chaobo.scm.mdm.domain.MasterDataVersionAggregate;
import com.chaobo.scm.mdm.domain.MdmEvent;
import com.chaobo.scm.mdm.infrastructure.persistence.MasterDataRecordMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MasterDataRecordApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class MasterDataRecordApplicationService {

    /**
     * mapper（类型：{@code MasterDataRecordMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataRecordMapper mapper;

    /**
     * mdmMapper（类型：{@code MdmMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final MdmMapper mdmMapper;

    /**
     * recordSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong recordSequence = new AtomicLong(200000);

    /**
     * 创建 MasterDataRecordApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code MasterDataRecordMapper}
     * @param mdmMapper 持久化访问依赖，类型为 {@code MdmMapper}
     */
    public MasterDataRecordApplicationService(MasterDataRecordMapper mapper, MdmMapper mdmMapper) {
        this.mapper = mapper;
        this.mdmMapper = mdmMapper;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CreateRecordCommand}
     * @return 执行命令的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MasterDataRecordMapper.RecordRow create(CreateRecordCommand command) {
        if (mdmMapper.findType(command.typeCode()) == null) {
            throw new IllegalStateException("type does not exist");
        }
        if (mapper.findRecordByCode(command.typeCode(), command.dataCode()) != null) {
            throw new IllegalStateException("master data code already exists");
        }
        MasterDataRecordAggregate aggregate = MasterDataRecordAggregate.create("MDR" + recordSequence.incrementAndGet(), command.typeCode(), command.dataCode(), command.dataName(), command.dataPayload());
        MasterDataRecordMapper.RecordRow row = toRow(aggregate);
        mapper.insertRecord(row);
        saveEvents(aggregate.pullEvents());
        log("CREATE_MASTER_DATA_RECORD", row.recordNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ChangeRecordCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MasterDataRecordMapper.RecordRow change(String recordNo, ChangeRecordCommand command) {
        MasterDataRecordAggregate aggregate = load(recordNo);
        aggregate.change(command.dataName(), command.dataPayload(), command.reason(), command.expectedVersion());
        mapper.updateRecord(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("CHANGE_MASTER_DATA_RECORD", recordNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRecord(recordNo);
    }

    /**
     * 执行命令 {@code submitReview}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code StateCommand}
     * @return 执行命令的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MasterDataRecordMapper.RecordRow submitReview(String recordNo, StateCommand command) {
        MasterDataRecordAggregate aggregate = load(recordNo);
        aggregate.submitReview(command.reason(), command.expectedVersion());
        mapper.updateRecord(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("SUBMIT_MASTER_DATA_RECORD", recordNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRecord(recordNo);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code StateCommand}
     * @return 执行命令的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MasterDataRecordMapper.RecordRow approve(String recordNo, StateCommand command) {
        MasterDataRecordAggregate aggregate = load(recordNo);
        aggregate.approve(command.reason(), command.expectedVersion());
        mapper.updateRecord(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        MasterDataVersionAggregate version = MasterDataVersionAggregate.generate("MDV" + recordNo.substring(Math.max(0, recordNo.length() - 6)) + "V" + aggregate.currentVersionNo(), aggregate, command.reason());
        mapper.insertVersion(toRow(version));
        saveEvents(version.pullEvents());
        log("APPROVE_MASTER_DATA_RECORD", recordNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRecord(recordNo);
    }

    /**
     * 执行命令 {@code reject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code StateCommand}
     * @return 执行命令的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MasterDataRecordMapper.RecordRow reject(String recordNo, StateCommand command) {
        MasterDataRecordAggregate aggregate = load(recordNo);
        aggregate.reject(command.reason(), command.expectedVersion());
        mapper.updateRecord(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("REJECT_MASTER_DATA_RECORD", recordNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRecord(recordNo);
    }

    /**
     * 执行命令 {@code freeze}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code StateCommand}
     * @return 执行命令的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MasterDataRecordMapper.RecordRow freeze(String recordNo, StateCommand command) {
        MasterDataRecordAggregate aggregate = load(recordNo);
        aggregate.freeze(command.reason(), command.expectedVersion());
        mapper.updateRecord(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("FREEZE_MASTER_DATA_RECORD", recordNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRecord(recordNo);
    }

    /**
     * 执行命令 {@code disable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code StateCommand}
     * @return 执行命令的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MasterDataRecordMapper.RecordRow disable(String recordNo, StateCommand command) {
        MasterDataRecordAggregate aggregate = load(recordNo);
        aggregate.disable(command.reason(), command.expectedVersion());
        mapper.updateRecord(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("DISABLE_MASTER_DATA_RECORD", recordNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRecord(recordNo);
    }

    /**
     * 查询并返回 {@code get}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
     */
    public MasterDataRecordMapper.RecordRow get(String recordNo) {
        return mapper.findRecord(recordNo);
    }

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param query 业务处理参数或成员，类型为 {@code Query}
     * @return 查询并返回的结果，类型为 {@code List<MasterDataRecordMapper.RecordRow>}
     */
    public List<MasterDataRecordMapper.RecordRow> list(Query query) {
        int pageNo = query.pageNo() == null || query.pageNo() < 1 ? 1 : query.pageNo();
        int pageSize = query.pageSize() == null ? 20 : query.pageSize();
        if (pageSize < 1 || pageSize > LIST_VALUE_100) {
            throw new IllegalArgumentException("page size must be between 1 and 100");
        }
        return mapper.listRecords(emptyToNull(query.typeCode()), query.status(), pageSize, (pageNo - 1) * pageSize);
    }

    /**
     * 查询并返回 {@code listVersions}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code List<MasterDataRecordMapper.VersionRow>}
     */
    public List<MasterDataRecordMapper.VersionRow> listVersions(String recordNo) {
        return mapper.listVersions(recordNo);
    }

    /**
     * 查询并返回 {@code getVersion}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param versionNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code MasterDataRecordMapper.VersionRow}
     */
    public MasterDataRecordMapper.VersionRow getVersion(String versionNo) {
        return mapper.findVersion(versionNo);
    }

    /**
     * 查询并返回 {@code load}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code MasterDataRecordAggregate}
     */
    private MasterDataRecordAggregate load(String recordNo) {
        MasterDataRecordMapper.RecordRow row = mapper.findRecord(recordNo);
        if (row == null) {
            throw new IllegalArgumentException("master data record not found");
        }
        return MasterDataRecordAggregate.restore(row.recordNo(), row.typeCode(), row.dataCode(), row.dataName(), row.dataPayload(), row.status(), row.currentVersionNo(), row.reason(), row.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code MasterDataRecordAggregate}
     * @return 转换数据模型的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
     */
    private MasterDataRecordMapper.RecordRow toRow(MasterDataRecordAggregate aggregate) {
        return new MasterDataRecordMapper.RecordRow(null, aggregate.recordNo(), aggregate.typeCode(), aggregate.dataCode(), aggregate.dataName(), aggregate.dataPayload(), aggregate.status(), aggregate.currentVersionNo(), aggregate.reason(), aggregate.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code MasterDataVersionAggregate}
     * @return 转换数据模型的结果，类型为 {@code MasterDataRecordMapper.VersionRow}
     */
    private MasterDataRecordMapper.VersionRow toRow(MasterDataVersionAggregate aggregate) {
        return new MasterDataRecordMapper.VersionRow(null, aggregate.versionNo(), aggregate.recordNo(), aggregate.typeCode(), aggregate.dataCode(), aggregate.versionNumber(), aggregate.snapshotPayload(), aggregate.changeSummary(), LocalDateTime.now());
    }

    /**
     * 执行命令 {@code saveEvents}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param events 业务处理参数或成员，类型为 {@code List<MdmEvent>}
     */
    private void saveEvents(List<MdmEvent> events) {
        for (MdmEvent event : events) {
            mapper.insertOutbox(new MdmMapper.OutboxRow(event.eventType(), event.businessNo(), event.payload(), 1, event.occurredAt()));
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
        mapper.insertOperationLog(new MdmMapper.OperationLogRow(operationType, businessNo, operatorId, idempotencyKey, LocalDateTime.now()));
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
     * CreateRecordCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateRecordCommand(String typeCode, String dataCode, String dataName, String dataPayload, Long operatorId, String idempotencyKey) {
    }

    /**
     * ChangeRecordCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ChangeRecordCommand(String dataName, String dataPayload, String reason, long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * StateCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record StateCommand(String reason, long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * Query。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Query(String typeCode, Integer status, Integer pageNo, Integer pageSize) {
    }

    /**
     * 业务常量 {@code LIST_VALUE_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int LIST_VALUE_100 = 100;
}
