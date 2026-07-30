package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.domain.TmsEvent;
import com.chaobo.scm.tms.domain.TrackingAggregate;
import com.chaobo.scm.tms.domain.WaybillAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TrackingMapper;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import com.chaobo.scm.tms.infrastructure.persistence.WaybillMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TrackingApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class TrackingApplicationService {

    /**
     * mapper（类型：{@code TrackingMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final TrackingMapper mapper;

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
    private final AtomicLong sequence = new AtomicLong(100000);

    /**
     * 创建 TrackingApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code TrackingMapper}
     * @param waybillService 应用或外部协作依赖，类型为 {@code WaybillApplicationService}
     */
    public TrackingApplicationService(TrackingMapper mapper, WaybillApplicationService waybillService) {
        this.mapper = mapper;
        this.waybillService = waybillService;
    }

    /**
     * 处理当前类型职责中的操作 {@code append}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code AppendCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TrackingMapper.TrackRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public TrackingMapper.TrackRow append(AppendCommand command) {
        ensureActiveWaybill(command.waybillNo());
        TrackingMapper.TrackRow existing = mapper.findTrackDuplicate(command.waybillNo(), command.nodeCode(), command.trackAt());
        if (existing != null) {
            return existing;
        }
        TrackingAggregate aggregate = TrackingAggregate.append("TRK" + sequence.incrementAndGet(), command.waybillNo(), command.nodeCode(), command.description(), command.location(), command.trackAt(), command.sourceType(), command.rawEventId());
        TrackingMapper.TrackRow row = toRow(aggregate);
        mapper.insertTrack(row);
        saveEvents(aggregate.pullEvents());
        log("APPEND_TRACKING", command.waybillNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 处理当前类型职责中的操作 {@code supplement}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code SupplementCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TrackingMapper.TrackRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public TrackingMapper.TrackRow supplement(String waybillNo, SupplementCommand command) {
        ensureActiveWaybill(waybillNo);
        TrackingAggregate aggregate = TrackingAggregate.supplement("TRK" + sequence.incrementAndGet(), waybillNo, command.nodeCode(), command.description(), command.location(), command.trackAt(), command.reason());
        TrackingMapper.TrackRow row = toRow(aggregate);
        mapper.insertTrack(row);
        saveEvents(aggregate.pullEvents());
        log("SUPPLEMENT_TRACKING", waybillNo, command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code List<TrackingMapper.TrackRow>}
     */
    public List<TrackingMapper.TrackRow> list(String waybillNo) {
        return mapper.listTracks(waybillNo);
    }

    /**
     * 校验业务约束 {@code ensureActiveWaybill}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     */
    private void ensureActiveWaybill(String waybillNo) {
        WaybillMapper.WaybillRow waybill = waybillService.get(waybillNo);
        if (waybill == null) {
            throw new IllegalArgumentException("waybill not found");
        }
        if (waybill.status() == WaybillAggregate.VOIDED) {
            throw new IllegalStateException("voided waybill cannot receive tracking");
        }
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code TrackingAggregate}
     * @return 转换数据模型的结果，类型为 {@code TrackingMapper.TrackRow}
     */
    private TrackingMapper.TrackRow toRow(TrackingAggregate aggregate) {
        return new TrackingMapper.TrackRow(null, aggregate.trackNo(), aggregate.waybillNo(), aggregate.nodeCode(), aggregate.description(), aggregate.location(), aggregate.trackAt(), aggregate.sourceType(), aggregate.rawEventId(), aggregate.manualReason());
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
     * AppendCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record AppendCommand(String waybillNo, String nodeCode, String description, String location, LocalDateTime trackAt, String sourceType, String rawEventId, Long operatorId, String idempotencyKey) {
    }

    /**
     * SupplementCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record SupplementCommand(String nodeCode, String description, String location, LocalDateTime trackAt, String reason, Long operatorId, String idempotencyKey) {
    }
}
