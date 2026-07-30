package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.domain.OmsEvent;
import com.chaobo.scm.oms.domain.ReverseAfterSaleAggregate;
import com.chaobo.scm.oms.infrastructure.persistence.OmsMapper;
import com.chaobo.scm.oms.infrastructure.persistence.ReverseAfterSaleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ReverseAfterSaleApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class ReverseAfterSaleApplicationService {

    /**
     * mapper（类型：{@code ReverseAfterSaleMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final ReverseAfterSaleMapper mapper;

    /**
     * orders（类型：{@code OmsMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final OmsMapper orders;

    /**
     * ids（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    /**
     * 创建 ReverseAfterSaleApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code ReverseAfterSaleMapper}
     * @param orders 业务处理参数或成员，类型为 {@code OmsMapper}
     */
    public ReverseAfterSaleApplicationService(ReverseAfterSaleMapper mapper, OmsMapper orders) {
        this.mapper = mapper;
        this.orders = orders;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code Create}
     * @return 执行命令的结果，类型为 {@code ReverseAfterSaleMapper.Row}
     */
    @Transactional(rollbackFor = Exception.class)
    public ReverseAfterSaleMapper.Row create(Create command) {
        var order = orders.findOrder(command.salesOrderNo());
        if (order == null) {
            throw new IllegalArgumentException("sales order not found");
        }
        var line = orderLine(order.linePayload(), command.sku());
        if (command.applyQty() == null || command.applyQty().signum() <= 0 || command.applyQty().compareTo(line.quantity()) > 0) {
            throw new IllegalArgumentException("after-sale quantity exceeds fulfilled quantity");
        }
        BigDecimal maxRefund = line.unitPrice().multiply(command.applyQty());
        if (command.refundAmount() == null || command.refundAmount().signum() < 0 || command.refundAmount().compareTo(maxRefund) > 0) {
            throw new IllegalArgumentException("refund amount exceeds refundable amount");
        }
        var existing = mapper.findActive(command.salesOrderNo(), command.sku());
        if (existing != null) {
            return existing;
        }
        String no = "RAS" + ids.incrementAndGet();
        var aggregate = ReverseAfterSaleAggregate.create(no, command.type(), command.salesOrderNo(), command.fulfillmentNo(), command.ownerId(), command.sku(), command.applyQty(), command.refundAmount(), command.returnWarehouseId(), command.reason());
        mapper.insert(row(aggregate));
        saveEvents(aggregate.pullEvents());
        return mapper.find(no);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code ReverseAfterSaleMapper.Row}
     */
    @Transactional(rollbackFor = Exception.class)
    public ReverseAfterSaleMapper.Row approve(String no, long version) {
        var aggregate = load(no);
        long oldVersion = aggregate.version();
        String rma = requiresReturn(aggregate.type()) ? "RMA" + ids.incrementAndGet() : null;
        aggregate.approve(rma, version);
        save(aggregate, oldVersion);
        if (requiresReturn(aggregate.type())) {
            String payload = payload(aggregate);
            command("CreateReturnTransportRequested", "TMS", aggregate, payload);
            command("CreateReturnInboundRequested", "WMS", aggregate, payload);
        }
        saveEvents(aggregate.pullEvents());
        return mapper.find(no);
    }

    /**
     * 处理当前类型职责中的操作 {@code requestRefund}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ReverseAfterSaleMapper.Row}
     */
    @Transactional(rollbackFor = Exception.class)
    public ReverseAfterSaleMapper.Row requestRefund(String no, long version) {
        var aggregate = load(no);
        long oldVersion = aggregate.version();
        aggregate.requestRefund(version);
        save(aggregate, oldVersion);
        command("RequestRefund", "BMS", aggregate, "{\"afterSaleNo\":\"" + no + "\",\"amount\":" + aggregate.refundAmount() + "}");
        saveEvents(aggregate.pullEvents());
        return mapper.find(no);
    }

    /**
     * 处理当前类型职责中的操作 {@code requestReship}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ReverseAfterSaleMapper.Row}
     */
    @Transactional(rollbackFor = Exception.class)
    public ReverseAfterSaleMapper.Row requestReship(String no, long version) {
        var aggregate = load(no);
        long oldVersion = aggregate.version();
        aggregate.requestReship(version);
        save(aggregate, oldVersion);
        command("CreateReshipFulfillmentRequested", "OMS", aggregate, payload(aggregate));
        saveEvents(aggregate.pullEvents());
        return mapper.find(no);
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code Event}
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("PMD.SwitchStatementRule")
    public void consume(Event event) {
        if (mapper.claimEvent(event.eventId(), event.eventType(), event.afterSaleNo(), event.payload()) == 0) {
            return;
        }
        try {
            var aggregate = load(event.afterSaleNo());
            long oldVersion = aggregate.version();
            switch(event.eventType()) {
                case "ReturnReceived" ->
                    aggregate.markReturnReceived(oldVersion);
                case "ReturnInspected" ->
                    aggregate.inspect(event.receivedQty(), event.acceptedQty(), event.unmatched(), oldVersion);
                case "RefundCompleted" ->
                    aggregate.markRefunded(event.amount(), oldVersion);
                case "ReshipFulfillmentCreated" ->
                    aggregate.markReshipCreated(oldVersion);
                default ->
                    throw new IllegalArgumentException("unsupported reverse after-sale event");
            }
            save(aggregate, oldVersion);
            saveEvents(aggregate.pullEvents());
            mapper.finishEvent(event.eventId(), 2, null);
        } catch (RuntimeException exception) {
            mapper.finishEvent(event.eventId(), 3, exception.getMessage());
            throw exception;
        }
    }

    /**
     * 查询并返回 {@code get}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ReverseAfterSaleMapper.Row}
     */
    public ReverseAfterSaleMapper.Row get(String no) {
        return mapper.find(no);
    }

    /**
     * 查询并返回 {@code load}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ReverseAfterSaleAggregate}
     */
    private ReverseAfterSaleAggregate load(String no) {
        var r = mapper.find(no);
        if (r == null) {
            throw new IllegalArgumentException("reverse after-sale not found");
        }
        return ReverseAfterSaleAggregate.restore(r.afterSaleNo(), ReverseAfterSaleAggregate.Type.valueOf(r.type()), r.salesOrderNo(), r.fulfillmentNo(), r.ownerId(), r.sku(), r.applyQty(), r.refundAmount(), r.returnWarehouseId(), r.reason(), r.rmaNo(), r.receivedQty(), r.acceptedQty(), r.refundedAmount(), r.status(), r.version());
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code ReverseAfterSaleAggregate}
     * @param oldVersion 乐观锁或契约版本，类型为 {@code long}
     */
    private void save(ReverseAfterSaleAggregate aggregate, long oldVersion) {
        if (mapper.update(row(aggregate), oldVersion) != 1) {
            throw new IllegalStateException("reverse after-sale persistence version conflict");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code command}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param target 业务处理参数或成员，类型为 {@code String}
     * @param aggregate 业务处理参数或成员，类型为 {@code ReverseAfterSaleAggregate}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     */
    private void command(String type, String target, ReverseAfterSaleAggregate aggregate, String payload) {
        mapper.insertCommand(type, target, aggregate.afterSaleNo(), aggregate.afterSaleNo() + ":" + type + ":" + aggregate.version(), payload);
    }

    /**
     * 执行命令 {@code saveEvents}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param events 业务处理参数或成员，类型为 {@code List<OmsEvent>}
     */
    private void saveEvents(List<OmsEvent> events) {
        for (var event : events) {
            mapper.insertOutbox(event.eventType(), event.businessNo(), event.payload(), event.occurredAt());
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code row}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code ReverseAfterSaleAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ReverseAfterSaleMapper.Row}
     */
    private static ReverseAfterSaleMapper.Row row(ReverseAfterSaleAggregate a) {
        return new ReverseAfterSaleMapper.Row(a.afterSaleNo(), a.type().name(), a.salesOrderNo(), a.fulfillmentNo(), a.ownerId(), a.sku(), a.applyQty(), a.refundAmount(), a.returnWarehouseId(), a.reason(), a.rmaNo(), a.receivedQty(), a.acceptedQty(), a.refundedAmount(), a.status(), a.version());
    }

    /**
     * 查询并返回 {@code requiresReturn}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param type 业务处理参数或成员，类型为 {@code ReverseAfterSaleAggregate.Type}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    private static boolean requiresReturn(ReverseAfterSaleAggregate.Type type) {
        return type == ReverseAfterSaleAggregate.Type.RETURN_REFUND || type == ReverseAfterSaleAggregate.Type.EXCHANGE;
    }

    /**
     * 处理当前类型职责中的操作 {@code orderLine}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param payload 业务处理参数或成员，类型为 {@code String}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Line}
     */
    private static Line orderLine(String payload, String sku) {
        if (payload == null) {
            throw new IllegalArgumentException("sales order lines are missing");
        }
        for (String item : payload.split(SEMICOLON_SEPARATOR)) {
            String[] parts = item.split(":", -1);
            if (parts.length >= 3 && parts[0].equals(sku)) {
                return new Line(new BigDecimal(parts[1]), new BigDecimal(parts[2]));
            }
        }
        throw new IllegalArgumentException("sales order line not found");
    }

    /**
     * 处理当前类型职责中的操作 {@code payload}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code ReverseAfterSaleAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String payload(ReverseAfterSaleAggregate a) {
        return "{\"afterSaleNo\":\"" + a.afterSaleNo() + "\",\"rmaNo\":" + (a.rmaNo() == null ? "null" : "\"" + a.rmaNo() + "\"") + ",\"salesOrderNo\":\"" + a.salesOrderNo() + "\",\"ownerId\":" + a.ownerId() + ",\"sku\":\"" + a.sku() + "\",\"qty\":" + a.applyQty() + ",\"returnWarehouseId\":" + a.returnWarehouseId() + "}";
    }

    /**
     * Line。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record Line(BigDecimal quantity, BigDecimal unitPrice) {
    }

    /**
     * Create。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Create(ReverseAfterSaleAggregate.Type type, String salesOrderNo, String fulfillmentNo, long ownerId, String sku, BigDecimal applyQty, BigDecimal refundAmount, long returnWarehouseId, String reason) {
    }

    /**
     * Event。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。表达已经发生的业务事实，载荷用于跨事务或跨上下文可靠传播。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Event(String eventId, String eventType, String afterSaleNo, BigDecimal receivedQty, BigDecimal acceptedQty, BigDecimal amount, boolean unmatched, String payload) {
    }

    /**
     * 业务常量 {@code SEMICOLON_SEPARATOR}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String SEMICOLON_SEPARATOR = ";";
}
