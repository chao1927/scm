package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.domain.FulfillmentAggregate;
import com.chaobo.scm.oms.domain.OmsEvent;
import com.chaobo.scm.oms.domain.OutboundAggregate;
import com.chaobo.scm.oms.infrastructure.persistence.FulfillmentMapper;
import com.chaobo.scm.oms.infrastructure.persistence.OmsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * FulfillmentApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class FulfillmentApplicationService {

    /**
     * RESERVATION_PENDING（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final int RESERVATION_PENDING = 1;

    /**
     * RESERVATION_RESERVED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final int RESERVATION_RESERVED = 2;

    /**
     * RESERVATION_FAILED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final int RESERVATION_FAILED = 3;

    /**
     * RESERVATION_RELEASE_REQUESTED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final int RESERVATION_RELEASE_REQUESTED = 4;

    /**
     * RESERVATION_RELEASED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final int RESERVATION_RELEASED = 5;

    /**
     * mapper（类型：{@code FulfillmentMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final FulfillmentMapper mapper;

    /**
     * omsMapper（类型：{@code OmsMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final OmsMapper omsMapper;

    /**
     * fulfillmentSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong fulfillmentSequence = new AtomicLong(200000);

    /**
     * reservationSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong reservationSequence = new AtomicLong(300000);

    /**
     * outboundSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong outboundSequence = new AtomicLong(400000);

    /**
     * 创建 FulfillmentApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code FulfillmentMapper}
     * @param omsMapper 持久化访问依赖，类型为 {@code OmsMapper}
     */
    public FulfillmentApplicationService(FulfillmentMapper mapper, OmsMapper omsMapper) {
        this.mapper = mapper;
        this.omsMapper = omsMapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code allocate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code AllocateCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code FulfillmentMapper.FulfillmentRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public FulfillmentMapper.FulfillmentRow allocate(AllocateCommand command) {
        FulfillmentMapper.FulfillmentRow existing = mapper.findBySalesOrder(command.salesOrderNo());
        if (existing != null) {
            return existing;
        }
        OmsMapper.SalesOrderRow order = omsMapper.findOrder(command.salesOrderNo());
        if (order == null) {
            throw new IllegalArgumentException("sales order not found");
        }
        if (order.status() != com.chaobo.scm.oms.domain.SalesOrderAggregate.APPROVED) {
            throw new IllegalStateException("sales order is not approved");
        }
        String fulfillmentNo = "FUL" + fulfillmentSequence.incrementAndGet();
        FulfillmentAggregate aggregate = FulfillmentAggregate.create(fulfillmentNo, order.orderNo(), order.channelCode(), order.customerId(), command.warehouseId(), command.warehouseCode(), command.logisticsProductCode(), salesLines(order.linePayload()));
        FulfillmentMapper.FulfillmentRow row = toRow(aggregate);
        mapper.insertFulfillment(row);
        saveEvents(aggregate.pullEvents());
        log("ALLOCATE_FULFILLMENT", fulfillmentNo, command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 处理当前类型职责中的操作 {@code changeWarehouse}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ChangeWarehouseCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code FulfillmentMapper.FulfillmentRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public FulfillmentMapper.FulfillmentRow changeWarehouse(String fulfillmentNo, ChangeWarehouseCommand command) {
        FulfillmentAggregate aggregate = loadFulfillment(fulfillmentNo);
        aggregate.changeWarehouse(command.warehouseId(), command.warehouseCode(), command.reason());
        FulfillmentMapper.FulfillmentRow row = toRow(aggregate);
        mapper.updateFulfillment(row);
        saveEvents(aggregate.pullEvents());
        log("CHANGE_FULFILLMENT_WAREHOUSE", fulfillmentNo, command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 处理当前类型职责中的操作 {@code split}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code SplitCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code FulfillmentMapper.FulfillmentRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public FulfillmentMapper.FulfillmentRow split(String fulfillmentNo, SplitCommand command) {
        FulfillmentAggregate parent = loadFulfillment(fulfillmentNo);
        String childNo = "FUL" + fulfillmentSequence.incrementAndGet();
        FulfillmentAggregate child = parent.split(childNo, command.lines(), command.reason());
        mapper.updateFulfillment(toRow(parent));
        mapper.insertFulfillment(toRow(child));
        saveEvents(parent.pullEvents());
        saveEvents(child.pullEvents());
        log("SPLIT_FULFILLMENT", fulfillmentNo, command.operatorId(), command.idempotencyKey());
        return toRow(child);
    }

    /**
     * 执行命令 {@code reserve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ReserveCommand}
     * @return 执行命令的结果，类型为 {@code FulfillmentMapper.FulfillmentRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public FulfillmentMapper.FulfillmentRow reserve(String fulfillmentNo, ReserveCommand command) {
        FulfillmentAggregate aggregate = loadFulfillment(fulfillmentNo);
        FulfillmentMapper.ReservationRow existing = mapper.findReservationByFulfillment(fulfillmentNo);
        if (existing != null) {
            return toRow(aggregate);
        }
        String reservationRefNo = "RESREF" + reservationSequence.incrementAndGet();
        aggregate.requestReservation(reservationRefNo);
        FulfillmentMapper.ReservationRow reservation = new FulfillmentMapper.ReservationRow(reservationRefNo, fulfillmentNo, null, totalQuantity(aggregate.lines()), BigDecimal.ZERO, RESERVATION_PENDING, null, 1);
        mapper.insertReservation(reservation);
        mapper.updateFulfillment(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        insertCommand("ReserveInventory", "INVENTORY", fulfillmentNo, reservationRefNo, reservationRefNo + "|" + aggregate.warehouseId() + "|" + formatLines(aggregate.lines()));
        log("REQUEST_STOCK_RESERVATION", fulfillmentNo, command.operatorId(), command.idempotencyKey());
        return toRow(aggregate);
    }

    /**
     * 执行命令 {@code releaseReservation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reservationRefNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ReleaseCommand}
     * @return 执行命令的结果，类型为 {@code FulfillmentMapper.FulfillmentRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public FulfillmentMapper.FulfillmentRow releaseReservation(String reservationRefNo, ReleaseCommand command) {
        FulfillmentMapper.ReservationRow reservation = requireReservation(reservationRefNo);
        if (reservation.status() == RESERVATION_RELEASED || reservation.status() == RESERVATION_RELEASE_REQUESTED) {
            return requireFulfillment(reservation.fulfillmentNo());
        }
        if (reservation.status() != RESERVATION_RESERVED) {
            throw new IllegalStateException("reservation is not releasable");
        }
        FulfillmentMapper.ReservationRow updated = new FulfillmentMapper.ReservationRow(reservation.reservationRefNo(), reservation.fulfillmentNo(), reservation.reservationNo(), reservation.reserveQty(), reservation.reservedQty(), RESERVATION_RELEASE_REQUESTED, command.reason(), reservation.version() + 1);
        mapper.updateReservation(updated);
        insertCommand("ReleaseInventory", "INVENTORY", reservation.fulfillmentNo(), reservation.reservationRefNo() + ":RELEASE", reservation.reservationNo() + "|" + command.reason());
        log("REQUEST_STOCK_RELEASE", reservation.fulfillmentNo(), command.operatorId(), command.idempotencyKey());
        return requireFulfillment(reservation.fulfillmentNo());
    }

    /**
     * 执行命令 {@code createOutbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code CreateOutboundCommand}
     * @return 执行命令的结果，类型为 {@code FulfillmentMapper.FulfillmentRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public FulfillmentMapper.FulfillmentRow createOutbound(String fulfillmentNo, CreateOutboundCommand command) {
        FulfillmentMapper.OutboundRow existing = mapper.findOutboundByFulfillment(fulfillmentNo);
        if (existing != null) {
            return requireFulfillment(fulfillmentNo);
        }
        FulfillmentAggregate fulfillment = loadFulfillment(fulfillmentNo);
        if (fulfillment.status() != FulfillmentAggregate.RESERVED) {
            throw new IllegalStateException("fulfillment is not reserved");
        }
        String outboundNo = "OUT" + outboundSequence.incrementAndGet();
        OutboundAggregate outbound = OutboundAggregate.create(outboundNo, fulfillment.fulfillmentNo(), fulfillment.salesOrderNo(), fulfillment.warehouseId(), fulfillment.warehouseCode());
        mapper.insertOutbound(toRow(outbound));
        saveEvents(outbound.pullEvents());
        log("CREATE_OUTBOUND", outboundNo, command.operatorId(), command.idempotencyKey());
        return requireFulfillment(fulfillmentNo);
    }

    /**
     * 执行命令 {@code dispatchOutbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code OutboundCommand}
     * @return 执行命令的结果，类型为 {@code FulfillmentMapper.OutboundRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public FulfillmentMapper.OutboundRow dispatchOutbound(String outboundNo, OutboundCommand command) {
        OutboundAggregate outbound = loadOutbound(outboundNo);
        outbound.dispatch();
        FulfillmentAggregate fulfillment = loadFulfillment(outbound.fulfillmentNo());
        fulfillment.markOutboundIssued(outboundNo);
        mapper.updateOutbound(toRow(outbound));
        mapper.updateFulfillment(toRow(fulfillment));
        saveEvents(outbound.pullEvents());
        saveEvents(fulfillment.pullEvents());
        insertCommand("CreateOutboundOrder", "WMS", outboundNo, outboundNo + ":" + outbound.retryCount(), outbound.fulfillmentNo() + "|" + outbound.warehouseId() + "|" + outbound.warehouseCode());
        log("DISPATCH_OUTBOUND", outboundNo, command.operatorId(), command.idempotencyKey());
        return toRow(outbound);
    }

    /**
     * 执行命令 {@code retryOutbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code OutboundCommand}
     * @return 执行命令的结果，类型为 {@code FulfillmentMapper.OutboundRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public FulfillmentMapper.OutboundRow retryOutbound(String outboundNo, OutboundCommand command) {
        OutboundAggregate outbound = loadOutbound(outboundNo);
        outbound.retryDispatch();
        mapper.updateOutbound(toRow(outbound));
        saveEvents(outbound.pullEvents());
        insertCommand("CreateOutboundOrder", "WMS", outboundNo, outboundNo + ":retry:" + outbound.retryCount(), outbound.fulfillmentNo() + "|" + outbound.warehouseId() + "|" + outbound.warehouseCode());
        log("RETRY_OUTBOUND", outboundNo, command.operatorId(), command.idempotencyKey());
        return toRow(outbound);
    }

    /**
     * 执行命令 {@code cancelOutbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code CancelOutboundCommand}
     * @return 执行命令的结果，类型为 {@code FulfillmentMapper.OutboundRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public FulfillmentMapper.OutboundRow cancelOutbound(String outboundNo, CancelOutboundCommand command) {
        OutboundAggregate outbound = loadOutbound(outboundNo);
        outbound.requestCancel(command.reason());
        mapper.updateOutbound(toRow(outbound));
        saveEvents(outbound.pullEvents());
        insertCommand("CancelOutboundOrder", "WMS", outboundNo, outboundNo + ":cancel:" + outbound.version(), outbound.fulfillmentNo() + "|" + command.reason());
        log("CANCEL_OUTBOUND", outboundNo, command.operatorId(), command.idempotencyKey());
        return toRow(outbound);
    }

    /**
     * 执行命令 {@code consumeEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code ExternalEvent}
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("PMD.SwitchStatementRule")
    public void consumeEvent(ExternalEvent event) {
        int claimed = mapper.claimEvent(new FulfillmentMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessNo(), event.payload(), 1, null));
        if (claimed == 0) {
            return;
        }
        try {
            switch(event.eventType()) {
                case "StockReserved" ->
                    recordReservationSuccess(event);
                case "StockReservationFailed" ->
                    recordReservationFailure(event);
                case "StockReleased" ->
                    recordReservationReleased(event);
                case "WmsOutboundAccepted" ->
                    recordWmsAccepted(event);
                case "WmsOutboundShipped" ->
                    recordWmsShipped(event);
                case "WmsOutboundCancelled" ->
                    recordWmsCancelled(event);
                default ->
                    throw new IllegalArgumentException("unsupported OMS event: " + event.eventType());
            }
            mapper.updateEvent(new FulfillmentMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessNo(), event.payload(), 2, null));
        } catch (RuntimeException exception) {
            mapper.updateEvent(new FulfillmentMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessNo(), event.payload(), 3, exception.getMessage()));
            throw exception;
        }
    }

    /**
     * 查询并返回 {@code listFulfillments}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<FulfillmentMapper.FulfillmentRow>}
     */
    public List<FulfillmentMapper.FulfillmentRow> listFulfillments() {
        return mapper.listFulfillments();
    }

    /**
     * 查询并返回 {@code listOutbounds}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<FulfillmentMapper.OutboundRow>}
     */
    public List<FulfillmentMapper.OutboundRow> listOutbounds() {
        return mapper.listOutbounds();
    }

    /**
     * 查询并返回 {@code getFulfillment}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code FulfillmentMapper.FulfillmentRow}
     */
    public FulfillmentMapper.FulfillmentRow getFulfillment(String fulfillmentNo) {
        return requireFulfillment(fulfillmentNo);
    }

    /**
     * 查询并返回 {@code getOutbound}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code FulfillmentMapper.OutboundRow}
     */
    public FulfillmentMapper.OutboundRow getOutbound(String outboundNo) {
        return requireOutbound(outboundNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code recordReservationSuccess}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code ExternalEvent}
     */
    private void recordReservationSuccess(ExternalEvent event) {
        FulfillmentMapper.ReservationRow reservation = requireReservation(event.reservationRefNo());
        FulfillmentAggregate aggregate = loadFulfillment(reservation.fulfillmentNo());
        aggregate.recordReservationSuccess(event.reservationNo(), event.quantity());
        mapper.updateFulfillment(toRow(aggregate));
        mapper.updateReservation(new FulfillmentMapper.ReservationRow(reservation.reservationRefNo(), reservation.fulfillmentNo(), event.reservationNo(), reservation.reserveQty(), event.quantity(), RESERVATION_RESERVED, null, reservation.version() + 1));
        saveEvents(aggregate.pullEvents());
    }

    /**
     * 处理当前类型职责中的操作 {@code recordReservationFailure}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code ExternalEvent}
     */
    private void recordReservationFailure(ExternalEvent event) {
        FulfillmentMapper.ReservationRow reservation = requireReservation(event.reservationRefNo());
        FulfillmentAggregate aggregate = loadFulfillment(reservation.fulfillmentNo());
        aggregate.recordReservationFailure(event.reason());
        mapper.updateFulfillment(toRow(aggregate));
        mapper.updateReservation(new FulfillmentMapper.ReservationRow(reservation.reservationRefNo(), reservation.fulfillmentNo(), reservation.reservationNo(), reservation.reserveQty(), reservation.reservedQty(), RESERVATION_FAILED, event.reason(), reservation.version() + 1));
        saveEvents(aggregate.pullEvents());
    }

    /**
     * 处理当前类型职责中的操作 {@code recordReservationReleased}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code ExternalEvent}
     */
    private void recordReservationReleased(ExternalEvent event) {
        FulfillmentMapper.ReservationRow reservation = requireReservation(event.reservationRefNo());
        mapper.updateReservation(new FulfillmentMapper.ReservationRow(reservation.reservationRefNo(), reservation.fulfillmentNo(), reservation.reservationNo(), reservation.reserveQty(), reservation.reservedQty(), RESERVATION_RELEASED, null, reservation.version() + 1));
    }

    /**
     * 处理当前类型职责中的操作 {@code recordWmsAccepted}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code ExternalEvent}
     */
    private void recordWmsAccepted(ExternalEvent event) {
        OutboundAggregate outbound = loadOutbound(event.outboundNo());
        outbound.markWmsAccepted(event.wmsOrderNo());
        mapper.updateOutbound(toRow(outbound));
        saveEvents(outbound.pullEvents());
    }

    /**
     * 处理当前类型职责中的操作 {@code recordWmsShipped}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code ExternalEvent}
     */
    private void recordWmsShipped(ExternalEvent event) {
        OutboundAggregate outbound = loadOutbound(event.outboundNo());
        outbound.markShipped();
        FulfillmentAggregate fulfillment = loadFulfillment(outbound.fulfillmentNo());
        fulfillment.markWmsShipped();
        mapper.updateOutbound(toRow(outbound));
        mapper.updateFulfillment(toRow(fulfillment));
        saveEvents(outbound.pullEvents());
        saveEvents(fulfillment.pullEvents());
    }

    /**
     * 处理当前类型职责中的操作 {@code recordWmsCancelled}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code ExternalEvent}
     */
    private void recordWmsCancelled(ExternalEvent event) {
        OutboundAggregate outbound = loadOutbound(event.outboundNo());
        outbound.markCancelled();
        FulfillmentAggregate fulfillment = loadFulfillment(outbound.fulfillmentNo());
        fulfillment.markCancelled("WMS 出库已取消");
        mapper.updateOutbound(toRow(outbound));
        mapper.updateFulfillment(toRow(fulfillment));
        saveEvents(outbound.pullEvents());
        saveEvents(fulfillment.pullEvents());
        FulfillmentMapper.ReservationRow reservation = mapper.findReservationByFulfillment(fulfillment.fulfillmentNo());
        if (reservation != null && reservation.status() == RESERVATION_RESERVED) {
            FulfillmentMapper.ReservationRow releaseRequested = new FulfillmentMapper.ReservationRow(reservation.reservationRefNo(), reservation.fulfillmentNo(), reservation.reservationNo(), reservation.reserveQty(), reservation.reservedQty(), RESERVATION_RELEASE_REQUESTED, "WMS 出库取消后释放库存", reservation.version() + 1);
            mapper.updateReservation(releaseRequested);
            insertCommand("ReleaseInventory", "INVENTORY", fulfillment.fulfillmentNo(), reservation.reservationRefNo() + ":WMS-CANCEL", reservation.reservationNo());
        }
    }

    /**
     * 查询并返回 {@code loadFulfillment}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code FulfillmentAggregate}
     */
    private FulfillmentAggregate loadFulfillment(String fulfillmentNo) {
        return fromRow(requireFulfillment(fulfillmentNo));
    }

    /**
     * 查询并返回 {@code loadOutbound}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code OutboundAggregate}
     */
    private OutboundAggregate loadOutbound(String outboundNo) {
        return fromRow(requireOutbound(outboundNo));
    }

    /**
     * 查询并返回 {@code requireFulfillment}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code FulfillmentMapper.FulfillmentRow}
     */
    private FulfillmentMapper.FulfillmentRow requireFulfillment(String fulfillmentNo) {
        FulfillmentMapper.FulfillmentRow row = mapper.findFulfillment(fulfillmentNo);
        if (row == null) {
            throw new IllegalArgumentException("fulfillment not found");
        }
        return row;
    }

    /**
     * 查询并返回 {@code requireReservation}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param reservationRefNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code FulfillmentMapper.ReservationRow}
     */
    private FulfillmentMapper.ReservationRow requireReservation(String reservationRefNo) {
        FulfillmentMapper.ReservationRow row = mapper.findReservation(reservationRefNo);
        if (row == null) {
            throw new IllegalArgumentException("reservation not found");
        }
        return row;
    }

    /**
     * 查询并返回 {@code requireOutbound}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code FulfillmentMapper.OutboundRow}
     */
    private FulfillmentMapper.OutboundRow requireOutbound(String outboundNo) {
        FulfillmentMapper.OutboundRow row = mapper.findOutbound(outboundNo);
        if (row == null) {
            throw new IllegalArgumentException("outbound not found");
        }
        return row;
    }

    /**
     * 转换数据模型 {@code fromRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code FulfillmentMapper.FulfillmentRow}
     * @return 转换数据模型的结果，类型为 {@code FulfillmentAggregate}
     */
    private FulfillmentAggregate fromRow(FulfillmentMapper.FulfillmentRow row) {
        return FulfillmentAggregate.restore(row.fulfillmentNo(), row.salesOrderNo(), row.channelCode(), row.customerId(), row.warehouseId(), row.warehouseCode(), row.logisticsProductCode(), parseLines(row.linePayload()), row.status(), row.reservationRefNo(), row.reservationNo(), row.outboundNo(), row.failureReason(), row.splitReason(), row.version());
    }

    /**
     * 转换数据模型 {@code fromRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code FulfillmentMapper.OutboundRow}
     * @return 转换数据模型的结果，类型为 {@code OutboundAggregate}
     */
    private OutboundAggregate fromRow(FulfillmentMapper.OutboundRow row) {
        return OutboundAggregate.restore(row.outboundNo(), row.fulfillmentNo(), row.salesOrderNo(), row.warehouseId(), row.warehouseCode(), row.wmsOrderNo(), row.status(), row.cancelReason(), row.retryCount(), row.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code FulfillmentAggregate}
     * @return 转换数据模型的结果，类型为 {@code FulfillmentMapper.FulfillmentRow}
     */
    private FulfillmentMapper.FulfillmentRow toRow(FulfillmentAggregate aggregate) {
        return new FulfillmentMapper.FulfillmentRow(aggregate.fulfillmentNo(), aggregate.salesOrderNo(), aggregate.channelCode(), aggregate.customerId(), aggregate.warehouseId(), aggregate.warehouseCode(), aggregate.logisticsProductCode(), formatLines(aggregate.lines()), aggregate.status(), aggregate.reservationRefNo(), aggregate.reservationNo(), aggregate.outboundNo(), aggregate.failureReason(), aggregate.splitReason(), aggregate.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code OutboundAggregate}
     * @return 转换数据模型的结果，类型为 {@code FulfillmentMapper.OutboundRow}
     */
    private FulfillmentMapper.OutboundRow toRow(OutboundAggregate aggregate) {
        return new FulfillmentMapper.OutboundRow(aggregate.outboundNo(), aggregate.fulfillmentNo(), aggregate.salesOrderNo(), aggregate.warehouseId(), aggregate.warehouseCode(), aggregate.wmsOrderNo(), aggregate.status(), aggregate.cancelReason(), aggregate.retryCount(), aggregate.version());
    }

    /**
     * 执行命令 {@code saveEvents}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param events 业务处理参数或成员，类型为 {@code List<OmsEvent>}
     */
    private void saveEvents(List<OmsEvent> events) {
        for (OmsEvent event : events) {
            mapper.insertOutbox(new FulfillmentMapper.OutboxRow(event.eventType(), event.businessNo(), event.payload(), event.occurredAt()));
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code insertCommand}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param commandType 用例输入命令，类型为 {@code String}
     * @param targetSystem 业务处理参数或成员，类型为 {@code String}
     * @param businessNo 可追踪业务编码，类型为 {@code String}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     */
    private void insertCommand(String commandType, String targetSystem, String businessNo, String idempotencyKey, String payload) {
        mapper.insertIntegrationCommand(new FulfillmentMapper.IntegrationCommandRow(commandType, targetSystem, businessNo, idempotencyKey, payload));
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
        mapper.insertOperationLog(new FulfillmentMapper.OperationLogRow(operationType, businessNo, operatorId, idempotencyKey));
    }

    /**
     * 处理当前类型职责中的操作 {@code salesLines}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param payload 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<FulfillmentAggregate.Line>}
     */
    private static List<FulfillmentAggregate.Line> salesLines(String payload) {
        return OmsApplicationService.parseLines(payload).stream().map(line -> new FulfillmentAggregate.Line(line.skuCode(), BigDecimal.valueOf(line.quantity()))).toList();
    }

    /**
     * 处理当前类型职责中的操作 {@code parseLines}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param payload 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<FulfillmentAggregate.Line>}
     */
    private static List<FulfillmentAggregate.Line> parseLines(String payload) {
        if (payload == null || payload.isBlank()) {
            return List.of();
        }
        return List.of(payload.split(";")).stream().map(item -> {
            String[] parts = item.split(":");
            if (parts.length != RESERVATION_RELEASE_REQUESTED) {
                throw new IllegalArgumentException("invalid fulfillment line payload");
            }
            return new FulfillmentAggregate.Line(parts[0], new BigDecimal(parts[1]), new BigDecimal(parts[2]), new BigDecimal(parts[3]));
        }).toList();
    }

    /**
     * 处理当前类型职责中的操作 {@code formatLines}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param lines 业务处理参数或成员，类型为 {@code List<FulfillmentAggregate.Line>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String formatLines(List<FulfillmentAggregate.Line> lines) {
        return lines.stream().map(line -> String.join(":", line.skuCode(), line.quantity().toPlainString(), line.reservedQty().toPlainString(), line.shippedQty().toPlainString())).collect(Collectors.joining(";"));
    }

    /**
     * 转换数据模型 {@code totalQuantity}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param lines 业务处理参数或成员，类型为 {@code List<FulfillmentAggregate.Line>}
     * @return 转换数据模型的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal totalQuantity(List<FulfillmentAggregate.Line> lines) {
        return lines.stream().map(FulfillmentAggregate.Line::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * AllocateCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record AllocateCommand(String salesOrderNo, Long warehouseId, String warehouseCode, String logisticsProductCode, Long operatorId, String idempotencyKey) {
    }

    /**
     * ChangeWarehouseCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ChangeWarehouseCommand(Long warehouseId, String warehouseCode, String reason, Long operatorId, String idempotencyKey) {
    }

    /**
     * SplitCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record SplitCommand(List<FulfillmentAggregate.Line> lines, String reason, Long operatorId, String idempotencyKey) {
    }

    /**
     * ReserveCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ReserveCommand(Long operatorId, String idempotencyKey) {
    }

    /**
     * ReleaseCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ReleaseCommand(String reason, Long operatorId, String idempotencyKey) {
    }

    /**
     * CreateOutboundCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateOutboundCommand(Long operatorId, String idempotencyKey) {
    }

    /**
     * OutboundCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record OutboundCommand(Long operatorId, String idempotencyKey) {
    }

    /**
     * CancelOutboundCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CancelOutboundCommand(String reason, Long operatorId, String idempotencyKey) {
    }

    /**
     * ExternalEvent。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。表达已经发生的业务事实，载荷用于跨事务或跨上下文可靠传播。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ExternalEvent(String eventId, String eventType, String businessNo, String fulfillmentNo, String reservationRefNo, String reservationNo, BigDecimal quantity, String outboundNo, String wmsOrderNo, String reason, String payload) {
    }
}
