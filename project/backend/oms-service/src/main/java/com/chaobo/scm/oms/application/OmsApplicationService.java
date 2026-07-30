package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.domain.OmsEvent;
import com.chaobo.scm.oms.domain.SalesOrderAggregate;
import com.chaobo.scm.oms.infrastructure.persistence.OmsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * OmsApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class OmsApplicationService {

    /**
     * mapper（类型：{@code OmsMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final OmsMapper mapper;

    /**
     * orderSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong orderSequence = new AtomicLong(100000);

    /**
     * 创建 OmsApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code OmsMapper}
     */
    public OmsApplicationService(OmsMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code receiveChannelOrder}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code ReceiveChannelOrder}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OmsMapper.SalesOrderRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public OmsMapper.SalesOrderRow receiveChannelOrder(ReceiveChannelOrder command) {
        OmsMapper.SalesOrderRow existing = mapper.findByChannelOrder(command.channelCode(), command.channelOrderNo());
        if (existing != null) {
            return existing;
        }
        String orderNo = "SO" + orderSequence.incrementAndGet();
        SalesOrderAggregate aggregate = SalesOrderAggregate.create(orderNo, command.channelCode(), command.channelOrderNo(), command.customerId(), command.receiverAddress(), command.lines());
        OmsMapper.SalesOrderRow row = toRow(
                aggregate, command.organizationId(), command.ownerId());
        mapper.insertOrder(row);
        mapper.insertChannelOrder(new OmsMapper.ChannelOrderRow(command.channelCode(), command.channelOrderNo(), orderNo, command.rawPayload(), LocalDateTime.now()));
        saveEvents(aggregate.pullEvents());
        log("RECEIVE_CHANNEL_ORDER", orderNo, command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 处理当前类型职责中的操作 {@code reviewSalesOrder}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ReviewCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OmsMapper.SalesOrderRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public OmsMapper.SalesOrderRow reviewSalesOrder(String orderNo, ReviewCommand command) {
        OmsMapper.SalesOrderRow persisted = requireOrder(orderNo);
        SalesOrderAggregate aggregate = restoreOrder(persisted);
        if (command.approved()) {
            aggregate.approve(command.remark());
            log("APPROVE_SALES_ORDER", orderNo, command.operatorId(), command.idempotencyKey());
        } else {
            aggregate.intercept(command.remark());
            log("INTERCEPT_SALES_ORDER", orderNo, command.operatorId(), command.idempotencyKey());
        }
        OmsMapper.SalesOrderRow row = toRow(
                aggregate, persisted.organizationId(), persisted.ownerId());
        mapper.updateOrder(row);
        saveEvents(aggregate.pullEvents());
        return row;
    }

    /**
     * 查询并返回 {@code listChannelOrders}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<OmsMapper.ChannelOrderRow>}
     */
    public List<OmsMapper.ChannelOrderRow> listChannelOrders() {
        return mapper.listChannelOrders();
    }

    /**
     * 查询并返回 {@code listOrders}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<OmsMapper.SalesOrderRow>}
     */
    public List<OmsMapper.SalesOrderRow> listOrders() {
        return mapper.listOrders();
    }

    /**
     * 查询并返回 {@code getOrder}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code OmsMapper.SalesOrderRow}
     */
    public OmsMapper.SalesOrderRow getOrder(String orderNo) {
        return mapper.findOrder(orderNo);
    }

    /**
     * 查询并返回 {@code listOutbox}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<OmsMapper.OutboxRow>}
     */
    public List<OmsMapper.OutboxRow> listOutbox() {
        return mapper.listOutbox();
    }

    /**
     * 查询并返回 {@code listOperationLogs}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<OmsMapper.OperationLogRow>}
     */
    public List<OmsMapper.OperationLogRow> listOperationLogs() {
        return mapper.listOperationLogs();
    }

    /**
     * 查询并返回 {@code loadOrder}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code SalesOrderAggregate}
     */
    private SalesOrderAggregate loadOrder(String orderNo) {
        return restoreOrder(requireOrder(orderNo));
    }

    private OmsMapper.SalesOrderRow requireOrder(String orderNo) {
        OmsMapper.SalesOrderRow row = mapper.findOrder(orderNo);
        if (row == null) {
            throw new IllegalArgumentException("sales order not found");
        }
        return row;
    }

    private SalesOrderAggregate restoreOrder(OmsMapper.SalesOrderRow row) {
        return SalesOrderAggregate.restore(row.orderNo(), row.channelCode(), row.channelOrderNo(), row.customerId(), row.receiverAddress(), parseLines(row.linePayload()), row.totalAmount(), row.status(), row.reviewRemark(), row.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code SalesOrderAggregate}
     * @return 转换数据模型的结果，类型为 {@code OmsMapper.SalesOrderRow}
     */
    private OmsMapper.SalesOrderRow toRow(
            SalesOrderAggregate aggregate, Long organizationId, Long ownerId) {
        return new OmsMapper.SalesOrderRow(
                null, organizationId, ownerId, aggregate.orderNo(),
                aggregate.channelCode(), aggregate.channelOrderNo(),
                aggregate.customerId(), aggregate.receiverAddress(),
                formatLines(aggregate.lines()), aggregate.totalAmount(),
                aggregate.status(), aggregate.reviewRemark(), aggregate.version());
    }

    /**
     * 执行命令 {@code saveEvents}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param events 业务处理参数或成员，类型为 {@code List<OmsEvent>}
     */
    private void saveEvents(List<OmsEvent> events) {
        for (OmsEvent event : events) {
            mapper.insertOutbox(new OmsMapper.OutboxRow(event.eventType(), event.businessNo(), event.payload(), 1, event.occurredAt()));
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
        mapper.insertOperationLog(new OmsMapper.OperationLogRow(operationType, businessNo, operatorId, idempotencyKey, LocalDateTime.now()));
    }

    /**
     * 处理当前类型职责中的操作 {@code formatLines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param lines 业务处理参数或成员，类型为 {@code List<SalesOrderAggregate.OrderLine>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public static String formatLines(List<SalesOrderAggregate.OrderLine> lines) {
        return lines.stream().map(line -> String.join(":", line.skuCode(), Integer.toString(line.quantity()), line.unitPrice().toPlainString())).collect(Collectors.joining(";"));
    }

    /**
     * 处理当前类型职责中的操作 {@code parseLines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param payload 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<SalesOrderAggregate.OrderLine>}
     */
    public static List<SalesOrderAggregate.OrderLine> parseLines(String payload) {
        if (payload == null || payload.isBlank()) {
            return List.of();
        }
        return List.of(payload.split(";")).stream().map(item -> {
            String[] parts = item.split(":");
            if (parts.length != PARSE_LINES_VALUE_3) {
                throw new IllegalArgumentException("invalid line payload");
            }
            return new SalesOrderAggregate.OrderLine(parts[0], Integer.parseInt(parts[1]), new BigDecimal(parts[2]));
        }).toList();
    }

    /**
     * ReceiveChannelOrder。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ReceiveChannelOrder(
            String channelCode,
            String channelOrderNo,
            Long customerId,
            String receiverAddress,
            List<SalesOrderAggregate.OrderLine> lines,
            String rawPayload,
            Long operatorId,
            String idempotencyKey,
            Long organizationId,
            Long ownerId) {

        public ReceiveChannelOrder(
                String channelCode,
                String channelOrderNo,
                Long customerId,
                String receiverAddress,
                List<SalesOrderAggregate.OrderLine> lines,
                String rawPayload,
                Long operatorId,
                String idempotencyKey) {
            this(channelCode, channelOrderNo, customerId, receiverAddress, lines,
                    rawPayload, operatorId, idempotencyKey, null, null);
        }
    }

    /**
     * ReviewCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ReviewCommand(boolean approved, String remark, Long operatorId, String idempotencyKey) {
    }

    /**
     * 业务常量 {@code PARSE_LINES_VALUE_3}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PARSE_LINES_VALUE_3 = 3;
}
