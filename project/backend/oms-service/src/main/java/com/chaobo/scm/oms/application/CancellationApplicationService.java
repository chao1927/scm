package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.domain.CancellationRequestAggregate;
import com.chaobo.scm.oms.domain.FulfillmentAggregate;
import com.chaobo.scm.oms.domain.OmsEvent;
import com.chaobo.scm.oms.infrastructure.persistence.CancellationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * CancellationApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class CancellationApplicationService {

    /**
     * mapper（类型：{@code CancellationMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final CancellationMapper mapper;

    /**
     * fulfillmentService（类型：{@code FulfillmentApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final FulfillmentApplicationService fulfillmentService;

    /**
     * sequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong sequence = new AtomicLong(500000);

    /**
     * 创建 CancellationApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code CancellationMapper}
     * @param fulfillmentService 应用或外部协作依赖，类型为 {@code FulfillmentApplicationService}
     */
    public CancellationApplicationService(CancellationMapper mapper, FulfillmentApplicationService fulfillmentService) {
        this.mapper = mapper;
        this.fulfillmentService = fulfillmentService;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CreateCommand}
     * @return 执行命令的结果，类型为 {@code CancellationMapper.CancelRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public CancellationMapper.CancelRow create(CreateCommand command) {
        FulfillmentMapperView fulfillment = new FulfillmentMapperView(fulfillmentService.getFulfillment(command.fulfillmentNo()));
        if (fulfillment.status() == FulfillmentAggregate.SHIPPED) {
            throw new IllegalStateException("shipped fulfillment must use after-sale");
        }
        CancellationMapper.CancelRow existing = mapper.findCancelByFulfillment(command.fulfillmentNo());
        if (existing != null) {
            return existing;
        }
        String cancellationNo = "CAN" + sequence.incrementAndGet();
        CancellationRequestAggregate aggregate = CancellationRequestAggregate.create(cancellationNo, fulfillment.salesOrderNo(), command.fulfillmentNo(), fulfillment.outboundNo(), fulfillment.reservationRefNo(), command.reason());
        CancellationMapper.CancelRow row = toRow(aggregate);
        mapper.insertCancel(row);
        saveEvents(aggregate.pullEvents());
        log("CREATE_CANCEL_REQUEST", cancellationNo, command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param cancellationNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ApproveCommand}
     * @return 执行命令的结果，类型为 {@code CancellationMapper.CancelRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public CancellationMapper.CancelRow approve(String cancellationNo, ApproveCommand command) {
        CancellationRequestAggregate aggregate = load(cancellationNo);
        aggregate.approve(command.remark());
        mapper.updateCancel(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("APPROVE_CANCEL_REQUEST", cancellationNo, command.operatorId(), command.idempotencyKey());
        return mapper.findCancel(cancellationNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code process}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param cancellationNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ProcessCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CancellationMapper.CancelRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public CancellationMapper.CancelRow process(String cancellationNo, ProcessCommand command) {
        CancellationRequestAggregate aggregate = load(cancellationNo);
        boolean requiresWms = aggregate.outboundNo() != null && !aggregate.outboundNo().isBlank();
        aggregate.process(requiresWms);
        mapper.updateCancel(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        if (requiresWms) {
            fulfillmentService.cancelOutbound(aggregate.outboundNo(), new FulfillmentApplicationService.CancelOutboundCommand(aggregate.reason(), command.operatorId(), command.idempotencyKey()));
        } else if (aggregate.reservationRefNo() != null && !aggregate.reservationRefNo().isBlank()) {
            mapper.insertIntegrationCommand(new CancellationMapper.IntegrationCommandRow("ReleaseInventory", "INVENTORY", cancellationNo, cancellationNo + ":RELEASE", aggregate.reservationRefNo()));
        } else {
            aggregate.markStockReleased();
            mapper.updateCancel(toRow(aggregate));
            saveEvents(aggregate.pullEvents());
        }
        log("PROCESS_CANCEL_REQUEST", cancellationNo, command.operatorId(), command.idempotencyKey());
        return mapper.findCancel(cancellationNo);
    }

    /**
     * 执行命令 {@code consumeEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code CancellationEvent}
     */
    @Transactional(rollbackFor = Exception.class)
    public void consumeEvent(CancellationEvent event) {
        int claimed = mapper.claimEvent(new CancellationMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessNo(), event.payload(), 1, null));
        if (claimed == 0) {
            return;
        }
        try {
            CancellationRequestAggregate aggregate = switch(event.eventType()) {
                case "WmsOutboundCancelled" ->
                    loadByOutbound(event.outboundNo());
                case "StockReleased" ->
                    loadByReservation(event.reservationRefNo());
                default ->
                    throw new IllegalArgumentException("unsupported cancellation event: " + event.eventType());
            };
            if (aggregate == null) {
                mapper.updateEvent(new CancellationMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessNo(), event.payload(), 2, null));
                return;
            }
            if (WMS_OUTBOUND_CANCELLED.equals(event.eventType())) {
                aggregate.markWmsCancelled();
            } else {
                aggregate.markStockReleased();
            }
            mapper.updateCancel(toRow(aggregate));
            saveEvents(aggregate.pullEvents());
            mapper.updateEvent(new CancellationMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessNo(), event.payload(), 2, null));
        } catch (RuntimeException exception) {
            mapper.updateEvent(new CancellationMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessNo(), event.payload(), 3, exception.getMessage()));
            throw exception;
        }
    }

    /**
     * 查询并返回 {@code get}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param cancellationNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code CancellationMapper.CancelRow}
     */
    public CancellationMapper.CancelRow get(String cancellationNo) {
        return mapper.findCancel(cancellationNo);
    }

    /**
     * 查询并返回 {@code load}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param cancellationNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code CancellationRequestAggregate}
     */
    private CancellationRequestAggregate load(String cancellationNo) {
        CancellationMapper.CancelRow row = mapper.findCancel(cancellationNo);
        if (row == null) {
            throw new IllegalArgumentException("cancellation request not found");
        }
        return fromRow(row);
    }

    /**
     * 查询并返回 {@code loadByOutbound}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code CancellationRequestAggregate}
     */
    private CancellationRequestAggregate loadByOutbound(String outboundNo) {
        if (outboundNo == null || outboundNo.isBlank()) {
            return null;
        }
        CancellationMapper.CancelRow row = mapper.findCancelByOutbound(outboundNo);
        if (row == null) {
            return null;
        }
        return fromRow(row);
    }

    /**
     * 查询并返回 {@code loadByReservation}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param reservationRefNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code CancellationRequestAggregate}
     */
    private CancellationRequestAggregate loadByReservation(String reservationRefNo) {
        if (reservationRefNo == null || reservationRefNo.isBlank()) {
            return null;
        }
        CancellationMapper.CancelRow row = mapper.findCancelByReservation(reservationRefNo);
        if (row == null) {
            return null;
        }
        return fromRow(row);
    }

    /**
     * 转换数据模型 {@code fromRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code CancellationMapper.CancelRow}
     * @return 转换数据模型的结果，类型为 {@code CancellationRequestAggregate}
     */
    private CancellationRequestAggregate fromRow(CancellationMapper.CancelRow row) {
        return CancellationRequestAggregate.restore(row.cancellationNo(), row.salesOrderNo(), row.fulfillmentNo(), row.outboundNo(), row.reservationRefNo(), row.reason(), row.status(), row.wmsCancelled(), row.stockReleased(), row.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code CancellationRequestAggregate}
     * @return 转换数据模型的结果，类型为 {@code CancellationMapper.CancelRow}
     */
    private CancellationMapper.CancelRow toRow(CancellationRequestAggregate aggregate) {
        return new CancellationMapper.CancelRow(aggregate.cancellationNo(), aggregate.salesOrderNo(), aggregate.fulfillmentNo(), aggregate.outboundNo(), aggregate.reservationRefNo(), aggregate.reason(), aggregate.status(), aggregate.wmsCancelled(), aggregate.stockReleased(), aggregate.version());
    }

    /**
     * 执行命令 {@code saveEvents}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param events 业务处理参数或成员，类型为 {@code List<OmsEvent>}
     */
    private void saveEvents(List<OmsEvent> events) {
        for (OmsEvent event : events) {
            mapper.insertOutbox(new CancellationMapper.OutboxRow(event.eventType(), event.businessNo(), event.payload(), event.occurredAt()));
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
        mapper.insertOperationLog(new CancellationMapper.OperationLogRow(operationType, businessNo, operatorId, idempotencyKey));
    }

    /**
     * FulfillmentMapperView。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record FulfillmentMapperView(com.chaobo.scm.oms.infrastructure.persistence.FulfillmentMapper.FulfillmentRow row) {

        /**
         * 处理当前类型职责中的操作 {@code status}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        int status() {
            return row.status();
        }

        /**
         * 处理当前类型职责中的操作 {@code salesOrderNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        String salesOrderNo() {
            return row.salesOrderNo();
        }

        /**
         * 处理当前类型职责中的操作 {@code outboundNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        String outboundNo() {
            return row.outboundNo();
        }

        /**
         * 处理当前类型职责中的操作 {@code reservationRefNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        String reservationRefNo() {
            return row.reservationRefNo();
        }
    }

    /**
     * CreateCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateCommand(String fulfillmentNo, String reason, Long operatorId, String idempotencyKey) {
    }

    /**
     * ApproveCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ApproveCommand(String remark, Long operatorId, String idempotencyKey) {
    }

    /**
     * ProcessCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ProcessCommand(Long operatorId, String idempotencyKey) {
    }

    /**
     * CancellationEvent。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。表达已经发生的业务事实，载荷用于跨事务或跨上下文可靠传播。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CancellationEvent(String eventId, String eventType, String businessNo, String outboundNo, String reservationRefNo, String payload) {
    }

    /**
     * 业务常量 {@code WMS_OUTBOUND_CANCELLED}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String WMS_OUTBOUND_CANCELLED = "WmsOutboundCancelled";
}
