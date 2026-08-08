package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.domain.ShippingLabelAggregate;
import com.chaobo.scm.tms.domain.TmsEvent;
import com.chaobo.scm.tms.domain.WaybillAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import com.chaobo.scm.tms.infrastructure.persistence.WaybillMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ShippingLabelApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class ShippingLabelApplicationService {

    /**
     * mapper（类型：{@code WaybillMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final WaybillMapper mapper;

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
    private final AtomicLong sequence = new AtomicLong(900000);

    /**
     * 创建 ShippingLabelApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code WaybillMapper}
     * @param waybillService 应用或外部协作依赖，类型为 {@code WaybillApplicationService}
     */
    public ShippingLabelApplicationService(WaybillMapper mapper, WaybillApplicationService waybillService) {
        this.mapper = mapper;
        this.waybillService = waybillService;
    }

    /**
     * 处理当前类型职责中的操作 {@code generate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code GenerateCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code WaybillMapper.LabelRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public WaybillMapper.LabelRow generate(String waybillNo, GenerateCommand command) {
        WaybillMapper.LabelRow existing = mapper.findActiveLabel(waybillNo, command.packageNo());
        if (existing != null) {
            return existing;
        }
        WaybillMapper.WaybillRow waybill = waybillService.get(waybillNo);
        if (waybill == null) {
            throw new IllegalArgumentException("waybill not found");
        }
        if (waybill.status() != WaybillAggregate.CREATED) {
            throw new IllegalStateException("shipping label requires active waybill");
        }
        String labelNo = "LBL" + sequence.incrementAndGet();
        ShippingLabelAggregate aggregate = ShippingLabelAggregate.generate(labelNo, waybillNo, command.packageNo(), command.templateVersion(), command.labelUrl());
        WaybillMapper.LabelRow row = toRow(aggregate);
        mapper.insertLabel(row);
        saveEvents(aggregate.pullEvents());
        log("GENERATE_SHIPPING_LABEL", labelNo, command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 处理当前类型职责中的操作 {@code print}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param labelNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code PrintCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code WaybillMapper.LabelRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public WaybillMapper.LabelRow print(String labelNo, PrintCommand command) {
        ShippingLabelAggregate aggregate = load(labelNo);
        aggregate.print(command.deviceNo());
        WaybillMapper.LabelRow row = toRow(aggregate);
        mapper.updateLabel(row);
        saveEvents(aggregate.pullEvents());
        log("PRINT_SHIPPING_LABEL", labelNo, command.operatorId(), command.idempotencyKey());
        return mapper.findLabel(labelNo);
    }

    /** 作废面单并保存领域事件和操作审计。 */
    @Transactional(rollbackFor = Exception.class)
    public WaybillMapper.LabelRow voidLabel(String labelNo, VoidCommand command) {
        ShippingLabelAggregate aggregate = load(labelNo);
        if (aggregate.version() != command.expectedVersion()) {
            throw new IllegalStateException("shipping label version conflict");
        }
        aggregate.voidLabel(command.reason());
        mapper.updateLabel(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("VOID_SHIPPING_LABEL", labelNo, command.operatorId(), command.idempotencyKey());
        return mapper.findLabel(labelNo);
    }

    /**
     * 查询并返回 {@code listByWaybill}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code List<WaybillMapper.LabelRow>}
     */
    public List<WaybillMapper.LabelRow> listByWaybill(String waybillNo) {
        return mapper.listLabelsByWaybill(waybillNo);
    }

    /**
     * 查询并返回 {@code load}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param labelNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ShippingLabelAggregate}
     */
    private ShippingLabelAggregate load(String labelNo) {
        WaybillMapper.LabelRow row = mapper.findLabel(labelNo);
        if (row == null) {
            throw new IllegalArgumentException("shipping label not found");
        }
        return ShippingLabelAggregate.restore(row.labelNo(), row.waybillNo(), row.packageNo(), row.templateVersion(), row.labelUrl(), row.status(), row.printCount(), row.lastPrintDevice(), row.voidReason(), row.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code ShippingLabelAggregate}
     * @return 转换数据模型的结果，类型为 {@code WaybillMapper.LabelRow}
     */
    private WaybillMapper.LabelRow toRow(ShippingLabelAggregate aggregate) {
        return new WaybillMapper.LabelRow(null, aggregate.labelNo(), aggregate.waybillNo(), aggregate.packageNo(), aggregate.templateVersion(), aggregate.labelUrl(), aggregate.status(), aggregate.printCount(), aggregate.lastPrintDevice(), aggregate.voidReason(), aggregate.version());
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
     * GenerateCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record GenerateCommand(String packageNo, String templateVersion, String labelUrl, Long operatorId, String idempotencyKey) {
    }

    /**
     * PrintCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record PrintCommand(String deviceNo, Long operatorId, String idempotencyKey) {
    }

    public record VoidCommand(String reason, long expectedVersion, Long operatorId,
                              String idempotencyKey) {
    }
}
