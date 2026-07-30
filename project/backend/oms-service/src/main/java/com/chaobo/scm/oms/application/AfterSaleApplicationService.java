package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.domain.AfterSaleAggregate;
import com.chaobo.scm.oms.domain.OmsEvent;
import com.chaobo.scm.oms.infrastructure.persistence.CancellationMapper;
import com.chaobo.scm.oms.infrastructure.persistence.OmsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AfterSaleApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class AfterSaleApplicationService {

    /**
     * mapper（类型：{@code CancellationMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final CancellationMapper mapper;

    /**
     * omsMapper（类型：{@code OmsMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final OmsMapper omsMapper;

    /**
     * sequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong sequence = new AtomicLong(600000);

    /**
     * 创建 AfterSaleApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code CancellationMapper}
     * @param omsMapper 持久化访问依赖，类型为 {@code OmsMapper}
     */
    public AfterSaleApplicationService(CancellationMapper mapper, OmsMapper omsMapper) {
        this.mapper = mapper;
        this.omsMapper = omsMapper;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CreateCommand}
     * @return 执行命令的结果，类型为 {@code CancellationMapper.AfterSaleRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public CancellationMapper.AfterSaleRow create(CreateCommand command) {
        OmsMapper.SalesOrderRow order = omsMapper.findOrder(command.salesOrderNo());
        if (order == null) {
            throw new IllegalArgumentException("sales order not found");
        }
        if (command.refundAmount() == null || command.refundAmount().compareTo(order.totalAmount()) > 0) {
            throw new IllegalArgumentException("refund amount exceeds order amount");
        }
        CancellationMapper.AfterSaleRow existing = mapper.findAfterSaleByOrder(command.salesOrderNo());
        if (existing != null) {
            return existing;
        }
        String afterSaleNo = "AS" + sequence.incrementAndGet();
        AfterSaleAggregate aggregate = AfterSaleAggregate.create(afterSaleNo, command.salesOrderNo(), command.fulfillmentNo(), command.refundAmount(), command.reason());
        mapper.insertAfterSale(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("CREATE_AFTER_SALE", afterSaleNo, command.operatorId(), command.idempotencyKey());
        return mapper.findAfterSale(afterSaleNo);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param afterSaleNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ApproveCommand}
     * @return 执行命令的结果，类型为 {@code CancellationMapper.AfterSaleRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public CancellationMapper.AfterSaleRow approve(String afterSaleNo, ApproveCommand command) {
        AfterSaleAggregate aggregate = load(afterSaleNo);
        aggregate.approve(command.remark());
        mapper.updateAfterSale(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("APPROVE_AFTER_SALE", afterSaleNo, command.operatorId(), command.idempotencyKey());
        return mapper.findAfterSale(afterSaleNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code requestRefund}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param afterSaleNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code RefundCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CancellationMapper.AfterSaleRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public CancellationMapper.AfterSaleRow requestRefund(String afterSaleNo, RefundCommand command) {
        AfterSaleAggregate aggregate = load(afterSaleNo);
        aggregate.requestRefund();
        mapper.updateAfterSale(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        mapper.insertIntegrationCommand(new CancellationMapper.IntegrationCommandRow("RequestRefund", "BMS", afterSaleNo, afterSaleNo + ":REFUND:" + aggregate.version(), aggregate.refundAmount().toPlainString()));
        log("REQUEST_REFUND", afterSaleNo, command.operatorId(), command.idempotencyKey());
        return mapper.findAfterSale(afterSaleNo);
    }

    /**
     * 执行命令 {@code consumeEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code RefundEvent}
     */
    @Transactional(rollbackFor = Exception.class)
    public void consumeEvent(RefundEvent event) {
        int claimed = mapper.claimEvent(new CancellationMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessNo(), event.payload(), 1, null));
        if (claimed == 0) {
            return;
        }
        try {
            AfterSaleAggregate aggregate = load(event.afterSaleNo());
            aggregate.markRefunded(event.refundedAmount());
            mapper.updateAfterSale(toRow(aggregate));
            saveEvents(aggregate.pullEvents());
            mapper.updateEvent(new CancellationMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessNo(), event.payload(), 2, null));
        } catch (RuntimeException exception) {
            mapper.updateEvent(new CancellationMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessNo(), event.payload(), 3, exception.getMessage()));
            throw exception;
        }
    }

    /**
     * 执行命令 {@code complete}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param afterSaleNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code CompleteCommand}
     * @return 执行命令的结果，类型为 {@code CancellationMapper.AfterSaleRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public CancellationMapper.AfterSaleRow complete(String afterSaleNo, CompleteCommand command) {
        AfterSaleAggregate aggregate = load(afterSaleNo);
        aggregate.complete();
        mapper.updateAfterSale(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("COMPLETE_AFTER_SALE", afterSaleNo, command.operatorId(), command.idempotencyKey());
        return mapper.findAfterSale(afterSaleNo);
    }

    /**
     * 查询并返回 {@code get}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param afterSaleNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code CancellationMapper.AfterSaleRow}
     */
    public CancellationMapper.AfterSaleRow get(String afterSaleNo) {
        return mapper.findAfterSale(afterSaleNo);
    }

    /**
     * 查询并返回 {@code load}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param afterSaleNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code AfterSaleAggregate}
     */
    private AfterSaleAggregate load(String afterSaleNo) {
        CancellationMapper.AfterSaleRow row = mapper.findAfterSale(afterSaleNo);
        if (row == null) {
            throw new IllegalArgumentException("after-sale not found");
        }
        return AfterSaleAggregate.restore(row.afterSaleNo(), row.salesOrderNo(), row.fulfillmentNo(), row.refundAmount(), row.reason(), row.status(), row.refundedAmount(), row.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code AfterSaleAggregate}
     * @return 转换数据模型的结果，类型为 {@code CancellationMapper.AfterSaleRow}
     */
    private CancellationMapper.AfterSaleRow toRow(AfterSaleAggregate aggregate) {
        return new CancellationMapper.AfterSaleRow(aggregate.afterSaleNo(), aggregate.salesOrderNo(), aggregate.fulfillmentNo(), aggregate.refundAmount(), aggregate.refundedAmount(), aggregate.reason(), aggregate.status(), aggregate.version());
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
     * CreateCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateCommand(String salesOrderNo, String fulfillmentNo, BigDecimal refundAmount, String reason, Long operatorId, String idempotencyKey) {
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
     * RefundCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record RefundCommand(Long operatorId, String idempotencyKey) {
    }

    /**
     * CompleteCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CompleteCommand(Long operatorId, String idempotencyKey) {
    }

    /**
     * RefundEvent。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。表达已经发生的业务事实，载荷用于跨事务或跨上下文可靠传播。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record RefundEvent(String eventId, String eventType, String businessNo, String afterSaleNo, BigDecimal refundedAmount, String payload) {
    }
}
